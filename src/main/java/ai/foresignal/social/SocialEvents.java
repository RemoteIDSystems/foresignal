package ai.foresignal.social;

import ai.foresignal.forecast.ForecastEvents;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes the {@code social} topic's events (prompt 135): {@code social.velocity} to {@code social_event_log} and to
 * live subscribers after the transaction commits; {@code social.platform.state} as an id-less control event.
 */
@Component
public class SocialEvents {

    /** A {@code social-velocity} finding was created. */
    public static final String VELOCITY = "social.velocity";

    /** An adapter changed state. */
    public static final String PLATFORM_STATE = "social.platform.state";

    private final SocialEventLogRepository logs;
    private final SocialTopic topic;
    private final Clock clock;

    /** Creates the component. */
    public SocialEvents(SocialEventLogRepository logs, SocialTopic topic, Clock clock) {
        this.logs = logs;
        this.topic = topic;
        this.clock = clock;
    }

    /** The {@code SocialVelocityEvent} body: {@code event} first, then the payload. */
    public static Map<String, Object> velocityBody(Map<String, Object> payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", VELOCITY);
        body.putAll(payload);
        return body;
    }

    /** The {@code SocialPlatformStateEvent} body. */
    public static Map<String, Object> platformStateBody(String platformId, String state, String previous,
            String reason, Map<String, Object> spend, Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", PLATFORM_STATE);
        body.put("platformId", platformId);
        body.put("state", state);
        body.put("previousState", previous);
        body.put("reason", reason);
        body.put("spend", spend);
        body.put("at", at.toString());
        return body;
    }

    /** Emits {@code social.velocity} with a {@code SocialVelocityEvent} payload (without {@code event}). */
    public void velocity(Map<String, Object> payload, boolean privateEvent) {
        Map<String, Object> body = velocityBody(payload);
        SocialEventLogEntity saved = logs.save(new SocialEventLogEntity(VELOCITY, body, privateEvent, clock.instant()));
        ForecastEvents.afterCommit(() -> topic.publish(saved));
    }

    /**
     * Emits a logged event of the {@code social} topic (prompt 136: {@code narrative.emerged},
     * {@code narrative.updated}, {@code claim.extracted}, {@code media.reuse}, {@code coordination.detected}): written to
     * {@code social_event_log} and sent to live subscribers after the transaction commits.
     *
     * @param event the event name
     * @param body the full body, {@code event} first
     * @param privateEvent true when built from a private source
     */
    public void emit(String event, Map<String, Object> body, boolean privateEvent) {
        SocialEventLogEntity saved = logs.save(new SocialEventLogEntity(event, body, privateEvent, clock.instant()));
        ForecastEvents.afterCommit(() -> topic.publish(saved));
    }

    /**
     * Emits {@code social.platform.state} ({@code SocialPlatformStateEvent}).
     *
     * @param platformId the adapter
     * @param state the new state
     * @param previous the previous state, or null
     * @param reason why, or null
     * @param spend the spend view, or null
     */
    public void platformState(String platformId, String state, String previous, String reason,
            Map<String, Object> spend) {
        Map<String, Object> body = platformStateBody(platformId, state, previous, reason, spend, Instant.now(clock));
        ForecastEvents.afterCommit(() -> topic.publishControl(PLATFORM_STATE, body, false));
    }
}
