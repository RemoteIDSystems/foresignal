package ai.foresignal.sink;

/**
 * A running sink (prompt 110). A {@code STATE_MIRROR} instance receives {@link #upsert} and
 * {@link #remove}; a {@code CHANGE_FEED} instance receives {@link #deliver} and acknowledges by
 * returning true (the supervisor advances the cursor only on an ack). {@link #close} releases
 * connections and listeners.
 */
public interface SinkInstance extends AutoCloseable {

    /** Delivers one change-feed record; true acknowledges it (the cursor may advance). */
    default boolean deliver(SinkRecord record) {
        return true;
    }

    /** Applies a state-mirror upsert. */
    default void upsert(SinkRecord record) {
    }

    /** Removes a state-mirror record. */
    default void remove(String sourceId, String externalId) {
    }

    /** A new snapshot is about to be replayed; drop mirrored state (resync). */
    default void resync() {
    }

    /** The number of connected clients (SERVE), or 0. */
    default int clients() {
        return 0;
    }

    /** Validates, connects or binds once and sends one synthetic record test. */
    default SinkTestResult test(SinkRecord probe) {
        return SinkTestResult.failed("send", "the sink type does not support a test");
    }

    @Override
    default void close() {
    }
}
