package ai.foresignal.sink;

/**
 * A running sink (prompt 110). A {@code STATE_MIRROR} instance receives {@link #upsert} and
 * {@link #remove}; a {@code CHANGE_FEED} instance receives {@link #deliver} and acknowledges by
 * returning true (the supervisor advances the cursor only on an ack). {@link #close} releases
 * connections and listeners.
 */
public interface SinkInstance extends AutoCloseable {

    /**
     * Delivers one change-feed record; true acknowledges it (the cursor may advance). A type that can tell a
     * retryable failure (5xx, 429 with {@code Retry-After}) from a permanent one (other 4xx) throws
     * {@link SinkDeliveryException} instead of returning false.
     */
    default boolean deliver(SinkRecord record) {
        return true;
    }

    /**
     * Delivers change-feed records in order and returns how many of them, from the start, were acknowledged; the
     * supervisor advances the cursor past exactly those. The default delivers one at a time and stops at the
     * first refusal; a batching type (CloudEvents {@code batchSize}) overrides it.
     *
     * @throws SinkDeliveryException on a failure the type can classify
     */
    default int deliverAll(java.util.List<SinkRecord> records) {
        int acked = 0;
        for (SinkRecord record : records) {
            if (!deliver(record)) {
                return acked;
            }
            acked++;
        }
        return acked;
    }

    /** The most records {@link #deliverAll} takes at once (1 unless the type batches). */
    default int batchSize() {
        return 1;
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

    /** The client cap (SERVE), or 0 when the type has none. */
    default int maxClients() {
        return 0;
    }

    /** Connections refused because the peer was not allowed or the cap was reached (SERVE). */
    default long rejected() {
        return 0;
    }

    /**
     * A connected client of a SERVE sink (OpenAPI {@code SinkStatus.clients.peers}).
     *
     * @param address the peer address
     * @param connectedAt when it connected (for HTTP, which has no lasting connection: its latest request)
     */
    record Peer(String address, java.time.Instant connectedAt) {}

    /** The connected (or, for HTTP, recently seen) peers (SERVE). */
    default java.util.List<Peer> peers() {
        return java.util.List.of();
    }

    /** {@link #peers()} of a TCP server's clients. */
    static java.util.List<Peer> peers(SinkTcpServer server) {
        return server.clients().stream().map(c -> new Peer(c.address(), c.connectedAt())).toList();
    }

    /** Validates, connects or binds once and sends one synthetic record test. */
    default SinkTestResult test(SinkRecord probe) {
        return SinkTestResult.failed("send", "the sink type does not support a test");
    }

    @Override
    default void close() {
    }
}
