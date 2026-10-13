package ai.foresignal.source.kit.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.spi.UpstreamFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link FeatureGeometry} (prompt 073): a geometry over 9,500 vertices is simplified at doubling tolerances until it
 * fits, stays valid, keeps its hole, and the record says so; smaller geometries are untouched.
 */
class FeatureGeometryTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** A polygon ring of {@code n} vertices around (lon, lat) with radius r degrees, with a jagged edge. */
    static String ring(int n, double lon, double lat, double r, boolean clockwise) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i <= n; i++) {
            int k = i % n;
            double a = 2 * Math.PI * k / n * (clockwise ? -1 : 1);
            double radius = r * (1 + 0.002 * Math.sin(k * 7.0));
            if (i > 0) {
                out.append(',');
            }
            out.append('[').append(lon + radius * Math.cos(a)).append(',').append(lat + radius * Math.sin(a)).append(']');
        }
        return out.append(']').toString();
    }

    @Test
    @DisplayName("a 20,000-vertex polygon with a hole is simplified below 9,500 vertices, valid, with the flags")
    void simplifies() {
        JsonNode polygon = JSON.readTree("{\"type\": \"Polygon\", \"coordinates\": [" + ring(20_000, -120, 40, 0.5, false)
                + "," + ring(2_000, -120, 40, 0.1, true) + "]}");
        FeatureGeometry.Result result = FeatureGeometry.fromGeoJson(polygon);
        assertThat(result.simplified()).isTrue();
        assertThat(result.toleranceDeg()).isGreaterThanOrEqualTo(FeatureGeometry.START_TOLERANCE_DEG);
        Geometry g = result.geometry();
        assertThat(g.getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
        assertThat(g.isValid()).isTrue();
        assertThat(g.getSRID()).isEqualTo(4326);
        assertThat(((org.locationtech.jts.geom.Polygon) g).getNumInteriorRing()).isEqualTo(1);
        // The tolerance doubles from 0.0005°: it is 0.0005 · 2^k.
        double steps = Math.log(result.toleranceDeg() / FeatureGeometry.START_TOLERANCE_DEG) / Math.log(2);
        assertThat(steps).isCloseTo(Math.rint(steps), org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("the mapper puts geometrySimplified and simplifyToleranceDeg in the payload")
    void flagsInPayload() {
        JsonNode feature = JSON.readTree("{\"type\": \"Feature\", \"properties\": {\"IrwinID\": \"x\","
                + " \"ModifiedOnDateTime_dt\": 1791913696835}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": ["
                + ring(12_000, 10, 10, 1, false) + "]}}");
        FeatureMapper.Mapped m = FeatureMapper.map(KitTestSpecs.arcgis("https://a.example/0"), feature);
        assertThat(m.accepted()).isTrue();
        assertThat(m.record().payload()).containsEntry(FeatureGeometry.SIMPLIFIED_KEY, true)
                .containsKey(FeatureGeometry.TOLERANCE_KEY);
        assertThat(m.record().geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
    }

    @Test
    @DisplayName("a small geometry is untouched; null and missing geometries are none; malformed ones throw")
    void untouched() {
        FeatureGeometry.Result small = FeatureGeometry.fromGeoJson(JSON.readTree("{\"type\": \"Polygon\","
                + " \"coordinates\": [" + ring(100, 0, 0, 1, false) + "]}"));
        assertThat(small.simplified()).isFalse();
        assertThat(small.toleranceDeg()).isNull();
        assertThat(small.geometry().getNumPoints()).isEqualTo(101);
        assertThat(FeatureGeometry.fromGeoJson(null)).isEqualTo(FeatureGeometry.Result.NONE);
        assertThat(FeatureGeometry.fromGeoJson(JSON.readTree("null"))).isEqualTo(FeatureGeometry.Result.NONE);
        assertThatThrownBy(() -> FeatureGeometry.fromGeoJson(JSON.readTree("{\"type\": \"Polygon\", \"coordinates\":"
                + " [[[0, 0], [1, 1]]]}"))).isInstanceOf(UpstreamFormatException.class);
    }
}
