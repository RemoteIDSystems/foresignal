package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.geo.H3Cells;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;

/** The cell center behind the population polyfill (prompts 101, 092). */
class PopulationPackServiceTest {

    @Test
    @DisplayName("an antimeridian cell's center is its H3 center near 180, not the two-part centroid near 0")
    void antimeridianCellCenter() {
        long crossing = 0;
        for (double lat = -60; lat <= 60 && crossing == 0; lat += 0.25) {
            long cell = H3Cells.latLngToCell(lat, 179.999, PopulationPackService.RES_FINE);
            if (H3Cells.cellToPolygon(cell) instanceof MultiPolygon) {
                crossing = cell;
            }
        }
        assertThat(crossing).as("some fine cell along 179.999 E crosses the antimeridian").isNotZero();
        double[] center = PopulationPackService.center(crossing);
        assertThat(center).isNotNull();
        assertThat(Math.abs(center[0])).as("longitude").isGreaterThan(179);
        assertThat(H3Cells.latLngToCell(center[1], center[0], PopulationPackService.RES_FINE)).isEqualTo(crossing);
    }

    @Test
    @DisplayName("an invalid cell has no center")
    void invalidCell() {
        assertThat(PopulationPackService.center(-1L)).isNull();
    }
}
