package ai.foresignal.notify.inbox;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.NotificationBus;
import ai.foresignal.notify.NotificationMessage;
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
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code inbox} topic behind {@code GET /v1/notifications/stream} (prompt 033), through the
 * generic SSE machinery of 015: {@code notification} events with {@code id:} = notification id,
 * replayed from the table after {@code Last-Event-ID} with this device's read state, and live from
 * the {@link NotificationBus}; plus id-less {@code badge} events to every open stream of the device
 * whose read state changed ({@link InboxBadgeChangedEvent}), so other windows clear their badge.
 *
 * <p>The filter carries the device, so the topic is opened only by the dedicated endpoint; asking
 * for it on {@code /v1/events/stream} is a {@code 400}.
 */
@Component
public class InboxTopic implements EventTopic<InboxFilter> {

    /** The topic name. */
    public static final String NAME = "inbox";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "i";

    /** Items per replay page. */
    static final int REPLAY_PAGE = 500;

    private final NotificationBus bus;
    private final InboxService inbox;
    private volatile SseHub hub;

    /** Creates the topic. */
    public InboxTopic(NotificationBus bus, InboxService inbox) {
        this.bus = bus;
        this.inbox = inbox;
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
        return 30;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<InboxFilter> filterType() {
        return InboxFilter.class;
    }

    @Override
    public InboxFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        throw ValidationException.forField("topics", "inbox is served at GET /v1/notifications/stream only");
    }

    @Override
    public InboxFilter parseJson(JsonNode block, StreamPrincipal principal) {
        throw ValidationException.forField("topics", "inbox is served at GET /v1/notifications/stream only");
    }

    @Override
    public long head() {
        return inbox.head();
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, InboxFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<InboxService.Item> page = inbox.after(filter.deviceKey(), cursor, REPLAY_PAGE, filter.excludedSources(),
                    filter.restriction());
            for (InboxService.Item item : page) {
                if (!sink.emit(event(item))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = item.id();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    @Override
    public Subscription subscribe(SseHub sseHub) {
        this.hub = sseHub;
        AutoCloseable handle = bus.subscribe(message -> {
            if (message.id() != null && InboxService.EVENTS.contains(message.event())) {
                InboxService.Item item = item(message);
                String source = message.data() == null ? null : (String) asText(message.data().get("sourceId"));
                sseHub.publish(NAME, filter -> {
                    InboxFilter f = (InboxFilter) filter;
                    if (source != null && f.excludedSources().contains(source)) {
                        return null;
                    }
                    if (f.restriction().hasDataRestrictions()
                            && !new ai.foresignal.stream.sse.NotificationFilter(f.excludedSources(), f.restriction(),
                                    inbox::alertSnapshot).matches(message)) {
                        // Outside the caller's read restriction (prompt 035).
                        return null;
                    }
                    return event(item);
                });
            }
        });
        return new Subscription() {
            @Override
            public String name() {
                return "sse-" + NAME;
            }

            @Override
            public void close() {
                try {
                    handle.close();
                } catch (Exception e) {
                    // removing a listener from a list does not fail
                }
                hub = null;
            }
        };
    }

    /** Sends the new badge to every open stream of the device. */
    @EventListener
    public void onBadgeChanged(InboxBadgeChangedEvent event) {
        SseHub current = hub;
        if (current != null) {
            // The device's badge counts every notification; a restricted stream reads its own badge
            // through GET /v1/notifications/badge instead (prompt 035).
            current.publishControl(NAME, filter -> ((InboxFilter) filter).deviceKey().equals(event.deviceKey())
                            && !((InboxFilter) filter).restriction().hasDataRestrictions(),
                    "badge", event.badge().toMap());
        }
    }

    /** The live item of a just-persisted message: unread, with the alert state it carried. */
    static InboxService.Item item(NotificationMessage m) {
        Object state = m.data() == null ? null : m.data().get("state");
        return new InboxService.Item(m.id(), m.event(), m.subtype(), m.severity(), m.title(), m.bodyMarkdown(),
                m.alertId(), m.briefingRunId(), m.ruleId(), m.createdAt() == null ? java.time.Instant.EPOCH : m.createdAt(),
                false, NotificationMessage.ALERT.equals(m.event()) && state != null ? String.valueOf(state) : null, List.of());
    }

    private static FifoEvent event(InboxService.Item item) {
        Map<String, Object> data = new java.util.LinkedHashMap<>(item.toMap());
        return new FifoEvent(NAME, item.id(), "notification", data, null);
    }

    private static Object asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
