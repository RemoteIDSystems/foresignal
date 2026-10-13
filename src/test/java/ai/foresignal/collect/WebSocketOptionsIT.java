package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.spi.WebSocketOptions;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code permessage-deflate} path of {@code SourceHttp.openWebSocket(…, WebSocketOptions)} (prompt 063):
 * against a deflate-negotiating loopback server, the budget, backoff, User-Agent and metering behave exactly
 * as on the JDK path, and the socket meters compressed bytes.
 */
@IntegrationTest
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class WebSocketOptionsIT {

    private static final String SOURCE = "ws-deflate-test";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private UsageMeter usage;
    private BackoffRegistry backoff;
    private BudgetGovernor budgets;
    private BoundSourceHttp http;
    private LoopbackWebSocketServer server;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(2, 10));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        http = (BoundSourceHttp) new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc,
                jsonMapper, properties, clock).forSource(SOURCE, FakeStreamingAdapter.defaultDescriptor(SOURCE).build(),
                null);
        server = LoopbackWebSocketServer.start(true);
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    /** Records whole messages and requests one more only when told to. */
    private static final class Recording implements WebSocket.Listener {
        final List<String> messages = new CopyOnWriteArrayList<>();
        final List<Boolean> lasts = new CopyOnWriteArrayList<>();
        final boolean autoRequest;

        Recording(boolean autoRequest) {
            this.autoRequest = autoRequest;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            messages.add(new String(bytes, StandardCharsets.UTF_8));
            lasts.add(last);
            if (autoRequest) {
                webSocket.request(1);
            }
            return null;
        }
    }

    private WebSocket open(WebSocket.Listener listener) throws Exception {
        return http.openWebSocket(server.uri(), listener, Map.of(), WebSocketOptions.DEFLATE);
    }

    @Test
    @DisplayName("permessage-deflate is offered and negotiated; the socket is a MeteredWebSocket")
    void negotiates() throws Exception {
        WebSocket socket = open(new Recording(true));

        assertThat(socket).isInstanceOf(MeteredWebSocket.class);
        assertThat(((MeteredWebSocket) socket).perMessageDeflateNegotiated()).isTrue();
        assertThat(server.handshakes().get(0).get("Sec-WebSocket-Extensions")).contains("permessage-deflate");
        socket.abort();
    }

    @Test
    @DisplayName("the handshake carries Foresignal's User-Agent; a caller's User-Agent is ignored")
    void userAgent() throws Exception {
        http.openWebSocket(server.uri(), new Recording(true), Map.of("User-Agent", "Mozilla/5.0", "X-Trace", "abc"),
                WebSocketOptions.DEFLATE).abort();

        Map<String, String> sent = server.handshakes().get(0);
        assertThat(sent.get("User-Agent")).isEqualTo(install.userAgent());
        assertThat(sent.get("X-Trace")).isEqualTo("abc");
    }

    @Test
    @DisplayName("each attempt consumes one budget unit and is metered as one request; the third is refused")
    void budgetOncePerAttempt() throws Exception {
        long before = budgets.status(SOURCE).minuteRemaining();

        open(new Recording(true)).abort();
        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(before - 1);
        open(new Recording(true)).abort();

        assertThatThrownBy(() -> open(new Recording(true))).isInstanceOf(BudgetExceededException.class);
        assertThat(server.connectionCount()).isEqualTo(2);
        UsageTotalsWindow totals = usage.lastHour(SOURCE);
        assertThat(totals.requests()).isEqualTo(2);
        assertThat(totals.errors()).isZero();
        assertThat(totals.refused()).isEqualTo(1);
        assertThat(http.traces()).as("a connection records no FetchTrace, as on the JDK path").isEmpty();
    }

    @Test
    @DisplayName("a source in backoff does not connect and spends nothing")
    void backoff() {
        backoff.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(10)));
        long before = budgets.status(SOURCE).minuteRemaining();

        assertThatThrownBy(() -> open(new Recording(true))).isInstanceOf(InBackoffException.class);

        assertThat(server.connectionCount()).isZero();
        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(before);
    }

    @Test
    @DisplayName("a connection that cannot be made is metered as an error and still costs its unit")
    void failedConnect() throws Exception {
        int port;
        try (ServerSocket probe = new ServerSocket(0)) {
            port = probe.getLocalPort();
        }
        URI dead = URI.create("ws://127.0.0.1:" + port + "/v0/stream");

        assertThatThrownBy(() -> http.openWebSocket(dead, new Recording(true), Map.of(), WebSocketOptions.DEFLATE))
                .isInstanceOf(UpstreamIOException.class);

        UsageTotalsWindow totals = usage.lastHour(SOURCE);
        assertThat(totals.requests()).isEqualTo(1);
        assertThat(totals.errors()).isEqualTo(1);
        assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(1);
    }

    @Test
    @DisplayName("messages arrive whole and inflated, while the wire count is the compressed size")
    void compressedMetering() throws Exception {
        Recording listener = new Recording(true);
        MeteredWebSocket socket = (MeteredWebSocket) open(listener);
        server.awaitConnection(Duration.ofSeconds(5));
        String message = "{\"MessageType\":\"PositionReport\"}".repeat(2000);

        server.sendFragmentedBinary(message, 3);

        await().atMost(Duration.ofSeconds(10)).until(() -> listener.messages.size() == 1);
        assertThat(listener.messages.get(0)).isEqualTo(message);
        assertThat(listener.lasts).containsExactly(true);
        assertThat(socket.wireBytesReceived()).isPositive().isLessThan(message.length() / 4);
        socket.abort();
    }

    @Test
    @DisplayName("without request(1) the next message waits: back-pressure as with the JDK client")
    void backPressure() throws Exception {
        Recording listener = new Recording(false);
        WebSocket socket = open(listener);
        server.awaitConnection(Duration.ofSeconds(5));

        server.sendBinary("one");
        server.sendBinary("two");
        await().atMost(Duration.ofSeconds(10)).until(() -> listener.messages.size() == 1);
        Thread.sleep(300);
        assertThat(listener.messages).containsExactly("one");

        socket.request(1);
        await().atMost(Duration.ofSeconds(10)).until(() -> listener.messages.size() == 2);
        socket.abort();
    }

    @Test
    @DisplayName("without the option the JDK client is used, as before")
    void defaultIsJdk() throws Exception {
        WebSocket socket = http.openWebSocket(server.uri(), new Recording(true), Map.of(), WebSocketOptions.DEFAULT);

        assertThat(socket).isNotInstanceOf(MeteredWebSocket.class);
        socket.abort();
    }
}
