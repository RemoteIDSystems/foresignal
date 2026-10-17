package ai.foresignal.analytics.finding;

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
 * The {@code analytics} SSE topic on {@code /v1/events/stream} (prompt 119): finding events on the FIFO lane, cursor
 * key {@value #CURSOR_KEY} = the {@code analytic_finding_log} id. Events are {@code finding.created},
 * {@code finding.updated} and {@code finding.dismissed} with the {@code FindingEvent} payload; a client resyncs with
 * {@code GET /v1/analytics/findings?since}. It has no filter fields; a caller without private access never sees a
 * private finding (EGR-1). Later prompts (120, 126, 127) add their events to this topic.
 */
@Component
public class AnalyticsTopic implements EventTopic<FindingFilter>, FindingListener {

    /** The topic name. */
    public static final String NAME = "analytics";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "f";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    private final FindingLogRepository logs;
    private final FindingService findings;
    private final FindingEventBus bus;
    private volatile SseHub hub;

    /** Creates the topic. */
    public AnalyticsTopic(FindingLogRepository logs, FindingService findings, FindingEventBus bus) {
        this.logs = logs;
        this.findings = findings;
        this.bus = bus;
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
        return 60;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<FindingFilter> filterType() {
        return FindingFilter.class;
    }

    @Override
    public FindingFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new FindingFilter(seesPrivate(principal));
    }

    @Override
    public FindingFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && !block.isNull()) {
            if (!block.isObject()) {
                throw ValidationException.forField(NAME, "must be an object");
            }
            for (String field : block.propertyNames()) {
                throw ValidationException.forField(NAME + "." + field, "is not an analytics filter field");
            }
        }
        return new FindingFilter(seesPrivate(principal));
    }

    /** The private-source rule of SSE: a caller that may see private sources and whose restriction allows it. */
    static boolean seesPrivate(StreamPrincipal principal) {
        return SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && (principal.restriction() == null || !principal.restriction().excludePrivate());
    }

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(FindingLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, FindingFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<FindingLogEntity> page = logs.findByIdGreaterThanAndEventIsNotNullOrderByIdAsc(cursor,
                    Limit.of(REPLAY_PAGE));
            for (FindingLogEntity row : page) {
                FindingEvent event = findings.eventOf(row);
                if (event != null && filter.matches(event) && !sink.emit(frame(event))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = row.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** The frame of an event. */
    FifoEvent frame(FindingEvent event) {
        return new FifoEvent(NAME, event.cursor(), event.event(), event.payload(), null);
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        Subscription subscription = bus.subscribe("sse-analytics", this, FindingEventBus.DEFAULT_QUEUE);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-analytics";
            }

            @Override
            public void close() {
                subscription.close();
                hub = null;
            }
        };
    }

    @Override
    public void onEvent(FindingEvent event) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((FindingFilter) filter).matches(event) ? frame(event) : null);
        }
    }

    @Override
    public void onGap() {
        SseHub target = hub;
        if (target != null) {
            target.gap(NAME);
        }
    }
}
