package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code alertmanager} preset (prompt 059): Alertmanager webhook v4 through the built-in format with the preset options. The captured fixture under
 * {@code fixtures/mapping/alertmanager/} gives the golden records, and no secret is stored.
 */
class AlertmanagerPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("alertmanager"));
    }

    @Test
    @DisplayName("tombstoneOnResolve turns a resolved alert into a tombstone; a station parameter is the geometry")
    void tombstoneAndStation() {
        ObjectNode params = PresetContract.JSON.createObjectNode().put("tombstoneOnResolve", true).put("lon", -97.7431)
                .put("lat", 30.2672);
        JsonNode sample = PresetContract.resource("fixtures/mapping/alertmanager/sample.json");
        var records = PresetContract.run("alertmanager", params, sample, null).parsed().records();
        assertThat(records.get(1).tombstone()).isTrue();
        assertThat(records.get(0).geometry().getCoordinate().x).isEqualTo(-97.74);
    }
}
