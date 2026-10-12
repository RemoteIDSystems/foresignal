package ai.foresignal.trigger;

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
                    + " a.suppressed_reason, a.tags::text AS tags, a.record_snapshot::text AS record_snapshot";

    static final String FROM = " FROM alert a LEFT JOIN trigger_rule r ON r.id = a.rule_id";

    static final String INSERT =
            "INSERT INTO alert (rule_id, seq, source_id, external_id, kind, fired_at, observed_at, message, severity, state,"
                    + " suppressed, suppressed_reason, tags, record_snapshot, rev)"
                    + " VALUES (:rule, :seq, :source, :externalId, :kind, :firedAt, :observedAt, :message, :severity, 'firing',"
                    + " :suppressed, CAST(:reason AS text), CAST(:tags AS jsonb), CAST(:snapshot AS jsonb), nextval('alert_rev'))"
                    + " RETURNING id";

    static final String RESOLVE_OPEN =
            "UPDATE alert SET state = 'resolved', resolved_at = :now, resolved_by = :by, resolved_seq = :resolvedSeq,"
                    + " rev = nextval('alert_rev')"
                    + " WHERE rule_id = :rule AND source_id = :source AND external_id = :externalId"
                    + " AND state <> 'resolved' AND seq < :before RETURNING id";

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
                  FROM alert WHERE rule_id = ANY(CAST(:ids AS uuid[])) GROUP BY rule_id""", p, rs -> {
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
                        + " AND snoozed_until > :now",
                new MapSqlParameterSource("rule", ruleId).addValue("now", utc(now)),
                rs -> {
                    keys.add(rs.getString(1) + '\u0000' + rs.getString(2));
                });
        return keys;
    }

    /** When {@code ruleId}'s unsuppressed alerts fired since {@code since}, oldest first (storm seed). */
    public List<Instant> firedSince(UUID ruleId, Instant since) {
        return jdbc.query("SELECT fired_at FROM alert WHERE rule_id = :rule AND NOT suppressed AND fired_at > :since"
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
                 WHERE a.rule_id = :rule AND a.state <> 'resolved'
                   AND (h.source_id IS NULL OR (l.valid_until IS NOT NULL AND l.valid_until < :now))""",
                new MapSqlParameterSource("rule", ruleId).addValue("now", utc(now)), Long.class);
    }

    /** Open alerts of {@code ruleId} whose entity was last observed before {@code cutoff}. */
    public List<Long> staleOpen(UUID ruleId, Instant cutoff) {
        return jdbc.queryForList("""
                SELECT a.id FROM alert a
                  JOIN record_live l ON l.source_id = a.source_id AND l.external_id = a.external_id
                 WHERE a.rule_id = :rule AND a.state <> 'resolved' AND l.observed_at < :cutoff""",
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
     */
    public record NewAlert(
            RuleSpec rule, MatchRow row, Instant firedAt, String message, String snapshot, String suppressedReason) {}

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
                    .addValue("snapshot", alert.snapshot());
            Long id = jdbc.queryForObject(INSERT, p, Long.class);
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
        if (!q.excludedSources().isEmpty()) {
            where.add("NOT (a.source_id = ANY(CAST(:excluded AS text[])))");
            p.addValue("excluded", PgArrays.text(q.excludedSources().stream().sorted().toList()));
        }
        return where;
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
                rs.getString("record_snapshot"));
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
