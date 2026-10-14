package ai.foresignal.source.adapters.ais;

import ai.foresignal.collect.socket.DefaultSocketTransport;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.net.Cidr;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DatagramHandler;
import ai.foresignal.source.spi.FrameHandler;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The real {@link DefaultSocketTransport}, with {@code tcpPublic} redirected to a loopback test server (prompt 085, a
 * copy of prompt 082's test transport, which is package-private there): the target must still be in the descriptor's
 * {@code allowedHosts}, the options and framing are passed through unchanged, and every call is recorded.
 */
final class AisLoopbackTransport implements SocketTransport {

    /** The public targets asked for. */
    final List<HostPort> targets = new CopyOnWriteArrayList<>();

    /** The options of every connection. */
    final List<SocketOptions> options = new CopyOnWriteArrayList<>();

    private final StreamContext ctx;
    private final DefaultSocketTransport delegate;
    private final int port;

    AisLoopbackTransport(StreamContext ctx, int port) {
        this.ctx = ctx;
        this.delegate = new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null);
        this.port = port;
    }

    @Override
    public StreamHandle tcp(LanEndpoint endpoint, SocketOptions opts, Framing framing, FrameHandler handler)
            throws AdapterException {
        options.add(opts);
        return delegate.tcp(endpoint, opts, framing, handler);
    }

    @Override
    public StreamHandle tcpPublic(HostPort target, SocketOptions opts, Framing framing, FrameHandler handler)
            throws AdapterException {
        if (!ctx.descriptor().allowsHost(target)) {
            throw new AdapterException(target + " is not in allowedHosts");
        }
        targets.add(target);
        options.add(opts);
        return delegate.tcp(new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", port, InetAddress.getLoopbackAddress()),
                opts, framing, handler);
    }

    @Override
    public StreamHandle udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler handler) throws AdapterException {
        return delegate.udp(bind, allowSenders, handler);
    }
}
