package ai.foresignal.evidence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fans canonical-event lifecycle events out to in-process listeners (prompt 116): the SSE topic
 * {@code evidence}. Delivery is synchronous and best effort; a failing listener never stops the resolver.
 */
@Component
public class EvidenceEventBus {

    private static final Logger log = LoggerFactory.getLogger(EvidenceEventBus.class);

    private final List<Consumer<EvidenceEvent>> listeners = new CopyOnWriteArrayList<>();

    /** Adds a listener; the returned handle removes it. */
    public AutoCloseable subscribe(Consumer<EvidenceEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /** Publishes an event to every listener. */
    public void publish(EvidenceEvent event) {
        for (Consumer<EvidenceEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                log.warn("An evidence listener failed: {}", e.toString());
            }
        }
    }
}
