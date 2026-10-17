package ai.foresignal.quality;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/** Reads and writes {@code alert_signal} (prompt 115). */
@Component
public class AlertSignalStore {

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the store. */
    public AlertSignalStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Records {@code type} for alert {@code alertId} once; a repeat is ignored. Exercise alerts are never recorded.
     *
     * @return true when a row was written
     */
    public boolean record(long alertId, AlertSignalType type, Instant at) {
        int n = jdbc.update("""
                INSERT INTO alert_signal (alert_id, rule_id, type, at)
                SELECT a.id, a.rule_id, :type, :at FROM alert a WHERE a.id = :alert AND NOT a.exercise
                ON CONFLICT (alert_id, type) DO NOTHING""",
                new MapSqlParameterSource("alert", alertId).addValue("type", type.name())
                        .addValue("at", Timestamp.from(at)));
        return n > 0;
    }

    /**
     * Records {@code SNOOZED} for every open, non-exercise alert of {@code ruleId} (the rule was snoozed).
     *
     * @return rows written
     */
    public int recordRuleSnoozed(UUID ruleId, Instant at) {
        return jdbc.update("""
                INSERT INTO alert_signal (alert_id, rule_id, type, at)
                SELECT a.id, a.rule_id, 'SNOOZED', :at FROM alert a
                 WHERE a.rule_id = :rule AND a.state <> 'resolved' AND NOT a.exercise AND NOT a.suppressed
                ON CONFLICT (alert_id, type) DO NOTHING""",
                new MapSqlParameterSource("rule", ruleId).addValue("at", Timestamp.from(at)));
    }

    /**
     * Records {@code UNOPENED} for the {@code alert.fired} notifications created in {@code [from, cutoff)} that no
     * device read, by a sparse read or a read cursor, provided read tracking (033) is in use at all.
     *
     * @return rows written
     */
    public int recordUnopened(Instant from, Instant cutoff) {
        return jdbc.update("""
                INSERT INTO alert_signal (alert_id, rule_id, type, at)
                SELECT a.id, a.rule_id, 'UNOPENED', n.created_at + interval '24 hours'
                  FROM notification n JOIN alert a ON a.id = n.alert_id
                 WHERE n.subtype = 'alert.fired' AND n.created_at >= :from AND n.created_at < :cutoff
                   AND NOT a.exercise AND NOT a.suppressed
                   AND (EXISTS (SELECT 1 FROM notification_read_cursor) OR EXISTS (SELECT 1 FROM notification_read))
                   AND NOT EXISTS (SELECT 1 FROM notification_read r WHERE r.notification_id = n.id)
                   AND NOT EXISTS (SELECT 1 FROM notification_read_cursor c WHERE c.read_up_to_id >= n.id)
                ON CONFLICT (alert_id, type) DO NOTHING""",
                new MapSqlParameterSource("from", Timestamp.from(from)).addValue("cutoff", Timestamp.from(cutoff)));
    }

    /** The signal counts of {@code ruleId}'s alerts fired since {@code since}, by wire name (all four present). */
    public Map<String, Long> counts(UUID ruleId, Instant since) {
        Map<String, Long> out = new LinkedHashMap<>();
        jdbc.query("""
                SELECT s.type, count(*) FROM alert_signal s JOIN alert a ON a.id = s.alert_id
                 WHERE s.rule_id = :rule AND a.fired_at >= :since AND NOT a.exercise GROUP BY s.type""",
                new MapSqlParameterSource("rule", ruleId).addValue("since", Timestamp.from(since)),
                rs -> {
                    out.put(rs.getString(1), rs.getLong(2));
                });
        return AlertSignalType.complete(out);
    }

    /** The signals of one alert, oldest first, as {@code AlertSignal} objects. */
    public List<Map<String, Object>> forAlert(long alertId) {
        return jdbc.query("SELECT alert_id, type, at FROM alert_signal WHERE alert_id = :alert ORDER BY at, id",
                new MapSqlParameterSource("alert", alertId), (rs, i) -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("alertId", rs.getLong(1));
                    out.put("type", rs.getString(2));
                    out.put("at", rs.getTimestamp(3).toInstant().toString());
                    return out;
                });
    }
}
