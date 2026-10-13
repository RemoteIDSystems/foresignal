package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The grid projections of prompt 045: round trips per grid type, the GOES-R PUG worked example, the
 * visible-disc limit, the HRRR corner points and the antimeridian wrap of a global lat/lon grid.
 */
class GridDefTest {

    /** GOES-East: perspective height 35786023 m, sub-satellite longitude −75°, GRS80. */
    static final Map<String, Object> GOES_EAST = Map.of("lon0", -75.0, "h", 35_786_023.0, "sweep", "x",
            "x0", -0.101332, "dx", 5.6e-5, "y0", 0.128212, "dy", -5.6e-5);

    /** HRRR: 1799 × 1059 Lambert conformal, 3 km, first point (south-west) 21.138123° N, 237.280472° E. */
    static GridDefinition hrrr() {
        return new GridDefinition(GridDefinition.LCC, 1799, 1059, -134.1, 21.1, -60.9, 52.6, Map.of(
                "lat1", 38.5, "lat2", 38.5, "lov", 262.5, "radius", 6_371_229.0, "la1", 21.138123, "lo1", 237.280472,
                "dx", 3000.0, "dy", 3000.0));
    }

    @Test
    @DisplayName("latlon from the box: cell centers half a cell inside, round trip, outside is NaN")
    void latLonBox() {
        GridDef grid = GridDef.of(GridDefinition.latLon(4, 3, -100.0, 28.0, -94.0, 34.0));
        assertThat(grid).isInstanceOf(LatLonGrid.class);
        assertThat(grid.indexToLonLat(0, 0)).containsExactly(-99.25, 33.0);
        double[] ij = grid.lonLatToIndex(-95.5, 29.5);
        assertThat(ij[0]).isCloseTo(2.5, within(1e-9));
        assertThat(ij[1]).isCloseTo(1.75, within(1e-9));
        assertThat(grid.nearestCell(-99.25, 33.0)).isZero();
        assertThat(grid.nearestCell(-94.75, 29.0)).isEqualTo(11);
        assertThat(grid.lonLatToIndex(-101, 30)[0]).isNaN();
        assertThat(grid.lonLatToIndex(-97, 35)[1]).isNaN();
        assertThat(grid.nearestCell(-101, 30)).isEqualTo(-1);
        assertThat(grid.bounds()).containsExactly(-100.0, 28.0, -94.0, 34.0);
        for (double lon = -99.9; lon < -94; lon += 0.37) {
            double[] back = grid.indexToLonLat(grid.lonLatToIndex(lon, 31.1)[0], grid.lonLatToIndex(lon, 31.1)[1]);
            assertThat(back[0]).isCloseTo(lon, within(1e-9));
            assertThat(back[1]).isCloseTo(31.1, within(1e-9));
        }
    }

    @Test
    @DisplayName("a global 0..360 latlon grid normalizes once and wraps at the antimeridian")
    void latLonGlobalWrap() {
        GridDef grid = GridDef.of(new GridDefinition(GridDefinition.LATLON, 1440, 721, -180, -90, 180, 90,
                Map.of("lo1", 0.0, "la1", 90.0, "dx", 0.25, "dy", 0.25)));
        assertThat(grid.wrapsLongitude()).isTrue();
        assertThat(grid.indexToLonLat(0, 0)).containsExactly(0.0, 90.0);
        assertThat(grid.lonLatToIndex(-0.25, 0)[0]).isCloseTo(1439, within(1e-9));
        assertThat(grid.lonLatToIndex(180, 0)[0]).isCloseTo(720, within(1e-9));
        assertThat(grid.lonLatToIndex(-180, 0)[0]).isCloseTo(720, within(1e-9));
        assertThat(grid.nearestCell(359.9, 0)).isEqualTo(360 * 1440);
        assertThat(grid.nearestCell(-0.1, 0)).isEqualTo(360 * 1440);
        assertThat(grid.column(1440)).isZero();
        assertThat(grid.column(-1)).isEqualTo(1439);
        double[] back = grid.indexToLonLat(grid.lonLatToIndex(179.9, -45)[0], grid.lonLatToIndex(179.9, -45)[1]);
        assertThat(back[0]).isCloseTo(179.9, within(1e-9));
        assertThat(back[1]).isCloseTo(-45, within(1e-9));
    }

