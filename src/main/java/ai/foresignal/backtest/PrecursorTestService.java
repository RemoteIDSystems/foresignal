package ai.foresignal.backtest;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Precursor tests (prompt 103 §5): CRUD and the job that runs {@link PrecursorTester}. Antecedent and
 * consequent occurrences are resolved from the configured selectors (an alert rule, an intelligence
 * event type or an anomaly rule) over the job's time range, each query in its own read transaction
 * with the 60 s backtest statement timeout.
 *
 * <p>Occurrences carry what the spatial rules need: a point (the event centroid, or the centroid of
 * the alert's record snapshot geometry), the country and first-level region (the event's region
 * tags, or the alert's region snapshot), and the entity keys {@code source:externalId} (the event's
 * members, the alerted record, or the anomaly group key).
 *
 * <p>This service does not depend on {@link BacktestJobService}; the job service calls it.
 */
@Service
public class PrecursorTestService {

    /** The longest {@code maxLag}. */
    public static final Duration MAX_LAG = Duration.ofDays(30);

    /** The selector keys, exactly one of which a selector carries. */
    public static final Set<String> SELECTORS = Set.of("rule", "alertRuleId", "intelEventType", "anomalyRuleId");

    /** The anomaly classifications counted when a selector names none. */
    public static final List<String> DEFAULT_ANOMALY_CLASSIFICATIONS = List.of("activity-up", "activity-down");

    /** The most occurrences read per selector. */
    public static final int MAX_OCCURRENCES = 200_000;

    private final PrecursorTestRepository tests;
    private final BacktestJobRepository jobs;
    private final TriggerRuleService rules;
    private final ReadQueryRunner reads;
    private final BacktestCoverage coverage;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public PrecursorTestService(PrecursorTestRepository tests, BacktestJobRepository jobs, TriggerRuleService rules,
            ReadQueryRunner reads, BacktestCoverage coverage, JsonMapper json, Clock clock) {
        this.tests = tests;
        this.jobs = jobs;
        this.rules = rules;
        this.reads = reads;
        this.coverage = coverage;
        this.json = json;
        this.clock = clock;
    }

    /** Every test. */
    public List<PrecursorTestEntity> list() {
        return tests.findAllByOrderByCreatedAtDescIdDesc();
    }

