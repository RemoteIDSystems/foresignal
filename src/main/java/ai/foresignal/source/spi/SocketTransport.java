package ai.foresignal.source.spi;

import ai.foresignal.net.Cidr;
import java.time.Duration;
import java.util.Set;

/**
 * The LAN-only socket transport of a streaming adapter (prompt 069), from {@link StreamContext#sockets()}.
 *
 * <p>Adapters never construct sockets themselves (adapter rule 2): this is the only way a source reads a
 * TCP stream or UDP datagrams, as {@link SourceHttp} is the only way it makes an HTTP request. Every
 * endpoint comes from a validated {@link ParamType#LAN_URL}, so it is a private address the operator
 * configured.
 *
 * <ul>
 *   <li><strong>Budget:</strong> one unit of the source's request budget per connect attempt or bind, so
 *       the budget bounds a reconnect storm; received bytes are metered through
 *       {@link StreamContext#reportBytes}.
 *   <li><strong>No server sockets:</strong> Foresignal never listens on TCP for a source. Serving TCP is
 *       prompt 110's SERVE sinks.
 *   <li><strong>Ending:</strong> the returned {@link StreamHandle} reports a drop through
 *       {@link StreamHandle#onClose}, so the streaming supervisor reconnects on its ladder.
 * </ul>
 *
 * <p>Metrics: {@code foresignal.socket.connects{source,result}}, {@code foresignal.socket.frames{source}},
 * {@code foresignal.socket.frames.dropped{source,reason}} and {@code foresignal.socket.udp.rejected{source}}.
 */
public interface SocketTransport {

    /** The TCP connect timeout. */
    Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /** The largest datagram a UDP listener reads; anything longer is dropped and counted. */
    int MAX_DATAGRAM_BYTES = 64 * 1024;

    /**
     * Opens a TCP client connection and delivers its frames until the handle is closed, the peer closes, or
     * the connection stays silent for {@link SocketOptions#silenceTimeout()}.
     *
     * <p>Consumes one budget unit before connecting; the connect times out after {@link #CONNECT_TIMEOUT},
     * TCP keepalive is on, and {@link SocketOptions#onConnect()} is written once when connected.
     *
     * @param endpoint a {@link LanEndpoint.Scheme#TCP} endpoint
     * @param options the silence timeout and the write on connect
     * @param framing how the byte stream is cut into frames
     * @param handler receives each frame on the connection's virtual thread
     * @return the running connection
     * @throws AdapterException when the budget is spent, the source is in backoff, or the connect failed
     */
    StreamHandle tcp(LanEndpoint endpoint, SocketOptions options, Framing framing, FrameHandler handler)
            throws AdapterException;

    /**
     * Binds a UDP listener and delivers the datagrams of allowed senders until the handle is closed.
     *
     * <p>Consumes one budget unit before binding. A datagram whose sender lies outside every block of
     * {@code allowSenders} is dropped and counted ({@code foresignal.socket.udp.rejected}) and never parsed;
     * one longer than {@link #MAX_DATAGRAM_BYTES} is dropped and counted too.
     *
     * @param bind a {@link LanEndpoint.Scheme#UDP} endpoint
     * @param allowSenders the address blocks a datagram may come from; never empty
     * @param handler receives each allowed datagram on the listener's virtual thread
     * @return the running listener
     * @throws AdapterException when the budget is spent or the bind failed
     */
    StreamHandle udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler handler) throws AdapterException;
}
