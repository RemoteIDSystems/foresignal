package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LccGrid;
import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Grid-relative to earth-relative winds on HRRR's tangent Lambert conformal grid (prompt 051):
 * {@code n = sin 38.5° = 0.622515}, {@code λ₀ = −97.5°}. Expected values were computed by hand
 * (Python {@code math}) from {@code α = n·(λ − λ₀)}, {@code u_e = u cos α + v sin α},
 * {@code v_e = −u sin α + v cos α}.
 */
class HrrrWindRotationTest {

    static final double N = 0.6225146366376195;

    /** A one-cell HRRR-parameter grid whose only cell is at {@code (lon, lat)}. */
    static LccGrid cellAt(double lon, double lat, int nx) {
        Map<String, Object> params = Map.of("lat1", 38.5, "lat2", 38.5, "lov", -97.5, "radius", 6_371_229.0,
                "la1", lat, "lo1", lon, "dx", 3000.0, "dy", 3000.0);
        return (LccGrid) GridDef.of(new GridDefinition(GridDefinition.LCC, nx, 1, -180, -90, 180, 90, params));
    }

    @Test
    @DisplayName("the cone constant and angle at HRRR's south-west corner (−122.719528°): α = −15.69953°")
    void angle() {
        assertThat(cellAt(-122.719528, 21.138123, 1).n()).isCloseTo(N, within(1e-12));
        assertThat(Math.toDegrees(WindRotation.angle(N, -97.5, -122.719528))).isCloseTo(-15.69952530909227,
                within(1e-9));
        assertThat(WindRotation.angle(N, -97.5, -97.5)).isZero();
        assertThat(Math.toDegrees(WindRotation.angle(N, -97.5, -72.0))).isCloseTo(15.8741232342593, within(1e-9));
    }

    @Test
    @DisplayName("grid north at the west edge points north-west: (0, 10) becomes (−2.70592, 9.62694)")
    void westEdge() {
        LccGrid grid = cellAt(-122.719528, 21.138123, 1);
        float[] u = {0f};
        float[] v = {10f};
        WindRotation.toEarthRelative(grid, u, v);
        assertThat(u[0]).isCloseTo(-2.7059247f, within(1e-4f));
        assertThat(v[0]).isCloseTo(9.6269399f, within(1e-4f));
        float[] u2 = {10f};
        float[] v2 = {0f};
        WindRotation.toEarthRelative(grid, u2, v2);
        assertThat(u2[0]).isCloseTo(9.6269399f, within(1e-4f));
        assertThat(v2[0]).isCloseTo(2.7059247f, within(1e-4f));
    }

    @Test
    @DisplayName("on the central meridian nothing turns; east of it the turn is the other way; speed is preserved")
    void centralAndEast() {
        float[] u = {3f};
        float[] v = {4f};
        WindRotation.toEarthRelative(cellAt(-97.5, 38.5, 1), u, v);
        assertThat(u[0]).isCloseTo(3f, within(1e-5f));
        assertThat(v[0]).isCloseTo(4f, within(1e-5f));
        double[] east = WindRotation.rotate(0, 10, WindRotation.angle(N, -97.5, -72.0));
        assertThat(east[0]).isCloseTo(2.735248349595375, within(1e-9));
        assertThat(east[1]).isCloseTo(9.61864940966432, within(1e-9));
        double[] any = WindRotation.rotate(-7.5, 2.25, WindRotation.angle(N, -97.5, -110.0));
        assertThat(Math.hypot(any[0], any[1])).isCloseTo(Math.hypot(-7.5, 2.25), within(1e-12));
    }

    @Test
    @DisplayName("each column turns by its own longitude")
    void perColumn() {
        LccGrid grid = cellAt(-122.719528, 21.138123, 3);
        float[] u = {0f, 0f, 0f};
        float[] v = {10f, 10f, 10f};
        WindRotation.toEarthRelative(grid, u, v);
        for (int i = 0; i < 3; i++) {
            double alpha = WindRotation.angle(N, -97.5, grid.indexToLonLat(i, 0)[0]);
            assertThat(u[i]).isCloseTo((float) (10 * Math.sin(alpha)), within(1e-4f));
        }
        assertThat(u[2]).as("further east, less turn").isGreaterThan(u[0]);
    }

    @Test
    @DisplayName("bit 5 of the resolution and component flags marks grid-relative winds (HRRR 0x08, GFS 0x30)")
    void flags() {
        assertThat(WindRotation.gridRelative(0x08)).isTrue();
        assertThat(WindRotation.gridRelative(0x30)).isFalse();
        assertThat(WindRotation.gridRelative(0x38)).isTrue();
    }

    @Test
    @DisplayName("arrays that do not fit the grid are refused")
    void sizes() {
        assertThatThrownBy(() -> WindRotation.toEarthRelative(cellAt(-97.5, 38.5, 2), new float[1], new float[2]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
