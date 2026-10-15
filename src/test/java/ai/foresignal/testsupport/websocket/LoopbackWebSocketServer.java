package ai.foresignal.testsupport.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.Opcode;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A WebSocket server on loopback for the streaming tests (prompt 063), built on Java-WebSocket's
 * {@link WebSocketServer}. WireMock has no WebSocket support.
 *
 * <p>It records every connection's handshake headers and every text or binary message a client sent, can
 * send whole or fragmented binary messages and text, and can close or drop connections. With
 * {@code deflate} it accepts {@code permessage-deflate} and compresses what it sends. An optional
 * {@link #onMessage} hook answers client messages (for example a subscription confirmation).
 */
public final class LoopbackWebSocketServer extends WebSocketServer implements AutoCloseable {

    /** One message a client sent. */
    public record Received(int connection, String text, Instant at) {}

    private final CountDownLatch started = new CountDownLatch(1);
    private final AtomicInteger connections = new AtomicInteger();
    private final List<Map<String, String>> handshakes = new CopyOnWriteArrayList<>();
    private final List<Received> received = new CopyOnWriteArrayList<>();
    private final List<WebSocket> open = new CopyOnWriteArrayList<>();
    private volatile BiConsumer<WebSocket, String> onMessage = (conn, text) -> {};

    private LoopbackWebSocketServer(List<Draft> drafts) {
        super(new InetSocketAddress("127.0.0.1", 0), drafts);
        setReuseAddr(true);
        setDaemon(true);
    }

    /**
     * Starts a server and waits until it listens.
     *
     * @param deflate true to accept {@code permessage-deflate}
     */
    public static LoopbackWebSocketServer start(boolean deflate) {
        List<Draft> drafts = deflate
                ? List.of(new Draft_6455(new PerMessageDeflateExtension()))
                : List.of(new Draft_6455());
        LoopbackWebSocketServer server = new LoopbackWebSocketServer(drafts);
        server.start();
        try {
            if (!server.started.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("the loopback WebSocket server did not start");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return server;
    }

    /** {@code ws://127.0.0.1:<port>/v0/stream}. */
    public URI uri() {
        return URI.create("ws://127.0.0.1:" + getPort() + "/v0/stream");
    }

    /** Sets the hook that answers client messages. */
    public LoopbackWebSocketServer onMessage(BiConsumer<WebSocket, String> hook) {
        this.onMessage = hook;
        return this;
    }

    /** Connections accepted so far. */
    public int connectionCount() {
        return connections.get();
    }

    /** The handshake headers of each connection, case-insensitive keys. */
    public List<Map<String, String>> handshakes() {
        return List.copyOf(handshakes);
    }

    /** Every message clients sent, oldest first. */
    public List<Received> received() {
        return List.copyOf(received);
    }

    /** The currently open connections. */
    public List<WebSocket> openConnections() {
        return new ArrayList<>(open);
    }

    /** The newest open connection; waits up to {@code timeout} for one. */
    public WebSocket awaitConnection(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (!open.isEmpty()) {
                return open.get(open.size() - 1);
            }
            sleep(10);
        }
        throw new IllegalStateException("no client connected within " + timeout);
    }

    /** Sends {@code text} as one binary message of UTF-8, as aisstream does, to every open connection. */
    public void sendBinary(String text) {
        for (WebSocket conn : open) {
            conn.send(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Sends {@code text} as one text message to every open connection. */
    public void sendText(String text) {
        for (WebSocket conn : open) {
            conn.send(text);
        }
    }

    /** Sends {@code text} as a binary message split into {@code parts} fragments. */
    public void sendFragmentedBinary(String text, int parts) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int size = Math.max(1, (bytes.length + parts - 1) / parts);
        for (WebSocket conn : open) {
            for (int offset = 0; offset < bytes.length; offset += size) {
                int length = Math.min(size, bytes.length - offset);
                boolean fin = offset + length >= bytes.length;
                // A copied slice: the permessage-deflate path reads the backing array, so wrapping a
                // view would send the whole message in every fragment.
                conn.sendFragmentedFrame(Opcode.BINARY,
                        ByteBuffer.wrap(java.util.Arrays.copyOfRange(bytes, offset, offset + length)), fin);
            }
        }
    }

    /** Closes every open connection with a close handshake. */
    public void closeAll(int code, String reason) {
        for (WebSocket conn : open) {
            conn.close(code, reason);
        }
    }

    /** Waits until at least {@code count} client messages arrived. */
    public List<Received> awaitReceived(int count, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (received.size() >= count) {
                return received();
            }
            sleep(10);
        }
        throw new IllegalStateException("expected " + count + " client messages, got " + received.size());
    }

    /** Stops the server. */
    @Override
    public void close() {
        try {
            stop(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- WebSocketServer ----------

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        int id = connections.incrementAndGet();
        conn.setAttachment(id);
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        handshake.iterateHttpFields().forEachRemaining(name -> headers.put(name, handshake.getFieldValue(name)));
        handshakes.add(headers);
        open.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        open.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Integer id = conn.getAttachment();
        received.add(new Received(id == null ? 0 : id, message, Instant.now()));
        onMessage.accept(conn, message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        byte[] bytes = new byte[message.remaining()];
        message.get(bytes);
        onMessage(conn, new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // a test that cares asserts on what the client saw
    }

    @Override
    public void onStart() {
        started.countDown();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
