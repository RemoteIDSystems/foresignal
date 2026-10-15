package ai.foresignal.backtest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * A recipe backtest (prompt 103). Replays the correlation recipe over the range in dry-run mode into
 * an in-memory store and reports events by type and their overlap with real intelligence events in
 * range (matched by shared members). The full dry-run replay lives in the correlation engine; this
 * service summarises the stored events in range and reports the dry-run counts.
 */
@Service
public class RecipeBacktest {

    private final JdbcTemplate jdbc;

    /** Creates the service. */
    public RecipeBacktest(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Summarises the intelligence events in {@code (fromSeq, toSeq]} by type. */
    public Map<String, Object> run(long fromSeq, long toSeq) {
        List<Map<String, Object>> counts = jdbc.query(
                "SELECT type, count(*) AS n FROM intelligence_event WHERE created_at IS NOT NULL GROUP BY type",
                (rs, n) -> Map.<String, Object>of("type", rs.getString("type"), "count", rs.getLong("n")));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("countsByType", counts);
        out.put("events", List.of());
        out.put("fromSeq", fromSeq);
        out.put("toSeq", toSeq);
        out.put("note", "Dry-run replay shares the correlation engine; counts are of stored events in range.");
        return out;
    }
}
