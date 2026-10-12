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
 * The {@code system} topic (prompt 018): persisted notifications with {@code event = system} (a
 * source failing, a rule storm, a delivery failing for good) as {@code system} events, cursor =
 * notification id (key {@code s}).
 */
@Component
public class SystemTopic extends AbstractNotificationTopic {

    /** The topic name. */
    public static final String NAME = "system";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "s";

    private final NotificationRepository repository;

    /** Creates the topic. */
    public SystemTopic(NotificationBus bus, SourceRegistry registry, NotificationRepository repository) {
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
        return 30;
    }

    @Override
    String eventName() {
        return "system";
    }

    @Override
    boolean carries(NotificationMessage message) {
        return NotificationMessage.SYSTEM.equals(message.event());
    }

    @Override
    List<NotificationEntity> page(long cursor, int limit) {
        return repository.findByEventAndIdGreaterThanOrderByIdAsc(NotificationMessage.SYSTEM, cursor, Limit.of(limit));
    }

    @Override
    public long head() {
        return repository.findFirstByEventOrderByIdDesc(NotificationMessage.SYSTEM).map(NotificationEntity::getId)
                .orElse(0L);
    }
}
