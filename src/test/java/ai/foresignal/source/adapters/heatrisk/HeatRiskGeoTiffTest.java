package ai.foresignal.source.adapters.heatrisk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.geotiff.GeoReference;
import ai.foresignal.raster.geotiff.GeoTiffValues;
import ai.foresignal.source.spi.GridDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The HeatRisk frame grid (prompt 106 task 6, post-review fix): the WPC GeoTIFF is EPSG:3857, whose
 * rows are uniform in Mercator meters, so the frame keeps the file's geotransform as an {@code affine}
 * EPSG:3857 grid instead of a latlon grid with one {@code dy}.
 *
 * <p>Synthetic georeference shaped like the verified day-1 file (2866 × 1672 pixels over CONUS); no
 * GeoTIFF bytes are needed because {@link HeatRiskAdapter#grid} reads only the georeference and size.
 */
class HeatRiskGeoTiffTest {

    static final double R = GeoReference.MERCATOR_RADIUS;
    static final int WIDTH = 2866;
    static final int HEIGHT = 1672;

    static double mercX(double lon) {
        return R * Math.toRadians(lon);
    }

    static double mercY(double lat) {
        return R * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
    }

    /** CONUS from 127° W to 65° W and 22° N to 52° N, in EPSG:3857. */
    static GeoTiffValues.Values conus() {
        double x0 = mercX(-127);
        double x1 = mercX(-65);
        double y0 = mercY(52);
        double y1 = mercY(22);
        double[] gt = {x0, (x1 - x0) / WIDTH, 0, y0, 0, (y1 - y0) / HEIGHT};
        return new GeoTiffValues.Values(new int[WIDTH * HEIGHT], WIDTH, HEIGHT, new GeoReference(3857, gt, false), 53.0);
    }

    @Test
    @DisplayName("the grid is affine EPSG:3857 with the file's geotransform and a WGS84 box")
    void affineGrid() {
        GridDefinition def = HeatRiskAdapter.grid(conus());
        assertThat(def.type()).isEqualTo(GridDefinition.AFFINE);
        assertThat(def.params()).containsEntry("crs", "EPSG:3857");
        assertThat(def.nx()).isEqualTo(WIDTH);
        assertThat(def.ny()).isEqualTo(HEIGHT);
        assertThat(def.west()).isCloseTo(-127, within(1e-6));
        assertThat(def.east()).isCloseTo(-65, within(1e-6));
        assertThat(def.north()).isCloseTo(52, within(1e-6));
        assertThat(def.south()).isCloseTo(22, within(1e-6));
    }

    @Test
    @DisplayName("a mid-CONUS latitude lands on its Mercator row, not the linear-latitude row")
    void midLatitudeRow() {
        GridDefinition def = HeatRiskAdapter.grid(conus());
        double lat = 37.0;
        double[] ij = GridDef.of(def).lonLatToIndex(-96.0, lat);
        double mercatorRow = (mercY(52) - mercY(lat)) / ((mercY(52) - mercY(22)) / HEIGHT) - 0.5;
        double linearRow = (52 - lat) / (30.0 / HEIGHT) - 0.5;
        assertThat(ij[1]).isCloseTo(mercatorRow, within(0.01));
        // The old single-dy grid was off by this many rows, about a degree of latitude.
        assertThat(Math.abs(linearRow - mercatorRow) * 30.0 / HEIGHT).isGreaterThan(0.5);
    }
}
