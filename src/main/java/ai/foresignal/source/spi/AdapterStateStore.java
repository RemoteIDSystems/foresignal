package ai.foresignal.source.spi;

import java.util.Optional;

/**
 * A small per-source key/value store for an adapter's own bookkeeping: an upstream cursor, an
 * ETag, a last-seen sequence number.
 *
 * <p>Persisted in {@code source_health.adapter_state}, so it survives restarts. Deliberately
 * limited to strings, and not a general escape hatch: anything an adapter needs to keep that is
 * bigger than a cursor is probably a record.
 */
public interface AdapterStateStore {

    /**
     * The reserved key holding a non-fatal health problem the adapter found (prompt 047), for example
     * a missing SPC layer. While it holds a reason, the {@code AdapterStateHealthRule} reports the
     * source {@code degraded} with that reason; the fetch itself still succeeds.
     */
    String DEGRADED_REASON_KEY = "health.degraded";

    /** The stored value, or empty. */
    Optional<String> get(String key);

    /** Stores {@code value}, replacing any previous one. */
    void put(String key, String value);

    /** Removes {@code key}, if present. */
    void remove(String key);

    /** The stored value, or {@code fallback}. */
    default String getOrDefault(String key, String fallback) {
        return get(key).orElse(fallback);
    }

    /**
     * Records a non-fatal problem that keeps the source {@code degraded} until it is cleared
     * (prompt 047). Writes only when the reason changed.
     *
     * @param reason the health reason shown to the operator
     */
    default void degrade(String reason) {
        if (reason == null || reason.isBlank()) {
            clearDegraded();
            return;
        }
        if (!reason.equals(get(DEGRADED_REASON_KEY).orElse(null))) {
            put(DEGRADED_REASON_KEY, reason);
        }
    }

    /** Clears a reason set by {@link #degrade}, if any. */
    default void clearDegraded() {
        if (get(DEGRADED_REASON_KEY).isPresent()) {
            remove(DEGRADED_REASON_KEY);
        }
    }
}
