package ai.foresignal.intel;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayOutcome;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.ReplaySink;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code intel} SSE topic (prompt 101): intelligence-event lifecycle events on the FIFO lane,
 * with the {@code intelligence_event_log} id as the cursor (key {@code t}). Events are
 * {@code intel.event.created|updated|resolved|merged}; the payload is a compact summary with the
 * event {@code version} for upsert, so a client resyncs with {@code GET /v1/intel/events?since}. A
 * caller without the private grant never sees a private event.
 */
@Component
public class IntelTopic implements EventTopic<IntelFilter>, IntelListener {

    /** The topic name. */
    public static final String NAME = "intel";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "t";

    /** Log rows per replay page. */
    static final int REPLAY_PAGE = 500;

    /** The PATCH block fields. */
    static final Set<String> JSON_FIELDS = Set.of("type", "status");

    private final IntelEventLogRepository logs;
    private final IntelligenceEventRepository events;
    private final IntelEventBus bus;
    private final SourceRegistry registry;
    private volatile SourceVisibility visibility;
    private volatile SseHub hub;

    /** Creates the topic. */
    public IntelTopic(IntelEventLogRepository logs, IntelligenceEventRepository events, IntelEventBus bus,
            SourceRegistry registry) {
        this.logs = logs;
        this.events = events;
        this.bus = bus;
        this.registry = registry;
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
        return 40;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<IntelFilter> filterType() {
        return IntelFilter.class;
    }

    // ------------------------------------------------------------------------- filters

    @Override
    public IntelFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new IntelFilter(excludedSources(principal), principal.privateAccess(), params.single("type"),
                params.single("status"));
    }

    @Override
    public IntelFilter parseJson(JsonNode block, StreamPrincipal principal) {
        String prefix = NAME + ".";
        if (block == null || block.isNull()) {
            return new IntelFilter(excludedSources(principal), principal.privateAccess(), null, null);
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!JSON_FIELDS.contains(field)) {
                throw ValidationException.forField(prefix + field, "is not an intel filter field");
            }
        }
        return new IntelFilter(excludedSources(principal), principal.privateAccess(), text(block, "type", prefix),
                text(block, "status", prefix));
    }

    private static String text(JsonNode block, String field, String prefix) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField(prefix + field, "must be a string");
        }
        return node.asString();
    }

    /** Private sources the caller may not see: all of them unless it is admin or holds the grant. */
    Set<String> excludedSources(StreamPrincipal principal) {
        SourceVisibility v = visibility();
        return ai.foresignal.api.support.ReadCallerResolver.withRestriction(registry, v,
                v.hiddenFor(principal.scope(), principal.privateAccess(), TokenRestrictions.NONE),
                principal.restriction());
    }

    // ------------------------------------------------------------------------- resume

    @Override
    public long head() {
        return logs.findTop1ByOrderByIdDesc().map(IntelEventLogEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, IntelFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<IntelEventLogEntity> page = logs.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(REPLAY_PAGE));
            for (IntelEventLogEntity log : page) {
                if (!sink.emit(frame(of(log)))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = log.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** The current-state stream event of a log row. */
    IntelEvent of(IntelEventLogEntity log) {
        IntelligenceEventEntity event = events.findById(log.getEventId()).orElse(null);
        Map<String, Object> summary = event == null ? Map.of("eventId", log.getEventId())
                : IntelEventMapper.summary(event);
        return new IntelEvent(log.getId(), log.getEventId(), log.getAction(), event == null ? 0 : event.getVersion(),
                summary, event == null ? null : event.getMergedInto());
    }

    /** The frame of an event. */
    FifoEvent frame(IntelEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("eventId", event.eventId());
        data.put("change", event.change());
        data.put("version", event.version());
        data.put("mergedInto", event.mergedInto());
        data.put("summary", event.summary());
        return new FifoEvent(NAME, event.cursor(), event.sseEvent(), data, null);
    }

    // ------------------------------------------------------------------------- live

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        Subscription subscription = bus.subscribe("sse-intel", this, IntelEventBus.DEFAULT_QUEUE);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-intel";
            }

            @Override
            public void close() {
                subscription.close();
                hub = null;
            }
        };
    }

    @Override
    public void onEvent(IntelEvent event) {
        SseHub target = hub;
        if (target != null) {
            target.publish(NAME, filter -> ((IntelFilter) filter).matches(event) ? frame(event) : null);
        }
    }

    @Override
    public void onGap() {
        SseHub target = hub;
        if (target != null) {
            target.gap(NAME);
        }
    }

    /** Injects the shared private-source visibility (prompt 039). */
    @Autowired(required = false)
    public void setVisibility(SourceVisibility visibility) {
        this.visibility = visibility;
    }

    private SourceVisibility visibility() {
        SourceVisibility v = visibility;
        if (v == null) {
            v = new SourceVisibility(registry, null, null);
            visibility = v;
        }
        return v;
    }

    /** Kept for parity with the other topics. */
    static Set<String> set(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }
}
