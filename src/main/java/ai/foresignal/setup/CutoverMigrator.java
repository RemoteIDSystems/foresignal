package ai.foresignal.setup;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserStatus;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The one-time global-to-shared/admin move of the v2.0 cutover (prompt R2-027; {@code V26__v2_cutover.sql}).
 *
 * <p>Owner-less configuration is left exactly as it is: a null owner already means shared/global (source, collector
 * and server settings, global channels, shared LLM providers, and every rule and view created before R2). What moves
 * to the first admin is what the single operator of a v1 instance owned through the instance {@code owner} principal
 * (the bootstrap admin token's principal): rows whose {@code owner_principal_id} is that principal in
 * {@code trigger_rule} (and its {@code alert} rows), {@code notification_channel}, {@code llm_provider} and
 * {@code saved_view}, and the {@code workspace} rows it owns with their sharing records. API tokens are not touched,
 * so scoped machine tokens keep working; the bootstrap token keeps its principal.
 *
 * <p>The move runs once: {@link #migrate} records it in {@code v2_cutover}, and a second call returns the recorded
 * result. {@code runSetup} calls it for the admin it creates; at startup, an instance that already has an active
 * Admin (users made before setup existed, or an upgrade whose admin was created otherwise) is migrated to the
 * earliest-created one, unless {@code foresignal.cutover.auto-migrate} is false. A fresh instance with no user records
 * nothing until setup.
 */
@Service
public class CutoverMigrator {

    private static final Logger log = LoggerFactory.getLogger(CutoverMigrator.class);

    /** {@code "CUTOVER1"}: the transaction-scoped advisory lock serializing the move. */
    static final long LOCK_KEY = 0x4355544F56455231L;

    /**
     * What a cutover did.
     *
     * @param adminUserId the admin the rows moved to
     * @param fromPrincipalId the principal they moved from, or null when there was no {@code owner} principal
     * @param migratedAt when it ran
     * @param counts rows moved per kind
     * @param alreadyDone true when this call found an earlier cutover and moved nothing
     */
    public record Result(UUID adminUserId, String fromPrincipalId, Instant migratedAt, Map<String, Integer> counts,
            boolean alreadyDone) {}

    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final PrincipalRepository principals;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate tx;
    private final JsonMapper json;
    private final Clock clock;
    private final boolean autoMigrate;

    /**
     * Creates the migrator.
     *
     * @param jdbc the template
     * @param users the users
     * @param principals the principals, for the {@code owner} principal
     * @param events the event publisher, so the trigger engine reloads moved rules
     * @param transactions the transaction manager, for the startup run
     * @param json the mapper, for the recorded counts
     * @param clock the clock
     * @param autoMigrate whether to migrate at startup when an admin exists
     */
    public CutoverMigrator(JdbcTemplate jdbc, UserRepository users, PrincipalRepository principals,
            ApplicationEventPublisher events, PlatformTransactionManager transactions, JsonMapper json, Clock clock,
            @Value("${foresignal.cutover.auto-migrate:true}") boolean autoMigrate) {
        this.jdbc = jdbc;
        this.users = users;
        this.principals = principals;
        this.events = events;
        this.tx = new TransactionTemplate(transactions);
        this.json = json;
        this.clock = clock;
        this.autoMigrate = autoMigrate;
    }

    /**
     * The recorded cutover, if one ran.
     *
     * @return the result, with {@code alreadyDone} true
     */
    public Optional<Result> recorded() {
        List<Result> rows = jdbc.query("SELECT admin_user_id, from_principal_id, migrated_at, counts::text"
                + " FROM v2_cutover WHERE id = 1", (rs, n) -> new Result(rs.getObject(1, UUID.class), rs.getString(2),
                        rs.getTimestamp(3).toInstant(), counts(rs.getString(4)), true));
        return rows.stream().findFirst();
    }

    /**
     * Moves what the v1 operator owned to {@code adminUserId}, once. Runs in the caller's transaction and holds a
     * transaction-scoped advisory lock, so two concurrent calls cannot both move rows.
     *
     * @param adminUserId the first admin
     * @return what moved, or the earlier result when the cutover already ran
     */
    public Result migrate(UUID adminUserId) {
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class, LOCK_KEY);
        Optional<Result> earlier = recorded();
        if (earlier.isPresent()) {
            return earlier.get();
        }
        String to = users.findById(adminUserId).map(UserEntity::getPrincipalId)
                .orElseThrow(() -> new IllegalArgumentException("No user " + adminUserId));
        String from = principals.findByHandle(PrincipalEntity.OWNER_HANDLE).map(PrincipalEntity::getId).orElse(null);
        Instant now = clock.instant();
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (from != null && !from.equals(to)) {
            List<UUID> rules = jdbc.queryForList("SELECT id FROM trigger_rule WHERE owner_principal_id = ?",
                    UUID.class, from);
            counts.put("triggerRules", jdbc.update("UPDATE trigger_rule SET owner_principal_id = ?, updated_at = ?"
                    + " WHERE owner_principal_id = ?", to, Timestamp.from(now), from));
            counts.put("alerts", jdbc.update("UPDATE alert SET owner_principal_id = ? WHERE owner_principal_id = ?",
                    to, from));
            counts.put("channels", jdbc.update("UPDATE notification_channel SET owner_principal_id = ?,"
                    + " updated_at = ? WHERE owner_principal_id = ?", to, Timestamp.from(now), from));
            counts.put("llmProviders", jdbc.update("UPDATE llm_provider SET owner_principal_id = ?"
                    + " WHERE owner_principal_id = ?", to, from));
            counts.put("savedViews", jdbc.update("UPDATE saved_view SET owner_principal_id = ?, updated_at = ?"
                    + " WHERE owner_principal_id = ?", to, Timestamp.from(now), from));
            counts.put("workspaces", jdbc.update("UPDATE workspace SET owner = ?, updated_at = ? WHERE owner = ?",
                    to, Timestamp.from(now), from));
            jdbc.update("UPDATE shareable_object SET owner = ?, updated_at = ? WHERE owner = ? AND object_ref LIKE ?",
                    to, Timestamp.from(now), from, "workspace:%");
            reloadRules(rules);
        }
        jdbc.update("INSERT INTO v2_cutover (id, admin_user_id, from_principal_id, migrated_at, counts)"
                + " VALUES (1, ?, ?, ?, ?::jsonb)", adminUserId, from, Timestamp.from(now),
                json.writeValueAsString(counts));
        log.info("v2.0 cutover: moved the v1 operator's configuration to the first admin {}: {}", adminUserId, counts);
        return new Result(adminUserId, from, now, counts, false);
    }

    /**
     * At startup, migrates an instance that already has an active Admin but no recorded cutover.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!autoMigrate) {
            return;
        }
        try {
            tx.executeWithoutResult(s -> {
                if (recorded().isPresent()) {
                    return;
                }
                firstAdmin().ifPresent(this::migrate);
            });
        } catch (RuntimeException e) {
            log.warn("The v2.0 cutover could not run at startup; it will be tried again at the next start", e);
        }
    }

    /** The earliest-created active user holding the built-in Admin role. */
    Optional<UUID> firstAdmin() {
        List<UUID> ids = jdbc.queryForList("SELECT u.id FROM \"user\" u JOIN user_role r ON r.user_id = u.id"
                + " WHERE r.role_id = ? AND u.status = ? ORDER BY u.created_at, u.id LIMIT 1", UUID.class,
                BuiltInRole.ADMIN.id(), UserStatus.ACTIVE.wireValue());
        return ids.stream().findFirst();
    }

    private void reloadRules(List<UUID> ids) {
        if (ids.isEmpty()) {
            return;
        }
        Runnable publish = () -> ids.forEach(id -> events.publishEvent(new TriggerRulesChangedEvent(id)));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    private Map<String, Integer> counts(String text) {
        Map<String, Integer> out = new LinkedHashMap<>();
        json.readTree(text).properties().forEach(e -> out.put(e.getKey(), e.getValue().asInt()));
        return out;
    }
}
