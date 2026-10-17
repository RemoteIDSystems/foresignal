package ai.foresignal.precursor;

import ai.foresignal.backtest.PrecursorTester;
import java.time.Duration;

/**
 * The validation and lifecycle rules of the precursor library (prompt 120 §3–§5), pure and documented constants.
 *
 * <ul>
 *   <li><b>Event validation:</b> on the validation window the verdict is again {@code notable}, the lift is at least
 *       {@value #VALIDATION_LIFT}, and the lower bound of the hit rate's Wilson 95% interval is above the base rate.
 *   <li><b>Series validation:</b> the same lag sign holds and {@code p ≤ }{@value #SERIES_P}.
 *   <li><b>Re-test:</b> {@code validated → stale} when the lift falls below {@value #STALE_LIFT} or the hit-rate
 *       interval includes the base rate (series: the sign flips or {@code p > }{@value #SERIES_P}); {@code stale} twice
 *       in a row → {@code retired}; a stale pattern that validates again returns to {@code validated}. Operator-pinned
 *       precursors keep their status.
 *   <li><b>Live probability:</b> {@code p = (hits + 1) / (n + 2)} shrunk toward the base rate by {@code n / (n + 20)}.
 * </ul>
 */
public final class PrecursorRules {

    /** The lift a validated event precursor needs on the validation window. */
    public static final double VALIDATION_LIFT = 1.5;

    /** The lift below which a re-tested event precursor becomes stale. */
    public static final double STALE_LIFT = 1.2;

    /** The p-value a validated series precursor needs on the validation window. */
    public static final double SERIES_P = 0.05;

    /** The pseudo-count of the shrinkage toward the base rate. */
    public static final double SHRINK_PRIOR = 20;

    /** The standardized threshold at which a series antecedent counts as an occurrence. */
    public static final double LIVE_THRESHOLD = 2.0;

    /** The shifted copies of the event tester during mining (p-values down to 1/1000). */
    public static final int MINING_SHIFTS = 999;

    private PrecursorRules() {}

    /** A lifecycle status. */
    public static final String CANDIDATE = "candidate";

    /** A lifecycle status. */
    public static final String VALIDATED = "validated";

    /** A lifecycle status. */
    public static final String STALE = "stale";

    /** A lifecycle status. */
    public static final String RETIRED = "retired";

    /**
     * Why an event result does not validate, or null when it does.
     *
     * @param r the validation-window result
     * @return the reason, or null
     */
    public static String eventValidationFailure(PrecursorTester.Result r) {
        if (r.insufficient()) {
            return "insufficient data on the validation window (" + r.nAntecedent() + " antecedents)";
        }
        if (!"notable".equals(r.verdict())) {
            return "the validation verdict was " + r.verdict() + ", not notable";
        }
        if (!(r.lift() >= VALIDATION_LIFT)) {
            return "the validation lift " + PrecursorText.num(r.lift()) + " is below " + VALIDATION_LIFT;
        }
        if (!(r.hitRateInterval()[0] > r.baseRate())) {
            return "the hit-rate interval includes the base rate on the validation window";
        }
        return null;
    }

    /**
     * Why a series result does not validate against its discovery result, or null when it does.
     *
     * @param discovery the discovery-window result
     * @param validation the validation-window result
     * @return the reason, or null
     */
    public static String seriesValidationFailure(LeadLagCcf.Result discovery, LeadLagCcf.Result validation) {
        if (validation.insufficient()) {
            return "insufficient overlap on the validation window (" + validation.overlap() + " points)";
        }
        if (!sameSign(discovery, validation)) {
            return "the lag sign changed on the validation window";
        }
        if (validation.forward().pValue() > SERIES_P) {
            return "the validation p-value " + PrecursorText.num(validation.forward().pValue()) + " is above "
                    + SERIES_P;
        }
        return null;
    }

    /** True when both results have a correlation of the same sign. */
    static boolean sameSign(LeadLagCcf.Result a, LeadLagCcf.Result b) {
        return a.sign() != null && a.sign().equals(b.sign());
    }

    /**
     * Why a re-tested event precursor is stale, or null when it still holds.
     *
     * @param r the re-test result
     * @return the reason, or null
     */
    public static String eventStaleReason(PrecursorTester.Result r) {
        if (r.insufficient()) {
            return "insufficient data on the re-test window (" + r.nAntecedent() + " antecedents)";
        }
        if (!(r.lift() >= STALE_LIFT)) {
            return "the lift fell to " + PrecursorText.num(r.lift()) + ", below " + STALE_LIFT;
        }
        if (r.hitRateInterval()[0] <= r.baseRate()) {
            return "the hit-rate interval includes the base rate";
        }
        return null;
    }

    /**
     * Why a re-tested series precursor is stale, or null when it still holds.
     *
     * @param original the stored sign ({@code positive} or {@code negative})
     * @param r the re-test result
     * @return the reason, or null
     */
    public static String seriesStaleReason(String original, LeadLagCcf.Result r) {
        if (r.insufficient()) {
            return "insufficient overlap on the re-test window (" + r.overlap() + " points)";
        }
        if (original == null || !original.equals(r.sign())) {
            return "the lag sign changed";
        }
        if (r.forward().pValue() > SERIES_P) {
            return "the p-value rose to " + PrecursorText.num(r.forward().pValue());
        }
        return null;
    }

    /**
     * The next status after a re-test.
     *
     * @param status the current status ({@code validated} or {@code stale})
     * @param staleReason why the re-test found it stale, or null when it held
     * @param validates true when the re-test also meets the validation rule
     * @return the next status
     */
    public static String afterRetest(String status, String staleReason, boolean validates) {
        if (STALE.equals(status)) {
            if (validates) {
                return VALIDATED;
            }
            return staleReason != null ? RETIRED : STALE;
        }
        if (VALIDATED.equals(status)) {
            return staleReason != null ? STALE : VALIDATED;
        }
        return status;
    }

    /**
     * The live probability: {@code (hits + 1) / (n + 2)} shrunk toward {@code baseRate} by {@code n / (n + 20)}.
     *
     * @param hits the hits
     * @param n the antecedent occurrences
     * @param baseRate the base rate
     * @return the probability
     */
    public static double probability(long hits, long n, double baseRate) {
        double laplace = (hits + 1.0) / (n + 2.0);
        double weight = shrinkWeight(n);
        double base = Double.isFinite(baseRate) ? baseRate : laplace;
        return Math.max(0, Math.min(1, weight * laplace + (1 - weight) * base));
    }

    /** The shrinkage weight {@code n / (n + 20)}. */
    public static double shrinkWeight(long n) {
        return n <= 0 ? 0 : n / (n + SHRINK_PRIOR);
    }

    /** The ISO-8601 form of a duration, in days when it is a whole number of days ({@code P3D}, {@code P0D}). */
    public static String iso(Duration d) {
        if (d == null) {
            return null;
        }
        if (d.toSeconds() % 86_400 == 0) {
            return "P" + d.toDays() + "D";
        }
        return d.toString();
    }
}
