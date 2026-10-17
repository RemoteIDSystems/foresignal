package ai.foresignal.forecast;

import ai.foresignal.forecast.language.EstimativeLanguage;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Writes the {@code forecasts} SSE events (prompt 122): each event is a {@code forecast_log} row written in the
 * caller's transaction and published to live subscribers once it commits.
 */
@Component
public class ForecastEvents {

    /** A question opened. */
    public static final String OPENED = "question.opened";

    /** A question stopped accepting forecasts. */
    public static final String CLOSED = "question.closed";

    /** A question resolved. */
    public static final String RESOLVED = "question.resolved";

    /** A question was annulled. */
    public static final String ANNULLED = "question.annulled";

    /** A forecast was recorded. */
    public static final String SUBMITTED = "forecast.submitted";

    /** Every event name. */
    public static final List<String> ALL = List.of(OPENED, CLOSED, RESOLVED, ANNULLED, SUBMITTED);

    private final ForecastLogRepository logs;
    private final ForecastsTopic topic;
    private final Clock clock;

    /** Creates the component. */
    public ForecastEvents(ForecastLogRepository logs, ForecastsTopic topic, Clock clock) {
        this.logs = logs;
        this.topic = topic;
        this.clock = clock;
    }

    /** The {@code ForecastQuestionEvent} payload. */
    public static Map<String, Object> questionPayload(String event, ForecastQuestionEntity q, long forecastCount,
            Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put("question", ForecastViews.summary(q, forecastCount));
        out.put("at", at.toString());
        return out;
    }

    /** The {@code ForecastSubmittedEvent} payload (summary only). */
    public static Map<String, Object> submittedPayload(ForecastEntity f, boolean privateQuestion) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", SUBMITTED);
        out.put("questionId", f.getQuestionId());
        out.put("forecastId", f.getId());
        out.put("forecasterId", f.getForecasterId());
        out.put("madeAt", f.getMadeAt().toString());
        out.put("probability", f.getProbability());
        out.put("estimativeTerm", EstimativeLanguage.termOrNull(f.getProbability()));
        out.put("private", privateQuestion);
        return out;
    }

    /** Emits a question lifecycle event. */
    public void question(String event, ForecastQuestionEntity q, long forecastCount) {
        Instant now = clock.instant();
        write(new ForecastLogEntity(event, q.getId(), questionPayload(event, q, forecastCount, now),
                q.isPrivateQuestion(), now));
    }

    /** Emits {@code forecast.submitted}. */
    public void submitted(ForecastEntity f, boolean privateQuestion) {
        write(new ForecastLogEntity(SUBMITTED, f.getQuestionId(), submittedPayload(f, privateQuestion),
                privateQuestion, clock.instant()));
    }

    private void write(ForecastLogEntity row) {
        ForecastLogEntity saved = logs.save(row);
        afterCommit(() -> topic.publish(saved));
    }

    /** Runs {@code action} once the current transaction commits, or at once outside one. */
    public static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
