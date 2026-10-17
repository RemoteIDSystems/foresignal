package ai.foresignal.analytics.math;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterMarsagliaTsangGammaSampler;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Seeded sampling for count forecasts (prompt 126): negative-binomial draws as a gamma–Poisson mixture
 * ({@code λ ~ Gamma(θ, μ/θ)}, {@code y ~ Poisson(λ)}), Poisson draws for very large {@code θ}, and correlated normal
 * coefficient draws for a parametric bootstrap. Every stream is seeded, so the same inputs give the same quantiles
 * (INT-1).
 */
public final class NbSampling {

    /** The largest mean a Poisson draw is asked for. */
    static final double MAX_MEAN = 1e9;

    private final UniformRandomProvider rng;
    private final ZigguratSampler.NormalizedGaussian gaussian;

    /** A sampler seeded with {@code seed}. */
    public NbSampling(long seed) {
        this.rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        this.gaussian = ZigguratSampler.NormalizedGaussian.of(rng);
    }

    /** One NB2 count with mean {@code mu} and size {@code theta}. */
    public int negBin(double mu, double theta) {
        if (!(mu > 0)) {
            return 0;
        }
        double lambda = Math.min(mu, MAX_MEAN);
        if (theta < NbGlm.THETA_MAX) {
            lambda = AhrensDieterMarsagliaTsangGammaSampler.of(rng, theta, mu / theta).sample();
            lambda = Math.min(lambda, MAX_MEAN);
        }
        if (!(lambda > 0)) {
            return 0;
        }
        return PoissonSampler.of(rng, lambda).sample();
    }

    /** A standard normal draw. */
    public double normal() {
        return gaussian.sample();
    }

    /** A draw from {@code N(mean, L·Lᵀ)} given the lower Cholesky factor {@code l}. */
    public double[] multivariateNormal(double[] mean, double[][] l) {
        int p = mean.length;
        double[] z = new double[p];
        for (int i = 0; i < p; i++) {
            z[i] = normal();
        }
        double[] out = mean.clone();
        for (int i = 0; i < p; i++) {
            double s = 0;
            for (int k = 0; k <= i; k++) {
                s += l[i][k] * z[k];
            }
            out[i] += s;
        }
        return out;
    }
}
