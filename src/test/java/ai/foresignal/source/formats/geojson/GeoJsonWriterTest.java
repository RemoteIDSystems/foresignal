package ai.foresignal.source.formats.geojson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

class GeoJsonWriterTest {

    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
    private final GeoJsonWriter writer = new GeoJsonWriter();
    private final GeoJsonReader reader = new GeoJsonReader();

    @Test
    @DisplayName("null stays null")
    void nullGeometry() {
        assertThat(writer.write(null)).isNull();
    }

    @Test
    @DisplayName("a point is [lon, lat], with elevation when set")
    void point() {
        assertThat(writer.write(factory.createPoint(new Coordinate(-79.4, 43.7))))
                .containsEntry("type", "Point")
                .containsEntry("coordinates", List.of(-79.4, 43.7));
        assertThat(writer.write(factory.createPoint(new Coordinate(1, 2, 3))).get("coordinates"))
                .isEqualTo(List.of(1.0, 2.0, 3.0));
        assertThat(writer.write(factory.createPoint()).get("coordinates")).isEqualTo(List.of());
    }

    @Test
    @DisplayName("every geometry type round-trips through the reader")
    void roundTrips() throws Exception {
        String[] samples = {
            "{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1]]}",
            "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[4,0],[4,4],[0,4],[0,0]],[[1,1],[2,1],[2,2],[1,1]]]}",
            "{\"type\":\"MultiPoint\",\"coordinates\":[[0,0],[1,1]]}",
            "{\"type\":\"MultiLineString\",\"coordinates\":[[[0,0],[1,1]],[[2,2],[3,3]]]}",
            "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[0,0],[1,0],[1,1],[0,0]]]]}",
            "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[5,6]}]}"
        };
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        for (String sample : samples) {
            Geometry original = reader.read(sample);
            Map<String, Object> written = writer.write(original);
            Geometry back = reader.read(mapper.writeValueAsString(written));
            assertThat(back.equalsExact(original)).as(sample).isTrue();
            assertThat(written.get("type")).isEqualTo(original.getGeometryType());
        }
    }
}
