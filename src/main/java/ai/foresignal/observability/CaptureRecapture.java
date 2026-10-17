package ai.foresignal.observability;

import ai.foresignal.backtest.Wilson;
import java.util.Optional;

/**
 * The capture–recapture detection estimate (prompt 116), pure: for source A against the union B of the
 * independent other sources over the same canonical events, {@code p_detect(A) = |A ∩ B| / |B|} with a 95%
 * Wilson interval. With fewer than {@value #MIN_REFERENCE} reference events there is no estimate.
 */
public final class CaptureRecapture {

    /** The estimator method name. */
    public static final String METHOD = "capture-recapture";

    /** The smallest |B| that gives an estimate. */
    public static final int MIN_REFERENCE = 20;

    /** |B| from which an estimate is high confidence. */
    public static final int HIGH_CONFIDENCE_REFERENCE = 100;

    private CaptureRecapture() {}

    /**
     * An estimate.
     *
     * @param p the detection probability
     * @param lo the interval's lower bound
     * @param hi the interval's upper bound
     * @param n the reference events |B|
     * @param confidence {@code moderate} below {@value #HIGH_CONFIDENCE_REFERENCE} reference events, else
     *     {@code high}
     */
    public record Estimate(double p, double lo, double hi, long n, String confidence) {}

    /**
     * The estimate from the reference count {@code |B|} and the overlap {@code |A ∩ B|}.
     *
     * @return empty when {@code reference < MIN_REFERENCE}
     */
    public static Optional<Estimate> estimate(long overlap, long reference) {
        if (reference < MIN_REFERENCE || overlap < 0) {
            return Optional.empty();
        }
        long hits = Math.min(overlap, reference);
        double[] interval = Wilson.interval(hits, reference);
        return Optional.of(new Estimate((double) hits / reference, interval[0], interval[1], reference,
                reference >= HIGH_CONFIDENCE_REFERENCE ? "high" : "moderate"));
    }
}