    @Test
    @DisplayName("GOES-East reproduces the PUG worked example in both directions")
    void geosPugExample() {
        GeosGrid grid = (GeosGrid) GridDef.of(new GridDefinition(GridDefinition.GEOS, 5000, 3000, -152, 14, -52, 56,
                GOES_EAST));
        double[] ll = grid.lonLatAt(-0.024052, 0.095340);
        assertThat(ll[0]).isCloseTo(-84.690932, within(1e-5));
        assertThat(ll[1]).isCloseTo(33.846162, within(1e-5));
        double[] xy = grid.scanAngles(-84.690932, 33.846162);
        assertThat(xy[0]).isCloseTo(-0.024052, within(1e-6));
        assertThat(xy[1]).isCloseTo(0.095340, within(1e-6));
    }

    @Test
    @DisplayName("geos round trips, the sub-satellite point is (0, 0), and beyond the disc is NaN (both sweeps)")
    void geosRoundTripAndDisc() {
        GridDef grid = GridDef.of(new GridDefinition(GridDefinition.GEOS, 5000, 3000, -152, 14, -52, 56, GOES_EAST));
        double[] ij = grid.lonLatToIndex(-90.0, 30.0);
        double[] back = grid.indexToLonLat(ij[0], ij[1]);
        assertThat(back[0]).isCloseTo(-90.0, within(1e-6));
        assertThat(back[1]).isCloseTo(30.0, within(1e-6));
        GeosGrid geos = (GeosGrid) grid;
        assertThat(geos.scanAngles(-75, 0)[0]).isCloseTo(0, within(1e-12));
        assertThat(geos.scanAngles(-75, 0)[1]).isCloseTo(0, within(1e-12));
        assertThat(geos.scanAngles(10, 20)[0]).as("85° from the sub-satellite point is off the disc").isNaN();
        assertThat(geos.scanAngles(-75 + 81.0, 0)[0]).as("81° east is still visible").isNotNaN();
        assertThat(geos.scanAngles(-75 + 81.3, 0)[0]).as("81.3° east is past the limb").isNaN();
        assertThat(geos.lonLatAt(0.2, 0)[0]).as("a scan angle past the Earth's edge").isNaN();
        GeosGrid meteosat = (GeosGrid) GridDef.of(new GridDefinition(GridDefinition.GEOS, 3712, 3712, -80, -80, 80, 80,
                Map.of("lon0", 0.0, "h", 35_785_831.0, "sweep", "y", "x0", -0.15, "dx", 8.1e-5, "y0", 0.15,
                        "dy", -8.1e-5)));
        double[] xy = meteosat.scanAngles(20, 45);
        double[] ll = meteosat.lonLatAt(xy[0], xy[1]);
        assertThat(ll[0]).isCloseTo(20, within(1e-6));
        assertThat(ll[1]).isCloseTo(45, within(1e-6));
    }

    @Test
    @DisplayName("HRRR corner points from the definition, and a round trip")
    void hrrrCorners() {
        GridDef grid = GridDef.of(hrrr());
        assertThat(grid).isInstanceOf(LccGrid.class);
        double[] sw = grid.indexToLonLat(0, 1058);
        assertThat(sw[0]).isCloseTo(-122.719528, within(1e-5));
        assertThat(sw[1]).isCloseTo(21.138123, within(1e-5));
        double[] se = grid.indexToLonLat(1798, 1058);
        assertThat(se[0]).isCloseTo(-72.2897, within(0.01));
        assertThat(se[1]).isCloseTo(21.1405, within(0.01));
        double[] nw = grid.indexToLonLat(0, 0);
        assertThat(nw[0]).isCloseTo(-134.0955, within(0.01));
        assertThat(nw[1]).isCloseTo(47.8386, within(0.01));
        double[] ne = grid.indexToLonLat(1798, 0);
        assertThat(ne[0]).isCloseTo(-60.9172, within(0.01));
        assertThat(ne[1]).isCloseTo(47.8422, within(0.01));
        double[] ij = grid.lonLatToIndex(-97.5, 38.5);
        double[] back = grid.indexToLonLat(ij[0], ij[1]);
        assertThat(back[0]).isCloseTo(-97.5, within(1e-6));
        assertThat(back[1]).isCloseTo(38.5, within(1e-6));
        assertThat(grid.lonLatToIndex(-150, 60)[0]).isNaN();
    }

