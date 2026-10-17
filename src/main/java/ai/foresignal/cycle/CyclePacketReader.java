package ai.foresignal.cycle;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.HealthStatus;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the raw inputs of a cycle packet from the intelligence tables (prompt 128 §2): situation transitions (118
 * situation log), hypothesis revisions, band changes, indicators and assumptions (125), findings (119–127), ledger
 * questions and scores (122), index and outlook changes (126), source health, low reporting density (121) and the
 * 125 collection gaps. Every query is bounded by the period; scope and ranking are applied by
 * {@link CyclePacketBuilder}.
 */
@Component
public class CyclePacketReader {

    private static final Logger log = LoggerFactory.getLogger(CyclePacketReader.class);

    /** The most rows one section query reads. */
    static final int ROW_LIMIT = 2_000;

    /** The smallest index move, in points, the packet reports. */
    static final double INDEX_MOVE = 5.0;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};
    private static final TypeReference<List<Object>> LIST = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<HealthService> health;
    private final JsonMapper json;

    /** Creates the reader. */
    public CyclePacketReader(NamedParameterJdbcTemplate jdbc, ObjectProvider<HealthService> health, JsonMapper json) {
        this.jdbc = jdbc;
        this.health = health;
        this.json = json;
    }

    /**
     * Reads the inputs of a period.
     *
     * @param since the period start
     * @param until the period end (also "now" for due items)
     * @param firstRun whether {@code since} is the natural lookback
     * @param cadence the cadence (the forecast horizon)
     * @param previousJudgments the previous assessment's judgments
     */
    public CyclePacketBuilder.Inputs read(Instant since, Instant until, boolean firstRun, String cadence,
            List<Map<String, Object>> previousJudgments) {
        Map<String, List<CyclePacketItem>> sections = new LinkedHashMap<>();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("since", Timestamp.from(since))
                .addValue("until", Timestamp.from(until)).addValue("limit", ROW_LIMIT);
        safely("situations", () -> situations(p, sections));
        safely("hypotheses", () -> hypotheses(p, sections));
        safely("findings", () -> findings(p, sections));
        safely("forecasts", () -> forecasts(p, until, CycleVocabulary.horizon(cadence), sections));
        safely("outlooks", () -> outlooks(since, until, p, sections));
        List<String> failing = new ArrayList<>();
        List<String> degraded = new ArrayList<>();
        safely("coverage", () -> coverage(failing, degraded));
        List<String> lowDensity = new ArrayList<>();
        safely("reporting density", () -> lowDensity.addAll(jdbc.queryForList(
                "SELECT region_code FROM region_profile WHERE density_decile = 1 ORDER BY region_code",
                new MapSqlParameterSource(), String.class)));
        List<Map<String, Object>> gaps = new ArrayList<>();
        safely("collection gaps", () -> gaps.addAll(collectionGaps()));
        return new CyclePacketBuilder.Inputs(since, until, firstRun, sections, failing, degraded, lowDensity, gaps,
                previousJudgments);
    }

    private void safely(String what, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            log.warn("The cycle packet could not read {}: {}", what, e.toString());
        }
    }

    // ------------------------------------------------------------------------- situations

    private void situations(MapSqlParameterSource p, Map<String, List<CyclePacketItem>> out) {
        jdbc.query("SELECT id, title, theme, status, severity, region_codes, created_at, \"private\""
                + " FROM situation WHERE created_at >= :since AND created_at < :until AND merged_into IS NULL"
                + " ORDER BY created_at DESC LIMIT :limit", p, rs -> {
                    String severity = rs.getString("severity");
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("status", rs.getString("status"));
                    detail.put("theme", rs.getString("theme"));
                    add(out, "situations.new", new CyclePacketItem("situation:" + rs.getString("id"),
                            rs.getString("title"), "new", severity, instant(rs, "created_at"), stakes(severity),
                            array(rs, "region_codes"), rs.getString("theme"), rs.getString("id"), null, null, null,
                            null, rs.getBoolean("private"), detail));
                });
        jdbc.query("SELECT l.situation_id, l.at, l.event, l.before::text AS before_state, l.after::text AS after_state,"
                + " s.title, s.theme, s.severity, s.region_codes, s.\"private\" FROM situation_log l"
                + " JOIN situation s ON s.id = l.situation_id WHERE l.at >= :since AND l.at < :until AND l.event IN"
                + " ('situation.activated', 'situation.escalated', 'situation.dormant', 'situation.closed')"
                + " ORDER BY l.at DESC LIMIT :limit", p, rs -> {
                    String event = rs.getString("event");
                    String change = event.substring("situation.".length());
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("before", map(rs.getString("before_state")));
                    detail.put("after", map(rs.getString("after_state")));
                    String severity = rs.getString("severity");
                    add(out, "situations." + change, new CyclePacketItem("situation:" + rs.getString("situation_id"),
                            rs.getString("title"), change, severity, instant(rs, "at"), stakes(severity),
                            array(rs, "region_codes"), rs.getString("theme"), rs.getString("situation_id"), null, null,
                            null, null, rs.getBoolean("private"), detail));
                });
    }

    // ------------------------------------------------------------------------- hypotheses

    private void hypotheses(MapSqlParameterSource p, Map<String, List<CyclePacketItem>> out) {
        String set = " s.question, s.cadence, s.situation_id, s.region_codes, s.themes, s.\"private\" ";
        jdbc.query("SELECT r.id, r.set_id, r.at, r.cause, r.before_state::text AS b, r.after_state::text AS a,"
                + " r.drivers::text AS drivers, r.rationale, r.band_changed," + set + "FROM hypothesis_revision r"
                + " JOIN hypothesis_set s ON s.id = r.set_id WHERE r.at >= :since AND r.at < :until"
                + " AND r.cause <> 'created' ORDER BY r.at DESC LIMIT :limit", p, rs -> {
                    List<Object> before = list(rs.getString("b"));
                    List<Object> after = list(rs.getString("a"));
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("revisionId", rs.getString("id"));
                    detail.put("cause", rs.getString("cause"));
                    detail.put("before", before);
                    detail.put("after", after);
                    List<Object> drivers = list(rs.getString("drivers"));
                    detail.put("drivers", drivers.size() > 5 ? drivers.subList(0, 5) : drivers);
                    detail.put("rationale", rs.getString("rationale"));
                    detail.put("bandChanged", rs.getBoolean("band_changed"));
                    add(out, "hypotheses.revisions", setItem(rs, "hypothesis-set:" + rs.getString("set_id"),
                            rs.getString("question"), "revised", instant(rs, "at"), maxMove(before, after), detail));
                });
        jdbc.query("SELECT h.event, h.set_id, h.payload::text AS payload, h.\"private\" AS log_private, h.at,"
                + set + "FROM hypothesis_log h JOIN hypothesis_set s ON s.id = h.set_id WHERE h.at >= :since"
                + " AND h.at < :until AND h.event IN ('set.band_changed', 'indicator.observed', 'indicator.absent',"
                + " 'assumption.broken') ORDER BY h.at DESC LIMIT :limit", p, rs -> {
                    String event = rs.getString("event");
                    Map<String, Object> payload = map(rs.getString("payload"));
                    Instant at = instant(rs, "at");
                    switch (event) {
                        case "set.band_changed" -> {
                            for (Object raw : payload.get("bandChanges") instanceof List<?> l ? l : List.of()) {
                                if (raw instanceof Map<?, ?> change) {
                                    Map<String, Object> detail = new LinkedHashMap<>();
                                    change.forEach((k, v) -> detail.put(String.valueOf(k), v));
                                    detail.put("setId", rs.getString("set_id"));
                                    add(out, "hypotheses.bandChanges", setItem(rs, "hypothesis:"
                                            + change.get("hypothesisId"), rs.getString("question") + ": "
                                            + change.get("label"), "band-changed", at, 1.0, detail));
                                }
                            }
                        }
                        case "indicator.observed", "indicator.absent" -> {
                            Map<String, Object> detail = new LinkedHashMap<>();
                            detail.put("setId", rs.getString("set_id"));
                            detail.put("signalFamily", payload.get("signalFamily"));
                            detail.put("evidence", payload.get("evidence"));
                            detail.put("revisionId", payload.get("revisionId"));
                            boolean observed = event.endsWith("observed");
                            add(out, observed ? "hypotheses.indicatorsObserved" : "hypotheses.indicatorsAbsent",
                                    setItem(rs, "indicator:" + payload.get("indicatorId"),
                                            String.valueOf(payload.get("description")), observed ? "observed"
                                                    : "absent", at, observed ? 1.0 : 0.5, detail));
                        }
                        default -> {
                            Map<String, Object> detail = new LinkedHashMap<>();
                            detail.put("setId", rs.getString("set_id"));
                            detail.put("linchpin", payload.get("linchpin"));
                            detail.put("previousStatus", payload.get("previousStatus"));
                            add(out, "hypotheses.assumptionsBroken", setItem(rs, "assumption:"
                                    + payload.get("assumptionId"), String.valueOf(payload.get("text")), "broken", at,
                                    Boolean.TRUE.equals(payload.get("linchpin")) ? 2.0 : 1.0, detail));
                        }
                    }
                });
        jdbc.query("SELECT i.id, i.set_id, i.description, i.last_state_change_at, i.p_detect," + set
                + "FROM indicator i JOIN hypothesis_set s ON s.id = i.set_id WHERE i.state = 'unknown-coverage'"
                + " AND i.last_state_change_at >= :since AND i.last_state_change_at < :until LIMIT :limit", p, rs -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("setId", rs.getString("set_id"));
                    detail.put("pDetect", rs.getObject("p_detect"));
                    add(out, "hypotheses.indicatorsUnknownCoverage", setItem(rs, "indicator:" + rs.getString("id"),
                            rs.getString("description"), "unknown-coverage", instant(rs, "last_state_change_at"), 0.5,
                            detail));
                });
        jdbc.query("SELECT a.id, a.set_id, a.text, a.status, a.linchpin, a.updated_at," + set
                + "FROM key_assumption a JOIN hypothesis_set s ON s.id = a.set_id WHERE a.status IN ('caveated',"
                + " 'unsupported') AND a.updated_at >= :since AND a.updated_at < :until LIMIT :limit", p, rs -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("setId", rs.getString("set_id"));
                    detail.put("status", rs.getString("status"));
                    detail.put("linchpin", rs.getBoolean("linchpin"));
                    add(out, "hypotheses.assumptionsQuestioned", setItem(rs, "assumption:" + rs.getString("id"),
                            rs.getString("text"), "questioned", instant(rs, "updated_at"),
                            rs.getBoolean("linchpin") ? 2.0 : 1.0, detail));
                });
        jdbc.query("SELECT s.id AS set_id, s.next_review_at, s.review_flagged, s.horizon," + set
                + "FROM hypothesis_set s WHERE s.status = 'active' AND s.next_review_at <= :until"
                + " ORDER BY s.next_review_at LIMIT :limit", p, rs -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("cadence", rs.getString("cadence"));
                    detail.put("nextReviewAt", instant(rs, "next_review_at") == null ? null
                            : instant(rs, "next_review_at").toString());
                    detail.put("reviewFlagged", rs.getBoolean("review_flagged"));
                    add(out, "hypotheses.dueForReview", setItem(rs, "hypothesis-set:" + rs.getString("set_id"),
                            rs.getString("question"), "due-for-review", instant(rs, "next_review_at"),
                            rs.getBoolean("review_flagged") ? 2.0 : 1.0, detail));
                });
    }

    private CyclePacketItem setItem(ResultSet rs, String ref, String title, String change, Instant at, double score,
            Map<String, Object> detail) throws SQLException {
        List<String> themes = array(rs, "themes");
        return new CyclePacketItem(ref, title, change, null, at, score, array(rs, "region_codes"),
                themes.isEmpty() ? null : themes.get(0), rs.getString("situation_id"), rs.getString("set_id"),
                rs.getString("cadence"), null, null, rs.getBoolean("private"), detail);
    }

    /** The largest probability move between two {@code HypothesisState} lists. */
    static double maxMove(List<Object> before, List<Object> after) {
        Map<String, Double> b = new HashMap<>();
        for (Object raw : before) {
            if (raw instanceof Map<?, ?> m && m.get("probability") instanceof Number n) {
                b.put(String.valueOf(m.get("hypothesisId")), n.doubleValue());
            }
        }
        double max = 0;
        for (Object raw : after) {
            if (raw instanceof Map<?, ?> m && m.get("probability") instanceof Number n) {
                Double prior = b.get(String.valueOf(m.get("hypothesisId")));
                if (prior != null) {
                    max = Math.max(max, Math.abs(n.doubleValue() - prior));
                }
            }
        }
        return max;
    }

    // ------------------------------------------------------------------------- findings

    private void findings(MapSqlParameterSource p, Map<String, List<CyclePacketItem>> out) {
        jdbc.query("SELECT id, finding_type, title, severity, region_codes, detected_at, base_rate::text AS base_rate,"
                + " caveats::text AS caveats, situation_id, \"private\", signal_family FROM analytic_finding"
                + " WHERE detected_at >= :since AND detected_at < :until AND status = 'open'"
                + " ORDER BY detected_at DESC LIMIT :limit", p, rs -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("findingType", rs.getString("finding_type"));
                    detail.put("signalFamily", rs.getString("signal_family"));
                    detail.put("baseRate", rs.getString("base_rate") == null ? null : map(rs.getString("base_rate")));
                    List<Object> caveats = list(rs.getString("caveats"));
                    detail.put("caveats", caveats);
                    String severity = rs.getString("severity");
                    add(out, "findings", new CyclePacketItem("finding:" + rs.getString("id"), rs.getString("title"),
                            rs.getString("finding_type"), severity, instant(rs, "detected_at"),
                            findingScore(severity), array(rs, "region_codes"), null, rs.getString("situation_id"),
                            null, null, null, null, rs.getBoolean("private"), detail));
                });
    }

    /** The ranking weight of a finding severity. */
    static double findingScore(String severity) {
        return switch (severity == null ? "" : severity) {
            case "critical" -> 3;
            case "significant" -> 2;
            case "notable" -> 1;
            default -> 0;
        };
    }

    /** The stakes of a situation severity: critical 3, warning 2, otherwise 1. */
    public static double stakes(String severity) {
        return switch (severity == null ? "" : severity) {
            case "critical" -> 3;
            case "warning" -> 2;
            default -> 1;
        };
    }

    // ------------------------------------------------------------------------- forecasts

    private void forecasts(MapSqlParameterSource p, Instant now, Duration horizon,
            Map<String, List<CyclePacketItem>> out) {
        String cols = "q.id, q.title, q.type, q.status, q.category, q.situation_id, q.hypothesis_set_id,"
                + " q.ensemble_probability, q.resolve_by, q.closes_at, q.created_at, q.updated_at, q.\"private\","
                + " q.resolution::text AS resolution, s.severity AS situation_severity, h.cadence AS set_cadence";
        String from = " FROM forecast_question q LEFT JOIN situation s ON s.id = q.situation_id"
                + " LEFT JOIN hypothesis_set h ON h.id = q.hypothesis_set_id ";
        jdbc.query("SELECT " + cols + from + "WHERE q.created_at >= :since AND q.created_at < :until"
                + " ORDER BY q.created_at DESC LIMIT :limit", p, rs -> {
                    add(out, "forecasts.opened", question(rs, "opened", instant(rs, "created_at"), 1.0));
                });
        MapSqlParameterSource due = new MapSqlParameterSource(p.getValues()).addValue("now", Timestamp.from(now))
                .addValue("horizon", Timestamp.from(now.plus(horizon)));
        jdbc.query("SELECT " + cols + from + "WHERE q.status IN ('open', 'closed') AND q.resolve_by >= :now"
                + " AND q.resolve_by < :horizon ORDER BY q.resolve_by LIMIT :limit", due, rs -> {
                    Double prob = rs.getObject("ensemble_probability") == null ? null
                            : rs.getDouble("ensemble_probability");
                    double uncertainty = prob == null ? 1.0 : 4 * prob * (1 - prob);
                    add(out, "forecasts.due", question(rs, "due", instant(rs, "resolve_by"),
                            stakes(rs.getString("situation_severity")) * uncertainty));
                });
        List<CyclePacketItem> resolved = new ArrayList<>();
        jdbc.query("SELECT " + cols + from + "WHERE q.status = 'resolved' AND q.updated_at >= :since"
                + " AND q.updated_at < :until ORDER BY q.updated_at DESC LIMIT :limit", p, rs -> {
                    resolved.add(question(rs, "resolved", instant(rs, "updated_at"), 1.0));
                });
        if (!resolved.isEmpty()) {
            Map<String, List<Map<String, Object>>> scores = new HashMap<>();
            jdbc.query("SELECT question_id, forecaster_id, brier, log_score, crps FROM forecast_score"
                    + " WHERE question_id IN (:ids) ORDER BY forecaster_id", new MapSqlParameterSource("ids",
                            resolved.stream().map(CyclePacketItem::id).toList()), rs -> {
                        Map<String, Object> s = new LinkedHashMap<>();
                        s.put("forecasterId", rs.getString("forecaster_id"));
                        s.put("brier", rs.getObject("brier"));
                        s.put("logScore", rs.getObject("log_score"));
                        s.put("crps", rs.getObject("crps"));
                        scores.computeIfAbsent(rs.getString("question_id"), k -> new ArrayList<>()).add(s);
                    });
            for (CyclePacketItem item : resolved) {
                Map<String, Object> detail = new LinkedHashMap<>(item.detail());
                detail.put("scores", scores.getOrDefault(item.id(), List.of()));
                add(out, "forecasts.resolved", new CyclePacketItem(item.ref(), item.title(), item.change(),
                        item.severity(), item.at(), item.score(), item.regionCodes(), item.theme(),
                        item.situationId(), item.setId(), item.setCadence(), item.sourceIds(), item.category(),
                        item.privateItem(), detail));
            }
        }
        Map<String, double[]> window = new TreeMap<>();
        Map<String, double[]> overall = new HashMap<>();
        jdbc.query("SELECT forecaster_id, brier, computed_at >= :since AND computed_at < :until AS in_window"
                + " FROM forecast_score WHERE brier IS NOT NULL", p, rs -> {
                    double brier = rs.getDouble("brier");
                    String id = rs.getString("forecaster_id");
                    double[] all = overall.computeIfAbsent(id, k -> new double[2]);
                    all[0] += brier;
                    all[1]++;
                    if (rs.getBoolean("in_window")) {
                        double[] w = window.computeIfAbsent(id, k -> new double[2]);
                        w[0] += brier;
                        w[1]++;
                    }
                });
        window.forEach((id, w) -> {
            double[] all = overall.get(id);
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("forecasterId", id);
            detail.put("newlyScored", (int) w[1]);
            detail.put("periodBrier", w[0] / w[1]);
            detail.put("overallBrier", all[0] / all[1]);
            detail.put("nResolved", (int) all[1]);
            out.computeIfAbsent("forecasts.scorecardChanges", k -> new ArrayList<>()).add(new CyclePacketItem(
                    "forecaster:" + id, "Scorecard of " + id, "scored", null, null, w[1], List.of(), null, null, null,
                    null, null, null, false, detail));
        });
    }

    private CyclePacketItem question(ResultSet rs, String change, Instant at, double score) throws SQLException {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("type", rs.getString("type"));
        detail.put("status", rs.getString("status"));
        detail.put("category", rs.getString("category"));
        detail.put("ensembleProbability", rs.getObject("ensemble_probability"));
        Instant resolveBy = instant(rs, "resolve_by");
        detail.put("resolveBy", resolveBy == null ? null : resolveBy.toString());
        detail.put("situationId", rs.getString("situation_id"));
        detail.put("hypothesisSetId", rs.getString("hypothesis_set_id"));
        if (rs.getString("resolution") != null) {
            detail.put("resolution", map(rs.getString("resolution")));
        }
        return new CyclePacketItem("question:" + rs.getString("id"), rs.getString("title"), change, null, at, score,
                List.of(), null, rs.getString("situation_id"), rs.getString("hypothesis_set_id"),
                rs.getString("set_cadence"), null, rs.getString("category"), rs.getBoolean("private"), detail);
    }

    // ------------------------------------------------------------------------- outlooks and indices

    private void outlooks(Instant since, Instant until, MapSqlParameterSource p,
            Map<String, List<CyclePacketItem>> out) {
        MapSqlParameterSource days = new MapSqlParameterSource()
                .addValue("from", java.sql.Date.valueOf(LocalDate.ofInstant(since, ZoneOffset.UTC).minusDays(1)))
                .addValue("to", java.sql.Date.valueOf(LocalDate.ofInstant(until, ZoneOffset.UTC)));
        Map<String, double[]> firstLast = new LinkedHashMap<>();
        Map<String, String[]> names = new HashMap<>();
        jdbc.query("SELECT v.def_id, v.region, v.day, v.value, d.name FROM regional_index_value v"
                + " JOIN regional_index_def d ON d.id = v.def_id WHERE v.day >= :from AND v.day <= :to"
                + " ORDER BY v.def_id, v.region, v.day", days, rs -> {
                    String key = rs.getString("def_id") + "|" + rs.getString("region");
                    double value = rs.getDouble("value");
                    double[] fl = firstLast.computeIfAbsent(key, k -> new double[] {value, value});
                    fl[1] = value;
                    names.put(key, new String[] {rs.getString("name"), rs.getString("region"), rs.getString("def_id"),
                        rs.getDate("day").toString()});
                });
        firstLast.forEach((key, fl) -> {
            double delta = fl[1] - fl[0];
            if (Math.abs(delta) < INDEX_MOVE) {
                return;
            }
            String[] n = names.get(key);
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("indexId", n[2]);
            detail.put("region", n[1]);
            detail.put("from", fl[0]);
            detail.put("to", fl[1]);
            detail.put("delta", delta);
            detail.put("day", n[3]);
            add(out, "outlooks.indexChanges", new CyclePacketItem("index:" + n[2] + "/" + n[1], n[0] + " in " + n[1],
                    delta > 0 ? "rose" : "fell", null, null, Math.abs(delta), List.of(n[1]), null, null, null, null,
                    null, null, false, detail));
        });
        jdbc.query("SELECT id, region, family, windows::text AS windows, updated_at FROM regional_outlook"
                + " WHERE updated_at >= :since AND updated_at < :until LIMIT :limit", p, rs -> {
                    for (Object raw : list(rs.getString("windows"))) {
                        if (raw instanceof Map<?, ?> w && "resolved".equals(w.get("status"))
                                && Boolean.FALSE.equals(w.get("withinP10P90"))) {
                            Map<String, Object> detail = new LinkedHashMap<>();
                            detail.put("outlookId", rs.getString("id"));
                            detail.put("family", rs.getString("family"));
                            detail.put("window", w.get("index"));
                            detail.put("windowStart", w.get("windowStart"));
                            detail.put("actual", w.get("actual"));
                            detail.put("exceedance", w.get("exceedance"));
                            add(out, "outlooks.windowsOutsideP10P90", new CyclePacketItem("outlook:" + rs.getString("id"),
                                    rs.getString("family") + " outlook for " + rs.getString("region") + ", window "
                                            + w.get("index"), "outside-p10-p90", null, instant(rs, "updated_at"),
                                    w.get("exceedance") == null ? 1.0 : 2.0, List.of(rs.getString("region")), null,
                                    null, null, null, null, null, false, detail));
                        }
                    }
                });
    }

    // ------------------------------------------------------------------------- coverage and gaps

    private void coverage(List<String> failing, List<String> degraded) {
        HealthService service = health.getIfAvailable();
        if (service == null) {
            return;
        }
        for (SourceHealthView view : service.all(Optional.of(Set.of(HealthStatus.FAILING, HealthStatus.DEGRADED)))) {
            (view.status() == HealthStatus.FAILING ? failing : degraded).add(view.sourceId());
        }
    }

    private List<Map<String, Object>> collectionGaps() {
        List<Map<String, Object>> out = new ArrayList<>();
        jdbc.query("SELECT g.id, g.set_id, g.indicator_id, g.potential_eig, g.actual_eig, g.p_detect,"
                + " g.recommendations::text AS recommendations, g.created_at, g.updated_at, s.cadence, s.situation_id,"
                + " s.region_codes, s.\"private\" FROM collection_gap g JOIN hypothesis_set s ON s.id = g.set_id"
                + " WHERE s.status = 'active' ORDER BY g.potential_eig DESC LIMIT :limit",
                new MapSqlParameterSource("limit", ROW_LIMIT), rs -> {
                    Map<String, Object> gap = new LinkedHashMap<>();
                    gap.put("id", rs.getString("id"));
                    gap.put("setId", rs.getString("set_id"));
                    gap.put("indicatorId", rs.getString("indicator_id"));
                    gap.put("potentialEig", rs.getDouble("potential_eig"));
                    gap.put("actualEig", rs.getDouble("actual_eig"));
                    gap.put("pDetect", rs.getObject("p_detect"));
                    gap.put("recommendations", list(rs.getString("recommendations")));
                    gap.put("createdAt", instant(rs, "created_at") == null ? null : instant(rs, "created_at").toString());
                    gap.put("updatedAt", instant(rs, "updated_at") == null ? null : instant(rs, "updated_at").toString());
                    gap.put("setCadence", rs.getString("cadence"));
                    gap.put("situationId", rs.getString("situation_id"));
                    gap.put("regionCodes", array(rs, "region_codes"));
                    gap.put("private", rs.getBoolean("private"));
                    out.add(gap);
                });
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private static void add(Map<String, List<CyclePacketItem>> out, String section, CyclePacketItem item) {
        out.computeIfAbsent(section, k -> new ArrayList<>()).add(item);
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    static List<String> array(ResultSet rs, String column) throws SQLException {
        Array array = rs.getArray(column);
        if (array == null) {
            return List.of();
        }
        Object raw = array.getArray();
        List<String> out = new ArrayList<>();
        if (raw instanceof Object[] values) {
            for (Object v : values) {
                if (v != null) {
                    out.add(String.valueOf(v));
                }
            }
        }
        return out;
    }

    private Map<String, Object> map(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return new LinkedHashMap<>();
        }
    }

    private List<Object> list(String text) {
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
