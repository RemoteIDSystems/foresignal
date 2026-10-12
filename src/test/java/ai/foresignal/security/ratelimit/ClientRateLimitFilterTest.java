package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link ClientRateLimitFilter} without a server (prompt 035): classification, canonical paths,
 * caller resolution, exemptions, overrides, the 429 document and stream slots held across async.
 * {@code ClientRateLimitFilterIT} covers the same rules over HTTP.
 */
class ClientRateLimitFilterTest {

    /**
     * Serializes problems as the application mapper does: Spring's mixin flattens {@code ProblemDetail}'s properties
     * (code, class) into the document (checkpoint 041; a plain mapper nested them under "properties").
     */
    static final JsonMapper JSON = JsonMapper.builder()
            .addMixIn(org.springframework.http.ProblemDetail.class,
                    org.springframework.http.converter.json.ProblemDetailJacksonMixin.class)
            .build();

    MutableClock clock;
    RateLimitSettingsService settings;
    ClientRateLimiter limiter;
    ClientRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        settings = mock(RateLimitSettingsService.class);
        use(new RateLimitSettings(2, 2, new RateLimitSettings.StreamRates(10, 1), 1, 2, 100, null));
        limiter = new ClientRateLimiter(settings, RateLimitProperties.defaults(), clock, new SimpleMeterRegistry());
        filter = new ClientRateLimitFilter(limiter, new ProblemWriter(JSON));
    }

    void use(RateLimitSettings value) {
        when(settings.get()).thenReturn(value);
    }

    static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("192.0.2.10");
        request.setAsyncSupported(true);
        return request;
    }

    MockHttpServletResponse send(HttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    static void asToken(MockHttpServletRequest request, Scope scope, Integer maxRequestsPerMinute) {
        UUID id = UUID.nameUUIDFromBytes(scope.name().getBytes());
        new RequestPrincipal(RequestPrincipal.Kind.TOKEN, id, "t", scope,
                new ReadRestriction(null, null, null, null, null, false, maxRequestsPerMinute)).bind(request);
    }

    @ParameterizedTest(name = "{0} {1} -> {2}")
    @CsvSource({
            "GET, /v1/records, READ",
            "HEAD, /v1/records, READ",
            "GET, /v1/search/status, READ",
            "GET, /v1/tiles.json, TILES",
            "GET, /v1/tiles/quakes.json, TILES",
            "GET, /v1/tiles/quakes/3/1/2.mvt, TILES",
            "GET, /v1/views/abc/tiles/3/1/2.mvt, TILES",
            "GET, /v1/views/abc/tiles.json, TILES",
            "GET, /v1/ogc/collections/c/map/tiles, TILES",
            "GET, /v1/changes/stream, STREAM",
            "GET, /v1/alerts/stream, STREAM",
            "GET, /v1/notifications/stream, STREAM",
            "GET, /v1/events/stream, STREAM",
            "GET, /mcp/sse, STREAM",
            "GET, /v1/search, EXPENSIVE",
            "POST, /v1/ask, EXPENSIVE",
            "POST, /v1/ask/stream, EXPENSIVE",
            "GET, /v1/sample, EXPENSIVE",
            "POST, /v1/sample/track, EXPENSIVE",
            "GET, /v1/ask/threads, READ",
            "POST, /v1/records/x, WRITE",
            "PATCH, /v1/events/stream/s1/filter, WRITE",
            "DELETE, /v1/tiles/quakes.json, WRITE",
            "POST, /mcp, WRITE"
    })
    void classify(String method, String path, RateClass expected) {
        assertThat(ClientRateLimitFilter.classify(method, path)).isEqualTo(expected);
    }

    @Test
    @DisplayName("canonical path: trailing slash dropped, query ignored, /v1/k/{key} folded to /v1")
    void canonicalPath() {
        assertThat(ClientRateLimitFilter.canonicalPath(request("GET", "/v1/records/"))).isEqualTo("/v1/records");
        assertThat(ClientRateLimitFilter.canonicalPath(request("GET", "/v1/k/fsf_secret/changes/stream")))
                .isEqualTo("/v1/changes/stream");
        assertThat(ClientRateLimitFilter.canonicalPath(request("GET", "/v1/k/fsf_secret"))).isEqualTo("/v1");
        MockHttpServletRequest servlet = request("GET", "/v1/searc%68");
        servlet.setServletPath("/v1/search");
        assertThat(ClientRateLimitFilter.canonicalPath(servlet)).isEqualTo("/v1/search");
    }

    @Test
    @DisplayName("callers: anonymous by IP, token by id with its override, admin flagged, ingest by principal")
    void callers() {
        MockHttpServletRequest anonymous = request("GET", "/v1/records");
        assertThat(ClientRateLimitFilter.caller(anonymous, "ip:a")).isEqualTo(
                new ClientRateLimitFilter.Caller("ip:a", true, false, null, null));

        MockHttpServletRequest profile = request("GET", "/v1/records");
        RequestPrincipal.anonymous(new ReadRestriction(null, null, null, null, null, true, 30)).bind(profile);
        assertThat(ClientRateLimitFilter.caller(profile, "ip:a").anonymousOverride()).isEqualTo(30);

        MockHttpServletRequest token = request("GET", "/v1/records");
        asToken(token, Scope.READ, 7);
        ClientRateLimitFilter.Caller t = ClientRateLimitFilter.caller(token, "ip:a");
        assertThat(t.key()).startsWith("token:");
        assertThat(t.readOverride()).isEqualTo(7);
        assertThat(t.admin()).isFalse();

        MockHttpServletRequest current = request("GET", "/v1/records");
        UUID id = UUID.randomUUID();
        current.setAttribute(CurrentToken.ATTRIBUTE, new CurrentToken(new AuthenticatedToken(id, "boot", Scope.ADMIN,
                TokenRestrictions.NONE, null, "p", true), false));
        assertThat(ClientRateLimitFilter.caller(current, "ip:a"))
                .isEqualTo(new ClientRateLimitFilter.Caller("token:" + id, false, true, null, null));

        MockHttpServletRequest feed = request("GET", "/v1/records");
        new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, id, "f", Scope.READ, null).bind(feed);
        assertThat(ClientRateLimitFilter.caller(feed, "ip:a").key()).isEqualTo("feed:" + id);

        MockHttpServletRequest ingest = request("POST", "/v1/ingest/s/json");
        ingest.setAttribute(IngestCaller.ATTRIBUTE, new IngestCaller(IngestCaller.INGEST_KEY, "s", "ingest-key:abcd1234",
                "abcd1234"));
        assertThat(ClientRateLimitFilter.caller(ingest, "ip:a"))
                .isEqualTo(new ClientRateLimitFilter.Caller("ingest:ingest-key:abcd1234", false, false, null, null));
    }

    @Test
    @DisplayName("over the limit: 429 RATE_LIMITED with Retry-After and the class")
    void refusal() throws Exception {
        send(request("GET", "/v1/records"));
        send(request("GET", "/v1/records"));
        MockHttpServletResponse refused = send(request("GET", "/v1/records"));

        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(refused.getHeader("Retry-After")).isEqualTo("30");
        JsonNode body = JSON.readTree(refused.getContentAsString());
        assertThat(body.get("code").asString()).isEqualTo("RATE_LIMITED");
        assertThat(body.get("class").asString()).isEqualTo("read");
        assertThat(refused.getContentType()).startsWith("application/problem+json");
    }

    @Test
    @DisplayName("an anonymous caller is also charged to the anonymous class, the stricter of setting and profile")
    void anonymousClass() throws Exception {
        use(new RateLimitSettings(100, 0, null, 0, 0, 100, null));
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest r = request("GET", "/v1/records");
            RequestPrincipal.anonymous(new ReadRestriction(null, null, null, null, null, true, 2)).bind(r);
            assertThat(send(r).getStatus()).isEqualTo(200);
        }
        MockHttpServletRequest r = request("GET", "/v1/records");
        RequestPrincipal.anonymous(new ReadRestriction(null, null, null, null, null, true, 2)).bind(r);
        MockHttpServletResponse refused = send(r);

        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(JSON.readTree(refused.getContentAsString()).get("class").asString()).isEqualTo("anonymous");
    }

    @Test
    @DisplayName("admin tokens are exempt from read and tiles, not from write or expensive")
    void adminExemptions() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest read = request("GET", "/v1/records");
            asToken(read, Scope.ADMIN, null);
            assertThat(send(read).getStatus()).isEqualTo(200);
            MockHttpServletRequest tiles = request("GET", "/v1/tiles.json");
            asToken(tiles, Scope.ADMIN, null);
            assertThat(send(tiles).getStatus()).isEqualTo(200);
        }
        MockHttpServletRequest search = request("GET", "/v1/search");
        asToken(search, Scope.ADMIN, null);
        assertThat(send(search).getStatus()).isEqualTo(200);
        MockHttpServletRequest again = request("GET", "/v1/search");
        asToken(again, Scope.ADMIN, null);
        assertThat(send(again).getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("a token's maxRequestsPerMinute overrides read, upwards too")
    void tokenOverride() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest r = request("GET", "/v1/records");
            asToken(r, Scope.READ, 5);
            assertThat(send(r).getStatus()).isEqualTo(200);
        }
        MockHttpServletRequest r = request("GET", "/v1/records");
        asToken(r, Scope.READ, 5);
        MockHttpServletResponse refused = send(r);
        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(refused.getHeader("Retry-After")).isEqualTo("12");
    }

    @Test
    @DisplayName("a stream slot is held while the async response is open and released on completion")
    void streamSlotAcrossAsync() throws Exception {
        MockHttpServletRequest open = request("GET", "/v1/changes/stream");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(open, response, (req, res) -> req.startAsync());
        assertThat(limiter.openStreams("ip:192.0.2.10")).isEqualTo(1);

        MockHttpServletResponse second = send(request("GET", "/v1/changes/stream"));
        assertThat(second.getStatus()).isEqualTo(429);
        assertThat(second.getHeader("Retry-After")).isEqualTo("5");

        ((MockAsyncContext) open.getAsyncContext()).complete();
        assertThat(limiter.openStreams("ip:192.0.2.10")).isZero();
    }

    @Test
    @DisplayName("a stream that did not go async releases its slot at once")
    void streamWithoutAsync() throws Exception {
        assertThat(send(request("GET", "/v1/changes/stream")).getStatus()).isEqualTo(200);
        assertThat(limiter.openStreams("ip:192.0.2.10")).isZero();
    }

    @Test
    @DisplayName("prometheus is limited per IP only when metricsPerMinute is set")
    void metrics() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(send(request("GET", "/actuator/prometheus")).getStatus()).isEqualTo(200);
        }
        use(new RateLimitSettings(2, 2, null, 1, 2, 100, 1));
        assertThat(send(request("GET", "/actuator/prometheus")).getStatus()).isEqualTo(200);
        assertThat(send(request("GET", "/actuator/prometheus")).getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("preflights, other actuator paths and non-API paths are never limited")
    void skipped() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(send(request("OPTIONS", "/v1/records")).getStatus()).isEqualTo(200);
            assertThat(send(request("GET", "/actuator/health")).getStatus()).isEqualTo(200);
            assertThat(send(request("GET", "/index.html")).getStatus()).isEqualTo(200);
        }
        assertThat(limiter.trackedBuckets()).isZero();
    }

    @Test
    @DisplayName("disabled: nothing is limited")
    void disabled() throws Exception {
        ClientRateLimiter off = new ClientRateLimiter(settings, new RateLimitProperties(false, false, 10, 10), clock,
                new SimpleMeterRegistry());
        ClientRateLimitFilter passThrough = new ClientRateLimitFilter(off, new ProblemWriter(JSON));
        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            passThrough.doFilter(request("GET", "/v1/records"), response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
