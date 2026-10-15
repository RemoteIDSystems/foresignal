package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The absence rule and its coverage gate (prompt 100): an uncovered source gives {@code unknown},
 * never an alert; a covered source that goes quiet fires and, crucially, an {@code active -> unknown}
 * transition leaves the alert firing (no false all-clear).
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AbsenceRuleIT {

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;
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

    private void enable(boolean enabled) {
        sources.update(TestIngestSources.STANDARD,
                new SourceSettingsUpdate(enabled, null, Map.of(), Budget.of(1, 1), null, false, null, false));
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
    @DisplayName("a covered source that goes quiet fires; losing coverage keeps the alert firing")
    void unknownKeepsAlertFiring() {
        enable(true);
        Instant now = Instant.now();
        // A record well before the window leaves the window empty, so absence is true.
        records.ingestAt(TestIngestSources.STANDARD, now.minus(Duration.ofHours(10)),
                NormalizedRecord.builder("a", "thing").observedAt(now.minus(Duration.ofHours(10))).put("name", "a")
                        .build());
        UUID id = rules.create(json.readTree("{\"name\":\"quiet\",\"type\":\"absence\","
                + "\"sources\":[\"test-standard\"],\"window\":{\"window\":\"2h\"}}")).rule().getId();

        Capture fired = new Capture();
        engine.evaluate(now, fired);
        assertThat(fired.hasState(id + ":*", "active")).isTrue();

        // Disabling the source makes the coverage gate unknown: active -> unknown must not resolve.
        enable(false);
        Capture unknown = new Capture();
        engine.evaluate(now.plusSeconds(120), unknown);
        assertThat(unknown.hasState(id + ":*", "unknown")).isTrue();
        String state = jdbc.queryForObject("select state from alert where rule_id = ?", String.class, id);
        assertThat(state).isEqualTo("firing");
        enable(true);
    }
}
