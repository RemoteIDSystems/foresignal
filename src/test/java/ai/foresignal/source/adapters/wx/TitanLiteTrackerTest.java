package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/** TITAN-lite association (prompt 105): IoU matching, merges, splits, advection. */
class TitanLiteTrackerTest {

    static final GeometryFactory GF = Geo.factory();
    static final Instant T0 = Instant.parse("2026-10-15T00:00:00Z");

    private static Geometry square(double x0, double y0, double x1, double y1) {
        return GF.createPolygon(new Coordinate[] {
                new Coordinate(x0, y0), new Coordinate(x1, y0), new Coordinate(x1, y1), new Coordinate(x0, y1),
                new Coordinate(x0, y0)});
    }

    private static TitanLiteTracker.Previous previous(String id, Geometry g, double speed) {
        return new TitanLiteTracker.Previous(id, g, g.getCentroid().getX(), g.getCentroid().getY(), T0, speed, 90);
    }

    private static TitanLiteTracker.Cell cell(Geometry g) {
        return new TitanLiteTracker.Cell(g, g.getCentroid().getX(), g.getCentroid().getY(), g.getArea(), 55);
    }

    @Test
    @DisplayName("a steady track keeps its id: the overlap is assigned")
    void steadyKeepsId() {
        Geometry g = square(0, 0, 1, 1);
        var result = TitanLiteTracker.associate(List.of(previous("t-1", g, 0)), List.of(cell(g)), T0.plusSeconds(600));
        assertThat(result.assignments()).hasSize(1);
        assertThat(result.assignments().get(0).previous()).isZero();
        assertThat(result.assignments().get(0).cell()).isZero();
        assertThat(result.assignments().get(0).iou()).isGreaterThan(0.99);
    }

    @Test
    @DisplayName("a current cell overlapping two tracks merges: one continues, the other ends")
    void merges() {
        Geometry left = square(0, 0, 1, 1);
        Geometry right = square(0.5, 0, 1.5, 1);
        Geometry merged = square(0, 0, 1.5, 1);
        var result = TitanLiteTracker.associate(
                List.of(previous("t-1", left, 0), previous("t-2", right, 0)),
                List.of(cell(merged)), T0.plusSeconds(600));
        assertThat(result.merges()).hasSize(1);
        assertThat(result.assignments()).hasSize(1);
        TitanLiteTracker.Merge merge = result.merges().get(0);
        assertThat(merge.intoPrevious()).isEqualTo(result.assignments().get(0).previous());
        assertThat(merge.endedPrevious()).isNotEqualTo(merge.intoPrevious());
    }

    @Test
    @DisplayName("a merge keeps the largest-area track even when the solver paired the cell with the smaller one")
    void mergeKeepsLargest() {
        Geometry big = square(0, 0, 1, 1);
        Geometry small = square(0.9, 0, 1.5, 1);
        Geometry merged = square(0.7, 0, 1.5, 1);
        var result = TitanLiteTracker.associate(
                List.of(previous("t-big", big, 0), previous("t-small", small, 0)),
                List.of(cell(merged)), T0.plusSeconds(600));
        assertThat(result.assignments()).singleElement().satisfies(a -> {
            assertThat(a.previous()).as("the larger track continues").isZero();
            assertThat(a.cell()).isZero();
        });
        assertThat(result.merges()).containsExactly(new TitanLiteTracker.Merge(1, 0));
        assertThat(result.unmatchedPrevious()).isEmpty();
    }

    @Test
    @DisplayName("one track overlapping two cells splits: the other cell starts a child")
    void splits() {
        Geometry whole = square(0, 0, 1.5, 1);
        Geometry c1 = square(0, 0, 1, 1);
        Geometry c2 = square(0.5, 0, 1.5, 1);
        var result = TitanLiteTracker.associate(List.of(previous("t-1", whole, 0)),
                List.of(cell(c1), cell(c2)), T0.plusSeconds(600));
        assertThat(result.assignments()).hasSize(1);
        assertThat(result.splits()).hasSize(1);
    }

    @Test
    @DisplayName("no overlap and too far away leaves both unmatched")
    void unmatched() {
        Geometry a = square(0, 0, 1, 1);
        Geometry b = square(50, 50, 51, 51);
        var result = TitanLiteTracker.associate(List.of(previous("t-1", a, 0)), List.of(cell(b)),
                T0.plusSeconds(600));
        assertThat(result.assignments()).isEmpty();
        assertThat(result.unmatchedCells()).containsExactly(0);
        assertThat(result.unmatchedPrevious()).containsExactly(0);
    }

    @Test
    @DisplayName("advection moves a polygon toward the bearing")
    void advects() {
        Geometry g = square(-1, -1, 0, 0);
        Geometry moved = TitanLiteTracker.advect(g, -0.5, -0.5, 36, 0, Duration.ofHours(1));
        assertThat(moved.getCentroid().getY()).isGreaterThan(g.getCentroid().getY());
    }

    @Test
    @DisplayName("IoU is the intersection over the union")
    void iou() {
        Geometry a = square(0, 0, 2, 2);
        Geometry b = square(1, 0, 3, 2);
        double value = TitanLiteTracker.iou(a, b);
        // intersection 1x2 = 2, union 3x2 = 6
        assertThat(value).isCloseTo(1.0 / 3, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(TitanLiteTracker.iou(a, null)).isZero();
    }

    @Test
    @DisplayName("destination moves north for bearing 0 and east for bearing 90")
    void destination() {
        double[] north = TitanLiteTracker.destination(0, 0, 100, 0, 1);
        assertThat(north[1]).isGreaterThan(0);
        double[] east = TitanLiteTracker.destination(0, 0, 100, 90, 1);
        assertThat(east[0]).isGreaterThan(0);
    }

    @Test
    @DisplayName("a Polygon is a Geometry")
    void polygonIsGeometry() {
        assertThat(square(0, 0, 1, 1)).isInstanceOf(Polygon.class);
    }
}
