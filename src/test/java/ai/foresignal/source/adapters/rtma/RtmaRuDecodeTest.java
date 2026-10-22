package ai.foresignal.source.adapters.rtma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.GridDefinition;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RTMA-RU keys, analysis times and wind rotation (prompt 106 task 3, post-review fix). The key grammar
 * is the live one verified 2026-10-16 ({@code rtma2p5_ru.20261016/rtma2p5_ru.t1945z.2dvaranl_ndfd.grb2},
 * analyses every 15 minutes); the rotation case uses HRRR-like Lambert parameters with values computed by
 * hand from {@code α = n·(λ − λ₀)}, as {@code HrrrWindRotationTest} does.
 */
class RtmaRuDecodeTest {

    @Test
    @DisplayName("the object key is the 15-minute RTMA-RU analysis, not the hourly plain RTMA")
    void objectKey() {
        assertThat(RtmaAdapter.ModelKeys.objectKey(RtmaAdapter.TEMPLATE, Instant.parse("2026-10-16T19:45:00Z")))
                .isEqualTo("rtma2p5_ru.20261016/rtma2p5_ru.t1945z.2dvaranl_ndfd.grb2");
        assertThat(RtmaAdapter.ModelKeys.objectKey(RtmaAdapter.TEMPLATE, Instant.parse("2026-10-16T00:00:00Z")))
                .isEqualTo("rtma2p5_ru.20261016/rtma2p5_ru.t0000z.2dvaranl_ndfd.grb2");
    }

    @Test
    @DisplayName("candidates are the last eight quarter hours, oldest first, across midnight")
    void candidates() {
        List<Instant> c = RtmaAdapter.candidates(Instant.parse("2026-10-16T00:20:00Z"), RtmaAdapter.DEFAULT_EVERY);
        assertThat(c).hasSize(RtmaAdapter.CANDIDATES);
        assertThat(c.get(c.size() - 1)).isEqualTo(Instant.parse("2026-10-16T00:15:00Z"));
        assertThat(c.get(0)).isEqualTo(Instant.parse("2026-10-15T22:30:00Z"));
        assertThat(RtmaAdapter.candidates(Instant.parse("2026-10-16T12:59:00Z"), Duration.ofMinutes(30)))
                .endsWith(Instant.parse("2026-10-16T12:30:00Z"));
    }

    @Test
    @DisplayName("analysisEvery accepts 15, 30 and 60 minutes and falls back to 15 otherwise")
    void analysisEvery() {
        assertThat(RtmaAdapter.analysisEvery(null)).isEqualTo(Duration.ofMinutes(15));
        assertThat(RtmaAdapter.analysisEvery(Duration.ofMinutes(30))).isEqualTo(Duration.ofMinutes(30));
        assertThat(RtmaAdapter.analysisEvery(Duration.ofHours(1))).isEqualTo(Duration.ofHours(1));
        assertThat(RtmaAdapter.analysisEvery(Duration.ofMinutes(20))).isEqualTo(Duration.ofMinutes(15));
        assertThat(RtmaAdapter.analysisEvery(Duration.ofMinutes(45))).isEqualTo(Duration.ofMinutes(15));
        assertThat(RtmaAdapter.analysisEvery(Duration.ofSeconds(90))).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("the state key carries minutes, so 19:30 and 19:45 are different analyses")
    void cycleKey() {
        assertThat(RtmaAdapter.cycleKey(Instant.parse("2026-10-16T19:45:00Z"))).isEqualTo("202610161945");
        assertThat(RtmaAdapter.cycleKey(Instant.parse("2026-10-16T19:30:00Z")))
                .isLessThan(RtmaAdapter.cycleKey(Instant.parse("2026-10-16T19:45:00Z")));
    }

    @Test
    @DisplayName("grid-relative winds on a Lambert grid are rotated to earth-relative")
    void rotatesOnLambert() {
        Map<String, Object> params = Map.of("lat1", 25.0, "lat2", 25.0, "lov", -95.0, "radius", 6_371_200.0,
                "la1", 40.0, "lo1", -120.0, "dx", 2539.703, "dy", 2539.703);
        GridDefinition grid = new GridDefinition(GridDefinition.LCC, 1, 1, -180, -90, 180, 90, params);
        float[] u = {0f};
        float[] v = {10f};
        RtmaAdapter.rotateToEarth(grid, List.of(u, v));
        double alpha = Math.sin(Math.toRadians(25.0)) * Math.toRadians(-120.0 - -95.0);
        assertThat(u[0]).isCloseTo((float) (10 * Math.sin(alpha)), within(1e-3f));
        assertThat(v[0]).isCloseTo((float) (10 * Math.cos(alpha)), within(1e-3f));
        assertThat(Math.hypot(u[0], v[0])).isCloseTo(10.0, within(1e-4));
    }

    @Test
    @DisplayName("grid-relative winds on a non-Lambert grid are refused, not stored unrotated")
    void refusesOtherGrids() {
        GridDefinition latlon = new GridDefinition(GridDefinition.LATLON, 1, 1, -100, 30, -99, 31, Map.of());
        assertThatThrownBy(() -> RtmaAdapter.rotateToEarth(latlon, List.of(new float[] {1f}, new float[] {1f})))
                .isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("the descriptor states the 15-minute cadence and the measured budget")
    void descriptor() {
        var d = new RtmaAdapter(null, null, "", "small").descriptor();
        assertThat(d.param(RtmaAdapter.ANALYSIS_EVERY)).isNotNull();
        assertThat(d.defaultBudget().mbPerDay()).isEqualTo(4_500);
        assertThat(d.products()).extracting(p -> p.temporal().cadence()).containsOnly(Duration.ofMinutes(15));
    }
}
