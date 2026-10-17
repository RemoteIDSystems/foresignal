package ai.foresignal.hypothesis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The ACH matrix arithmetic (prompt 125 §3, Intelligence-Design §6.3). Each hypothesis's inconsistency score is
 * {@code −Σ w · inc} over the live evidence rows, with {@code inc(I) = 1}, {@code inc(II) = 2} and
 * {@code w = credibility × relevance} (L 0.5, M 1, H 2); the least-inconsistent hypothesis has the highest score. A
 * row rated identically for every hypothesis is non-diagnostic. The matrix is an audit view: probabilities come from
 * the updater. Pure.
 */
public final class AchMatrix {

    private AchMatrix() {}

    /**
     * One evidence row.
     *
     * @param id the row id
     * @param ratings the rating per hypothesis id (missing means {@code NA})
     * @param credibility H, M or L
     * @param relevance H, M or L
     */
    public record Row(String id, Map<String, String> ratings, String credibility, String relevance) {}

    /**
     * The computed matrix.
     *
     * @param scores the inconsistency score per hypothesis id, in the given order
     * @param leastInconsistent the hypothesis with the highest score (the first on ties), or null without hypotheses
     * @param nonDiagnostic the ids of rows rated identically for every hypothesis
     * @param weights the weight per row id
     */
    public record Result(Map<String, Double> scores, String leastInconsistent, List<String> nonDiagnostic,
            Map<String, Double> weights) {}

    /** The weight of a row: credibility × relevance with L 0.5, M 1, H 2 (unknown grades count as M). */
    public static double weight(String credibility, String relevance) {
        return HypothesisVocabulary.GRADE_WEIGHT.getOrDefault(credibility, 1.0)
                * HypothesisVocabulary.GRADE_WEIGHT.getOrDefault(relevance, 1.0);
    }

    /** The inconsistency of a rating: 1 for {@code I}, 2 for {@code II}, otherwise 0. */
    public static int inconsistency(String rating) {
        if ("II".equals(rating)) {
            return 2;
        }
        if ("I".equals(rating)) {
            return 1;
        }
        return 0;
    }

    /** The ratings an indicator's effective LRs imply, for every hypothesis ({@code NA} where it has no LR). */
    public static Map<String, String> ratingsFromLr(List<String> hypothesisIds, Map<String, Double> lrByHypothesis) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String id : hypothesisIds) {
            out.put(id, LikelihoodRatios.rating(lrByHypothesis.get(id)));
        }
        return out;
    }

    /** Computes the matrix. */
    public static Result compute(List<String> hypothesisIds, List<Row> rows) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String id : hypothesisIds) {
            scores.put(id, 0.0);
        }
        List<String> nonDiagnostic = new ArrayList<>();
        Map<String, Double> weights = new LinkedHashMap<>();
        for (Row row : rows) {
            double w = weight(row.credibility(), row.relevance());
            weights.put(row.id(), w);
            String first = null;
            boolean same = true;
            for (String id : hypothesisIds) {
                String rating = row.ratings().getOrDefault(id, "NA");
                scores.put(id, scores.get(id) - w * inconsistency(rating));
                if (first == null) {
                    first = rating;
                } else if (!first.equals(rating)) {
                    same = false;
                }
            }
            if (same && !hypothesisIds.isEmpty()) {
                nonDiagnostic.add(row.id());
            }
        }
        String best = null;
        for (String id : hypothesisIds) {
            if (best == null || scores.get(id) > scores.get(best) + 1e-12) {
                best = id;
            }
        }
        // A negative zero reads oddly in JSON; normalize it.
        scores.replaceAll((k, v) -> v == 0.0 ? 0.0 : v);
        return new Result(scores, best, nonDiagnostic, weights);
    }
}
