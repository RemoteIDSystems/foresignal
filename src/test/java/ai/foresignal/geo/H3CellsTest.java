package ai.foresignal.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/** {@link H3Cells} (prompt 092): cell conversion, valid polygons, antimeridian splitting and invalid-index handling. */
class H3CellsTest {

    @Test
    @DisplayName("a lat/lng maps to a valid cell whose polygon is a closed ring")
    void roundTrip() {
        long cell = H3Cells.latLngToCell(51.5, -0.12, 4);
        assertThat(H3Cells.isValid(cell)).isTrue();
        var polygon = H3Cells.cellToPolygon(cell);
        assertThat(polygon.getNumPoints()).isEqualTo(7);
        assertThat(polygon.getSRID()).isEqualTo(4326);
        assertThat(polygon.isValid()).isTrue();
        assertThat(polygon.getArea()).isPositive();
    }

    @Test
    @DisplayName("an address string converts, and an invalid index is refused")
    void address() throws Exception {
        long cell = H3Cells.latLngToCell(0, 0, 4);
        String address = com.uber.h3core.H3Core.newInstance().h3ToString(cell);
        assertThat(H3Cells.isValid(address)).isTrue();
        assertThat(H3Cells.cellToPolygon(address).getNumPoints()).isEqualTo(7);
        assertThat(H3Cells.isValid("not-an-h3")).isFalse();
        assertThatThrownBy(() -> H3Cells.cellToPolygon(-1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a cell across the antimeridian is split into a valid multipolygon inside [-180, 180]")
    void antimeridian() {
        Geometry split = null;
        for (double lat = -60; lat <= 60 && split == null; lat += 0.25) {
            Geometry candidate = H3Cells.cellToPolygon(H3Cells.latLngToCell(lat, 179.98, 4));
            if (candidate instanceof MultiPolygon) {
                split = candidate;
            }
        }
        assertThat(split).as("some resolution-4 cell along 179.98 E crosses the antimeridian").isNotNull();
        assertThat(split.getNumGeometries()).isEqualTo(2);
        assertThat(split.isValid()).isTrue();
        assertThat(split.getSRID()).isEqualTo(4326);
        for (Coordinate c : split.getCoordinates()) {
            assertThat(c.x).isBetween(-180.0, 180.0);
        }
        assertThat(split.getEnvelopeInternal().getWidth()).as("not a band around the globe").isGreaterThan(300);
        // One half touches +180, the other -180.
        assertThat(split.getGeometryN(0).getEnvelopeInternal().getWidth()).isLessThan(5);
        assertThat(split.getGeometryN(1).getEnvelopeInternal().getWidth()).isLessThan(5);
    }

    @Test
    @DisplayName("a cell away from the antimeridian stays one polygon with no longitude past 180")
    void ordinaryCellStaysPolygon() {
        Geometry cell = H3Cells.cellToPolygon(H3Cells.latLngToCell(-36.85, 174.76, 4));
        assertThat(cell).isInstanceOf(Polygon.class);
        for (Coordinate c : cell.getCoordinates()) {
            assertThat(c.x).isBetween(-180.0, 180.0);
        }
    }

    @Test
    @DisplayName("the center of an antimeridian cell stays near 180, where its boundary's centroid does not")
    void antimeridianCenter() {
        long crossing = 0;
        for (double lat = -60; lat <= 60 && crossing == 0; lat += 0.25) {
            long cell = H3Cells.latLngToCell(lat, 179.98, 4);
            if (H3Cells.cellToPolygon(cell) instanceof MultiPolygon) {
                crossing = cell;
            }
        }
        assertThat(crossing).as("some resolution-4 cell along 179.98 E crosses the antimeridian").isNotZero();
        Coordinate center = H3Cells.cellCenter(crossing);
        assertThat(Math.abs(center.x)).isGreaterThan(170);
        assertThat(Math.abs(H3Cells.cellToPolygon(crossing).getCentroid().getX()))
                .as("the two-part centroid is the wrong answer this guards against").isLessThan(170);
        assertThat(H3Cells.latLngToCell(center.y, center.x, 4)).isEqualTo(crossing);
        assertThatThrownBy(() -> H3Cells.cellCenter(-1L)).isInstanceOf(IllegalArgumentException.class);
    }
}
