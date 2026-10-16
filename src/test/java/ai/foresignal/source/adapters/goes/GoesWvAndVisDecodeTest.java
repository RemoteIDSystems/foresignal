package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.GeosGrid;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.GeosGrids;
import ai.foresignal.source.spi.GridDefinition;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The GOES C02 crop and the daytime check (prompt 106 task 2, post-review fix): the crop window is
 * worked out in scan-angle space and the cropped grid's bounds are projected back from scan angles, so
 * they are degrees, not the parent box plus radians.
 *
 * <p>The grid is the GOES-East CONUS 0.5 km C02 fixed grid (sub-satellite longitude −75°,
 * perspective height 35,786,023 m, 10,000 × 6,000 cells of 14 µrad from x = −0.101346, y = 0.128226),
 * as in a CMIPC C02 file's {@code x}/{@code y} coordinates; no file is needed.
 */
class GoesWvAndVisDecodeTest {

    static final int NX = 10_000;
    static final int NY = 6_000;

    /** A {@code geos} definition of the CONUS C02 grid, its box from the projection. */
    static GridDefinition conusC02() {
        GeosGrid grid = new GeosGrid(NX, NY, -75, 35_786_023, GeosGrid.GRS80_A, GeosGrid.GRS80_B, true, -0.101346, 1.4e-5,
                0.128226, -1.4e-5, new double[] {-180, -90, 180, 90});
        double[] box = GeosGrids.boxOf(grid);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lon0", -75.0);
        params.put("h", 35_786_023.0);
        params.put("sweep", "x");
        params.put("x0", -0.101346);
        params.put("dx", 1.4e-5);
        params.put("y0", 0.128226);
        params.put("dy", -1.4e-5);
        return new GridDefinition(GridDefinition.GEOS, NX, NY, box[0], box[1], box[2], box[3], params);
    }

    static final double[] TEXAS = {-100.0, 29.0, -96.0, 33.0};

    @Test
    @DisplayName("the crop window covers the AOI + 300 km and stays inside the image")
    void cropWindowCoversAoi() {
        GeosGrid grid = (GeosGrid) GridDef.of(conusC02());
        int[] w = GoesAbiAdapter.cropWindow(grid, TEXAS);
        assertThat(w).isNotNull();
        assertThat(w[0]).isGreaterThanOrEqualTo(0);
        assertThat(w[1]).isGreaterThanOrEqualTo(0);
        assertThat(w[0] + w[2]).isLessThanOrEqualTo(NX);
        assertThat(w[1] + w[3]).isLessThanOrEqualTo(NY);
        // Far smaller than the full image: the point of cropping.
        assertThat((long) w[2] * w[3]).isLessThan((long) NX * NY / 4);
    }

    @Test
    @DisplayName("the cropped grid's bounds are degrees from the projection and contain the AOI + margin")
    void croppedBoundsAreGeographic() {
        GridDefinition def = conusC02();
        GeosGrid grid = (GeosGrid) GridDef.of(def);
        int[] w = GoesAbiAdapter.cropWindow(grid, TEXAS);
        GridDefinition cropped = GoesAbiAdapter.croppedGrid(new DecodedGrid(def, NX, NY, null, null, null, null,
                Map.of()), w);
        assertThat(cropped.nx()).isEqualTo(w[2]);
        assertThat(cropped.ny()).isEqualTo(w[3]);
        // AOI + 300 km is about 3.1° in longitude at 31° N and 2.7° in latitude.
        assertThat(cropped.west()).isLessThan(TEXAS[0] - 3.0).isGreaterThan(-115.0);
        assertThat(cropped.east()).isGreaterThan(TEXAS[2] + 3.0).isLessThan(-85.0);
        assertThat(cropped.south()).isLessThan(TEXAS[1] - 2.6).isGreaterThan(20.0);
        assertThat(cropped.north()).isGreaterThan(TEXAS[3] + 2.6).isLessThan(42.0);
        assertThat((double) cropped.params().get("x0")).isCloseTo(-0.101346 + w[0] * 1.4e-5, within(1e-12));
        assertThat((double) cropped.params().get("y0")).isCloseTo(0.128226 - w[1] * 1.4e-5, within(1e-12));
    }

