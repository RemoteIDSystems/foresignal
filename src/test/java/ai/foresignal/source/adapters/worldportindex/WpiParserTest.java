package ai.foresignal.source.adapters.worldportindex;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link WpiParser} contract (prompt 094). */
class WpiParserTest {

    private final SourceDescriptor descriptor = new WorldPortIndexAdapter("").descriptor();

    @Test
    @DisplayName("parses ports and buffered areas from the decimal coordinates, ignoring the DMS strings")
    void parses() {
        List<NormalizedRecord> records =
                WpiParser.parse(AdapterFixtures.bytes(WorldPortIndexAdapter.ID, "ports.json"));
        assertThat(records).hasSize(7);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord rotterdam = records.stream().filter(r -> r.externalId().equals("wpi:12345"))
                .findFirst().orElseThrow();
        assertThat(rotterdam.kind()).isEqualTo(WpiParser.PORT_KIND);
        assertThat(rotterdam.payload()).containsEntry("portName", "Rotterdam")
                .containsEntry("unloCode", "NLRTM")
                .containsEntry("harborSize", "L")
                .containsEntry("channelDepthM", 24.0)
                .containsEntry("maxVesselDraftM", 24.0);
        assertThat(rotterdam.geometry().getCoordinate().x).isCloseTo(4.4833,
                org.assertj.core.data.Offset.offset(1e-4));
        assertThat(rotterdam.geometry().getCoordinate().y).isCloseTo(51.9167,
                org.assertj.core.data.Offset.offset(1e-4));

        NormalizedRecord area = records.stream().filter(r -> r.externalId().equals("wpi-area:12345"))
                .findFirst().orElseThrow();
        assertThat(area.kind()).isEqualTo(WpiParser.AREA_KIND);
        assertThat(area.payload()).containsEntry("bufferKm", 10.0).containsEntry("portNumber", "12345");
        assertThat(area.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(area.geometry().getNumPoints()).isEqualTo(WpiParser.BUFFER_VERTICES + 1);

        NormalizedRecord noCoords = records.stream().filter(r -> r.externalId().equals("wpi:12348"))
                .findFirst().orElseThrow();
        assertThat(noCoords.geometry()).isNull();
        assertThat(records).extracting(NormalizedRecord::externalId).doesNotContain("wpi-area:12348");
    }

    @Test
    @DisplayName("buffer radius follows the harbor size code")
    void buffers() {
        assertThat(WpiParser.bufferKm("V")).isEqualTo(2.0);
        assertThat(WpiParser.bufferKm("S")).isEqualTo(3.0);
        assertThat(WpiParser.bufferKm("M")).isEqualTo(6.0);
        assertThat(WpiParser.bufferKm("L")).isEqualTo(10.0);
        assertThat(WpiParser.bufferKm("Z")).isEqualTo(3.0);
        assertThat(WpiParser.bufferKm(null)).isEqualTo(3.0);
    }
}
