package ai.foresignal.backtest;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Precursor tests (prompt 103): CRUD and the job that runs {@link PrecursorTester}. Antecedent and
 * consequent occurrences are resolved from the configured selectors (an alert rule, an intelligence
 * event type or an anomaly rule) over the job's range.
 */
@Service
public class PrecursorTestService {

    private final PrecursorTestRepository tests;
    private final BacktestJobService jobs;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public PrecursorTestService(PrecursorTestRepository tests, BacktestJobService jobs, JdbcTemplate jdbc,
            JsonMapper json, Clock clock) {
        this.tests = tests;
        this.jobs = jobs;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** Every test. */
    public List<PrecursorTestEntity> list() {
        return tests.findAllByOrderByCreatedAtDescIdDesc();
    }

    /** One test, or 404. */
    public PrecursorTestEntity get(String id) {
        return tests.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PRECURSOR_TEST,
                "No such precursor test: " + id));
    }

    /** Creates a test. */
    @Transactional
    public PrecursorTestEntity create(JsonNode body) {
        Instant now = clock.instant();
        PrecursorTestEntity entity = new PrecursorTestEntity(Ulids.next(now),
                body.path("name").asString("Precursor test"), now);
        apply(entity, body);
        return tests.save(entity);
    }

    /** Replaces a test. */
    @Transactional
    public PrecursorTestEntity update(String id, JsonNode body) {
        PrecursorTestEntity entity = get(id);
        apply(entity, body);
        return tests.save(entity);
    }

    /** Deletes a test. */
    @Transactional
    public void delete(String id) {
        tests.delete(get(id));
    }

    private void apply(PrecursorTestEntity entity, JsonNode body) {
        if (body.hasNonNull("name")) {
            entity.setName(body.get("name").asString());
        }
        if (body.hasNonNull("statement")) {
            entity.setStatement(body.get("statement").asString());
        }
        if (body.has("antecedent") && body.get("antecedent").isObject()) {
            entity.setAntecedent(map(body.get("antecedent")));
        }
        if (body.has("consequent") && body.get("consequent").isObject()) {
            entity.setConsequent(map(body.get("consequent")));
        }
        if (body.hasNonNull("minLag")) {
            entity.setMinLagSeconds(Duration.parse(body.get("minLag").asString()).toSeconds());
        }
        if (body.hasNonNull("maxLag")) {
            Duration maxLag = Duration.parse(body.get("maxLag").asString());
            if (maxLag.compareTo(Duration.ofDays(30)) > 0) {
                throw ValidationException.forField("maxLag", "must be at most 30 days");
            }
            entity.setMaxLagSeconds(maxLag.toSeconds());
        }
        if (entity.getMaxLagSeconds() <= 0) {
            throw ValidationException.forField("maxLag", "is required");
        }
        if (body.has("spatial") && body.get("spatial").isObject()) {
            entity.setSpatial(map(body.get("spatial")));
        }
        if (body.hasNonNull("dedupWindow")) {
            entity.setDedupWindowSeconds(Duration.parse(body.get("dedupWindow").asString()).toSeconds());
        }
    }

    /** The job that tests this precursor test. */
    public Map<String, Object> runJob(BacktestJobEntity job) {
        PrecursorTestEntity test = get(job.getTargetId());
        Instant from = job.getRangeFrom() == null ? clock.instant().minus(Duration.ofDays(30)) : job.getRangeFrom();
        Instant to = job.getRangeTo() == null ? clock.instant() : job.getRangeTo();
        List<PrecursorTester.Occurrence> antecedents = occurrences(test.getAntecedent(), from, to);
        List<PrecursorTester.Occurrence> consequents = occurrences(test.getConsequent(), from, to);
        PrecursorTester.Request request = new PrecursorTester.Request(antecedents, consequents,
                Duration.ofSeconds(test.getMinLagSeconds()), Duration.ofSeconds(test.getMaxLagSeconds()),
                spatial(test.getSpatial()),
                test.getDedupWindowSeconds() == null ? null : Duration.ofSeconds(test.getDedupWindowSeconds()), from, to,
                0.0, test.getId() + ":" + from + ":" + to);
        PrecursorTester.Result result = PrecursorTester.test(request);
        long testsLast30d = tests.countByCreatedAtGreaterThanEqual(clock.instant().minus(Duration.ofDays(30)));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nAntecedent", result.nAntecedent());
        out.put("hits", result.hits());
        out.put("hitRate", result.hitRate());
        out.put("hitRateInterval", List.of(result.hitRateInterval()[0], result.hitRateInterval()[1]));
        out.put("lag", Map.of("min", result.lagMin(), "p25", result.lagP25(), "median", result.lagMedian(),
                "p75", result.lagP75(), "max", result.lagMax(), "meanMinutes", result.lagMeanMinutes()));
        out.put("baseRate", result.baseRate());
        out.put("lift", result.lift());
        out.put("pValue", result.pValue());
        out.put("verdict", result.verdict());
        out.put("coOccurrence", result.coOccurrence());
        out.put("note", result.note());
        out.put("multipleTesting", Map.of("testsLast30d", testsLast30d,
                "bonferroniPValue", Math.min(1.0, result.pValue() * Math.max(1, testsLast30d))));
        return out;
    }

    private List<PrecursorTester.Occurrence> occurrences(Map<String, Object> selector, Instant from, Instant to) {
        List<PrecursorTester.Occurrence> out = new ArrayList<>();
        if (selector == null) {
            return out;
        }
        if (selector.get("intelEventType") != null) {
            jdbc.query("SELECT id, start_time, ST_X(centroid) AS lon, ST_Y(centroid) AS lat FROM intelligence_event"
                            + " WHERE type = ? AND start_time >= ? AND start_time <= ?",
                    (rs, n) -> {
                        Instant t = rs.getTimestamp("start_time").toInstant();
                        out.add(new PrecursorTester.Occurrence(t, rs.getObject("lon", Double.class),
                                rs.getObject("lat", Double.class), null, rs.getString("id")));
                        return null;
                    }, String.valueOf(selector.get("intelEventType")), java.sql.Timestamp.from(from),
                    java.sql.Timestamp.from(to));
        } else if (selector.get("alertRuleId") != null) {
            jdbc.query("SELECT fired_at, source_id, external_id FROM alert WHERE rule_id = ? AND fired_at >= ? AND"
                            + " fired_at <= ?",
                    (rs, n) -> {
                        out.add(new PrecursorTester.Occurrence(rs.getTimestamp("fired_at").toInstant(), null, null,
                                null, rs.getString("source_id") + ":" + rs.getString("external_id")));
                        return null;
                    },
                    UUID.fromString(String.valueOf(selector.get("alertRuleId"))), java.sql.Timestamp.from(from),
                    java.sql.Timestamp.from(to));
        } else if (selector.get("anomalyRuleId") != null) {
            jdbc.query("SELECT bucket_start, group_key FROM anomaly_observation WHERE rule_id = ? AND bucket_start >= ?"
                            + " AND bucket_start <= ?",
                    (rs, n) -> {
                        out.add(new PrecursorTester.Occurrence(rs.getTimestamp("bucket_start").toInstant(), null, null,
                                null, rs.getString("group_key")));
                        return null;
                    },
                    UUID.fromString(String.valueOf(selector.get("anomalyRuleId"))), java.sql.Timestamp.from(from),
                    java.sql.Timestamp.from(to));
        }
        return out;
    }

    private static PrecursorTester.Spatial spatial(Map<String, Object> spatial) {
        if (spatial == null || spatial.isEmpty()) {
            return PrecursorTester.Spatial.none();
        }
        if (spatial.get("sameEntity") != null) {
            return PrecursorTester.Spatial.sameEntity();
        }
        if (spatial.get("sameRegion") != null) {
            return PrecursorTester.Spatial.sameRegion();
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
