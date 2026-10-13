package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code tts-v3} preset (prompt 059): The Things Stack v3 webhook (static user location). The captured fixture under
 * {@code fixtures/mapping/tts-v3/} gives the golden records, and no secret is stored.
 */
class TtsV3PresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("tts-v3"));
    }

    @Test
    @DisplayName("a frm-payload location wins over the configured user location")
    void frmPayloadWins() {
        ObjectNode sample = (ObjectNode) PresetContract.resource("fixtures/mapping/tts-v3/sample.json").deepCopy();
        ObjectNode body = (ObjectNode) PresetContract.JSON.readTree(sample.get("body").asString());
        ((ObjectNode) body.get("uplink_message").get("locations")).putObject("frm-payload").put("latitude", 30.5)
                .put("longitude", -97.5);
        sample.put("body", PresetContract.JSON.writeValueAsString(body));
        var record = PresetContract.run("tts-v3", null, sample, null).parsed().records().getFirst();
        assertThat(record.geometry().getCoordinate().y).isEqualTo(30.5);
        assertThat(record.quality().extra()).doesNotContainKey("lora.locationSource");
    }

    @Test
    @DisplayName("a message without uplink_message is skipped by the filter")
    void nonUplinkSkipped() {
        ObjectNode sample = (ObjectNode) PresetContract.resource("fixtures/mapping/tts-v3/sample.json").deepCopy();
        sample.put("body", "{\"end_device_ids\":{\"dev_eui\":\"70B3D57ED0051234\"},\"received_at\":\"2026-10-13T15:04:05Z\","
                + "\"join_accept\":{}}");
        assertThat(PresetContract.run("tts-v3", null, sample, null).parsed().records()).isEmpty();
    }
}
