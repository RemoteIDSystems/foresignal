package ai.foresignal.baserate.analog;

import ai.foresignal.baserate.BaseRateCalculator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The analog search (prompt 121, {@code analog-search} {@value #VERSION}): pure and deterministic.
 *
 * <ol>
 *   <li>Candidates that overlap the target in time, or belong to its merge group, are excluded.
 *   <li>The top {@value #TOP} by feature cosine and the top {@value #TOP} by summary-embedding cosine (when embeddings
 *       exist) are fused by reciprocal rank: score = Σ 1 ÷ ({@value #RRF_K} + rank). Without embeddings the ranking
 *       is by feature cosine alone ({@code feature-only}).
 *   <li>The top k are returned with the count found (the fused set before truncation).
 * </ol>
 *
 * <p>The outcome distribution over the returned analogs whose outcome is known is the frequency of the target
 * outcome, shrunk toward the base rate by w = n ÷ (n + {@value #SHRINK}), with a Jeffreys interval of the raw
 * frequency.
 */
public final class AnalogSearch {

    /** The search's name. */
    public static final String NAME = "analog-search";

    /** The search's version. */
    public static final String VERSION = "1.0.0";

    /** Candidates kept per ranking. */
    public static final int TOP = 50;

    /** The reciprocal-rank constant. */
    public static final int RRF_K = 60;

    /** The shrinkage constant: w = n ÷ (n + 10). */
    public static final double SHRINK = 10;

    private AnalogSearch() {}

    /**
     * One candidate.
     *
     * @param id the situation id (or a region-period key)
     * @param signature its signature
     * @param start its start
     * @param end its end (or the end of its outcome horizon), null while open
     * @param group its merge group (the root it was merged into, or itself)
     */
    public record Candidate(String id, SignatureBuilder.Signature signature, Instant start, Instant end,
            String group) {}

    /**
     * One ranked analog.
     *
     * @param id the candidate id
     * @param rank 1 = most similar
     * @param similarity the fused score (feature cosine when feature-only)
     * @param featureSimilarity the feature cosine
     * @param embeddingSimilarity the embedding cosine, or null
     */
    public record Ranked(String id, int rank, double similarity, double featureSimilarity, Double embeddingSimilarity) {}

    /**
     * A search result.
     *
     * @param analogs the top k
     * @param countFound the candidates found before truncation
     * @param fused true when embeddings took part
     */
    public record Result(List<Ranked> analogs, int countFound, boolean fused) {}

    /**
     * Searches.
     *
     * @param target the target's signature
     * @param targetStart the target's start
     * @param targetEnd the target's end (now while open)
     * @param targetGroup the target's merge group
     * @param candidates the candidates
     * @param embedding embedding cosine per candidate id (top {@value #TOP}), or null without embeddings
     * @param k how many to return
     * @return the result
     */
    public static Result search(SignatureBuilder.Signature target, Instant targetStart, Instant targetEnd,
            String targetGroup, List<Candidate> candidates, Map<String, Double> embedding, int k) {
        List<Candidate> eligible = new ArrayList<>();
        for (Candidate c : candidates) {
            if (overlaps(c, targetStart, targetEnd) || (targetGroup != null && targetGroup.equals(c.group()))
                    || c.signature() == null || c.signature().zero()) {
                continue;
            }
            eligible.add(c);
        }
        Map<String, Double> featureSim = new HashMap<>();
        for (Candidate c : eligible) {
            featureSim.put(c.id(), SignatureBuilder.cosine(target, c.signature()));
        }
        List<String> byFeature = new ArrayList<>(featureSim.keySet());
        byFeature.sort(Comparator.comparingDouble((String id) -> -featureSim.get(id)).thenComparing(id -> id));
        byFeature = byFeature.subList(0, Math.min(TOP, byFeature.size()));
        boolean fused = embedding != null;
        Map<String, Double> score = new LinkedHashMap<>();
        for (int i = 0; i < byFeature.size(); i++) {
            score.merge(byFeature.get(i), fused ? 1.0 / (RRF_K + i + 1) : featureSim.get(byFeature.get(i)),
                    Double::sum);
        }
        if (fused) {
            Set<String> allowed = featureSim.keySet();
            List<String> byEmbedding = new ArrayList<>();
            for (String id : embedding.keySet()) {
                if (allowed.contains(id)) {
                    byEmbedding.add(id);
                }
            }
            byEmbedding.sort(Comparator.comparingDouble((String id) -> -embedding.get(id)).thenComparing(id -> id));
            byEmbedding = byEmbedding.subList(0, Math.min(TOP, byEmbedding.size()));
            for (int i = 0; i < byEmbedding.size(); i++) {
                score.merge(byEmbedding.get(i), 1.0 / (RRF_K + i + 1), Double::sum);
            }
        }
        List<String> order = new ArrayList<>(score.keySet());
        order.sort(Comparator.comparingDouble((String id) -> -score.get(id)).thenComparing(id -> id));
        List<Ranked> out = new ArrayList<>();
        for (int i = 0; i < order.size() && i < k; i++) {
            String id = order.get(i);
            out.add(new Ranked(id, i + 1, score.get(id), featureSim.get(id), fused ? embedding.get(id) : null));
        }
        return new Result(out, order.size(), fused);
    }

    /** True when the candidate's span overlaps the target's. */
    static boolean overlaps(Candidate c, Instant targetStart, Instant targetEnd) {
        Instant cEnd = c.end() == null ? Instant.MAX : c.end();
        Instant tEnd = targetEnd == null ? Instant.MAX : targetEnd;
        return !c.start().isAfter(tEnd) && !targetStart.isAfter(cEnd);
    }

    /**
     * An outcome distribution.
     *
     * @param analogCount analogs whose outcome is known
     * @param hits analogs where the outcome occurred
     * @param frequency hits ÷ analogCount, or null without analogs
     * @param baseRate the base rate, or null
     * @param probability the shrunk probability
     * @param lower the Jeffreys interval's lower bound (of the frequency)
     * @param upper its upper bound
     * @param weight the shrinkage weight n ÷ (n + 10)
     */
    public record Distribution(int analogCount, int hits, Double frequency, Double baseRate, double probability,
            double lower, double upper, double weight) {}

    /**
     * The outcome distribution: frequency among analogs shrunk toward the base rate by n ÷ (n + 10); without a base
     * rate the frequency stands (and 0.5 without analogs either).
     */
    public static Distribution distribution(int hits, int analogCount, Double baseRate) {
        double w = analogCount / (analogCount + SHRINK);
        Double frequency = analogCount == 0 ? null : (double) hits / analogCount;
        double probability;
        if (frequency == null) {
            probability = baseRate == null ? 0.5 : baseRate;
        } else if (baseRate == null) {
            probability = frequency;
        } else {
            probability = w * frequency + (1 - w) * baseRate;
        }
        double[] interval = BaseRateCalculator.jeffreys(hits, analogCount);
        return new Distribution(analogCount, hits, frequency, baseRate, probability, interval[0], interval[1], w);
    }
}
