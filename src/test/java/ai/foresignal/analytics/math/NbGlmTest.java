package ai.foresignal.analytics.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The IRLS negative-binomial regression (prompt 126) on synthetic data with known coefficients. */
class NbGlmTest {

    /** A synthetic design: intercept, a uniform covariate and a binary one, with NB(μ, θ) counts. */
    static double[][] design(int n, long seed) {
        java.util.Random random = new java.util.Random(seed);
        double[][] x = new double[n][];
        for (int i = 0; i < n; i++) {
            x[i] = new double[] {1, random.nextDouble() * 2 - 1, random.nextBoolean() ? 1 : 0};
        }
        return x;
    }

    static double[] counts(double[][] x, double[] beta, double theta, long seed) {
        NbSampling sampler = new NbSampling(seed);
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            double eta = 0;
            for (int j = 0; j < beta.length; j++) {
                eta += beta[j] * x[i][j];
            }
            y[i] = sampler.negBin(Math.exp(eta), theta);
        }
        return y;
    }

    @Test
    @DisplayName("IRLS recovers the coefficients and the dispersion of a synthetic NB dataset within tolerance")
    void recovers() {
        double[] beta = {1.5, 0.8, -0.5};
        double[][] x = design(4_000, 11);
        double[] y = counts(x, beta, 3.0, 12);
        NbGlm.Fit fit = NbGlm.fit(x, y);
        assertThat(fit.converged()).isTrue();
        assertThat(fit.beta()[0]).isCloseTo(1.5, within(0.06));
        assertThat(fit.beta()[1]).isCloseTo(0.8, within(0.06));
        assertThat(fit.beta()[2]).isCloseTo(-0.5, within(0.06));
        assertThat(fit.theta()).isCloseTo(3.0, within(0.6));
        for (int j = 0; j < 3; j++) {
            assertThat(fit.standardError(j)).isPositive().isLessThan(0.1);
        }
    }

    @Test
    @DisplayName("Poisson counts give the maximum dispersion (a Poisson fit)")
    void poisson() {
        double[] beta = {0.7, 0.4, 0.2};
        double[][] x = design(3_000, 21);
        double[] y = counts(x, beta, NbGlm.THETA_MAX, 22);
        NbGlm.Fit fit = NbGlm.fit(x, y);
        assertThat(fit.theta()).isGreaterThan(50);
        assertThat(fit.beta()[1]).isCloseTo(0.4, within(0.08));
    }

    @Test
    @DisplayName("an L2 penalty shrinks the non-intercept coefficients; non-negative columns stay at or above zero")
    void penaltyAndNonNegative() {
        double[] beta = {1.0, 0.6, -0.4};
        double[][] x = design(1_500, 31);
        double[] y = counts(x, beta, 5.0, 32);
        NbGlm.Fit free = NbGlm.fit(x, y, 0, null, true);
        NbGlm.Fit shrunk = NbGlm.fit(x, y, 2_000, null, true);
        assertThat(Math.abs(shrunk.beta()[1])).isLessThan(Math.abs(free.beta()[1]));
        NbGlm.Fit constrained = NbGlm.fit(x, y, 0, new boolean[] {false, true, true}, true);
        assertThat(constrained.beta()[2]).isEqualTo(0.0);
        assertThat(constrained.beta()[1]).isPositive();
    }

    @Test
    @DisplayName("NB CDF, quantile and log-pmf agree; the linear solver and inverse are exact on a known system")
    void distributionAndAlgebra() {
        double mu = 4;
        double theta = 2;
        int q = NbGlm.quantile(0.5, mu, theta);
        assertThat(NbGlm.cdf(q, mu, theta)).isGreaterThanOrEqualTo(0.5);
        assertThat(NbGlm.cdf(q - 1, mu, theta)).isLessThan(0.5);
        double sum = 0;
        for (int k = 0; k < 200; k++) {
            sum += Math.exp(NbGlm.logPmf(k, mu, theta));
        }
        assertThat(sum).isCloseTo(1, within(1e-9));
        double[][] a = {{4, 1}, {1, 3}};
        double[] x = NbGlm.solve(a, new double[] {1, 2});
        assertThat(x[0]).isCloseTo(1.0 / 11, within(1e-12));
        assertThat(x[1]).isCloseTo(7.0 / 11, within(1e-12));
        double[][] inv = NbGlm.invert(a);
        assertThat(inv[0][0]).isCloseTo(3.0 / 11, within(1e-12));
        assertThat(inv[0][1]).isCloseTo(-1.0 / 11, within(1e-12));
        double[][] l = NbGlm.cholesky(a);
        assertThat(l[0][0] * l[0][0]).isCloseTo(4, within(1e-12));
        assertThat(l[1][0] * l[0][0]).isCloseTo(1, within(1e-12));
        assertThat(NbGlm.momentTheta(new double[] {1, 1, 1}, new double[] {1, 1, 1}, 0)).isEqualTo(NbGlm.THETA_MAX);
        assertThatThrownBy(() -> NbGlm.fit(new double[0][0], new double[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
