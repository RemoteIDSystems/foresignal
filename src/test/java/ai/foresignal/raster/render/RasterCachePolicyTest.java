package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.RasterTilesController;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.GridStore;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/**
 * The one raster cache rule of prompt 045: the immutable/short matrix, {@code public} versus
 * {@code private}, and a 304 answered before anything renders (the renderer is never called).
 */
class RasterCachePolicyTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");

    @ParameterizedTest(name = "contentAddressed={0} public={1} → {2}")
    @CsvSource({
        "true,  true,  'public, max-age=31536000, immutable'",
        "true,  false, 'private, max-age=31536000, immutable'",
        "false, true,  'public, max-age=60'",
        "false, false, 'private, max-age=60'"
    })
    @DisplayName("content-addressed URLs are immutable and nothing else is; public only when openReads covers it")
    void matrix(boolean contentAddressed, boolean publicCache, String expected) {
        ProductSpec spec = RenderFixtures.reflectivity();
        assertThat(RasterCachePolicy.decide(contentAddressed, publicCache, spec).cacheControl()).isEqualTo(expected);
    }

    @Test
    @DisplayName("the short lifetime is min(cadence / 2, 60 s); Foresignal-Frame-Time only on short responses")
    void shortLifetime() {
        ProductSpec fast = ProductSpec.builder("f").temporal(ProductSpec.Temporal.observation(
                java.time.Duration.ofSeconds(30), null)).build();
        assertThat(RasterCachePolicy.decide(false, true, fast).cacheControl()).isEqualTo("public, max-age=15");
        assertThat(RasterCachePolicy.decide(false, false, null).cacheControl()).isEqualTo("private, max-age=60");
        assertThat(RasterCachePolicy.headers(RasterCachePolicy.decide(false, true, fast), "\"e\"", T)
                .getFirst(RasterCachePolicy.FRAME_TIME_HEADER)).isEqualTo(T.toString());
        assertThat(RasterCachePolicy.headers(RasterCachePolicy.decide(true, true, fast), "\"e\"", T)
                .getFirst(RasterCachePolicy.FRAME_TIME_HEADER)).isNull();
        assertThat(RasterCachePolicy.headers(RasterCachePolicy.decide(true, true, fast), "\"e\"", T).getETag())
                .isEqualTo("\"e\"");
    }

    @Test
    @DisplayName("ETags are strong and stable; If-None-Match is checked with WebRequest.checkNotModified")
    void etags() {
        String etag = RasterCachePolicy.etag("p", "20261010T140000Z~1a2b3c4d", "nws-reflectivity@1", "3/1/2@1");
        assertThat(etag).startsWith("\"r-").endsWith("\"").doesNotStartWith("W/");
        assertThat(etag).isEqualTo(RasterCachePolicy.etag("p", "20261010T140000Z~1a2b3c4d", "nws-reflectivity@1",
                "3/1/2@1"));
        assertThat(etag).isNotEqualTo(RasterCachePolicy.etag("p", "20261010T140000Z~1a2b3c4d", "nws-reflectivity@1",
                "3/1/2@2"));
        MockHttpServletRequest match = new MockHttpServletRequest("GET", "/v1/products/p/tiles/x/3/1/2.png");
        match.addHeader("If-None-Match", etag);
        assertThat(RasterCachePolicy.notModified(match, etag)).isTrue();
        MockHttpServletRequest other = new MockHttpServletRequest("GET", "/x");
        other.addHeader("If-None-Match", "\"r-0000\"");
        assertThat(RasterCachePolicy.notModified(other, etag)).isFalse();
        assertThat(RasterCachePolicy.notModified(new MockHttpServletRequest("GET", "/x"), etag)).isFalse();
        assertThat(RasterCachePolicy.etagOf(new byte[] {1, 2})).isEqualTo(RasterCachePolicy.etagOf(new byte[] {1, 2}));
    }

    @Test
    @DisplayName("a matching If-None-Match answers 304 before rendering: the renderer is never called")
    void notModifiedBeforeRender() {
        ProductSpec spec = RenderFixtures.reflectivity();
        RasterFrameRow frame = new RasterFrameRow(spec.id(), "20261010T140000Z", T, null, null, null, null, "g",
                "p/f.fsg", 10, "1a2b3c4d" + "0".repeat(56), "u8", 1, Map.of(), null, RasterFrameRow.COMPLETE, false,
                false, T, null);
        ProductCatalogService catalog = mock(ProductCatalogService.class);
        FrameResolver resolver = mock(FrameResolver.class);
        RasterRenderService renderer = mock(RasterRenderService.class);
        ReadCallerResolver callers = mock(ReadCallerResolver.class);
        ProductUrls urls = mock(ProductUrls.class);
        StyleRegistry.Resolved style = new StyleRegistry().resolve("nws-reflectivity@1");
        when(callers.resolve(any())).thenReturn(new ReadCaller(Scope.READ, false, true));
        when(catalog.visible(eq(spec.id()), any())).thenReturn(new ProductRegistry.Registered(spec, "src", false));
        when(renderer.style(spec, "nws-reflectivity@1")).thenReturn(style);
        when(urls.base(any())).thenReturn("http://localhost");
        when(resolver.resolve(eq(spec), eq(frame.frameKey()), any(), eq(false), any(), any()))
                .thenReturn(new FrameResolver.Found(frame, true));
        var controller = new RasterTilesController(catalog, resolver, renderer, mock(LoopService.class),
                mock(GridStore.class), callers, mock(SourceVisibility.class), mock(PlaceService.class), urls,
                JsonMapper.builder().build(), Clock.fixed(T, ZoneOffset.UTC));
        String etag = RasterCachePolicy.etag(spec.id(), frame.frameKey(), null, frame.status(), "nws-reflectivity@1",
                "5/7/12@1");
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/v1/products/" + spec.id() + "/tiles/" + frame.frameKey() + "/5/7/12.png");
        request.addHeader("If-None-Match", etag);
        ResponseEntity<byte[]> response = controller.getProductTile(spec.id(), frame.frameKey(), 5, 7, "12",
                "nws-reflectivity@1", null, null, null, null, null, request);
        assertThat(response.getStatusCode().value()).isEqualTo(304);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("public, max-age=31536000, immutable");
        verify(renderer, never()).tile(any(), any(), any(), anyInt(), anyInt(), anyInt(), anyInt(), any(), anyString());
    }
}
