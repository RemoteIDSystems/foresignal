package ai.foresignal.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.celestrak.CelestrakGpParser;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link OmmFormats} against CelesTrak's own output of the same element set (prompt 064). */
class OmmFormatsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static List<OmmElements> stations() {
        return CelestrakGpParser.parseCsv(AdapterFixtures.bytes("celestrak-gp", "stations.csv"), "stations").elements();
    }

    @Test
    @DisplayName("TLE lines match CelesTrak's FORMAT=tle byte for byte: columns, checksums, name padding, CRLF")
    void tleMatchesCelestrak() {
        OmmElements iss = stations().get(0);
        String expected = AdapterFixtures.text("celestrak-gp", "iss-tle.txt");
        assertThat(OmmFormats.tle(List.of(iss), true)).isEqualTo(expected);
        String[] lines = OmmFormats.tleLines(iss);
        assertThat(lines[0]).hasSize(69).startsWith("1 25544U 98067A   26286.11204827");
        assertThat(lines[1]).hasSize(69);
        assertThat(OmmFormats.checksum(lines[0])).isEqualTo(lines[0].charAt(68) - '0');
        assertThat(OmmFormats.checksum(lines[1])).isEqualTo(lines[1].charAt(68) - '0');
        assertThat(OmmFormats.tle(List.of(iss), false)).doesNotContain("ISS (ZARYA)").startsWith("1 25544U");
    }

    @Test
    @DisplayName("every generated TLE re-parses with Orekit and carries the same elements")
    void orekitReparses() {
        for (OmmElements e : stations()) {
            if (!e.tleCompatible()) {
                continue;
            }
            String[] lines = OmmFormats.tleLines(e);
            org.orekit.propagation.analytical.tle.TLE tle = new org.orekit.propagation.analytical.tle.TLE(lines[0],
                    lines[1], ai.foresignal.orbit.OrekitData.context().getTimeScales().getUTC());
            assertThat(tle.getSatelliteNumber()).isEqualTo((int) e.norad());
            assertThat(Math.toDegrees(tle.getI())).isCloseTo(e.inclination(), org.assertj.core.data.Offset.offset(1e-4));
            assertThat(tle.getE()).isCloseTo(e.eccentricity(), org.assertj.core.data.Offset.offset(1e-7));
        }
    }

    @Test
    @DisplayName("objects of 100000 and above are left out of TLE and refused by tleLines")
    void noAlpha5() {
        List<OmmElements> all = stations();
        String tle = OmmFormats.tle(all, true);
        assertThat(tle).doesNotContain("SOYUZ-MS 29").contains("ISS (ZARYA)");
        assertThat(tle.split("\r\n")).hasSize(3 * (int) all.stream().filter(OmmElements::tleCompatible).count());
        OmmElements big = all.stream().filter(e -> e.norad() == 100057).findFirst().orElseThrow();
        assertThatThrownBy(() -> OmmFormats.tleLines(big)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("exponential and derivative fields use CelesTrak's layout, zero is 00000+0")
    void fields() {
        assertThat(OmmFormats.exponential(0.11499036E-3)).isEqualTo(" 11499-3");
        assertThat(OmmFormats.exponential(0)).isEqualTo(" 00000+0");
        assertThat(OmmFormats.exponential(-0.12128066E-4)).isEqualTo("-12128-4");
        assertThat(OmmFormats.exponential(0.17527107E-1)).isEqualTo(" 17527-1");
        assertThat(OmmFormats.firstDerivative(0.5858E-4)).isEqualTo(" .00005858");
        assertThat(OmmFormats.firstDerivative(-0.2969709E-1)).isEqualTo("-.02969709");
        assertThat(OmmFormats.intlDesignator("2011-037PF")).isEqualTo("11037PF");
        assertThat(OmmFormats.intlDesignator(null)).isEmpty();
        assertThat(OmmFormats.eccentricityDigits(0.00067245)).isEqualTo(6724);
        assertThat(OmmFormats.tleEpoch(Instant.parse("2026-01-05T12:00:00Z"))).isEqualTo("26005.50000000");
    }

    @Test
    @DisplayName("CSV has the 17 OMM columns and parses back to the same element sets")
    void csvRoundTrip() {
        List<OmmElements> all = stations();
        String csv = OmmFormats.csv(all);
        assertThat(csv).startsWith(String.join(",", OmmElements.KEYS) + "\r\n");
        assertThat(CelestrakGpParser.parseCsv(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8), "x").elements())
                .isEqualTo(all);
    }

    @Test
    @DisplayName("JSON and JSON-PRETTY are arrays of OMM objects that parse back")
    void json() {
        List<OmmElements> all = stations();
        String compact = OmmFormats.json(all, JSON, false);
        String pretty = OmmFormats.json(all, JSON, true);
        assertThat(pretty).contains("\n");
        JsonNode a = JSON.readTree(compact);
        assertThat(a.isArray()).isTrue();
        assertThat(a.get(0).get("NORAD_CAT_ID").asLong()).isEqualTo(25544);
        assertThat(JSON.readTree(pretty)).isEqualTo(a);
        assertThat(CelestrakGpParser.parseJson(compact.getBytes(java.nio.charset.StandardCharsets.UTF_8), "x")
                .elements()).isEqualTo(all);
    }

    @Test
    @DisplayName("OMM XML and KVN carry every element of every object, XML-escaped")
    void ccsds() {
        List<OmmElements> all = stations();
        Instant created = Instant.parse("2026-10-13T12:00:00Z");
        String xml = OmmFormats.xml(all, created);
        assertThat(xml).startsWith("<?xml").contains("<ndm").endsWith("</ndm>\n")
                .contains("<OBJECT_NAME>ISS (ZARYA)</OBJECT_NAME>").contains("<NORAD_CAT_ID>100057</NORAD_CAT_ID>")
                .contains("<REF_FRAME>TEME</REF_FRAME>").contains("<MEAN_ELEMENT_THEORY>SGP4</MEAN_ELEMENT_THEORY>");
        assertThat(xml.split("<omm ", -1)).hasSize(all.size() + 1);
        OmmElements amp = new OmmElements("A&B <C>", "2026-001A", created, 15, 0.001, 51, 1, 2, 3, 0, "U", 1, 999, 1,
                0, 0, 0);
        assertThat(OmmFormats.xml(List.of(amp), created)).contains("A&amp;B &lt;C&gt;");
        String kvn = OmmFormats.kvn(all, created);
        assertThat(kvn).contains("CCSDS_OMM_VERS      = 2.0").contains("OBJECT_NAME         = ISS (ZARYA)")
                .contains("NORAD_CAT_ID        = 25544");
        assertThat(kvn.split("CCSDS_OMM_VERS", -1)).hasSize(all.size() + 1);
    }
}
