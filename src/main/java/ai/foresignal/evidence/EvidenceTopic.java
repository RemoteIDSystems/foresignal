package ai.foresignal.evidence;

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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The SSE topic {@code evidence} (prompt 116): {@code canonical.created}, {@code canonical.merged} and
 * {@code canonical.flagged}, summary only. Events are live-only (the spec marks them id-less): there is no
 * replay, and a client resyncs with {@code GET /v1/evidence/events?since}. The topic cursor is a process-local
 * counter seeded from the wall clock (microseconds), so it keeps increasing across restarts. An event built only
 * from sources the caller may not see is withheld.
 */
@Component
public class EvidenceTopic implements EventTopic<EvidenceFilter> {

    /** The topic name. */
    public static final String NAME = "evidence";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "v";

    private final EvidenceEventBus bus;
    private final SourceRegistry registry;
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() * 1000);
    private volatile SourceVisibility visibility;
    private volatile SseHub hub;

    /** Creates the topic. */
    public EvidenceTopic(EvidenceEventBus bus, SourceRegistry registry) {
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
        return 45;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<EvidenceFilter> filterType() {
        return EvidenceFilter.class;
    }

    @Override
    public EvidenceFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return new EvidenceFilter(excludedSources(principal), params.single("family"));
    }

    @Override
    public EvidenceFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block == null || block.isNull()) {
            return new EvidenceFilter(excludedSources(principal), null);
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!"family".equals(field)) {
                throw ValidationException.forField(NAME + "." + field, "is not an evidence filter field");
            }
        }
        JsonNode family = block.get("family");
        if (family != null && !family.isNull() && !family.isString()) {
            throw ValidationException.forField(NAME + ".family", "must be a string");
        }
        return new EvidenceFilter(excludedSources(principal), family == null || family.isNull() ? null
                : family.asString());
    }

    private Set<String> excludedSources(StreamPrincipal principal) {
        SourceVisibility v = visibility();
        return ai.foresignal.api.support.ReadCallerResolver.withRestriction(registry, v,
                v.hiddenFor(principal.scope(), principal.privateAccess(), TokenRestrictions.NONE),
                principal.restriction());
    }

    @Override
    public long head() {
        return counter.get();
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, EvidenceFilter filter, ReplaySink sink) {
        return new ReplayOutcome.Done(Math.max(request.after(), counter.get()));
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        AutoCloseable handle = bus.subscribe(this::onEvent);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-evidence";
            }

            @Override
            public void close() {
                try {
                    handle.close();
                } catch (Exception e) {
                    // nothing to release
                }
                hub = null;
            }
        };
    }

    /** Publishes one event to the subscribed clients whose filter accepts it. */
    void onEvent(EvidenceEvent event) {
        SseHub target = hub;
        if (target == null) {
            return;
        }
        long cursor = counter.incrementAndGet();
        target.publish(NAME, filter -> ((EvidenceFilter) filter).matches(event)
                ? new FifoEvent(NAME, cursor, event.type(), new java.util.LinkedHashMap<>(event.payload()), null)
                : null);
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
}
