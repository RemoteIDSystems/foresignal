package ai.foresignal.intel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The deterministic confidence scorer (prompt 101, version 1.0.0): a score, not a calibrated
 * probability. {@code c = 1 − Π_f (1 − w_f · r_f)} over distinct signal families, where {@code r_f}
 * is the best member's source reliability (default 0.6), then {@code ×(1 − 0.25)} per active
 * contradiction, capped at 0.95. It becomes calibrated only when 103 sets {@code calibrated}.
 */
public final class EventScorer {

    /** The scorer name recorded in {@code detector}. */
    public static final String NAME = "event-scorer";

    /** The scorer version. */
    public static final String VERSION = "1.0.0";

    /** The confidence method string carried on events. */
    public static final String METHOD = "event-scorer-1.0.0";

    /** The default source reliability until 109 grades exist. */
    public static final double DEFAULT_RELIABILITY = 0.6;

    /** The per-contradiction penalty. */
    public static final double CONTRADICTION_PENALTY = 0.25;

    /** The confidence cap. */
    public static final double CAP = 0.95;

    private EventScorer() {}

    /**
     * One member as the scorer sees it.
     *
     * @param signalFamily the family it contributes
     * @param weight the recipe weight of its family (0–1)
     * @param sourceId its source
     * @param sourceHealthy whether the source is currently healthy
     * @param reliability the source reliability, or null for the default
     */
    public record MemberView(String signalFamily, double weight, String sourceId, boolean sourceHealthy,
            Double reliability) {}

    /**
     * The assay.
     *
     * @param confidence the score in [0, 0.95]
     * @param label low / moderate / high
     * @param evidenceQuality strong / moderate / weak / insufficient
     */
    public record Score(double confidence, String label, String evidenceQuality) {}

    /** Scores an event's supporting members and active contradictions. */
    public static Score score(List<MemberView> supporting, int contradictions) {
        Map<String, Double> bestByFamily = new LinkedHashMap<>();
        Map<String, java.util.Set<String>> sourcesByFamily = new LinkedHashMap<>();
        boolean anyHealthy = false;
        for (MemberView member : supporting) {
            if (member.signalFamily() == null) {
                continue;
            }
            double weight = clamp01(member.weight());
            double reliability = member.reliability() == null ? DEFAULT_RELIABILITY : clamp01(member.reliability());
            double contribution = weight * reliability;
            bestByFamily.merge(member.signalFamily(), contribution, Math::max);
            sourcesByFamily.computeIfAbsent(member.signalFamily(), k -> new java.util.LinkedHashSet<>())
                    .add(member.sourceId());
            anyHealthy = anyHealthy || member.sourceHealthy();
        }
        double product = 1;
        for (double contribution : bestByFamily.values()) {
            product *= (1 - clamp01(contribution));
        }
        double confidence = 1 - product;
        confidence *= Math.pow(1 - CONTRADICTION_PENALTY, Math.max(0, contradictions));
        confidence = Math.max(0, Math.min(CAP, confidence));
        String label = label(confidence);
        String quality = quality(bestByFamily, sourcesByFamily, supporting, anyHealthy);
        return new Score(confidence, label, quality);
    }

    /** low &lt; 0.4 ≤ moderate &lt; 0.7 ≤ high. */
    public static String label(double confidence) {
        if (confidence < 0.4) {
            return "low";
        }
        return confidence < 0.7 ? "moderate" : "high";
    }

    /**
     * strong: ≥ 3 families from ≥ 2 independent sources; moderate: 2 families; weak: 1 family;
     * insufficient: members only from sources currently unhealthy.
     */
    static String quality(Map<String, Double> bestByFamily, Map<String, java.util.Set<String>> sourcesByFamily,
            List<MemberView> supporting, boolean anyHealthy) {
        if (supporting.isEmpty() || !anyHealthy) {
            return "insufficient";
        }
        int families = bestByFamily.size();
        int independent = independentSources(sourcesByFamily);
        if (families >= 3 && independent >= 2) {
            return "strong";
        }
        if (families >= 2) {
            return "moderate";
        }
        return "weak";
    }

    private static int independentSources(Map<String, java.util.Set<String>> sourcesByFamily) {
        java.util.Set<String> all = new java.util.LinkedHashSet<>();
        for (java.util.Set<String> sources : sourcesByFamily.values()) {
            all.addAll(sources);
        }
        return all.size();
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
