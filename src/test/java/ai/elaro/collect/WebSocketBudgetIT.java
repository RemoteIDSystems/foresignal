package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.elaro.config.ElaroProperties;
import ai.elaro.security.InstallIdService;
import ai.elaro.source.CredentialsAccess;
import ai.elaro.source.SourceService;
import ai.elaro.source.spi.Budget;
import ai.elaro.source.spi.SourceDescriptor;
import ai.elaro.source.spi.SourceHttp;
import ai.elaro.source.spi.ValidatedParams;
import ai.elaro.test.IntegrationTest;
import ai.elaro.test.MutableClock;
import ai.elaro.testsupport.adapters.FakeStreamingAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A streaming source's connection is a budgeted request like any other.
 *
 * <p>This is the guarantee that bounds a reconnect storm: a stream that keeps dropping spends its
 * budget rather than hammering the upstream, and once the budget is gone it stops reconnecting
 * instead of reconnecting forever. Asserting it needs a real handshake, because what is being
 * tested is that the budget is consumed <em>before</em> the socket is opened — a mock would only
 * show that a method was called.
 *
 * <p>WireMock has no WebSocket support, so the server here is the smallest thing that completes
 * RFC 6455's opening handshake: it reads the request headers, answers 101 with the computed
 * {@code Sec-WebSocket-Accept}, and never sends a frame. That is enough, and it lets the test
 * inspect exactly which headers Elaro sent.
 */
@IntegrationTest
class WebSocketBudgetIT {

    private static final String SOURCE = "ws-test";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ElaroProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private SourceService sources;
    private CredentialsAccess credentials;
    private UsageMeter usage;
    private BackoffRegistry backoff;
    private BudgetGovernor budgets;
    private HandshakeServer server;

