package ai.foresignal.cycle;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the {@link CycleState} of a scope (prompt 128 §4): open situations by severity and recency, active hypothesis
 * sets with their hypotheses and the drivers of their latest revision, pending indicators due in the next period, and
 * the 90-day scorecard summary (ensemble, operator, and every forecaster) from the 122 scores.
 */
@Component
public class CycleStateReader {

    private static final Logger log = LoggerFactory.getLogger(CycleStateReader.class);

    /** The most situations described. */
    static final int MAX_SITUATIONS = 25;

    /** The most sets described. */
    static final int MAX_SETS = 25;

    /** The scorecard window. */
    public static final Duration SCORE_WINDOW = Duration.ofDays(90);

    /** The fewest resolved forecasts for the operator to be shown next to the machine. */
    public static final int OPERATOR_MIN = 20;

    private static final TypeReference<List<Object>> LIST = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public CycleStateReader(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * The state of {@code scope} at {@code now}.
     *
     * @param scope the scope
     * @param cadence the cadence (the next period's length and whether a scorecard is included)
     * @param now now
     */
    public CycleState read(CycleScope scope, String cadence, Instant now) {
        List<Map<String, Object>> situations = new ArrayList<>();
        List<Map<String, Object>> sets = new ArrayList<>();
        List<Map<String, Object>> indicators = new ArrayList<>();
        Map<String, Object> scorecard = null;
        try {
            situations = situations(scope, cadence);
            sets = sets(scope, cadence);
            indicators = indicators(sets, now, CycleVocabulary.lookback(cadence));
            if (CycleVocabulary.WEEKLY.equals(cadence) || CycleVocabulary.MONTHLY.equals(cadence)) {
                scorecard = scorecard(now);
            }
        } catch (RuntimeException e) {
            log.warn("The cycle state could not be read: {}", e.toString());
        }
        return new CycleState(situations, sets, indicators, scorecard);
    }

    private List<Map<String, Object>> situations(CycleScope scope, String cadence) {
        List<Map<String, Object>> out = new ArrayList<>();
        jdbc.query("SELECT id, title, status, severity, theme, region_codes, last_activity_at, member_count,"
                + " \"private\" FROM situation WHERE merged_into IS NULL AND status <> 'closed'"
                + " ORDER BY CASE severity WHEN 'critical' THEN 0 WHEN 'warning' THEN 1 ELSE 2 END,"
                + " last_activity_at DESC NULLS LAST LIMIT 500", new MapSqlParameterSource(), rs -> {
                    List<String> regions = CyclePacketReader.array(rs, "region_codes");
                    if (out.size() >= MAX_SITUATIONS || !scope.admits(regions, rs.getString("theme"),
                            rs.getString("id"), null, null, List.of(), null, cadence)) {
                        return;
                    }
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("id", rs.getString("id"));
                    s.put("title", rs.getString("title"));
                    s.put("status", rs.getString("status"));
                    s.put("severity", rs.getString("severity"));
                    s.put("theme", rs.getString("theme"));
                    s.put("regionCodes", regions);
                    Instant last = CyclePacketReader.instant(rs, "last_activity_at");
                    s.put("lastActivityAt", last == null ? null : last.toString());
                    s.put("memberCount", rs.getInt("member_count"));
                    s.put("private", rs.getBoolean("private"));
                    out.add(s);
                });
        return out;
    }

    private List<Map<String, Object>> sets(CycleScope scope, String cadence) {
        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        jdbc.query("SELECT h.id, h.question, h.cadence, h.situation_id, h.question_id, h.region_codes, h.themes,"
                + " h.\"private\", h.last_revision_id, h.horizon, s.severity FROM hypothesis_set h"
                + " LEFT JOIN situation s ON s.id = h.situation_id WHERE h.status = 'active'"
                + " ORDER BY h.last_revision_at DESC NULLS LAST, h.id LIMIT 500", new MapSqlParameterSource(), rs -> {
                    List<String> themes = CyclePacketReader.array(rs, "themes");
                    if (byId.size() >= MAX_SETS || !scope.admits(CyclePacketReader.array(rs, "region_codes"),
                            themes.isEmpty() ? null : themes.get(0), rs.getString("situation_id"), rs.getString("id"),
                            rs.getString("cadence"), List.of(), null, cadence)) {
                        return;
                    }
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("setId", rs.getString("id"));
                    s.put("question", rs.getString("question"));
                    s.put("cadence", rs.getString("cadence"));
                    s.put("situationId", rs.getString("situation_id"));
                    s.put("questionId", rs.getString("question_id"));
                    s.put("private", rs.getBoolean("private"));
                    s.put("stakes", CyclePacketReader.stakes(rs.getString("severity")));
                    s.put("horizon", rs.getDate("horizon") == null ? null : rs.getDate("horizon").toString());
                    s.put("lastRevisionId", rs.getString("last_revision_id"));
                    s.put("hypotheses", new ArrayList<Map<String, Object>>());
                    s.put("drivers", List.of());
                    byId.put(rs.getString("id"), s);
                });
        if (byId.isEmpty()) {
            return out;
        }
        MapSqlParameterSource ids = new MapSqlParameterSource("ids", new ArrayList<>(byId.keySet()));
        jdbc.query("SELECT id, set_id, label, probability, estimative_term, type, alternatives::text AS alternatives,"
                + " coincidence_baseline::text AS coincidence FROM hypothesis WHERE set_id IN (:ids)"
                + " ORDER BY set_id, probability DESC, position", ids, rs -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> hyps = (List<Map<String, Object>>) byId.get(rs.getString("set_id"))
                            .get("hypotheses");
                    Map<String, Object> h = new LinkedHashMap<>();
                    h.put("hypothesisId", rs.getString("id"));
                    h.put("label", rs.getString("label"));
                    h.put("probability", rs.getDouble("probability"));
                    h.put("estimativeTerm", rs.getString("estimative_term"));
                    h.put("type", rs.getString("type"));
                    h.put("alternatives", parse(rs.getString("alternatives")));
                    h.put("coincidenceBaseline", rs.getString("coincidence") == null ? null
                            : json.readValue(rs.getString("coincidence"), Object.class));
                    hyps.add(h);
                });
        List<String> revisionIds = new ArrayList<>();
        for (Map<String, Object> s : byId.values()) {
            if (s.get("lastRevisionId") != null) {
                revisionIds.add(String.valueOf(s.get("lastRevisionId")));
            }
        }
        if (!revisionIds.isEmpty()) {
            Map<String, List<Object>> drivers = new HashMap<>();
            jdbc.query("SELECT set_id, drivers::text AS drivers FROM hypothesis_revision WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", revisionIds), rs -> {
                        List<Object> list = parse(rs.getString("drivers"));
                        drivers.put(rs.getString("set_id"), list.size() > 3 ? list.subList(0, 3) : list);
                    });
            drivers.forEach((setId, list) -> byId.get(setId).put("drivers", list));
        }
        out.addAll(byId.values());
        return out;
    }

