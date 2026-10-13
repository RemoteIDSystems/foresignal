package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The adapter state a probe sees (prompt 047): reads fall through to the persistent store, writes stay
 * in memory and are discarded with the probe.
 *
 * <p>A probe never ingests, so it must not move an adapter's bookkeeping either. Without this, probing
 * {@code nws-alerts} would record the probed alerts as the live set, and the next real fetch would no
 * longer tombstone the ones that had gone; probing a cursor-based source would skip data.
 */
final class ProbeStateOverlay implements AdapterStateStore {

    /** Marks a key removed inside the probe. */
    private static final String REMOVED = "\u0000removed";

    private final AdapterStateStore delegate;
    private final Map<String, String> writes = new ConcurrentHashMap<>();

    ProbeStateOverlay(AdapterStateStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<String> get(String key) {
        String local = writes.get(key);
        if (local != null) {
            return REMOVED.equals(local) ? Optional.empty() : Optional.of(local);
        }
        return delegate.get(key);
    }

    @Override
    public void put(String key, String value) {
        if (value == null) {
            remove(key);
        } else {
            writes.put(key, value);
        }
    }

    @Override
    public void remove(String key) {
        writes.put(key, REMOVED);
    }

    /** The writes made during the probe, for tests. */
    Map<String, String> writes() {
        return Map.copyOf(writes);
    }
}