    /** One test, or 404. */
    public PrecursorTestEntity get(String id) {
        if (id == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PRECURSOR_TEST, "No such precursor test: null");
        }
        return tests.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PRECURSOR_TEST,
                "No such precursor test: " + id));
    }

    /** Creates a test with no recorded creator. */
    @Transactional
    public PrecursorTestEntity create(JsonNode body) {
        return create(body, null);
    }

    /** Creates a test, recording its creator. */
    @Transactional
    public PrecursorTestEntity create(JsonNode body, String createdBy) {
        Instant now = clock.instant();
        PrecursorTestEntity entity = new PrecursorTestEntity(Ulids.next(now),
                body.path("name").asString("Precursor test"), now);
        entity.setCreatedBy(createdBy);
        apply(entity, body, true);
        return tests.save(entity);
    }

    /** Replaces a test. */
    @Transactional
    public PrecursorTestEntity update(String id, JsonNode body) {
        PrecursorTestEntity entity = get(id);
        apply(entity, body, false);
        return tests.save(entity);
    }

    /** Deletes a test. */
    @Transactional
    public void delete(String id) {
        tests.delete(get(id));
    }

    private void apply(PrecursorTestEntity entity, JsonNode body, boolean creating) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw invalid(List.of(new FieldError("body", "must be an object")));
        }
        if (body.hasNonNull("name")) {
            String name = body.get("name").asString();
            if (name.isBlank() || name.length() > 200) {
                errors.add(new FieldError("name", "must be 1 to 200 characters"));
            } else {
                entity.setName(name);
            }
        }
        if (body.hasNonNull("statement")) {
            entity.setStatement(body.get("statement").asString());
        }
        selector(body, "antecedent", creating, errors).ifPresent(entity::setAntecedent);
        selector(body, "consequent", creating, errors).ifPresent(entity::setConsequent);
        Duration minLag = duration(body, "minLag", errors);
        if (minLag != null) {
            if (minLag.isNegative()) {
                errors.add(new FieldError("minLag", "must not be negative"));
            } else {
                entity.setMinLagSeconds(minLag.toSeconds());
            }
        }
        Duration maxLag = duration(body, "maxLag", errors);
        if (maxLag != null) {
            if (maxLag.compareTo(MAX_LAG) > 0 || maxLag.isNegative() || maxLag.isZero()) {
                errors.add(new FieldError("maxLag", "must be more than zero and at most 30 days"));
            } else {
                entity.setMaxLagSeconds(maxLag.toSeconds());
            }
        }
        if (entity.getMaxLagSeconds() <= 0 && errors.stream().noneMatch(e -> e.field().equals("maxLag"))) {
            errors.add(new FieldError("maxLag", "is required"));
        }
        if (entity.getMinLagSeconds() > entity.getMaxLagSeconds() && entity.getMaxLagSeconds() > 0) {
            errors.add(new FieldError("minLag", "must not exceed maxLag"));
        }
        if (body.has("spatial")) {
            JsonNode spatial = body.get("spatial");
            if (spatial.isString() && "none".equals(spatial.asString())) {
                entity.setSpatial(Map.of());
            } else if (!spatial.isObject()) {
                errors.add(new FieldError("spatial", "must be an object or \"none\""));
            } else {
                Map<String, Object> map = map(spatial);
                String problem = spatialProblem(map);
                if (problem != null) {
                    errors.add(new FieldError("spatial", problem));
                } else {
                    entity.setSpatial(map);
                }
            }
        }
        Duration dedup = duration(body, "dedupWindow", errors);
        if (dedup != null) {
            if (dedup.isNegative()) {
                errors.add(new FieldError("dedupWindow", "must not be negative"));
            } else {
                entity.setDedupWindowSeconds(dedup.toSeconds());
            }
        }
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
    }

    private java.util.Optional<Map<String, Object>> selector(JsonNode body, String field, boolean required,
            List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return java.util.Optional.empty();
        }
        if (!node.isObject()) {
            errors.add(new FieldError(field, "must be an object"));
            return java.util.Optional.empty();
        }
        Map<String, Object> map = map(node);
        long keys = map.keySet().stream().filter(SELECTORS::contains).count();
        if (keys != 1) {
            errors.add(new FieldError(field, "must name exactly one of rule, alertRuleId, intelEventType or"
                    + " anomalyRuleId"));
            return java.util.Optional.empty();
        }
        for (String key : List.of("alertRuleId", "anomalyRuleId")) {
            if (map.get(key) != null) {
                try {
                    UUID.fromString(String.valueOf(map.get(key)));
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError(field + "." + key, "must be a UUID"));
                }
            }
        }
        return java.util.Optional.of(map);
    }

    private static Duration duration(JsonNode body, String field, List<FieldError> errors) {
        if (!body.hasNonNull(field)) {
            return null;
        }
        try {
            return Duration.parse(body.get(field).asString());
        } catch (DateTimeParseException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 duration such as PT6H"));
            return null;
        }
    }

    /** Null when the spatial map is valid, else what is wrong. */
    static String spatialProblem(Map<String, Object> spatial) {
        if (spatial.isEmpty() || spatial.containsKey("none")) {
            return null;
        }
        if (spatial.containsKey("sameEntity")) {
            return null;
        }
        if (spatial.containsKey("withinKm")) {
            return spatial.get("withinKm") instanceof Number km && km.doubleValue() > 0 ? null
                    : "withinKm must be a positive number";
        }
        if (spatial.containsKey("sameRegion")) {
            Object level = spatial.get("sameRegion");
            return "country".equals(level) || "admin1".equals(level) || Boolean.TRUE.equals(level) ? null
                    : "sameRegion must be country or admin1";
        }
        return "must be sameEntity, withinKm, sameRegion or none";
    }

    private static ValidationException invalid(List<FieldError> errors) {
        return new ValidationException(ErrorCodes.PRECURSOR_TEST_INVALID, "The precursor test is not valid.", errors);
    }

    /** The sources whose coverage and retention matter to a test (its alert and anomaly rules' sources). */
    public Set<String> involvedSources(PrecursorTestEntity test) {
        Set<String> out = new LinkedHashSet<>();
        for (Map<String, Object> selector : java.util.Arrays.asList(test.getAntecedent(), test.getConsequent())) {
            if (selector == null) {
                continue;
            }
            Object ruleId = selector.get("alertRuleId") != null ? selector.get("alertRuleId")
                    : selector.get("anomalyRuleId");
            if (ruleId == null) {
                continue;
            }
            try {
                out.addAll(rules.load(UUID.fromString(String.valueOf(ruleId))).getSources());
            } catch (RuntimeException e) {
                // A deleted rule contributes no sources.
            }
        }
        return out;
    }

    /** The job that tests this precursor test over the job's resolved time range. */
    public Map<String, Object> runJob(BacktestJobEntity job) {
        return runJob(job, JobMonitor.NONE);
    }

    /** The job that tests this precursor test over the job's resolved time range, with a monitor. */
    public Map<String, Object> runJob(BacktestJobEntity job, JobMonitor monitor) {
        PrecursorTestEntity test = get(job.getTargetId());
        Instant from = job.getRangeFrom();
        Instant to = job.getRangeTo();
        if (from == null || to == null || !to.isAfter(from)) {
            throw new ValidationException(ErrorCodes.BACKTEST_RANGE_INVALID, "The job has no time range.", List.of());
        }
        monitor.checkpoint(0.0);
        List<PrecursorTester.Occurrence> antecedents = occurrences(test.getAntecedent(), from, to);
        monitor.checkpoint(0.2);
        List<PrecursorTester.Occurrence> consequents = occurrences(test.getConsequent(), from, to);
        monitor.checkpoint(0.4);
        double gapFraction = coverage.gapFraction(involvedSources(test), from, to);
        monitor.checkpoint(0.5);
        Duration maxLag = Duration.ofSeconds(test.getMaxLagSeconds());
        Duration dedup = test.getDedupWindowSeconds() == null ? maxLag : Duration.ofSeconds(test.getDedupWindowSeconds());
        PrecursorTester.Request request = new PrecursorTester.Request(antecedents, consequents,
                Duration.ofSeconds(test.getMinLagSeconds()), maxLag, spatial(test.getSpatial()), dedup, from, to,
                gapFraction, job.getDefinitionHash() + ":" + from + ":" + to);
        PrecursorTester.Result result = PrecursorTester.test(request);
        monitor.checkpoint(0.95);
        long testsLast30d = jobs.countByTypeAndStatusInAndCreatedAtGreaterThanEqual("precursor",
                List.of("running", "done"), clock.instant().minus(Duration.ofDays(30)));
        long hypotheses = Math.max(1, testsLast30d);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("testerVersion", PrecursorTester.VERSION);
        out.put("nAntecedent", result.nAntecedent());
        out.put("nConsequent", result.nConsequent());
        out.put("hits", result.hits());
        out.put("hitRate", result.hitRate());
        out.put("hitRateCi", hitRateCi(result.hitRateInterval()));
        out.put("lag", lag(result));
        out.put("baseRate", result.baseRate());
        out.put("lift", finite(result.lift()));
        out.put("pValue", result.pValue());
        out.put("permutations", PrecursorTester.SHIFTS);
        out.put("seed", Long.toHexString(result.seed()));
        out.put("verdict", result.verdict());
        out.put("coOccurrenceNotPrecedence", result.coOccurrence());
        out.put("statement", PrecursorTester.NOTE);
        PrecursorTester.Direction reverse = result.reverse();
        Map<String, Object> reverseMap = new LinkedHashMap<>();
        reverseMap.put("nAntecedent", reverse.nAntecedent());
        reverseMap.put("hits", reverse.hits());
        reverseMap.put("hitRate", reverse.hitRate());
        reverseMap.put("lift", finite(reverse.lift()));
        reverseMap.put("pValue", reverse.pValue());
        out.put("reverse", reverseMap);
        out.put("coverageGapFraction", gapFraction);
        out.put("note", result.note());
        out.put("multipleTesting", Map.of("testsLast30d", testsLast30d,
                "bonferroniPValue", Math.min(1.0, result.pValue() * hypotheses)));
        return out;
    }

    /**
     * The {@code hitRateCi} object of {@code PrecursorTestResult}.
     *
     * @param interval the Wilson 95% interval {@code [lower, upper]}
     * @return {@code {lower, upper, method: "wilson-95"}}
     */
    static Map<String, Object> hitRateCi(double[] interval) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lower", interval[0]);
        out.put("upper", interval[1]);
        out.put("method", "wilson-95");
        return out;
    }

    /**
     * The {@code lag} object of {@code PrecursorTestResult}: the lag distribution of the hits as ISO-8601
     * durations, each null when there are no hits.
     *
     * @param result the test result (lags in minutes)
     * @return {@code {min, p25, median, p75, max, mean}}
     */
    static Map<String, Object> lag(PrecursorTester.Result result) {
        boolean none = result.hits() == 0;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("min", none ? null : Duration.ofMinutes(result.lagMin()).toString());
        out.put("p25", none ? null : Duration.ofMinutes(result.lagP25()).toString());
        out.put("median", none ? null : Duration.ofMinutes(result.lagMedian()).toString());
        out.put("p75", none ? null : Duration.ofMinutes(result.lagP75()).toString());
        out.put("max", none ? null : Duration.ofMinutes(result.lagMax()).toString());
        out.put("mean", none ? null : Duration.ofSeconds(Math.round(result.lagMeanMinutes() * 60)).toString());
        return out;
    }

    /** Infinity (a zero base rate) is reported as null, which JSON can carry. */
    private static Double finite(double value) {
        return Double.isFinite(value) ? value : null;
    }

    private List<PrecursorTester.Occurrence> occurrences(Map<String, Object> selector, Instant from, Instant to) {
        if (selector == null) {
            return List.of();
        }
        Timestamp start = Timestamp.from(from);
        Timestamp end = Timestamp.from(to);
        if (selector.get("intelEventType") != null) {
            return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
                Map<String, Set<String>> members = new HashMap<>();
                t.getJdbcTemplate().query("SELECT m.event_id, m.source_id, m.external_id FROM intelligence_event_member m"
                                + " JOIN intelligence_event e ON e.id = m.event_id WHERE e.type = ? AND e.start_time >= ?"
                                + " AND e.start_time <= ? AND e.merged_into IS NULL",
                        rs -> {
                            members.computeIfAbsent(rs.getString(1), k -> new HashSet<>())
                                    .add(rs.getString(2) + ":" + rs.getString(3));
                        },
                        String.valueOf(selector.get("intelEventType")), start, end);
                return t.getJdbcTemplate().query("SELECT id, start_time, ST_X(centroid) AS lon, ST_Y(centroid) AS lat,"
                                + " region_tags::text AS tags FROM intelligence_event WHERE type = ? AND start_time >= ?"
                                + " AND start_time <= ? AND merged_into IS NULL ORDER BY start_time, id LIMIT "
                                + MAX_OCCURRENCES,
                        (rs, n) -> {
                            String[] region = regionOf(rs.getString("tags"));
                            return new PrecursorTester.Occurrence(rs.getTimestamp("start_time").toInstant(),
                                    rs.getObject("lon", Double.class), rs.getObject("lat", Double.class), region[0],
                                    region[1], members.getOrDefault(rs.getString("id"), Set.of()));
                        },
                        String.valueOf(selector.get("intelEventType")), start, end);
            });
        }
        if (selector.get("alertRuleId") != null) {
            UUID ruleId = UUID.fromString(String.valueOf(selector.get("alertRuleId")));
            return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.getJdbcTemplate().query(
                    "SELECT fired_at, source_id, external_id, region ->> 'iso2' AS iso2, region ->> 'admin1' AS admin1,"
                            + " CASE WHEN jsonb_typeof(record_snapshot -> 'geometry') = 'object' THEN"
                            + " ST_X(ST_Centroid(ST_GeomFromGeoJSON((record_snapshot -> 'geometry')::text))) END AS lon,"
                            + " CASE WHEN jsonb_typeof(record_snapshot -> 'geometry') = 'object' THEN"
                            + " ST_Y(ST_Centroid(ST_GeomFromGeoJSON((record_snapshot -> 'geometry')::text))) END AS lat"
                            + " FROM alert WHERE rule_id = ? AND fired_at >= ? AND fired_at <= ? AND suppressed = false"
                            + " ORDER BY fired_at, id LIMIT " + MAX_OCCURRENCES,
                    (rs, n) -> alertOccurrence(rs), ruleId, start, end));
        }
        if (selector.get("anomalyRuleId") != null) {
            UUID ruleId = UUID.fromString(String.valueOf(selector.get("anomalyRuleId")));
            List<String> classes = selector.get("classification") == null ? DEFAULT_ANOMALY_CLASSIFICATIONS
                    : List.of(String.valueOf(selector.get("classification")));
            String placeholders = String.join(",", classes.stream().map(c -> "?").toList());
            List<Object> params = new ArrayList<>(List.of(ruleId, start, end));
            params.addAll(classes);
            return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.getJdbcTemplate().query(
                    "SELECT bucket_start, group_key FROM anomaly_observation WHERE rule_id = ? AND bucket_start >= ?"
                            + " AND bucket_start <= ? AND classification IN (" + placeholders + ")"
                            + " ORDER BY bucket_start, group_key LIMIT " + MAX_OCCURRENCES,
                    (rs, n) -> new PrecursorTester.Occurrence(rs.getTimestamp("bucket_start").toInstant(), null, null,
                            null, rs.getString("group_key")),
                    params.toArray()));
        }
        throw new ValidationException(ErrorCodes.PRECURSOR_TEST_INVALID,
                "The inline rule selector is not supported by this tester; use alertRuleId, intelEventType or"
                        + " anomalyRuleId.", List.of(new FieldError("rule", "is not supported")));
    }

    private static PrecursorTester.Occurrence alertOccurrence(ResultSet rs) throws SQLException {
        return new PrecursorTester.Occurrence(rs.getTimestamp("fired_at").toInstant(), rs.getObject("lon", Double.class),
                rs.getObject("lat", Double.class), rs.getString("iso2"), rs.getString("admin1"),
                Set.of(rs.getString("source_id") + ":" + rs.getString("external_id")));
    }

    /**
     * {@code [country, admin1]} from an event's region tags (a JSON array holding the ISO 3166-1
     * alpha-2 country and the first-level region code, which always contains a hyphen).
     */
    String[] regionOf(String tagsJson) {
        String country = null;
        String admin1 = null;
        if (tagsJson != null) {
            try {
                for (JsonNode tag : json.readTree(tagsJson)) {
                    String value = tag.asString();
                    if (value.contains("-")) {
                        admin1 = admin1 == null ? value : admin1;
                    } else if (value.length() == 2) {
                        country = country == null ? value : country;
                    }
                }
            } catch (RuntimeException e) {
                // Unreadable tags leave the region unknown.
            }
        }
        return new String[] {country, admin1};
    }

    /** The tester's spatial rule from the stored map. */
    static PrecursorTester.Spatial spatial(Map<String, Object> spatial) {
        if (spatial == null || spatial.isEmpty()) {
            return PrecursorTester.Spatial.none();
        }
        if (spatial.get("sameEntity") != null) {
            return PrecursorTester.Spatial.sameEntity();
        }
        if (spatial.get("sameRegion") != null) {
            return PrecursorTester.Spatial.sameRegion(String.valueOf(spatial.get("sameRegion")));
        }
        if (spatial.get("withinKm") instanceof Number km) {
            return PrecursorTester.Spatial.withinKm(km.doubleValue());
        }
        return PrecursorTester.Spatial.none();
    }

    private Map<String, Object> map(JsonNode node) {
        return json.readValue(json.writeValueAsString(node), new TypeReference<>() {});
    }
}
