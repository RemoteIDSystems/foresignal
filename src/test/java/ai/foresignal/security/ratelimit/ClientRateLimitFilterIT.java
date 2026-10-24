package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.StreamTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-client rate limits over HTTP (prompt 035), with the application clock frozen by
 * {@link MutableClock} so buckets never refill behind the test's back: per-class limits and
 * {@code Retry-After}, the anonymous class, admin exemptions, a token's {@code maxRequestsPerMinute},
 * the concurrent SSE cap and SSE opens per minute, keying by the client IP that the trusted proxy
 * forwarded, and the metrics limit. {@link ClientRateLimitUntrustedProxyIT} is the spoofed
 * {@code X-Forwarded-For} counterpart.
 *
 * <p>Requests go to {@code 127.0.0.1} explicitly, so the anonymous client key is
 * {@code ip:127.0.0.1} rather than the IPv6 loopback.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
@TestPropertySource(properties = "foresignal.ratelimit.enabled=true")
class ClientRateLimitFilterIT {

    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    static final Duration TIMEOUT = Duration.ofSeconds(10);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String LOOPBACK_KEY = "ip:127.0.0.1";

    @LocalServerPort int port;
    @Autowired RateLimitSettingsService rateLimits;
    @Autowired ClientRateLimiter limiter;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired SseHub hub;
    @Autowired MeterRegistry meters;

