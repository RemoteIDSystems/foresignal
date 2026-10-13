package ai.foresignal.collect.socket;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * The socket transport's counters (prompt 069), tagged with the source id.
 *
 * <ul>
 *   <li>{@value #CONNECTS}{@code {source,result}}: TCP connect attempts and UDP binds, {@code result}
 *       {@code ok} or {@code failed};
 *   <li>{@value #FRAMES}{@code {source}}: frames and datagrams delivered to a handler;
 *   <li>{@value #FRAMES_DROPPED}{@code {source,reason}}: frames dropped before a handler saw them
 *       ({@code oversize});
 *   <li>{@value #UDP_REJECTED}{@code {source}}: datagrams from a sender outside the allowlist, never
 *       parsed.
 * </ul>
 *
 * <p>Without a registry (a unit test) every call does nothing.
 */
public final class SocketMetrics {

    /** Connect attempts and binds. */
    public static final String CONNECTS = "foresignal.socket.connects";

    /** Frames delivered. */
    public static final String FRAMES = "foresignal.socket.frames";

    /** Frames dropped before delivery. */
    public static final String FRAMES_DROPPED = "foresignal.socket.frames.dropped";

    /** Datagrams from senders outside the allowlist. */
    public static final String UDP_REJECTED = "foresignal.socket.udp.rejected";

    /** The {@code reason} of a frame or datagram longer than the limit. */
    public static final String REASON_OVERSIZE = "oversize";

    private final MeterRegistry registry;
    private final String sourceId;

    /**
     * Creates the counters of one source.
     *
     * @param registry the registry, or null to count nothing
     * @param sourceId the source tag
     */
    public SocketMetrics(MeterRegistry registry, String sourceId) {
        this.registry = registry;
        this.sourceId = sourceId;
    }

    /** Counts one connect attempt or bind. */
    public void connect(boolean ok) {
        increment(CONNECTS, "result", ok ? "ok" : "failed");
    }

    /** Counts one delivered frame or datagram. */
    public void frame() {
        increment(FRAMES, null, null);
    }

    /** Counts one dropped frame or datagram. */
    public void dropped(String reason) {
        increment(FRAMES_DROPPED, "reason", reason);
    }

    /** Counts one datagram from a sender outside the allowlist. */
    public void udpRejected() {
        increment(UDP_REJECTED, null, null);
    }

    private void increment(String name, String tag, String value) {
        if (registry == null) {
            return;
        }
        Counter.Builder builder = Counter.builder(name).tag("source", sourceId);
        if (tag != null) {
            builder.tag(tag, value);
        }
        builder.register(registry).increment();
    }
}
