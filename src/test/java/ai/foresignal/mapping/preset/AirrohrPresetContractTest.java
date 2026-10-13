package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code airrohr} preset (prompt 059): Sensor.Community airrohr (sensordatavalues pairs, Pa to hPa). The captured fixture under
 * {@code fixtures/mapping/airrohr/} gives the golden records, and no secret is stored.
 */
class AirrohrPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden records and drops every secret")
    void golden() {
        PresetContract.assertGolden(PresetContract.run("airrohr"));
    }

    @Test
    @DisplayName("PMS and SPS30 sensors feed the same field names; the first sensor with a value wins")
    void alternativeSensors() {
        ObjectNode sample = (ObjectNode) PresetContract.resource("fixtures/mapping/airrohr/sample.json").deepCopy();
        sample.put("body", "{\"esp8266id\":\"42\",\"software_version\":\"NRZ-2020-133\",\"sensordatavalues\":["
                + "{\"value_type\":\"SPS30_P0\",\"value\":\"1.5\"},{\"value_type\":\"SPS30_P2\",\"value\":\"3.5\"},"
                + "{\"value_type\":\"SPS30_P4\",\"value\":\"4.5\"},{\"value_type\":\"SPS30_P1\",\"value\":\"5.5\"}]}");
        var record = PresetContract.run("airrohr", null, sample, null).parsed().records().getFirst();
        assertThat(record.externalId()).isEqualTo("airrohr-42");
        assertThat(record.payload()).containsEntry("pm1", 1.5).containsEntry("pm25", 3.5).containsEntry("pm4", 4.5)
                .containsEntry("pm10", 5.5);
    }
}
