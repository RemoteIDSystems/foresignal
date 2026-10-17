package ai.foresignal.forecast.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ForecastScorer} (prompt 122, LED-1): Brier, multi-category Brier, RPS, clipped log score, CRPS from quantiles
 * against hand-computed reference values, and the time-averaged Brier with carry-forward and the base-rate fill.
 */
class ForecastScorerTest {

    @Test
    @DisplayName("binary Brier and log score, clipped to [0.01, 0.99]")
    void binary() {
        assertThat(ForecastScorer.brier(0.8, true)).isCloseTo(0.04, within(1e-12));
        assertThat(ForecastScorer.brier(0.8, false)).isCloseTo(0.64, within(1e-12));
        assertThat(ForecastScorer.logScore(0.8, true)).isCloseTo(-Math.log(0.8), within(1e-12));
        assertThat(ForecastScorer.logScore(0.0, true)).isCloseTo(-Math.log(0.01), within(1e-12));
        assertThat(ForecastScorer.logScore(1.0, true)).isCloseTo(-Math.log(0.99), within(1e-12));
        assertThat(ForecastScorer.logScore(1.0, false)).isCloseTo(-Math.log(0.01), within(1e-12));
    }

    @Test
    @DisplayName("multi-category Brier sums over every label (0 to 2)")
    void multiBrier() {
        Map<String, Double> d = Map.of("a", 0.5, "b", 0.3, "c", 0.2);
        // (0.5-1)^2 + 0.3^2 + 0.2^2 = 0.25 + 0.09 + 0.04
        assertThat(ForecastScorer.multiBrier(d, "a", List.of("a", "b", "c"))).isCloseTo(0.38, within(1e-12));
        assertThat(ForecastScorer.multiBrier(Map.of("b", 1.0), "a", List.of("a", "b", "c"))).isCloseTo(2.0,
                within(1e-12));
    }

    @Test
    @DisplayName("RPS over cumulative probabilities, normalized by K − 1")
    void rps() {
        Map<String, Double> d = Map.of("low", 0.2, "mid", 0.5, "high", 0.3);
        // F = (0.2, 0.7); outcome high: O = (0, 0) → (0.04 + 0.49) / 2
        assertThat(ForecastScorer.rps(d, "high", List.of("low", "mid", "high"))).isCloseTo(0.265, within(1e-12));
        // outcome low: O = (1, 1) → (0.64 + 0.09) / 2
        assertThat(ForecastScorer.rps(d, "low", List.of("low", "mid", "high"))).isCloseTo(0.365, within(1e-12));
        assertThat(ForecastScorer.rps(Map.of("only", 1.0), "only", List.of("only"))).isZero();
    }

    @Test
    @DisplayName("pinball losses, CRPS as twice their mean, and interval hits")
    void crps() {
        Map<String, Double> q = new LinkedHashMap<>();
        q.put("p05", 0.0);
        q.put("p25", 5.0);
        q.put("p50", 10.0);
        q.put("p75", 15.0);
        q.put("p95", 20.0);
        Map<String, Double> losses = ForecastScorer.pinball(q, 12.0);
        assertThat(losses.get("p05")).isCloseTo(0.05 * 12, within(1e-12));
        assertThat(losses.get("p50")).isCloseTo(0.5 * 2, within(1e-12));
        assertThat(losses.get("p75")).isCloseTo(0.25 * 3, within(1e-12));
        assertThat(losses.get("p95")).isCloseTo(0.05 * 8, within(1e-12));
        double mean = (0.6 + 0.25 * 7 + 1.0 + 0.75 + 0.4) / 5;
        assertThat(ForecastScorer.crps(q, 12.0)).isCloseTo(2 * mean, within(1e-12));
        assertThat(ForecastScorer.intervalHits(q, 12.0)).containsEntry("p05p95", true).containsEntry("p25p75", true);
        assertThat(ForecastScorer.intervalHits(q, 25.0)).containsEntry("p05p95", false).containsEntry("p25p75", false);
        // A perfect point forecast scores zero.
        Map<String, Double> point = Map.of("p05", 3.0, "p25", 3.0, "p50", 3.0, "p75", 3.0, "p95", 3.0);
        assertThat(ForecastScorer.crps(point, 3.0)).isZero();
    }

    static final Instant OPEN = Instant.parse("2026-03-01T06:00:00Z");

    @Test
    @DisplayName("time-averaged Brier: one score per day, the standing forecast carried forward until superseded")
    void timeAveragedCarryForward() {
        // Open 2026-03-01 06:00 to 2026-03-04 06:00: days 1, 2, 3 and 4 (the last scored at 06:00).
        Instant end = Instant.parse("2026-03-04T06:00:00Z");
        List<ForecastScorer.Timed<Double>> own = List.of(
                new ForecastScorer.Timed<>(Instant.parse("2026-03-01T08:00:00Z"), 0.6),
                new ForecastScorer.Timed<>(Instant.parse("2026-03-03T12:00:00Z"), 0.9));
        ForecastScorer.TimeAveraged t = ForecastScorer.timeAveraged(own, List.of(), OPEN, end,
                (Double p) -> ForecastScorer.brier(p, true));
        // Day 1: 0.6, day 2: 0.6 (carried), day 3: 0.9, day 4: 0.9.
        double expected = (0.16 + 0.16 + 0.01 + 0.01) / 4;
        assertThat(t.nDays()).isEqualTo(4);
        assertThat(t.filledDays()).isZero();
        assertThat(t.score()).isCloseTo(expected, within(1e-12));
    }

    @Test
    @DisplayName("time-averaged Brier: days before the first own forecast use the base-rate forecaster's")
    void timeAveragedBaseRateFill() {
        Instant end = Instant.parse("2026-03-04T06:00:00Z");
        List<ForecastScorer.Timed<Double>> own = List.of(
                new ForecastScorer.Timed<>(Instant.parse("2026-03-03T01:00:00Z"), 0.9));
        List<ForecastScorer.Timed<Double>> fill = List.of(
                new ForecastScorer.Timed<>(Instant.parse("2026-03-01T06:30:00Z"), 0.3));
        ForecastScorer.TimeAveraged t = ForecastScorer.timeAveraged(own, fill, OPEN, end,
                (Double p) -> ForecastScorer.brier(p, false));
        // Days 1-2 base rate 0.3 (0.09 each), days 3-4 own 0.9 (0.81 each).
        assertThat(t.nDays()).isEqualTo(4);
        assertThat(t.filledDays()).isEqualTo(2);
        assertThat(t.score()).isCloseTo((0.09 * 2 + 0.81 * 2) / 4, within(1e-12));
        // Without any forecast nothing is scored.
        ForecastScorer.TimeAveraged none = ForecastScorer.timeAveraged(List.<ForecastScorer.Timed<Double>>of(),
                List.of(), OPEN, end, (Double p) -> 1.0);
        assertThat(none.nDays()).isZero();
        assertThat(none.score()).isNaN();
    }
}
