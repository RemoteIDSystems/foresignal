package ai.foresignal.collect.socket;

import ai.foresignal.collect.UpstreamIOException;
import ai.foresignal.net.Cidr;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.LocalAddresses;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DatagramHandler;
import ai.foresignal.source.spi.FrameHandler;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One streaming source's {@link SocketTransport} (prompt 069), bound to its {@link StreamContext}.
 *
 * <ul>
 *   <li><strong>Destination check:</strong> before anything is sent, the endpoint's address is checked
 *       again: a TCP address must be loopback, RFC 1918 or unique-local (and pass {@code LAN_ONLY} when the
 *       outbound policy is available); a UDP bind address must be a wildcard or a local address. The
 *       parameter validator already refused anything else, so this is defense in depth.
 *   <li><strong>Budget:</strong> {@link StreamContext#chargeRequest} takes one unit per connect attempt or
 *       bind; received bytes go to {@link StreamContext#reportBytes} after every read.
 *   <li><strong>TCP:</strong> connect timeout {@link SocketTransport#CONNECT_TIMEOUT}, keepalive on, and a
 *       read timeout of {@link SocketOptions#silenceTimeout()}: a connection that delivers nothing for that
 *       long is closed with a {@link SilenceTimeoutException}, so the supervisor's ladder reconnects.
 *   <li><strong>UDP:</strong> a {@link DatagramChannel} read on a virtual thread; a datagram from outside
 *       the allowlist is counted and dropped before its bytes are looked at.
 * </ul>
 */
public final class DefaultSocketTransport implements SocketTransport {

    private static final Logger log = LoggerFactory.getLogger(DefaultSocketTransport.class);

    /** The read buffer of a TCP connection. */
    static final int READ_BUFFER_BYTES = 16 * 1024;

    private final StreamContext ctx;
    private final SocketMetrics metrics;
    private final OutboundUrlPolicy policy;
    private final int maxDatagramBytes;
    private final int maxLineBytes;

    /**
     * Creates the transport of one stream context with the SPI limits.
     *
     * @param ctx the stream context that pays for and meters the sockets
     * @param metrics the source's counters
     * @param policy the outbound policy re-checking TCP destinations, or null to use the address check only
     */
    public DefaultSocketTransport(StreamContext ctx, SocketMetrics metrics, OutboundUrlPolicy policy) {
        this(ctx, metrics, policy, MAX_DATAGRAM_BYTES, Framing.MAX_LINE_BYTES);
    }

    /**
     * Creates the transport with explicit limits (tests use small ones).
     *
     * @param maxDatagramBytes the longest datagram delivered
     * @param maxLineBytes the longest line delivered
     */
    public DefaultSocketTransport(StreamContext ctx, SocketMetrics metrics, OutboundUrlPolicy policy,
            int maxDatagramBytes, int maxLineBytes) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.metrics = metrics == null ? new SocketMetrics(null, ctx.sourceId()) : metrics;
        this.policy = policy;
        this.maxDatagramBytes = maxDatagramBytes;
        this.maxLineBytes = maxLineBytes;
    }

    /** A connection that stayed silent for its whole silence timeout. */
    public static final class SilenceTimeoutException extends IOException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public SilenceTimeoutException(Duration timeout) {
            super("no data for " + timeout + "; closing so the stream reconnects");
        }
    }

    @Override
    public StreamHandle tcp(LanEndpoint endpoint, SocketOptions options, Framing framing, FrameHandler handler)
            throws AdapterException {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(handler, "handler");
        SocketOptions opts = options == null ? SocketOptions.defaults() : options;
        Framing frames = framing == null ? Framing.LINES : framing;
        if (endpoint.scheme() != LanEndpoint.Scheme.TCP) {
            throw new AdapterException(endpoint + " is not a tcp:// endpoint");
        }
        requireLanDestination(endpoint.address());
        ctx.chargeRequest("tcp-connect " + endpoint);

        Socket socket = new Socket();
        try {
            socket.setKeepAlive(true);
            socket.setSoTimeout(Math.toIntExact(Math.min(Integer.MAX_VALUE, opts.silenceTimeout().toMillis())));
            socket.connect(endpoint.socketAddress(), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            byte[] hello = opts.onConnect();
            if (hello != null) {
                OutputStream out = socket.getOutputStream();
                out.write(hello);
                out.flush();
            }
        } catch (IOException | RuntimeException e) {
            metrics.connect(false);
            closeQuietly(socket);
            throw new UpstreamIOException("Could not connect to " + endpoint + ": " + e.getMessage(), e);
        }
        metrics.connect(true);
        TcpConnection connection = new TcpConnection(endpoint, socket, opts, frames, handler);
        connection.start();
        return connection;
    }

    @Override
    public StreamHandle udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler handler)
            throws AdapterException {
        Objects.requireNonNull(bind, "bind");
        Objects.requireNonNull(handler, "handler");
        if (bind.scheme() != LanEndpoint.Scheme.UDP) {
            throw new AdapterException(bind + " is not a udp:// endpoint");
        }
        if (allowSenders == null || allowSenders.isEmpty()) {
            throw new AdapterException("A UDP listener needs at least one allowed sender");
        }
        for (Cidr block : allowSenders) {
            if (!LocalAddresses.isPrivateBlock(block)) {
                throw new AdapterException("The allowed sender " + block + " is not wholly inside private space");
            }
        }
        ctx.chargeRequest("udp-bind " + bind);
        DatagramChannel channel = null;
        try {
            InetAddress address = bind.address();
            channel = DatagramChannel.open(address instanceof java.net.Inet6Address
                    ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET);
            channel.bind(bind.socketAddress());
        } catch (IOException | RuntimeException e) {
            metrics.connect(false);
            closeQuietly(channel);
            throw new UpstreamIOException("Could not bind " + bind + ": " + e.getMessage(), e);
        }
        metrics.connect(true);
        UdpListener listener = new UdpListener(bind, channel, List.copyOf(allowSenders), handler);
        listener.start();
        return listener;
    }

    /**
     * Refuses a TCP destination that is not loopback, RFC 1918 or unique-local, before any budget is spent.
     */
    void requireLanDestination(InetAddress address) throws AdapterException {
        if (address == null || !LocalAddresses.isPrivate(address) || LocalAddresses.isCarrierGradeNat(address)
                || address.isAnyLocalAddress()) {
            throw new AdapterException("Refusing to connect to " + (address == null ? "no address"
                    : address.getHostAddress()) + ": only loopback, private and unique-local addresses are allowed");
        }
        if (policy != null) {
            OutboundUrlPolicy.Result result = policy.checkHost(address.getHostAddress(),
                    OutboundUrlPolicy.Profile.LAN_ONLY);
            if (result instanceof OutboundUrlPolicy.Denied denied) {
                throw new AdapterException("Refusing to connect to " + address.getHostAddress() + ": " + denied.reason());
            }
        }
    }

    private Clock clock() {
        return ctx.clock();
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            log.debug("closing a socket failed", e);
        }
    }

    /** The shared end-of-stream bookkeeping of a connection or listener. */
    private abstract class Running implements StreamHandle {

        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicBoolean ended = new AtomicBoolean();
        private volatile Consumer<Throwable> callback;
        private volatile Throwable endCause;
        private volatile boolean endedOnItsOwn;

        @Override
        public boolean isRunning() {
            return !ended.get();
        }

        @Override
        public void onClose(Consumer<Throwable> listener) {
            callback = listener;
            if (ended.get() && endedOnItsOwn && listener != null) {
                listener.accept(endCause);
            }
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                release();
            }
            ended.set(true);
        }

        /** True once {@link #close()} was called. */
        boolean isClosed() {
            return closed.get();
        }

        /** Records that the stream ended without a {@link #close()}: the peer closed, or the loop failed. */
        void ended(Throwable cause) {
            if (closed.get()) {
                ended.set(true);
                return;
            }
            endCause = cause;
            endedOnItsOwn = true;
            if (ended.compareAndSet(false, true)) {
                closed.set(true);
                release();
                Consumer<Throwable> listener = callback;
                if (listener != null) {
                    listener.accept(cause);
                }
            }
        }

        /** Closes the underlying socket; idempotent and non-blocking. */
        abstract void release();
    }

    /** One TCP connection and its read loop. */
    private final class TcpConnection extends Running {

        private final LanEndpoint endpoint;
        private final Socket socket;
        private final SocketOptions options;
        private final Framing framing;
        private final FrameHandler handler;

        TcpConnection(LanEndpoint endpoint, Socket socket, SocketOptions options, Framing framing,
                FrameHandler handler) {
            this.endpoint = endpoint;
            this.socket = socket;
            this.options = options;
            this.framing = framing;
            this.handler = handler;
        }

        void start() {
            Thread.ofVirtual().name("socket-tcp-" + ctx.sourceId()).start(this::readLoop);
        }

        private void readLoop() {
            Throwable failure = null;
            try (InputStream in = socket.getInputStream()) {
                byte[] buffer = new byte[READ_BUFFER_BYTES];
                FrameDelivery delivery = new FrameDelivery();
                LineFramer framer = new LineFramer(maxLineBytes, delivery::deliver,
                        () -> metrics.dropped(SocketMetrics.REASON_OVERSIZE));
                while (!isClosed()) {
                    int read;
                    try {
                        read = in.read(buffer);
                    } catch (SocketTimeoutException e) {
                        throw new SilenceTimeoutException(options.silenceTimeout());
                    }
                    if (read < 0) {
                        break;
                    }
                    if (read == 0) {
                        continue;
                    }
                    ctx.reportBytes(read);
                    if (framing == Framing.RAW) {
                        delivery.deliver(java.util.Arrays.copyOf(buffer, read));
                    } else {
                        framer.feed(buffer, 0, read);
                    }
                    delivery.rethrow();
                }
            } catch (Throwable e) {
                failure = e;
            }
            if (failure != null && !isClosed()) {
                log.debug("{}: connection to {} ended: {}", ctx.sourceId(), endpoint, failure.toString());
            }
            ended(failure);
        }

        @Override
        void release() {
            closeQuietly(socket);
        }

        /** Hands frames to the handler, keeping the first failure so the read loop can end on it. */
        private final class FrameDelivery {

            private Exception failure;

            void deliver(byte[] frame) {
                if (failure != null) {
                    return;
                }
                metrics.frame();
                try {
                    handler.onFrame(frame, clock().instant());
                } catch (Exception e) {
                    failure = e;
                }
            }

            void rethrow() throws Exception {
                if (failure != null) {
                    throw failure;
                }
            }
        }
    }

    /** One UDP listener and its receive loop. */
    private final class UdpListener extends Running {

        private final LanEndpoint bind;
        private final DatagramChannel channel;
        private final List<Cidr> allowSenders;
        private final DatagramHandler handler;

        UdpListener(LanEndpoint bind, DatagramChannel channel, List<Cidr> allowSenders, DatagramHandler handler) {
            this.bind = bind;
            this.channel = channel;
            this.allowSenders = allowSenders;
            this.handler = handler;
        }

        void start() {
            Thread.ofVirtual().name("socket-udp-" + ctx.sourceId()).start(this::receiveLoop);
        }

        private void receiveLoop() {
            Throwable failure = null;
            ByteBuffer buffer = ByteBuffer.allocate(maxDatagramBytes + 1);
            try {
                while (!isClosed()) {
                    buffer.clear();
                    java.net.SocketAddress from = channel.receive(buffer);
                    if (!(from instanceof InetSocketAddress sender)) {
                        continue;
                    }
                    int size = buffer.position();
                    ctx.reportBytes(size);
                    if (!allowed(sender.getAddress())) {
                        // Never parsed: the bytes are discarded with the buffer.
                        metrics.udpRejected();
                        continue;
                    }
                    if (size > maxDatagramBytes) {
                        metrics.dropped(SocketMetrics.REASON_OVERSIZE);
                        continue;
                    }
                    byte[] datagram = java.util.Arrays.copyOf(buffer.array(), size);
                    metrics.frame();
                    handler.onDatagram(datagram, sender, clock().instant());
                }
            } catch (ClosedChannelException e) {
                failure = isClosed() ? null : e;
            } catch (Throwable e) {
                failure = e;
            }
            if (failure != null && !isClosed()) {
                log.debug("{}: listener on {} ended: {}", ctx.sourceId(), bind, failure.toString());
            }
            ended(failure);
        }

        /** True when {@code address} lies in one of the allowed blocks. */
        boolean allowed(InetAddress address) {
            for (Cidr block : allowSenders) {
                if (block.contains(address)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        void release() {
            closeQuietly(channel);
        }
    }
}
