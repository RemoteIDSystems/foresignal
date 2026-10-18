package ai.foresignal.social.narrative;

import java.util.Map;

/**
 * The Admiralty grade of a claim as a 125 evidence item ({@code source = social-claim}, prompt 136).
 *
 * <ul>
 *   <li><strong>Source reliability</strong> of the account or channel the claim was first observed from: {@code F}
 *       (cannot be judged) by default; {@code E} once it has at least {@value #MIN_ASSESSED} claims assessed by 137
 *       with at least 70% corroborated; {@code D} at 85% or more. An operator override for the account
 *       ({@code account:{platform}:{authorKey}}) or channel ({@code channel:{platform}:{channel}}) in
 *       {@code social.sourceReliability} wins.
 *   <li><strong>Information credibility</strong> from the claim's 137 status: 6 (cannot be judged) while
 *       unassessed, pending or without coverage; 2 corroborated; 5 unobserved; 4 contradicted.
 * </ul>
 * Pure.
 */
public final class SocialEvidenceGrades {

    /** Assessed claims an account needs before its record moves its reliability. */
    public static final int MIN_ASSESSED = 10;

    /** The corroborated share that earns {@code E}. */
    public static final double E_SHARE = 0.70;

    /** The corroborated share that earns {@code D}. */
    public static final double D_SHARE = 0.85;

    private SocialEvidenceGrades() {}

    /**
     * The source reliability.
     *
     * @param assessed claims of the source assessed by 137
     * @param corroborated of them, corroborated
     * @param override the operator override (A–F), or null
     */
    public static String reliability(int assessed, int corroborated, String override) {
        if (override != null && override.matches("^[A-F]$")) {
            return override;
        }
        if (assessed < MIN_ASSESSED) {
            return "F";
        }
        double share = (double) corroborated / assessed;
        if (share >= D_SHARE) {
            return "D";
        }
        if (share >= E_SHARE) {
            return "E";
        }
        return "F";
    }

    /**
     * The track-record thresholds ({@code social.trackRecord}, prompt 137).
     *
     * @param minAssessed assessed claims before the record moves the reliability
     * @param eShare the corroborated share that earns {@code E}
     * @param dShare the corroborated share that earns {@code D}
     */
    public record Thresholds(int minAssessed, double eShare, double dShare) {

        /** The defaults: 10, 70%, 85%. */
        public static final Thresholds DEFAULT = new Thresholds(MIN_ASSESSED, E_SHARE, D_SHARE);
    }

    /** The thresholds of a stored {@code social.trackRecord} object (defaults for missing members). */
    public static Thresholds thresholds(Map<?, ?> stored) {
        if (stored == null) {
            return Thresholds.DEFAULT;
        }
        return new Thresholds((int) number(stored.get("minAssessed"), MIN_ASSESSED),
                number(stored.get("gradeEShare"), E_SHARE), number(stored.get("gradeDShare"), D_SHARE));
    }

    private static double number(Object v, double fallback) {
        return v instanceof Number n ? n.doubleValue() : fallback;
    }

    /**
     * The source reliability under the given thresholds.
     *
     * @param assessed claims of the source assessed by 137
     * @param corroborated of them, corroborated
     * @param override the operator override (A–F), or null
     * @param t the thresholds
     */
    public static String reliability(int assessed, int corroborated, String override, Thresholds t) {
        if (override != null && override.matches("^[A-F]$")) {
            return override;
        }
        Thresholds th = t == null ? Thresholds.DEFAULT : t;
        if (assessed < th.minAssessed() || assessed <= 0) {
            return "F";
        }
        double share = (double) corroborated / assessed;
        if (share >= th.dShare()) {
            return "D";
        }
        if (share >= th.eShare()) {
            return "E";
        }
        return "F";
    }

    /** The override for a source key in the operator's map, or null. */
    public static String override(Map<?, ?> overrides, String sourceKey) {
        if (overrides == null || sourceKey == null) {
            return null;
        }
        Object v = overrides.get(sourceKey);
        return v == null ? null : v.toString().strip().toUpperCase(java.util.Locale.ROOT);
    }

    /** The information credibility of a 137 status. */
    public static int credibility(String status) {
        return switch (status == null ? "" : status) {
            case "corroborated" -> 2;
            case "contradicted" -> 4;
            case "unobserved" -> 5;
            default -> 6;
        };
    }

    /** The grade, e.g. {@code F6}. */
    public static String grade(String reliability, int credibility) {
        return reliability + credibility;
    }

    /**
     * The source key of a post: its channel when it has one, else its account.
     *
     * @param platform the network
     * @param channel the subreddit, instance, YouTube channel or equivalent, or null
     * @param authorKey the author key
     */
    public static String sourceKey(String platform, String channel, String authorKey) {
        if (channel != null && !channel.isBlank()) {
            return "channel:" + platform + ":" + channel.strip();
        }
        return "account:" + platform + ":" + authorKey;
    }
}
