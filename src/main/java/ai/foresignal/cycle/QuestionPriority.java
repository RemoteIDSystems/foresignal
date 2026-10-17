package ai.foresignal.cycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Chooses the questions the forecaster role works on (prompt 128 §3), by <strong>value of information</strong>:
 * {@code stakes × uncertainty}, where stakes come from the linked situation's severity (critical 3, warning 2, info
 * 1, none 1) and uncertainty is {@code 4 · p · (1 − p)} of the current ensemble (1 without an ensemble forecast). Ties
 * go to the earlier due date, then to the question this forecaster forecast least recently (never first). Pure.
 */
public final class QuestionPriority {

    private QuestionPriority() {}

    /**
     * One candidate question.
     *
     * @param questionId the question
     * @param situationSeverity the linked situation's severity, or null
     * @param ensembleProbability the current ensemble probability (binary), or null
     * @param resolveBy the due date
     * @param lastForecastAt when this forecaster last forecast it, or null
     */
    public record Candidate(String questionId, String situationSeverity, Double ensembleProbability, Instant resolveBy,
            Instant lastForecastAt) {

        /** The value of information. */
        public double value() {
            double p = ensembleProbability == null ? Double.NaN : ensembleProbability;
            double uncertainty = Double.isNaN(p) ? 1.0 : 4 * p * (1 - p);
            return CyclePacketReader.stakes(situationSeverity) * uncertainty;
        }
    }

    /** The order: value descending, due date ascending, least recently forecast first. */
    static final Comparator<Candidate> ORDER = Comparator.comparingDouble(Candidate::value).reversed()
            .thenComparing(Candidate::resolveBy, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Candidate::lastForecastAt, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Candidate::questionId);

    /** The first {@code max} candidates in priority order. */
    public static List<Candidate> choose(List<Candidate> candidates, int max) {
        List<Candidate> sorted = new ArrayList<>(candidates);
        sorted.sort(ORDER);
        return sorted.size() > max ? List.copyOf(sorted.subList(0, Math.max(0, max))) : List.copyOf(sorted);
    }
}
