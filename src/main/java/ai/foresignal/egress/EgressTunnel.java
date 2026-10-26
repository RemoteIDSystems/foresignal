package ai.foresignal.egress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A loopback {@code CONNECT} bridge for one egress route (prompt R2-018).
 *
 * <p>{@code java.net.http.HttpClient} sends through an HTTP proxy only: it ignores a SOCKS proxy (and would then
 * connect direct), and its proxy authentication does not cover the {@code CONNECT} of an https request by default. So a
 * SOCKS5 route, and an HTTP route with credentials, get a bridge: the route's client uses
 * {@code 127.0.0.1:{port}} as a plain HTTP proxy, and the bridge opens each tunnel upstream itself:
 * <ul>
 *   <li>{@code socks5}: the RFC 1928 greeting, RFC 1929 username/password when the route has them, and a
 *       {@code CONNECT} by host name (address type 3), so the target is resolved by the proxy, not here;
 *   <li>{@code http}: {@code CONNECT host:port} with {@code Proxy-Authorization: Basic}.
 * </ul>
 * Only {@code CONNECT} is bridged. A plain-{@code http} request through a bridged route is answered 502 and fails: the
 * bridge cannot carry it on a connection the client may reuse for another host, and it is never sent direct instead.
 *
 * <p>The listener binds the loopback address only and is closed when the route changes or is deleted.
 */
