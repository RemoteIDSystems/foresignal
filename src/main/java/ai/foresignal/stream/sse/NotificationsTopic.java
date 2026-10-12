package ai.foresignal.stream.sse;

import ai.foresignal.notify.NotificationBus;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationEntity;
import ai.foresignal.store.NotificationRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * The {@code notifications} topic (prompt 018): every persisted notification as a
 * {@code notification} event ({@code NotificationMessage}), cursor = notification id (key {@code n}).
 */
@Component
public class NotificationsTopic extends AbstractNotificationTopic {

    /** The topic name. */
    public static final String NAME = "notifications";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "n";

    private final NotificationRepository repository;

    /** Creates the topic. */
    public NotificationsTopic(NotificationBus bus, SourceRegistry registry, NotificationRepository repository) {
        super(bus, registry);
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
        return 20;
    }

    @Override
    String eventName() {
        return "notification";
    }

    @Override
    boolean carries(NotificationMessage message) {
        return true;
    }

    @Override
    List<NotificationEntity> page(long cursor, int limit) {
        return repository.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(limit));
    }

    @Override
    public long head() {
        return repository.findFirstByOrderByIdDesc().map(NotificationEntity::getId).orElse(0L);
    }
}
