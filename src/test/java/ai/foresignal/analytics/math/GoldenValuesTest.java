package ai.foresignal.analytics.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.forecast.ensemble.EnsembleMath;
import ai.foresignal.forecast.score.Calibration;
import ai.foresignal.forecast.score.ForecastScorer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden values for the statistics the intelligence layer relies on (checkpoint 124, focus area 1).
 *
 * <p>Every expected number below was computed independently of this code base, on 2026-10-17, with a separate
 * Python 3 standard-library implementation written from the published definitions (no shared code): the Poisson and
 * Gaussian CUSUM recursions (Page 1954; the Poisson log-likelihood-ratio form), the Adams–MacKay (2007) run-length
 * recursion with the Poisson–Gamma negative-binomial predictive, the Benjamini–Hochberg (1995) and
 * Benjamini–Yekutieli (2001) step-up adjustments, the Wilson (1927) score interval at z = 1.959963984540054, the Brier
 * (1950), log and ranked probability (Epstein 1969, normalized by K − 1) scores, the CRPS as twice the mean pinball
 * loss over the stored quantile levels (Gneiting and Raftery 2007), the Murphy (1973) decomposition over ten bins with
 * its within-bin terms (Stephenson et al. 2008), pool-adjacent-violators isotonic regression with tied forecasts
 * pooled first and the CORP decomposition (Dimitriadis, Gneiting and Jordan 2021), and Platt (1999) scaling fitted by
 * Newton–Raphson on the Bernoulli log likelihood. The scripts' printed values are pasted as literals.
 */
class GoldenValuesTest {

    private static final double EPS = 1e-12;

    @Test
    @DisplayName("CUSUM: Poisson and Gaussian alarms, change indexes and final statistic match an independent recursion")
    void cusum() {
        double[] x = {3, 2, 4, 8, 9, 7, 10, 3, 2, 9, 11};
        double[] mu0 = new double[x.length];
        java.util.Arrays.fill(mu0, 4.0);
        Cusum.Run poisson = Cusum.run(x, mu0, null, null, new Cusum.Params(Cusum.Model.POISSON, 1, 0.5, 1.0, 10), 3.0);
        assertThat(poisson.alarms()).containsExactly(new Cusum.Alarm(5, 3), new Cusum.Alarm(10, 6));
        assertThat(poisson.finalS()).isZero();

        double[] z = {0.1, -0.3, 1.6, 2.2, 1.9, 2.5, -0.2};
        double[] zero = new double[z.length];
        double[] one = new double[z.length];
        java.util.Arrays.fill(one, 1.0);
        Cusum.Run gaussian = Cusum.run(z, zero, one, null, new Cusum.Params(Cusum.Model.GAUSSIAN, 1, 0.5, 1.0, 10),
                4.0);
        assertThat(gaussian.alarms()).containsExactly(new Cusum.Alarm(4, 2));
        assertThat(gaussian.finalS()).isCloseTo(1.3, within(1e-12));
        assertThat(gaussian.openChangeIndex()).isEqualTo(5);
        assertThat(Cusum.referenceValue(4.0, 6.0)).isCloseTo(2.0 / Math.log(1.5), within(EPS));
    }

    @Test
    @DisplayName("BOCPD: the run-length posterior matches an independent Adams–MacKay recursion")
    void bocpd() {
        Bocpd.Result r = Bocpd.run(new double[] {1, 2, 3, 8, 9, 7},
                new Bocpd.Params(Bocpd.Model.POISSON_GAMMA, 0.1, 300, 2.0, 1.0, 1.0));
        double[] expected = {0.1, 0.014265889726902879, 0.02314057710759028, 0.4534276060062159, 0.24727809937146722,
            0.06917271801380472, 0.09271510977401902};
        assertThat(r.posterior()).hasSize(expected.length);
        for (int i = 0; i < expected.length; i++) {
            assertThat(r.posterior()[i]).as("P(r = %d)", i).isCloseTo(expected[i], within(1e-9));
        }
        assertThat(r.changeWithin(3)).isCloseTo(0.13740646683449317, within(1e-9));
        assertThat(r.mapRunLength()).isEqualTo(3);
    }

    @Test
    @DisplayName("Benjamini–Hochberg and Benjamini–Yekutieli adjusted p-values match an independent step-up")
    void falseDiscovery() {
        double[] p = {0.01, 0.04, 0.03, 0.005, 0.20, 0.5};
        assertThat(MultipleTesting.benjaminiHochberg(p))
                .containsExactly(new double[] {0.03, 0.06, 0.06, 0.03, 0.24, 0.5}, within(1e-12));
        assertThat(MultipleTesting.benjaminiYekutieli(p))
                .containsExactly(new double[] {0.0735, 0.147, 0.147, 0.0735, 0.588, 1.0}, within(1e-12));
    }

