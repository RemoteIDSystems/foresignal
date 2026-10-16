package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The leaflet-velocity / wind-js field encoding (prompt 106). */
class VelocityJsonEncoderTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("encodes two components with the header and row-major data")
    void encodes() throws Exception {
        // A 3x2 grid: u row-major north->south, v likewise.
        float[] u = {1, 2, 3, 4, 5, 6};
        float[] v = {-1, -2, -3, -4, -5, -6};
        byte[] raw = FieldStore.planar(u, v);
        byte[] json = VelocityJsonEncoder.encode(raw, 3, 2, 0, 90, 1, 1, Instant.parse("2026-10-15T00:00:00Z"), 6);
        JsonNode node = JSON.readTree(json);
        assertThat(node.isArray()).isTrue();
        assertThat(node.size()).isEqualTo(2);

        JsonNode uComp = node.get(0);
        JsonNode uHeader = uComp.get("header");
        assertThat(uHeader.get("discipline").asInt()).isZero();
        assertThat(uHeader.get("parameterCategory").asInt()).isEqualTo(2);
        assertThat(uHeader.get("parameterNumber").asInt()).isEqualTo(2);
        assertThat(uHeader.get("nx").asInt()).isEqualTo(3);
        assertThat(uHeader.get("ny").asInt()).isEqualTo(2);
        assertThat(uHeader.get("lo1").asDouble()).isZero();
        assertThat(uHeader.get("la1").asDouble()).isEqualTo(90);
        assertThat(uHeader.get("lo2").asDouble()).isEqualTo(2);
        assertThat(uHeader.get("la2").asDouble()).isEqualTo(89);
        assertThat(uHeader.get("dx").asDouble()).isEqualTo(1);
        assertThat(uHeader.get("dy").asDouble()).isEqualTo(1);
        assertThat(uHeader.get("refTime").asString()).isEqualTo("2026-10-15T00:00:00Z");
        assertThat(uHeader.get("forecastTime").asDouble()).isEqualTo(6);
        List<Double> uData = data(uComp);
        assertThat(uData).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

        JsonNode vComp = node.get(1);
        assertThat(vComp.get("header").get("parameterNumber").asInt()).isEqualTo(3);
        assertThat(data(vComp)).containsExactly(-1.0, -2.0, -3.0, -4.0, -5.0, -6.0);
    }

    @Test
    @DisplayName("a mismatched buffer is refused")
    void refusesWrongSize() {
        byte[] raw = new byte[8];
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> VelocityJsonEncoder.encode(raw, 3, 2, 0, 90, 1, 1, null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static List<Double> data(JsonNode component) {
        java.util.List<Double> out = new java.util.ArrayList<>();
        for (JsonNode n : component.get("data")) {
            out.add(n.asDouble());
        }
        return out;
    }
}
