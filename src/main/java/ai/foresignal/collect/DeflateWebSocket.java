package ai.foresignal.collect;

import ai.foresignal.source.spi.MeteredWebSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code permessage-deflate} WebSocket behind the JDK's {@link WebSocket} interface (prompt 063).
 *
 * <p>The JDK client cannot negotiate extensions, and aisstream.io limits the bandwidth of connections that
 * do not compress. This class opens the connection with TooTallNate Java-WebSocket (MIT), offering
 * {@code permessage-deflate} (RFC 7692), and presents it to the adapter as a {@link WebSocket} driven by a
 * {@link WebSocket.Listener}, so a streaming adapter is written once for both clients:
 *
 * <ul>
 *   <li><strong>Whole messages.</strong> The library reassembles fragments and inflates them; the listener
 *       receives each message once, with {@code last == true}.
 *   <li><strong>Back-pressure.</strong> A message is handed to the listener only while it has demand from
 *       {@link #request(long)}. Without demand the library's read thread waits, so it stops reading the
 *       socket and TCP pushes back on the server, exactly as with the JDK client.
 *   <li><strong>Terminal events.</strong> The listener gets {@code onClose} or {@code onError}, never both,
 *       and nothing after {@link #abort()}.
 *   <li><strong>Metering.</strong> {@link #wireBytesReceived()} counts the frame bytes as they arrived,
 *       compressed, so the adapter meters what the connection cost rather than the inflated size.
 * </ul>
 *
 * <p>Only {@link BoundSourceHttp} creates it, after the backoff check and the budget unit; it is part of the
 * single network path (invariant SRC-1).
 */
final class DeflateWebSocket implements MeteredWebSocket {

    private static final Logger log = LoggerFactory.getLogger(DeflateWebSocket.class);

    /** The response header that carries the accepted extensions. */
    static final String EXTENSIONS_HEADER = "Sec-WebSocket-Extensions";

    private final WebSocket.Listener listener;
    private final AtomicLong wireBytes = new AtomicLong();
    private final AtomicBoolean terminal = new AtomicBoolean();
    private final Object demandLock = new Object();
    private final Client client;

    private long demand;
    private volatile boolean opened;
    private volatile boolean aborted;
    private volatile boolean inputClosed;
    private volatile boolean outputClosed;
    private volatile boolean deflateNegotiated;
    private volatile Exception connectFailure;

    private DeflateWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers,
            Duration connectTimeout, int maxFrameBytes) {
        this.listener = listener;
        this.client = new Client(uri, new CountingDraft(wireBytes, maxFrameBytes), headers,
                (int) Math.min(Integer.MAX_VALUE, connectTimeout.toMillis()));
        this.client.setDaemon(true);
    }

    /**
     * Opens a connection and waits for the handshake.
     *
     * @param headers the handshake headers, the User-Agent included
     * @param connectTimeout how long the TCP connect and the handshake may take
     * @param maxFrameBytes the largest frame accepted; a larger one closes the connection
     * @return the open socket; the listener's {@code onOpen} has been called or is being called
     * @throws IOException when the connection or the handshake failed or timed out
     * @throws InterruptedException when interrupted while waiting
     */
    static DeflateWebSocket open(URI uri, WebSocket.Listener listener, Map<String, String> headers,
            Duration connectTimeout, int maxFrameBytes) throws IOException, InterruptedException {
        DeflateWebSocket socket = new DeflateWebSocket(uri, listener, headers, connectTimeout, maxFrameBytes);
        boolean open;
        try {
            // The library applies the connect timeout to the TCP connect only; the wait covers the handshake.
            open = socket.client.connectBlocking(connectTimeout.toMillis() * 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            socket.abort();
            throw e;
        }
        if (!open) {
            Exception failure = socket.connectFailure;
            socket.abort();
            if (failure instanceof IOException io) {
                throw io;
            }
            throw new IOException(failure == null
                    ? "the WebSocket handshake did not complete within " + connectTimeout.multipliedBy(2)
                    : failure.getMessage(), failure);
        }
        return socket;
    }

    // ---------- MeteredWebSocket ----------

    @Override
    public long wireBytesReceived() {
        return wireBytes.get();
    }

    @Override
    public boolean perMessageDeflateNegotiated() {
        return deflateNegotiated;
    }

    // ---------- WebSocket ----------

    /** Sends one text message; fragments are not supported (aisstream refuses them anyway). */
    @Override
    public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
        if (!last) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("fragmented messages are not sent"));
        }
        return send(() -> client.send(data.toString()));
    }

    /** Sends one binary message; fragments are not supported. */
    @Override
    public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
        if (!last) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("fragmented messages are not sent"));
        }
        return send(() -> client.send(data));
    }

    @Override
    public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
        PingFrame frame = new PingFrame();
        frame.setPayload(message);
        return send(() -> client.sendFrame(frame));
    }

    @Override
    public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
        PongFrame frame = new PongFrame();
        frame.setPayload(message);
        return send(() -> client.sendFrame(frame));
    }

    @Override
    public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
        outputClosed = true;
        return send(() -> client.close(statusCode, reason == null ? "" : reason));
    }

    @Override
    public void request(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("request(" + n + ")");
        }
        synchronized (demandLock) {
            demand = demand + n < 0 ? Long.MAX_VALUE : demand + n;
            demandLock.notifyAll();
        }
    }

    @Override
    public String getSubprotocol() {
        return "";
    }

    @Override
    public boolean isOutputClosed() {
        return outputClosed || aborted;
    }

    @Override
    public boolean isInputClosed() {
        return inputClosed || aborted;
    }

    /** Closes the TCP connection at once, without a close handshake; the listener hears nothing more. */
    @Override
    public void abort() {
        aborted = true;
        inputClosed = true;
        outputClosed = true;
        synchronized (demandLock) {
            demandLock.notifyAll();
        }
        try {
            client.closeConnection(CloseFrame.ABNORMAL_CLOSE, "aborted");
        } catch (RuntimeException e) {
            log.debug("closing the WebSocket connection failed", e);
        }
        Socket socket = client.getSocket();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.debug("closing the WebSocket socket failed", e);
            }
        }
    }

    // ---------- internals ----------

    private CompletableFuture<WebSocket> send(Runnable action) {
        if (aborted) {
            return CompletableFuture.failedFuture(new IOException("the WebSocket was aborted"));
        }
        try {
            action.run();
            return CompletableFuture.completedFuture(this);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(new IOException("could not send on the WebSocket: " + e.getMessage(), e));
        }
    }

    /** Waits on the read thread until the listener has demand, then hands one message over. */
    private void deliver(Runnable delivery) {
        synchronized (demandLock) {
            while (demand == 0 && !aborted) {
                try {
                    demandLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (aborted) {
                return;
            }
            demand--;
        }
        try {
            delivery.run();
        } catch (RuntimeException e) {
            log.warn("a WebSocket listener failed", e);
        }
    }

    private void onOpened(ServerHandshake handshake) {
        String extensions = handshake.getFieldValue(EXTENSIONS_HEADER);
        deflateNegotiated = extensions != null
                && extensions.toLowerCase(Locale.ROOT).contains("permessage-deflate");
        opened = true;
        try {
            listener.onOpen(this);
        } catch (RuntimeException e) {
            log.warn("a WebSocket listener failed in onOpen", e);
        }
    }

    private void onClosed(int code, String reason) {
        inputClosed = true;
        outputClosed = true;
        if (!opened || aborted || !terminal.compareAndSet(false, true)) {
            return;
        }
        try {
            listener.onClose(this, code, reason == null ? "" : reason);
        } catch (RuntimeException e) {
            log.warn("a WebSocket listener failed in onClose", e);
        }
    }

    private void onFailed(Exception error) {
        if (!opened) {
            connectFailure = error;
            return;
        }
        inputClosed = true;
        if (aborted || !terminal.compareAndSet(false, true)) {
            return;
        }
        try {
            listener.onError(this, error);
        } catch (RuntimeException e) {
            log.warn("a WebSocket listener failed in onError", e);
        }
    }

    /** The library client, forwarding to the bridge. */
    private final class Client extends WebSocketClient {

        Client(URI uri, Draft draft, Map<String, String> headers, int connectTimeoutMillis) {
            super(uri, draft, headers, connectTimeoutMillis);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            onOpened(handshake);
        }

        @Override
        public void onMessage(String message) {
            deliver(() -> listener.onText(DeflateWebSocket.this, message, true));
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            deliver(() -> listener.onBinary(DeflateWebSocket.this, bytes, true));
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            onClosed(code, reason);
        }

        @Override
        public void onError(Exception error) {
            onFailed(error);
        }
    }

    /**
     * RFC 6455 with {@code permessage-deflate} offered, counting every frame byte it decodes.
     *
     * <p>The library copies the draft for each connection, so the copy shares the counter.
     */
    static final class CountingDraft extends Draft_6455 {

        private final AtomicLong counter;

        CountingDraft(AtomicLong counter, int maxFrameBytes) {
            this(List.of(new PerMessageDeflateExtension()), List.of(), maxFrameBytes, counter);
        }

        private CountingDraft(List<IExtension> extensions, List<IProtocol> protocols, int maxFrameBytes,
                AtomicLong counter) {
            super(extensions, protocols.isEmpty() ? defaultProtocols() : protocols, maxFrameBytes);
            this.counter = counter;
        }

        private static List<IProtocol> defaultProtocols() {
            return new Draft_6455().getKnownProtocols();
        }

        @Override
        public Draft copyInstance() {
            List<IExtension> extensions = new ArrayList<>();
            for (IExtension extension : getKnownExtensions()) {
                extensions.add(extension.copyInstance());
            }
            List<IProtocol> protocols = new ArrayList<>();
            for (IProtocol protocol : getKnownProtocols()) {
                protocols.add(protocol.copyInstance());
            }
            return new CountingDraft(extensions, protocols, getMaxFrameSize(), counter);
        }

        @Override
        public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
            counter.addAndGet(buffer.remaining());
            return super.translateFrame(buffer);
        }
    }
}
