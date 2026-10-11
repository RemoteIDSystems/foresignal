package ai.foresignal.ingest;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * The default {@link PersistGate}: no rules, so it never flips and needs nothing loaded.
 *
 * <p>Prompt 017's {@code TriggerPersistGate} is {@code @Primary} and takes over when it exists.
 */
@Component
public class NoopPersistGate implements PersistGate {

    private final AtomicReference<GateSnapshot> snapshot = new AtomicReference<>(GateSnapshot.EMPTY);

    @Override
    public GateSnapshot snapshot() {
        return snapshot.get();
    }
}
