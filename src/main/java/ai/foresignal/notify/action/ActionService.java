package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.Durations;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Notification action capabilities (prompt 033): minting, the confirm view and single-use execution.
 *
 * <ul>
 *   <li>{@link #mint} creates one capability per action when a firing alert is rendered for a
 *       delivery (or for a calling device, {@code GET /v1/notifications/{id}}), valid 12 h by default
 *       and at most 24 h.
 *   <li>{@link #peek} backs the confirm page: it verifies the MAC before any lookup and never acts.
 *   <li>{@link #execute} runs in one transaction: it locks the capability row
 *       ({@code SELECT … FOR UPDATE}), refuses an unknown, expired or used one ({@code 410
 *       ACTION_EXPIRED}), marks it used, then calls the alert lifecycle of 017: ack
 *       ({@code acknowledged_by = action:<channelType>:<channelName>}), snooze (the alert snooze) or
 *       mute (the rule's {@code muted_until}). An alert already acknowledged or resolved returns
 *       {@code already-<state>}. Every execution, refused ones included, is audited with actor kind
 *       {@value #ACTOR_KIND} and the token redacted from the path.
 *   <li>{@link #supersede} marks the open capabilities of an alert used ({@code superseded}) when the
 *       alert is acknowledged or resolved by any path ({@link ActionRevoker}).
 * </ul>
 */
@Service
public class ActionService {

    /** The audit actor kind of an action execution. */
    public static final String ACTOR_KIND = "action";

    /** The result of a capability revoked because the alert moved on. */
    public static final String SUPERSEDED = "superseded";

    /** The path prefix of capability URLs. */
    public static final String PATH = "/v1/actions/";

    /**
     * How a capability was used.
     *
     * @param kind {@code http}, {@code telegram}, … (stored in {@code used_via})
     * @param who a display name (a Telegram first name), or null
     * @param remoteIp the client address, or null
     */
    public record Via(String kind, String who, String remoteIp) {

        /** The {@code used_via} text. */
        String text() {
            String out = who == null || who.isBlank() ? kind : kind + ":" + who;
            return out.length() > 200 ? out.substring(0, 200) : out;
        }
    }

    /**
     * The result of an execution ({@code ActionResult}).
     *
     * @param action {@code ack}, {@code snooze} or {@code mute}
     * @param alertId the alert
     * @param result {@code acknowledged}, {@code snoozed}, {@code muted}, {@code already-acknowledged},
     *     {@code already-resolved} or {@code rule-deleted}
     * @param deliveryId the delivery the capability was rendered into, or null
     * @param channelId its channel, or null
     */
    public record Result(String action, long alertId, String result, Long deliveryId, UUID channelId) {}

    /**
     * What the confirm page shows.
     *
     * @param alert the alert
     * @param action the action
     * @param duration the snooze or mute duration, or null
     * @param expiresAt when the capability expires
     */
    public record Confirm(AlertView alert, String action, Duration duration, Instant expiresAt) {}

    private record Row(long id, long alertId, String action, Map<String, Object> params, UUID channelId,
            Long deliveryId, String deviceId, Instant expiresAt, Instant usedAt) {}

    private final ActionKeyService keys;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final AlertStore alerts;
    private final AlertService alertService;
    private final TriggerRuleService rules;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final AuditService audit;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public ActionService(ActionKeyService keys, NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactions,
            AlertStore alerts, AlertService alertService, TriggerRuleService rules,
            NotificationChannelRepository channels, ChannelService channelService, AuditService audit, JsonMapper json,
            Clock clock) {
        this.keys = keys;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.alerts = alerts;
        this.alertService = alertService;
        this.rules = rules;
        this.channels = channels;
        this.channelService = channelService;
        this.audit = audit;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- mint

    /**
     * Mints one capability per action for a delivery of {@code alert} (prompt 033's
     * {@code mint(alert, channel, delivery, actions)}), valid {@link ActionDurations#TTL_DEFAULT}.
     *
     * @param channelId the channel, or null for a device
     * @param deliveryId the delivery, or null
     * @param deviceKey the device the capabilities are for, or null
     * @param actions {@code ack}, {@code snooze}, {@code mute}, in button order
     * @return the rendered actions, in the same order; empty when the alert is not firing
     */
    public List<NotificationAction> mint(AlertView alert, UUID channelId, Long deliveryId, String deviceKey,
            List<String> actions) {
        return mint(alert, channelId, deliveryId, deviceKey, actions, ActionDurations.TTL_DEFAULT);
    }

    /** {@link #mint(AlertView, UUID, Long, String, List)} with a validity of {@code ttl} (at most 24 h). */
    public List<NotificationAction> mint(AlertView alert, UUID channelId, Long deliveryId, String deviceKey,
            List<String> actions, Duration ttl) {
        if (alert == null || !AlertState.FIRING.wire().equals(alert.state()) || actions == null || actions.isEmpty()) {
            return List.of();
        }
        Duration validity = ttl == null || ttl.isNegative() || ttl.isZero() ? ActionDurations.TTL_DEFAULT
                : ttl.compareTo(ActionDurations.TTL_MAX) > 0 ? ActionDurations.TTL_MAX : ttl;
        Instant now = clock.instant();
        Instant expires = now.plus(validity);
        byte[] key = keys.key();
        String base = baseUrl();
        List<NotificationAction> out = new ArrayList<>();
        for (String action : actions) {
            if (!ActionsConfig.SUPPORTED.contains(action)
                    || (NotificationAction.MUTE.equals(action) && alert.ruleId() == null)) {
                continue;
            }
            Duration duration = ActionDurations.defaultFor(action);
            Map<String, Object> params = new LinkedHashMap<>();
            if (duration != null) {
                params.put("duration", Durations.format(duration));
            }
            String token = ActionToken.mint(key);
            jdbc.update("""
                    insert into action_capability (token_hash, alert_id, action, params, channel_id, delivery_id,
                                                   device_id, created_at, expires_at)
                    values (:hash, :alert, :action, CAST(:params AS jsonb), :channel, :delivery, :device, :now, :expires)
                    """, new MapSqlParameterSource("hash", ActionToken.hash(token))
                    .addValue("alert", alert.id())
                    .addValue("action", action)
                    .addValue("params", json.writeValueAsString(params))
                    .addValue("channel", channelId)
                    .addValue("delivery", deliveryId)
                    .addValue("device", deviceKey)
                    .addValue("now", Timestamp.from(now))
                    .addValue("expires", Timestamp.from(expires)));
            out.add(new NotificationAction(action, ActionDurations.label(action, duration), base + PATH + token, token,
                    duration == null ? null : Durations.format(duration), expires));
        }
        return out;
    }

    // ------------------------------------------------------------------------- confirm

    /**
     * The confirm view of {@code token}; never acts.
     *
     * @throws ActionExpiredException when the token is forged, unknown, expired or used, or its alert
     *     is gone
     */
    public Confirm peek(String token) {
        if (!ActionToken.verify(keys.key(), token)) {
            throw new ActionExpiredException();
        }
        List<Row> rows = jdbc.query(SELECT, new MapSqlParameterSource("hash", ActionToken.hash(token)), this::row);
        Instant now = clock.instant();
        if (rows.isEmpty() || !usable(rows.getFirst(), now)) {
            throw new ActionExpiredException();
        }
        Row row = rows.getFirst();
        AlertView alert = alerts.find(row.alertId()).orElseThrow(ActionExpiredException::new);
        return new Confirm(alert, row.action(), ActionDurations.parse(row.action(), row.params().get("duration")),
                row.expiresAt());
    }

    // ------------------------------------------------------------------------- execute

    /**
     * Executes {@code token} once, in one transaction, and audits it.
     *
     * @throws ActionExpiredException when the token is forged, unknown, expired or used
     */
    public Result execute(String token, Via via) {
        Instant now = clock.instant();
        if (!ActionToken.verify(keys.key(), token)) {
            audit(now, via, null, null, 410);
            throw new ActionExpiredException();
        }
        Result result;
        try {
            result = tx.execute(status -> executeLocked(token, via, now));
        } catch (ActionExpiredException e) {
            audit(now, via, null, null, 410);
            throw e;
        }
        audit(now, via, result, null, 200);
        return result;
    }

    private Result executeLocked(String token, Via via, Instant now) {
        List<Row> rows = jdbc.query(SELECT + " for update", new MapSqlParameterSource("hash", ActionToken.hash(token)),
                this::row);
        if (rows.isEmpty() || !usable(rows.getFirst(), now)) {
            throw new ActionExpiredException();
        }
        Row row = rows.getFirst();
        Optional<AlertView> found = alerts.find(row.alertId());
        if (found.isEmpty()) {
            markUsed(row.id(), now, via, "alert-deleted");
            throw new ActionExpiredException();
        }
        AlertView alert = found.get();
        String outcome;
        String feedback = NotificationAction.feedbackOf(row.action());
        if (feedback != null) {
            // Prompt 115: feedback is recorded in any state and does not supersede the other capabilities.
            markUsed(row.id(), now, via, "executing");
            alertService.setFeedback(alert.id(), new AlertService.Transition(null, feedback));
            outcome = "feedback-" + feedback;
            jdbc.update("update action_capability set result = :result where id = :id",
                    new MapSqlParameterSource("result", outcome).addValue("id", row.id()));
            return new Result(row.action(), row.alertId(), outcome, row.deliveryId(), row.channelId());
        }
        if (!AlertState.FIRING.wire().equals(alert.state())) {
            outcome = "already-" + alert.state();
            markUsed(row.id(), now, via, outcome);
            return new Result(row.action(), row.alertId(), outcome, row.deliveryId(), row.channelId());
        }
        // Mark used first: the acknowledgement below supersedes every other open capability of the alert.
        markUsed(row.id(), now, via, "executing");
        Duration duration = ActionDurations.parse(row.action(), row.params().get("duration"));
        switch (row.action()) {
            case NotificationAction.SNOOZE -> {
                alertService.snooze(alert.id(), now.plus(duration));
                outcome = "snoozed";
            }
            case NotificationAction.MUTE -> {
                if (alert.ruleId() == null || alert.ruleName() == null) {
                    outcome = "rule-deleted";
                } else {
                    rules.mute(alert.ruleId(), now.plus(duration));
                    outcome = "muted";
                }
            }
            default -> {
                alertService.acknowledge(alert.id(), AlertService.Transition.NONE, actor(row));
                outcome = "acknowledged";
            }
        }
        jdbc.update("update action_capability set result = :result where id = :id",
                new MapSqlParameterSource("result", outcome).addValue("id", row.id()));
        return new Result(row.action(), row.alertId(), outcome, row.deliveryId(), row.channelId());
    }

    /** {@code action:<channelType>:<channelName>}, or {@code action:device:<key>} for a device capability. */
    String actor(Row row) {
        String actor = rawActor(row);
        return actor.length() > 200 ? actor.substring(0, 200) : actor;
    }

    private String rawActor(Row row) {
        if (row.channelId() != null) {
            Optional<NotificationChannelEntity> channel = channels.findById(row.channelId());
            if (channel.isPresent()) {
                return "action:" + channel.get().getType() + ":" + channel.get().getName();
            }
            return "action:channel:" + row.channelId();
        }
        return "action:device:" + (row.deviceId() == null ? "unknown" : row.deviceId());
    }

    // ------------------------------------------------------------------------- revoke

    /**
     * Marks every open capability of {@code alertId} used with result {@value #SUPERSEDED} (the alert
     * was acknowledged or resolved by some path), in its own statement.
     *
     * @return how many were revoked
     */
    public int supersede(long alertId) {
        return jdbc.update("""
                update action_capability set used_at = :now, used_via = 'lifecycle', result = :result
                 where alert_id = :alert and used_at is null
                """, new MapSqlParameterSource("now", Timestamp.from(clock.instant()))
                .addValue("result", SUPERSEDED).addValue("alert", alertId));
    }

    /** Deletes capabilities that expired before {@code cutoff} (retention). */
    public int purgeExpired(Instant cutoff) {
        return jdbc.update("delete from action_capability where expires_at < :cutoff",
                new MapSqlParameterSource("cutoff", Timestamp.from(cutoff)));
    }

    // ------------------------------------------------------------------------- helpers

    private static final String SELECT = """
            select id, alert_id, action, params::text as params, channel_id, delivery_id, device_id, expires_at, used_at
              from action_capability where token_hash = :hash
            """;

    private static boolean usable(Row row, Instant now) {
        return row.usedAt() == null && row.expiresAt().isAfter(now);
    }

    private void markUsed(long id, Instant now, Via via, String result) {
        jdbc.update("update action_capability set used_at = :now, used_via = :via, result = :result where id = :id",
                new MapSqlParameterSource("now", Timestamp.from(now)).addValue("via", via.text())
                        .addValue("result", result).addValue("id", id));
    }

    @SuppressWarnings("unchecked")
    private Row row(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        long delivery = rs.getLong("delivery_id");
        Long deliveryId = rs.wasNull() ? null : delivery;
        String params = rs.getString("params");
        OffsetDateTime used = rs.getObject("used_at", OffsetDateTime.class);
        return new Row(rs.getLong("id"), rs.getLong("alert_id"), rs.getString("action"),
                params == null ? Map.of() : json.readValue(params, Map.class), rs.getObject("channel_id", UUID.class),
                deliveryId, rs.getString("device_id"), rs.getObject("expires_at", OffsetDateTime.class).toInstant(),
                used == null ? null : used.toInstant());
    }

    private void audit(Instant now, Via via, Result result, String note, int status) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("via", via.text());
        if (result != null) {
            detail.put("action", result.action());
            detail.put("result", result.result());
        } else {
            detail.put("result", "expired");
        }
        if (note != null) {
            detail.put("note", note);
        }
        audit.record(new AuditRecord(now, ACTOR_KIND, null, null, null, null, "POST", ActionPaths.REDACTED,
                "executeAction", status, result == null ? null : "alerts",
                result == null ? null : Long.toString(result.alertId()), via.remoteIp(), detail));
    }

    /** The public base URL capability links use. */
    public String baseUrl() {
        String base = channelService.serverSettings().normalizedPublicBaseUrl();
        return base == null ? NotificationRouter.DEFAULT_BASE_URL : base;
    }
}
