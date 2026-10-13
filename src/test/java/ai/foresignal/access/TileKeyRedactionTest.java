package ai.foresignal.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.config.RedactingErrorAttributes;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.security.ratelimit.RateLimitProperties;
import ai.foresignal.security.restrict.PathKeyFilter;
import ai.foresignal.security.restrict.PathKeyResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import tools.jackson.databind.json.JsonMapper;

/**
 * Leak hygiene of the tile-key carrier without a server (prompt 050): the access-log URI keeps only the
 * key's first eight characters, problem bodies never contain the key, every {@code /v1/k/**} response
 * carries {@code Referrer-Policy: no-referrer}, tile keys get their own refusal codes, and Spring
 * Boot's error attributes redact the key from {@code path}. The real {@link TileKeyService} runs over a
 * stubbed {@link FeedKeyService} lookup.
 */
class TileKeyRedactionTest {

    private static final String VALID = "fsk_AbCdEfGhIjKlMnOpQrStUvWxYz0123456789abcdefg";
    private static final String GONE = "fsk_ZyXwVuTsRqPoNmLkJiHgFeDcBa9876543210zyxwvut";
    private static final String UNKNOWN = "fsk_unknownunknownunknownunknownunknownunknown";

    private final UUID id = UUID.randomUUID();
    private PathKeyFilter filter;
    private AuthFailureBackoff backoff;

    @BeforeEach
    void setUp() {
        FeedKeyService feedKeys = mock(FeedKeyService.class);
        when(feedKeys.resolveOfType(anyString(), any(), any(), any())).thenReturn(new PathKeyResolver.Invalid());
        RequestPrincipal principal = new RequestPrincipal(RequestPrincipal.Kind.TILE_KEY, id, "tablet", Scope.READ,
                ReadRestriction.NONE.withExcludePrivate(true), false);
        when(feedKeys.resolveOfType(eq(VALID), eq("fsk_"), eq("tile_key"), any()))
                .thenReturn(new PathKeyResolver.Valid(principal, List.of("/v1/products/", "/v1/changes/stream")));
        when(feedKeys.resolveOfType(eq(GONE), eq("fsk_"), eq("tile_key"), any())).thenReturn(new PathKeyResolver.Gone());
        TileKeyService tileKeys = new TileKeyService(feedKeys, RestrictionValidator.structural(), new MutableClock());
        backoff = new AuthFailureBackoff(new MutableClock(), RateLimitProperties.defaults(), new SimpleMeterRegistry());
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("backoff", backoff);
        filter = new PathKeyFilter(List.of(tileKeys), new ProblemWriter(JsonMapper.builder().build()),
                beans.getBeanProvider(AuthFailureBackoff.class));
    }

    private record Outcome(MockHttpServletRequest request, MockHttpServletResponse response,
            HttpServletRequest forwarded) {}

    private Outcome run(String method, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("198.51.100.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return new Outcome(request, response, (HttpServletRequest) chain.getRequest());
    }

    @Test
    @DisplayName("a forwarded request logs only the first eight characters of the key and carries no-referrer")
    void forwardedRequestIsRedacted() throws Exception {
        Outcome out = run("GET", "/v1/k/" + VALID + "/products/mrms/tiles/latest/1/0/0.png");
        assertThat(out.forwarded()).isNotNull();
        assertThat(out.forwarded().getRequestURI()).isEqualTo("/v1/products/mrms/tiles/latest/1/0/0.png");
        assertThat(RequestPrincipal.of(out.forwarded()).orElseThrow().isTileKey()).isTrue();
        Object logged = out.request().getAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE);
        assertThat(logged).isEqualTo("/v1/k/fsk_AbCd…/products/mrms/tiles/latest/1/0/0.png");
        assertThat(String.valueOf(logged)).doesNotContain(VALID);
        assertThat(out.response().getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(out.response().getHeader("X-Robots-Tag")).isEqualTo("noindex");
    }

    @Test
    @DisplayName("refusals use the tile-key codes, carry no-referrer and never echo the key")
    void refusals() throws Exception {
        Outcome unknown = run("GET", "/v1/k/" + UNKNOWN + "/products/x");
        assertThat(unknown.response().getStatus()).isEqualTo(401);
        assertThat(unknown.response().getContentAsString()).contains("INVALID_TILE_KEY").doesNotContain(UNKNOWN)
                .doesNotContain("fsk_unkn");
        assertThat(unknown.response().getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(unknown.request().getAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE)).isEqualTo("/v1/k/fsk_unkn…/products/x");

        Outcome gone = run("GET", "/v1/k/" + GONE + "/products/x");
        assertThat(gone.response().getStatus()).as("expired and revoked look like unknown").isEqualTo(401);
        assertThat(gone.response().getContentAsString()).contains("INVALID_TILE_KEY").doesNotContain(GONE);

        Outcome outside = run("GET", "/v1/k/" + VALID + "/records");
        assertThat(outside.response().getStatus()).isEqualTo(403);
        assertThat(outside.response().getContentAsString()).contains("INSUFFICIENT_SCOPE").doesNotContain(VALID);
        assertThat(outside.forwarded()).isNull();

        Outcome patch = run("PATCH", "/v1/k/" + VALID + "/changes/stream");
        assertThat(patch.response().getStatus()).isEqualTo(405);
        assertThat(patch.response().getContentAsString()).doesNotContain(VALID);
    }

    @Test
    @DisplayName("a bad tile key counts against the auth-failure backoff; a valid one resets the address")
    void backoffCounts() throws Exception {
        run("GET", "/v1/k/" + UNKNOWN + "/products/x");
        assertThat(backoff.trackedAddresses()).isEqualTo(1);
        run("GET", "/v1/k/" + VALID + "/products/x");
        assertThat(backoff.trackedAddresses()).isZero();
        for (int i = 0; i < AuthFailureBackoff.THRESHOLD; i++) {
            assertThat(run("GET", "/v1/k/" + GONE + "/products/x").response().getStatus()).isEqualTo(401);
        }
        assertThat(run("GET", "/v1/k/" + GONE + "/products/x").response().getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("redact keeps eight characters of a real key and nothing of a short one")
    void redactHelper() {
        assertThat(PathKeyFilter.redact("/v1/k/" + VALID + "/styles")).isEqualTo("/v1/k/fsk_AbCd…/styles");
        assertThat(PathKeyFilter.redact("/v1/k/" + VALID)).isEqualTo("/v1/k/fsk_AbCd…");
        assertThat(PathKeyFilter.redact("/v1/k/fsk_short/styles")).isEqualTo("/v1/k/…/styles");
        assertThat(PathKeyFilter.keyHint(VALID)).isEqualTo("fsk_AbCd…");
        assertThat(PathKeyFilter.keyHint(null)).isEqualTo("…");
    }

    @Test
    @DisplayName("Spring Boot's error attributes report a redacted path")
    void errorAttributes() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/v1/k/" + VALID + "/products/x");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
        Map<String, Object> attributes = new RedactingErrorAttributes()
                .getErrorAttributes(new ServletWebRequest(request), ErrorAttributeOptions.defaults());
        assertThat(attributes.get("path")).isEqualTo("/v1/k/fsk_AbCd…/products/x");
        assertThat(attributes.toString()).doesNotContain(VALID);
    }
}
