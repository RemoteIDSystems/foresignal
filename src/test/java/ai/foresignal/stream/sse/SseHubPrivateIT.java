package ai.foresignal.stream.sse;

import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Private sources on the SSE hub (prompt 039): each client is filtered at publish time with its own
 * private gate (admin and holders of the private grant get private changes; read, plain agent and
 * anonymous clients do not), and revoking a token or a feed key resets its open streams ({@code reset}
 * with reason {@code revoked}, then the stream ends). Every wait has a timeout.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class SseHubPrivateIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(5);
    static final Duration QUIET = Duration.ofSeconds(2);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;
    @Autowired SseHub hub;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired FeedKeyService feedKeys;

    private final List<SseTestClient> streams = new ArrayList<>();
    private Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        retention.reset();
        settings.invalidateCache();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        retention.reset();
    }

    private SseTestClient open(String path, String bearer) {
        Map<String, String> headers = bearer == null ? Map.of() : Map.of("Authorization", bearer);
        SseTestClient stream = SseTestClient.connect("http://localhost:" + port, path, headers);
        streams.add(stream);
        assertThat(stream.status()).as(path).isEqualTo(200);
        return stream;
    }

    private void ingest(String source, String kind, String id) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingestAt(source, now, NormalizedRecord.builder(id, kind).observedAt(now).build());
    }

    private static boolean mentions(List<Event> events, String source) {
        return events.stream().anyMatch(e -> e.data() != null && e.data().contains(source));
    }

    @Test
    @DisplayName("the hub filters per client: private changes reach admin and the private grant only")
    void filteredAtTheHub() {
        String admin = TestTokens.bearer(TestTokens.create(tokens, "sse-admin", Scope.ADMIN));
        String granted = TestTokens.bearer(TestTokens.create(tokens, "sse-granted", Scope.READ, true));
        String reader = TestTokens.bearer(TestTokens.create(tokens, "sse-reader", Scope.READ));
        String agent = TestTokens.bearer(TestTokens.create(tokens, "sse-agent", Scope.AGENT));
        SseTestClient adminStream = open("/v1/changes/stream", admin);
        SseTestClient grantedStream = open("/v1/changes/stream", granted);
        SseTestClient readerStream = open("/v1/changes/stream", reader);
        SseTestClient agentStream = open("/v1/changes/stream", agent);
        SseTestClient anonymousStream = open("/v1/changes/stream", null);

        ingest(PRIVATE, PRIVATE_KIND, "secret-1");
        ingest(STANDARD, STANDARD_KIND, "open-1");

        assertThat(adminStream.next("change", WAIT)).isNotNull();
        adminStream.collect("change", QUIET);
        assertThat(mentions(adminStream.received(), PRIVATE)).as("admin gets the private change").isTrue();
        grantedStream.collect("change", QUIET);
        assertThat(mentions(grantedStream.received(), PRIVATE)).as("the private grant gets it").isTrue();
        for (SseTestClient hidden : List.of(readerStream, agentStream, anonymousStream)) {
            hidden.collect("change", QUIET);
            assertThat(mentions(hidden.received(), STANDARD)).as("public changes still flow").isTrue();
            assertThat(mentions(hidden.received(), PRIVATE)).as("no private change").isFalse();
        }
    }

    @Test
    @DisplayName("revoking a token resets its streams with reason revoked, then the stream ends")
    void tokenRevocationResets() {
        TokenService.CreatedToken created = TestTokens.create(tokens, "sse-revoked", Scope.READ, true);
        SseTestClient stream = open("/v1/changes/stream", TestTokens.bearer(created));

        tokens.revoke(created.entity().getId());

        Event reset = stream.next("reset", WAIT);
        assertThat(reset).as("a reset control event").isNotNull();
        assertThat(JSON.readTree(reset.data()).get("reason").asString()).isEqualTo("revoked");
        assertThat(stream.awaitEnd(WAIT)).isTrue();
    }

    @Test
    @DisplayName("revoking a feed key resets its streams too")
    void feedKeyRevocationResets() {
        FeedKeyService.CreatedKey key = feedKeys.create(JSON.readTree("{\"name\":\"sse-kiosk\","
                + "\"prefixes\":[\"/v1/changes/stream\"],\"restrictions\":{\"sources\":[\"" + PRIVATE + "\",\""
                + STANDARD + "\"]},\"expiresAt\":\"" + T.plus(Duration.ofDays(30)) + "\",\"privateAccess\":true}"));
        SseTestClient stream = open("/v1/k/" + key.key() + "/changes/stream", null);

        ingest(PRIVATE, PRIVATE_KIND, "secret-feed");
        assertThat(stream.next("change", WAIT)).as("the granted feed key gets the private change").isNotNull();
        assertThat(mentions(stream.received(), PRIVATE)).isTrue();

        feedKeys.revoke(key.entity().getId());

        Event reset = stream.next("reset", WAIT);
        assertThat(reset).isNotNull();
        assertThat(JSON.readTree(reset.data()).get("reason").asString()).isEqualTo("revoked");
        assertThat(stream.awaitEnd(WAIT)).isTrue();
    }
}