    final List<SseTestClient> streams = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        limiter.reset();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        rateLimits.replace(RateLimitSettings.defaults());
        limiter.reset();
    }

    /** Limits for one test: everything generous except what the test sets. */
    void limits(int read, int tiles, int opens, int concurrent, int expensive, int write, int anonymous,
            Integer metrics) {
        rateLimits.replace(new RateLimitSettings(read, tiles, new RateLimitSettings.StreamRates(opens, concurrent),
                expensive, write, anonymous, metrics));
    }

    String base() {
        return "http://127.0.0.1:" + port;
    }

    HttpResponse<String> send(String method, String path, Map<String, String> headers, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).timeout(TIMEOUT);
        headers.forEach(request::header);
        request.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> get(String path, Map<String, String> headers) throws Exception {
        return send("GET", path, headers, null);
    }

    HttpResponse<String> get(String path) throws Exception {
        return get(path, Map.of());
    }

    static Map<String, String> bearer(TokenService.CreatedToken token) {
        return Map.of("Authorization", TestTokens.bearer(token));
    }

    static Map<String, String> forwardedFor(String ip) {
        return Map.of("X-Forwarded-For", ip);
    }

    static void assertLimited(HttpResponse<String> response, String rateClass, long retryAfter) {
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(response.headers().firstValue("Retry-After")).contains(Long.toString(retryAfter));
        JsonNode problem = JSON.readTree(response.body());
        assertThat(problem.get("code").asString()).isEqualTo("RATE_LIMITED");
        assertThat(problem.get("class").asString()).isEqualTo(rateClass);
    }

    SseTestClient open(String path, Map<String, String> headers) {
        SseTestClient stream = SseTestClient.connect(base(), path, headers);
        streams.add(stream);
        return stream;
    }

    // ------------------------------------------------------------------------- per class

    @Test
    @DisplayName("read: n per minute pass, the next is 429 with Retry-After 60/n s, and the bucket refills on the clock")
    void readLimit() throws Exception {
        limits(3, 100, 100, 8, 100, 100, 100, null);
        for (int i = 0; i < 3; i++) {
            assertThat(get("/v1/records").statusCode()).isEqualTo(200);
        }

        assertLimited(get("/v1/records"), "read", 20);
        clock.advanceSeconds(20);
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("tiles: TileJSON and tiles have their own bucket")
    void tilesLimit() throws Exception {
        limits(100, 2, 100, 8, 100, 100, 100, null);
        assertThat(get("/v1/tiles.json").statusCode()).isNotEqualTo(429);
        assertThat(get("/v1/tiles.json").statusCode()).isNotEqualTo(429);

        assertLimited(get("/v1/tiles.json"), "tiles", 30);
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("expensive: search is charged to expensive, not read")
    void expensiveLimit() throws Exception {
        limits(100, 100, 100, 8, 1, 100, 100, null);
        // Search is always authenticated (SEC-5): an anonymous call is 401 before the limiter, so a read token is used
        // (checkpoint 041).
        Map<String, String> reader = bearer(TestTokens.create(tokens, "rl-search-reader", Scope.READ));
        assertThat(get("/v1/search?q=quake", reader).statusCode()).isNotEqualTo(429);

        assertLimited(get("/v1/search?q=quake", reader), "expensive", 60);
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("write: an admin token's writes are limited (admins are exempt from read and tiles only)")
    void writeLimitAppliesToAdmins() throws Exception {
        limits(100, 100, 100, 8, 100, 2, 100, null);
        TokenService.CreatedToken admin = TestTokens.create(tokens, "admin", Scope.ADMIN);
        Map<String, String> headers = new LinkedHashMap<>(bearer(admin));
        headers.put("Content-Type", "application/json");
        for (int i = 0; i < 2; i++) {
            assertThat(send("PUT", "/v1/settings/security", headers, "{\"openReads\":true}").statusCode())
                    .isEqualTo(200);
        }

        assertLimited(send("PUT", "/v1/settings/security", headers, "{\"openReads\":true}"), "write", 30);
    }

    @Test
    @DisplayName("admin tokens are exempt from read and tiles")
    void adminExemptFromReadAndTiles() throws Exception {
        limits(1, 1, 100, 8, 100, 100, 100, null);
        TokenService.CreatedToken admin = TestTokens.create(tokens, "admin", Scope.ADMIN);
        for (int i = 0; i < 5; i++) {
            assertThat(get("/v1/records", bearer(admin)).statusCode()).isEqualTo(200);
            assertThat(get("/v1/tiles.json", bearer(admin)).statusCode()).isNotEqualTo(429);
        }

        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
        assertLimited(get("/v1/records"), "read", 60);
    }

    @Test
    @DisplayName("tokens have their own buckets, apart from the anonymous client on the same IP")
    void tokenKeyedApartFromIp() throws Exception {
        limits(1, 100, 100, 8, 100, 100, 100, null);
        TokenService.CreatedToken reader = TestTokens.create(tokens, "reader", Scope.READ);
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
        assertThat(get("/v1/records").statusCode()).isEqualTo(429);

        assertThat(get("/v1/records", bearer(reader)).statusCode()).isEqualTo(200);
        assertLimited(get("/v1/records", bearer(reader)), "read", 60);
    }

    @Test
    @DisplayName("a token's maxRequestsPerMinute overrides read")
    void tokenOverride() throws Exception {
        limits(100, 100, 100, 8, 100, 100, 100, null);
        TokenService.CreatedToken kiosk = TestTokens.create(tokens, "kiosk", Scope.READ,
                new TokenRestrictions(null, null, null, null, null, null, 2), null);
        for (int i = 0; i < 2; i++) {
            assertThat(get("/v1/records", bearer(kiosk)).statusCode()).isEqualTo(200);
        }

        assertLimited(get("/v1/records", bearer(kiosk)), "read", 30);
    }

    @Test
    @DisplayName("anonymous callers are also limited per IP by the anonymous class")
    void anonymousClass() throws Exception {
        limits(100, 100, 100, 8, 100, 100, 2, null);
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);
        // Tiles are limited by their own class only, never charged to anonymous (a map view would use it up).
        for (int i = 0; i < 3; i++) {
            assertThat(get("/v1/tiles.json").statusCode()).isNotEqualTo(429);
        }
        assertThat(get("/v1/records").statusCode()).isEqualTo(200);

        assertLimited(get("/v1/records"), "anonymous", 30);
    }

    // ------------------------------------------------------------------------- streams

    @Test
    @DisplayName("SSE: at most `concurrent` open streams per client; closing one frees its slot")
    void concurrentStreams() {
        limits(100, 100, 100, 2, 100, 100, 100, null);
        assertThat(open("/v1/changes/stream", Map.of()).status()).isEqualTo(200);
        assertThat(open("/v1/changes/stream", Map.of()).status()).isEqualTo(200);
        assertThat(limiter.openStreams(LOOPBACK_KEY)).isEqualTo(2);

        SseTestClient third = open("/v1/changes/stream", Map.of());
        assertThat(third.status()).isEqualTo(429);
        assertThat(third.header("Retry-After")).isEqualTo("5");

        hub.clients().forEach(SseClient::close);
        await().atMost(Duration.ofSeconds(10)).until(() -> limiter.openStreams(LOOPBACK_KEY) == 0);
        assertThat(open("/v1/changes/stream", Map.of()).status()).isEqualTo(200);
    }

    @Test
    @DisplayName("SSE: opens per minute are limited apart from the concurrent cap")
    void streamOpensPerMinute() {
        limits(100, 100, 2, 8, 100, 100, 100, null);
        assertThat(open("/v1/changes/stream", Map.of()).status()).isEqualTo(200);
        assertThat(open("/v1/changes/stream", Map.of()).status()).isEqualTo(200);

        SseTestClient third = open("/v1/changes/stream", Map.of());
        assertThat(third.status()).isEqualTo(429);
        assertThat(third.header("Retry-After")).isEqualTo("30");
    }

    // ------------------------------------------------------------------------- client IP

    @Test
    @DisplayName("behind the trusted proxy, each forwarded client IP has its own bucket")
    void trustedProxyKeying() throws Exception {
        limits(1, 100, 100, 8, 100, 100, 100, null);
        assertThat(get("/v1/records", forwardedFor("203.0.113.1")).statusCode()).isEqualTo(200);
        assertLimited(get("/v1/records", forwardedFor("203.0.113.1")), "read", 60);

        assertThat(get("/v1/records", forwardedFor("203.0.113.2")).statusCode()).isEqualTo(200);
        assertThat(limiter.trackedBuckets()).isPositive();
    }

    // ------------------------------------------------------------------------- metrics

    @Test
    @DisplayName("prometheus is limited per IP only when metricsPerMinute is set")
    void metricsLimit() throws Exception {
        limits(100, 100, 100, 8, 100, 100, 100, null);
        for (int i = 0; i < 3; i++) {
            assertThat(get("/actuator/prometheus").statusCode()).isNotEqualTo(429);
        }
        limits(100, 100, 100, 8, 100, 100, 100, 1);
        assertThat(get("/actuator/prometheus").statusCode()).isNotEqualTo(429);

        assertLimited(get("/actuator/prometheus"), "metrics", 60);
    }

    @Test
    @DisplayName("refusals are counted in foresignal.ratelimit.rejected{class}; preflights are never limited")
    void rejectedMetricAndPreflights() throws Exception {
        limits(1, 100, 100, 8, 100, 100, 100, null);
        double before = meters.get(ClientRateLimiter.REJECTED_METER).tag("class", "read").counter().count();
        get("/v1/records");
        get("/v1/records");
        get("/v1/records");

        assertThat(meters.get(ClientRateLimiter.REJECTED_METER).tag("class", "read").counter().count())
                .isEqualTo(before + 2);
        for (int i = 0; i < 3; i++) {
            HttpResponse<String> preflight = send("OPTIONS", "/v1/records",
                    Map.of("Origin", "http://example.invalid", "Access-Control-Request-Method", "GET"), null);
            assertThat(preflight.statusCode()).isNotEqualTo(429);
        }
    }
}
