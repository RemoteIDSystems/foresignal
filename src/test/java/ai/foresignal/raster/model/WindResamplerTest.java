package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The God's Eye View wind grid (prompt 051): cases ported from God's Eye View
 * {@code src/data/windGrid.test.mjs} (MIT), plus the seam, the poles and the 1° output.
 */
class WindResamplerTest {

    static float[] fill(int n, float v) {
        float[] a = new float[n];
        Arrays.fill(a, v);
        return a;
    }

    @Test
    @DisplayName("resamples and wraps a wind grid (God's Eye View case)")
    void gevCase() {
        float[] u = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        WindResampler.Field r = WindResampler.resample(u, fill(12, 1), 4, 3, 0, 90, 90, 90, 90, 90);
        assertThat(r.nx()).isEqualTo(4);
        assertThat(r.ny()).isEqualTo(3);
        assertThat(r.u()[0]).isEqualTo(0f);
        assertThat(r.u()[3]).isEqualTo(3f);
        assertThat(r.u()[4]).isEqualTo(4f);
        assertThat(r.lo1()).isZero();
        assertThat(r.la1()).isEqualTo(90);
    }

    @Test
    @DisplayName("a 0.25° source becomes 360 × 181 at 1°: 521,280 bytes of planar u/v")
    void oneDegreeDims() {
        int ni = 1440;
        int nj = 721;
        float[] u = new float[ni * nj];
        float[] v = new float[ni * nj];
        for (int j = 0; j < nj; j++) {
            for (int i = 0; i < ni; i++) {
                u[j * ni + i] = i * 0.25f;
                v[j * ni + i] = 90 - j * 0.25f;
            }
        }
        WindResampler.Field r = WindResampler.resample(u, v, ni, nj, 0, 90, 0.25, 0.25, 1, 1);
        assertThat(r.nx()).isEqualTo(360);
        assertThat(r.ny()).isEqualTo(181);
        assertThat(r.byteLength()).isEqualTo(521_280);
        assertThat(r.u()[10]).as("column 10 is longitude 10").isCloseTo(10f, within(1e-4f));
        assertThat(r.v()[45 * 360]).as("row 45 is latitude 45").isCloseTo(45f, within(1e-4f));
    }

    @Test
    @DisplayName("longitude wraps across the seam: the last output column blends the last and first source columns")
    void seamWrap() {
        // Source at 90° steps starting at 45°: columns at 45, 135, 225, 315; longitude 0 lies between 315 and 45.
        float[] u = {10, 20, 30, 40, 10, 20, 30, 40, 10, 20, 30, 40};
        WindResampler.Field r = WindResampler.resample(u, fill(12, 0), 4, 3, 45, 90, 90, 90, 90, 90);
        assertThat(r.u()[0]).as("lon 0, halfway from 315 (40) to 45 (10)").isCloseTo(25f, within(1e-4f));
        assertThat(r.u()[1]).as("lon 90, halfway from 45 (10) to 135 (20)").isCloseTo(15f, within(1e-4f));
    }

    @Test
    @DisplayName("latitude clamps at the poles when the source stops short of them")
    void latitudeClamp() {
        // Two rows at 60 N and 60 S: output rows at 90 and −90 clamp to the source's first and last rows.
        float[] u = {1, 1, 1, 1, 9, 9, 9, 9};
        WindResampler.Field r = WindResampler.resample(u, fill(8, 0), 4, 2, 0, 60, 90, 120, 90, 90);
        assertThat(r.ny()).isEqualTo(3);
        assertThat(r.u()[0]).as("north pole clamps to 60 N").isEqualTo(1f);
        assertThat(r.u()[2 * 4]).as("south pole clamps to 60 S").isEqualTo(9f);
        assertThat(r.u()[4]).as("the equator is halfway").isCloseTo(5f, within(1e-4f));
    }

    @Test
    @DisplayName("invalid geometry, non-finite values and an oversized output are rejected (God's Eye View messages)")
    void rejected() {
        float[] twelve = fill(12, 1);
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 3, 3, 0, 90, 90, 90, 90, 90))
                .hasMessage("Invalid wind grid geometry");
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 4, 3, Double.NaN, 90, 90, 90, 90, 90))
                .hasMessage("Invalid wind grid geometry");
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 4, 3, 0, 90, 0, 90, 90, 90))
                .hasMessage("Invalid wind grid geometry");
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 4, 3, 0, 90, 90, 90, 0.1, 0.1))
                .hasMessage("Invalid wind grid geometry");
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 4, 3, 0, 90, 90, 90, 200, 90))
                .hasMessage("Invalid wind grid geometry");
        assertThatThrownBy(() -> WindResampler.resample(null, twelve, 4, 3, 0, 90, 90, 90, 90, 90))
                .hasMessage("Invalid wind grid geometry");
        float[] bad = fill(12, 1);
        bad[5] = Float.NaN;
        assertThatThrownBy(() -> WindResampler.resample(twelve, bad, 4, 3, 0, 90, 90, 90, 90, 90))
                .hasMessage("Invalid wind grid values");
        assertThatThrownBy(() -> WindResampler.resample(new float[2_000_001], new float[2_000_001], 2_000_001, 1, 0, 90,
                1, 1, 1, 1)).hasMessage("Invalid wind grid geometry");
        // dy = 0.25 with dx = 0.25 fits (1440 × 721); a finer row step than the budget allows does not.
        assertThat(WindResampler.resample(twelve, twelve, 4, 3, 0, 90, 90, 90, 0.25, 0.25).nx()).isEqualTo(1440);
        assertThatThrownBy(() -> WindResampler.resample(twelve, twelve, 4, 3, 0, 90, 90, 90, 0.25, 0.249))
                .hasMessage("Invalid wind grid geometry");
    }
}
