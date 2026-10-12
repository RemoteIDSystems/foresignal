package ai.foresignal.stream.sse;

import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.NotificationBus;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationStore;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.NotificationEntity;
import ai.foresignal.stream.Subscription;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import tools.jackson.databind.JsonNode;

/**
 * The shared machinery of the {@code notifications} and {@code system} topics (prompt 018):
 * persisted notifications on the FIFO lane with the notification id as the cursor, replayed from the
 * {@code notification} table and published live from the {@link NotificationBus} (which delivers in
 * id order after commit).
 */
abstract class AbstractNotificationTopic implements EventTopic<NotificationFilter> {

    /** Notifications per replay page. */
    static final int REPLAY_PAGE = 500;

    private final NotificationBus bus;
    private final SourceRegistry registry;
    private volatile ai.foresignal.trigger.AlertStore alerts;

    AbstractNotificationTopic(NotificationBus bus, SourceRegistry registry) {
        this.bus = bus;
        this.registry = registry;
    }

    /**
     * The alert store, for the record snapshot of alert notifications under a spatial restriction
     * (prompt 035). Optional so the topic still works in slices without the trigger engine.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setAlertStore(ai.foresignal.trigger.AlertStore alerts) {
        this.alerts = alerts;
    }

    /** The SSE event name. */
    abstract String eventName();

    /** True when the topic carries {@code message}. */
    abstract boolean carries(NotificationMessage message);

    /** One replay page after {@code cursor}, oldest first. */
    abstract List<NotificationEntity> page(long cursor, int limit);

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<NotificationFilter> filterType() {
        return NotificationFilter.class;
    }

    @Override
    public NotificationFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return filter(principal);
    }

    @Override
    public NotificationFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && !block.isNull() && !block.isObject()) {
            throw ValidationException.forField(name(), "must be an object");
        }
        return filter(principal);
    }

    /** The filter of {@code principal}: private sources hidden, its restriction applied (prompt 035). */
    private NotificationFilter filter(StreamPrincipal principal) {
        return new NotificationFilter(excludedSources(principal), principal.restriction(), id -> {
            ai.foresignal.trigger.AlertStore store = alerts;
            return store == null ? null : store.find(id).map(a -> a.recordSnapshot()).orElse(null);
        });
    }

    private Set<String> excludedSources(StreamPrincipal principal) {
        // Private sources: agent and admin tokens only (SourceVisibilityGate, prompt 023).
        return ai.foresignal.api.support.ReadCallerResolver.withRestriction(registry,
                SourceVisibilityGate.excludedSources(registry, principal.scope(), TokenRestrictions.NONE),
                principal.restriction());
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, NotificationFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<NotificationEntity> page = page(cursor, REPLAY_PAGE);
            for (NotificationEntity entity : page) {
                NotificationMessage message = NotificationStore.toMessage(entity);
                if (filter.matches(message) && !sink.emit(event(message))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = entity.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    private FifoEvent event(NotificationMessage message) {
        return new FifoEvent(name(), message.id(), eventName(), NotificationJson.toMap(message), null);
    }

    @Override
    public Subscription subscribe(SseHub hub) {
        AutoCloseable handle = bus.subscribe(message -> {
            if (message.id() != null && carries(message)) {
                hub.publish(name(), filter -> ((NotificationFilter) filter).matches(message) ? event(message) : null);
            }
        });
        String name = "sse-" + name();
        return new Subscription() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void close() {
                try {
                    handle.close();
                } catch (Exception e) {
                    // removing a listener from a list does not fail
                }
            }
        };
    }
}
