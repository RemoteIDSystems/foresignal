package ai.foresignal.source.adapters.nhc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/** {@link NhcCurrentStormsParser} against the captured and hand-made {@code CurrentStorms.json} fixtures (prompt 067). */
class NhcCurrentStormsParserTest {

    private static List<NormalizedRecord> parse(String fixture) {
        return NhcCurrentStormsParser.parse(AdapterFixtures.bytes(NhcCyclonesAdapter.ID, fixture));
    }

    private static List<NormalizedRecord> parse(byte[] json) {
        return NhcCurrentStormsParser.parse(json);
    }

    @Test
    @DisplayName("the captured file: Hurricane Simon with string numerics, mph converted to knots, links kept")
    void captured() {
        List<NormalizedRecord> records = parse("CurrentStorms.json");

        assertThat(records).hasSize(1);
        NormalizedRecord simon = records.get(0);
        assertThat(simon.externalId()).isEqualTo("ep202026");
        assertThat(simon.kind()).isEqualTo("cyclone");
        assertThat(simon.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(simon.eventTime()).isNull();
        assertThat(simon.validUntil()).isNull();
        Point point = (Point) simon.geometry();
        assertThat(point.getX()).isEqualTo(-107.2);
        assertThat(point.getY()).isEqualTo(20.4);
        assertThat(simon.payload())
                .containsEntry("name", "Simon")
                .containsEntry("classification", "HU")
                .containsEntry("classificationLabel", "Hurricane")
                .containsEntry("basin", "EP")
                .containsEntry("binNumber", "EP5")
                .containsEntry("intensityKt", 80)
                .containsEntry("pressureMb", 977)
                .containsEntry("movementDirDeg", 285)
                .containsEntry("movementSpeedKt", 2.6)
                .containsEntry("advisoryNumber", "24")
                .containsEntry("advisoryIssued", "2026-10-13T09:00:00Z")
                .containsEntry("publicAdvisoryUrl", "https://www.nhc.noaa.gov/text/MIATCPEP5.shtml")
                .containsEntry("forecastAdvisoryUrl", "https://www.nhc.noaa.gov/text/MIATCMEP5.shtml");
        FieldSpecContract.assertPayloadKeysDeclared(new NhcCyclonesAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("numerics as strings or numbers; unreadable ones are left out; names sanitized; cp ids accepted")
    void stringNumbers() {
        List<NormalizedRecord> records = parse("string-numbers.json");

        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("al092026", "cp012026", "ep142026");
        NormalizedRecord ida = records.get(0);
        assertThat(ida.payload())
                .containsEntry("name", "Idascript")
                .containsEntry("intensityKt", 55)
                .containsEntry("pressureMb", 1001)
                .containsEntry("movementDirDeg", 315)
                .containsEntry("movementSpeedKt", 8.7)
                .containsEntry("advisoryNumber", "12")
                .containsEntry("classificationLabel", "Tropical Storm")
                .containsEntry("publicAdvisoryUrl", "https://www.nhc.noaa.gov/text/MIATCPAT4.shtml")
                .doesNotContainKey("forecastAdvisoryUrl");
        NormalizedRecord akoni = records.get(1);
        assertThat(akoni.payload()).containsEntry("basin", "CP").containsEntry("intensityKt", 95)
                .containsEntry("movementSpeedKt", 10.4).containsEntry("forecastAdvisoryUrl",
                        "https://www.nhc.noaa.gov/text/MIATCMCP1.shtml")
                .as("http: is not an official link").doesNotContainKey("publicAdvisoryUrl");
        NormalizedRecord lowell = records.get(2);
        assertThat(lowell.payload())
                .doesNotContainKeys("intensityKt", "pressureMb", "movementDirDeg", "movementSpeedKt", "classificationLabel",
                        "advisoryNumber", "advisoryIssued", "forecastAdvisoryUrl", "publicAdvisoryUrl")
                .containsEntry("classification", "XX");
        assertThat(lowell.geometry()).as("numeric strings for coordinates are read").isInstanceOf(Point.class);
        FieldSpecContract.assertPayloadKeysDeclared(new NhcCyclonesAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("every product object null: the storm is kept without advisory fields")
    void nullProducts() {
        List<NormalizedRecord> records = parse("null-products.json");

        assertThat(records).singleElement().satisfies(r -> {
            assertThat(r.payload()).containsEntry("classificationLabel", "Post-tropical Cyclone")
                    .containsEntry("movementSpeedKt", 17.4)
                    .doesNotContainKeys("advisoryNumber", "advisoryIssued", "publicAdvisoryUrl", "forecastAdvisoryUrl");
        });
    }

    @Test
    @DisplayName("an empty list is zero records")
    void empty() {
        assertThat(parse("empty.json")).isEmpty();
    }

    @Test
    @DisplayName("an invalid or duplicate id, a missing lastUpdate, over 32 storms or no array rejects the document")
    void rejected() {
        assertThatThrownBy(() -> parse("invalid-id.json")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("wp012026");
        String duplicate = "{\"activeStorms\":[{\"id\":\"al012026\",\"lastUpdate\":\"2026-09-02T09:00:00.000Z\"},"
                + "{\"id\":\"al012026\",\"lastUpdate\":\"2026-09-02T09:00:00.000Z\"}]}";
        assertThatThrownBy(() -> parse(bytes(duplicate))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> parse(bytes("{\"activeStorms\":[{\"id\":\"al012026\"}]}")))
                .isInstanceOf(UpstreamFormatException.class);
        StringBuilder many = new StringBuilder("{\"activeStorms\":[");
        for (int i = 0; i < 33; i++) {
            many.append(i == 0 ? "" : ",").append("{\"id\":\"al").append(String.format("%02d", i)).append("2026\","
                    + "\"lastUpdate\":\"2026-09-02T09:00:00.000Z\"}");
        }
        assertThatThrownBy(() -> parse(bytes(many.append("]}").toString()))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> parse(bytes("{}"))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> parse(bytes("<html>"))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("advisory numbers lose leading zeros and are upper-cased; links must be https://www.nhc.noaa.gov/")
    void helpers() {
        assertThat(NhcCurrentStormsParser.advisory("024a")).isEqualTo("24A");
        assertThat(NhcCurrentStormsParser.advisory("000")).isEqualTo("0");
        assertThat(NhcCurrentStormsParser.advisory("1234")).isNull();
        assertThat(NhcCurrentStormsParser.advisory(null)).isNull();
        var json = tools.jackson.databind.json.JsonMapper.builder().build();
        assertThat(NhcCurrentStormsParser.officialLink(json.readTree("\"https://www.nhc.noaa.gov/text/A.shtml\"")))
                .isEqualTo("https://www.nhc.noaa.gov/text/A.shtml");
        assertThat(NhcCurrentStormsParser.officialLink(json.readTree("\"https://user@www.nhc.noaa.gov/x\""))).isNull();
        assertThat(NhcCurrentStormsParser.officialLink(json.readTree("\"https://www.nhc.noaa.gov.evil.com/x\""))).isNull();
        assertThat(NhcCurrentStormsParser.officialLink(json.readTree("\"https://www.hurricanes.gov/x\""))).isNull();
        assertThat(NhcCurrentStormsParser.officialLink(json.readTree("\"https://www.nhc.noaa.gov/x#f\""))).isNull();
    }

    private static byte[] bytes(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
