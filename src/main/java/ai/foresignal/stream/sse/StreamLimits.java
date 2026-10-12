package ai.foresignal.stream.sse;

/**
 * SSE limits, stored as the app setting {@code stream.limits} (prompt 015). A missing or
 * non-positive value takes its default, so a partial setting is valid.
 *
 * @param maxClients open streams across the server; beyond it a connect is {@code 503 STREAM_LIMIT}
 * @param clientBuffer FIFO events queued for one client; overflow sends {@code reset
 *     {reason: buffer_overflow}} and closes the stream
 * @param replayCap FIFO events one connect may replay; at the cap the stream sends {@code reset
 *     {reason: replay_cap}} and closes
 * @param heartbeatSeconds seconds between {@code heartbeat} events
 * @param liveIntervalMs milliseconds between drains of the conflation lane
 * @param liveMaxEntities entities the conflation lane may hold; beyond it the lane is cleared and
 *     {@code resync {reason: live_cap}} is sent
 * @param maxStreamsPerToken open streams per token principal; beyond it {@code 429 STREAM_LIMIT}
 * @param maxStreamsAnonymousPerAddress open anonymous streams per remote address; beyond it
 *     {@code 429 STREAM_LIMIT}
 */
public record StreamLimits(
        int maxClients,
        int clientBuffer,
        int replayCap,
        int heartbeatSeconds,
        int liveIntervalMs,
        int liveMaxEntities,
        int maxStreamsPerToken,
        int maxStreamsAnonymousPerAddress) {

    /** Default {@link #maxClients}. */
    public static final int DEFAULT_MAX_CLIENTS = 100;

    /** Default {@link #clientBuffer}. */
    public static final int DEFAULT_CLIENT_BUFFER = 1_000;

    /** Default {@link #replayCap}. */
    public static final int DEFAULT_REPLAY_CAP = 10_000;

    /** Default {@link #heartbeatSeconds}. */
    public static final int DEFAULT_HEARTBEAT_SECONDS = 15;

    /** Default {@link #liveIntervalMs}. */
    public static final int DEFAULT_LIVE_INTERVAL_MS = 1_000;

    /** Default {@link #liveMaxEntities}. */
    public static final int DEFAULT_LIVE_MAX_ENTITIES = 50_000;

    /** Default {@link #maxStreamsPerToken}. */
    public static final int DEFAULT_MAX_STREAMS_PER_TOKEN = 8;

    /** Default {@link #maxStreamsAnonymousPerAddress}. */
    public static final int DEFAULT_MAX_STREAMS_ANONYMOUS_PER_ADDRESS = 4;

    /** Replaces non-positive values with their defaults. */
    public StreamLimits {
        maxClients = orDefault(maxClients, DEFAULT_MAX_CLIENTS);
        clientBuffer = orDefault(clientBuffer, DEFAULT_CLIENT_BUFFER);
        replayCap = orDefault(replayCap, DEFAULT_REPLAY_CAP);
        heartbeatSeconds = orDefault(heartbeatSeconds, DEFAULT_HEARTBEAT_SECONDS);
        liveIntervalMs = orDefault(liveIntervalMs, DEFAULT_LIVE_INTERVAL_MS);
        liveMaxEntities = orDefault(liveMaxEntities, DEFAULT_LIVE_MAX_ENTITIES);
        maxStreamsPerToken = orDefault(maxStreamsPerToken, DEFAULT_MAX_STREAMS_PER_TOKEN);
        maxStreamsAnonymousPerAddress =
                orDefault(maxStreamsAnonymousPerAddress, DEFAULT_MAX_STREAMS_ANONYMOUS_PER_ADDRESS);
    }

    /** The shipped defaults: 100 clients, buffer 1,000, replay 10,000, 15 s, 1 s, 50,000, 8 and 4. */
    public static StreamLimits defaults() {
        return new StreamLimits(0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** The per-principal limit that applies to {@code principal}. */
    public int perPrincipalLimit(StreamPrincipal principal) {
        return principal.anonymous() ? maxStreamsAnonymousPerAddress : maxStreamsPerToken;
    }

    private static int orDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }
}
