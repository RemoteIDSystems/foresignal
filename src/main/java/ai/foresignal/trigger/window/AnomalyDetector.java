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
        boolean learning = !baseline.ready(spec.minWeeks()) || baseline.mean() <= 0;
        double pUp = tailUp(observed, expected, baseline);
        double pDown = tailDown(observed, expected, baseline);
        double pValue = observed >= expected ? pUp : pDown;
        double score = PoissonTail.anscombe(observed, expected);
        AnomalyClassifier.Result classified = AnomalyClassifier.classify(observed, expected, coverageRatio,
                baseline.slotMedianCoverage(), pValue, spec.alpha(), spec.minAbsoluteDelta(), spec.direction(),
                learning, coverageOk);
        return new Result(observed, expected, coverageRatio, pValue, score, classified.classification(),
                classified.direction(), classified.fire());
    }

    static double tailUp(int observed, double expected, Baseline baseline) {
        if ("nb".equals(baseline.model())) {
            return NegBinTail.atLeast(observed, baseline.size(), baseline.probability());
        }
        return PoissonTail.atLeast(observed, expected);
    }

    static double tailDown(int observed, double expected, Baseline baseline) {
        if ("nb".equals(baseline.model())) {
            return NegBinTail.atMost(observed, baseline.size(), baseline.probability());
        }
        return PoissonTail.atMost(observed, expected);
    }
}
