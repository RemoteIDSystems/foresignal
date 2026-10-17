package ai.foresignal.forecast;

/**
 * Told when a ledger question resolves (prompt 125): hypothesis sets confirm or refute their hypotheses, and
 * {@code question} indicators are observed or absent. The ledger calls every listener once the resolving transaction
 * has committed, so a failing listener never undoes a resolution.
 */
public interface QuestionResolutionListener {

    /**
     * A question resolved.
     *
     * @param questionId the question
     * @param outcome the resolved outcome label ({@code yes}/{@code no} for binary questions), or null for numeric ones
     */
    void questionResolved(String questionId, String outcome);
}
