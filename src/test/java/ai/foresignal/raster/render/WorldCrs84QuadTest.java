package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code WorldCRS84Quad} tile ↔ lon/lat mapping (prompt 108). */
class WorldCrs84QuadTest {

    @Test
    @DisplayName("z0 is two tiles wide and one tall")
    void z0() {
        assertThat(WorldCrs84Quad.INSTANCE.tilesX(0)).isEqualTo(2);
        assertThat(WorldCrs84Quad.INSTANCE.tilesY(0)).isEqualTo(1);
        assertThat(WorldCrs84Quad.INSTANCE.tilesX(1)).isEqualTo(4);
        assertThat(WorldCrs84Quad.INSTANCE.tilesY(1)).isEqualTo(2);
    }

    @Test
    @DisplayName("the two z0 tiles split the globe at the prime meridian")
    void bounds() {
        // each z0 tile is 180 degrees square: the full latitude span, half the longitude span
        assertThat(WorldCrs84Quad.INSTANCE.tileBounds(0, 0, 0)).containsExactly(-180.0, -90.0, 0.0, 90.0);
        assertThat(WorldCrs84Quad.INSTANCE.tileBounds(0, 1, 0)).containsExactly(0.0, -90.0, 180.0, 90.0);
        assertThat(WorldCrs84Quad.tileWidth(2)).isEqualTo(45.0);
        assertThat(WorldCrs84Quad.INSTANCE.tileBounds(1, 0, 1)).containsExactly(-180.0, -90.0, -90.0, 0.0);
    }

    @Test
    @DisplayName("a tile range is inclusive and clamped")
    void range() {
        assertThat(WorldCrs84Quad.INSTANCE.tileRange(0, -10, 10, 10, 20)).containsExactly(0, 0, 1, 0);
        assertThat(WorldCrs84Quad.INSTANCE.tileRange(1, -100, -10, -80, 10)).containsExactly(0, 0, 1, 1);
    }

    @Test
    @DisplayName("the pixel locator maps the first pixel centre inside the north-west corner")
    void locator() {
        double[] out = new double[2];
        WorldCrs84Quad.INSTANCE.locator(0, 0, 0, 256).locate(0, 0, out);
        assertThat(out[0]).isCloseTo(-180.0 + (180.0 / 256) / 2, org.assertj.core.data.Offset.offset(1e-9));
        // pixels are square: a z0 tile spans 180 degrees of latitude as well as longitude
        assertThat(out[1]).isCloseTo(90.0 - (180.0 / 256) / 2, org.assertj.core.data.Offset.offset(1e-9));
    }
}
