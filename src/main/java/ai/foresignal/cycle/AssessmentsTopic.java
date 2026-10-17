package ai.foresignal.cycle;

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
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code assessments} SSE topic on {@code /v1/events/stream} (prompt 128): {@code assessment.published}
 * ({@code AssessmentPublishedEvent}) and {@code cycle.run_failed} ({@code CycleRunFailedEvent}) on the FIFO lane,
 * cursor key {@value #CURSOR_KEY} = the {@code assessment_log} id. It has no filter fields; private assessments reach
 * only callers with private access. A client resyncs with {@code GET /v1/assessments}.
 */
@Component
public class AssessmentsTopic implements EventTopic<AssessmentFilter> {

    /** The topic name. */
    public static final String NAME = "assessments";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "m";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    private final AssessmentLogRepository logs;
    private volatile SseHub hub;

    /** Creates the topic. */
    public AssessmentsTopic(AssessmentLogRepository logs) {
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
        return 75;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<AssessmentFilter> filterType() {
        return AssessmentFilter.class;
    }

    @Override
    public AssessmentFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new AssessmentFilter(seesPrivate(principal));
    }

    @Override
    public AssessmentFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && !block.isNull()) {
            if (!block.isObject()) {
                throw ValidationException.forField(NAME, "must be an object");
            }
            for (String field : block.propertyNames()) {
                throw ValidationException.forField(NAME + "." + field, "is not an assessments filter field");
            }
        }
        return new AssessmentFilter(seesPrivate(principal));
    }

    /** The private-source rule of SSE: a caller that may see private sources and whose restriction allows it. */
    static boolean seesPrivate(StreamPrincipal principal) {
        return SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && (principal.restriction() == null || !principal.restriction().excludePrivate());
    }

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(AssessmentLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, AssessmentFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<AssessmentLogEntity> page = logs.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(REPLAY_PAGE));
            for (AssessmentLogEntity row : page) {
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
    static FifoEvent frame(AssessmentLogEntity row) {
        return new FifoEvent(NAME, row.getId(), row.getEvent(), row.getPayload(), null);
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        return new Subscription() {
            @Override
            public String name() {
                return "sse-assessments";
            }

            @Override
            public void close() {
                hub = null;
            }
        };
    }

    /** Publishes a committed log row to live subscribers. */
    public void publish(AssessmentLogEntity row) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((AssessmentFilter) filter).matches(row.isPrivateEvent()) ? frame(row)
                    : null);
        }
    }
}
