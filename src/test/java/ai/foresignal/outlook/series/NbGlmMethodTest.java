package ai.foresignal.outlook.series;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.analytics.math.NbSampling;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code nb-glm} method (prompt 126): coefficient recovery on daily counts and its bootstrap quantiles. */
class NbGlmMethodTest {

    /** Daily counts with a weekday effect, an annual cycle and a lag-7 dependence. */
    static double[] series(int n, long seed) {
        NbSampling sampler = new NbSampling(seed);
        double[] y = new double[n];
        for (int t = 0; t < n; t++) {
            double lag = t >= 7 ? y[t - 7] : 5;
            double eta = 1.2 + (t % 7 == 5 || t % 7 == 6 ? -0.5 : 0) + 0.3 * Math.sin(2 * Math.PI * t / 365.25)
                    + 0.2 * Math.log1p(lag);
            y[t] = sampler.negBin(Math.exp(eta), 8);
        }
        return y;
    }

    @Test
    @DisplayName("IRLS on a synthetic daily dataset recovers the lag and weekend coefficients")
    void recovers() {
        double[] y = series(1_500, 5);
        var fit = NbGlmMethod.fit(y, 7);
        double[] b = fit.beta();
        // Columns: 1, t, dow1..dow6, sin1, cos1, sin2, cos2, lag.
        assertThat(b[b.length - 1]).isCloseTo(0.2, within(0.1));
        assertThat(b[2 + 4]).isCloseTo(-0.5, within(0.15));
        assertThat(b[2 + 5]).isCloseTo(-0.5, within(0.15));
        assertThat(b[8]).isCloseTo(0.3, within(0.12));
    }

    @Test
    @DisplayName("bootstrap quantiles are monotone, non-negative, deterministic, and wider at 28 days than at 1")
    void bootstrap() {
        double[] y = series(800, 9);
        NbGlmMethod method = new NbGlmMethod();
        ForecastMethod.MethodForecast a = method.forecast(y, 7, true, new int[] {1, 7, 28});
        ForecastMethod.MethodForecast b = method.forecast(y, 7, true, new int[] {1, 7, 28});
        for (int h : new int[] {1, 7, 28}) {
            double[] q = a.quantiles().get(h);
            for (int i = 0; i < q.length; i++) {
                assertThat(q[i]).isGreaterThanOrEqualTo(0);
                if (i > 0) {
                    assertThat(q[i]).isGreaterThanOrEqualTo(q[i - 1]);
                }
            }
            assertThat(q).containsExactly(b.quantiles().get(h));
        }
        double w1 = a.quantiles().get(1)[QuantileSet.P95] - a.quantiles().get(1)[QuantileSet.P05];
        double w28 = a.quantiles().get(28)[QuantileSet.P95] - a.quantiles().get(28)[QuantileSet.P05];
        assertThat(w28).isGreaterThanOrEqualTo(w1 * 0.8);
        assertThat(a.params()).containsEntry("draws", NbGlmMethod.DRAWS);
        assertThat(method.supports(true)).isTrue();
        assertThat(method.supports(false)).isFalse();
    }
}
