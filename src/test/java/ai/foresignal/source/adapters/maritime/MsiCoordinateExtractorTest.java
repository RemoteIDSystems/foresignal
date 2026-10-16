package ai.foresignal.source.adapters.maritime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.adapters.maritime.MsiCoordinateExtractor.Result;
import ai.foresignal.source.adapters.maritime.MsiCoordinateExtractor.Rule;
import ai.foresignal.source.spi.Geo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * {@link MsiCoordinateExtractor} (prompt 088): both position formats with and without the space, hemisphere signs,
 * range checks (minutes and seconds below 60, degrees at most 90/180), and each geometry rule in order, including the
 * texts of the live NGA list ("IN AREA BOUND BY", "WITHIN THREE MILES OF", lettered areas and tracklines).
 */
class MsiCoordinateExtractorTest {

    @Test
    @DisplayName("DD-MM.mmN DDD-MM.mmW, with and without the space")
    void decimalMinutes() {
        Result spaced = MsiCoordinateExtractor.extract("DERELICT IN 19-23.0N 092-03.1W.");
        assertThat(spaced.positions()).hasSize(1);
        assertThat(spaced.positions().getFirst().y).isCloseTo(19 + 23.0 / 60, within(1e-6));
        assertThat(spaced.positions().getFirst().x).isCloseTo(-(92 + 3.1 / 60), within(1e-6));
        Result tight = MsiCoordinateExtractor.extract("DERELICT IN 19-23.0N092-03.1W.");
        assertThat(tight.positions()).containsExactlyElementsOf(spaced.positions());
    }

    @Test
    @DisplayName("DD-MM-SS.sN DDD-MM-SS.sW, with and without the space, and southern and eastern hemispheres")
    void degreesMinutesSeconds() {
        Result r = MsiCoordinateExtractor.extract("ILE DE LA TORTUE W. POINT LIGHT\n20-03-44N 072-58-02W UNLIT.");
        assertThat(r.rule()).isEqualTo(Rule.POINTS);
        assertThat(r.geometry()).isInstanceOf(Point.class);
        assertThat(r.positions().getFirst().y).isCloseTo(20 + 3 / 60.0 + 44 / 3600.0, within(1e-6));
        assertThat(r.positions().getFirst().x).isCloseTo(-(72 + 58 / 60.0 + 2 / 3600.0), within(1e-6));
        Result tight = MsiCoordinateExtractor.extract("34-10-30.5S119-20-15.0E");
        assertThat(tight.positions()).hasSize(1);
        assertThat(tight.positions().getFirst().y).isCloseTo(-(34 + 10 / 60.0 + 30.5 / 3600.0), within(1e-6));
        assertThat(tight.positions().getFirst().x).isCloseTo(119 + 20 / 60.0 + 15.0 / 3600.0, within(1e-6));
    }

