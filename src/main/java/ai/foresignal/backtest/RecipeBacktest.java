package ai.foresignal.backtest;

import ai.foresignal.store.query.ReadQueryRunner;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * A recipe backtest (prompt 103 §4), as far as the correlation engine allows.
 *
 * <p>The prompt asks for a dry-run replay of {@code CorrelationEngine} over the range into an
 * in-memory store. The engine has no dry-run mode (and is outside this package), so this backtest
 * reports the <em>stored</em> intelligence events the recipe formed in the requested time range:
 * events whose {@code start_time} falls in {@code [from, to)}, of the given recipe when one is named,
 * excluding events merged into another and private events. It reports each event (type, start/end,
 * members with join reasons, confidence, evidence quality) and the counts by type. Because the
 * events are the real ones, no overlap with real events is computed; {@code replay} says so.
 */
@Service
public class RecipeBacktest {

    /** The most events listed in a result. */
    public static final int MAX_EVENTS = 200;

    /** The most members listed per event. */
    public static final int MAX_MEMBERS = 20;

    /** What the {@code replay} field reports. */
    public static final String REPLAY = "stored-events";

    private final ReadQueryRunner reads;
    private final JsonMapper json;

    /** Creates the service. */
    public RecipeBacktest(ReadQueryRunner reads, JsonMapper json) {
        this.reads = reads;
        this.json = json;
    }

    /**
     * The stored events of {@code recipeId} (every recipe when null) that started in {@code [from, to)}.
     *
     * @param recipeId the recipe, or null for every recipe
     * @param from the start of the range
     * @param to the end of the range
     * @param monitor the job's monitor
     */
    public Map<String, Object> run(String recipeId, Instant from, Instant to, JobMonitor monitor) {
        monitor.checkpoint(0);
        String filter = " FROM intelligence_event WHERE start_time >= ? AND start_time < ? AND merged_into IS NULL"
                + " AND private = false" + (recipeId == null ? "" : " AND recipe_id = ?");
        Object[] params = recipeId == null ? new Object[] {Timestamp.from(from), Timestamp.from(to)}
                : new Object[] {Timestamp.from(from), Timestamp.from(to), recipeId};
        List<Map<String, Object>> counts = reads.read(BacktestCoverage.STATEMENT_TIMEOUT,
                t -> t.getJdbcTemplate().query("SELECT type, count(*) AS n" + filter + " GROUP BY type ORDER BY type",
                        (rs, n) -> Map.<String, Object>of("type", rs.getString("type"), "count", rs.getLong("n")),
                        params));
        monitor.checkpoint(0.3);
        List<Map<String, Object>> events = reads.read(BacktestCoverage.STATEMENT_TIMEOUT,
                t -> t.getJdbcTemplate().query("SELECT id, type, start_time, end_time, confidence, evidence_quality,"
                                + " recipe_id, recipe_version" + filter + " ORDER BY start_time, id LIMIT " + MAX_EVENTS,
                        (rs, n) -> {
                            Map<String, Object> event = new LinkedHashMap<>();
                            event.put("id", rs.getString("id"));
                            event.put("type", rs.getString("type"));
                            event.put("startTime", rs.getTimestamp("start_time").toInstant().toString());
                            Timestamp end = rs.getTimestamp("end_time");
                            event.put("endTime", end == null ? null : end.toInstant().toString());
                            event.put("confidence", rs.getObject("confidence"));
                            event.put("evidenceQuality", rs.getString("evidence_quality"));
                            event.put("recipeId", rs.getString("recipe_id"));
                            event.put("recipeVersion", rs.getObject("recipe_version"));
                            return event;
                        }, params));
        monitor.checkpoint(0.6);
        attachMembers(events);
        long total = counts.stream().mapToLong(c -> ((Number) c.get("count")).longValue()).sum();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recipeId", recipeId);
        out.put("replay", REPLAY);
        out.put("eventCount", total);
        out.put("countsByType", countsByType(counts));
        out.put("events", events);
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("note", "Stored intelligence events that started in the range; the correlation engine has no dry-run"
                + " replay, so these are the events Foresignal formed, not a re-run of the recipe.");
        return out;
    }

    /**
     * The {@code countsByType} object of {@code RecipeBacktestResult}: event type to count, in the
     * order of the rows.
     *
     * @param rows {@code {type, count}} rows
     * @return the object
     */
    static Map<String, Long> countsByType(List<Map<String, Object>> rows) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            out.merge(String.valueOf(row.get("type")), ((Number) row.get("count")).longValue(), Long::sum);
        }
        return out;
    }

    private void attachMembers(List<Map<String, Object>> events) {
        if (events.isEmpty()) {
            return;
        }
        List<String> ids = events.stream().map(e -> String.valueOf(e.get("id"))).toList();
        Map<String, List<Map<String, Object>>> members = new HashMap<>();
        String placeholders = String.join(",", ids.stream().map(i -> "?").toList());
        reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
            t.getJdbcTemplate().query("SELECT event_id, role, source_id, external_id, seq, join_reason::text AS reason"
                            + " FROM intelligence_event_member WHERE event_id IN (" + placeholders + ")"
                            + " ORDER BY event_id, id",
                    rs -> {
                        List<Map<String, Object>> list = members.computeIfAbsent(rs.getString("event_id"),
                                k -> new ArrayList<>());
                        if (list.size() < MAX_MEMBERS) {
                            Map<String, Object> member = new LinkedHashMap<>();
                            member.put("role", rs.getString("role"));
                            member.put("sourceId", rs.getString("source_id"));
                            member.put("externalId", rs.getString("external_id"));
                            member.put("seq", rs.getLong("seq"));
                            String reason = rs.getString("reason");
                            member.put("joinReason", reason == null ? null : json.readValue(reason, Map.class));
                            list.add(member);
                        }
                    }, ids.toArray());
            return null;
        });
        for (Map<String, Object> event : events) {
            event.put("members", members.getOrDefault(String.valueOf(event.get("id")), List.of()));
        }
    }
}
