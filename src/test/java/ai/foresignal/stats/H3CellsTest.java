package ai.foresignal.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link H3Cells} (prompt 038): cells, parents, centers and boundaries, including the split of
 * boundaries that cross the antimeridian.
 */
class H3CellsTest {

    final H3Cells cells = new H3Cells();

    @Test
    @DisplayName("a point maps to a cell at the requested resolution; bad coordinates map to the no-geometry cell")
    void cells() {
        long cell = cells.cell(-97.74, 30.27, 4);
        assertThat(cells.valid(cell)).isTrue();
        assertThat(cells.resolution(cell)).isEqualTo(4);
        assertThat(cells.parse(cells.address(cell))).isEqualTo(cell);
        assertThat(cells.cell(Double.NaN, 30, 4)).isEqualTo(H3Cells.NO_CELL);
        assertThat(cells.cell(10, 95, 4)).isEqualTo(H3Cells.NO_CELL);
        assertThat(cells.cell(190, 10, 4)).as("longitudes wrap").isEqualTo(cells.cell(-170, 10, 4));
        assertThat(cells.valid(H3Cells.NO_CELL)).isFalse();
        assertThat(cells.valid(H3Cells.ALL_CELLS)).isFalse();
        double[] center = cells.center(cell);
        assertThat(center[0]).isBetween(-98.2, -97.2);
        assertThat(center[1]).isBetween(29.8, 30.8);
    }

    @Test
    @DisplayName("parents aggregate children at a coarser resolution, through any intermediate resolution")
    void parents() {
        long child = cells.cell(10.0, 50.0, 4);
        long parent = cells.parent(child, 2);
        assertThat(cells.resolution(parent)).isEqualTo(2);
        assertThat(cells.parent(parent, 3)).as("never finer than the cell").isEqualTo(parent);
        assertThat(cells.parent(H3Cells.NO_CELL, 2)).isEqualTo(H3Cells.NO_CELL);
        assertThat(cells.parent(cells.parent(child, 3), 2)).as("parents chain").isEqualTo(parent);
    }

    @Test
    @DisplayName("an ordinary cell's boundary is a closed six-vertex polygon")
    void boundary() {
        Map<String, Object> geometry = cells.boundary(cells.cell(10.0, 50.0, 4));
        assertThat(geometry).containsEntry("type", "Polygon");
        @SuppressWarnings("unchecked")
        List<List<double[]>> rings = (List<List<double[]>>) geometry.get("coordinates");
        List<double[]> ring = rings.getFirst();
        assertThat(ring).hasSize(7);
        assertThat(ring.getFirst()).isEqualTo(ring.getLast());
    }

    @Test
    @DisplayName("a boundary crossing the antimeridian becomes a MultiPolygon with one part on each side")
    void antimeridian() {
        Map<String, Object> geometry = H3Cells.boundary(List.of(new double[] {179.5, 10}, new double[] {-179.5, 10},
                new double[] {-179.5, 11}, new double[] {179.5, 11}));
        assertThat(geometry).containsEntry("type", "MultiPolygon");
        @SuppressWarnings("unchecked")
        List<List<List<double[]>>> parts = (List<List<List<double[]>>>) geometry.get("coordinates");
        assertThat(parts).hasSize(2);
        for (List<List<double[]>> part : parts) {
            List<double[]> ring = part.getFirst();
            double min = ring.stream().mapToDouble(v -> v[0]).min().orElseThrow();
            double max = ring.stream().mapToDouble(v -> v[0]).max().orElseThrow();
            assertThat(max - min).as("no part spans the globe").isLessThanOrEqualTo(0.5 + 1e-9);
            assertThat(min).isGreaterThanOrEqualTo(-180);
            assertThat(max).isLessThanOrEqualTo(180);
        }
        long fijiCell = cells.cell(180.0, -17.0, 2);
        Map<String, Object> real = cells.boundary(fijiCell);
        assertThat(real.get("type")).isIn("Polygon", "MultiPolygon");
    }

    @Test
    @DisplayName("an open ring is closed, and coordinates are rounded to seven decimals")
    void closesAndRounds() {
        Map<String, Object> geometry = H3Cells.boundary(List.of(new double[] {1.123456789, 2}, new double[] {3, 2},
                new double[] {3, 4}));
        @SuppressWarnings("unchecked")
        List<List<double[]>> rings = (List<List<double[]>>) geometry.get("coordinates");
        assertThat(rings.getFirst()).hasSize(4);
        assertThat(rings.getFirst().getFirst()[0]).isEqualTo(1.1234568);
        assertThat(H3Cells.round(0.123456789)).isEqualTo(0.1234568);
    }
}
