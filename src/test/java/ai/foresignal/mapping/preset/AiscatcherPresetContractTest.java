package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code aiscatcher} preset (prompt 059): AIS-catcher jsonaiscatcher (merged split messages, AIS sentinels). The captured fixture under
 * {@code fixtures/mapping/aiscatcher/} gives the golden records, and no secret is stored.
 */
class AiscatcherPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("aiscatcher"));
    }

    @Test
    @DisplayName("the static message is merged over the position: name and position on one snapshot")
    void merged() {
        PresetContract.Run run = PresetContract.run("aiscatcher");
        var second = run.parsed().records().get(1);
        assertThat(second.payload()).containsEntry("name", "GULF TRADER").containsKey("sog");
        assertThat(second.geometry()).isNotNull();
        assertThat(run.parsed().records().get(2).payload()).doesNotContainKeys("sog", "cog", "heading");
    }
}
