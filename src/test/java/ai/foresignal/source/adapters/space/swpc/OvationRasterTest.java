package ai.foresignal.source.adapters.space.swpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The OVATION raster grid (prompt 106). */
class OvationRasterTest {

    @Test
    @DisplayName("rowMajorNorthSouth lays the cells out row from lat 90 to -90")
    void rowMajor() {
        byte[] cells = new byte[OvationSampler.LONS * OvationSampler.LATS];
        cells[OvationSampler.index(10, 90)] = 100;
        cells[OvationSampler.index(10, -90)] = 50;
        cells[OvationSampler.index(0, 0)] = 7;
        OvationSampler grid = OvationSampler.of(cells, Instant.EPOCH, Instant.EPOCH);
        int[] out = grid.rowMajorNorthSouth();
        assertThat(out).hasSize(OvationSampler.LONS * OvationSampler.LATS);
        assertThat(out[0 * 360 + 10]).isEqualTo(100);
        assertThat(out[180 * 360 + 10]).isEqualTo(50);
        assertThat(out[90 * 360 + 0]).isEqualTo(7);
    }
}
