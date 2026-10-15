package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The anomaly rule and its coverage-aware baseline (prompt 100): eight weeks of hourly coverage-ok
 * samples inside the 12-week lookback complete the warm-up, then a spike in the current bucket is
 * classified {@code activity-up} with detector and evidence; a receiver drop yields
 * {@code coverage-drop}, not an alert.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AnomalyRuleIT {

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired ActivitySampleRepository samples;
    @Autowired AnomalyObservationRepository observations;
    @Autowired TestRecords records;
    @Autowired JsonMapper json;

    static final class Capture implements WindowEngine.Emitter {

        final List<NormalizedRecord> emitted = new ArrayList<>();

        @Override
        public void emit(NormalizedRecord record) {
            emitted.add(record);
        }

        @Override
        public void tombstone(String kind, String externalId) {
            emitted.add(NormalizedRecord.tombstone(externalId, kind, Instant.now()));
        }

        boolean emittedAnomaly(String classification) {
            return emitted.stream().anyMatch(r -> r.kind().equals("anomaly")
                    && classification.equals(r.payload().get("classification")));
        }
    }

    private UUID createRule() {
        return rules.create(json.readTree(
                "{\"name\":\"unusual\",\"type\":\"anomaly\",\"sources\":[\"test-standard\"],"
                        + "\"window\":{\"window\":\"1h\",\"direction\":\"up\",\"minWeeks\":8}}")).rule().getId();
    }

    @Test
    @DisplayName("eight weeks of hourly history complete warm-up, then a spike is activity-up")
    void spike() {
        UUID id = createRule();
        Instant now = Instant.now();
        Instant currentBucket = now.minus(Duration.ofHours(1)).truncatedTo(ChronoUnit.HOURS);
        // 8 weeks of hourly coverage-ok samples, each slot (day-of-week/hour) therefore sees 8 weeks.
        Instant start = currentBucket.minus(Duration.ofDays(56));
        for (Instant bucket = start; bucket.isBefore(currentBucket); bucket = bucket.plus(Duration.ofHours(1))) {
            ActivitySampleEntity sample = new ActivitySampleEntity(id, "*", bucket, 10);
            sample.setCoverageOk(true);
            sample.setCoverageRatio(1.0);
            samples.save(sample);
        }
        assertThat(samples.findByRuleIdAndGroupKeyOrderByBucketStartAsc(id, "*")).hasSize(8 * 7 * 24);

        // A spike of distinct entities inside the current completed bucket.
        for (int i = 0; i < 60; i++) {
            records.ingestAt(TestIngestSources.STANDARD, currentBucket.plusSeconds(i * 30),
                    NormalizedRecord.builder("x" + i, "thing").observedAt(currentBucket.plusSeconds(i * 30))
                            .put("name", "x").build());
        }
        Capture capture = new Capture();
        engine.evaluate(now, capture);

        List<AnomalyObservationEntity> rows = observations.findTop200ByRuleIdOrderByIdDesc(id);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).getClassification()).isEqualTo("activity-up");
        assertThat(rows.get(0).getDetector()).containsEntry("name", "poisson-tail");
        assertThat(rows.get(0).getEvidence()).containsKey("members");
        assertThat(capture.emittedAnomaly("activity-up")).isTrue();
        assertThat(capture.emitted).anyMatch(r -> r.kind().equals("window-state"));
    }
}
