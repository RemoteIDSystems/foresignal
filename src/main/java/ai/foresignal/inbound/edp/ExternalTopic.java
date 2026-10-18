package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The SSE topic {@code external} (prompt 138): {@code external.source.state} (a provider registered, approved,
 * suspended, deleted or its manifest changed) and {@code external.binding.disabled}. Provider data is admin data over
 * HTTP, so the topic needs the {@code admin} scope. Events are live-only (the spec marks them id-less): there is no
 * replay, and a client resyncs with {@code GET /v1/external/sources}. The topic cursor is a process-local counter
 * seeded from the wall clock (microseconds). The filter block {@code external.sourceIds} (or the query parameter
 * {@code external.sourceId}) narrows the stream to some providers.
 */
@Component
public class ExternalTopic implements EventTopic<ExternalTopicFilter> {

    /** The topic name. */
    public static final String NAME = "external";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "x";

    private final ExternalEventBus bus;
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() * 1000);
    private volatile SseHub hub;

    /** Creates the topic. */
    public ExternalTopic(ExternalEventBus bus) {
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
        return 85;
    }

    @Override
    public Scope requiredScope() {
        return Scope.ADMIN;
    }

    @Override
    public Class<ExternalTopicFilter> filterType() {
        return ExternalTopicFilter.class;
    }

    @Override
    public ExternalTopicFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        String source = params.single("sourceId");
        return new ExternalTopicFilter(source == null ? Set.of() : Set.of(source));
    }

    @Override
    public ExternalTopicFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block == null || block.isNull()) {
            return new ExternalTopicFilter(Set.of());
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!"sourceIds".equals(field)) {
                throw ValidationException.forField(NAME + "." + field, "is not an external filter field");
            }
        }
        JsonNode ids = block.get("sourceIds");
        Set<String> out = new LinkedHashSet<>();
        if (ids != null && !ids.isNull()) {
            if (!ids.isArray()) {
                throw ValidationException.forField(NAME + ".sourceIds", "must be an array of source ids");
            }
            for (JsonNode id : ids) {
                if (!id.isString()) {
                    throw ValidationException.forField(NAME + ".sourceIds", "must be an array of source ids");
                }
                out.add(id.asString());
            }
        }
        return new ExternalTopicFilter(out);
    }

    @Override
    public long head() {
        return counter.get();
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, ExternalTopicFilter filter, ReplaySink sink) {
        return new ReplayOutcome.Done(Math.max(request.after(), counter.get()));
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        AutoCloseable handle = bus.subscribe(this::onEvent);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-external";
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
    void onEvent(ExternalEvent event) {
        SseHub target = hub;
        if (target == null) {
            return;
        }
        long cursor = counter.incrementAndGet();
        target.publish(NAME, filter -> ((ExternalTopicFilter) filter).matches(event)
                ? new FifoEvent(NAME, cursor, event.type(), new LinkedHashMap<>(event.payload()), null)
                : null);
    }
}
