package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.Geo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/** {@link CorrelationEngine#geometry} and {@link CorrelationEngine#unionAll}: an event's area from mixed members. */
class CorrelationGeometryTest {

    private static Geometry point(double x, double y) {
        return Geo.factory().createPoint(new Coordinate(x, y));
    }

    private static Geometry square(double x, double y) {
        return Geo.factory().createPolygon(new Coordinate[] {new Coordinate(x, y), new Coordinate(x + 1, y),
            new Coordinate(x + 1, y + 1), new Coordinate(x, y + 1), new Coordinate(x, y)});
    }

    @Test
    @DisplayName("a point and a polygon apart, then a third member: pairwise union fails, the event area does not")
    void mixedMembers() {
        List<Geometry> members = List.of(point(0, 0), square(5, 5), point(10, 10));
        // The failure the recipes reported: the first union is a GeometryCollection.
        assertThatThrownBy(() -> members.get(0).union(members.get(1)).union(members.get(2)))
                .hasMessageContaining("GeometryCollection");

        Geometry area = CorrelationEngine.geometry(members);
        assertThat(area.getGeometryType()).isIn("Polygon", "MultiPolygon");
        for (Geometry member : members) {
            assertThat(area.covers(member)).isTrue();
        }
    }

    @Test
    @DisplayName("an event area that is already a collection merges with a new candidate")
    void mergeCollection() {
        Geometry stored = Geo.factory().buildGeometry(List.of(point(0, 0), square(5, 5)));
        Geometry merged = CorrelationEngine.unionAll(List.of(stored, point(10, 10)));
        assertThat(merged.covers(point(0, 0))).isTrue();
        assertThat(merged.covers(point(10, 10))).isTrue();
        assertThat(merged.covers(square(5, 5))).isTrue();
    }

    @Test
    @DisplayName("no members, no area")
    void empty() {
        assertThat(CorrelationEngine.geometry(List.of())).isNull();
    }
}
