package ai.foresignal.notify;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Announces every persisted notification, in id order, after its transaction commits (prompt 018).
 * The {@code notifications} and {@code system} SSE topics listen here; listeners must not block,
 * because they run on the publishing thread inside {@link NotificationStore}'s ordering lock.
 */
@Component
public class NotificationBus {

    private static final Logger log = LoggerFactory.getLogger(NotificationBus.class);

    private final List<Consumer<NotificationMessage>> listeners = new CopyOnWriteArrayList<>();

    /** Adds a listener; the returned handle removes it. */
    public AutoCloseable subscribe(Consumer<NotificationMessage> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /** Hands {@code message} to every listener; a throwing listener is logged and skipped. */
    public void publish(NotificationMessage message) {
        for (Consumer<NotificationMessage> listener : listeners) {
            try {
                listener.accept(message);
            } catch (RuntimeException e) {
                log.error("Notification listener failed on notification {}", message.id(), e);
            }
        }
    }
}
