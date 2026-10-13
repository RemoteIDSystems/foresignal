package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code ecowitt} preset (prompt 059): Ecowitt Customized upload (form POST, key in the path). The captured fixture under
 * {@code fixtures/mapping/ecowitt/} gives the golden records, and no secret is stored.
 */
class EcowittPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("ecowitt"));
    }

    @Test
    @DisplayName("the location parameter is rounded to 0.01 degrees and becomes a constant point; PASSKEY is dropped")
    void locationAndDrop() {
        PresetContract.Run run = PresetContract.run("ecowitt");
        assertThat(run.merged().path("mapping").path("geometry").path("const").get(0).asDouble()).isEqualTo(-97.74);
        assertThat(run.merged().path("allowPathKey").asBoolean()).isTrue();
        assertThat(run.merged().path("mapping").path("drop").toString()).contains("PASSKEY");
    }
}
