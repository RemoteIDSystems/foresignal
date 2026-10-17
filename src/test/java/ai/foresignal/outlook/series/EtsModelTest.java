package ai.foresignal.outlook.series;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden values of the ETS state-space forms (prompt 126): the ETS(A,N,N) and ETS(A,A,A) recursions with fixed
 * parameters against hand computation, the variance formula of Hyndman et al. (2008, Table 6.1), and maximum
 * likelihood recovering the smoothing parameter of a simulated series.
 */
class EtsModelTest {

    static final EtsModel.Spec ANN = new EtsModel.Spec(EtsModel.Trend.NONE, 1);

    @Test
    @DisplayName("ETS(A,N,N) filter: l(t) = l(t−1) + α·e(t), SSE and the flat forecast (hand-computed)")
    void annFilter() {
        // α = 0.5, l0 = 10; y = 12, 11, 15.
        // e1 = 2, l1 = 11; e2 = 0, l2 = 11; e3 = 4, l3 = 13; SSE = 4 + 0 + 16 = 20.
        EtsModel.Params p = new EtsModel.Params(0.5, 0, 0, 1, 10, 0, new double[0]);
        EtsModel.State s = EtsModel.filter(new double[] {12, 11, 15}, ANN, p);
        assertThat(s.sse()).isCloseTo(20, within(1e-12));
        assertThat(s.level()).isCloseTo(13, within(1e-12));
        for (int h = 1; h <= 5; h++) {
            assertThat(EtsModel.mean(ANN, p, s, h)).isCloseTo(13, within(1e-12));
        }
    }

    @Test
    @DisplayName("ETS(A,N,N) variance: σ²·(1 + α²·(h − 1))")
    void annVariance() {
        EtsModel.Params p = new EtsModel.Params(0.4, 0, 0, 1, 0, 0, new double[0]);
        for (int h = 1; h <= 10; h++) {
            assertThat(EtsModel.variance(ANN, p, 2.0, h)).isCloseTo(2.0 * (1 + 0.16 * (h - 1)), within(1e-12));
        }
    }

    @Test
    @DisplayName("ETS(A,A,A) filter and forecast: one step by hand, with trend and season")
    void aaaFilter() {
        EtsModel.Spec spec = new EtsModel.Spec(EtsModel.Trend.ADDITIVE, 2);
        // α = 0.5, β = 0.1, γ = 0.2, l0 = 10, b0 = 1, s0 = (+1, −1).
        EtsModel.Params p = new EtsModel.Params(0.5, 0.1, 0.2, 1, 10, 1, new double[] {1, -1});
        // t = 0: ŷ = 10 + 1 + 1 = 12, y = 13, e = 1 → l = 11.5, b = 1.1, s0 = 1.2.
        EtsModel.State s = EtsModel.filter(new double[] {13}, spec, p);
        assertThat(s.sse()).isCloseTo(1, within(1e-12));
        assertThat(s.level()).isCloseTo(11.5, within(1e-12));
        assertThat(s.trend()).isCloseTo(1.1, within(1e-12));
        assertThat(s.season()[0]).isCloseTo(1.2, within(1e-12));
        // h = 1 uses slot 1: 11.5 + 1.1 − 1 = 11.6; h = 2 slot 0: 11.5 + 2.2 + 1.2 = 14.9.
        assertThat(EtsModel.mean(spec, p, s, 1)).isCloseTo(11.6, within(1e-12));
        assertThat(EtsModel.mean(spec, p, s, 2)).isCloseTo(14.9, within(1e-12));
        // Variance h = 3: c1 = α + β·1 = 0.6; c2 = α + β·2 + γ (2 is a multiple of m) = 0.9.
        assertThat(EtsModel.variance(spec, p, 1.0, 3)).isCloseTo(1 + 0.36 + 0.81, within(1e-12));
    }

    @Test
    @DisplayName("damped trend: φ_h = φ + … + φ^h in the mean and in c_j")
    void damped() {
        EtsModel.Spec spec = new EtsModel.Spec(EtsModel.Trend.DAMPED, 1);
        EtsModel.Params p = new EtsModel.Params(0.3, 0.1, 0, 0.9, 0, 0, new double[0]);
        assertThat(EtsModel.dampSum(spec, p, 3)).isCloseTo(0.9 + 0.81 + 0.729, within(1e-12));
        EtsModel.State s = new EtsModel.State(0, 5, 2, new double[1], 10);
        assertThat(EtsModel.mean(spec, p, s, 2)).isCloseTo(5 + 2 * (0.9 + 0.81), within(1e-12));
        double c1 = 0.3 + 0.1 * 0.9;
        assertThat(EtsModel.variance(spec, p, 1.0, 2)).isCloseTo(1 + c1 * c1, within(1e-12));
    }

    @Test
    @DisplayName("maximum likelihood recovers α of a simulated ETS(A,N,N) series and gives nominal-width intervals")
    void annMle() {
        Random random = new Random(7);
        double alpha = 0.3;
        double level = 50;
        double[] y = new double[600];
        for (int t = 0; t < y.length; t++) {
            double e = random.nextGaussian();
            y[t] = level + e;
            level += alpha * e;
        }
        EtsModel.Fit fit = EtsModel.fit(y, ANN, true);
        assertThat(fit.params().alpha()).isCloseTo(alpha, within(0.08));
        assertThat(fit.sigma2()).isCloseTo(1.0, within(0.15));
        double[] q = EtsModel.quantiles(fit, 1, false);
        assertThat(q[QuantileSet.P95] - q[QuantileSet.P05]).isCloseTo(2 * QuantileSet.z(0.95)
                * Math.sqrt(fit.sigma2()), within(1e-9));
        for (int i = 1; i < q.length; i++) {
            assertThat(q[i]).isGreaterThanOrEqualTo(q[i - 1]);
        }
    }

    @Test
    @DisplayName("ETS(A,A,A) fit on a trending weekly series forecasts the season and the trend")
    void aaaFit() {
        double[] y = new double[140];
        int[] pattern = {0, 3, 5, 4, 2, -6, -8};
        Random random = new Random(3);
        for (int t = 0; t < y.length; t++) {
            y[t] = 100 + 0.2 * t + pattern[t % 7] + 0.3 * random.nextGaussian();
        }
        EtsModel.Fit fit = EtsModel.fit(y, new EtsModel.Spec(EtsModel.Trend.ADDITIVE, 7), false);
        for (int h = 1; h <= 7; h++) {
            int t = y.length - 1 + h;
            assertThat(fit.mean(h)).isCloseTo(100 + 0.2 * t + pattern[t % 7], within(1.5));
        }
    }

    @Test
    @DisplayName("count series: fitted on log1p and back-transformed, never below zero")
    void counts() {
        double[] y = new double[60];
        for (int t = 0; t < y.length; t++) {
            y[t] = t % 5 == 0 ? 0 : 3;
        }
        ForecastMethod.MethodForecast f = new EtsAnn().forecast(y, 7, true, new int[] {1, 28});
        for (double q : f.quantiles().get(28)) {
            assertThat(q).isGreaterThanOrEqualTo(0);
        }
        assertThat(f.params()).containsEntry("scale", "log1p");
    }
}
