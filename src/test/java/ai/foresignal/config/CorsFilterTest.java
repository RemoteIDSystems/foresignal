package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.CorsSettings;
import ai.foresignal.settings.CorsSettingsService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** {@link CorsFilter} without a server: the map-client header set (prompt 050, Weather-Imagery-Design §6.7). */
class CorsFilterTest {

    private static final String ORIGIN = "https://map.example";

    private CorsFilter filter;

    @BeforeEach
    void setUp() {
        CorsSettingsService settings = mock(CorsSettingsService.class);
        when(settings.get()).thenReturn(new CorsSettings(List.of("*"), List.of()));
        filter = new CorsFilter(settings);
    }

    private MockHttpServletResponse preflight(String uri, String requestHeaders) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", uri);
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
        if (requestHeaders != null) {
            request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, requestHeaders);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertThat(chain.getRequest()).as("a preflight is answered by the filter").isNull();
        return response;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/v1/k/fsk_abcdefghijklmnopqrstuvwxyz/products/mrms/tiles/x/1/2/3.png",
            "/v1/products/mrms-reflectivity/tiles/20261010T143240Z~abcd1234/5/8/12.png"})
    @DisplayName("a preflight to a keyed or canonical raster path allows the map-client headers for a day")
    void preflightHeaders(String uri) throws Exception {
        MockHttpServletResponse response = preflight(uri, "foresignal-prefetch, if-none-match");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo(ORIGIN);
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE)).isEqualTo("86400");
        String allowed = response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
        assertThat(allowed).contains("Authorization", "If-None-Match", "Foresignal-Prefetch", "Last-Event-ID");
        String exposed = response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
        assertThat(exposed).contains("Foresignal-Frame-Time", "Foresignal-Image-Bounds", "Foresignal-Frame-Expired",
                "Retry-After", "Link", "ETag", "Location", "Mcp-Session-Id");
    }

    @Test
    @DisplayName("an actual keyed read exposes the raster headers but sends no preflight-only headers")
    void actualRead() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/k/fsk_abcdefghijklmnop/styles");
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)).isEqualTo(CorsFilter.EXPOSED_HEADERS);
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE)).isNull();
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)).isNull();
    }

    @Test
    @DisplayName("a request without Origin passes through untouched")
    void sameOrigin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }
}
