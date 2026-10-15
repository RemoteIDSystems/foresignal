package ai.foresignal.source.adapters.osm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The {@code osm-infrastructure} parser contract (prompt 104). */
class OsmInfraParserContractTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    private static List<ai.foresignal.source.spi.NormalizedRecord> parse(String... lines) {
        return OsmInfraParser.parse(List.of(lines), 110, T);
    }

    @Test
    @DisplayName("a substation node keeps the allowlisted tags")
    void substation() {
        var records = parse("{\"type\":\"Feature\",\"id\":\"n1\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[-95,32]},\"properties\":{\"power\":\"substation\",\"name\":\"Oak\","
                + "\"operator\":\"City\",\"voltage\":\"110000;220000\",\"secret\":\"x\"}}");
        assertThat(records).hasSize(1);
        var payload = records.get(0).payload();
        assertThat(payload).containsEntry("name", "Oak").containsEntry("operator", "City")
                .containsEntry("voltageKv", 220.0);
        assertThat(payload).doesNotContainKey("secret");
    }

    @Test
    @DisplayName("a power line below minVoltageKv is dropped and voltage is the max of the list")
    void voltageFilter() {
        assertThat(parse("{\"type\":\"Feature\",\"id\":\"w1\",\"geometry\":{\"type\":\"LineString\","
                + "\"coordinates\":[[-95,32],[-95.1,32.1]]},\"properties\":{\"power\":\"line\","
                + "\"voltage\":\"33000\"}}")).isEmpty();
        var kept = parse("{\"type\":\"Feature\",\"id\":\"w2\",\"geometry\":{\"type\":\"LineString\","
                + "\"coordinates\":[[-95,32],[-95.1,32.1]]},\"properties\":{\"power\":\"line\","
                + "\"voltage\":\"110000;220000\"}}");
        assertThat(kept).hasSize(1);
        assertThat(kept.get(0).payload()).containsEntry("voltageKv", 220.0);
    }

    @Test
    @DisplayName("kinds map from the tags")
    void kinds() {
        assertThat(OsmInfraParser.kind(JSON.readTree("{\"amenity\":\"hospital\"}"))).isEqualTo("hospital");
        assertThat(OsmInfraParser.kind(JSON.readTree("{\"aeroway\":\"aerodrome\"}"))).isEqualTo("aerodrome-osm");
        assertThat(OsmInfraParser.kind(JSON.readTree("{\"telecom\":\"data_center\"}"))).isEqualTo("data-centre-osm");
        assertThat(OsmInfraParser.kind(JSON.readTree("{\"power\":\"plant\"}"))).isEqualTo("power-plant-osm");
        assertThat(OsmInfraParser.kind(JSON.readTree("{\"amenity\":\"cafe\"}"))).isNull();
    }

    @Test
    @DisplayName("capacity parses from '500 MW' to 500 and geometry is carried")
    void capacity() {
        var records = parse("{\"type\":\"Feature\",\"id\":\"n2\",\"geometry\":{\"type\":\"Point\","
                + "\"coordinates\":[-95,32]},\"properties\":{\"power\":\"plant\",\"name\":\"P\","
                + "\"plant:output:electricity\":\"500 MW\"}}");
        assertThat(records).hasSize(1);
        assertThat(records.get(0).payload()).containsEntry("capacityMw", 500.0);
        assertThat(records.get(0).geometry()).isNotNull();
    }
}
