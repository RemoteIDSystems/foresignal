package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.GridDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A native 512 px tile matches four 256 px tiles of the next zoom (prompt 108). */
class Tile512Test {

    @Test
    @DisplayName("each 512 px pixel maps to the same cell as the corresponding 256 px child pixel")
    void native512() {
        GridDef grid = GridDef.of(GridDefinition.latLon(720, 361, -180, -90, 180, 90));
        int z = 3;
        int x = 2;
        int y = 3;
        IndexMap big = IndexMap.build(grid, 512, 512,
                WebMercatorQuad.INSTANCE.locator(z, x, y, 512), false);
        assertThat(big.width()).isEqualTo(512);
        int[] bigNearest = big.nearest();
        for (int child = 0; child < 4; child++) {
            int cx = 2 * x + (child % 2);
            int cy = 2 * y + (child / 2);
            IndexMap small = IndexMap.build(grid, 256, 256,
                    WebMercatorQuad.INSTANCE.locator(z + 1, cx, cy, 256), false);
            int[] smallNearest = small.nearest();
            int xOffset = (child % 2) * 256;
            int yOffset = (child / 2) * 256;
            for (int py = 0; py < 256; py++) {
                for (int px = 0; px < 256; px++) {
                    int bigPixel = (yOffset + py) * 512 + (xOffset + px);
                    int smallPixel = py * 256 + px;
                    assertThat(bigNearest[bigPixel])
                            .as("child %d pixel (%d,%d)", child, px, py)
                            .isEqualTo(smallNearest[smallPixel]);
                }
            }
        }
    }
}
