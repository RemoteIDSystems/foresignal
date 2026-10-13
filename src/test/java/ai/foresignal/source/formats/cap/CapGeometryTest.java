package ai.foresignal.source.formats.cap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/** {@link CapGeometry} (prompt 067). */
class CapGeometryTest {

    /** Kilometers per degree of latitude on the sphere {@link CapGeometry} uses. */
    private static final double KM_PER_DEGREE = Math.PI * CapGeometry.EARTH_RADIUS_KM / 180;

    @Test
    @DisplayName("lat,lon pairs are swapped to lon/lat")
    void swap() {
        Polygon polygon = CapGeometry.polygon("38.47,-120.14 38.34,-119.95 38.52,-119.74 38.47,-120.14");

        Coordinate first = polygon.getCoordinates()[0];
        assertThat(first.x).isEqualTo(-120.14);
        assertThat(first.y).isEqualTo(38.47);
        assertThat(polygon.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("an open ring is closed")
    void ringClosure() {
        Polygon polygon = CapGeometry.polygon("31.00,-98.00 31.00,-97.90 31.10,-97.90");

        Coordinate[] ring = polygon.getExteriorRing().getCoordinates();
        assertThat(ring).hasSize(4);
        assertThat(ring[3].equals2D(ring[0])).isTrue();
    }

    @Test
    @DisplayName("the polygons of an info block become one MultiPolygon")
    void multiPolygon() {
        CapAlert alert = CapParser.parse(AdapterFixtures.bytes("cap", "polygon.xml"));

        Geometry geometry = CapGeometry.of(alert.firstInfo());

        assertThat(geometry).isInstanceOf(MultiPolygon.class);
        assertThat(geometry.getNumGeometries()).isEqualTo(2);
        assertThat(geometry.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a 10 km circle is a closed 64-segment polygon whose area is within 1% of pi r squared")
    void circleArea() {
        Polygon polygon = (Polygon) CapGeometry.circle("32.9525,-115.5527 10");

        assertThat(polygon.getExteriorRing().getNumPoints()).isEqualTo(CapGeometry.CIRCLE_SEGMENTS + 1);
        assertThat(polygon.getCentroid().getX()).isCloseTo(-115.5527, offset(0.01));
        assertThat(polygon.getCentroid().getY()).isCloseTo(32.9525, offset(0.01));
        // Local equirectangular area: square degrees times km per degree of latitude and of longitude.
        double km2 = polygon.getArea() * KM_PER_DEGREE * KM_PER_DEGREE * Math.cos(Math.toRadians(32.9525));
        double expected = Math.PI * 10 * 10;
        assertThat(km2).isCloseTo(expected, offset(expected * 0.01));
    }

    @Test
    @DisplayName("radius 0 is a Point; zero-radius circles alone make a Point or MultiPoint; polygons win")
    void zeroRadius() {
        Geometry point = CapGeometry.circle("-0.2,-19.2 0.0");
        assertThat(point).isInstanceOf(Point.class);
        assertThat(((Point) point).getX()).isEqualTo(-19.2);
        assertThat(((Point) point).getY()).isEqualTo(-0.2);

        CapAlert.Area one = new CapAlert.Area("a", List.of(), List.of("1,2 0"), List.of(), null, null);
        CapAlert.Area two = new CapAlert.Area("b", List.of(), List.of("3,4 0"), List.of(), null, null);
        assertThat(CapGeometry.of(List.of(one))).isInstanceOf(Point.class);
        assertThat(CapGeometry.of(List.of(one, two))).isInstanceOf(MultiPoint.class);

        CapAlert circle = CapParser.parse(AdapterFixtures.bytes("cap", "circle.xml"));
        Geometry geometry = CapGeometry.of(circle.firstInfo());
        assertThat(geometry).as("the 10 km circle wins over the zero-radius one").isInstanceOf(MultiPolygon.class);
        assertThat(geometry.getNumGeometries()).isEqualTo(1);
    }

    @Test
    @DisplayName("geocode-only areas and missing info have no geometry")
    void geocodeOnly() {
        CapAlert alert = CapParser.parse(AdapterFixtures.bytes("cap", "geocode-only.xml"));

        assertThat(CapGeometry.of(alert.firstInfo())).isNull();
        assertThat(CapGeometry.of((CapAlert.Info) null)).isNull();
    }

    @Test
    @DisplayName("malformed pairs, out-of-range coordinates, too few points and bad radii are format errors")
    void malformed() {
        assertThatThrownBy(() -> CapGeometry.polygon("1,2 3,4")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapGeometry.polygon("1;2 3,4 5,6")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapGeometry.polygon("91,0 1,1 2,2")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapGeometry.circle("1,2")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapGeometry.circle("1,2 -5")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapGeometry.circle("1,2 x")).isInstanceOf(UpstreamFormatException.class);
    }
}
