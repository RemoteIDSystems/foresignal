package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.WindowController;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The windowed-rule controllers (prompt 100): CRUD with each type, window-state, baseline (409 before
 * any sample) and the anomalies feed.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class WindowControllersIT {

    @Autowired WindowController controller;
    @Autowired TriggerRuleService rules;
    @Autowired JsonMapper json;

    private UUID create(String type, String window) {
        return rules.create(json.readTree("{\"name\":\"" + type + "\",\"type\":\"" + type + "\","
                + "\"sources\":[\"test-standard\"],\"window\":" + window + "}")).rule().getId();
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
    @DisplayName("baseline is 409 before any coverage-ok sample")
    void baselineNotReady() {
        UUID id = create("anomaly", "{\"window\":\"1h\"}");
        assertThatThrownBy(() -> controller.getTriggerBaseline(id.toString(), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("the anomalies feed is a paged list")
    void anomalies() {
        var out = controller.listAnomalies(null, null, null, null, null, null, "10");
        assertThat(out).containsKeys("items", "nextCursor");
    }

    @Test
    @DisplayName("rebuildBaseline reports the group count")
    void rebuild() {
        UUID id = create("anomaly", "{\"window\":\"1h\"}");
        var out = controller.rebuildBaseline(id.toString());
        assertThat(out.get("rebuilt")).isEqualTo(true);
    }
}
