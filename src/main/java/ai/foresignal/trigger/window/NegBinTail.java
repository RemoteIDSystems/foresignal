package ai.foresignal.trigger.window;

/**
 * The negative-binomial tail the anomaly detector uses when a slot's samples are overdispersed
 * (variance/mean &gt; 1.5), sized by the method of moments (prompt 100).
 */
public final class NegBinTail {

    private NegBinTail() {}

    /** The size {@code r = μ²/(v − μ)} and probability {@code p = r/(r + μ)}. */
    public static double[] moments(double mean, double variance) {
        if (variance <= mean) {
            return null;
        }
        double size = mean * mean / (variance - mean);
        double probability = size / (size + mean);
        return new double[] {size, probability};
    }

    /** {@code P(X ≥ k)} for a negative binomial. */
    public static double atLeast(double k, double size, double probability) {
        if (k <= 0) {
            return 1;
        }
        double p = atMost(k - 1, size, probability);
        return Math.min(1, Math.max(0, 1 - p));
    }

    /** {@code P(X ≤ k)} for a negative binomial. */
    public static double atMost(double k, double size, double probability) {
        if (k < 0) {
            return 0;
        }
        int max = (int) Math.floor(k);
        double logP = size * Math.log(probability);
        double sum = 0;
        for (int i = 0; i <= max; i++) {
            double logTerm = logP + Gammas.logGamma(i + size) - Gammas.logGamma(size) - Gammas.logGamma(i + 1)
                    + i * Math.log(1 - probability);
            sum += Math.exp(logTerm);
        }
        return Math.min(1, Math.max(0, sum));
    }
}
