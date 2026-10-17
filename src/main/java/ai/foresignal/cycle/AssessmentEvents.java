package ai.foresignal.cycle;

import ai.foresignal.forecast.ForecastEvents;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes the {@code assessments} topic's events (prompt 128) to {@code assessment_log} and publishes each to live SSE
 * subscribers after the transaction commits: {@code assessment.published} and {@code cycle.run_failed}.
 */
@Component
public class AssessmentEvents {

    /** An assessment was published. */
    public static final String PUBLISHED = "assessment.published";

    /** A cycle run failed. */
    public static final String RUN_FAILED = "cycle.run_failed";

    private final AssessmentLogRepository logs;
    private final AssessmentsTopic topic;
    private final Clock clock;

    /** Creates the component. */
    public AssessmentEvents(AssessmentLogRepository logs, AssessmentsTopic topic, Clock clock) {
        this.logs = logs;
        this.topic = topic;
        this.clock = clock;
    }

    /** The {@code AssessmentPublishedEvent} payload. */
    public static Map<String, Object> publishedPayload(AssessmentEntity a, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("assessmentId", a.getId());
        out.put("cycleId", a.getCycleId());
        out.put("situationId", a.getSituationId());
        out.put("cadence", a.getCadence());
        out.put("status", a.getStatus());
        out.put("periodStart", a.getPeriodStart().toString());
        out.put("periodEnd", a.getPeriodEnd().toString());
        out.put("headline", a.getHeadline());
        out.put("private", a.isPrivateAssessment());
        out.put("at", at.toString());
        return out;
    }

    /** The {@code CycleRunFailedEvent} payload. */
    public static Map<String, Object> failedPayload(CycleRunEntity run, String reason, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("runId", run.getId());
        out.put("cycleId", run.getCycleId());
        out.put("situationId", run.getSituationId());
        out.put("cadence", run.getCadence());
        out.put("reason", reason == null ? "unknown" : reason);
        out.put("at", at.toString());
        return out;
    }

    /** Emits {@code assessment.published}. */
    public void published(AssessmentEntity a) {
        Instant now = clock.instant();
        write(new AssessmentLogEntity(PUBLISHED, publishedPayload(a, now), a.isPrivateAssessment(), now));
    }

    /** Emits {@code cycle.run_failed}. */
    public void runFailed(CycleRunEntity run, String reason, boolean privateRun) {
        Instant now = clock.instant();
        write(new AssessmentLogEntity(RUN_FAILED, failedPayload(run, reason, now), privateRun, now));
    }

    private void write(AssessmentLogEntity row) {
        AssessmentLogEntity saved = logs.save(row);
        ForecastEvents.afterCommit(() -> topic.publish(saved));
    }
}
