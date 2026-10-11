package ai.foresignal.source.formats.geojson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

/** Foresignal's Jackson 3 GeoJSON reader (replacing jts-io-common). */
class GeoJsonReaderTest {

    private final GeoJsonReader reader = new GeoJsonReader();

    private Geometry read(String json) {
        return reader.read(json);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"type\":\"Point\",\"coordinates\":[1,2]}|POINT (1 2)",
                "{\"type\":\"MultiPoint\",\"coordinates\":[[1,2],[3,4]]}|MULTIPOINT ((1 2), (3 4))",
                "{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1]]}|LINESTRING (0 0, 1 1)",
                "{\"type\":\"MultiLineString\",\"coordinates\":[[[0,0],[1,1]],[[2,2],[3,3]]]}|MULTILINESTRING ((0 0, 1 1), (2 2, 3 3))",
                "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[4,0],[4,4],[0,4],[0,0]],[[1,1],[2,1],[2,2],[1,1]]]}|POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 2 1, 2 2, 1 1))",
                "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[0,0],[1,0],[1,1],[0,0]]]]}|MULTIPOLYGON (((0 0, 1 0, 1 1, 0 0)))",
                "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[1,2]}]}|GEOMETRYCOLLECTION (POINT (1 2))"
            })
    @DisplayName("every geometry type reads with SRID 4326")
    void geometryTypes(String spec) {
        String[] parts = spec.split("\\|");
        Geometry geometry = read(parts[0]);
        assertThat(geometry.toText()).isEqualTo(parts[1]);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a Feature unwraps to its geometry; a FeatureCollection to a collection")
    void features() {
        assertThat(read("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]}}").toText())
                .isEqualTo("POINT (1 2)");
        Geometry collection =
                read("{\"type\":\"FeatureCollection\",\"features\":["
                        + "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]}},"
                        + "{\"type\":\"Feature\",\"geometry\":null},"
                        + "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[3,4]}}]}");
        assertThat(collection).isInstanceOf(GeometryCollection.class);
        assertThat(collection.getNumGeometries()).isEqualTo(2);
    }

    @Test
    @DisplayName("a third ordinate is kept as Z (the normalizer drops it)")
    void keepsZ() {
        assertThat(read("{\"type\":\"Point\",\"coordinates\":[1,2,300]}").getCoordinate().getZ()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("vertices are counted before anything is constructed")
    void vertexLimit() {
        GeoJsonReader small = new GeoJsonReader(3, 8);
        assertThat(small.read("{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1],[2,2]]}").getNumPoints()).isEqualTo(3);
        assertThatThrownBy(() -> small.read("{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1],[2,2],[3,3]]}"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("vertices");
        assertThatThrownBy(() -> small.read("{\"type\":\"GeometryCollection\",\"geometries\":["
                        + "{\"type\":\"MultiPoint\",\"coordinates\":[[0,0],[1,1]]},"
                        + "{\"type\":\"MultiPoint\",\"coordinates\":[[0,0],[1,1]]}]}"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("collection nesting is bounded")
    void depthLimit() {
        GeoJsonReader shallow = new GeoJsonReader(100, 2);
        String nested = "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"GeometryCollection\",\"geometries\":["
                + "{\"type\":\"GeometryCollection\",\"geometries\":[]}]}]}";
        assertThatThrownBy(() -> shallow.read(nested)).isInstanceOf(InvalidInputException.class).hasMessageContaining("nest");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "not json",
                "[]",
                "{}",
                "{\"type\":\"Circle\",\"coordinates\":[0,0]}",
                "{\"type\":\"Point\"}",
                "{\"type\":\"Point\",\"coordinates\":[1]}",
                "{\"type\":\"Point\",\"coordinates\":[\"1\",2]}",
                "{\"type\":\"Point\",\"coordinates\":[NaN,2]}",
                "{\"type\":\"Point\",\"coordinates\":[1e999,2]}",
                "{\"type\":\"LineString\",\"coordinates\":[0,0]}",
                "{\"type\":\"LineString\",\"coordinates\":[[0,0]]}",
                "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1]]]}",
                "{\"type\":\"Feature\",\"geometry\":null}",
                "{\"type\":\"FeatureCollection\"}",
                "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Point\",\"coordinates\":[1,2]}]}",
                "{\"type\":7}"
            })
    @DisplayName("malformed GeoJSON is an InvalidInputException")
    void rejectsMalformed(String json) {
        assertThatThrownBy(() -> read(json)).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("empty coordinate arrays read as empty geometries (Geo rejects them)")
    void emptyArrays() {
        assertThat(read("{\"type\":\"LineString\",\"coordinates\":[]}").isEmpty()).isTrue();
        assertThat(read("{\"type\":\"Polygon\",\"coordinates\":[]}").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("limits must be positive")
    void limitsMustBePositive() {
        assertThatThrownBy(() -> new GeoJsonReader(0, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reader.read((tools.jackson.databind.JsonNode) null)).isInstanceOf(InvalidInputException.class);
    }
}