    @BeforeEach
    void setUp() throws IOException {
        clock = new MutableClock(START);
        sources = mock(SourceService.class);
        credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(2, 10));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());

        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        server = new HandshakeServer();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    // ---------- harness ----------

    private SourceHttp http() {
        return http(descriptor().build());
    }

    private SourceHttp http(SourceDescriptor descriptor) {
        return new UpstreamClient(
                        budgets,
                        backoff,
                        usage,
                        install,
                        credentials,
                        sources,
                        jdbc,
                        jsonMapper,
                        properties,
                        clock)
                .forSource(SOURCE, descriptor, null);
    }

    private static SourceDescriptor.Builder descriptor() {
        return FakeStreamingAdapter.defaultDescriptor(SOURCE);
    }

    /** A listener that does nothing: the test is about the connection, not the frames. */
    private static WebSocket.Listener silent() {
        return new WebSocket.Listener() {};
    }

    private WebSocket connect() throws Exception {
        return http().openWebSocket(server.uri(), silent(), Map.of());
    }

    // ---------- budget ----------

    @Test
    @DisplayName("a connection attempt consumes one budget unit")
    void connectionConsumesOneUnit() throws Exception {
        BudgetStatus before = budgets.status(SOURCE);

        WebSocket socket = connect();

        assertThat(socket).isNotNull();
        assertThat(server.connections()).isEqualTo(1);
        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(before.minuteRemaining() - 1);
        socket.abort();
    }

    @Test
    @DisplayName("a connection is metered as one request, so a reconnect loop is visible in usage")
    void connectionIsMeteredAsOneRequest() throws Exception {
        connect().abort();

        UsageTotalsWindow totals = usage.lastHour(SOURCE);
        assertThat(totals.requests()).isEqualTo(1);
        assertThat(totals.errors()).isZero();
        assertThat(totals.refused()).isZero();
    }

    @Test
    @DisplayName("reconnecting past the budget is refused and never reaches the upstream")
    void reconnectStormIsBounded() throws Exception {
        // Two per minute: the third attempt is the one that must not be made.
        WebSocket first = connect();
        WebSocket second = connect();
        assertThat(server.connections()).isEqualTo(2);

        assertThatThrownBy(this::connect)
                .isInstanceOf(BudgetExceededException.class)
                .hasMessageContaining("was not sent");

        assertThat(server.connections()).isEqualTo(2);
        assertThat(usage.lastHour(SOURCE).refused()).isEqualTo(1);

        first.abort();
        second.abort();
    }

    @Test
    @DisplayName("the per-minute refill lets a stream reconnect in the next minute")
    void budgetRefillsForTheNextMinute() throws Exception {
        connect().abort();
        connect().abort();
        assertThatThrownBy(this::connect).isInstanceOf(BudgetExceededException.class);

        clock.advance(Duration.ofMinutes(1));

        connect().abort();
        assertThat(server.connections()).isEqualTo(3);
    }

    // ---------- backoff ----------

    @Test
    @DisplayName("a source in backoff does not connect, and the attempt costs no budget")
    void backoffBlocksWithoutSpendingBudget() {
        backoff.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(10)));
        long remainingBefore = budgets.status(SOURCE).minuteRemaining();

        assertThatThrownBy(this::connect).isInstanceOf(InBackoffException.class);

        assertThat(server.connections()).isZero();
        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(remainingBefore);
        assertThat(usage.lastHour(SOURCE)).isEqualTo(UsageTotalsWindow.ZERO);
    }

    @Test
    @DisplayName("a successful connection clears a backoff that has expired")
    void successClearsBackoff() throws Exception {
        backoff.onThrottled(SOURCE, Optional.of(Duration.ofSeconds(30)));
        clock.advance(Duration.ofSeconds(31));

        connect().abort();

        assertThat(backoff.consecutiveThrottles(SOURCE)).isZero();
        assertThat(backoff.isInBackoff(SOURCE)).isFalse();
    }

    // ---------- headers ----------

    @Test
    @DisplayName("the handshake carries Elaro's User-Agent")
    void sendsTheInstallUserAgent() throws Exception {
        connect().abort();

        assertThat(server.lastHandshake().get("user-agent")).isEqualTo(install.userAgent());
    }

    @Test
    @DisplayName("a caller's User-Agent is ignored: identification is not the adapter's to choose")
    void callerCannotOverrideTheUserAgent() throws Exception {
        http().openWebSocket(
                        server.uri(),
                        silent(),
                        new LinkedHashMap<>(Map.of("User-Agent", "Mozilla/5.0", "X-Trace", "abc")))
                .abort();

        Map<String, String> sent = server.lastHandshake();
        assertThat(sent.get("user-agent")).isEqualTo(install.userAgent());
        assertThat(sent.get("x-trace")).isEqualTo("abc");
    }

    @Test
    @DisplayName("a descriptor's User-Agent override is used for upstreams that demand one")
    void descriptorOverrideWins() throws Exception {
        SourceDescriptor overriding = descriptor().userAgentOverride("SatDump/1.2 (contact@example.org)").build();

        http(overriding).openWebSocket(server.uri(), silent(), Map.of()).abort();

        assertThat(server.lastHandshake().get("user-agent")).isEqualTo("SatDump/1.2 (contact@example.org)");
    }

    // ---------- failure ----------

    @Test
    @DisplayName("a connection that cannot be made is metered as an error, not a success")
    void failedConnectIsMeteredAsAnError() {
        URI dead = server.uriOnClosedPort();

        assertThatThrownBy(() -> http().openWebSocket(dead, silent(), Map.of()))
                .isInstanceOf(UpstreamIOException.class);

        UsageTotalsWindow totals = usage.lastHour(SOURCE);
        assertThat(totals.requests()).isEqualTo(1);
        assertThat(totals.errors()).isEqualTo(1);
    }

    @Test
    @DisplayName("a failed connection still spends its budget, so a dead upstream is not retried forever")
    void failedConnectStillCostsBudget() {
        URI dead = server.uriOnClosedPort();

        assertThatThrownBy(() -> http().openWebSocket(dead, silent(), Map.of()))
                .isInstanceOf(UpstreamIOException.class);

        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(1);
    }

    // ---------- the server ----------

    /**
     * Completes the RFC 6455 opening handshake and nothing else.
     *
     * <p>Sockets are kept open until {@link #close}, because closing one mid-test would surface as a
     * failed connection rather than as the successful one the test is asserting on.
     */
    private static final class HandshakeServer implements AutoCloseable {

        /** RFC 6455 §4.2.2: the GUID the key is concatenated with before hashing. */
        private static final String MAGIC = "258EAFA5-E914-47DA-95CA-5AB0DC85B11F";

        private final ServerSocket socket;
        private final Thread acceptor;
        private final List<Socket> accepted = new CopyOnWriteArrayList<>();
        private final List<Map<String, String>> handshakes =
                Collections.synchronizedList(new ArrayList<>());
        private final AtomicInteger connections = new AtomicInteger();
        private volatile boolean running = true;

        HandshakeServer() throws IOException {
            this.socket = new ServerSocket(0, 0, java.net.InetAddress.getLoopbackAddress());
            this.acceptor = new Thread(this::acceptLoop, "ws-handshake-server");
            this.acceptor.setDaemon(true);
            this.acceptor.start();
        }

        URI uri() {
            return URI.create("ws://127.0.0.1:" + socket.getLocalPort() + "/stream");
        }

        /** A URL on a port nothing is listening on, for the connect-failure cases. */
        URI uriOnClosedPort() {
            try (ServerSocket probe = new ServerSocket(0)) {
                return URI.create("ws://127.0.0.1:" + probe.getLocalPort() + "/stream");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        int connections() {
            return connections.get();
        }

        Map<String, String> lastHandshake() {
            synchronized (handshakes) {
                assertThat(handshakes).isNotEmpty();
                return handshakes.get(handshakes.size() - 1);
            }
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket client = socket.accept();
                    accepted.add(client);
                    handshake(client);
                } catch (IOException e) {
                    // A closed server socket is how this thread is meant to end.
                    return;
                }
            }
        }

        private void handshake(Socket client) throws IOException {
            // ISO-8859-1: the handshake is a byte-oriented HTTP message, not UTF-8 text.
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));

            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }
            Map<String, String> headers = new LinkedHashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    headers.put(
                            line.substring(0, colon).trim().toLowerCase(),
                            line.substring(colon + 1).trim());
                }
            }

            String key = headers.get("sec-websocket-key");
            OutputStream out = client.getOutputStream();
            if (key == null) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
                out.flush();
                return;
            }

            handshakes.add(Map.copyOf(headers));
            connections.incrementAndGet();

            String response =
                    "HTTP/1.1 101 Switching Protocols\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Sec-WebSocket-Accept: "
                            + accept(key)
                            + "\r\n\r\n";
            out.write(response.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
        }

        private static String accept(String key) {
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                byte[] digest = sha1.digest((key + MAGIC).getBytes(StandardCharsets.ISO_8859_1));
                return Base64.getEncoder().encodeToString(digest);
            } catch (NoSuchAlgorithmException e) {
                throw new AssertionError("SHA-1 is required of every JVM", e);
            }
        }

        @Override
        public void close() {
            running = false;
            for (Socket client : accepted) {
                closeQuietly(client);
            }
            closeQuietly(socket);
            acceptor.interrupt();
        }

        private static void closeQuietly(java.io.Closeable target) {
            try {
                target.close();
            } catch (IOException ignored) {
                // Closing a test fixture is best effort.
            }
        }
    }
}
