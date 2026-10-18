package ai.foresignal.chronicle.network;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Overlap estimates and the link decision (prompt 134). Pure.
 *
 * <p>A pair of chronicles is linked when, for at least one key type, the estimated shared keys reach the minimum
 * ({@code network.minSharedKeys}, default 5) <b>and</b> the containment of the smaller set reaches 0.05. The key
 * types that pass are the link's dimensions; its strength comes from the largest Jaccard among them (low below 0.1,
 * medium below 0.3, high from 0.3).
 */
public final class LinkScores {

    private LinkScores() {}

    /**
     * The overlap of one key type.
     *
     * @param jaccard the (estimated or exact) Jaccard similarity
     * @param containment the containment of the smaller set
     * @param sharedEstimate the (estimated or exact) number of shared keys
     */
    public record Estimate(double jaccard, double containment, double sharedEstimate) {

        /** The {@code scores} entry. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("jaccard", round(jaccard));
            out.put("containment", round(containment));
            out.put("sharedEstimate", round(sharedEstimate));
            return out;
        }
    }

    /**
     * What the scores decide.
     *
     * @param linked true when the pair is linked
     * @param dimensions the key types that passed, in display order
     * @param strength the strength band, or null when not linked
     * @param scores the per-key-type scores
     * @param sharedKeys the shared keys summed over the passing dimensions
     */
    public record Decision(boolean linked, List<String> dimensions, String strength, Map<String, Object> scores,
            long sharedKeys) {}

    /** The estimate of two MinHash signatures with their set sizes. */
    public static Estimate estimate(MinHashSketch a, double sizeA, MinHashSketch b, double sizeB) {
        double j = a.jaccard(b);
        double shared = MinHashSketch.sharedEstimate(j, sizeA, sizeB);
        return new Estimate(j, MinHashSketch.containment(shared, sizeA, sizeB), shared);
    }

    /** The exact overlap of two key sets from their sizes and shared count. */
    public static Estimate exact(long shared, long sizeA, long sizeB) {
        long union = sizeA + sizeB - shared;
        double j = union <= 0 ? 0 : shared / (double) union;
        return new Estimate(j, MinHashSketch.containment(shared, sizeA, sizeB), shared);
    }

    /**
     * Decides a link from the per-key-type overlaps.
     *
     * @param perType key type → overlap
     * @param minShared the minimum shared keys
     * @return the decision
     */
    public static Decision decide(Map<String, Estimate> perType, int minShared) {
        List<String> dimensions = new ArrayList<>();
        Map<String, Object> scores = new LinkedHashMap<>();
        double best = 0;
        long shared = 0;
        for (String type : NetworkVocabulary.KEY_TYPES) {
            Estimate e = perType.get(type);
            if (e == null) {
                continue;
            }
            scores.put(type, e.toMap());
            if (passes(e, minShared)) {
                dimensions.add(type);
                best = Math.max(best, e.jaccard());
                shared += Math.round(e.sharedEstimate());
            }
        }
        if (dimensions.isEmpty()) {
            return new Decision(false, List.of(), null, scores, 0);
        }
        return new Decision(true, List.copyOf(dimensions), NetworkVocabulary.strength(best), scores, shared);
    }

    /** True when one key type's overlap reaches both minimums. */
    public static boolean passes(Estimate e, int minShared) {
        return e.sharedEstimate() >= minShared && e.containment() >= NetworkVocabulary.MIN_CONTAINMENT;
    }

    private static double round(double v) {
        return Math.round(v * 10_000) / 10_000.0;
    }
}
