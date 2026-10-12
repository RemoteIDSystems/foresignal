package ai.foresignal.source.formats.cap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.CapFormat;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

/** {@link CapParser} and {@link CapFormat} (prompt 024). */
class CapParserTest {

    private static final Instant NOW = Instant.parse("2026-10-12T08:00:00Z");

    private final CapParser parser = new CapParser();
    private final CapFormat format = new CapFormat();
    private final SourceDescriptor source =
            SourceDescriptor.builder("custom-cap").kind("cap-alert").mode(Mode.PUSH).defaultBudget(60, 86_400).build();

    static String alert(String msgType, String status, String references, String areas) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
                  <identifier>KSTO1055887203</identifier>
                  <sender>w-nws.webmaster@noaa.gov</sender>
                  <sent>2026-10-12T07:15:00-00:00</sent>
                  <status>%s</status>
                  <msgType>%s</msgType>
                  <scope>Public</scope>
                  %s
                  <info>
                    <language>en-US</language>
                    <category>Met</category>
                    <event>SEVERE THUNDERSTORM</event>
                    <urgency>Immediate</urgency>
                    <severity>Severe</severity>
                    <certainty>Observed</certainty>
                    <onset>2026-10-12T07:20:00-00:00</onset>
                    <effective>2026-10-12T07:15:00-00:00</effective>
                    <expires>2026-10-12T08:00:00-00:00</expires>
                    <headline>SEVERE THUNDERSTORM WARNING</headline>
                    <description>Large hail possible.</description>
                    <instruction>Take shelter.</instruction>
                    %s
                  </info>
                </alert>
                """.formatted(status, msgType, references == null ? "" : "<references>" + references + "</references>", areas);
    }

    static final String POLYGON_AREA = """
            <area><areaDesc>EXTREME NORTH CENTRAL TUOLUMNE COUNTY</areaDesc>
              <polygon>38.47,-120.14 38.34,-119.95 38.52,-119.74 38.62,-119.89 38.47,-120.14</polygon>
              <geocode><valueName>SAME</valueName><value>006109</value></geocode></area>""";

    static final String CIRCLE_AREA = """
            <area><areaDesc>Around the plant</areaDesc><circle>32.9525,-115.5527 10</circle></area>""";

    private static byte[] bytes(String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("polygons are lat,lon and are swapped to lon/lat; fields, times and geocodes are read")
    void polygonSwap() {
        CapAlert alert = parser.parse(bytes(alert("Alert", "Actual", null, POLYGON_AREA)));

        assertThat(alert.identifier()).isEqualTo("KSTO1055887203");
        assertThat(alert.sent()).isEqualTo(Instant.parse("2026-10-12T07:15:00Z"));
        CapAlert.Info info = alert.firstInfo();
        assertThat(info.event()).isEqualTo("SEVERE THUNDERSTORM");
        assertThat(info.expires()).isEqualTo(Instant.parse("2026-10-12T08:00:00Z"));
        Polygon polygon = info.areas().get(0).polygons().get(0);
        assertThat(polygon.getCoordinates()[0].x).isEqualTo(-120.14);
        assertThat(polygon.getCoordinates()[0].y).isEqualTo(38.47);
        assertThat(info.areas().get(0).geocodes()).containsExactly(new CapAlert.Geocode("SAME", "006109"));
    }

    @Test
    @DisplayName("a circle becomes a closed 64-segment polygon around its centre")
    void circle() {
        Polygon polygon = parser.circle("32.9525,-115.5527 10");

        assertThat(polygon.getExteriorRing().getNumPoints()).isEqualTo(CapParser.CIRCLE_SEGMENTS + 1);
        assertThat(polygon.getCentroid().getX()).isCloseTo(-115.5527, org.assertj.core.data.Offset.offset(0.01));
        assertThat(polygon.getCentroid().getY()).isCloseTo(32.9525, org.assertj.core.data.Offset.offset(0.01));
        // 10 km is about 0.09 degrees of latitude.
        assertThat(polygon.getEnvelopeInternal().getMaxY() - 32.9525).isCloseTo(0.0899, org.assertj.core.data.Offset.offset(0.002));
    }

    @Test
    @DisplayName("several areas make a MultiPolygon")
    void multipleAreas() {
        CapAlert alert = parser.parse(bytes(alert("Alert", "Actual", null, POLYGON_AREA + CIRCLE_AREA)));

        Geometry geometry = alert.geometry(parser.factory());
        assertThat(geometry).isInstanceOf(MultiPolygon.class);
        assertThat(geometry.getNumGeometries()).isEqualTo(2);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("an Alert becomes one record with sent, onset, expires and the payload fields")
    void alertRecord() {
        ParsedIngest parsed = format.parse(request(alert("Alert", "Actual", null, POLYGON_AREA)), source, FormatOptions.empty());

        NormalizedRecord record = parsed.records().get(0);
        assertThat(record.externalId()).isEqualTo("KSTO1055887203");
        assertThat(record.kind()).isEqualTo("cap-alert");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:15:00Z"));
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-10-12T07:20:00Z"));
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-12T08:00:00Z"));
        assertThat(record.payload()).containsEntry("severity", "Severe").containsEntry("msgType", "Alert")
                .containsEntry("areaDesc", "EXTREME NORTH CENTRAL TUOLUMNE COUNTY").containsKey("geocodes");
    }

    @Test
    @DisplayName("an Update adds the new record and tombstones the references; a Cancel only tombstones")
    void updateAndCancel() {
        String refs = "w-nws.webmaster@noaa.gov,KSTO1055887200,2026-10-12T06:00:00-00:00 "
                + "w-nws.webmaster@noaa.gov,KSTO1055887201,2026-10-12T06:30:00-00:00";
        ParsedIngest update = format.parse(request(alert("Update", "Actual", refs, POLYGON_AREA)), source, FormatOptions.empty());
        assertThat(update.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("KSTO1055887200", "KSTO1055887201", "KSTO1055887203");
        assertThat(update.records()).extracting(NormalizedRecord::tombstone).containsExactly(true, true, false);
        assertThat(update.indexes()).containsOnly(0);

        ParsedIngest cancel = format.parse(request(alert("Cancel", "Actual", refs, POLYGON_AREA)), source, FormatOptions.empty());
        assertThat(cancel.records()).hasSize(2).allSatisfy(r -> assertThat(r.tombstone()).isTrue());
    }

    @Test
    @DisplayName("a non-Actual status is rejected unless acceptNonActual is set")
    void nonActual() {
        ParsedIngest rejected = format.parse(request(alert("Alert", "Exercise", null, POLYGON_AREA)), source, FormatOptions.empty());
        assertThat(rejected.records()).isEmpty();
        assertThat(rejected.rejections()).singleElement().satisfies(r -> assertThat(r.reason()).isEqualTo("NON_ACTUAL_STATUS"));

        ParsedIngest accepted = format.parse(request(alert("Alert", "Exercise", null, POLYGON_AREA)), source,
                new FormatOptions(JsonMapper.builder().build().readTree("{\"acceptNonActual\":true}")));
        assertThat(accepted.records()).hasSize(1);
    }

    @Test
    @DisplayName("an XXE payload with a DOCTYPE is refused before anything is resolved")
    void xxeRejected() {
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE alert [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <alert xmlns="urn:oasis:names:tc:emergency:cap:1.2"><identifier>&xxe;</identifier></alert>
                """;
        assertThatThrownBy(() -> parser.parse(bytes(xxe))).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> parser.parse(bytes("<notcap/>"))).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> parser.parse(new byte[0])).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("an un-namespaced alert is accepted too")
    void noNamespace() {
        CapAlert alert = parser.parse(bytes("<alert><identifier>x</identifier><status>Actual</status><msgType>Alert</msgType></alert>"));
        assertThat(alert.identifier()).isEqualTo("x");
        assertThat(alert.geometry(parser.factory())).isNull();
    }

    private static IngestRequest request(String xml) {
        return IngestRequest.of(bytes(xml), MediaType.parseMediaType("application/cap+xml"), NOW);
    }
}
