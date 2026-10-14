package ai.foresignal.source.adapters.deepstate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * {@link DeepStateParser} and {@link GeodesicArea} on two real consecutive daily files (prompt 081): the MultiPolygon
 * kept as published, the WGS84 area and the change between the days, the 03:00 UTC time base, simplification above the
 * vertex limit, format errors, the fieldSpecs contract and a stable static hash.
 */
class DeepStateParserTest {

    private static final DeepStateAdapter ADAPTER = new DeepStateAdapter("");

    private static DeepStateParser.MapFile map(String day) {
        return DeepStateParser.parse(AdapterFixtures.bytes("deepstate-frontline", "deepstatemap_data_" + day
                + ".geojson"));
    }

    @Test
    @DisplayName("12 and 13 October: 21-polygon MultiPolygons of 2,908 and 2,918 vertices, kept as published")
    void geometry() {
        DeepStateParser.MapFile twelfth = map("20261012");
        DeepStateParser.MapFile thirteenth = map("20261013");
        assertThat(twelfth.geometry().getGeometryType()).isEqualTo("MultiPolygon");
        assertThat(twelfth.geometry().getNumGeometries()).isEqualTo(21);
        assertThat(twelfth.geometry().getNumPoints()).isEqualTo(2_908);
        assertThat(thirteenth.geometry().getNumPoints()).isEqualTo(2_918);
        assertThat(twelfth.simplified()).isFalse();
        assertThat(twelfth.geometry().getSRID()).isEqualTo(Geo.SRID);
        assertThat(twelfth.geometry().isValid()).isTrue();
    }

    @Test
    @DisplayName("areas 117,147.4 and 117,154.9 km²; the record of the 13th carries the change of +7.5 km²")
    void areaAndChange() {
        DeepStateParser.MapFile twelfth = map("20261012");
        DeepStateParser.MapFile thirteenth = map("20261013");
        assertThat(twelfth.areaKm2()).isEqualTo(117_147.4);
        assertThat(thirteenth.areaKm2()).isEqualTo(117_154.9);
        NormalizedRecord r = DeepStateParser.record(thirteenth, LocalDate.of(2026, 10, 13), "main",
                thirteenth.areaKm2() - twelfth.areaKm2());
        assertThat(r.externalId()).isEqualTo("occupied-area");
        assertThat(r.kind()).isEqualTo("frontline");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T03:00:00Z"));
        assertThat(r.eventTime()).isEqualTo(r.observedAt());
        assertThat(r.payload()).containsExactlyInAnyOrderEntriesOf(Map.of("mapDate", "2026-10-13",
                "areaKm2", 117_154.9, "areaChangeKm2", 7.5, "ref", "main"));
        NormalizedRecord first = DeepStateParser.record(twelfth, LocalDate.of(2026, 10, 12), "main", null);
        assertThat(first.payload()).doesNotContainKey("areaChangeKm2");
    }

    @Test
    @DisplayName("the WGS84 area of a 1° cell: 12,308.8 km² at the equator, 8,217.9 km² at 48° N (within 0.1 %)")
    void geodesicArea() {
        assertThat(GeodesicArea.km2(box(0, 0))).isCloseTo(12_308.78, within(12.3));
        assertThat(GeodesicArea.km2(box(30, 48))).isCloseTo(8_217.9, within(8.2));
        Geometry withHole = Geo.fromGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[2,0],[2,2],[0,2],[0,0]],"
                + "[[0.5,0.5],[1.5,0.5],[1.5,1.5],[0.5,1.5],[0.5,0.5]]]}");
        assertThat(GeodesicArea.km2(withHole)).isCloseTo(3 * 12_308.0, within(40.0));
        assertThat(GeodesicArea.km2(null)).isZero();
        assertThat(GeodesicArea.km2(Geo.point(30, 48))).isZero();
    }

    private static Geometry box(double lon, double lat) {
        return Geo.fromGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[" + lon + "," + lat + "],[" + (lon + 1) + ","
                + lat + "],[" + (lon + 1) + "," + (lat + 1) + "],[" + lon + "," + (lat + 1) + "],[" + lon + "," + lat
                + "]]]}");
    }

    @Test
    @DisplayName("a bare Feature is read; a geometry above 9,500 vertices is simplified and flagged")
    void simplification() {
        StringBuilder ring = new StringBuilder();
        int n = 12_000;
        for (int i = 0; i <= n; i++) {
            double a = 2 * Math.PI * (i % n) / n;
            if (i > 0) {
                ring.append(',');
            }
            ring.append('[').append(35 + Math.cos(a)).append(',').append(48 + Math.sin(a) * 0.7).append(']');
        }
        String feature = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[["
                + ring + "]]}}";
        DeepStateParser.MapFile big = DeepStateParser.parse(feature.getBytes(StandardCharsets.UTF_8));
        assertThat(big.simplified()).isTrue();
        assertThat(big.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
        NormalizedRecord r = DeepStateParser.record(big, LocalDate.of(2026, 10, 13), "main", null);
        assertThat(r.payload()).containsEntry(FeatureGeometry.SIMPLIFIED_KEY, true)
                .containsKey(FeatureGeometry.TOLERANCE_KEY);
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), List.of(r));
    }

    @Test
    @DisplayName("a file without a polygon feature, a point, or HTML is a format error")
    void formatErrors() {
        for (String body : new String[] {"{\"type\":\"FeatureCollection\",\"features\":[]}",
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[35,48]}}",
                "<html>rate limited</html>", "[]"}) {
            assertThatThrownBy(() -> DeepStateParser.parse(body.getBytes(StandardCharsets.UTF_8)))
                    .as(body).isInstanceOf(UpstreamFormatException.class);
        }
    }

    @Test
    @DisplayName("every payload key is declared with its type (the fieldSpecs contract); the descriptor validates")
    void fieldSpecs() {
        DeepStateParser.MapFile m = map("20261013");
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(),
                List.of(DeepStateParser.record(m, LocalDate.of(2026, 10, 13), "main", 7.5)));
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
    }

    @Test
    @DisplayName("the same map read twice is one static content")
    void stableStaticHash() {
        DeepStateParser.MapFile m = map("20261013");
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(), List.of(
                DeepStateParser.record(m, LocalDate.of(2026, 10, 13), "main", 7.5),
                DeepStateParser.record(map("20261013"), LocalDate.of(2026, 10, 13), "main", 7.5)));
    }
}
