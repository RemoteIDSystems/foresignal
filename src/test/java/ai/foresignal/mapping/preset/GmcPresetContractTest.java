package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code gmc} preset (prompt 059): GQ GMC counter upload (GET; unverified). The captured fixture under
 * {@code fixtures/mapping/gmc/} gives the golden records, and no secret is stored.
 */
class GmcPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("gmc"));
    }

    @Test
    @DisplayName("the preset accepts GET and the key in the path, and answers OK.")
    void getAndReply() {
        MappingPreset preset = PresetCatalog.find("gmc").orElseThrow();
        assertThat(preset.allowsGet()).isTrue();
        assertThat(preset.document().path("allowPathKey").asBoolean()).isTrue();
        var format = PresetContract.format(preset, null, null);
        assertThat(format.nativeReply(null).body()).isEqualTo("OK.");
    }
}
