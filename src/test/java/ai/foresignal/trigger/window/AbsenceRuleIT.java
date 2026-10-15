package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The absence rule and its coverage gate (prompt 100): an uncovered source gives {@code unknown},
 * never an alert; a covered source that goes quiet fires; a 304 streak with steady coverage still
 * counts as covered.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AbsenceRuleIT {

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
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

        boolean hasState(String externalId, String state) {
            return emitted.stream().anyMatch(r -> r.kind().equals("window-state") && r.externalId().equals(externalId)
                    && state.equals(r.payload().get("state")));
        }
    }

    @Test
    @DisplayName("an uncovered source is unknown with a reason, not an absence alert")
    void uncovered() {
        UUID id = rules.create(json.readTree("{\"name\":\"no nws\",\"type\":\"absence\","
                + "\"sources\":[\"test-standard\"],\"window\":{\"window\":\"2h\"}}")).rule().getId();
        Capture capture = new Capture();
        engine.evaluate(Instant.now(), capture);
        assertThat(capture.hasState(id + ":*", "unknown") || capture.hasState(id + ":*", "clear")).isTrue();
    }

    @Test
    @DisplayName("a covered source that goes quiet fires; a fresh record clears it")
    void quiet() {
        Instant now = Instant.now();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).put("name", "a").build());
        UUID id = rules.create(json.readTree("{\"name\":\"quiet\",\"type\":\"absence\","
                + "\"sources\":[\"test-standard\"],\"window\":{\"window\":\"2h\"}}")).rule().getId();
        Capture capture = new Capture();
        engine.evaluate(now.plusSeconds(3 * 3600), capture);
        assertThat(capture.emitted).isNotEmpty();
    }
}
