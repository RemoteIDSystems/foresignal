package ai.foresignal.cycle;

import ai.foresignal.api.support.ETags;
import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.forecast.language.EstimativeLanguage;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The intel board (prompt 128 §6): the top five situations by severity and recency, hypothesis band changes and
 * observed indicators of the last 24 hours, questions due in the next 7 days with the ensemble probability and its
 * term, the latest assessment of each cadence with its headline, the ensemble's 90-day Brier skill and calibration
 * next to the operator's (when the operator has at least 20 resolved forecasts), and the latest benchmark status per
 * component, within 8 KB ({@link IntelBoardBuilder}). Private objects are left out for callers without private
 * access; the board carries no raw series values, so nothing {@code LOCAL_ONLY} leaves through it. Boards are cached
 * for 60 seconds per privacy class; the ETag covers everything but {@code generatedAt}, so it is stable while nothing
 * changes.
 */
@Service
public class IntelBoardService {

    private static final Logger log = LoggerFactory.getLogger(IntelBoardService.class);

    /** Seconds a board is cached, here and by clients. */
    public static final int CACHE_SECONDS = 60;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final CycleStateReader state;
    private final ObjectProvider<BenchmarkGate> benchmarks;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<Boolean, Cached> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public IntelBoardService(NamedParameterJdbcTemplate jdbc, CycleStateReader state,
            ObjectProvider<BenchmarkGate> benchmarks, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.state = state;
        this.benchmarks = benchmarks;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A built board.
     *
     * @param body the JSON
     * @param etag the entity tag (over everything but {@code generatedAt})
     */
    public record Board(String body, String etag) {}

    private record Cached(Board board, Instant expires) {}

    /** The board for a caller that may (or may not) see private objects. */
    public Board board(boolean seesPrivate) {
        Instant now = clock.instant();
        Cached cached = cache.get(seesPrivate);
        if (cached != null && now.isBefore(cached.expires())) {
            return cached.board();
        }
        Map<String, Object> board = build(seesPrivate, now);
        Map<String, Object> stable = new LinkedHashMap<>(board);
        stable.remove("generatedAt");
        Board out = new Board(json.writeValueAsString(board), ETags.forDocument(json.writeValueAsString(stable)));
        cache.put(seesPrivate, new Cached(out, now.plusSeconds(CACHE_SECONDS)));
        return out;
    }

    /** Drops the cached boards (tests). */
    public void clearCache() {
        cache.clear();
    }

    /** The board document. */
    public Map<String, Object> build(boolean seesPrivate, Instant now) {
        List<Map<String, Object>> situations = new ArrayList<>();
        List<Map<String, Object>> bands = new ArrayList<>();
        List<Map<String, Object>> indicators = new ArrayList<>();
        List<Map<String, Object>> due = new ArrayList<>();
        List<Map<String, Object>> latest = new ArrayList<>();
        Map<String, Object> calibration = null;
        List<Map<String, Object>> bench = new ArrayList<>();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("now", Timestamp.from(now))
                .addValue("day", Timestamp.from(now.minus(Duration.ofHours(24))))
                .addValue("week", Timestamp.from(now.plus(Duration.ofDays(7))))
                .addValue("private", seesPrivate);
        try {
            jdbc.query("SELECT id, title, status, severity, last_activity_at FROM situation WHERE merged_into IS NULL"
                    + " AND status <> 'closed' AND (:private OR NOT \"private\") ORDER BY CASE severity WHEN"
                    + " 'critical' THEN 0 WHEN 'warning' THEN 1 ELSE 2 END, last_activity_at DESC NULLS LAST, id"
                    + " LIMIT 5", p, rs -> {
                        Map<String, Object> s = new LinkedHashMap<>();
                        s.put("id", rs.getString("id"));
                        s.put("title", rs.getString("title"));
                        s.put("status", rs.getString("status"));
                        s.put("severity", rs.getString("severity"));
                        Instant at = CyclePacketReader.instant(rs, "last_activity_at");
                        s.put("lastActivityAt", at == null ? null : at.toString());
                        situations.add(s);
                    });
            jdbc.query("SELECT event, set_id, payload::text AS payload, at FROM hypothesis_log WHERE at >= :day"
                    + " AND event IN ('set.band_changed', 'indicator.observed') AND (:private OR NOT \"private\")"
                    + " ORDER BY at DESC, id DESC LIMIT 200", p, rs -> {
                        Map<String, Object> payload = parse(rs.getString("payload"));
                        String at = CyclePacketReader.instant(rs, "at").toString();
                        if ("set.band_changed".equals(rs.getString("event"))) {
                            for (Object raw : payload.get("bandChanges") instanceof List<?> l ? l : List.of()) {
                                if (raw instanceof Map<?, ?> c) {
                                    Map<String, Object> b = new LinkedHashMap<>();
                                    b.put("setId", rs.getString("set_id"));
                                    b.put("hypothesisId", c.get("hypothesisId"));
                                    b.put("question", payload.get("question"));
                                    b.put("label", c.get("label"));
                                    b.put("fromTerm", c.get("fromTerm"));
                                    b.put("toTerm", c.get("toTerm"));
                                    b.put("probability", c.get("probability"));
                                    b.put("at", at);
                                    bands.add(b);
                                }
                            }
                        } else {
                            Map<String, Object> i = new LinkedHashMap<>();
                            i.put("indicatorId", payload.get("indicatorId"));
                            i.put("setId", rs.getString("set_id"));
                            i.put("description", payload.get("description"));
                            i.put("at", at);
                            indicators.add(i);
                        }
                    });
            jdbc.query("SELECT id, title, resolve_by, ensemble_probability FROM forecast_question WHERE status IN"
                    + " ('open', 'closed') AND resolve_by >= :now AND resolve_by < :week AND (:private OR NOT"
                    + " \"private\") ORDER BY resolve_by, id LIMIT 100", p, rs -> {
                        Map<String, Object> q = new LinkedHashMap<>();
                        q.put("questionId", rs.getString("id"));
                        q.put("title", rs.getString("title"));
                        q.put("dueAt", CyclePacketReader.instant(rs, "resolve_by").toString());
                        Double prob = rs.getObject("ensemble_probability") == null ? null
                                : rs.getDouble("ensemble_probability");
                        q.put("probability", prob);
                        q.put("estimativeTerm", prob == null ? null : EstimativeLanguage.term(Math.max(0,
                                Math.min(1, prob))));
                        due.add(q);
                    });
            for (String cadence : CycleVocabulary.CADENCES) {
                jdbc.query("SELECT id, headline, created_at FROM assessment WHERE cadence = :cadence AND (:private OR"
                        + " NOT \"private\") ORDER BY id DESC LIMIT 1", new MapSqlParameterSource(p.getValues())
                                .addValue("cadence", cadence), rs -> {
                            Map<String, Object> a = new LinkedHashMap<>();
                            a.put("cadence", cadence);
                            a.put("assessmentId", rs.getString("id"));
                            a.put("headline", rs.getString("headline"));
                            a.put("createdAt", CyclePacketReader.instant(rs, "created_at").toString());
                            latest.add(a);
                        });
            }
            calibration = calibration(state.scorecard(now));
        } catch (RuntimeException e) {
            log.warn("The intel board could not read everything: {}", e.toString());
        }
        BenchmarkGate gate = benchmarks.getIfAvailable();
        if (gate != null) {
            try {
                Map<String, BenchmarkGate.ComponentStatus> statuses = gate.latest();
                for (String component : BenchmarkInputs.COMPONENTS) {
                    BenchmarkGate.ComponentStatus s = statuses.get(component);
                    Map<String, Object> b = new LinkedHashMap<>();
                    b.put("component", component);
                    b.put("status", s == null || s.status() == null ? "unknown" : s.status());
                    bench.add(b);
                }
            } catch (RuntimeException e) {
                log.debug("Benchmark status unavailable: {}", e.toString());
            }
        }
        return IntelBoardBuilder.build(new IntelBoardBuilder.Inputs(now, situations, bands, indicators, due, latest,
                calibration, bench), json);
    }

    /** The board's calibration block from a 90-day scorecard summary. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> calibration(Map<String, Object> scorecard) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("window", "P90D");
        out.put("ensemble", headline((Map<String, Object>) scorecard.get("ensemble")));
        out.put("operator", headline((Map<String, Object>) scorecard.get("operator")));
        return out;
    }

    private static Map<String, Object> headline(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("brierSkill", row.get("brierSkill"));
        out.put("calibration", row.get("calibration"));
        out.put("n", row.get("n"));
        return out;
    }

    private Map<String, Object> parse(String text) {
        try {
            return text == null ? Map.of() : json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }
}
