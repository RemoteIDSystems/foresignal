package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The SERVE TCP listener (prompt 110, task 3; {@code ServeBindingTest} in the prompt together with
 * {@link ServeGuardTest}): a disallowed peer is closed and counted, {@code maxClients} is enforced, the snapshot
 * runs on the client's thread, an over-long line disconnects, writes are queued (never blocking the caller), and
 * the keepalive line reaches every client.
 */
class SinkTcpServerTest {

    static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

    static SinkTcpServer server(ServeGuard guard, int maxClients, SinkTcpServer.Handler handler) {
        return new SinkTcpServer(guard, LOOPBACK, 0, maxClients, false, handler);
    }

    static boolean closedByServer(Socket socket) throws IOException {
        socket.setSoTimeout(5_000);
        return socket.getInputStream().read() == -1;
    }

    @Test
    @DisplayName("a peer the guard refuses is closed immediately and counted")
    void disallowedPeer() throws Exception {
        ServeGuard refuseAll = new ServeGuard() {
            @Override
            public boolean allowedPeer(InetAddress address, boolean allowTailnet) {
                return false;
            }
        };
        try (SinkTcpServer server = server(refuseAll, 8, client -> client.sendLine("hello"));
                Socket socket = new Socket(LOOPBACK, server.port())) {
            assertThat(closedByServer(socket)).isTrue();
            await().atMost(Duration.ofSeconds(5)).until(() -> server.rejected() == 1);
            assertThat(server.clients()).isEmpty();
        }
    }

    @Test
    @DisplayName("maxClients: the connection over the cap is closed; the others get the snapshot")
    void maxClients() throws Exception {
        try (SinkTcpServer server = server(new ServeGuard(), 2, client -> client.sendLine("snapshot"));
                Socket a = new Socket(LOOPBACK, server.port());
                Socket b = new Socket(LOOPBACK, server.port())) {
            await().atMost(Duration.ofSeconds(5)).until(() -> server.clients().size() == 2);
            try (Socket c = new Socket(LOOPBACK, server.port())) {
                assertThat(closedByServer(c)).isTrue();
            }
            assertThat(server.rejected()).isEqualTo(1);
            assertThat(server.maxClients()).isEqualTo(2);
            a.setSoTimeout(5_000);
            b.setSoTimeout(5_000);
            BufferedReader ra = new BufferedReader(new InputStreamReader(a.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader rb = new BufferedReader(new InputStreamReader(b.getInputStream(), StandardCharsets.UTF_8));
            assertThat(ra.readLine()).isEqualTo("snapshot");
            assertThat(rb.readLine()).isEqualTo("snapshot");
            server.broadcast("MSG,3");
            assertThat(ra.readLine()).isEqualTo("MSG,3");
        }
    }

    @Test
    @DisplayName("received lines are capped: an over-long line disconnects the client; normal lines reach the handler")
    void lineLimit() throws Exception {
        List<String> lines = new CopyOnWriteArrayList<>();
        SinkTcpServer.Handler handler = new SinkTcpServer.Handler() {
            @Override
            public void onConnect(SinkTcpServer.Client client) {
            }

            @Override
            public void onLine(SinkTcpServer.Client client, String line) {
                lines.add(line);
            }
        };
        try (SinkTcpServer server = server(new ServeGuard(), 4, handler);
                Socket socket = new Socket(LOOPBACK, server.port())) {
            OutputStream out = socket.getOutputStream();
            out.write("user N0CALL pass -1\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            await().atMost(Duration.ofSeconds(5)).until(() -> lines.contains("user N0CALL pass -1"));
            out.write(new byte[SinkTcpServer.MAX_LINE + 10]);
            out.flush();
            assertThat(closedByServer(socket)).isTrue();
            await().atMost(Duration.ofSeconds(5)).until(() -> server.clients().isEmpty());
        }
    }

    @Test
    @DisplayName("readLine: LF and CRLF, end of stream, and the length limit")
    void readLineUnit() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("a\r\nb\nc".getBytes(StandardCharsets.UTF_8));
        assertThat(SinkTcpServer.readLine(in)).isEqualTo("a");
        assertThat(SinkTcpServer.readLine(in)).isEqualTo("b");
        assertThat(SinkTcpServer.readLine(in)).isEqualTo("c");
        assertThat(SinkTcpServer.readLine(in)).isNull();
        assertThatThrownBy(() -> SinkTcpServer.readLine(new ByteArrayInputStream(new byte[SinkTcpServer.MAX_LINE + 1])))
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("the keepalive line is queued to every client at its interval")
    void keepalive() throws Exception {
        SinkTcpServer.Handler handler = new SinkTcpServer.Handler() {
            @Override
            public void onConnect(SinkTcpServer.Client client) {
            }

            @Override
            public SinkTcpServer.Keepalive keepalive() {
                return new SinkTcpServer.Keepalive("# keepalive", Duration.ofMillis(100));
            }
        };
        try (SinkTcpServer server = server(new ServeGuard(), 4, handler);
                Socket socket = new Socket(LOOPBACK, server.port())) {
            assertThat(readLine(socket)).isEqualTo("# keepalive");
        }
    }

    @Test
    @DisplayName("sendLine only queues: a caller is never blocked by a client that does not read")
    void nonBlocking() throws Exception {
        try (SinkTcpServer server = server(new ServeGuard(), 4, client -> { });
                Socket socket = new Socket(LOOPBACK, server.port())) {
            await().atMost(Duration.ofSeconds(5)).until(() -> server.clients().size() == 1);
            String big = "x".repeat(1000);
            long start = System.nanoTime();
            for (int i = 0; i < SinkTcpServer.QUEUE_CAPACITY + 100; i++) {
                server.broadcast(big);
            }
            assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(5));
        }
    }

    static String readLine(Socket socket) throws IOException {
        socket.setSoTimeout(5_000);
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
    }
}
