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

    /**
     * The reserved key holding the time of the newest upstream input the adapter processed (prompt 048),
     * an ISO-8601 instant: for {@code goes-glm} and {@code goes-fdc} the coverage end of the newest
     * processed file. With {@link #MAX_INPUT_GAP_KEY}, the {@code InputAgeHealthRule} reports the source
     * {@code degraded} once the input is older than the gap.
     */
    String NEWEST_INPUT_KEY = "health.newestInputAt";

    /** The reserved key holding the largest tolerated input age, an ISO-8601 duration (prompt 048). */
    String MAX_INPUT_GAP_KEY = "health.maxInputGap";

    /**
     * The reserved key holding a fatal health problem the adapter found (prompt 049), for example a
     * SatDump {@code /api} that has been unreachable for three polls while the stream itself keeps
     * running. While it holds a reason, the {@code AdapterStateHealthRule} reports the source
     * {@code failing} with that reason.
     */
    String FAILING_REASON_KEY = "health.failing";

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

    /**
     * Records that the adapter processed upstream input covering up to {@code newestInputAt}, and the
     * largest age it tolerates (prompt 048: "newest file age" health). Writes only on change.
     *
     * @param newestInputAt the coverage end of the newest processed input
     * @param maxGap the age after which the source shows {@code degraded}
     */
    default void recordInput(java.time.Instant newestInputAt, java.time.Duration maxGap) {
        if (newestInputAt != null) {
            String at = newestInputAt.toString();
            if (!at.equals(get(NEWEST_INPUT_KEY).orElse(null))) {
                put(NEWEST_INPUT_KEY, at);
            }
        }
        if (maxGap != null) {
            String gap = maxGap.toString();
            if (!gap.equals(get(MAX_INPUT_GAP_KEY).orElse(null))) {
                put(MAX_INPUT_GAP_KEY, gap);
            }
        }
    }

    /**
     * Records a problem that keeps the source {@code failing} until it is cleared (prompt 049). Writes
     * only when the reason changed.
     *
     * @param reason the health reason shown to the operator
     */
    default void fail(String reason) {
        if (reason == null || reason.isBlank()) {
            clearFailing();
            return;
        }
        if (!reason.equals(get(FAILING_REASON_KEY).orElse(null))) {
            put(FAILING_REASON_KEY, reason);
        }
    }

    /** Clears a reason set by {@link #fail}, if any. */
    default void clearFailing() {
        if (get(FAILING_REASON_KEY).isPresent()) {
            remove(FAILING_REASON_KEY);
        }
    }

    /** Clears a reason set by {@link #degrade}, if any. */
    default void clearDegraded() {
        if (get(DEGRADED_REASON_KEY).isPresent()) {
            remove(DEGRADED_REASON_KEY);
        }
    }
}
