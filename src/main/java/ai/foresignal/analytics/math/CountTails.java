package ai.foresignal.analytics.math;

import org.apache.commons.numbers.gamma.RegularizedBeta;
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;
import org.apache.commons.statistics.distribution.PoissonDistribution;

/**
 * Tail probabilities of count models and Fisher's combination of p-values (prompt 119). Poisson tails come from
 * Commons Statistics; negative-binomial tails from the regularized incomplete beta function, so the size need not be
 * an integer; the negative binomial is sized by the method of moments, as in prompt 100.
 */
public final class CountTails {

    /** The smallest p-value used inside a logarithm. */
    static final double MIN_P = 1e-300;

    private CountTails() {}

    /**
     * A negative binomial sized by the method of moments.
     *
     * @param size {@code r = μ²/(v − μ)}
     * @param probability {@code p = r/(r + μ)}
     */
    public record NegBin(double size, double probability) {

        /** The mean {@code r(1 − p)/p}. */
        public double mean() {
            return size * (1 - probability) / probability;
        }
    }

    /** The method-of-moments negative binomial, or null when the counts are not overdispersed ({@code v ≤ μ}). */
    public static NegBin negBinMoments(double mean, double variance) {
        if (!(mean > 0) || !Double.isFinite(variance) || variance <= mean) {
            return null;
        }
        double size = mean * mean / (variance - mean);
        return new NegBin(size, size / (size + mean));
    }

    /** {@code P(X ≥ k)} for {@code X ~ Poisson(λ)}. */
    public static double poissonAtLeast(double k, double lambda) {
        if (k <= 0) {
            return 1;
        }
        if (!(lambda > 0)) {
            return 0;
        }
        int kk = (int) Math.ceil(k);
        return clamp(PoissonDistribution.of(lambda).survivalProbability(kk - 1));
    }

    /** {@code P(X ≤ k)} for {@code X ~ Poisson(λ)}. */
    public static double poissonAtMost(double k, double lambda) {
        if (k < 0) {
            return 0;
        }
        if (!(lambda > 0)) {
            return 1;
        }
        return clamp(PoissonDistribution.of(lambda).cumulativeProbability((int) Math.floor(k)));
    }

    /** {@code P(X ≤ k)} for a negative binomial: {@code I_p(r, k + 1)}. */
    public static double negBinAtMost(double k, NegBin nb) {
        if (k < 0) {
            return 0;
        }
        return clamp(RegularizedBeta.value(nb.probability(), nb.size(), Math.floor(k) + 1));
    }

    /** {@code P(X ≥ k)} for a negative binomial: {@code 1 − I_p(r, k)}. */
    public static double negBinAtLeast(double k, NegBin nb) {
        if (k <= 0) {
            return 1;
        }
        return clamp(RegularizedBeta.complement(nb.probability(), nb.size(), Math.ceil(k)));
    }

    /**
     * The upper-tail p-value of a count against a baseline mean and variance: negative binomial when overdispersed,
     * Poisson otherwise.
     */
    public static double exceedanceP(double count, double mean, double variance) {
        NegBin nb = negBinMoments(mean, variance);
        return nb == null ? poissonAtLeast(count, mean) : negBinAtLeast(count, nb);
    }

    /**
     * Fisher's combination: {@code X = −2 Σ ln pᵢ} is χ² with {@code 2k} degrees of freedom under the joint null.
     *
     * @return the combined p-value; 1 for no input
     */
    public static double fisher(double... pValues) {
        if (pValues == null || pValues.length == 0) {
            return 1;
        }
        double x = 0;
        for (double p : pValues) {
            x += -2 * Math.log(Math.max(MIN_P, Math.min(1, p)));
        }
        return clamp(ChiSquaredDistribution.of(2.0 * pValues.length).survivalProbability(x));
    }

    private static double clamp(double p) {
        if (Double.isNaN(p)) {
            return 1;
        }
        return Math.max(0, Math.min(1, p));
    }
}
