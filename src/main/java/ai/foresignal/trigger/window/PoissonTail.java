package ai.foresignal.trigger.window;

/**
 * The Poisson tail probabilities the anomaly detector uses (prompt 100). This is a tail under the
 * stated Poisson model, not the probability that something is happening.
 */
public final class PoissonTail {

    private PoissonTail() {}

    /** {@code P(X ≥ k)} for {@code X ~ Poisson(λ)}. */
    public static double atLeast(double k, double lambda) {
        if (k <= 0) {
            return 1;
        }
        if (lambda <= 0) {
            return k <= 0 ? 1 : 0;
        }
        return Gammas.lower(k, lambda);
    }

    /** {@code P(X ≤ k)} for {@code X ~ Poisson(λ)}. */
    public static double atMost(double k, double lambda) {
        if (k < 0) {
            return 0;
        }
        if (lambda <= 0) {
            return 1;
        }
        return Gammas.upper(k + 1, lambda);
    }

    /** The Anscombe-transformed z score, {@code 2√(k+3/8) − 2√(λ+3/8)}. */
    public static double anscombe(double k, double lambda) {
        return 2 * Math.sqrt(Math.max(0, k) + 0.375) - 2 * Math.sqrt(Math.max(0, lambda) + 0.375);
    }
}
