package ai.foresignal.source.adapters.roads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;

/**
 * {@link Datex3SituationParser} on NDW's real DATEX II v3 files captured 2026-10-13 (the gzip files and trimmed XML
 * of 30 and 20 situations), hand-made edge cases, the "lat lon" axis order, XXE and a decompression bomb.
 */
class Datex3SituationParserTest {

    static final Instant NOW = Instant.parse("2026-10-13T19:00:00Z");

    static ParsedEvents parse(String source, String name) {
        return Datex3SituationParser.parse(AdapterFixtures.bytes(source, name), NOW);
    }

    static SourceDescriptor descriptor() {
        return new NdwSituationsAdapter(NdwSituationsAdapter.FEEDS.getFirst(), null).descriptor();
    }

    static long count(List<NormalizedRecord> records, String key, Object value) {
        return records.stream().filter(r -> value.equals(r.payload().get(key))).count();
    }

    @Test
    @DisplayName("the trimmed current situations: 30 situations, category from the first record, closures")
    void trimmedSituations() {
        ParsedEvents parsed = parse("ndw-situations", "actueel_beeld.xml");
        List<NormalizedRecord> records = parsed.records();
        assertThat(records).hasSize(30);
        assertThat(parsed.skipped()).isEmpty();
        assertThat(count(records, "category", "closure")).isEqualTo(9);
        assertThat(count(records, "category", "incident")).isEqualTo(10);
        assertThat(count(records, "category", "restriction")).isEqualTo(4);
        assertThat(count(records, "category", "roadwork")).isEqualTo(4);
        assertThat(count(records, "category", "other")).isEqualTo(3);
        assertThat(count(records, "isClosure", true)).isEqualTo(11);
        assertThat(records.stream().filter(r -> r.validFrom() != null)).hasSize(30);
        assertThat(records.stream().filter(r -> r.validUntil() != null)).hasSize(15);
        assertThat(records).allSatisfy(r -> assertThat(r.payload()).containsEntry("format", "datex3")
                .containsEntry("language", "nl").containsKey("headline").containsKey("severity"));
        NormalizedRecord closure = records.stream().filter(r -> r.externalId()
                .equals("NDW18_5db53aee-5bfc-44e5-b941-a860a315e879_SIT")).findFirst().orElseThrow();
        assertThat(closure.observedAt()).isEqualTo(Instant.parse("2026-09-29T18:10:17.011553895Z"));
        assertThat(closure.validUntil()).isEqualTo(Instant.parse("2026-11-06T15:00:00Z"));
        assertThat(closure.payload()).containsEntry("category", "closure").containsEntry("isClosure", true)
                .containsEntry("severity", "major").containsEntry("subtype", "carriageway-closures")
                .containsEntry("headline", "Carriageway closures (road maintenance)");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("the whole gzip files: 995 current situations and 169 SRTI messages, every point inside the Netherlands")
    void gzipFilesAndAxisOrder() {
        ParsedEvents situations = parse("ndw-situations", "actueel_beeld.xml.gz");
        assertThat(situations.records()).hasSize(995);
        ParsedEvents srti = parse("ndw-srti", "veiligheidsgerelateerde_berichten_srti.xml.gz");
        assertThat(srti.records()).hasSize(169);
        assertThat(srti.records()).allSatisfy(r -> assertThat(r.payload()).containsEntry("category", "incident"));
        int points = 0;
        for (ParsedEvents parsed : List.of(situations, srti)) {
            for (NormalizedRecord r : parsed.records()) {
                if (r.geometry() == null) {
                    continue;
                }
                for (Coordinate c : r.geometry().getCoordinates()) {
                    points++;
                    assertThat(c.y).as("latitude of %s (posList is lat lon)", r.externalId()).isBetween(50.5, 53.7);
                    assertThat(c.x).as("longitude of %s", r.externalId()).isBetween(3.2, 7.3);
                }
            }
        }
        assertThat(points).isGreaterThan(1000);
    }

    @Test
    @DisplayName("the trimmed SRTI file: 20 point messages")
    void trimmedSrti() {
        ParsedEvents parsed = parse("ndw-srti", "veiligheidsgerelateerde_berichten_srti.xml");
        assertThat(parsed.records()).hasSize(20).allSatisfy(r -> {
            assertThat(r.geometry().getGeometryType()).isEqualTo("Point");
            assertThat(r.payload()).containsEntry("category", "incident");
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), parsed.records());
    }

    @Test
    @DisplayName("edge cases: combined geometry, lanes, speed, the interval, comments, weather, skips")
    void edgeCases() {
        ParsedEvents parsed = parse("ndw-situations", "edge-cases.xml");
        assertThat(parsed.skipped()).containsOnly(Map.entry(ParsedEvents.MISSING_ID, 1),
                Map.entry(ParsedEvents.MISSING_OBSERVED_AT, 1));
        assertThat(parsed.ids()).containsExactly("EDGE_WORKS", "EDGE_WEATHER", "EDGE_ABNORMAL", "EDGE_CLOSED");

        NormalizedRecord works = parsed.records().get(0);
        assertThat(works.geometry().getGeometryType()).isEqualTo("GeometryCollection");
        assertThat(works.geometry().getGeometryN(0).getCoordinates()[0]).isEqualTo(new Coordinate(5.0, 52.0));
        assertThat(works.validFrom()).isEqualTo(Instant.parse("2026-10-20T20:00:00Z"));
        assertThat(works.validUntil()).isEqualTo(Instant.parse("2026-10-21T06:00:00Z"));
        assertThat(works.payload()).containsEntry("category", "roadwork").containsEntry("subtype", "resurfacing-work")
                .containsEntry("severity", "major").containsEntry("headline", "Werkzaamheden aan de A2.")
                .containsEntry("description", "Werkzaamheden aan de A2. Rijstrook dicht.")
                .containsEntry("lanesAffected", "1 of 3 lanes restricted").containsEntry("speedLimitKph", 70.0)
                .containsEntry("direction", "both").containsEntry("status", "planned")
                .containsEntry("isClosure", false);

        NormalizedRecord weather = parsed.records().get(1);
        assertThat(weather.geometry().getGeometryType()).as("a one-pair posList is a point").isEqualTo("Point");
        assertThat(weather.validUntil()).isNull();
        assertThat(weather.payload()).containsEntry("category", "weather").containsEntry("severity", "minor")
                .containsEntry("headline", "Fog");

        NormalizedRecord abnormal = parsed.records().get(2);
        assertThat(abnormal.geometry()).isNull();
        assertThat(abnormal.payload()).containsEntry("category", "other").containsEntry("headline",
                "Stationary traffic");

        NormalizedRecord closed = parsed.records().get(3);
        assertThat(closed.payload()).containsEntry("category", "closure").containsEntry("isClosure", true)
                .containsEntry("severity", "severe").containsEntry("headline", "Road closed (accident)");
    }

    @ParameterizedTest
    @CsvSource({
        "Accident, , , incident",
        "VehicleObstruction, , , incident",
        "GeneralObstruction, , , incident",
        "RoadOrCarriagewayOrLaneManagement, carriagewayClosures, roadMaintenance, closure",
        "RoadOrCarriagewayOrLaneManagement, roadClosed, , closure",
        "RoadOrCarriagewayOrLaneManagement, laneClosures, roadMaintenance, roadwork",
        "RoadOrCarriagewayOrLaneManagement, laneClosures, accident, restriction",
        "MaintenanceWorks, , , roadwork",
        "SpeedManagement, , , restriction",
        "ReroutingManagement, , , restriction",
        "PoorEnvironmentConditions, , , weather",
        "AbnormalTraffic, , , other",
        "GeneralNetworkManagement, , , other",
    })
    @DisplayName("first record type → category")
    void categories(String type, String management, String cause, String category) {
        assertThat(Datex3SituationParser.category(type, management, cause)).isEqualTo(category);
    }

    @ParameterizedTest
    @CsvSource({"highest, severe", "high, major", "medium, moderate", "low, minor", "lowest, minor", "none, minor",
        "unknown, unknown"})
    @DisplayName("overallSeverity → severity")
    void severities(String upstream, String severity) {
        assertThat(Datex3SituationParser.SEVERITY.get(upstream)).isEqualTo(severity);
    }

    @Test
    @DisplayName("a DOCTYPE or entity reference is refused and never resolved")
    void xxe() {
        assertThatThrownBy(() -> parse("ndw-situations", "xxe.xml")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("DOCTYPE");
    }

    @Test
    @DisplayName("a gzip stream that inflates beyond 64 MB is refused; a corrupt one is a format error")
    void decompressionLimits() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(bytes)) {
            byte[] zeros = new byte[1 << 20];
            for (int i = 0; i < 65; i++) {
                gz.write(zeros);
            }
        }
        assertThatThrownBy(() -> Datex3SituationParser.parse(bytes.toByteArray(), NOW))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("inflates beyond");
        assertThatThrownBy(() -> Datex3SituationParser.parse(new byte[] {0x1f, (byte) 0x8b, 1, 2, 3}, NOW))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("gzip");
        assertThatThrownBy(() -> Datex3SituationParser.parse("not xml <".getBytes(StandardCharsets.UTF_8), NOW))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("two parses share each situation's static hash (adapter rule 7)")
    void stableHash() {
        List<NormalizedRecord> first = parse("ndw-situations", "actueel_beeld.xml").records();
        List<NormalizedRecord> second = parse("ndw-situations", "actueel_beeld.xml").records();
        for (NormalizedRecord a : first) {
            NormalizedRecord b = second.stream().filter(r -> r.externalId().equals(a.externalId())).findFirst()
                    .orElseThrow();
            SnapshotContract.assertStableStaticHash(descriptor(), List.of(a, b));
        }
    }
}
