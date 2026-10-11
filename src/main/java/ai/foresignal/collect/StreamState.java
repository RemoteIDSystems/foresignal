package ai.foresignal.collect;

import java.time.Instant;

/**
 * Whether a streaming source is connected, and since when.
 *
 * <p>Held in memory by {@link HealthTracker}: a connection belongs to this process, so a persisted
 * "connected" flag would be wrong after every restart.
 *
 * @param connected true while a stream handle is open
 * @param since when the current state began
 */
public record StreamState(boolean connected, Instant since) {

    /** A connected state starting at {@code at}. */
    public static StreamState connectedAt(Instant at) {
        return new StreamState(true, at);
    }

    /** A disconnected state starting at {@code at}. */
    public static StreamState disconnectedAt(Instant at) {
        return new StreamState(false, at);
    }
}