    @Test
    @DisplayName("affine 4326 and 3857 geotransforms round trip; corners sit half a cell from centers")
    void affine() {
        GridDef deg = GridDef.of(new GridDefinition(GridDefinition.AFFINE, 100, 50, -100, 25, -90, 30, Map.of(
                "crs", "EPSG:4326", "gt0", -100.0, "gt1", 0.1, "gt2", 0.0, "gt3", 30.0, "gt4", 0.0, "gt5", -0.1)));
        assertThat(deg.indexToLonLat(0, 0)[0]).isCloseTo(-99.95, within(1e-9));
        assertThat(deg.indexToLonLat(0, 0)[1]).isCloseTo(29.95, within(1e-9));
        assertThat(deg.nearestCell(-99.96, 29.96)).isZero();
        double[] ij = deg.lonLatToIndex(-93.33, 27.77);
        double[] back = deg.indexToLonLat(ij[0], ij[1]);
        assertThat(back[0]).isCloseTo(-93.33, within(1e-9));
        assertThat(back[1]).isCloseTo(27.77, within(1e-9));
        GridDef merc = GridDef.of(new GridDefinition(GridDefinition.AFFINE, 256, 256, -10, -10, 10, 10, Map.of(
                "crs", "EPSG:3857", "gt0", -1_113_194.9, "gt1", 8696.8, "gt2", 0.0, "gt3", 1_118_890.0, "gt4", 0.0,
                "gt5", -8741.3)));
        double[] mij = merc.lonLatToIndex(3.0, -4.0);
        double[] mback = merc.indexToLonLat(mij[0], mij[1]);
        assertThat(mback[0]).isCloseTo(3.0, within(1e-7));
        assertThat(mback[1]).isCloseTo(-4.0, within(1e-7));
        assertThat(merc.lonLatToIndex(30, 0)[0]).isNaN();
    }

    @Test
    @DisplayName("054: an EPSG:4326 affine grid running past 180° (a Bering Sea pass) is found one turn east")
    void affinePastAntimeridian() {
        GridDef grid = GridDef.of(new GridDefinition(GridDefinition.AFFINE, 120, 160, 165, 30, -165, 70, Map.of(
                "crs", "EPSG:4326", "gt0", 165.0, "gt1", 0.25, "gt2", 0.0, "gt3", 70.0, "gt4", 0.0, "gt5", -0.25)));
        double[] west = grid.lonLatToIndex(170.0, 60.0);
        assertThat(west[0]).isCloseTo(19.5, within(1e-9));
        double[] east = grid.lonLatToIndex(-170.0, 60.0);
        assertThat(east[0]).as("190°E is column 100").isCloseTo(99.5, within(1e-9));
        assertThat(east[1]).isCloseTo(39.5, within(1e-9));
        assertThat(grid.lonLatToIndex(0.0, 60.0)[0]).isNaN();
        assertThat(grid.indexToLonLat(99.5, 39.5)[0]).isCloseTo(-170.0, within(1e-9));
    }

    @Test
    @DisplayName("missing or invalid parameters are refused")
    void refusals() {
        assertThatThrownBy(() -> GridDef.of(new GridDefinition(GridDefinition.GEOS, 10, 10, -10, -10, 10, 10,
                Map.of("lon0", 0.0)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("'h'");
        assertThatThrownBy(() -> GridDef.of(new GridDefinition(GridDefinition.GEOS, 10, 10, -10, -10, 10, 10,
                Map.of("lon0", 0.0, "h", 1.0, "x0", 0.0, "dx", 1.0, "y0", 0.0, "dy", -1.0, "sweep", "z"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GridDef.of(new GridDefinition(GridDefinition.AFFINE, 10, 10, -10, -10, 10, 10,
                Map.of("crs", "EPSG:32614", "gt0", 0, "gt1", 1, "gt2", 0, "gt3", 0, "gt4", 0, "gt5", -1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GridDef.of(new GridDefinition(GridDefinition.LCC, 10, 10, -10, -10, 10, 10,
                Map.of("lat1", "north")))).isInstanceOf(IllegalArgumentException.class);
    }
}