    @Test
    @DisplayName("invalid minutes, seconds and degrees are dropped, never guessed")
    void invalidRejected() {
        assertThat(MsiCoordinateExtractor.extract("12-75.0N 045-10.0W").rule()).isEqualTo(Rule.NONE);
        assertThat(MsiCoordinateExtractor.extract("12-10.0N 045-60.0W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("12-10-61N 045-10-00W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("91-00.0N 045-10.0W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("10-00.0N 181-00.0W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("90-30.0N 045-10.0W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("12-10.5-30N 045-10-00W").positions()).as("seconds need whole"
                + " minutes").isEmpty();
        assertThat(MsiCoordinateExtractor.extract("12-10.0X 045-10.0W").positions()).isEmpty();
        assertThat(MsiCoordinateExtractor.extract("90-00.0S 180-00.0E").positions()).hasSize(1);
    }

    @Test
    @DisplayName("rule 1: AREA BOUND BY with at least 3 positions is a closed polygon")
    void polygon() {
        Result r = MsiCoordinateExtractor.extract("HAZARDOUS OPERATIONS IN PROGRESS UNTIL\nFURTHER NOTICE IN AREA BOUND BY\n"
                + "13-05.00N 066-43.00W, 10-59.10N 066-43.00W,\n10-59.10N 064-14.00W, 13-05.00N 064-14.00W.");
        assertThat(r.rule()).isEqualTo(Rule.POLYGON);
        Polygon p = (Polygon) r.geometry();
        assertThat(p.getExteriorRing().getNumPoints()).isEqualTo(5);
        assertThat(p.getExteriorRing().isClosed()).isTrue();
        assertThat(p.getSRID()).isEqualTo(Geo.factory().getSRID());
        assertThat(MsiCoordinateExtractor.extract("AREA BOUNDED BY 10-00.0N 060-00.0W, 11-00.0N 060-00.0W, 11-00.0N"
                + " 061-00.0W.").rule()).isEqualTo(Rule.POLYGON);
        assertThat(MsiCoordinateExtractor.extract("IN AREAS BOUNDED BY 10-00.0N 060-00.0W, 11-00.0N 060-00.0W,"
                + " 11-00.0N 061-00.0W, 10-00.0N 060-00.0W.").geometry().getCoordinates()).hasSize(4);
    }

    @Test
    @DisplayName("rule 1: lettered areas with 3 or more positions each are a MultiPolygon")
    void multiPolygon() {
        Result r = MsiCoordinateExtractor.extract("UNDERWATER OPERATIONS IN AREAS BOUND BY\n"
                + "A. 08-00.66N 056-44.58W, 08-00.66N 056-40.12W,\n   07-54.87N 056-40.12W, 07-54.87N 056-44.58W.\n"
                + "B. 08-15.96N 057-02.23W, 08-15.96N 056-54.25W,\n   07-58.60N 056-54.25W, 07-58.60N 057-02.23W.\n"
                + "WIDE BERTH REQUESTED.");
        assertThat(r.rule()).isEqualTo(Rule.POLYGON);
        assertThat(r.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(r.geometry().getNumGeometries()).isEqualTo(2);
    }

    @Test
    @DisplayName("rule 1 needs 3 positions: with 2 the next rule applies")
    void polygonNeedsThree() {
        Result r = MsiCoordinateExtractor.extract("AREA BOUND BY 10-00.0N 060-00.0W, 11-00.0N 060-00.0W.");
        assertThat(r.rule()).isEqualTo(Rule.POINTS);
        assertThat(r.geometry()).isInstanceOf(MultiPoint.class);
    }

    @Test
    @DisplayName("rule 2: WITHIN n MILES/NM/METERS OF a position is a 64-vertex geodesic circle; words count too")
    void circle() {
        Result r = MsiCoordinateExtractor.extract("HAZARDOUS OPERATIONS IN AREA WITHIN 2 NM OF\n36-50.0N 076-17.5W.");
        assertThat(r.rule()).isEqualTo(Rule.CIRCLE);
        Polygon circle = (Polygon) r.geometry();
        assertThat(circle.getExteriorRing().getNumPoints()).isEqualTo(MsiCoordinateExtractor.CIRCLE_VERTICES + 1);
        Coordinate center = new Coordinate(-(76 + 17.5 / 60), 36 + 50.0 / 60);
        for (Coordinate c : circle.getExteriorRing().getCoordinates()) {
            assertThat(Geo.haversineKm(center.y, center.x, c.y, c.x)).isCloseTo(2 * 1.852, within(0.01));
        }
        Result words = MsiCoordinateExtractor.extract("MINES REPORTED IN AREA WITHIN FIVE MILES\nOF 45-07.10N 030-09.70E.");
        assertThat(words.rule()).isEqualTo(Rule.CIRCLE);
        Coordinate first = words.geometry().getCoordinates()[0];
        assertThat(Geo.haversineKm(45 + 7.1 / 60, 30 + 9.7 / 60, first.y, first.x)).isCloseTo(5 * 1.852, within(0.01));
        Result meters = MsiCoordinateExtractor.extract("DIVING WITHIN 500 METERS OF 25-46.12N 080-08.50W.");
        Coordinate m = meters.geometry().getCoordinates()[0];
        assertThat(Geo.haversineKm(25 + 46.12 / 60, -(80 + 8.5 / 60), m.y, m.x)).isCloseTo(0.5, within(0.002));
    }

    @Test
    @DisplayName("rule 2 needs the position right after OF: WITHIN … OF TRACKLINE is a trackline")
    void withinTrackline() {
        Result r = MsiCoordinateExtractor.extract("CABLE OPERATIONS WITHIN THREE MILES OF TRACKLINE JOINING\n"
                + "03-00.70N 006-57.40E, 03-35.20N 007-13.90E, 03-48.50N 007-33.10E.");
        assertThat(r.rule()).isEqualTo(Rule.TRACKLINE);
        assertThat(r.geometry()).isInstanceOf(LineString.class);
        assertThat(r.geometry().getCoordinates()).hasSize(3);
    }

    @Test
    @DisplayName("rule 3: BETWEEN … AND … is a line, and lettered tracklines a MultiLineString")
    void trackline() {
        Result between = MsiCoordinateExtractor.extract("SURVEY OPERATIONS BETWEEN 40-30.0N 073-50.0W\nAND 40-25.0N"
                + " 073-40.0W.");
        assertThat(between.rule()).isEqualTo(Rule.TRACKLINE);
        assertThat(between.geometry().getCoordinates()).hasSize(2);
        Result lettered = MsiCoordinateExtractor.extract("CABLE OPERATIONS ALONG TRACKLINES JOINING:\n"
                + "A. 13-10.34N 069-51.47W, 12-55.78N 069-48.86W,\n   12-43.47N 069-46.66W.\n"
                + "B. 13-47.75N 069-55.00W, 13-30.00N 069-55.00W.\n");
        assertThat(lettered.geometry()).isInstanceOf(MultiLineString.class);
        assertThat(lettered.geometry().getNumGeometries()).isEqualTo(2);
        Result collision = MsiCoordinateExtractor.extract("COLLISION BETWEEN 2 VESSELS IN 36-27.00N 015-15.00E.");
        assertThat(collision.rule()).as("BETWEEN without two positions").isEqualTo(Rule.POINTS);
    }

    @Test
    @DisplayName("rule 4: several positions are a MultiPoint, one a Point; rule 5: none is no geometry")
    void pointsAndNone() {
        Result modu = MsiCoordinateExtractor.extract("MOBILE OFFSHORE DRILLING UNITS\nA. AGOSTO 12 19-23.0N 092-03.1W.\n"
                + "B. ARGOS 27-10.4N 090-22.1W.\nC. ATLANTIS 19-26.5N 092-01.9W.");
        assertThat(modu.rule()).isEqualTo(Rule.POINTS);
        assertThat(modu.geometry()).isInstanceOf(MultiPoint.class);
        assertThat(modu.geometry().getNumGeometries()).isEqualTo(3);
        Result none = MsiCoordinateExtractor.extract("1. NAVAREA IV WARNINGS IN FORCE AS OF 031554Z MAY.\n"
                + "496/2024, 495/2024.");
        assertThat(none.rule()).isEqualTo(Rule.NONE);
        assertThat(none.geometry()).isNull();
        assertThat(MsiCoordinateExtractor.extract(null).rule()).isEqualTo(Rule.NONE);
    }

    @Test
    @DisplayName("antimeridian: a circle across 180° is a valid MultiPolygon inside ±180°, one part per side")
    void circleAcrossAntimeridian() {
        Result r = MsiCoordinateExtractor.extract("HAZARDOUS OPERATIONS WITHIN 20 MILES OF 51-50.0N 179-55.0E.");
        assertThat(r.rule()).isEqualTo(Rule.CIRCLE);
        assertThat(r.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(r.geometry().getNumGeometries()).isEqualTo(2);
        assertThat(r.geometry().isValid()).isTrue();
        assertInRange(r.geometry());
        Envelope east = r.geometry().getGeometryN(0).getEnvelopeInternal();
        Envelope west = r.geometry().getGeometryN(1).getEnvelopeInternal();
        assertThat(Math.max(east.getMaxX(), west.getMaxX())).isEqualTo(180.0);
        assertThat(Math.min(east.getMinX(), west.getMinX())).isEqualTo(-180.0);
        assertThat(r.geometry().getEnvelopeInternal().getWidth()).as("both sides of the line").isGreaterThan(359);
        for (Coordinate c : r.geometry().getCoordinates()) {
            if (Math.abs(c.x) < 179.999) {
                double lon = c.x < 0 ? c.x + 360 : c.x;
                assertThat(Geo.haversineKm(51 + 50.0 / 60, 179 + 55.0 / 60, c.y, lon)).isCloseTo(20 * 1.852,
                        within(0.01));
            }
        }
    }

    @Test
    @DisplayName("antimeridian: a circle near but not across 180° stays one Polygon")
    void circleNearAntimeridian() {
        Result r = MsiCoordinateExtractor.extract("DIVING WITHIN 2 NM OF 51-50.0N 179-00.0E.");
        assertThat(r.geometry()).isInstanceOf(Polygon.class);
        assertInRange(r.geometry());
    }

    @Test
    @DisplayName("antimeridian: an area bound by positions on both sides of 180° is split, not 358° wide")
    void polygonAcrossAntimeridian() {
        Result r = MsiCoordinateExtractor.extract("MISSILE TESTING IN AREA BOUND BY\n"
                + "52-00.0N 179-00.0E, 52-00.0N 179-00.0W,\n51-00.0N 179-00.0W, 51-00.0N 179-00.0E.");
        assertThat(r.rule()).isEqualTo(Rule.POLYGON);
        assertThat(r.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(r.geometry().getNumGeometries()).isEqualTo(2);
        assertThat(r.geometry().isValid()).isTrue();
        assertInRange(r.geometry());
        for (int i = 0; i < 2; i++) {
            assertThat(r.geometry().getGeometryN(i).getEnvelopeInternal().getWidth()).isCloseTo(1.0, within(1e-6));
        }
        assertThat(r.geometry().getArea()).as("two 1x1 degree halves").isCloseTo(2.0, within(1e-6));
    }

    @Test
    @DisplayName("antimeridian: lettered areas keep one part each side and are still a MultiPolygon")
    void letteredAcrossAntimeridian() {
        Result r = MsiCoordinateExtractor.extract("HAZARDOUS OPERATIONS IN AREAS BOUND BY\n"
                + "A. 52-00.0N 179-30.0E, 52-00.0N 179-30.0W, 51-00.0N 179-30.0W.\n"
                + "B. 40-00.0N 170-00.0E, 40-00.0N 171-00.0E, 39-00.0N 171-00.0E.\n");
        assertThat(r.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(r.geometry().getNumGeometries()).isEqualTo(3);
        assertInRange(r.geometry());
    }

    @Test
    @DisplayName("antimeridian: a trackline from 179E to 179W is a MultiLineString of two short pieces")
    void tracklineAcrossAntimeridian() {
        Result r = MsiCoordinateExtractor.extract("CABLE OPERATIONS ALONG TRACKLINE JOINING\n"
                + "51-00.0N 179-00.0E, 51-30.0N 179-00.0W.");
        assertThat(r.rule()).isEqualTo(Rule.TRACKLINE);
        assertThat(r.geometry()).isInstanceOf(MultiLineString.class);
        assertThat(r.geometry().getNumGeometries()).isEqualTo(2);
        assertInRange(r.geometry());
        assertThat(r.geometry().getLength()).as("about 2.06 degrees, not 358").isLessThan(3.0);
    }

    @Test
    @DisplayName("a circle enclosing the North Pole is a valid polar cap inside ±180°")
    void polarCircle() {
        Result r = MsiCoordinateExtractor.extract("ICE OPERATIONS WITHIN 30 MILES OF 89-50.0N 045-00.0E.");
        assertThat(r.rule()).isEqualTo(Rule.CIRCLE);
        assertThat(r.geometry()).isInstanceOf(Polygon.class);
        assertThat(r.geometry().isValid()).isTrue();
        assertInRange(r.geometry());
        assertThat(r.geometry().getEnvelopeInternal().getMaxY()).isEqualTo(90.0);
    }

    private static void assertInRange(org.locationtech.jts.geom.Geometry geometry) {
        for (Coordinate c : geometry.getCoordinates()) {
            assertThat(c.x).isBetween(-180.0, 180.0);
            assertThat(c.y).isBetween(-90.0, 90.0);
        }
    }

    @Test
    @DisplayName("units: miles and cables are nautical; kilometers, yards and meters convert")
    void units() {
        assertThat(MsiCoordinateExtractor.meters(1, "MILES")).isEqualTo(1852.0);
        assertThat(MsiCoordinateExtractor.meters(1, "NAUTICAL MILES")).isEqualTo(1852.0);
        assertThat(MsiCoordinateExtractor.meters(2, "KM")).isEqualTo(2000.0);
        assertThat(MsiCoordinateExtractor.meters(10, "CABLES")).isEqualTo(1852.0);
        assertThat(MsiCoordinateExtractor.meters(100, "YARDS")).isCloseTo(91.44, within(1e-9));
        assertThat(MsiCoordinateExtractor.meters(200, "METERS")).isEqualTo(200.0);
        assertThat(MsiCoordinateExtractor.amount("THREE")).isEqualTo(3.0);
        assertThat(MsiCoordinateExtractor.amount("2.5")).isEqualTo(2.5);
        assertThat(MsiCoordinateExtractor.amount("SEVERAL")).isNull();
    }
}
