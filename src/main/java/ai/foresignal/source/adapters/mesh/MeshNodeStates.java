package ai.foresignal.source.adapters.mesh;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The per-node merge state of one stream (prompt 083): at most {@link #DEFAULT_CAPACITY} nodes, least recently used
 * first out, so a busy public mesh cannot grow memory without bound. Thread-safe: MQTT messages are handled on
 * concurrent virtual threads.
 */
public final class MeshNodeStates {

    /** The default bound. */
    public static final int DEFAULT_CAPACITY = 20_000;

    private final int capacity;
    private final LinkedHashMap<String, MeshNodeState> states;

    /** A map of {@link #DEFAULT_CAPACITY} nodes. */
    public MeshNodeStates() {
        this(DEFAULT_CAPACITY);
    }

    /** A map of at most {@code capacity} nodes. */
    public MeshNodeStates(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.states = new LinkedHashMap<>(16, 0.75f, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, MeshNodeState> eldest) {
                return size() > MeshNodeStates.this.capacity;
            }
        };
    }

    /**
     * Applies {@code update} to the state of {@code externalId} (created when absent) under the lock and returns what
     * it returns, typically the record built from the merged state.
     */
    public synchronized <T> T update(String externalId, String network, Function<MeshNodeState, T> update) {
        MeshNodeState state = states.computeIfAbsent(externalId, id -> new MeshNodeState(id, network));
        return update.apply(state);
    }

    /** Applies {@code visit} to every state under the lock (repeater re-evaluation). */
    public synchronized void forEach(java.util.function.Consumer<MeshNodeState> visit) {
        states.values().forEach(visit);
    }

    /** The number of nodes held. */
    public synchronized int size() {
        return states.size();
    }

    /** True when {@code externalId} is held (does not count as a use). */
    public synchronized boolean contains(String externalId) {
        return states.containsKey(externalId);
    }
}
