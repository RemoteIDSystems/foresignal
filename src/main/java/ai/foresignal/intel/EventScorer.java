package ai.foresignal.intel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The deterministic confidence scorer (prompt 101; version 1.1.0 since prompt 116): a score, not a calibrated
 * probability. {@code c = 1 − Π_f (1 − w_f · r_f)} over distinct signal families, where {@code r_f}
 * is the best member's source reliability (default 0.6), then {@code ×(1 − 0.25)} per active
 * contradiction, capped at 0.95. It becomes calibrated only when 103 sets {@code calibrated}.
 *
 * <p><strong>1.1.0 (prompt 116, evidence independence):</strong> members are first collapsed by evidence
 * group: two members in one evidence group contribute once (the stronger), so re-reports of one originator
 * never raise confidence or evidence quality. Evidence quality {@code strong} needs at least 3 families from
 * at least 2 <em>independent evidence groups</em> (distinct originator roots). A member without an evidence
 * group is its own group, judged by its {@code originatorRoot} (its source when none is given), which keeps
 * the 1.0.0 results for members that share nothing. 1.0.0 counted every member and judged independence by
 * source id; existing events are re-scored with 1.1.0 lazily, on their next update.
 */
public final class EventScorer {

    /** The scorer name recorded in {@code detector}. */
    public static final String NAME = "event-scorer";

    /** The scorer version. */
    public static final String VERSION = "1.1.0";

    /** The previous version (prompt 101), recorded for the version change. */
    public static final String PREVIOUS_VERSION = "1.0.0";

    /** The confidence method string carried on events. */
    public static final String METHOD = "event-scorer-1.1.0";

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
     * @param evidenceGroup the member's evidence group key (116 {@code EvidenceService.GroupKey.key()}), or null
     *     when it shares no group with any other member
     * @param originatorRoot the originator root independence is judged by, or null for the source id
     * @param modality the sensing modality (prompt 154, {@link Modality}), or null when it was not resolved
     */
    public record MemberView(String signalFamily, double weight, String sourceId, boolean sourceHealthy,
            Double reliability, String evidenceGroup, String originatorRoot, String modality) {

        /** A member without evidence identity (each member its own group, independence by source). */
        public MemberView(String signalFamily, double weight, String sourceId, boolean sourceHealthy,
                Double reliability) {
            this(signalFamily, weight, sourceId, sourceHealthy, reliability, null, null, null);
        }

        /** A member with evidence identity but no resolved modality (keeps the pre-154 shape). */
        public MemberView(String signalFamily, double weight, String sourceId, boolean sourceHealthy,
                Double reliability, String evidenceGroup, String originatorRoot) {
            this(signalFamily, weight, sourceId, sourceHealthy, reliability, evidenceGroup, originatorRoot, null);
        }

        /** The root independence is judged by. */
        String root() {
            return originatorRoot != null ? originatorRoot : sourceId;
        }
    }

    /**
     * The evidence-quality thresholds (prompt 154), tunable through the corroboration policy so the "promote to
     * trusted" gate is config, not a constant.
     *
     * @param strongFamilies the distinct signal families {@code strong} needs
     * @param strongGroups the independent evidence groups {@code strong} needs
     * @param moderateFamilies the distinct signal families {@code moderate} needs
     * @param requireModalities the distinct sensing modalities {@code strong} also needs, or 0 to not require any
     */
    public record Quality(int strongFamilies, int strongGroups, int moderateFamilies, int requireModalities) {

        /** The historical defaults (prompt 116): strong = 3 families from 2 independent groups; moderate = 2. */
        public static final Quality DEFAULT = new Quality(3, 2, 2, 0);
    }

    /**
     * The assay.
     *
     * @param confidence the score in [0, 0.95]
     * @param label low / moderate / high
     * @param evidenceQuality strong / moderate / weak / insufficient
     */
    public record Score(double confidence, String label, String evidenceQuality) {}

