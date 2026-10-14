package ai.foresignal.source.adapters.faa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * {@link XnotamParser} against captured tfr.faa.gov details (2026-10-14) and two hand-made documents (prompt 087):
 * polygons, circle and arc densification, several areas as a MultiPolygon, coordinate formats, vertical limits, times
 * that are UTC whatever {@code codeTimeZone} says, and a DOCTYPE refused.
 */
class XnotamParserTest {

    private static XnotamParser.Tfr parse(String fixture) {
        return XnotamParser.parse(AdapterFixtures.bytes("faa-tfr", fixture));
    }

    @Test
    @DisplayName("a polygon TFR: one merged area, vertical limits, times, text and location")
    void polygon() {
        XnotamParser.Tfr tfr = parse("detail_6_9375.xml");

        assertThat(tfr.geometry()).isInstanceOf(Polygon.class);
        assertThat(tfr.areas()).isEqualTo(1);
        Polygon polygon = (Polygon) tfr.geometry();
        assertThat(polygon.getExteriorRing().getNumPoints()).isEqualTo(5);
        Coordinate first = polygon.getCoordinates()[0];
        assertThat(first.y).isCloseTo(44.06666667, within(1e-9));
        assertThat(first.x).isCloseTo(-118.81666667, within(1e-9));
        assertThat(tfr.lowerFt()).isZero();
        assertThat(tfr.upperFt()).isEqualTo(6100);
        assertThat(tfr.lowerRef()).isEqualTo("ALT");
        assertThat(tfr.upperRef()).isEqualTo("ALT");
        assertThat(tfr.issued()).isEqualTo(Instant.parse("2026-10-14T01:12:41Z"));
        assertThat(tfr.effective()).isEqualTo(Instant.parse("2026-10-14T15:00:00Z"));
        assertThat(tfr.expires()).isEqualTo(Instant.parse("2026-10-19T03:00:00Z"));
        assertThat(tfr.regulation()).isEqualTo("91.137(a)(1)");
        assertThat(tfr.city()).isEqualTo("11NM SE SENECA");
        assertThat(tfr.stateName()).isEqualTo("OREGON");
        assertThat(tfr.facility()).isEqualTo("ZLC");
        assertThat(tfr.description()).startsWith("!FDC 6/9375 ZLC OR..AIRSPACE 11NM SE SENECA");
        assertThat(tfr.notamId()).isEqualTo("6/9375");
    }

    @Test
    @DisplayName("a circle TFR the FAA merged into GRC vertices; no effective or expiry date means until further notice")
    void mergedCircleWithoutExpiry() {
        XnotamParser.Tfr tfr = parse("detail_4_3634.xml");

        assertThat(tfr.geometry()).isInstanceOf(Polygon.class);
        assertThat(((Polygon) tfr.geometry()).getExteriorRing().getNumPoints()).isEqualTo(37);
        assertThat(tfr.effective()).isNull();
        assertThat(tfr.expires()).isNull();
        assertThat(tfr.lowerRef()).isEqualTo("HEI");
        assertThat(tfr.upperFt()).isEqualTo(3000);
        assertThat(tfr.regulation()).isEqualTo("99.7");
    }

