package ai.foresignal.trigger;

import ai.foresignal.security.restrict.AlertAudience;
import ai.foresignal.security.restrict.RestrictionClamp;

import ai.foresignal.store.query.PgArrays;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes the {@code alert} table with native SQL (prompt 017).
 *
 * <p><b>Writes are serialized.</b> Every change takes {@code rev} from the {@code alert_rev}
 * sequence, and the alert stream resumes by {@code rev}, so revisions must become visible, and be
 * published, in order: a client that has seen rev 6 drops a rev 5 that commits later. {@link #write}
 * therefore holds one lock across the transaction and the publishing of its events, so commit order,
 * publish order and {@code rev} order agree. The trigger engine and the alert service both write
 * through it.
 */
@Component
public class AlertStore {

    static final String COLUMNS =
            "a.id, a.rule_id, r.name AS rule_name, a.seq, a.source_id, a.external_id, a.kind, a.fired_at, a.observed_at,"
                    + " a.message, a.severity, a.state, a.rev, a.acknowledged_at, a.acknowledged_by, a.resolved_at,"
                    + " a.resolved_by, a.resolved_seq, a.feedback, a.feedback_at, a.note, a.snoozed_until, a.suppressed,"
                    + " a.suppressed_reason, a.tags::text AS tags, a.record_snapshot::text AS record_snapshot,"
                    + " a.summary, a.enriched_at, a.summary_citations::text AS summary_citations,"
                    + " a.sensitive, a.context::text AS context, a.region::text AS region, a.exercise, a.exercise_run_id,"
                    + " a.owner_principal_id";

    static final String FROM = " FROM alert a LEFT JOIN trigger_rule r ON r.id = a.rule_id";

    /**
     * The rule's owner at fire time (prompt R2-008), read in the insert itself so the engine's cached rule never
     * attributes an alert to a former owner.
     */
    static final String RULE_OWNER = "(SELECT o.owner_principal_id FROM trigger_rule o WHERE o.id = :rule)";

    static final String INSERT =
            "INSERT INTO alert (rule_id, seq, source_id, external_id, kind, fired_at, observed_at, message, severity, state,"
                    + " suppressed, suppressed_reason, tags, record_snapshot, rev, sensitive, context, region,"
                    + " owner_principal_id)"
                    + " VALUES (:rule, :seq, :source, :externalId, :kind, :firedAt, :observedAt, :message, :severity, 'firing',"
                    + " :suppressed, CAST(:reason AS text), CAST(:tags AS jsonb), CAST(:snapshot AS jsonb), nextval('alert_rev'),"
                    + " :sensitive, CAST(:context AS jsonb), CAST(:region AS jsonb), " + RULE_OWNER + ")"
                    + " RETURNING id";

    static final String RESOLVE_OPEN =
            "UPDATE alert SET state = 'resolved', resolved_at = :now, resolved_by = :by, resolved_seq = :resolvedSeq,"
                    + " rev = nextval('alert_rev')"
                    + " WHERE rule_id = :rule AND source_id = :source AND external_id = :externalId"
                    + " AND state <> 'resolved' AND NOT exercise AND seq < :before RETURNING id";

    static final String INSERT_EXERCISE =
            "INSERT INTO alert (rule_id, seq, source_id, external_id, kind, fired_at, observed_at, message, severity, state,"
                    + " suppressed, suppressed_reason, tags, record_snapshot, rev, sensitive, context, region, exercise,"
                    + " exercise_run_id, owner_principal_id)"
                    + " VALUES (:rule, :seq, :source, :externalId, :kind, :firedAt, :observedAt, :message, :severity, 'firing',"
                    + " false, NULL, CAST(:tags AS jsonb), CAST(:snapshot AS jsonb), nextval('alert_rev'),"
                    + " :sensitive, CAST(:context AS jsonb), CAST(:region AS jsonb), true, :runId, " + RULE_OWNER + ")"
                    + " RETURNING id";

    static final String RESOLVE_IDS =
            "UPDATE alert SET state = 'resolved', resolved_at = :now, resolved_by = :by, rev = nextval('alert_rev')"
                    + " WHERE id = ANY(CAST(:ids AS bigint[])) AND state <> 'resolved' RETURNING id";

    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final AlertBus bus;
    private final JsonMapper json;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final RowMapper<AlertView> mapper = this::map;

    /** Creates the store. */
    public AlertStore(
            NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactionManager, AlertBus bus, JsonMapper json) {
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.bus = bus;
        this.json = json;
    }

    // ------------------------------------------------------------------------- reads

    /** The alert {@code id}, if it exists. */
    public Optional<AlertView> find(long id) {
        List<AlertView> rows = jdbc.query(
                "SELECT " + COLUMNS + FROM + " WHERE a.id = :id", new MapSqlParameterSource("id", id), mapper);
        return rows.stream().findFirst();
    }

    /** The alerts among {@code ids} that exist, in the order of {@code ids} (prompt 030). */
    public List<AlertView> findAll(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, AlertView> byId = new HashMap<>();
        for (AlertView view : jdbc.query("SELECT " + COLUMNS + FROM + " WHERE a.id = ANY(CAST(:ids AS bigint[]))",
                new MapSqlParameterSource("ids", PgArrays.bigint(ids)), mapper)) {
            byId.put(view.id(), view);
        }
        List<AlertView> out = new ArrayList<>(byId.size());
        for (Long id : ids) {
            AlertView view = byId.get(id);
            if (view != null) {
                out.add(view);
            }
        }
        return out;
    }

    /**
     * One page of alerts, newest first, keyset on id.
     *
     * @param query the filters
     * @param beforeId only ids below this, or null for the first page
     * @param limit the page size
     */
    public List<AlertView> list(AlertQuery query, Long beforeId, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        List<String> where = where(query, p);
        if (beforeId != null) {
            where.add("a.id < :beforeId");
            p.addValue("beforeId", beforeId);
        }
        p.addValue("limit", limit);
        return jdbc.query("SELECT " + COLUMNS + FROM + whereClause(where) + " ORDER BY a.id DESC LIMIT :limit", p, mapper);
    }

    /** The last lifecycle change of an alert row: fired, acknowledged or resolved, whichever is latest. */
    static final String LAST_CHANGE_SQL = "GREATEST(a.fired_at, a.acknowledged_at, a.resolved_at)";

    /**
     * The newest alerts by last lifecycle change (prompt 058: Atom and JSON Feed), then by id, both
     * descending; at most {@code limit}.
     */
    public List<AlertView> feed(AlertQuery query, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        List<String> where = where(query, p);
        p.addValue("limit", limit);
        return jdbc.query("SELECT " + COLUMNS + FROM + whereClause(where) + " ORDER BY " + LAST_CHANGE_SQL
                + " DESC, a.id DESC LIMIT :limit", p, mapper);
    }

    /**
     * Alerts matching {@code query} per severity (prompt 038: the glance's open counts); severities
     * without alerts are absent.
     */
    public Map<String, Long> countBySeverity(AlertQuery query) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        List<String> where = where(query, p);
        Map<String, Long> out = new java.util.TreeMap<>();
        jdbc.query("SELECT a.severity, count(*) FROM alert a" + whereClause(where) + " GROUP BY a.severity", p,
                rs -> {
                    out.put(rs.getString(1), rs.getLong(2));
                });
        return out;
    }

    /** Alerts with {@code rev > afterRev} in rev order, at most {@code limit} (the stream's replay). */
    public List<AlertView> afterRev(AlertQuery query, long afterRev, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        List<String> where = where(query, p);
        where.add("a.rev > :afterRev");
        p.addValue("afterRev", afterRev);
        p.addValue("limit", limit);
        return jdbc.query("SELECT " + COLUMNS + FROM + whereClause(where) + " ORDER BY a.rev LIMIT :limit", p, mapper);
    }

    /** The highest {@code rev} written so far, 0 for none. */
    public long maxRev() {
        Long rev = jdbc.getJdbcTemplate().queryForObject("SELECT coalesce(max(rev), 0) FROM alert", Long.class);
        return rev == null ? 0 : rev;
    }

    /**
     * Per-rule statistics for {@code ruleIds}.
     *
     * @return rule id to statistics; rules without alerts are absent
     */
    public Map<UUID, RuleStats> stats(Collection<UUID> ruleIds) {
        if (ruleIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource p = new MapSqlParameterSource("ids",
                PgArrays.text(ruleIds.stream().map(UUID::toString).toList()));
        Map<UUID, RuleStats> stats = new HashMap<>();
        jdbc.query("""
                SELECT rule_id,
                       max(fired_at) FILTER (WHERE NOT suppressed) AS last_fired,
                       count(*) FILTER (WHERE NOT suppressed) AS alert_count,
                       count(*) FILTER (WHERE NOT suppressed AND state <> 'resolved') AS open_count,
                       count(*) FILTER (WHERE feedback = 'useful') AS useful,
                       count(*) FILTER (WHERE feedback = 'noise') AS noise,
                       count(*) FILTER (WHERE feedback = 'late') AS late,
                       count(*) FILTER (WHERE feedback = 'false_positive') AS false_positive
                  FROM alert WHERE rule_id = ANY(CAST(:ids AS uuid[])) AND NOT exercise GROUP BY rule_id""", p, rs -> {
            Map<String, Long> feedback = new LinkedHashMap<>();
            for (String name : Feedback.WIRE_VALUES) {
                feedback.put(name, rs.getLong(name));
            }
            stats.put(rs.getObject("rule_id", UUID.class), new RuleStats(
                    instant(rs, "last_fired"), rs.getLong("alert_count"), rs.getLong("open_count"), feedback));
        });
        return stats;
    }

    /** Entity keys ({@link MatchRow#entityKey()}) with an open, snoozed alert of {@code ruleId} at {@code now}. */
    public Set<String> snoozedEntities(UUID ruleId, Instant now) {
        Set<String> keys = new HashSet<>();
        jdbc.query("SELECT source_id, external_id FROM alert WHERE rule_id = :rule AND state <> 'resolved'"
                        + " AND NOT exercise AND snoozed_until > :now",
                new MapSqlParameterSource("rule", ruleId).addValue("now", utc(now)),
                rs -> {
                    keys.add(rs.getString(1) + '\u0000' + rs.getString(2));
                });
        return keys;
    }

    /** When {@code ruleId}'s unsuppressed alerts fired since {@code since}, oldest first (storm seed). */
    public List<Instant> firedSince(UUID ruleId, Instant since) {
        return jdbc.query("SELECT fired_at FROM alert WHERE rule_id = :rule AND NOT suppressed AND NOT exercise AND fired_at > :since"
                        + " ORDER BY fired_at",
                new MapSqlParameterSource("rule", ruleId).addValue("since", utc(since)),
                (rs, i) -> instant(rs, "fired_at"));
    }

    /** Open alerts of {@code ruleId} whose entity's live row expired before {@code now} or whose head is gone. */
    public List<Long> expiredOpen(UUID ruleId, Instant now) {
        return jdbc.queryForList("""
                SELECT a.id FROM alert a
                  LEFT JOIN record_head h ON h.source_id = a.source_id AND h.external_id = a.external_id
                  LEFT JOIN record_live l ON l.source_id = a.source_id AND l.external_id = a.external_id
                 WHERE a.rule_id = :rule AND a.state <> 'resolved' AND NOT a.exercise
                   AND (h.source_id IS NULL OR (l.valid_until IS NOT NULL AND l.valid_until < :now))""",
                new MapSqlParameterSource("rule", ruleId).addValue("now", utc(now)), Long.class);
    }

    /** Open alerts of {@code ruleId} whose entity was last observed before {@code cutoff}. */
    public List<Long> staleOpen(UUID ruleId, Instant cutoff) {
        return jdbc.queryForList("""
                SELECT a.id FROM alert a
                  JOIN record_live l ON l.source_id = a.source_id AND l.external_id = a.external_id
                 WHERE a.rule_id = :rule AND a.state <> 'resolved' AND NOT a.exercise AND l.observed_at < :cutoff""",
                new MapSqlParameterSource("rule", ruleId).addValue("cutoff", utc(cutoff)), Long.class);
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Runs {@code work} in one transaction under the write lock, then publishes the events it
     * recorded and runs its after-commit actions, still under the lock.
     */
    public <T> T write(Function<Writer, T> work) {
        lock.lock();
        try {
            Writer writer = new Writer();
            T result = transactions.execute(status -> work.apply(writer));
            for (AlertEvent event : writer.events) {
                bus.publish(event);
            }
            for (Runnable action : writer.afterCommit) {
                action.run();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * A new alert.
     *
     * @param rule the rule
     * @param row the matching version
     * @param firedAt when it fired
     * @param message the rendered message
     * @param snapshot the record snapshot as JSON text
     * @param suppressedReason {@code alert_snoozed}, {@code rule_snoozed} or null
     * @param context the computed {@code AlertContext} as JSON text, or null (prompt 036)
     * @param region the record's {@code RegionTags} at fire time as JSON text, or null (prompt 040)
     */
    public record NewAlert(
            RuleSpec rule, MatchRow row, Instant firedAt, String message, String snapshot, String suppressedReason,
            String context, String region) {

        /** An alert without a computed context. */
        public NewAlert(RuleSpec rule, MatchRow row, Instant firedAt, String message, String snapshot,
                String suppressedReason) {
            this(rule, row, firedAt, message, snapshot, suppressedReason, null, null);
        }

        /** An alert without region tags (the shape before prompt 040). */
        public NewAlert(RuleSpec rule, MatchRow row, Instant firedAt, String message, String snapshot,
                String suppressedReason, String context) {
            this(rule, row, firedAt, message, snapshot, suppressedReason, context, null);
        }
    }

    /** The operations available inside {@link #write}; events are published after commit. */
    public final class Writer {

        private final List<AlertEvent> events = new ArrayList<>();
        private final List<Runnable> afterCommit = new ArrayList<>();

        /** The template, for writes that are not alerts (the cursor, rule counters). */
        public NamedParameterJdbcTemplate jdbc() {
            return jdbc;
        }

        /** Runs {@code action} after the transaction commits. */
        public void afterCommit(Runnable action) {
            afterCommit.add(action);
        }

        /** Inserts {@code alert} and records a {@code fired} event. */
        public AlertView insert(NewAlert alert) {
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("rule", alert.rule().id())
                    .addValue("seq", alert.row().seq())
                    .addValue("source", alert.row().sourceId())
                    .addValue("externalId", alert.row().externalId())
                    .addValue("kind", alert.row().kind())
                    .addValue("firedAt", utc(alert.firedAt()))
                    .addValue("observedAt", utc(alert.row().observedAt()))
                    .addValue("message", alert.message())
                    .addValue("severity", alert.rule().severity().wire())
                    .addValue("suppressed", alert.suppressedReason() != null)
                    .addValue("reason", alert.suppressedReason())
                    .addValue("tags", json.writeValueAsString(alert.rule().tags()))
                    .addValue("snapshot", alert.snapshot())
                    .addValue("sensitive", alert.rule().sensitive())
                    .addValue("context", alert.context())
                    .addValue("region", alert.region());
            Long id = jdbc.queryForObject(INSERT, p, Long.class);
            AlertView view = find(id).orElseThrow();
            events.add(AlertEvent.of(AlertEvent.FIRED, view));
            return view;
        }

        /**
         * Inserts an exercise replay alert (prompt 115) of run {@code runId} and records a {@code fired}
         * event. It is never suppressed and touches no real alert state.
         */
        public AlertView insertExercise(NewAlert alert, String runId) {
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("rule", alert.rule().id())
                    .addValue("seq", alert.row().seq())
                    .addValue("source", alert.row().sourceId())
                    .addValue("externalId", alert.row().externalId())
                    .addValue("kind", alert.row().kind())
                    .addValue("firedAt", utc(alert.firedAt()))
                    .addValue("observedAt", utc(alert.row().observedAt()))
                    .addValue("message", alert.message())
                    .addValue("severity", alert.rule().severity().wire())
                    .addValue("tags", json.writeValueAsString(alert.rule().tags()))
                    .addValue("snapshot", alert.snapshot())
                    .addValue("sensitive", alert.rule().sensitive())
                    .addValue("context", alert.context())
                    .addValue("region", alert.region())
                    .addValue("runId", runId);
            Long id = jdbc.queryForObject(INSERT_EXERCISE, p, Long.class);
            AlertView view = find(id).orElseThrow();
            events.add(AlertEvent.of(AlertEvent.FIRED, view));
            return view;
        }

        /**
         * Resolves the open alerts of {@code ruleId} for one entity that fired before
         * {@code beforeSeq}, recording {@code resolved} events.
         */
        public List<AlertView> resolveOpen(UUID ruleId, String sourceId, String externalId, long beforeSeq,
                String by, Long resolvedSeq, Instant now) {
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("rule", ruleId)
                    .addValue("source", sourceId)
                    .addValue("externalId", externalId)
                    .addValue("before", beforeSeq)
                    .addValue("by", by)
                    .addValue("resolvedSeq", resolvedSeq)
                    .addValue("now", utc(now));
            return recorded(AlertEvent.RESOLVED, jdbc.queryForList(RESOLVE_OPEN, p, Long.class));
        }

        /** Resolves the open alerts among {@code ids} with {@code by}, recording {@code resolved} events. */
        public List<AlertView> resolveIds(Collection<Long> ids, String by, Instant now) {
            if (ids.isEmpty()) {
                return List.of();
            }
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("ids", PgArrays.bigint(ids))
                    .addValue("by", by)
                    .addValue("now", utc(now));
            return recorded(AlertEvent.RESOLVED, jdbc.queryForList(RESOLVE_IDS, p, Long.class));
        }

        /**
         * Runs an {@code UPDATE … RETURNING id} on one alert and records an event of {@code type} when
         * a row changed.
         *
         * @return the alert after the update, or empty when nothing changed
         */
        public Optional<AlertView> update(String type, String sql, MapSqlParameterSource params) {
            List<AlertView> changed = recorded(type, jdbc.queryForList(sql, params, Long.class));
            return changed.stream().findFirst();
        }

        private List<AlertView> recorded(String type, List<Long> ids) {
            if (ids.isEmpty()) {
                return List.of();
            }
            List<AlertView> views = jdbc.query("SELECT " + COLUMNS + FROM + " WHERE a.id = ANY(CAST(:ids AS bigint[]))"
                    + " ORDER BY a.rev", new MapSqlParameterSource("ids", PgArrays.bigint(ids)), mapper);
            for (AlertView view : views) {
                events.add(AlertEvent.of(type, view));
            }
            return views;
        }
    }

    // ------------------------------------------------------------------------- helpers

    private List<String> where(AlertQuery q, MapSqlParameterSource p) {
        List<String> where = new ArrayList<>();
        if (!q.ruleIds().isEmpty()) {
            where.add("a.rule_id = ANY(CAST(:ruleIds AS uuid[]))");
            p.addValue("ruleIds", PgArrays.text(q.ruleIds().stream().map(UUID::toString).sorted().toList()));
        }
        if (!q.sources().isEmpty()) {
            where.add("a.source_id = ANY(CAST(:sources AS text[]))");
            p.addValue("sources", PgArrays.text(q.sources()));
        }
        if (!q.kinds().isEmpty()) {
            where.add("a.kind = ANY(CAST(:kinds AS text[]))");
            p.addValue("kinds", PgArrays.text(q.kinds()));
        }
        if (q.since() != null) {
            where.add("a.fired_at >= :since");
            p.addValue("since", utc(q.since()));
        }
        if (q.acknowledged() != null) {
            where.add(q.acknowledged() ? "a.acknowledged_at IS NOT NULL" : "a.acknowledged_at IS NULL");
        }
        if (!q.states().isEmpty()) {
            where.add("a.state = ANY(CAST(:states AS text[]))");
            p.addValue("states", PgArrays.text(q.states()));
        }
        if (!q.feedback().isEmpty()) {
            where.add("a.feedback = ANY(CAST(:feedback AS text[]))");
            p.addValue("feedback", PgArrays.text(q.feedback()));
        }
        if (q.minSeverity() != null) {
            where.add("a.severity = ANY(CAST(:severities AS text[]))");
            p.addValue("severities", PgArrays.text(q.minSeverity().atOrAbove()));
        }
        if (q.tag() != null) {
            where.add("a.tags @> CAST(:tag AS jsonb)");
            p.addValue("tag", json.writeValueAsString(List.of(q.tag())));
        }
        if (!q.includeSuppressed()) {
            where.add("NOT a.suppressed");
        }
        if (!q.includeExercise()) {
            // Exercise replay alerts (prompt 115) are shown only when asked for.
            where.add("NOT a.exercise");
        }
        if (!q.excludedSources().isEmpty()) {
            where.add("NOT (a.source_id = ANY(CAST(:excluded AS text[])))");
            p.addValue("excluded", PgArrays.text(q.excludedSources().stream().sorted().toList()));
        }
        RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL, q.restriction(), p,
                "restrict").ifPresent(where::add);
        audiencePredicate("a", q.restriction().alertAudience(), p).ifPresent(where::add);
        if (q.restriction().regionFilter() != null) {
            // The country, admin1 and county filters (prompt 040) read the region snapshot taken at fire time.
            int[] n = {0};
            where.add(q.restriction().regionFilter().jsonSql("a.region", codes -> {
                String name = "region" + n[0]++;
                p.addValue(name, PgArrays.text(codes));
                return "CAST(:" + name + " AS text[])";
            }));
        }
        return where;
    }

    /**
     * The SQL test of an alert audience (prompt R2-008) on the alert alias {@code alias}: shared alerts, plus the
     * audience's own; empty when there is no audience (every alert).
     *
     * @param alias the alert table alias
     * @param audience the audience, or null
     * @param p the parameters, receiving {@code audience}
     * @return the predicate, or empty
     */
    public static Optional<String> audiencePredicate(String alias, AlertAudience audience, MapSqlParameterSource p) {
        if (audience == null) {
            return Optional.empty();
        }
        if (audience.principalId() == null) {
            return Optional.of(alias + ".owner_principal_id IS NULL");
        }
        p.addValue("audience", audience.principalId());
        return Optional.of("(" + alias + ".owner_principal_id IS NULL OR " + alias + ".owner_principal_id = :audience)");
    }

    private static String whereClause(List<String> where) {
        return where.isEmpty() ? "" : " WHERE " + String.join(" AND ", where);
    }

    private AlertView map(ResultSet rs, int row) throws SQLException {
        long resolvedSeq = rs.getLong("resolved_seq");
        Long resolved = rs.wasNull() ? null : resolvedSeq;
        String tags = rs.getString("tags");
        return new AlertView(
                rs.getLong("id"),
                rs.getObject("rule_id", UUID.class),
                rs.getString("rule_name"),
                rs.getLong("seq"),
                rs.getString("source_id"),
                rs.getString("external_id"),
                rs.getString("kind"),
                instant(rs, "fired_at"),
                instant(rs, "observed_at"),
                rs.getString("message"),
                rs.getString("severity"),
                rs.getString("state"),
                rs.getLong("rev"),
                instant(rs, "acknowledged_at"),
                rs.getString("acknowledged_by"),
                instant(rs, "resolved_at"),
                rs.getString("resolved_by"),
                resolved,
                rs.getString("feedback"),
                instant(rs, "feedback_at"),
                rs.getString("note"),
                instant(rs, "snoozed_until"),
                rs.getBoolean("suppressed"),
                rs.getString("suppressed_reason"),
                tags == null ? List.of() : json.readValue(tags, STRINGS),
                rs.getString("record_snapshot"),
                rs.getString("summary"),
                instant(rs, "enriched_at"),
                rs.getString("summary_citations"),
                rs.getBoolean("sensitive"),
                rs.getString("context"),
                rs.getString("region"),
                rs.getBoolean("exercise"),
                rs.getString("exercise_run_id"),
                rs.getString("owner_principal_id"));
    }

    static Instant instant(ResultSet rs, String label) throws SQLException {
        OffsetDateTime value = rs.getObject(label, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    /** {@code instant} as a UTC {@code OffsetDateTime} for binding as {@code timestamptz}, or null. */
    static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * A rule's alert statistics.
     *
     * @param lastFiredAt the last unsuppressed alert, or null
     * @param alertCount unsuppressed alerts
     * @param openAlertCount unsuppressed alerts not resolved
     * @param feedbackCounts feedback wire value to count
     */
    public record RuleStats(Instant lastFiredAt, long alertCount, long openAlertCount, Map<String, Long> feedbackCounts) {

        /** No alerts. */
        public static RuleStats empty() {
            Map<String, Long> feedback = new LinkedHashMap<>();
            for (String name : Feedback.WIRE_VALUES) {
                feedback.put(name, 0L);
            }
            return new RuleStats(null, 0, 0, feedback);
        }
    }
}
