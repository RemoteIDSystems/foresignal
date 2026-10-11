package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class GeoTest {

    @Test
    @DisplayName("a point carries SRID 4326 and lon/lat in that order")
    void pointIsWgs84() {
        Point point = Geo.point(-122.4194, 37.7749);

        assertThat(point.getSRID()).isEqualTo(4326);
        assertThat(point.getX()).isEqualTo(-122.4194);
        assertThat(point.getY()).isEqualTo(37.7749);
    }

    @ParameterizedTest
    @CsvSource({"-180, -90", "180, 90", "0, 0", "179.999, 89.999"})
    void acceptsTheRangeBoundaries(double lon, double lat) {
        assertThat(Geo.point(lon, lat)).isNotNull();
    }

    @ParameterizedTest(name = "rejects ({0}, {1})")
    @CsvSource({"181, 0", "-181, 0", "0, 91", "0, -91", "200, 200"})
    @DisplayName("out-of-range coordinates are rejected, which usually means a swapped lat/lon")
    void rejectsOutOfRange(double lon, double lat) {
        assertThatThrownBy(() -> Geo.point(lon, lat))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("outside");
    }

    @Test
    void rejectsNaN() {
        assertThatThrownBy(() -> Geo.point(Double.NaN, 0))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> Geo.point(0, Double.NaN))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("GeoJSON points parse with SRID 4326")
    void parsesGeoJsonPoint() {
        var geometry = Geo.fromGeoJson("{\"type\":\"Point\",\"coordinates\":[-122.4,37.8]}");

        assertThat(geometry).isInstanceOf(Point.class);
        assertThat(geometry.getSRID()).isEqualTo(4326);
        assertThat(geometry.getCoordinate().x).isEqualTo(-122.4);
        assertThat(geometry.getCoordinate().y).isEqualTo(37.8);
    }

    @Test
    void parsesGeoJsonLineString() {
        var geometry =
                Geo.fromGeoJson("{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1],[2,2]]}");

        assertThat(geometry).isInstanceOf(LineString.class);
        assertThat(geometry.getNumPoints()).isEqualTo(3);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    void parsesGeoJsonPolygon() {
        var geometry =
                Geo.fromGeoJson(
                        "{\"type\":\"Polygon\",\"coordinates\":"
                                + "[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}");

        assertThat(geometry).isInstanceOf(Polygon.class);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a GeoJSON geometry already decoded into a map parses too")
    void parsesGeoJsonFromMap() {
        Map<String, Object> geoJson = new LinkedHashMap<>();
        geoJson.put("type", "Point");
        geoJson.put("coordinates", List.of(10.0, 20.0));

        var geometry = Geo.fromGeoJson(geoJson);

        assertThat(geometry).isInstanceOf(Point.class);
        assertThat(geometry.getSRID()).isEqualTo(4326);
        assertThat(geometry.getCoordinate().x).isEqualTo(10.0);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "   ",
                "not json",
                "{}",
                "{\"type\":\"Nonsense\",\"coordinates\":[0,0]}",
                "{\"coordinates\":[0,0]}",
                // A geometry object with no coordinates: JTS reads this as POINT EMPTY rather
                // than failing, and an empty geometry is a missing position, not a location.
                "{\"type\":\"Point\"}",
                "{\"type\":\"LineString\",\"coordinates\":[]}",
            })
    @DisplayName("invalid GeoJSON is reported as an upstream format problem")
    void rejectsInvalidGeoJson(String geoJson) {
        assertThatThrownBy(() -> Geo.fromGeoJson(geoJson))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    void rejectsNullGeoJson() {
        assertThatThrownBy(() -> Geo.fromGeoJson((String) null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> Geo.fromGeoJson((Map<String, Object>) null))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the failure carries a snippet of what would not parse")
    void failureCarriesSnippet() {
        assertThatThrownBy(() -> Geo.fromGeoJson("{\"type\":\"Nope\"}"))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("Nope");
    }

    @Test
    @DisplayName("a long snippet is truncated so a 32 MB body cannot reach a log line")
    void snippetIsTruncated() {
        String huge = "x".repeat(10_000);

        var exception = new UpstreamFormatException("boom", huge);

        assertThat(exception.getSnippet())
                .hasSize(UpstreamFormatException.MAX_SNIPPET_LENGTH + "… (truncated)".length())
                .startsWith("xxxx")
                .endsWith("… (truncated)");
    }

    @Test
    @DisplayName("a short snippet is kept whole")
    void shortSnippetIsKeptWhole() {
        assertThat(new UpstreamFormatException("boom", "{\"a\":1}").getSnippet())
                .isEqualTo("{\"a\":1}");
        assertThat(new UpstreamFormatException("boom", null).getSnippet()).isEmpty();
    }

    @Test
    @DisplayName("exactly (0,0) is recognised as null island")
    void detectsNullIsland() {
        assertThat(Geo.isNullIsland(Geo.point(0, 0))).isTrue();
        assertThat(Geo.isNullIsland(Geo.point(0.0001, 0))).isFalse();
        assertThat(Geo.isNullIsland(Geo.point(-122.4, 37.8))).isFalse();
        assertThat(Geo.isNullIsland(null)).isFalse();
    }

    @Test
    @DisplayName("a non-point geometry is never null island")
    void nullIslandOnlyAppliesToPoints() {
        var line = Geo.fromGeoJson("{\"type\":\"LineString\",\"coordinates\":[[0,0],[0,0]]}");

        assertThat(Geo.isNullIsland(line)).isFalse();
    }

    @Test
    void sharedFactoryIsWgs84() {
        assertThat(Geo.factory().getSRID()).isEqualTo(4326);
        assertThat(Geo.SRID).isEqualTo(4326);
    }
}
