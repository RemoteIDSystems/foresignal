package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.raster.render.WebpSupport;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/** WMTS tile addressing and capabilities headers (prompt 108, post-review fixes). */
class WmtsControllerTest {

    private static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    private static ProductSpec spec() {
        return ProductSpec.builder("test-refl")
                .title("Reflectivity")
                .category("radar")
                .units("dBZ")
                .dtype(RasterDtype.U8)
                .scaleOffset(0.5, -33.0)
                .nodata(0.0)
                .defaultStyle("nws-reflectivity@1")
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6)))
                .spatial(new ProductSpec.Spatial("CONUS", 6, 2, 6, List.of(-130.0, 20.0, -60.0, 55.0)))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static WmtsController controller(ProductSpec spec) {
        RasterFrameRow frame = new RasterFrameRow(spec.id(), "20261016T120000Z", T, null, null, null, null, "g",
                "p/f.fsg", 10, "1a2b3c4d" + "0".repeat(56), "u8", 1, Map.of(), null, RasterFrameRow.COMPLETE, false,
                false, T, null);
        ProductCatalogService catalog = mock(ProductCatalogService.class);
        FrameResolver resolver = mock(FrameResolver.class);
        ReadCallerResolver callers = mock(ReadCallerResolver.class);
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(anyString(), any(), any()))
                .thenAnswer(inv -> ((Supplier<Object>) inv.getArgument(2)).get());
        when(callers.resolve(any())).thenReturn(new ReadCaller(Scope.READ, false, true, null, false));
        when(catalog.visible(eq(spec.id()), any())).thenReturn(new ProductRegistry.Registered(spec, "src", false));
        when(resolver.resolve(eq(spec), any(), any(), eq(false), any(), any()))
                .thenReturn(new FrameResolver.Found(frame, true));
        return new WmtsController(mock(ProductRegistry.class), catalog, resolver, mock(RasterFrameQueries.class),
                mock(RasterRenderService.class), callers, mock(SourceVisibility.class), settings,
                mock(WebpSupport.class), Clock.fixed(T, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("KVP GetTile maps TILEROW to y and TILECOL to x (redirect is …/z/x/y)")
    void kvpRowColOrder() {
        ProductSpec spec = spec();
        WmtsController controller = controller(spec);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/wmts");
        ResponseEntity<String> response = controller.getWmtsKvp("WMTS", "GetTile", "1.0.0", spec.id(), "default",
                "image/png", "WebMercatorQuad", "5", "12", "7", null, null, request);
        assertThat(response.getStatusCode().value()).isEqualTo(301);
        assertThat(response.getHeaders().getLocation().toString())
                .endsWith("/tiles/20261016T120000Z/WebMercatorQuad/5/7/12.png");
    }

    @Test
    @DisplayName("RESTful GetTile and KVP GetTile agree on the same row and column")
    void restMatchesKvp() {
        ProductSpec spec = spec();
        WmtsController controller = controller(spec);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/wmts/1.0.0/x");
        ResponseEntity<String> rest = controller.getWmtsTile(spec.id(), "default", "current", "WebMercatorQuad",
                "5", 12, 7, "png", null, request);
        ResponseEntity<String> kvp = controller.getWmtsKvp("WMTS", "GetTile", "1.0.0", spec.id(), "default",
                "image/png", "WebMercatorQuad", "5", "12", "7", null, null, request);
        assertThat(rest.getHeaders().getLocation()).isEqualTo(kvp.getHeaders().getLocation());
    }

    @Test
    @DisplayName("capabilities are private and vary by Authorization (the layer list depends on the caller)")
    void capabilitiesPrivate() {
        ResponseEntity<String> response = WmtsController.capabilitiesResponse("<x/>",
                new MockHttpServletRequest("GET", "/v1/wmts/1.0.0/WMTSCapabilities.xml"));
        assertThat(response.getHeaders().getCacheControl()).startsWith("private");
        assertThat(response.getHeaders().getFirst(HttpHeaders.VARY)).contains("Authorization");
        assertThat(response.getHeaders().getETag()).isNotNull();
    }

    @Test
    @DisplayName("TileMatrixSetLimits advertise only the native zoom range and the covered rows/cols")
    void limits() {
        StringBuilder xml = new StringBuilder();
        ProductSpec spec = spec();
        WmtsController.appendLimits(xml, spec, TileMatrixSets.resolve("WebMercatorQuad"), spec.spatial().bounds());
        String out = xml.toString();
        assertThat(out).contains("<TileMatrix>2</TileMatrix>").contains("<TileMatrix>6</TileMatrix>")
                .doesNotContain("<TileMatrix>1</TileMatrix>").doesNotContain("<TileMatrix>7</TileMatrix>");
        // z2 over North America: columns 0..1 (west half), rows 1 (20..55 N sits in row 1 at z2)
        assertThat(out).contains("<TileMatrix>2</TileMatrix><MinTileRow>1</MinTileRow><MaxTileRow>1</MaxTileRow>"
                + "<MinTileCol>0</MinTileCol><MaxTileCol>1</MaxTileCol>");
    }
}
