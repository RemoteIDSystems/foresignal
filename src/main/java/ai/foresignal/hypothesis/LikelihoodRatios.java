package ai.foresignal.hypothesis;

import java.util.Map;

/**
 * Likelihood ratios of indicators (prompt 125 §4.2, Intelligence-Design §6.3): {@code LR(H) = P(observed | H) /
 * P(observed | not H)}. A diagnosticity class gives the value (high 5.0 supporting or 0.2 weakening, medium 2.5 or
 * 0.4, low 1.4 or 0.7, neutral 1.0) unless an explicit value with a rationale is set, bounded to [0.05, 20]. The
 * default absence LR is the reciprocal of the observed LR moved halfway toward 1 in log space ({@code 1/√LR}), and an
 * absence is scaled by detectability: {@code 1 + (absenceLr − 1) × pDetect}. Pure functions.
 */
public final class LikelihoodRatios {

    /** The smallest explicit LR. */
    public static final double MIN = 0.05;

    /** The largest explicit LR. */
    public static final double MAX = 20.0;

    private LikelihoodRatios() {}

    /** The value of a class in a direction ({@code neutral} is 1 either way). */
    public static double classValue(String lrClass, String direction) {
        boolean weakens = "weakens".equals(direction);
        return switch (lrClass == null ? "neutral" : lrClass) {
            case "high" -> weakens ? 0.2 : 5.0;
            case "medium" -> weakens ? 0.4 : 2.5;
            case "low" -> weakens ? 0.7 : 1.4;
            default -> 1.0;
        };
    }

    /** {@code value} bounded to [{@value #MIN}, {@value #MAX}]. */
    public static double bound(double value) {
        if (Double.isNaN(value)) {
            return 1.0;
        }
        return Math.max(MIN, Math.min(MAX, value));
    }

    /**
     * The LR the updater applies when the indicator is observed, from an {@code IndicatorLikelihood} map: the explicit
     * {@code value} (bounded) when present, else the class value.
     *
     * @param likelihood the likelihood map, or null (neutral)
     */
    public static double observed(Map<String, Object> likelihood) {
        if (likelihood == null) {
            return 1.0;
        }
        Object value = likelihood.get("value");
        if (value instanceof Number n) {
            return bound(n.doubleValue());
        }
        return classValue(text(likelihood.get("class")), text(likelihood.get("direction")));
    }

    /** The default absence LR for an observed LR: {@code exp(−ln(lr) / 2)}, e.g. 1/√5 ≈ 0.447 for high support. */
    public static double defaultAbsence(double observedLr) {
        return Math.exp(-Math.log(observedLr) / 2.0);
    }

    /** The absence LR scaled by detectability: {@code 1 + (absenceLr − 1) × pDetect}, {@code pDetect} in [0, 1]. */
    public static double absenceEffective(double absenceLr, double pDetect) {
        double p = Math.max(0, Math.min(1, pDetect));
        return 1.0 + (absenceLr - 1.0) * p;
    }

    /**
     * The ACH rating an LR implies (prompt 125 §3): ≥ 4 {@code CC}; ≥ 1.5 {@code C}; ≤ 0.25 {@code II}; ≤ 0.67
     * {@code I}; otherwise {@code N}; {@code NA} without an LR.
     */
    public static String rating(Double lr) {
        if (lr == null) {
            return "NA";
        }
        if (lr >= 4.0) {
            return "CC";
        }
        if (lr >= 1.5) {
            return "C";
        }
        if (lr <= 0.25) {
            return "II";
        }
        if (lr <= 0.67) {
            return "I";
        }
        return "N";
    }

    /**
     * The LR an author's ACH rating carries when manual evidence is added: {@code CC} the high supporting class (5.0),
     * {@code C} medium (2.5), {@code I} medium weakening (0.4), {@code II} high weakening (0.2), otherwise 1.
     */
    public static double fromRating(String rating) {
        return switch (rating == null ? "N" : rating) {
            case "CC" -> 5.0;
            case "C" -> 2.5;
            case "I" -> 0.4;
            case "II" -> 0.2;
            default -> 1.0;
        };
    }

    /** The diagnosticity word of an LR for prose: high, medium, low or neutral (by |ln LR|). */
    public static String diagnosticity(double lr) {
        double x = Math.abs(Math.log(lr));
        if (x >= Math.log(4.0)) {
            return "high";
        }
        if (x >= Math.log(2.0)) {
            return "medium";
        }
        if (x > 1e-9) {
            return "low";
        }
        return "neutral";
    }

    private static String text(Object o) {
        return o == null ? null : o.toString();
    }
}