    private List<Map<String, Object>> indicators(List<Map<String, Object>> sets, Instant now, Duration period) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (sets.isEmpty()) {
            return out;
        }
        List<String> setIds = sets.stream().map(s -> String.valueOf(s.get("setId"))).toList();
        jdbc.query("SELECT id, set_id, description, expected_by, signal_family FROM indicator WHERE set_id IN (:ids)"
                + " AND state = 'pending' AND expected_by IS NOT NULL AND expected_by >= :now AND expected_by < :end"
                + " ORDER BY expected_by LIMIT 100", new MapSqlParameterSource("ids", setIds)
                        .addValue("now", Timestamp.from(now)).addValue("end", Timestamp.from(now.plus(period))), rs -> {
                    Map<String, Object> i = new LinkedHashMap<>();
                    i.put("indicatorId", rs.getString("id"));
                    i.put("setId", rs.getString("set_id"));
                    i.put("description", rs.getString("description"));
                    i.put("signalFamily", rs.getString("signal_family"));
                    i.put("expectedBy", CyclePacketReader.instant(rs, "expected_by").toString());
                    out.add(i);
                });
        return out;
    }

    /**
     * The 90-day scorecard summary: Brier and the base-rate skill on shared questions, the mean forecast minus the
     * observed frequency, per forecaster, plus the ensemble and the pooled operator forecasters.
     */
    public Map<String, Object> scorecard(Instant now) {
        Map<String, double[]> stats = new LinkedHashMap<>();
        Map<String, Map<String, Double>> brierByQuestion = new HashMap<>();
        jdbc.query("SELECT question_id, forecaster_id, brier, final_probability, observed FROM forecast_score"
                + " WHERE resolved_at >= :since AND brier IS NOT NULL", new MapSqlParameterSource("since",
                        Timestamp.from(now.minus(SCORE_WINDOW))), rs -> {
                    String id = rs.getString("forecaster_id");
                    String key = id.startsWith("operator:") ? "operator" : id;
                    double brier = rs.getDouble("brier");
                    accumulate(stats, key, brier, rs.getObject("final_probability"), rs.getObject("observed"));
                    brierByQuestion.computeIfAbsent(key, k -> new HashMap<>()).put(rs.getString("question_id"), brier);
                });
        Map<String, Double> reference = brierByQuestion.getOrDefault("base-rate", Map.of());
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> ensemble = null;
        Map<String, Object> operator = null;
        for (Map.Entry<String, double[]> e : stats.entrySet()) {
            Map<String, Object> row = row(e.getKey(), e.getValue(), brierByQuestion.get(e.getKey()), reference);
            rows.add(row);
            if ("ensemble".equals(e.getKey())) {
                ensemble = row;
            } else if ("operator".equals(e.getKey())) {
                operator = ((Number) row.get("n")).intValue() >= OPERATOR_MIN ? row : null;
            }
        }
        rows.sort((a, b) -> Integer.compare(((Number) b.get("n")).intValue(), ((Number) a.get("n")).intValue()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("window", "P90D");
        out.put("ensemble", ensemble);
        out.put("operator", operator);
        out.put("forecasters", rows);
        return out;
    }

    private static void accumulate(Map<String, double[]> stats, String key, double brier, Object probability,
            Object observed) {
        double[] s = stats.computeIfAbsent(key, k -> new double[5]);
        s[0] += brier;
        s[1]++;
        if (probability instanceof Number p && observed instanceof Number o) {
            s[2] += p.doubleValue();
            s[3] += o.doubleValue();
            s[4]++;
        } else if (probability instanceof Number p && observed instanceof Boolean b) {
            s[2] += p.doubleValue();
            s[3] += b ? 1 : 0;
            s[4]++;
        }
    }

    /** One scorecard row: n, Brier, base-rate skill on shared questions, and the calibration headline. */
    static Map<String, Object> row(String key, double[] s, Map<String, Double> mine, Map<String, Double> reference) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("forecasterId", key);
        row.put("n", (int) s[1]);
        row.put("brier", s[1] == 0 ? null : s[0] / s[1]);
        double own = 0;
        double ref = 0;
        int shared = 0;
        if (mine != null && !key.equals("base-rate")) {
            for (Map.Entry<String, Double> e : mine.entrySet()) {
                Double r = reference.get(e.getKey());
                if (r != null) {
                    own += e.getValue();
                    ref += r;
                    shared++;
                }
            }
        }
        row.put("brierSkill", shared == 0 || ref <= 0 ? null : 1 - own / ref);
        Double bias = s[4] == 0 ? null : (s[2] - s[3]) / s[4];
        row.put("meanForecastMinusObserved", bias);
        row.put("calibration", bias == null ? null : calibrationHeadline(bias, (int) s[1]));
        row.put("label", s[1] < 20 ? "insufficient" : "ok");
        return row;
    }

    /** "well calibrated", "over-forecasts by 7 pp" or "under-forecasts by 4 pp" (within 2 pp is well calibrated). */
    static String calibrationHeadline(double bias, int n) {
        long pp = Math.round(Math.abs(bias) * 100);
        String head = pp <= 2 ? "well calibrated" : (bias > 0 ? "over-forecasts by " : "under-forecasts by ") + pp
                + " pp";
        return head + " over " + n + " resolved";
    }

    private List<Object> parse(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return json.readValue(text, LIST);
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }
}
