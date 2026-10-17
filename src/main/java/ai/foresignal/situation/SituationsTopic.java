package ai.foresignal.situation;

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
import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code situations} SSE topic on {@code /v1/events/stream} (prompt 118): situation lifecycle events on the FIFO
 * lane, cursor key {@value #CURSOR_KEY} = the {@code situation_log} id. Events are {@code situation.created},
 * {@code .updated}, {@code .activated}, {@code .escalated}, {@code .dormant}, {@code .closed} and {@code .merged};
 * the payload is the {@code SituationSummary} plus its {@code version} for upsert, so a client resyncs with
 * {@code GET /v1/situations?since}. Optional filters {@code situations.status} and {@code situations.theme}. A
 * caller without private access never sees a private situation (EGR-1); replay sends the current state of each
 * situation the log names.
 */
@Component
public class SituationsTopic implements EventTopic<SituationFilter>, SituationListener {

    /** The topic name. */
    public static final String NAME = "situations";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "u";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    /** The PATCH block fields. */
    static final Set<String> JSON_FIELDS = Set.of("status", "theme");

    private final SituationLogRepository logs;
    private final SituationRepository situations;
    private final SituationEventBus bus;
    private final Clock clock;
    private volatile SseHub hub;

    /** Creates the topic. */
    public SituationsTopic(SituationLogRepository logs, SituationRepository situations, SituationEventBus bus,
            Clock clock) {
        this.logs = logs;
        this.situations = situations;
        this.bus = bus;
        this.clock = clock;
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
        return 55;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<SituationFilter> filterType() {
        return SituationFilter.class;
    }

    @Override
    public SituationFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new SituationFilter(seesPrivate(principal), validStatus(params.single("status"), NAME + ".status"),
                validTheme(params.single("theme"), NAME + ".theme"));
    }

    @Override
    public SituationFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block == null || block.isNull()) {
            return new SituationFilter(seesPrivate(principal), null, null);
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!JSON_FIELDS.contains(field)) {
                throw ValidationException.forField(NAME + "." + field, "is not a situations filter field");
            }
        }
        return new SituationFilter(seesPrivate(principal), validStatus(text(block, "status"), NAME + ".status"),
                validTheme(text(block, "theme"), NAME + ".theme"));
    }

    private static String text(JsonNode block, String field) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField(NAME + "." + field, "must be a string");
        }
        return node.asString();
    }

    private static String validStatus(String status, String field) {
        if (status != null && !SituationLifecycle.ALL.contains(status)) {
            throw ValidationException.forField(field, "must be one of " + SituationLifecycle.ALL);
        }
        return status;
    }

    private static String validTheme(String theme, String field) {
        if (theme != null && !SituationThemes.valid(theme)) {
            throw ValidationException.forField(field, "must be one of " + SituationThemes.ALL);
        }
        return theme;
    }

    /** The private-source rule of SSE: a caller that may see private sources and whose restriction allows it. */
    static boolean seesPrivate(StreamPrincipal principal) {
        return SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && (principal.restriction() == null || !principal.restriction().excludePrivate());
    }

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(SituationLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, SituationFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<SituationLogEntity> page = logs.findByIdGreaterThanAndEventIsNotNullOrderByIdAsc(cursor,
                    Limit.of(REPLAY_PAGE));
            for (SituationLogEntity log : page) {
                SituationEvent event = of(log);
                if (event != null && filter.matches(event) && !sink.emit(frame(event))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = log.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** The current-state stream event of a log row, or null when the situation no longer exists. */
    SituationEvent of(SituationLogEntity log) {
        SituationEntity situation = situations.findById(log.getSituationId()).orElse(null);
        if (situation == null) {
            return null;
        }
        Object previousStatus = log.getBefore() == null ? null : log.getBefore().get("status");
        Object previousSeverity = log.getBefore() == null ? null : log.getBefore().get("severity");
        return new SituationEvent(log.getId(), log.getEvent(), situation.getId(), situation.getVersion(),
                SituationMapper.summary(situation, clock.instant()),
                previousStatus == null ? null : previousStatus.toString(),
                previousSeverity == null ? null : previousSeverity.toString(), situation.getMergedInto(), log.getAt());
    }

    /** The frame of an event. */
    FifoEvent frame(SituationEvent event) {
        return new FifoEvent(NAME, event.cursor(), event.event(), event.payload(), null);
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        Subscription subscription = bus.subscribe("sse-situations", this, SituationEventBus.DEFAULT_QUEUE);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-situations";
            }

            @Override
            public void close() {
                subscription.close();
                hub = null;
            }
        };
    }

    @Override
    public void onEvent(SituationEvent event) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((SituationFilter) filter).matches(event) ? frame(event) : null);
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
