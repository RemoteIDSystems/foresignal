package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.security.ticket.TicketStore;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.StreamTestSources;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Stream and tile tickets over a real socket (prompt 023), with {@code openReads} off so the ticket
 * is what authenticates. Time windows run on the application's {@link MutableClock}; every HTTP call
 * has a timeout.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class StreamTicketIT {

    private static final Instant T0 = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired TicketStore tickets;
    @Autowired SseHub hub;
    @Autowired JsonMapper json;

    private final List<SseTestClient> streams = new ArrayList<>();
    private TokenService.CreatedToken reader;
    private String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        tickets.clear();
        admin = adminTokens.rotate();
        reader = TestTokens.create(tokens, "browser", Scope.READ);
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return HTTP.send(request.timeout(TIMEOUT).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String mint(String body) throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(base() + "/v1/stream-tickets"))
                .header("Authorization", "Bearer " + reader.plaintext())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(201);
        JsonNode node = json.readTree(response.body());
        assertThat(node.get("ticket").asString()).matches("^fst_[A-Za-z0-9_-]{32}$");
        return node.get("ticket").asString();
    }

    private SseTestClient stream(String path, String ticket) {
        SseTestClient client = SseTestClient.connect(base(), path + "?ticket=" + ticket, Map.of());
        streams.add(client);
        return client;
    }

    private int get(String path) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base() + path)).GET()).statusCode();
    }

    private void closeServerSide(String ticket) {
        hub.clients().forEach(SseClient::close);
        await().atMost(TIMEOUT).until(() -> !tickets.inUse(ticket));
    }

    @Test
    @DisplayName("without a ticket the stream is 401 (openReads off); with one it opens")
    void ticketAuthenticatesTheStream() throws Exception {
        String ticket = mint("{\"purpose\":\"stream\"}");

        assertThat(get("/v1/changes/stream")).isEqualTo(401);
        SseTestClient client = stream("/v1/changes/stream", ticket);

        assertThat(client.status()).isEqualTo(200);
        assertThat(hub.clients()).anySatisfy(c -> assertThat(c.principal().id())
                .isEqualTo("token:" + reader.entity().getId()));
    }

    @Test
    @DisplayName("041: with openReads on, stream and tile tickets still authenticate, and an invalid ticket is 401")
    void ticketsUnderOpenReads() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        String stream = mint("{\"purpose\":\"stream\"}");
        String tiles = mint("{\"purpose\":\"tiles\",\"ttl\":\"10m\"}");

        assertThat(get("/v1/tiles.json")).isEqualTo(200);
        assertThat(get("/v1/tiles.json?ticket=" + tiles)).isEqualTo(200);
        // A ticket on a covered path is checked even where the path would be open without it (023 Issue 2).
        assertThat(get("/v1/tiles.json?ticket=fst_" + "x".repeat(32))).isEqualTo(401);
        SseTestClient client = stream("/v1/changes/stream", stream);
        assertThat(client.status()).isEqualTo(200);
        assertThat(hub.clients()).anySatisfy(c -> assertThat(c.principal().id())
                .isEqualTo("token:" + reader.entity().getId()));
    }

    @Test
    @DisplayName("the first connect must come within 60 s of minting")
    void firstConnectWindow() throws Exception {
        String late = mint("{\"purpose\":\"stream\"}");
        clock.advance(Duration.ofSeconds(61));

        assertThat(stream("/v1/alerts/stream", late).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("one concurrent stream per ticket: a second connect is 429 STREAM_LIMIT")
    void oneConcurrentStream() throws Exception {
        String ticket = mint("{\"purpose\":\"stream\"}");
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(200);

        assertThat(stream("/v1/events/stream", ticket).status()).isEqualTo(429);
    }

    @Test
    @DisplayName("reconnects work within 5 min of the last disconnect, then the ticket is gone")
    void reconnectWindow() throws Exception {
        String ticket = mint("{\"purpose\":\"stream\"}");
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(200);
        closeServerSide(ticket);

        clock.advance(Duration.ofMinutes(4));
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(200);
        closeServerSide(ticket);

        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("a stream ticket is rejected after 24 h even inside the reconnect window")
    void streamLifetime() throws Exception {
        String ticket = mint("{\"purpose\":\"stream\"}");
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(200);
        // Keep the stream open past 24 h, then drop it: the reconnect window has not closed, the
        // lifetime has.
        clock.advance(Duration.ofHours(24).plusSeconds(1));
        closeServerSide(ticket);

        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("a tile ticket is reusable until its ttl, then 401")
    void tileTicketReuse() throws Exception {
        String ticket = mint("{\"purpose\":\"tiles\",\"ttl\":\"10m\"}");

        assertThat(get("/v1/tiles.json")).isEqualTo(401);
        for (int i = 0; i < 3; i++) {
            assertThat(get("/v1/tiles.json?ticket=" + ticket)).isEqualTo(200);
        }
        clock.advance(Duration.ofMinutes(10));
        assertThat(get("/v1/tiles.json?ticket=" + ticket)).isEqualTo(401);
    }

    @Test
    @DisplayName("a ticket is accepted only for its purpose's paths")
    void wrongPurposeIsRejected() throws Exception {
        String stream = mint("{\"purpose\":\"stream\"}");
        String tiles = mint("{\"purpose\":\"tiles\"}");

        assertThat(get("/v1/tiles.json?ticket=" + stream)).isEqualTo(401);
        assertThat(stream("/v1/changes/stream", tiles).status()).isEqualTo(401);
        assertThat(get("/v1/records?ticket=" + tiles)).isEqualTo(401);
    }

    @Test
    @DisplayName("revoking the parent token closes its ticket streams and invalidates its tickets")
    void revokeCascades() throws Exception {
        String ticket = mint("{\"purpose\":\"stream\"}");
        String tiles = mint("{\"purpose\":\"tiles\"}");
        SseTestClient client = stream("/v1/changes/stream", ticket);
        assertThat(client.status()).isEqualTo(200);

        HttpResponse<String> revoke = send(HttpRequest.newBuilder(
                        URI.create(base() + "/v1/tokens/" + reader.entity().getId()))
                .header("Authorization", "Bearer " + admin)
                .DELETE());
        assertThat(revoke.statusCode()).isEqualTo(204);

        assertThat(client.awaitEnd(TIMEOUT)).isTrue();
        assertThat(get("/v1/tiles.json?ticket=" + tiles)).isEqualTo(401);
        assertThat(stream("/v1/changes/stream", ticket).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("the ticket never appears in logs or in URLs echoed in responses")
    void ticketIsNotEchoed() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        root.addAppender(logs);
        Level level = root.getLevel();
        root.setLevel(Level.DEBUG);
        try {
            String tiles = mint("{\"purpose\":\"tiles\"}");
            HttpResponse<String> index = send(HttpRequest.newBuilder(URI.create(base() + "/v1/tiles.json?ticket=" + tiles)).GET());
            assertThat(index.statusCode()).isEqualTo(200);
            assertThat(index.body()).doesNotContain(tiles);
            String stream = mint("{\"purpose\":\"stream\"}");
            assertThat(stream("/v1/changes/stream", stream).status()).isEqualTo(200);
            assertThat(get("/v1/tiles.json?ticket=" + stream)).isEqualTo(401);

            assertThat(logs.list).extracting(ILoggingEvent::getFormattedMessage)
                    .noneMatch(m -> m != null && (m.contains(tiles) || m.contains(stream)));
        } finally {
            root.setLevel(level);
            root.detachAppender(logs);
            logs.stop();
        }
    }

    @Test
    @DisplayName("minting needs a token even under openReads, and validates the request")
    void mintingRules() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        HttpResponse<String> anonymous = send(HttpRequest.newBuilder(URI.create(base() + "/v1/stream-tickets"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"purpose\":\"stream\"}")));
        assertThat(anonymous.statusCode()).isEqualTo(401);

        for (String bad : List.of("{}", "{\"purpose\":\"video\"}", "{\"purpose\":\"stream\",\"ttl\":\"5m\"}",
                "{\"purpose\":\"tiles\",\"ttl\":\"2h\"}", "{\"purpose\":\"tiles\",\"paths\":[\"/v1/records\"]}")) {
            HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(base() + "/v1/stream-tickets"))
                    .header("Authorization", "Bearer " + reader.plaintext())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bad)));
            assertThat(response.statusCode()).as(bad).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("a ticket narrowed to some paths is refused on the others")
    void narrowedPaths() throws Exception {
        String ticket = mint("{\"purpose\":\"tiles\",\"paths\":[\"/v1/tiles.json\"]}");

        assertThat(get("/v1/tiles.json?ticket=" + ticket)).isEqualTo(200);
        assertThat(get("/v1/tiles/earthquake.json?ticket=" + ticket)).isEqualTo(401);
    }
}