    @Test
    @DisplayName("a point keeps its place: the cropped index is the full index minus the window origin")
    void croppedIndexMatchesFull() {
        GridDefinition def = conusC02();
        int[] w = GoesAbiAdapter.cropWindow((GeosGrid) GridDef.of(def), TEXAS);
        GridDefinition cropped = GoesAbiAdapter.croppedGrid(new DecodedGrid(def, NX, NY, null, null, null, null,
                Map.of()), w);
        double[] full = GridDef.of(def).lonLatToIndex(-97.74, 30.27);
        double[] crop = GridDef.of(cropped).lonLatToIndex(-97.74, 30.27);
        assertThat(crop[0]).isCloseTo(full[0] - w[0], within(1e-6));
        assertThat(crop[1]).isCloseTo(full[1] - w[1], within(1e-6));
        // And the cropped grid's bounds really contain Austin (rendering rejects points outside them).
        assertThat(-97.74).isBetween(cropped.west(), cropped.east());
        assertThat(30.27).isBetween(cropped.south(), cropped.north());
    }

    @Test
    @DisplayName("an AOI off the visible disc gives no window")
    void offDiscNoWindow() {
        GeosGrid grid = (GeosGrid) GridDef.of(conusC02());
        assertThat(GoesAbiAdapter.cropWindow(grid, new double[] {100.0, 10.0, 104.0, 14.0})).isNull();
    }

    @Test
    @DisplayName("an AOI partly beyond the image clamps the window instead of shrinking it")
    void partlyOutsideClamps() {
        GeosGrid grid = (GeosGrid) GridDef.of(conusC02());
        int[] w = GoesAbiAdapter.cropWindow(grid, new double[] {-128.0, 40.0, -120.0, 50.0});
        assertThat(w).isNotNull();
        assertThat(w[0]).isZero();
    }

    @Test
    @DisplayName("the daytime check uses the smallest zenith over the AOI, not the centroid's")
    void zenithOverWholeAoi() {
        double[] wide = {-110.0, 25.0, -80.0, 45.0};
        Instant sunriseEast = Instant.parse("2026-10-16T12:15:00Z");
        double min = GoesAbiAdapter.minSolarZenith(wide, sunriseEast);
        double center = GoesAbiAdapter.solarZenith(-95.0, 35.0, sunriseEast);
        double eastCorner = GoesAbiAdapter.solarZenith(-80.0, 25.0, sunriseEast);
        assertThat(min).isLessThan(center);
        assertThat(min).isLessThanOrEqualTo(eastCorner);
        assertThat(min).isLessThan(GoesAbiAdapter.VIS_MAX_SOLAR_ZENITH);
        assertThat(center).isGreaterThan(GoesAbiAdapter.VIS_MAX_SOLAR_ZENITH);
    }

    @Test
    @DisplayName("at local midnight the whole AOI is dark and the frame would be skipped")
    void nightSkipped() {
        assertThat(GoesAbiAdapter.minSolarZenith(TEXAS, Instant.parse("2026-10-16T06:00:00Z")))
                .isGreaterThan(GoesAbiAdapter.VIS_MAX_SOLAR_ZENITH);
        assertThat(GoesAbiAdapter.minSolarZenith(TEXAS, Instant.parse("2026-10-16T18:00:00Z")))
                .isLessThan(GoesAbiAdapter.VIS_MAX_SOLAR_ZENITH);
    }

    @Test
    @DisplayName("crop() copies the window rows")
    void cropCopiesRows() {
        float[] data = new float[4 * 3];
        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }
        assertThat(GoesAbiAdapter.crop(data, 4, 3, new int[] {1, 1, 2, 2})).containsExactly(5f, 6f, 9f, 10f);
    }
}
