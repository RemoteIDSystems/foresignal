package ai.foresignal.sink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only SERVE TCP listener (prompt 110): binds a validated LAN address, accepts at most
 * {@code maxClients} peers that pass {@link ServeGuard#allowedPeer}, and pushes lines to each. One
 * virtual thread accepts and one reads each client.
 */
public final class SinkTcpServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SinkTcpServer.class);

    /** A connected client. */
    public interface Handler {
        /** Called once when a client connects; {@code client.sendLine(...)} writes to it. */
        void onConnect(Client client);

        /** Called for each received line (clients cannot command; the line is usually discarded). */
        default void onLine(Client client, String line) {
        }

        /** Called when a client disconnects. */
        default void onClose(Client client) {
        }
    }

    /** A connected client handle. */
    public static final class Client {
        private final Socket socket;
        private final String address;
        private final Instant connectedAt;
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

        /** Writes a line (with CRLF). */
        public synchronized void sendLine(String line) {
            sendRaw((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        /** Writes raw bytes. */
        public synchronized void sendRaw(byte[] bytes) {
            if (!open) {
                return;
            }
            try {
                OutputStream out = socket.getOutputStream();
                out.write(bytes);
                out.flush();
            } catch (IOException e) {
                close();
            }
        }

        void close() {
            open = false;
            try {
                socket.close();
            } catch (IOException e) {
                // Already closed.
            }
        }
    }

    private final ServerSocket server;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong rejected = new AtomicLong();
    private final int maxClients;
    private final boolean allowTailnet;
    private final Handler handler;
    private final Thread acceptor;
    private volatile boolean closed;

    /** Binds and starts accepting. */
    public SinkTcpServer(ServeGuard guard, InetAddress bind, int port, int maxClients, boolean allowTailnet,
            Handler handler) {
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
    }

    /** The bound port. */
    public int port() {
        return server.getLocalPort();
    }

    /** The connected clients. */
    public List<Client> clients() {
        return List.copyOf(clients);
    }

    /** Connections closed because the peer was not allowed. */
    public long rejected() {
        return rejected.get();
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
            try {
                socket.setTcpNoDelay(true);
            } catch (IOException e) {
                closeQuietly(socket);
                continue;
            }
            InetAddress peer = socket.getInetAddress();
            if (!guardAllows(peer) || clients.size() >= maxClients) {
                rejected.incrementAndGet();
                closeQuietly(socket);
                continue;
            }
            Client client = new Client(socket);
            clients.add(client);
            try {
                handler.onConnect(client);
            } catch (RuntimeException e) {
                log.warn("Sink client handler failed on connect", e);
            }
            Thread.ofVirtual().start(() -> readLoop(client));
        }
    }

    private boolean guardAllows(InetAddress peer) {
        return new ServeGuard().allowedPeer(peer, allowTailnet);
    }

    private void readLoop(Client client) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(client.socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    handler.onLine(client, line);
                } catch (RuntimeException e) {
                    log.warn("Sink client handler failed on line", e);
                }
            }
        } catch (IOException e) {
            // Client disconnected.
        } finally {
            clients.remove(client);
            client.close();
            handler.onClose(client);
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
        for (Client client : clients) {
            client.close();
        }
        clients.clear();
    }
}
