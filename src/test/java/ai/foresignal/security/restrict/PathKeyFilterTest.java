package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.security.Scope;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.security.ratelimit.RateLimitProperties;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/** {@link PathKeyFilter} without a server: resolution, method, prefixes, hygiene, backoff (prompt 035). */
class PathKeyFilterTest {

    private static final String VALID = "fsf_valid";
    private static final String GONE = "fsf_gone";
    private static final ReadRestriction RESTRICTION =
            new ReadRestriction(List.of("usgs"), null, null, null, null, true, null);

    private final UUID id = UUID.randomUUID();
    private PathKeyFilter filter;
    private AuthFailureBackoff backoff;

    @BeforeEach
    void setUp() {
        PathKeyResolver resolver = new PathKeyResolver() {
            @Override
            public String prefix() {
                return "fsf_";
            }

            @Override
            public Resolution resolve(String key) {
                if (VALID.equals(key)) {
                    return new Valid(new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, id, "kiosk", Scope.READ,
                            RESTRICTION), List.of("/v1/records", "/v1/changes/stream", "/v1/tiles/"));
                }
                return GONE.equals(key) ? new Gone() : new Invalid();
            }
        };
        backoff = new AuthFailureBackoff(new MutableClock(), RateLimitProperties.defaults(), new SimpleMeterRegistry());
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("backoff", backoff);
        filter = new PathKeyFilter(List.of(resolver), new ProblemWriter(JsonMapper.builder().build()),
                beans.getBeanProvider(AuthFailureBackoff.class));
    }

    private record Outcome(MockHttpServletResponse response, HttpServletRequest forwarded) {}

    private Outcome run(String method, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("203.0.113.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return new Outcome(response, (HttpServletRequest) chain.getRequest());
    }

    @Test
    @DisplayName("a valid key on an allowed prefix is forwarded to the canonical path with its principal")
    void forwards() throws Exception {
        Outcome out = run("GET", "/v1/k/" + VALID + "/records");
        assertThat(out.forwarded()).isNotNull();
        assertThat(out.forwarded().getRequestURI()).isEqualTo("/v1/records");
        assertThat(out.forwarded().getServletPath()).isEqualTo("/v1/records");
        RequestPrincipal principal = RequestPrincipal.of(out.forwarded()).orElseThrow();
        assertThat(principal.isFeedKey()).isTrue();
        assertThat(principal.restriction()).isEqualTo(RESTRICTION);
        assertThat(out.response().getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(out.response().getHeader("X-Robots-Tag")).isEqualTo("noindex");
        assertThat(out.response().getHeader("Cache-Control")).isEqualTo("private");
        assertThat(out.forwarded().getAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE)).isEqualTo("/v1/k/…/records");
    }

    @Test
    @DisplayName("HEAD, streams and tile paths under a trailing-slash prefix are forwarded")
    void forwardsOtherAllowedShapes() throws Exception {
        assertThat(run("HEAD", "/v1/k/" + VALID + "/records/usgs/abc").forwarded().getRequestURI())
                .isEqualTo("/v1/records/usgs/abc");
        assertThat(run("GET", "/v1/k/" + VALID + "/changes/stream").forwarded().getRequestURI())
                .isEqualTo("/v1/changes/stream");
        assertThat(run("GET", "/v1/k/" + VALID + "/tiles/usgs/3/4/2.mvt").forwarded().getRequestURI())
                .isEqualTo("/v1/tiles/usgs/3/4/2.mvt");
    }

    @Test
    @DisplayName("a path outside the prefixes is 403 RESTRICTED and nothing is forwarded")
    void wrongPrefix() throws Exception {
        Outcome out = run("GET", "/v1/k/" + VALID + "/alerts");
        assertThat(out.response().getStatus()).isEqualTo(403);
        assertThat(out.response().getContentAsString()).contains("RESTRICTED").doesNotContain(VALID);
        assertThat(out.forwarded()).isNull();
        assertThat(run("GET", "/v1/k/" + VALID + "/recordsx").response().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("POST is 405 with Allow: GET, HEAD")
    void postRejected() throws Exception {
        Outcome out = run("POST", "/v1/k/" + VALID + "/records");
        assertThat(out.response().getStatus()).isEqualTo(405);
        assertThat(out.response().getHeader("Allow")).isEqualTo("GET, HEAD");
        assertThat(out.forwarded()).isNull();
    }

    @Test
    @DisplayName("an unknown key is 401 INVALID_FEED_KEY; an expired or revoked one is 410")
    void invalidAndGone() throws Exception {
        Outcome invalid = run("GET", "/v1/k/fsf_nope/records");
        assertThat(invalid.response().getStatus()).isEqualTo(401);
        assertThat(invalid.response().getContentAsString()).contains("INVALID_FEED_KEY").doesNotContain("fsf_nope");
        assertThat(run("GET", "/v1/k/xyz_other/records").response().getStatus()).isEqualTo(401);
        Outcome gone = run("GET", "/v1/k/" + GONE + "/records");
        assertThat(gone.response().getStatus()).isEqualTo(410);
        assertThat(gone.response().getContentAsString()).contains("FEED_KEY_GONE");
    }

    @Test
    @DisplayName("dot segments, empty segments and encoded separators are 400")
    void unsafePaths() throws Exception {
        for (String rest : List.of("/records/../tokens", "/records//x", "/records/%2e%2e/tokens", "/records%2Fx", "")) {
            assertThat(run("GET", "/v1/k/" + VALID + rest).response().getStatus()).as(rest).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("repeated bad keys back off with 429 and Retry-After; a valid key still works")
    void backoff() throws Exception {
        for (int i = 0; i < AuthFailureBackoff.THRESHOLD; i++) {
            assertThat(run("GET", "/v1/k/fsf_bad" + i + "/records").response().getStatus()).isEqualTo(401);
        }
        MockHttpServletResponse limited = run("GET", "/v1/k/fsf_bad/records").response();
        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(limited.getHeader("Retry-After")).isNotBlank();
        assertThat(run("GET", "/v1/k/" + VALID + "/records").forwarded()).isNotNull();
        assertThat(run("GET", "/v1/k/fsf_bad/records").response().getStatus()).as("success reset the address")
                .isEqualTo(401);
    }

    @Test
    @DisplayName("other paths pass untouched, with the log URI attribute set")
    void otherPaths() throws Exception {
        Outcome out = run("GET", "/v1/records");
        assertThat(out.forwarded().getRequestURI()).isEqualTo("/v1/records");
        assertThat(out.forwarded().getAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE)).isEqualTo("/v1/records");
        assertThat(out.response().getHeader("Referrer-Policy")).isNull();
    }

    @Test
    @DisplayName("helpers: redaction, prefix boundaries and path safety")
    void helpers() {
        assertThat(PathKeyFilter.redact("/v1/k/fsf_abc/records?x=1")).isEqualTo("/v1/k/…/records?x=1");
        assertThat(PathKeyFilter.redact("/v1/records")).isEqualTo("/v1/records");
        assertThat(PathKeyFilter.redact(null)).isNull();
        assertThat(PathKeyFilter.allowed("/v1/records", List.of("/v1/records"))).isTrue();
        assertThat(PathKeyFilter.allowed("/v1/records/a/b", List.of("/v1/records"))).isTrue();
        assertThat(PathKeyFilter.allowed("/v1/tiles/usgs.json", List.of("/v1/tiles/usgs"))).isTrue();
        assertThat(PathKeyFilter.allowed("/v1/recordsx", List.of("/v1/records"))).isFalse();
        assertThat(PathKeyFilter.allowed("/v1/tiles/x", List.of("/v1/tiles/"))).isTrue();
        assertThat(PathKeyFilter.safe("/records")).isTrue();
        assertThat(PathKeyFilter.safe("/records/./x")).isFalse();
        assertThat(PathKeyFilter.safe("/records;jsessionid=1")).isFalse();
    }
}
