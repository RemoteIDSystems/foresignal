package ai.foresignal.forecast;

/**
 * What the ledger does after its own writes (prompt 122): baseline forecasts on a new question, an ensemble refresh
 * after each non-ensemble forecast, and scores after a resolution. One component implements it
 * ({@code LedgerCoordinator}); {@link ForecastService} calls it so that the services stay acyclic.
 */
public interface LedgerHooks {

    /** A question was opened (called after its transaction commits). */
    void questionOpened(String questionId);

    /** A non-ensemble forecast was recorded (called inside the recording transaction). */
    void forecastRecorded(ForecastQuestionEntity question, ForecastEntity forecast);

    /** A question was resolved (called inside the resolving transaction). */
    void questionResolved(ForecastQuestionEntity question);

    /**
     * A question was annulled (called inside the annulling transaction). The default does nothing.
     *
     * @param question the annulled question
     */
    default void questionAnnulled(ForecastQuestionEntity question) {
        // nothing by default
    }
}
