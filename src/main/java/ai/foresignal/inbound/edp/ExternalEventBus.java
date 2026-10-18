package ai.foresignal.inbound.edp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fans the events of the SSE topic {@code external} out to in-process listeners (prompt 138). Provider lifecycle
 * changes ({@link ExternalSourceChangedEvent}) arrive after their transaction commits and become
 * {@code external.source.state}; the binding service publishes {@code external.binding.disabled} directly. Delivery
 * is synchronous and best effort; a failing listener never stops the publisher.
 */
@Component
public class ExternalEventBus {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventBus.class);

    private final List<Consumer<ExternalEvent>> listeners = new CopyOnWriteArrayList<>();

    /** Adds a listener; the returned handle removes it. */
    public AutoCloseable subscribe(Consumer<ExternalEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /** Publishes an event to every listener. */
    public void publish(ExternalEvent event) {
        for (Consumer<ExternalEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                log.warn("An external-topic listener failed: {}", e.toString());
            }
        }
    }

    /** Turns a committed provider lifecycle change into {@code external.source.state}. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSourceChanged(ExternalSourceChangedEvent change) {
        publish(ExternalEvent.sourceState(change));
    }
}
