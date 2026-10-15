package ai.foresignal.stream.sse;

import ai.foresignal.notify.NotificationBus;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationEntity;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.stream.Subscription;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * The {@code analyst} topic (prompt 102): analyst notes and proposals as SSE events
 * {@code analyst.note} and {@code analyst.proposal}, cursor = notification id (key {@code b}). Live
 * events come from the {@link NotificationBus}; a replay pages the {@code notification} table.
 */
@Component
public class AnalystTopic extends AbstractNotificationTopic {

    /** The topic name. */
    public static final String NAME = "analyst";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "b";

    /** The note event. */
    public static final String NOTE = "analyst.note";

    /** The proposal event. */
    public static final String PROPOSAL = "analyst.proposal";

    private final NotificationRepository repository;
    private final NotificationBus bus;

    /** Creates the topic. */
    public AnalystTopic(NotificationBus bus, SourceRegistry registry, NotificationRepository repository) {
        super(bus, registry);
        this.bus = bus;
        this.repository = repository;
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
        return 50;
    }

    @Override
    String eventName() {
        return NOTE;
    }

    @Override
    boolean carries(NotificationMessage message) {
        return NOTE.equals(message.event()) || PROPOSAL.equals(message.event());
    }

    @Override
    List<NotificationEntity> page(long cursor, int limit) {
        return repository.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(limit));
    }

    @Override
    public long head() {
        return repository.findFirstByOrderByIdDesc().map(NotificationEntity::getId).orElse(0L);
    }

    @Override
    public Subscription subscribe(SseHub hub) {
        AutoCloseable handle = bus.subscribe(message -> {
            if (message.id() == null || !carries(message)) {
                return;
            }
            FifoEvent frame = new FifoEvent(NAME, message.id(), message.event(), NotificationJson.toMap(message), null);
            hub.publish(NAME, filter -> ((NotificationFilter) filter).matches(message) ? frame : null);
        });
        return new Subscription() {
            @Override
            public String name() {
                return "sse-analyst";
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