    /** Scores an event's supporting members and active contradictions with the default quality thresholds. */
    public static Score score(List<MemberView> supporting, int contradictions) {
        return score(supporting, contradictions, Quality.DEFAULT);
    }

    /** Scores an event's supporting members and active contradictions under the given quality thresholds. */
    public static Score score(List<MemberView> supporting, int contradictions, Quality quality) {
        Quality q = quality == null ? Quality.DEFAULT : quality;
        boolean anyHealthy = false;
        for (MemberView member : supporting) {
            anyHealthy = anyHealthy || member.sourceHealthy();
        }
        List<MemberView> independent = strongestPerGroup(supporting);
        Map<String, Double> bestByFamily = new LinkedHashMap<>();
        Map<String, java.util.Set<String>> rootsByFamily = new LinkedHashMap<>();
        java.util.Set<String> modalities = new java.util.LinkedHashSet<>();
        for (MemberView member : independent) {
            bestByFamily.merge(member.signalFamily(), contribution(member), Math::max);
            rootsByFamily.computeIfAbsent(member.signalFamily(), k -> new java.util.LinkedHashSet<>())
                    .add(member.root());
            if (member.modality() != null) {
                modalities.add(member.modality());
            }
        }
        double product = 1;
        for (double contribution : bestByFamily.values()) {
            product *= (1 - clamp01(contribution));
        }
        double confidence = 1 - product;
        confidence *= Math.pow(1 - CONTRADICTION_PENALTY, Math.max(0, contradictions));
        confidence = Math.max(0, Math.min(CAP, confidence));
        String label = label(confidence);
        String quality1 = quality(bestByFamily, rootsByFamily, modalities.size(), supporting, anyHealthy, q);
        return new Score(confidence, label, quality1);
    }

    /**
     * The members that count (1.1.0): within each evidence group only the strongest member (highest
     * {@code weight × reliability}, ties to the first); a member without a group stands alone. Members without
     * a family never count.
     */
    static List<MemberView> strongestPerGroup(List<MemberView> supporting) {
        Map<String, MemberView> best = new LinkedHashMap<>();
        int index = 0;
        for (MemberView member : supporting) {
            index++;
            if (member.signalFamily() == null) {
                continue;
            }
            String key = member.evidenceGroup() == null ? "member:" + index : member.evidenceGroup();
            MemberView kept = best.get(key);
            if (kept == null || contribution(member) > contribution(kept)) {
                best.put(key, member);
            }
        }
        return new java.util.ArrayList<>(best.values());
    }

    private static double contribution(MemberView member) {
        double weight = clamp01(member.weight());
        double reliability = member.reliability() == null ? DEFAULT_RELIABILITY : clamp01(member.reliability());
        return weight * reliability;
    }

    /** low &lt; 0.4 ≤ moderate &lt; 0.7 ≤ high. */
    public static String label(double confidence) {
        if (confidence < 0.4) {
            return "low";
        }
        return confidence < 0.7 ? "moderate" : "high";
    }

    /**
     * strong: ≥ {@code strongFamilies} families from ≥ {@code strongGroups} independent evidence groups (distinct
     * originator roots), and — when {@code requireModalities} &gt; 0 — ≥ that many distinct sensing modalities;
     * moderate: ≥ {@code moderateFamilies} families; weak: 1 family; insufficient: members only from sources
     * currently unhealthy. The thresholds come from the corroboration policy (prompt 154).
     */
    static String quality(Map<String, Double> bestByFamily, Map<String, java.util.Set<String>> rootsByFamily,
            int modalities, List<MemberView> supporting, boolean anyHealthy, Quality q) {
        if (supporting.isEmpty() || !anyHealthy) {
            return "insufficient";
        }
        int families = bestByFamily.size();
        int independent = independentSources(rootsByFamily);
        boolean enoughModalities = q.requireModalities() <= 0 || modalities >= q.requireModalities();
        if (families >= q.strongFamilies() && independent >= q.strongGroups() && enoughModalities) {
            return "strong";
        }
        if (families >= q.moderateFamilies()) {
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
