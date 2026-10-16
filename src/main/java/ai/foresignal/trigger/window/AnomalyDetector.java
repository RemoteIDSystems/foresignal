package ai.foresignal.trigger.window;

/** The anomaly detector (prompt 100): turns a bucket count and a baseline into a classification. */
public final class AnomalyDetector {

    /** The detector name recorded on every result. */
    public static final String NAME = "poisson-tail";

    /** The detector version recorded on every result. */
    public static final String VERSION = "1.0.0";

    private AnomalyDetector() {}

    /**
     * The evaluated bucket.
     *
     * @param observed the bucket's count
     * @param expected λ = slot mean × coverage ratio
     * @param exposure the coverage ratio applied
     * @param pValue the tail p-value under the stated model
     * @param score the Anscombe-transformed z
     * @param classification the classification
     * @param direction {@code up}, {@code down} or null
     * @param fire true when this bucket is an alert-worthy anomaly
     */
    public record Result(int observed, double expected, double exposure, double pValue, double score,
            String classification, String direction, boolean fire) {}

    /** Evaluates one bucket against a baseline. */
    public static Result evaluate(int observed, Baseline baseline, double coverageRatio, boolean coverageOk,
            WindowSpec spec) {
        double expected = baseline.mean() * coverageRatio;
        boolean learning = !ready(baseline, spec) || baseline.mean() <= 0;
        double pUp = tailUp(observed, expected, baseline);
        double pDown = tailDown(observed, expected, baseline);
        double pValue = observed >= expected ? pUp : pDown;
        double score = PoissonTail.anscombe(observed, expected);
        AnomalyClassifier.Result classified = AnomalyClassifier.classify(observed, expected, baseline.mean(),
                coverageRatio, baseline.slotMedianCoverage(), pValue, spec.alpha(), spec.minAbsoluteDelta(),
                spec.direction(), learning, coverageOk);
        return new Result(observed, expected, coverageRatio, pValue, score, classified.classification(),
                classified.direction(), classified.fire());
    }

    /**
     * True when the baseline may be used: {@code minWeeks} of slot history, or for {@code hour-of-day}
     * 14 days (two weeks), after which results carry {@code lowConfidence} until {@code minWeeks}.
     */
    static boolean ready(Baseline baseline, WindowSpec spec) {
        if ("hour-of-day".equals(spec.slotMode())) {
            return baseline.weeks() >= HOUR_OF_DAY_START_WEEKS;
        }
        return baseline.ready(spec.minWeeks());
    }

    /** True when a result is low-confidence: an {@code hour-of-day} baseline short of {@code minWeeks}. */
    public static boolean lowConfidence(Baseline baseline, WindowSpec spec) {
        return "hour-of-day".equals(spec.slotMode()) && baseline.weeks() < spec.minWeeks();
    }

    /** How many weeks an {@code hour-of-day} baseline needs before it may start (14 days). */
    public static final double HOUR_OF_DAY_START_WEEKS = 2.0;

    /**
     * {@code P(X ≥ k)} under the stated model with mean {@code expected} (λ = slot mean × coverage
     * ratio). The negative binomial keeps the slot's dispersion (size {@code r} from the method of
     * moments) and takes the coverage-adjusted mean: {@code p = r/(r + λ)}.
     */
    static double tailUp(int observed, double expected, Baseline baseline) {
        if ("nb".equals(baseline.model())) {
            if (expected <= 0) {
                return observed <= 0 ? 1.0 : 0.0;
            }
            return NegBinTail.atLeast(observed, baseline.size(), nbProbability(baseline.size(), expected));
        }
        return PoissonTail.atLeast(observed, expected);
    }

    /** {@code P(X ≤ k)} under the stated model with mean {@code expected}; see {@link #tailUp}. */
    static double tailDown(int observed, double expected, Baseline baseline) {
        if ("nb".equals(baseline.model())) {
            if (expected <= 0) {
                return 1.0;
            }
            return NegBinTail.atMost(observed, baseline.size(), nbProbability(baseline.size(), expected));
        }
        return PoissonTail.atMost(observed, expected);
    }

    /** The negative-binomial {@code p} of size {@code r} and mean {@code λ}: {@code r/(r + λ)}. */
    static double nbProbability(double size, double mean) {
        return size / (size + mean);
    }
}
