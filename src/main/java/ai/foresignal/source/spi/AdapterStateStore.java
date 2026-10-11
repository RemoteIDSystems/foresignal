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
}
