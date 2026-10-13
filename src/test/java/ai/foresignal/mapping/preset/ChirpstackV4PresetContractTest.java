package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code chirpstack-v4} preset (prompt 059): ChirpStack v4 HTTP integration (JSON, event=up). The captured fixture under
 * {@code fixtures/mapping/chirpstack-v4/} gives the golden records, and no secret is stored.
 */
class ChirpstackV4PresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("chirpstack-v4"));
    }

    @Test
    @DisplayName("other events are acknowledged with no records, and Protobuf is 415")
    void otherEventsAndProtobuf() {
        JsonNode sample = PresetContract.resource("fixtures/mapping/chirpstack-v4/sample.json");
        ObjectNode join = ((ObjectNode) sample.deepCopy()).put("query", "event=join");
        assertThat(PresetContract.run("chirpstack-v4", null, join, null).parsed().records()).isEmpty();
        ObjectNode protobuf = ((ObjectNode) sample.deepCopy()).put("contentType", "application/octet-stream");
        assertThatThrownBy(() -> PresetContract.run("chirpstack-v4", null, protobuf, null))
                .isInstanceOf(ai.foresignal.api.error.ForesignalException.class)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    @DisplayName("a location event moves the device and flags the location source")
    void locationEvent() {
        ObjectNode sample = PresetContract.JSON.createObjectNode();
        sample.put("receivedAt", "2026-10-13T15:05:00Z");
        sample.put("method", "POST");
        sample.put("contentType", "application/json");
        sample.put("query", "event=location");
        sample.put("body", "{\"time\":\"2026-10-13T15:04:30Z\",\"deviceInfo\":{\"devEui\":\"a84041000181c0de\"},"
                + "\"location\":{\"latitude\":30.3,\"longitude\":-97.8,\"source\":\"GEO_RESOLVER_TDOA\",\"accuracy\":150}}");
        var record = PresetContract.run("chirpstack-v4", null, sample, null).parsed().records().getFirst();
        assertThat(record.externalId()).isEqualTo("a84041000181c0de");
        assertThat(record.geometry().getCoordinate().x).isEqualTo(-97.8);
        assertThat(record.quality().extra()).containsEntry("lora.locationSource", "geo_resolver_tdoa");
        assertThat(record.quality().accuracyMeters()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("the source is private by default")
    void privateByDefault() {
        assertThat(PresetContract.run("chirpstack-v4").descriptor().privateSource()).isTrue();
    }
}
