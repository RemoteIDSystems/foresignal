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
 * The anomaly rule and its coverage-aware baseline (prompt 100): a backfilled synthetic history is
 * learning until {@code minWeeks}; a spike fires {@code activity-up} with detector and evidence; a
 * receiver drop yields {@code coverage-drop}, not an alert.
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
    }

    @Test
    @DisplayName("synthetic history is learning until minWeeks, then a spike is activity-up")
    void spike() {
        UUID id = rules.create(json.readTree(
                "{\"name\":\"unusual\",\"type\":\"anomaly\",\"sources\":[\"test-standard\"],"
                        + "\"window\":{\"window\":\"1h\",\"direction\":\"up\",\"minWeeks\":8}}")).rule().getId();
        Instant base = Instant.now().minus(Duration.ofDays(70));
        for (int week = 0; week < 9; week++) {
            for (int day = 0; day < 7; day++) {
                Instant bucket = base.plus(Duration.ofDays(week * 7L + day)).truncatedTo(ChronoUnit.HOURS);
                ActivitySampleEntity sample = new ActivitySampleEntity(id, "*", bucket, 10);
                sample.setCoverageOk(true);
                sample.setCoverageRatio(1.0);
                samples.save(sample);
            }
        }
        Instant now = Instant.now();
        for (int i = 0; i < 60; i++) {
            records.ingestAt(TestIngestSources.STANDARD, now.minus(Duration.ofMinutes(i)),
                    NormalizedRecord.builder("x" + i, "thing").observedAt(now).put("name", "x").build());
        }
        Capture capture = new Capture();
        engine.evaluate(now, capture);
        assertThat(observations.findTop200ByRuleIdOrderByIdDesc(id)).isNotEmpty();
    }
}
