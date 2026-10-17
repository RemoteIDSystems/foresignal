package ai.foresignal.forecast.ensemble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastScoreEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The ensemble's arithmetic and gates (prompt 122 §6): log-odds weighting, shrinkage toward equal weights, LLM caps,
 * quantile averaging, the Platt fit on synthetic miscalibrated forecasts, relative Brier scores, the change rule and
 * the extremizing gate.
 */
class EnsembleTest {

    @Test
    @DisplayName("equal weights pool by the mean of log-odds, capped to [0.01, 0.99]")
    void logOddsPool() {
        double[] p = {0.8, 0.5};
        double[] w = {0.5, 0.5};
        double expected = EnsembleMath.sigmoid(0.5 * Math.log(0.8 / 0.2));
        assertThat(EnsembleMath.combineBinary(p, w, 1.0)).isCloseTo(expected, within(1e-12));
        assertThat(EnsembleMath.combineBinary(new double[] {1.0, 1.0}, w, 1.0)).isEqualTo(0.99);
        // Extremizing with a > 1 pushes away from one half.
        assertThat(EnsembleMath.combineBinary(p, w, 2.0)).isGreaterThan(expected);
    }

    @Test
    @DisplayName("weights favor a lower relative Brier and shrink toward equal by n ÷ (n + 20)")
    void weightsAndShrinkage() {
        double[] none = EnsembleMath.weights(List.of(new EnsembleMath.Track(-0.1, 0), new EnsembleMath.Track(0.1, 0)),
                2, 20);
        assertThat(none[0]).isCloseTo(0.5, within(1e-12));
        double[] some = EnsembleMath.weights(List.of(new EnsembleMath.Track(-0.1, 20),
                new EnsembleMath.Track(0.1, 20)), 2, 20);
        double raw = Math.exp(0.2) / (Math.exp(0.2) + Math.exp(-0.2));
        assertThat(some[0]).isCloseTo(0.5 * raw + 0.25, within(1e-12));
        double[] many = EnsembleMath.weights(List.of(new EnsembleMath.Track(-0.1, 100000),
                new EnsembleMath.Track(0.1, 100000)), 2, 20);
        assertThat(many[0]).isCloseTo(raw, within(1e-3));
        assertThat(some[0] + some[1]).isCloseTo(1.0, within(1e-12));
    }

    @Test
    @DisplayName("LLM forecasts are clamped to [0.03, 0.97]; distributions renormalize per label")
    void capsAndDistributions() {
        assertThat(EnsembleMath.capLlm(0.999)).isEqualTo(0.97);
        assertThat(EnsembleMath.capLlm(0.0)).isEqualTo(0.03);
        assertThat(EnsembleMath.capLlm(0.5)).isEqualTo(0.5);
        Map<String, Double> d = EnsembleMath.combineDistribution(List.of(Map.of("a", 0.6, "b", 0.4),
                Map.of("a", 0.2, "b", 0.8)), new double[] {0.5, 0.5}, List.of("a", "b"));
        assertThat(d.get("a") + d.get("b")).isCloseTo(1.0, within(1e-12));
    }

    @Test
    @DisplayName("quantiles average per level and stay monotone; only levels every member has are kept")
    void quantiles() {
        Map<String, Double> q = EnsembleMath.combineQuantiles(List.of(
                Map.of("p05", 1.0, "p25", 2.0, "p50", 3.0, "p75", 4.0, "p95", 5.0, "p10", 1.5),
                Map.of("p05", 3.0, "p25", 4.0, "p50", 5.0, "p75", 6.0, "p95", 7.0)), new double[] {0.5, 0.5});
        assertThat(q).containsOnlyKeys("p05", "p25", "p50", "p75", "p95");
        assertThat(q.get("p50")).isCloseTo(4.0, within(1e-12));
    }

