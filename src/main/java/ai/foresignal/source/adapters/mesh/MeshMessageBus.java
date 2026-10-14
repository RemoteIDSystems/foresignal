package ai.foresignal.source.adapters.mesh;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The in-process bus of text messages heard by the operator's own mesh nodes (prompt 083): {@code meshtastic-local}
 * and {@code meshcore-local} publish every decoded text here, whether or not the source persists text. Nothing is
 * stored; a subscriber that is not registered when a message arrives never sees it. Prompt 084 subscribes for field
 * reports.
 *
 * <p>Subscribers run synchronously on the publishing thread (a virtual thread of the source's connection), one after
 * another; a subscriber that throws is logged and the others still run. A subscriber must not block for long.
 */
@Component
public class MeshMessageBus {

    private static final Logger log = LoggerFactory.getLogger(MeshMessageBus.class);

    private final List<Consumer<MeshTextMessage>> subscribers = new CopyOnWriteArrayList<>();

    /** Creates an empty bus. */
    public MeshMessageBus() {}

    /**
     * Registers {@code subscriber}.
     *
     * @return closing it unregisters the subscriber
     */
    public AutoCloseable subscribe(Consumer<MeshTextMessage> subscriber) {
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

    /** Hands {@code message} to every subscriber. */
    public void publish(MeshTextMessage message) {
        if (message == null) {
            return;
        }
        for (Consumer<MeshTextMessage> subscriber : subscribers) {
            try {
                subscriber.accept(message);
            } catch (RuntimeException e) {
                log.warn("A mesh message subscriber failed on a message from {}", message.sourceId(), e);
            }
        }
    }

    /** The number of subscribers (tests, diagnostics). */
    public int subscribers() {
        return subscribers.size();
    }
}
