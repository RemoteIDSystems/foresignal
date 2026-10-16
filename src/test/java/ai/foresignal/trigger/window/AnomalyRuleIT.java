package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The anomaly rule and its coverage-aware baseline (prompt 100), on the frozen {@link MutableClock}:
 * the rule is learning until every slot has {@code minWeeks} of coverage-ok history; a spike is
 * {@code activity-up} with the detector version, real evidence seqs and {@code fetch_log} ranges, and
 * alerts with {@code context.anomaly} and the honest default message; halving the receiver count is a
 * {@code coverage-drop}, never an alert; and the durable cursor catches up the buckets missed during
 * downtime, in order.
 */
@WebIntegrationTest
@Import({TestIngestSources.class, CoverageSupport.Config.class})
class AnomalyRuleIT {

    /** Two minutes past noon: the bucket [11:00, 12:00) has settled. */
    private static final Instant NOW = Instant.parse("2026-03-01T12:03:00Z");
    private static final Instant CURRENT = Instant.parse("2026-03-01T11:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired ActivitySampleRepository samples;
    @Autowired AnomalyObservationRepository observations;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
        sources.update(SOURCE, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    private UUID createRule() {
        return rules.create(json.readTree("{\"name\":\"unusual\",\"type\":\"anomaly\",\"sources\":[\"" + SOURCE + "\"],"
                + "\"window\":{\"window\":\"1h\",\"direction\":\"both\",\"minWeeks\":8}}")).rule().getId();
    }

    /** Hourly coverage-ok samples of {@code count} for {@code weeks} weeks, ending just before {@code end}. */
    private void history(UUID rule, int weeks, int count, Instant end) {
        List<ActivitySampleEntity> batch = new ArrayList<>();
        for (Instant bucket = end.minus(Duration.ofDays(7L * weeks)); bucket.isBefore(end);
                bucket = bucket.plus(Duration.ofHours(1))) {
            ActivitySampleEntity sample = new ActivitySampleEntity(rule, "*", bucket, count);
            sample.setCoverageOk(true);
            sample.setCoverageRatio(1.0);
            batch.add(sample);
        }
        samples.saveAll(batch);
    }

    /** {@code n} distinct entities observed and ingested inside the current bucket. */
    private void entities(int n) {
        Instant at = CURRENT.plus(Duration.ofMinutes(30));
        clock.setInstant(at);
        for (int i = 0; i < n; i++) {
            records.ingestAt(SOURCE, at, NormalizedRecord.builder("x" + i, TestIngestSources.STANDARD_KIND)
                    .observedAt(at).put("name", "x" + i).build());
        }
        clock.setInstant(NOW);
    }

    private void covered() {
        coverage.fetch(SOURCE, "OK", CURRENT.minus(Duration.ofDays(1)));
        coverage.markCovered(SOURCE, CURRENT, CURRENT.plus(Duration.ofHours(1)));
    }

    private AnomalyObservationEntity newest(UUID rule) {
        return observations.findTop200ByRuleIdOrderByIdDesc(rule).get(0);
    }

    private int alerts(UUID rule) {
        Integer n = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("seven weeks of history are still learning: no alert, state learning")
    void learningUntilMinWeeks() {
        UUID id = createRule();
        history(id, 7, 10, CURRENT);
        covered();
        entities(60);

        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);

        assertThat(newest(id).getClassification()).isEqualTo("learning");
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("learning");
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("eight weeks complete warm-up; a spike is activity-up with detector, evidence and an honest alert")
    void spikeIsActivityUp() {
        UUID id = createRule();
        history(id, 8, 10, CURRENT);
        covered();
        entities(60);
        WindowEmitters.Ingesting emitter = new WindowEmitters.Ingesting(records, clock);

        engine.evaluate(NOW, emitter);

        AnomalyObservationEntity row = newest(id);
        assertThat(row.getBucketStart()).isEqualTo(CURRENT);
        assertThat(row.getObserved()).isEqualTo(60);
        assertThat(row.getClassification()).isEqualTo("activity-up");
        assertThat(row.getDetector()).containsEntry("name", "poisson-tail").containsEntry("version", "1.0.0")
                .containsKey("params");
        assertThat(row.getWatermarkSeq()).isPositive();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> members = (List<Map<String, Object>>) row.getEvidence().get("members");
        assertThat(members).isNotEmpty().hasSizeLessThanOrEqualTo(20)
                .allSatisfy(m -> assertThat(((Number) m.get("seq")).longValue()).isPositive());
        assertThat((List<?>) row.getEvidence().get("fetchLog")).isNotEmpty();
        assertThat(emitter.emittedAnomaly("activity-up")).isTrue();
        assertThat(emitter.lastState(id + ":*", "state")).isEqualTo("active");

        clock.advance(Duration.ofSeconds(10));
        engine.evaluate(clock.instant(), emitter);
        assertThat(alerts(id)).isEqualTo(1);
        String message = jdbc.queryForObject("select message from alert where rule_id = ?", String.class, id);
        assertThat(message).startsWith("Unusual activity: 60 observed vs ~10 expected (p≈")
                .endsWith("under a Poisson model)");
        String context = jdbc.queryForObject("select context::text from alert where rule_id = ?", String.class, id);
        assertThat(context).contains("\"anomaly\"").contains("\"pValue\"").contains("\"watermarkSeq\"");
    }

    @Test
    @DisplayName("halving the receiver count is a coverage-drop and never alerts")
    void halvedReceiversIsCoverageDrop() {
        UUID id = createRule();
        history(id, 8, 10, CURRENT);
        for (int i = 20; i >= 1; i--) {
            coverage.fetchWithReceivers(SOURCE, CURRENT.minus(Duration.ofMinutes(10L * i)), 10);
        }
        coverage.fetchWithReceivers(SOURCE, CURRENT.plus(Duration.ofMinutes(5)), 5);
        entities(5);

        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        clock.advance(Duration.ofSeconds(10));
        engine.evaluate(clock.instant(), capture);

        assertThat(newest(id).getClassification()).isEqualTo("coverage-drop");
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("unknown");
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("after downtime the cursor evaluates every missed bucket in order, then stops at the newest")
    void cursorCatchesUp() {
        UUID id = createRule();
        Instant lastSampled = CURRENT.minus(Duration.ofHours(5));
        history(id, 8, 10, lastSampled.plus(Duration.ofHours(1)));

        engine.evaluate(NOW, new WindowEmitters.Capture());

        for (int h = 4; h >= 0; h--) {
            Instant bucket = CURRENT.minus(Duration.ofHours(h));
            assertThat(samples.findById(new ActivitySampleEntity.Key(id, "*", bucket))).as("bucket " + bucket)
                    .isPresent();
        }
        assertThat(engine.cursor(id).bucketStart()).isEqualTo(CURRENT.getEpochSecond());
        assertThat(samples.findById(new ActivitySampleEntity.Key(id, "*", CURRENT.plus(Duration.ofHours(1)))))
                .as("the unsettled bucket is not evaluated").isEmpty();
    }
}
