package ai.foresignal.source.spi;

/**
 * What a streaming adapter is given when its stream is started.
 *
 * <p>Same surface as {@link FetchContext}, plus one addition: a stream's cost is not one request
 * but a continuing flow of bytes, and nothing in the request path can see it. The connection
 * attempt is budgeted and metered by {@code SourceHttp.openWebSocket}; everything that arrives
 * afterwards is only visible to the adapter, so the adapter reports it.
 */
public interface StreamContext extends FetchContext {

    /**
     * Reports bytes received on an open stream, so a streaming source's spend is measured rather
     * than left at the single connection request.
     *
     * <p>Call it per batch rather than per frame: the meter is cheap but not free, and per-minute
     * accounting cannot tell the difference.
     *
     * @param bytes bytes received since the last call; a non-positive value is ignored
     */
    void recordReceivedBytes(long bytes);

    /**
     * Reports bytes received on an open stream; the same as {@link #recordReceivedBytes}, under the
     * name Architecture §5.5 uses.
     *
     * @param bytes bytes received since the last call; a non-positive value is ignored
     */
    default void reportBytes(long bytes) {
        recordReceivedBytes(bytes);
    }

    /**
     * Tells the supervisor the stream is connected and delivering.
     *
     * <p>Optional: the supervisor already treats a successful {@code start} as connected. An adapter
     * whose {@code start} returns before the upstream has actually accepted the subscription calls
     * this once it has. Idempotent.
     */
    void connected();

    /**
     * Tells the supervisor the stream has dropped.
     *
     * <p>The supervisor records it in health and schedules a reconnect on its ladder. Pass a
     * {@link CredentialsRejectedException} when the upstream refused the credentials, which
     * retries hourly instead of on the ladder.
     *
     * @param error why, or null for a clean close by the upstream
     */
    void disconnected(Throwable error);

    /**
     * The LAN-only socket transport (prompt 069): the only way a streaming adapter opens a TCP connection
     * or a UDP listener. Each connect or bind consumes one budget unit; bytes are metered through
     * {@link #reportBytes}.
     *
     * @throws UnsupportedOperationException when no transport is installed (a hand-built context in a unit
     *     test)
     */
    default SocketTransport sockets() {
        throw new UnsupportedOperationException("No socket transport is available to " + sourceId() + " in this context.");
    }
}