final class EgressTunnel implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(EgressTunnel.class);

    /** The longest request or response head read. */
    static final int MAX_HEAD = 16 * 1024;

    /** The connect and handshake timeout upstream. */
    static final Duration HANDSHAKE_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Where and how the bridge connects upstream.
     *
     * @param type {@link EgressRoute#HTTP} or {@link EgressRoute#SOCKS5}
     * @param host the proxy host
     * @param port the proxy port
     * @param username the proxy username, or null
     * @param password the proxy password, or null
     */
    record Upstream(String type, String host, int port, String username, String password) {

        /** Never prints the credentials. */
        @Override
        public String toString() {
            return type + "://" + host + ":" + port;
        }
    }

    private final Upstream upstream;
    private final ServerSocket server;
    private volatile boolean closed;

    /** Opens the listener on an ephemeral loopback port and starts accepting. */
    EgressTunnel(Upstream upstream) throws IOException {
        this.upstream = upstream;
        this.server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().name("egress-tunnel-" + server.getLocalPort()).start(this::accept);
    }

    /** The loopback address the route's client uses as its proxy. */
    InetSocketAddress address() {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), server.getLocalPort());
    }

    @Override
    public void close() {
        closed = true;
        try {
            server.close();
        } catch (IOException e) {
            log.debug("Egress tunnel listener close: {}", e.getMessage());
        }
    }

    private void accept() {
        while (!closed) {
            try {
                Socket client = server.accept();
                Thread.ofVirtual().start(() -> serve(client));
            } catch (IOException e) {
                if (!closed) {
                    log.warn("Egress tunnel to {} stopped accepting: {}", upstream, e.getMessage());
                }
                return;
            }
        }
    }

    private void serve(Socket client) {
        Socket remote = null;
        try {
            client.setSoTimeout((int) HANDSHAKE_TIMEOUT.toMillis());
            InputStream fromClient = client.getInputStream();
            OutputStream toClient = client.getOutputStream();
            String head = readHead(fromClient);
            String[] line = head.substring(0, Math.max(head.indexOf("\r\n"), 0)).split(" ");
            if (line.length < 3 || !"CONNECT".equalsIgnoreCase(line[0])) {
                refuse(toClient, "This egress route carries https only: a plain-http request is not routed through"
                        + " it, and not sent direct either.");
                return;
            }
            Target target = Target.parse(line[1]);
            remote = new Socket();
            remote.connect(new InetSocketAddress(upstream.host(), upstream.port()),
                    (int) HANDSHAKE_TIMEOUT.toMillis());
            remote.setSoTimeout((int) HANDSHAKE_TIMEOUT.toMillis());
            if (EgressRoute.SOCKS5.equals(upstream.type())) {
                socks5(remote, target);
            } else {
                httpConnect(remote, target);
            }
            toClient.write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            toClient.flush();
            client.setSoTimeout(0);
            remote.setSoTimeout(0);
            Socket upstreamSocket = remote;
            remote = null;
            Thread.ofVirtual().start(() -> pipe(client, upstreamSocket));
            pipe(upstreamSocket, client);
        } catch (IOException | RuntimeException e) {
            try {
                refuse(client.getOutputStream(), "The egress route " + upstream + " could not open the tunnel: "
                        + e.getMessage());
            } catch (IOException ignored) {
                // The client has gone.
            }
            closeQuietly(client);
        } finally {
            closeQuietly(remote);
        }
    }

    /** Copies {@code from} to {@code to} until either ends, then closes both. */
    private static void pipe(Socket from, Socket to) {
        try {
            from.getInputStream().transferTo(to.getOutputStream());
        } catch (IOException e) {
            // A reset or a close on either side ends the tunnel.
        } finally {
            closeQuietly(from);
            closeQuietly(to);
        }
    }

    private void socks5(Socket remote, Target target) throws IOException {
        InputStream in = remote.getInputStream();
        OutputStream out = remote.getOutputStream();
        boolean auth = upstream.username() != null;
        out.write(auth ? new byte[] {5, 2, 0, 2} : new byte[] {5, 1, 0});
        out.flush();
        byte[] choice = readFully(in, 2);
        if (choice[0] != 5) {
            throw new IOException("the proxy is not a SOCKS5 proxy");
        }
        if (choice[1] == 2) {
            if (!auth) {
                throw new IOException("the SOCKS5 proxy requires a username and password");
            }
            byte[] user = upstream.username().getBytes(StandardCharsets.UTF_8);
            byte[] pass = upstream.password() == null ? new byte[0]
                    : upstream.password().getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream request = new ByteArrayOutputStream();
            request.write(1);
            request.write(user.length);
            request.write(user);
            request.write(pass.length);
            request.write(pass);
            out.write(request.toByteArray());
            out.flush();
            byte[] status = readFully(in, 2);
            if (status[1] != 0) {
                throw new IOException("the SOCKS5 proxy refused the username and password");
            }
        } else if (choice[1] != 0) {
            throw new IOException("the SOCKS5 proxy accepts none of the offered authentication methods");
        }
        byte[] host = target.host().getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        request.write(new byte[] {5, 1, 0, 3, (byte) host.length});
        request.write(host);
        request.write(target.port() >> 8);
        request.write(target.port() & 0xff);
        out.write(request.toByteArray());
        out.flush();
        byte[] reply = readFully(in, 4);
        if (reply[1] != 0) {
            throw new IOException("the SOCKS5 proxy answered CONNECT with code " + (reply[1] & 0xff));
        }
        int addressLength = switch (reply[3]) {
            case 1 -> 4;
            case 4 -> 16;
            case 3 -> readFully(in, 1)[0] & 0xff;
            default -> throw new IOException("the SOCKS5 proxy answered with address type " + reply[3]);
        };
        readFully(in, addressLength + 2);
    }

    private void httpConnect(Socket remote, Target target) throws IOException {
        StringBuilder request = new StringBuilder()
                .append("CONNECT ").append(target.authority()).append(" HTTP/1.1\r\n")
                .append("Host: ").append(target.authority()).append("\r\n");
        if (upstream.username() != null) {
            String pair = upstream.username() + ":" + (upstream.password() == null ? "" : upstream.password());
            request.append("Proxy-Authorization: Basic ")
                    .append(Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8))).append("\r\n");
        }
        request.append("\r\n");
        OutputStream out = remote.getOutputStream();
        out.write(request.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
        String head = readHead(remote.getInputStream());
        String status = head.substring(0, Math.max(head.indexOf("\r\n"), 0));
        String[] parts = status.split(" ");
        if (parts.length < 2 || !parts[1].equals("200")) {
            throw new IOException("the HTTP proxy answered CONNECT with \"" + status + "\"");
        }
    }

    /** Reads a head up to and including the blank line. */
    static String readHead(InputStream in) throws IOException {
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        int matched = 0;
        while (head.size() < MAX_HEAD) {
            int b = in.read();
            if (b < 0) {
                throw new IOException("the connection closed before the end of the head");
            }
            head.write(b);
            matched = (b == '\r' && (matched == 0 || matched == 2)) || (b == '\n' && (matched == 1 || matched == 3))
                    ? matched + 1 : (b == '\r' ? 1 : 0);
            if (matched == 4) {
                return head.toString(StandardCharsets.ISO_8859_1);
            }
        }
        throw new IOException("the head is longer than " + MAX_HEAD + " bytes");
    }

    private static byte[] readFully(InputStream in, int length) throws IOException {
        byte[] bytes = in.readNBytes(length);
        if (bytes.length < length) {
            throw new IOException("the proxy closed the connection during the handshake");
        }
        return bytes;
    }

    private static void refuse(OutputStream out, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 502 Bad Gateway\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: "
                + body.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Nothing to do.
        }
    }

    /**
     * A {@code CONNECT} target.
     *
     * @param host the host name or address literal, without brackets
     * @param port the port
     */
    record Target(String host, int port) {

        /** {@code host:port}, bracketing an IPv6 literal. */
        String authority() {
            return (host.contains(":") ? "[" + host + "]" : host) + ":" + port;
        }

        /** Parses {@code host:port} or {@code [v6]:port}. */
        static Target parse(String authority) {
            String host;
            String port;
            if (authority.startsWith("[")) {
                int close = authority.indexOf(']');
                if (close < 0 || close + 1 >= authority.length() || authority.charAt(close + 1) != ':') {
                    throw new IllegalArgumentException("bad CONNECT target " + authority);
                }
                host = authority.substring(1, close);
                port = authority.substring(close + 2);
            } else {
                int colon = authority.lastIndexOf(':');
                if (colon <= 0) {
                    throw new IllegalArgumentException("bad CONNECT target " + authority);
                }
                host = authority.substring(0, colon);
                port = authority.substring(colon + 1);
            }
            int number = Integer.parseInt(port);
            if (number < 1 || number > 65535 || host.isEmpty() || host.length() > 255
                    || !host.chars().allMatch(c -> c > 0x20 && c < 0x7f)) {
                throw new IllegalArgumentException("bad CONNECT target " + authority);
            }
            return new Target(host.toLowerCase(Locale.ROOT), number);
        }
    }

    /** True once {@link #close} has run (tests). */
    boolean closed() {
        return closed || server.isClosed();
    }
}
