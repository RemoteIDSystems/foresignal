package ai.foresignal.quality;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes and reads {@code rule_quality_daily} (prompt 115). Exercise and suppressed alerts are never counted.
 */
@Component
public class RuleQualityDailyStore {

    /**
     * Adds suppressed matches to a rule's row of a day (the trigger engine, after its pass committed): parameters
     * rule, day, by cooldown, by rate cap, now.
     */
    public static final String COUNT_SUPPRESSED = """
            INSERT INTO rule_quality_daily (rule_id, day, fired, useful, noise, late, false_positive, flaps,
                                            suppressed_by_cooldown, suppressed_by_rate_cap, updated_at)
            VALUES (?, ?, 0, 0, 0, 0, 0, 0, ?, ?, ?)
            ON CONFLICT (rule_id, day) DO UPDATE SET
                suppressed_by_cooldown = rule_quality_daily.suppressed_by_cooldown + EXCLUDED.suppressed_by_cooldown,
                suppressed_by_rate_cap = rule_quality_daily.suppressed_by_rate_cap + EXCLUDED.suppressed_by_rate_cap,
                updated_at = EXCLUDED.updated_at""";

    /** How long rows are kept. */
    public static final int RETENTION_DAYS = 400;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public RuleQualityDailyStore(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Recomputes the alert, feedback, acknowledgement, flap and signal columns of every rule with alerts fired on
     * {@code day} (UTC), keeping the suppression counters.
     *
     * @return the rows written
     */
    public int aggregate(LocalDate day, Instant now) {
        Instant from = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        MapSqlParameterSource p = new MapSqlParameterSource("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to)).addValue("day", Date.valueOf(day))
                .addValue("now", Timestamp.from(now));
        return jdbc.update("""
                INSERT INTO rule_quality_daily (rule_id, day, fired, useful, noise, late, false_positive, signals,
                                                median_ack_seconds, flaps, suppressed_by_cooldown,
                                                suppressed_by_rate_cap, updated_at)
                SELECT a.rule_id, :day, count(*),
                       count(*) FILTER (WHERE a.feedback = 'useful'),
                       count(*) FILTER (WHERE a.feedback = 'noise'),
                       count(*) FILTER (WHERE a.feedback = 'late'),
                       count(*) FILTER (WHERE a.feedback = 'false_positive'),
                       (SELECT coalesce(jsonb_object_agg(t.type, t.n), '{}'::jsonb) FROM (
                            SELECT s.type, count(*) AS n FROM alert_signal s JOIN alert x ON x.id = s.alert_id
                             WHERE x.rule_id = a.rule_id AND x.fired_at >= :from AND x.fired_at < :to
                               AND NOT x.exercise AND NOT x.suppressed
                             GROUP BY s.type) t),
                       percentile_cont(0.5) WITHIN GROUP (ORDER BY extract(epoch FROM a.acknowledged_at - a.fired_at))
                           FILTER (WHERE a.acknowledged_at IS NOT NULL),
                       count(*) FILTER (WHERE a.resolved_by = 'inverse_edge'
                                          AND a.resolved_at <= a.fired_at + interval '10 minutes'),
                       0, 0, :now
                  FROM alert a
                 WHERE a.fired_at >= :from AND a.fired_at < :to AND NOT a.exercise AND NOT a.suppressed
                   AND a.rule_id IS NOT NULL
                 GROUP BY a.rule_id
                ON CONFLICT (rule_id, day) DO UPDATE SET
                    fired = EXCLUDED.fired, useful = EXCLUDED.useful, noise = EXCLUDED.noise, late = EXCLUDED.late,
                    false_positive = EXCLUDED.false_positive, signals = EXCLUDED.signals,
                    median_ack_seconds = EXCLUDED.median_ack_seconds, flaps = EXCLUDED.flaps,
                    updated_at = EXCLUDED.updated_at""", p);
    }

    /** The rules with a row since {@code since} (the nightly suggestion pass). */
    public List<UUID> rulesSince(LocalDate since) {
        return jdbc.queryForList("SELECT DISTINCT rule_id FROM rule_quality_daily WHERE day >= :day AND fired > 0",
                new MapSqlParameterSource("day", Date.valueOf(since)), UUID.class);
    }

    /** {@code ruleId}'s rows from {@code since} (UTC day of it) on, oldest first, as {@code RuleQualityDaily}. */
    public List<Map<String, Object>> daily(UUID ruleId, Instant since) {
        LocalDate day = since.atZone(ZoneOffset.UTC).toLocalDate();
        return jdbc.query("""
                SELECT rule_id, day, fired, useful, noise, late, false_positive, signals::text AS signals,
                       median_ack_seconds, flaps, suppressed_by_cooldown, suppressed_by_rate_cap
                  FROM rule_quality_daily WHERE rule_id = :rule AND day >= :day ORDER BY day""",
                new MapSqlParameterSource("rule", ruleId).addValue("day", Date.valueOf(day)), (rs, i) -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("ruleId", rs.getObject("rule_id", UUID.class).toString());
                    out.put("day", rs.getDate("day").toLocalDate().toString());
                    out.put("fired", rs.getInt("fired"));
                    Map<String, Object> feedback = new LinkedHashMap<>();
                    feedback.put("useful", rs.getInt("useful"));
                    feedback.put("noise", rs.getInt("noise"));
                    feedback.put("late", rs.getInt("late"));
                    feedback.put("false_positive", rs.getInt("false_positive"));
                    out.put("feedback", feedback);
                    out.put("signals", signals(rs.getString("signals")));
                    double median = rs.getDouble("median_ack_seconds");
                    out.put("medianAckSeconds", rs.wasNull() ? null : median);
                    out.put("flaps", rs.getInt("flaps"));
                    out.put("suppressedByCooldown", rs.getInt("suppressed_by_cooldown"));
                    out.put("suppressedByRateCap", rs.getInt("suppressed_by_rate_cap"));
                    return out;
                });
    }

    /**
     * Per-rule totals since {@code since}: {@code ruleId, fired, useful, noise, late, falsePositive, days}
     * (the weekly quality briefing section).
     */
    public List<Map<String, Object>> totals(LocalDate since) {
        List<Map<String, Object>> out = new ArrayList<>();
        jdbc.query("""
                SELECT d.rule_id, r.name, sum(d.fired) AS fired, sum(d.useful) AS useful, sum(d.noise) AS noise,
                       sum(d.late) AS late, sum(d.false_positive) AS false_positive, count(*) AS days
                  FROM rule_quality_daily d LEFT JOIN trigger_rule r ON r.id = d.rule_id
                 WHERE d.day >= :day GROUP BY d.rule_id, r.name ORDER BY d.rule_id""",
                new MapSqlParameterSource("day", Date.valueOf(since)), rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ruleId", rs.getObject("rule_id", UUID.class).toString());
                    row.put("ruleName", rs.getString("name"));
                    row.put("fired", rs.getLong("fired"));
                    row.put("useful", rs.getLong("useful"));
                    row.put("noise", rs.getLong("noise"));
                    row.put("late", rs.getLong("late"));
                    row.put("falsePositive", rs.getLong("false_positive"));
                    row.put("days", rs.getLong("days"));
                    out.add(row);
                });
        return out;
    }

    /** Deletes rows older than {@value #RETENTION_DAYS} days before {@code now}. */
    public int purge(Instant now) {
        LocalDate cutoff = now.atZone(ZoneOffset.UTC).toLocalDate().minusDays(RETENTION_DAYS);
        return jdbc.update("DELETE FROM rule_quality_daily WHERE day < :day",
                new MapSqlParameterSource("day", Date.valueOf(cutoff)));
    }

    /** Signal counts with all four names present. */
    private Map<String, Object> signals(String text) {
        Map<String, Object> raw = text == null ? Map.of() : json.readValue(text, MAP);
        Map<String, Object> out = new LinkedHashMap<>();
        for (AlertSignalType type : AlertSignalType.values()) {
            Object value = raw.get(type.name());
            out.put(type.name(), value instanceof Number n ? n.longValue() : 0L);
        }
        return out;
    }
}
