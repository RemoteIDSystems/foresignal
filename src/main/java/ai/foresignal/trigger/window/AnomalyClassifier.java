package ai.foresignal.trigger.window;

/**
 * Classifies one anomaly bucket (prompt 100): {@code activity-up}, {@code activity-down},
 * {@code coverage-drop}, {@code insufficient-coverage}, {@code learning} or {@code normal}. A
 * coverage drop is never an alert; neither is an insufficient-coverage or learning bucket.
 */
public final class AnomalyClassifier {

    /** The coverage drop against the slot median that marks a coverage drop. */
    public static final double COVERAGE_DROP_FRACTION = 0.7;

    private AnomalyClassifier() {}

    /**
     * The classification result.
     *
     * @param classification the classification
     * @param direction {@code up}, {@code down} or null
     * @param fire true when this bucket is an alert-worthy anomaly
     */
    public record Result(String classification, String direction, boolean fire) {}

    /**
     * Classifies a bucket.
     *
     * @param observed the bucket's count
     * @param expected the expected count λ
     * @param coverageRatio the bucket's coverage ratio
     * @param slotMedianCoverage the slot's median coverage ratio
     * @param pValue the tail p-value (0..1), or -1 when not computed
     * @param alpha the significance
     * @param minAbsoluteDelta the smallest absolute deviation that may fire
     * @param direction the rule's direction: {@code up}, {@code down} or {@code both}
     * @param learning true while the baseline is warming up
     * @param coverageOk true when the bucket's coverage is at or above {@code coverageMin}
     */
    public static Result classify(int observed, double expected, double coverageRatio, double slotMedianCoverage,
            double pValue, double alpha, double minAbsoluteDelta, String direction, boolean learning,
            boolean coverageOk) {
        if (learning) {
            return new Result("learning", null, false);
        }
        if (!coverageOk) {
            return new Result("insufficient-coverage", null, false);
        }
        boolean down = observed < expected;
        boolean coverageDrop = down && slotMedianCoverage > 0
                && coverageRatio <= COVERAGE_DROP_FRACTION * slotMedianCoverage;
        if (coverageDrop) {
            return new Result("coverage-drop", "down", false);
        }
        boolean deltaEnough = Math.abs(observed - expected) >= minAbsoluteDelta;
        boolean significant = pValue >= 0 && pValue < alpha;
        if (significant && deltaEnough) {
            String actual = down ? "down" : "up";
            boolean allowed = "both".equals(direction) || actual.equals(direction);
            if (allowed) {
                return new Result(down ? "activity-down" : "activity-up", actual, true);
            }
        }
        return new Result("normal", null, false);
    }
}
