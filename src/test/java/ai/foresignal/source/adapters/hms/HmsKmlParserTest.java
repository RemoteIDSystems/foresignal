package ai.foresignal.source.adapters.hms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;

/**
 * {@link HmsKmlParser} (prompt 048): the captured file, density from the style URL, ExtendedData,
 * the time base and ids, and a hardened parser that refuses DTDs and external entities.
 */
class HmsKmlParserTest {

    static final String FILE = "hms_smoke20261012.kml";
    static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    static byte[] kml(String placemarks) {
        return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>"
                + placemarks + "</Document></kml>").getBytes(StandardCharsets.UTF_8);
    }

    static String placemark(String style, String description, String extended) {
        return "<Placemark>" + (description == null ? "" : "<description><![CDATA[" + description + "]]></description>")
                + (style == null ? "" : "<styleUrl>" + style + "</styleUrl>") + (extended == null ? "" : extended)
                + "<Polygon><outerBoundaryIs><LinearRing><coordinates>-97.0,30.0,0 -96.0,30.0,0 -96.0,31.0,0 -97.0,31.0,0"
                + "</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>";
    }

    @Test
    @DisplayName("the captured file: 67 polygons, 62 light and 5 medium, none skipped")
    void captured() {
        HmsKmlParser.Result r = HmsKmlParser.parse(AdapterFixtures.bytes("hms", FILE), DAY);

        assertThat(r.placemarks()).isEqualTo(67);
        assertThat(r.records()).hasSize(67);
        assertThat(r.skipped()).isZero();
        Map<Object, Long> density = r.records().stream()
                .collect(Collectors.groupingBy(x -> x.payload().get("density"), Collectors.counting()));
        assertThat(density).containsEntry("light", 62L).containsEntry("medium", 5L).hasSize(2);
        NormalizedRecord first = r.records().get(0);
        assertThat(first.kind()).isEqualTo("smoke-polygon");
        assertThat(first.externalId()).matches("[0-9a-f]{16}");
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-12T15:00:00Z"));
        assertThat(first.geometry()).isInstanceOf(Polygon.class);
        assertThat(first.payload()).containsEntry("density", "light").containsEntry("densityRank", 1)
                .containsEntry("start", "2026-10-12T12:00:00Z").containsEntry("end", "2026-10-12T15:00:00Z")
                .containsEntry("satellite", "GOES-WEST")
                .containsEntry("label", "Light smoke (HMS, GOES-WEST, 12:00-15:00 UTC)");
        // Ids are stable across parses.
        assertThat(HmsKmlParser.parse(AdapterFixtures.bytes("hms", FILE), DAY).ids()).isEqualTo(r.ids());
    }

    @Test
    @DisplayName("density from the style URL wins; heavy maps to rank 3")
    void densityFromStyle() {
        HmsKmlParser.Result r = HmsKmlParser.parse(kml(placemark("#Smoke_Heavy_style",
                "Start Time: 2026285 1800UTC<br>End Time: 2026285 2100UTC<br>Density: Light<br>Satellite: GOES-EAST", null)),
                DAY);

        assertThat(r.records()).hasSize(1);
        assertThat(r.records().get(0).payload()).containsEntry("density", "heavy").containsEntry("densityRank", 3)
                .containsEntry("satellite", "GOES-EAST")
                .containsEntry("label", "Heavy smoke (HMS, GOES-EAST, 18:00-21:00 UTC)");
        assertThat(HmsKmlParser.density("#Smoke_Medium_style", null, null)).isEqualTo("medium");
        assertThat(HmsKmlParser.density(null, "27.000", null)).isEqualTo("heavy");
        assertThat(HmsKmlParser.density(null, "16", null)).isEqualTo("medium");
        assertThat(HmsKmlParser.density(null, null, "Light")).isEqualTo("light");
        assertThat(HmsKmlParser.density("#Other", "x", null)).isNull();
    }

    @Test
    @DisplayName("ExtendedData supplies density, start, end and satellite when there is no style or description")
    void extendedData() {
        String extended = "<ExtendedData><SchemaData schemaUrl=\"#smoke\"><SimpleData name=\"Satellite\">GOES-EAST"
                + "</SimpleData><SimpleData name=\"Start\">2026285 1200</SimpleData><SimpleData name=\"End\">2026285 1400"
                + "</SimpleData><SimpleData name=\"Density\">16.000</SimpleData></SchemaData></ExtendedData>";
        HmsKmlParser.Result r = HmsKmlParser.parse(kml(placemark(null, null, extended)), DAY);

        assertThat(r.records()).hasSize(1);
        NormalizedRecord rec = r.records().get(0);
        assertThat(rec.payload()).containsEntry("density", "medium").containsEntry("satellite", "GOES-EAST");
        assertThat(rec.eventTime()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(rec.observedAt()).isEqualTo(Instant.parse("2026-10-12T14:00:00Z"));

        String data = "<ExtendedData><Data name=\"Density\"><value>Light</value></Data><Data name=\"Start\"><value>"
                + "2026285 0600</value></Data><Data name=\"End\"><value>2026285 0700</value></Data></ExtendedData>";
        assertThat(HmsKmlParser.parse(kml(placemark(null, null, data)), DAY).records().get(0).payload())
                .containsEntry("density", "light").doesNotContainKey("satellite");
    }

    @Test
    @DisplayName("a placemark without a time or density is skipped; duplicates keep one id")
    void skipsAndDuplicates() {
        String good = placemark("#Smoke_Light_style", "Start Time: 2026285 1200UTC<br>End Time: 2026285 1500UTC", null);
        String noTime = placemark("#Smoke_Light_style", "Density: Light", null);
        String noDensity = placemark(null, "Start Time: 2026285 1200UTC<br>End Time: 2026285 1500UTC", null);
        HmsKmlParser.Result r = HmsKmlParser.parse(kml(good + good + noTime + noDensity), DAY);

        assertThat(r.placemarks()).isEqualTo(4);
        assertThat(r.skipped()).isEqualTo(2);
        assertThat(r.records()).hasSize(1);
    }

    @Test
    @DisplayName("the id hashes date, times, density, satellite and the centroid to 0.1 degree")
    void ids() {
        Instant start = Instant.parse("2026-10-12T12:00:00Z");
        Instant end = Instant.parse("2026-10-12T15:00:00Z");
        String id = HmsKmlParser.externalId(DAY, start, end, "light", "GOES-WEST", 30.51, -96.52);
        assertThat(id).hasSize(16).matches("[0-9a-f]+");
        assertThat(HmsKmlParser.externalId(DAY, start, end, "light", "GOES-WEST", 30.54, -96.51)).isEqualTo(id);
        assertThat(HmsKmlParser.externalId(DAY, start, end, "medium", "GOES-WEST", 30.51, -96.52)).isNotEqualTo(id);
        assertThat(HmsKmlParser.externalId(DAY.plusDays(1), start, end, "light", "GOES-WEST", 30.51, -96.52))
                .isNotEqualTo(id);
    }

    @Test
    @DisplayName("an XXE attempt is refused: the DTD never resolves")
    void xxeRejected() {
        byte[] xxe = ("<?xml version=\"1.0\"?>\n<!DOCTYPE kml [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>\n"
                + "<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document><Placemark><description>&xxe;</description>"
                + "</Placemark></Document></kml>").getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> HmsKmlParser.parse(xxe, DAY)).isInstanceOf(UpstreamFormatException.class);

        byte[] billion = ("<?xml version=\"1.0\"?>\n<!DOCTYPE kml [<!ENTITY a \"aaaaaaaaaa\"><!ENTITY b \"&a;&a;&a;&a;\">]>"
                + "<kml><Document><name>&b;</name></Document></kml>").getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> HmsKmlParser.parse(billion, DAY)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("malformed XML, an empty body, a non-KML root or bad coordinates are format errors")
    void formatErrors() {
        assertThatThrownBy(() -> HmsKmlParser.parse(new byte[0], DAY)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> HmsKmlParser.parse("<kml><Document>".getBytes(StandardCharsets.UTF_8), DAY))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> HmsKmlParser.parse("<html/>".getBytes(StandardCharsets.UTF_8), DAY))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> HmsKmlParser.coordinates("-97.0 30.0")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> HmsKmlParser.coordinates("-197.0,30.0")).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("julian times: YYYYDDD HHMM in UTC")
    void julian() {
        assertThat(HmsKmlParser.julian("2026285", "1200")).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(HmsKmlParser.julian("2026001", "0000")).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(HmsKmlParser.julian("2026400", "0000")).isNull();
        assertThat(HmsKmlParser.julian("20x6285", "0000")).isNull();
    }
}
