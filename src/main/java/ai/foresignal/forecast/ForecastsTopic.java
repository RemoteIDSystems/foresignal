package ai.foresignal.forecast;

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
 * The {@code forecasts} SSE topic on {@code /v1/events/stream} (prompt 122): {@code question.opened},
 * {@code question.closed}, {@code question.resolved}, {@code question.annulled} ({@code ForecastQuestionEvent}) and
 * {@code forecast.submitted} ({@code ForecastSubmittedEvent}, summary only) on the FIFO lane, cursor key
 * {@value #CURSOR_KEY} = the {@code forecast_log} id. It has no filter fields; private questions reach only callers
 * with private access (EGR-1). A client resyncs with {@code GET /v1/forecasts/questions}.
 */
@Component
public class ForecastsTopic implements EventTopic<ForecastFilter> {

    /** The topic name. */
    public static final String NAME = "forecasts";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "q";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    private final ForecastLogRepository logs;
    private volatile SseHub hub;

    /** Creates the topic. */
    public ForecastsTopic(ForecastLogRepository logs) {
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
        return 65;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<ForecastFilter> filterType() {
        return ForecastFilter.class;
    }

    @Override
    public ForecastFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new ForecastFilter(seesPrivate(principal));
    }

    @Override
    public ForecastFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && !block.isNull()) {
            if (!block.isObject()) {
                throw ValidationException.forField(NAME, "must be an object");
            }
            for (String field : block.propertyNames()) {
                throw ValidationException.forField(NAME + "." + field, "is not a forecasts filter field");
            }
        }
        return new ForecastFilter(seesPrivate(principal));
    }

    /** The private-source rule of SSE: a caller that may see private sources and whose restriction allows it. */
    static boolean seesPrivate(StreamPrincipal principal) {
        return SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && (principal.restriction() == null || !principal.restriction().excludePrivate());
    }

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(ForecastLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, ForecastFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<ForecastLogEntity> page = logs.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(REPLAY_PAGE));
            for (ForecastLogEntity row : page) {
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
    static FifoEvent frame(ForecastLogEntity row) {
        return new FifoEvent(NAME, row.getId(), row.getEvent(), row.getPayload(), null);
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        return new Subscription() {
            @Override
            public String name() {
                return "sse-forecasts";
            }

            @Override
            public void close() {
                hub = null;
            }
        };
    }

    /** Publishes a committed log row to live subscribers. */
    public void publish(ForecastLogEntity row) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((ForecastFilter) filter).matches(row.isPrivateEvent()) ? frame(row)
                    : null);
        }
    }
}
