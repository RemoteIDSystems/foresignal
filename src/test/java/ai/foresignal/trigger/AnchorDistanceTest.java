package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/** {@link AnchorDistance}, {@link NearAnchor} and the degree prefilter of {@link RuleSqlBuilder} (prompt 036). */
class AnchorDistanceTest {

    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    static Point point(double lon, double lat) {
        return WGS84.createPoint(new Coordinate(lon, lat));
    }

    static Polygon square(double x0, double y0, double x1, double y1) {
        return WGS84.createPolygon(new Coordinate[] {new Coordinate(x0, y0), new Coordinate(x1, y0), new Coordinate(x1, y1),
            new Coordinate(x0, y1), new Coordinate(x0, y0)});
    }

    @Test
    @DisplayName("haversine: one degree of latitude is about 111.2 km")
    void haversine() {
        assertThat(AnchorDistance.haversine(0, 0, 1, 0)).isCloseTo(111_195, offset(50.0));
        assertThat(AnchorDistance.distanceMeters(point(0, 0), point(0.1, 0))).isCloseTo(11_119.5, offset(5.0));
    }

    @Test
    @DisplayName("near: inside the radius, outside it, and ambiguous within the band; radius 0 means inside the polygon")
    void near() {
        NearAnchor fifty = new NearAnchor("phone", "p", 50, null, 200);
        assertThat(AnchorDistance.near(fifty, point(0, 0), point(0.1, 0))).isEqualTo(Evaluation.MATCH);
        assertThat(AnchorDistance.near(fifty, point(0, 0), point(1, 0))).isEqualTo(Evaluation.NO_MATCH);
        assertThat(AnchorDistance.near(fifty, point(0, 0), point(0.4497, 0))).isEqualTo(Evaluation.AMBIGUOUS);
        assertThat(AnchorDistance.near(fifty, point(0, 0), null)).isEqualTo(Evaluation.NO_MATCH);
        assertThat(AnchorDistance.near(fifty, point(0, 0), square(0.2, -0.1, 0.3, 0.1))).isEqualTo(Evaluation.MATCH);
        assertThat(AnchorDistance.near(fifty, point(0, 0), square(2, 2, 3, 3))).isEqualTo(Evaluation.NO_MATCH);
        NearAnchor inside = new NearAnchor("phone", "p", 0, null, 200);
        assertThat(AnchorDistance.near(inside, point(0, 0), square(-1, -1, 1, 1))).isEqualTo(Evaluation.MATCH);
        assertThat(AnchorDistance.near(inside, point(5, 5), square(-1, -1, 1, 1))).isEqualTo(Evaluation.NO_MATCH);
        assertThat(AnchorDistance.distanceMeters(point(0, 0), square(-1, -1, 1, 1))).isZero();
        assertThat(AnchorDistance.distanceMeters(point(179.9, 0), square(-179.9, -1, -179.8, 1)))
                .isCloseTo(22_239, offset(200.0));
    }

    @Test
    @DisplayName("NearAnchor: defaults, the stored form and back, keys")
    void nearAnchor() {
        NearAnchor anchor = new NearAnchor("phone", "pixel", 150, null, 500);
        assertThat(anchor.maxAnchorAge()).isEqualTo(Duration.ofMinutes(30));
        assertThat(anchor.radiusMeters()).isEqualTo(150_000);
        assertThat(anchor.inside()).isFalse();
        Map<String, Object> stored = anchor.toMap();
        assertThat(NearAnchor.fromMap(stored)).isEqualTo(anchor);
        assertThat(NearAnchor.fromMap(Map.of("placeIds", java.util.List.of("home")))).isNull();
        assertThat(NearAnchor.fromMap(null)).isNull();
        assertThat(NearAnchor.fromMap(Map.of("anchor", Map.of("source", "s", "externalId", "e"), "maxAnchorAge", "bad"))
                .maxAnchorAge()).isEqualTo(NearAnchor.DEFAULT_MAX_AGE);
        assertThat(anchor.key()).isEqualTo(NearAnchor.key("phone", "pixel"));
    }

    @Test
    @DisplayName("the degree prefilter covers the radius at every latitude and is clamped")
    void expandDegrees() {
        double[] equator = RuleSqlBuilder.expandDegrees(0, 111_320);
        assertThat(equator[1]).isGreaterThanOrEqualTo(1.0);
        assertThat(equator[0]).isGreaterThanOrEqualTo(1.0);
        double[] north = RuleSqlBuilder.expandDegrees(60, 111_320);
        assertThat(north[0]).isGreaterThan(1.9);
        double[] pole = RuleSqlBuilder.expandDegrees(89.99, 1_000_000);
        assertThat(pole[0]).isLessThanOrEqualTo(360);
    }
}
