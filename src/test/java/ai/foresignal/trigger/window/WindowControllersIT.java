package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.WindowController;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

/**
 * The windowed-rule controllers (prompt 100): CRUD with each type, window-state, the per-slot baseline
 * (409 before any sample), the rebuild that actually clears and re-backfills, and the anomalies feed.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class WindowControllersIT {

    /** A Monday, 10:00 UTC: hour-of-week slot 10. */
    private static final Instant MONDAY_10 = Instant.parse("2026-10-12T10:00:00Z");

    @Autowired WindowController controller;
    @Autowired ai.foresignal.api.TriggersController triggers;
    @Autowired ai.foresignal.testsupport.ingest.TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired ActivitySampleRepository samples;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    private UUID create(String type, String window) {
        // Disabled, so the background window scheduler never evaluates (and fills samples for) the rule.
        return rules.create(json.readTree("{\"name\":\"" + type + "\",\"enabled\":false,\"type\":\"" + type + "\","
                + "\"sources\":[\"test-standard\"],\"window\":" + window + "}")).rule().getId();
    }

    private void sample(UUID rule, Instant bucketStart, int count) {
        ActivitySampleEntity sample = new ActivitySampleEntity(rule, "*", bucketStart, count);
        sample.setCoverageOk(true);
        sample.setCoverageRatio(1.0);
        samples.save(sample);
    }

    @Test
    @DisplayName("window-state returns the rule type and its groups")
    void windowState() {
        UUID id = create("count", "{\"window\":\"30m\",\"op\":\">=\",\"threshold\":2}");
        var out = controller.getTriggerWindowState(id.toString());
        assertThat(out.get("type")).isEqualTo("count");
        assertThat(out.get("ruleId")).isEqualTo(id);
    }

    @Test
    @DisplayName("baseline is 409 before any sample")
    void baselineNotReady() {
        UUID id = create("anomaly", "{\"window\":\"1h\"}");
        assertThatThrownBy(() -> controller.getTriggerBaseline(id.toString(), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("baseline is per slot of the rule's own slot mode, with the weeks available and learning")
    @SuppressWarnings("unchecked")
    void baselinePerSlot() {
        clock.setInstant(MONDAY_10.plus(Duration.ofDays(1)));
        UUID id = create("anomaly", "{\"window\":\"1h\"}");
        sample(id, MONDAY_10.minus(Duration.ofDays(14)), 4);
        sample(id, MONDAY_10.minus(Duration.ofDays(7)), 6);
        sample(id, MONDAY_10, 8);
        sample(id, MONDAY_10.plus(Duration.ofHours(1)), 1);

        Map<String, Object> out = controller.getTriggerBaseline(id.toString(), null);

        assertThat(out).containsEntry("slotMode", "hour-of-week").containsEntry("bucket", "1h")
                .containsEntry("learning", true).containsEntry("samplePath", "activity_sample")
                .containsEntry("minWeeks", 8);
        List<Map<String, Object>> slots = (List<Map<String, Object>>) out.get("slots");
        assertThat(slots).extracting(s -> s.get("slot")).containsExactly(10, 11);
        Map<String, Object> ten = slots.get(0);
        assertThat(ten).containsEntry("groupKey", "*").containsEntry("n", 3).containsEntry("mean", 6.0)
                .containsEntry("model", "poisson");
        assertThat((Double) out.get("weeksAvailable")).isEqualTo(3.0);
        assertThat(out.get("learningUntil")).isNotNull();
    }

    @Test
    @DisplayName("the anomalies feed is a paged list")
    void anomalies() {
        var out = controller.listAnomalies(null, null, null, null, null, null, "10");
        assertThat(out).containsKeys("items", "nextCursor");
    }

    @Test
    @DisplayName("rebuildBaseline clears the samples, resets the cursor for a backfill and answers 202")
    void rebuild() {
        clock.setInstant(MONDAY_10.plus(Duration.ofDays(1)));
        UUID id = create("anomaly", "{\"window\":\"1h\"}");
        sample(id, MONDAY_10, 8);
        assertThat(engine.hasSamples(id)).isTrue();

        var response = controller.rebuildBaseline(id.toString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).containsEntry("accepted", true).containsKeys("weeksAvailable", "backfillFrom");
        assertThat(engine.hasSamples(id)).isFalse();
        assertThat(engine.cursor(id).bucketStart()).isNegative();
        assertThatThrownBy(() -> controller.getTriggerBaseline(id.toString(), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("rebuildBaseline of a count rule is WINDOW_RULE_INVALID")
    void rebuildCountRuleRejected() {
        UUID id = create("count", "{\"window\":\"30m\",\"op\":\">=\",\"threshold\":2}");
        assertThatThrownBy(() -> controller.rebuildBaseline(id.toString()))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.WINDOW_RULE_INVALID));
    }

    @Test
    @DisplayName("testTrigger of a count rule evaluates its steps from versions only and says it is approximate")
    @SuppressWarnings("unchecked")
    void testTriggerWindowed() {
        Instant at = MONDAY_10.minus(Duration.ofMinutes(10));
        clock.setInstant(at);
        records.ingestAt(TestIngestSources.STANDARD, at, ai.foresignal.source.spi.NormalizedRecord
                .builder("a", TestIngestSources.STANDARD_KIND).observedAt(at).put("name", "a").build());
        clock.setInstant(MONDAY_10.plus(Duration.ofHours(1)));
        UUID id = create("count", "{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1}");

        Map<String, Object> out = triggers.testTrigger(id.toString(), MONDAY_10.minus(Duration.ofHours(1)).toString(),
                MONDAY_10.toString(), null, null, null, null);

        assertThat(out).containsEntry("approximate", true).containsEntry("matchCount", 0);
        assertThat((String) out.get("approximateReason")).startsWith("versions only");
        List<Map<String, Object>> evaluations = (List<Map<String, Object>>) out.get("windowEvaluations");
        assertThat(evaluations).isNotEmpty();
        assertThat(evaluations).anySatisfy(e -> assertThat(e).containsEntry("state", "active"));
        assertThat(evaluations).anySatisfy(e -> assertThat(e).containsEntry("state", "clear"));
    }
}
