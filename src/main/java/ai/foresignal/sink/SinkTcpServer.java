package ai.foresignal.sink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only SERVE TCP listener (prompt 110): binds a validated LAN address, accepts at most
 * {@code maxClients} peers that pass {@link ServeGuard#allowedPeer}, and pushes lines to each.
 *
 * <p><strong>Threads and back-pressure.</strong> One virtual thread accepts and does nothing else: the peer
 * check and the client cap, then it hands the socket over. Each client gets a reader thread (which also runs
 * {@link Handler#onConnect}, so a large snapshot never stalls the acceptor) and a writer thread draining a
 * bounded queue of {@value #QUEUE_CAPACITY} frames. {@link Client#sendLine} and {@link Client#sendRaw} only
 * enqueue, so a sink's delivery thread never blocks on a slow socket; a client whose queue is full is
 * disconnected (it can reconnect and receives a fresh snapshot). Received lines are capped at
 * {@value #MAX_LINE} bytes; a longer line disconnects the client. An optional keepalive line is queued to every
 * client at a fixed interval ({@link Handler#keepalive()}, the APRS-IS 20 s comment).
 */
public final class SinkTcpServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SinkTcpServer.class);

    /** Frames queued per client before it is disconnected as too slow. */
    public static final int QUEUE_CAPACITY = 10_000;

    /** The longest line a client may send, in bytes. */
    public static final int MAX_LINE = 1024;

    /** A connected client. */
    public interface Handler {
        /** Called once when a client connects, on the client's own thread; {@code client.sendLine(...)} queues to it. */
        void onConnect(Client client);

        /** Called for each received line (clients cannot command; the line is usually discarded). */
        default void onLine(Client client, String line) {
        }

        /** Called when a client disconnects. */
        default void onClose(Client client) {
        }

        /** The keepalive line and its interval, or null for none. */
        default Keepalive keepalive() {
            return null;
        }
    }

    /**
     * A line queued to every client at a fixed interval.
     *
     * @param line the line (without CRLF)
     * @param every the interval
     */
    public record Keepalive(String line, Duration every) {}

    /** A connected client handle. */
    public static final class Client {
        private final Socket socket;
        private final String address;
        private final Instant connectedAt;
        private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        private volatile boolean open = true;

        Client(Socket socket) {
            this.socket = socket;
            this.address = socket.getRemoteSocketAddress() instanceof InetSocketAddress inet
                    ? inet.getAddress().getHostAddress() : "unknown";
            this.connectedAt = Instant.now();
        }

        /** The peer address. */
        public String address() {
            return address;
        }

        /** When the client connected. */
        public Instant connectedAt() {
            return connectedAt;
        }

        /** True while the socket is open. */
        public boolean open() {
            return open;
        }

        /** Queues a line (with CRLF). */
        public void sendLine(String line) {
            sendRaw((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        /** Queues raw bytes; a full queue disconnects the client (too slow). Never blocks. */
        public void sendRaw(byte[] bytes) {
            if (!open) {
                return;
            }
            if (!queue.offer(bytes)) {
                log.debug("Sink client {} is too slow; disconnecting", address);
                close();
            }
        }

        /** Frames waiting to be written. */
        public int queued() {
            return queue.size();
        }

        void writeLoop() {
            try {
                OutputStream out = socket.getOutputStream();
                while (open) {
                    byte[] next = queue.poll(1, TimeUnit.SECONDS);
                    if (next == null) {
                        continue;
                    }
                    out.write(next);
                    byte[] more;
                    while ((more = queue.poll()) != null) {
                        out.write(more);
                    }
                    out.flush();
                }
            } catch (IOException e) {
                // Peer went away.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                close();
            }
        }

        void close() {
            open = false;
            queue.clear();
            try {
                socket.close();
            } catch (IOException e) {
                // Already closed.
            }
        }
    }

    private final ServerSocket server;
    private final ServeGuard guard;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong rejected = new AtomicLong();
    private final int maxClients;
    private final boolean allowTailnet;
    private final Handler handler;
    private final Thread acceptor;
    private final Thread keepaliveThread;
    private volatile boolean closed;

    /** Binds and starts accepting. */
    public SinkTcpServer(ServeGuard guard, InetAddress bind, int port, int maxClients, boolean allowTailnet,
            Handler handler) {
        this.guard = guard == null ? new ServeGuard() : guard;
        this.maxClients = Math.max(1, maxClients);
        this.allowTailnet = allowTailnet;
        this.handler = handler;
        try {
            this.server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(bind, port));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not bind the sink server to " + bind + ":" + port, e);
        }
        this.acceptor = Thread.ofVirtual().name("sink-serve-" + port).start(this::acceptLoop);
        Keepalive keepalive = handler.keepalive();
        this.keepaliveThread = keepalive == null ? null
                : Thread.ofVirtual().name("sink-keepalive-" + port).start(() -> keepaliveLoop(keepalive));
    }

    /** The bound port. */
    public int port() {
        return server.getLocalPort();
    }

    /** The connected clients. */
    public List<Client> clients() {
        return List.copyOf(clients);
    }

    /** The client cap. */
    public int maxClients() {
        return maxClients;
    }

    /** Connections closed because the peer was not allowed or the cap was reached. */
    public long rejected() {
        return rejected.get();
    }

    /** Queues {@code line} to every connected client. */
    public void broadcast(String line) {
        byte[] bytes = (line + "\r\n").getBytes(StandardCharsets.UTF_8);
        for (Client client : clients) {
            client.sendRaw(bytes);
        }
    }

    private void acceptLoop() {
        while (!closed) {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException e) {
                if (closed) {
                    return;
                }
                continue;
            }
            InetAddress peer = socket.getInetAddress();
            if (!guard.allowedPeer(peer, allowTailnet) || clients.size() >= maxClients) {
                rejected.incrementAndGet();
                closeQuietly(socket);
                continue;
            }
            try {
                socket.setTcpNoDelay(true);
            } catch (IOException e) {
                closeQuietly(socket);
                continue;
            }
            Client client = new Client(socket);
            clients.add(client);
            Thread.ofVirtual().name("sink-client-writer").start(client::writeLoop);
            Thread.ofVirtual().name("sink-client-reader").start(() -> clientLoop(client));
        }
    }

    private void clientLoop(Client client) {
        try {
            try {
                handler.onConnect(client);
            } catch (RuntimeException e) {
                log.warn("Sink client handler failed on connect", e);
            }
            InputStream in = new java.io.BufferedInputStream(client.socket.getInputStream());
            String line;
            while ((line = readLine(in)) != null) {
                try {
                    handler.onLine(client, line);
                } catch (RuntimeException e) {
                    log.warn("Sink client handler failed on line", e);
                }
            }
        } catch (IOException e) {
            // Client disconnected, or sent a line longer than MAX_LINE.
        } finally {
            clients.remove(client);
            client.close();
            try {
                handler.onClose(client);
            } catch (RuntimeException e) {
                log.warn("Sink client handler failed on close", e);
            }
        }
    }

    /**
     * Reads one LF- or CRLF-terminated line of at most {@value #MAX_LINE} bytes (UTF-8), or null at end of stream.
     *
     * @throws IOException when the line is longer than the limit
     */
    static String readLine(InputStream in) throws IOException {
        byte[] buffer = new byte[MAX_LINE];
        int length = 0;
        int b;
        boolean any = false;
        while ((b = in.read()) != -1) {
            any = true;
            if (b == '\n') {
                break;
            }
            if (length >= MAX_LINE) {
                throw new IOException("line longer than " + MAX_LINE + " bytes");
            }
            buffer[length++] = (byte) b;
        }
        if (!any) {
            return null;
        }
        if (length > 0 && buffer[length - 1] == '\r') {
            length--;
        }
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }

    private void keepaliveLoop(Keepalive keepalive) {
        while (!closed) {
            try {
                Thread.sleep(keepalive.every().toMillis());
            } catch (InterruptedException e) {
                return;
            }
            broadcast(keepalive.line());
        }
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore.
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            server.close();
        } catch (IOException e) {
            // Ignore.
        }
        if (keepaliveThread != null) {
            keepaliveThread.interrupt();
        }
        acceptor.interrupt();
        for (Client client : clients) {
            client.close();
        }
        clients.clear();
    }
}