    @Test
    @DisplayName("three areas become a MultiPolygon; an EDT TFR's times are read as UTC, as its text says")
    void multiAreaAndTimeZone() {
        XnotamParser.Tfr tfr = parse("detail_6_8928.xml");

        assertThat(tfr.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(tfr.geometry().getNumGeometries()).isEqualTo(3);
        assertThat(tfr.areas()).isEqualTo(3);
        assertThat(tfr.timeZone()).isEqualTo("EDT");
        assertThat(tfr.effective()).isEqualTo(Instant.parse("2026-10-14T13:45:00Z"));
        assertThat(tfr.expires()).isEqualTo(Instant.parse("2026-10-14T19:00:00Z"));
        assertThat(tfr.upperFt()).isEqualTo(4999);
        assertThat(tfr.notamId()).as("txtLocalName is not always the NOTAM id").isEqualTo("261014 Atlanta, GA VIP");
    }

    @Test
    @DisplayName("Abd fallback: two clockwise arcs densified at most 2 degrees apart, a lone KM circle of 64 vertices,"
            + " FL limits and the modern text without markup")
    void handMadeArcsAndCircle() {
        XnotamParser.Tfr tfr = parse("detail_9_0001.xml");

        assertThat(tfr.geometry()).isInstanceOf(MultiPolygon.class);
        Polygon arcs = (Polygon) tfr.geometry().getGeometryN(0);
        Polygon circle = (Polygon) tfr.geometry().getGeometryN(1);
        // W, about 90 points over the north, E, about 90 over the south, W again (the great-circle bearings from the
        // center to W and E are a few hundredths of a degree off 270 and 90, so each half has 89 or 90).
        assertThat(arcs.getExteriorRing().getNumPoints()).isBetween(181, 183);
        assertThat(arcs.getEnvelopeInternal().getMaxY()).isGreaterThan(30.08);
        assertThat(arcs.getEnvelopeInternal().getMinY()).isLessThan(29.92);
        assertThat(circle.getExteriorRing().getNumPoints()).isEqualTo(XnotamParser.CIRCLE_VERTICES + 1);
        double northKm = (circle.getEnvelopeInternal().getMaxY() - 31.0) * 111.195;
        assertThat(northKm).isCloseTo(5.0, within(0.01));
        assertThat(tfr.lowerFt()).isZero();
        assertThat(tfr.lowerRef()).isEqualTo("HEI");
        assertThat(tfr.upperFt()).isEqualTo(18000);
        assertThat(tfr.upperRef()).isEqualTo("ALT");
        assertThat(tfr.effective()).as("CDT is a label: the value is UTC").isEqualTo(Instant.parse("2026-10-14T00:00:00Z"));
        assertThat(tfr.expires()).isNull();
        assertThat(tfr.description()).isEqualTo("SYNTHETIC TFR, NO EXPIRY");
    }

    @Test
    @DisplayName("an arc's direction: clockwise from west to east passes north, counter-clockwise passes south")
    void arcDirection() {
        double[] center = {30.0, -96.9};
        double radius = 5.2 * 1852;
        double[] west = {30.0, -97.0};
        double[] east = {30.0, -96.8};
        List<Coordinate> clockwise = XnotamParser.arc(center, radius, west, east, true);
        List<Coordinate> counter = XnotamParser.arc(center, radius, west, east, false);

        assertThat(clockwise).hasSizeBetween(89, 90).allSatisfy(c -> assertThat(c.y).isGreaterThan(29.999));
        assertThat(counter).hasSizeBetween(89, 90).allSatisfy(c -> assertThat(c.y).isLessThan(30.001));
        for (int i = 1; i < clockwise.size(); i++) {
            double step = XnotamParser.bearing(center, new double[] {clockwise.get(i).y, clockwise.get(i).x})
                    - XnotamParser.bearing(center, new double[] {clockwise.get(i - 1).y, clockwise.get(i - 1).x});
            assertThat((step + 360) % 360).isLessThanOrEqualTo(XnotamParser.ARC_STEP_DEGREES + 1e-6);
        }
    }

    @Test
    @DisplayName("coordinate formats: hemispheres, leading zeros, out-of-range and garbage refused")
    void coordinates() {
        assertThat(XnotamParser.point("39.00838055N", "105.41666667W")).containsExactly(39.00838055, -105.41666667);
        assertThat(XnotamParser.point("13.57111111N", "144.88222222E")).containsExactly(13.57111111, 144.88222222);
        assertThat(XnotamParser.point("14.2S", "081.5W")).containsExactly(-14.2, -81.5);
        assertThat(XnotamParser.point("91.0N", "001.0W")).isNull();
        assertThat(XnotamParser.point("10.0E", "001.0W")).isNull();
        assertThat(XnotamParser.point("10.0", "001.0W")).isNull();
        assertThat(XnotamParser.point(null, "001.0W")).isNull();
    }

    @Test
    @DisplayName("vertical limits: FT as given, FL times 100; times with an offset are honored")
    void unitsAndTimes() {
        assertThat(XnotamParser.feet("180", "FL")).isEqualTo(18000);
        assertThat(XnotamParser.feet("2500", "FT")).isEqualTo(2500);
        assertThat(XnotamParser.feet("x", "FT")).isNull();
        assertThat(XnotamParser.time("2026-10-14T13:45:00")).isEqualTo(Instant.parse("2026-10-14T13:45:00Z"));
        assertThat(XnotamParser.time("2026-10-14T13:45:00-04:00")).isEqualTo(Instant.parse("2026-10-14T17:45:00Z"));
        assertThat(XnotamParser.time("soon")).isNull();
    }

    @Test
    @DisplayName("a DOCTYPE (with an external entity) is refused before anything is resolved")
    void doctypeRejected() {
        assertThatThrownBy(() -> parse("detail_doctype.xml")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("DOCTYPE");
    }

    @Test
    @DisplayName("malformed XML and a document without Not are format errors")
    void malformed() {
        assertThatThrownBy(() -> XnotamParser.parse("<XNOTAM-Update><Group>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> XnotamParser.parse("<XNOTAM-Update/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("no Not");
        assertThatThrownBy(() -> XnotamParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
    }
}
