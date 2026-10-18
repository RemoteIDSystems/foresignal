package ai.foresignal.social;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayOutcome;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.ReplaySink;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code social} SSE topic on {@code /v1/events/stream} (prompt 135): {@code social.velocity}
 * ({@code SocialVelocityEvent}) on the FIFO lane, cursor key {@value #CURSOR_KEY} = the {@code social_event_log} id,
 * and the id-less control event {@code social.platform.state} ({@code SocialPlatformStateEvent}); a client that misses
 * one re-reads {@code GET /v1/social/platforms}. No filter fields; events built from private sources reach only
 * callers with private access. 136 and 137 add their events to this topic.
 */
@Component
public class SocialTopic implements EventTopic<SocialFilter> {

    /** The topic name. */
    public static final String NAME = "social";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "o";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    private final SocialEventLogRepository logs;
    private volatile SseHub hub;

    /** Creates the topic. */
    public SocialTopic(SocialEventLogRepository logs) {
        this.logs = logs;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String cursorKey() {
        return CURSOR_KEY;
    }

    @Override
    public int cursorOrder() {
        return 80;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<SocialFilter> filterType() {
        return SocialFilter.class;
    }

    @Override
    public SocialFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new SocialFilter(seesPrivate(principal));
    }

    @Override
    public SocialFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && !block.isNull()) {
            if (!block.isObject()) {
                throw ValidationException.forField(NAME, "must be an object");
            }
            for (String field : block.propertyNames()) {
                throw ValidationException.forField(NAME + "." + field, "is not a social filter field");
            }
        }
        return new SocialFilter(seesPrivate(principal));
    }

    /** The private-source rule of SSE: a caller that may see private sources and whose restriction allows it. */
    static boolean seesPrivate(StreamPrincipal principal) {
        return SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && (principal.restriction() == null || !principal.restriction().excludePrivate());
    }

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(SocialEventLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, SocialFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<SocialEventLogEntity> page = logs.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(REPLAY_PAGE));
            for (SocialEventLogEntity row : page) {
                if (filter.matches(row.isPrivateEvent()) && !sink.emit(frame(row))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = row.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** The frame of a log row. */
    static FifoEvent frame(SocialEventLogEntity row) {
        return new FifoEvent(NAME, row.getId(), row.getEvent(), row.getPayload(), null);
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        return new Subscription() {
            @Override
            public String name() {
                return "sse-social";
            }

            @Override
            public void close() {
                hub = null;
            }
        };
    }

    /** Publishes a committed log row to live subscribers. */
    public void publish(SocialEventLogEntity row) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((SocialFilter) filter).matches(row.isPrivateEvent()) ? frame(row) : null);
        }
    }

    /**
     * Sends an id-less control event to every live subscriber that may see it.
     *
     * @return the subscribers it reached
     */
    public int publishControl(String event, Map<String, Object> payload, boolean privateEvent) {
        SseHub target = hub;
        if (target == null) {
            return 0;
        }
        return target.publishControl(NAME, f -> f instanceof SocialFilter sf && sf.matches(privateEvent), event,
                payload);
    }
}
