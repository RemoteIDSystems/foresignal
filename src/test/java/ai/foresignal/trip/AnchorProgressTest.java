package ai.foresignal.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.kinematics.GeoMath;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * Anchor progress along a trip route (prompt 114): positions along the route in meters, hazards behind the anchor
 * excluded, the route ETA at the anchor's speed, and the antimeridian split of long routes.
 */
class AnchorProgressTest {

    /** A 10 km route due east along the equator. */
    static LineString route() {
        double end = 10_000 / GeoMath.METERS_PER_DEGREE_LON;
        return RouteMeasure.FACTORY.createLineString(new Coordinate[] {new Coordinate(0, 0),
                new Coordinate(end / 2, 0), new Coordinate(end, 0)});
    }

    static TripHazards.Hazard hazard(String id, double along) {
        return new TripHazards.Hazard("src", id, "road-event", id, "warning", "on-route", along, 0, null, null, null,
                null);
    }

    @Test
    @DisplayName("a point projects to its distance along the route and its offset from it")
    void locate() {
        RouteMeasure m = new RouteMeasure(route());
        assertThat(m.length()).isCloseTo(9_989, within(5.0));
        double lon = 7_000 / GeoMath.METERS_PER_DEGREE_LON;
        double lat = 300 / GeoMath.METERS_PER_DEGREE_LAT;
        RouteMeasure.Located at = m.locate(lon, lat);
        assertThat(at.alongMeters()).isCloseTo(6_993, within(10.0));
        assertThat(at.offsetMeters()).isCloseTo(300, within(2.0));
        assertThat(m.locate(-0.01, 0).alongMeters()).as("before the start clamps to 0").isZero();
    }

    @Test
    @DisplayName("a polygon hazard is located by its point nearest the route")
    void polygon() {
        RouteMeasure m = new RouteMeasure(route());
        double x = 5_000 / GeoMath.METERS_PER_DEGREE_LON;
        double y0 = 500 / GeoMath.METERS_PER_DEGREE_LAT;
        double y1 = 900 / GeoMath.METERS_PER_DEGREE_LAT;
        double w = 200 / GeoMath.METERS_PER_DEGREE_LON;
        Geometry box = RouteMeasure.FACTORY.createPolygon(new Coordinate[] {new Coordinate(x - w, y0),
                new Coordinate(x + w, y0), new Coordinate(x + w, y1), new Coordinate(x - w, y1), new Coordinate(x - w, y0)});
        RouteMeasure.Located at = m.locate(box);
        assertThat(at.offsetMeters()).isCloseTo(500, within(5.0));
        assertThat(at.alongMeters()).isBetween(4_750.0, 5_250.0);
    }

    @Test
    @DisplayName("hazards behind the anchor are excluded; the rest are ahead with the route ETA")
    void ahead() {
        List<TripHazards.Hazard> hazards = List.of(hazard("behind", 1_000), hazard("near", 4_000), hazard("far", 9_000));
        List<TripHazards.Hazard> ahead = TripHazards.ahead(hazards, 3_000, 10.0);
        assertThat(ahead).extracting(TripHazards.Hazard::externalId).containsExactly("near", "far");
        assertThat(ahead.get(0).ahead()).isTrue();
        assertThat(ahead.get(0).etaSeconds()).isCloseTo(100, within(1e-9));
        assertThat(ahead.get(1).etaSeconds()).isCloseTo(600, within(1e-9));
    }

    @Test
    @DisplayName("without a usable speed the hazards ahead carry no ETA")
    void noSpeed() {
        List<TripHazards.Hazard> ahead = TripHazards.ahead(List.of(hazard("near", 4_000)), 3_000, null);
        assertThat(ahead).singleElement().satisfies(h -> assertThat(h.etaSeconds()).isNull());
        assertThat(TripHazards.ahead(List.of(hazard("near", 4_000)), 3_000, 0.2).getFirst().etaSeconds()).isNull();
    }

    @Test
    @DisplayName("the hazard object carries the record ref, section and rounded distances")
    void hazardMap() {
        var map = new TripHazards.Hazard("nws-alerts", "urn:oid:2.49 x", "nws-alert", "Flood", "warning", "timing", 1234.6,
                55.4, Instant.parse("2026-10-16T13:00:00Z"), null, true, 61.7).toMap();
        assertThat(map.get("ref")).isEqualTo("record:nws-alerts/urn%3Aoid%3A2.49%20x");
        assertThat(map.get("distanceAlongRouteM")).isEqualTo(1235L);
        assertThat(map.get("section")).isEqualTo("timing");
        assertThat(map.get("etaSeconds")).isEqualTo(62.0);
    }

    @Test
    @DisplayName("a route crossing the antimeridian is split into two parts at ±180°")
    void antimeridian() {
        LineString line = RouteMeasure.FACTORY.createLineString(new Coordinate[] {new Coordinate(179.5, 10),
                new Coordinate(-179.5, 12)});
        Geometry split = RouteMeasure.splitAntimeridian(line);
        assertThat(split).isInstanceOf(MultiLineString.class);
        assertThat(split.getNumGeometries()).isEqualTo(2);
        assertThat(split.getGeometryN(0).getCoordinates()[1].x).isEqualTo(180.0);
        assertThat(split.getGeometryN(0).getCoordinates()[1].y).isCloseTo(11, within(1e-9));
        assertThat(split.getGeometryN(1).getCoordinates()[0].x).isEqualTo(-180.0);
        assertThat(RouteMeasure.splitAntimeridian(route())).isInstanceOf(LineString.class);
        assertThat(new RouteMeasure(split).length()).isCloseTo(GeoMath.distanceMeters(179.5, 10, -179.5, 12), within(500.0));
    }

    @Test
    @DisplayName("severity ranks and the hazard severity mapping")
    void severity() {
        assertThat(TripHazards.rank("critical")).isGreaterThan(TripHazards.rank("warning"));
        assertThat(TripHazards.rank("bogus")).isEqualTo(-1);
        assertThat(TripHazards.severity("nws-alert", java.util.Map.of("severity", "Extreme"))).isEqualTo("critical");
        assertThat(TripHazards.severity("nws-alert", java.util.Map.of("severity", "Moderate"))).isEqualTo("warning");
        assertThat(TripHazards.severity("earthquake", java.util.Map.of("mag", 5.4))).isEqualTo("warning");
        assertThat(TripHazards.severity("incident", java.util.Map.of())).isNull();
    }
}