    @Test
    @DisplayName("Platt recovers the miscalibration of synthetic overconfident forecasts")
    void plattFit() {
        Random r = new Random(7);
        int n = 4000;
        double[] p = new double[n];
        double[] o = new double[n];
        for (int i = 0; i < n; i++) {
            double truth = 0.05 + 0.9 * r.nextDouble();
            // The forecaster doubles the log-odds (overconfident): logit p = 2 logit truth.
            p[i] = EnsembleMath.sigmoid(2 * EnsembleMath.logit(truth));
            o[i] = r.nextDouble() < truth ? 1 : 0;
        }
        double[] ab = EnsembleMath.fitPlatt(p, o);
        assertThat(ab[0]).isCloseTo(0.5, within(0.08));
        assertThat(ab[1]).isCloseTo(0.0, within(0.1));
        assertThat(EnsembleMath.platt(1, 0, 0.3)).isCloseTo(0.3, within(1e-9));
    }

    @Test
    @DisplayName("the extremizing fit and the gate: ≥ 100 resolutions, ≥ 5 types, positive gain and a > 1")
    void extremizing() {
        Random r = new Random(3);
        int n = 2000;
        double[] x = new double[n];
        double[] o = new double[n];
        for (int i = 0; i < n; i++) {
            double truthLogit = (r.nextDouble() - 0.5) * 6;
            x[i] = truthLogit / 2;
            o[i] = r.nextDouble() < EnsembleMath.sigmoid(truthLogit) ? 1 : 0;
        }
        double a = EnsembleMath.fitExtremizing(x, o);
        assertThat(a).isCloseTo(2.0, within(0.3));
        double gain = EnsembleMath.meanLogScore(x, o, 1.0) - EnsembleMath.meanLogScore(x, o, a);
        assertThat(gain).isPositive();
        assertThat(EnsembleService.gate(120, 5, gain, a)).isTrue();
        assertThat(EnsembleService.gate(99, 5, gain, a)).isFalse();
        assertThat(EnsembleService.gate(120, 4, gain, a)).isFalse();
        assertThat(EnsembleService.gate(120, 5, -0.01, a)).isFalse();
        assertThat(EnsembleService.gate(120, 5, gain, 0.9)).isFalse();
        assertThat(EnsembleService.gate(120, 5, null, a)).isFalse();
    }

    @Test
    @DisplayName("relative Brier against the per-question median, ignoring the ensemble")
    void relativeBrier() {
        List<ForecastScoreEntity> rows = new ArrayList<>();
        Instant at = Instant.parse("2026-06-01T00:00:00Z");
        String[] ids = {"a", "b", "c", "ensemble"};
        double[] briers = {0.1, 0.2, 0.4, 0.0};
        for (int i = 0; i < ids.length; i++) {
            ForecastScoreEntity s = new ForecastScoreEntity("q1", ids[i]);
            s.setBrier(briers[i]);
            s.setResolvedAt(at);
            rows.add(s);
        }
        Map<String, double[]> rel = EnsembleService.relativeBrier(rows);
        assertThat(rel).doesNotContainKey("ensemble");
        assertThat(rel.get("a")[0]).isCloseTo(-0.1, within(1e-12));
        assertThat(rel.get("c")[0]).isCloseTo(0.2, within(1e-12));
        assertThat(rel.get("b")[1]).isEqualTo(1.0);
    }

    @Test
    @DisplayName("a new ensemble forecast only when members change or the value moves by 0.005 or more")
    void changeRule() {
        ForecastEntity previous = new ForecastEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "01JA8Q3V7S2K9W4T6Y1B5N0M3D",
                "ensemble", "ensemble", Instant.parse("2026-06-01T00:00:00Z"), 1);
        previous.setProbability(0.40);
        previous.setInputs(Map.of("members", List.of(Map.of("forecastId", "f1"))));
        List<Map<String, Object>> same = List.of(Map.of("forecastId", "f1"));
        assertThat(EnsembleService.changed(previous, ForecastValue.binary(0.403), same)).isFalse();
        assertThat(EnsembleService.changed(previous, ForecastValue.binary(0.41), same)).isTrue();
        assertThat(EnsembleService.changed(previous, ForecastValue.binary(0.40), List.of(Map.of("forecastId", "f2"))))
                .isTrue();
    }
}