    @Test
    @DisplayName("Wilson score interval of 7/20 matches the closed form")
    void wilson() {
        double[] ci = Wilson.interval(7, 20);
        assertThat(ci[0]).isCloseTo(0.18119182410108206, within(EPS));
        assertThat(ci[1]).isCloseTo(0.5671457233147638, within(EPS));
    }

    @Test
    @DisplayName("Brier, log (clipped), RPS and quantile CRPS match their definitions")
    void scores() {
        assertThat(ForecastScorer.brier(0.7, true)).isCloseTo(0.09, within(EPS));
        assertThat(ForecastScorer.brier(0.2, false)).isCloseTo(0.04, within(EPS));
        assertThat(ForecastScorer.logScore(0.7, true)).isCloseTo(0.35667494393873245, within(EPS));
        assertThat(ForecastScorer.logScore(0.2, false)).isCloseTo(0.2231435513142097, within(EPS));
        assertThat(ForecastScorer.logScore(1.0, true)).as("clipped at 0.99").isCloseTo(0.01005033585350145, within(EPS));
        Map<String, Double> dist = new LinkedHashMap<>();
        dist.put("a", 0.1);
        dist.put("b", 0.2);
        dist.put("c", 0.3);
        dist.put("d", 0.4);
        assertThat(ForecastScorer.rps(dist, "c", List.of("a", "b", "c", "d"))).isCloseTo(0.08666666666666666,
                within(EPS));
        Map<String, Double> q = new LinkedHashMap<>();
        q.put("p05", 1.0);
        q.put("p10", 2.0);
        q.put("p25", 4.0);
        q.put("p50", 5.0);
        q.put("p75", 7.0);
        q.put("p90", 9.0);
        q.put("p95", 10.0);
        assertThat(ForecastScorer.crps(q, 6.0)).isCloseTo(0.6857142857142858, within(EPS));
    }

    static final double[] P = {0.05, 0.15, 0.15, 0.35, 0.55, 0.55, 0.75, 0.95, 0.95, 0.92};
    static final double[] O = {0, 0, 1, 0, 1, 0, 1, 1, 1, 0};

    @Test
    @DisplayName("Murphy decomposition with within-bin terms matches the independent computation and sums to the Brier")
    void murphy() {
        Calibration.Murphy m = Calibration.murphy(P, O);
        assertThat(m.brier()).isCloseTo(0.22889, within(1e-12));
        assertThat(m.reliability()).isCloseTo(0.06616333333333332, within(1e-12));
        assertThat(m.resolution()).isCloseTo(0.08333333333333333, within(1e-12));
        assertThat(m.uncertainty()).isCloseTo(0.25, within(1e-12));
        assertThat(m.withinBinVariance()).isCloseTo(5.9999999999999656e-05, within(1e-12));
        assertThat(m.withinBinCovariance()).isCloseTo(0.002, within(1e-12));
        assertThat(m.reliability() - m.resolution() + m.uncertainty() + m.withinBinVariance()
                - 2 * m.withinBinCovariance()).isCloseTo(m.brier(), within(1e-12));
    }

    @Test
    @DisplayName("PAV isotonic recalibration and the CORP decomposition match the independent computation")
    void corpAndPav() {
        Calibration.Corp c = Calibration.corp(P, O);
        Map<Double, Double> expected = Map.of(0.05, 0.0, 0.15, 1.0 / 3, 0.35, 1.0 / 3, 0.55, 0.5, 0.75, 0.5, 0.92, 0.5,
                0.95, 1.0);
        assertThat(c.curve()).hasSize(expected.size());
        for (Calibration.Point point : c.curve()) {
            assertThat(point.calibrated()).as("PAV at %s", point.forecast())
                    .isCloseTo(expected.get(point.forecast()), within(1e-12));
        }
        assertThat(c.score()).isCloseTo(0.22889, within(1e-12));
        assertThat(c.miscalibration()).isCloseTo(0.0622233333333333, within(1e-12));
        assertThat(c.discrimination()).isCloseTo(0.08333333333333331, within(1e-12));
        assertThat(c.uncertainty()).isCloseTo(0.25, within(1e-12));
        assertThat(c.miscalibration() - c.discrimination() + c.uncertainty()).isCloseTo(c.score(), within(1e-12));
    }

    @Test
    @DisplayName("Platt scaling fitted by maximum likelihood matches an independent Newton–Raphson fit")
    void platt() {
        double[] p = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.3, 0.6, 0.8};
        double[] o = {0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1};
        double[] ab = EnsembleMath.fitPlatt(p, o);
        // The production fit adds a 1e-6 ridge toward the identity; the reference has none.
        assertThat(ab[0]).isCloseTo(1.837439297987387, within(1e-4));
        assertThat(ab[1]).isCloseTo(0.4622039937200247, within(1e-4));
        assertThat(EnsembleMath.platt(ab[0], ab[1], 0.5)).isCloseTo(0.6135368943003561, within(1e-4));
    }
}
