package ai.foresignal.inbound.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * 024's {@code cap} ingest format after prompt 067 moved it onto the shared {@code CapParser} and
 * {@code CapRecordMapper}: the same records, tombstones, rejections and option, plus the shared time mapping.
 * (These cases lived in {@code CapParserTest} before 067.)
 */
class CapIngestFormatTest {

    private static final Instant NOW = Instant.parse("2026-10-12T08:00:00Z");

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

    private static IngestRequest request(String xml) {
        return IngestRequest.of(xml.getBytes(StandardCharsets.UTF_8), MediaType.parseMediaType("application/cap+xml"), NOW);
    }

    @Test
    @DisplayName("an Alert becomes one record keyed by identifier with sent, onset, validFrom, expires and the payload")
    void alertRecord() {
        ParsedIngest parsed = format.parse(request(alert("Alert", "Actual", null, POLYGON_AREA)), source,
                FormatOptions.empty());

        NormalizedRecord record = parsed.records().get(0);
        assertThat(record.externalId()).isEqualTo("KSTO1055887203");
        assertThat(record.kind()).isEqualTo("cap-alert");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:15:00Z"));
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-10-12T07:20:00Z"));
        assertThat(record.validFrom()).isEqualTo(Instant.parse("2026-10-12T07:20:00Z"));
        assertThat(record.validUntil()).isEqualTo(Instant.parse("2026-10-12T08:00:00Z"));
        assertThat(record.payload()).containsEntry("severity", "Severe").containsEntry("msgType", "Alert")
                .containsEntry("state", "active")
                .containsEntry("areaDesc", "EXTREME NORTH CENTRAL TUOLUMNE COUNTY").containsKey("geocodes");
        assertThat(record.geometry()).isNotNull();
        assertThat(record.geometry().getCoordinates()[0].x).as("lat,lon swapped").isEqualTo(-120.14);
    }

    @Test
    @DisplayName("several areas make a MultiPolygon")
    void multipleAreas() {
        ParsedIngest parsed = format.parse(request(alert("Alert", "Actual", null, POLYGON_AREA + CIRCLE_AREA)), source,
                FormatOptions.empty());

        Geometry geometry = parsed.records().get(0).geometry();
        assertThat(geometry).isInstanceOf(MultiPolygon.class);
        assertThat(geometry.getNumGeometries()).isEqualTo(2);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("an Update adds the new record and tombstones the references; a Cancel only tombstones")
    void updateAndCancel() {
        String refs = "w-nws.webmaster@noaa.gov,KSTO1055887200,2026-10-12T06:00:00-00:00 "
                + "w-nws.webmaster@noaa.gov,KSTO1055887201,2026-10-12T06:30:00-00:00";
        ParsedIngest update = format.parse(request(alert("Update", "Actual", refs, POLYGON_AREA)), source,
                FormatOptions.empty());
        assertThat(update.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("KSTO1055887200", "KSTO1055887201", "KSTO1055887203");
        assertThat(update.records()).extracting(NormalizedRecord::tombstone).containsExactly(true, true, false);
        assertThat(update.indexes()).containsOnly(0);

        ParsedIngest cancel = format.parse(request(alert("Cancel", "Actual", refs, POLYGON_AREA)), source,
                FormatOptions.empty());
        assertThat(cancel.records()).hasSize(2).allSatisfy(r -> assertThat(r.tombstone()).isTrue());
    }

    @Test
    @DisplayName("a non-Actual status is rejected unless acceptNonActual is set")
    void nonActual() {
        ParsedIngest rejected = format.parse(request(alert("Alert", "Exercise", null, POLYGON_AREA)), source,
                FormatOptions.empty());
        assertThat(rejected.records()).isEmpty();
        assertThat(rejected.rejections()).singleElement().satisfies(r -> assertThat(r.reason()).isEqualTo("NON_ACTUAL_STATUS"));

        ParsedIngest accepted = format.parse(request(alert("Alert", "Exercise", null, POLYGON_AREA)), source,
                new FormatOptions(JsonMapper.builder().build().readTree("{\"acceptNonActual\":true}")));
        assertThat(accepted.records()).hasSize(1);
    }

    @Test
    @DisplayName("XXE, non-CAP, empty and (since 067) un-namespaced documents are 400s")
    void invalidInput() {
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE alert [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <alert xmlns="urn:oasis:names:tc:emergency:cap:1.2"><identifier>&xxe;</identifier></alert>
                """;
        assertThatThrownBy(() -> format.parse(request(xxe), source, FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(request("<notcap/>"), source, FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(request(""), source, FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        String bare = "<alert><identifier>x</identifier><sender>s</sender><sent>2026-10-12T07:15:00Z</sent>"
                + "<status>Actual</status><msgType>Alert</msgType><scope>Public</scope></alert>";
        assertThatThrownBy(() -> format.parse(request(bare), source, FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }
}
