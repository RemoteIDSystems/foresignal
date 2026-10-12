package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.render.LegendService;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.StyleDefinition;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Styles and legends (OpenAPI tag {@code Products}, prompt 045; Weather-Imagery-Design §6.3):
 * {@code listStyles}, {@code getStyle}, {@code getProductLegend} and {@code getProductLegendPng}, all
 * {@code read}.
 *
 * <p>Styles are global and versioned; an unregistered name is {@code 400 UNKNOWN_STYLE}. A legend in
 * a versioned style ({@code name@v}) is immutable under {@link RasterCachePolicy}; one in a bare name
 * gets the short lifetime.
 */
@RestController
@Tag(name = "Products")
public class StylesController {

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 500;
    static final String CURSOR_PREFIX = "s1.";

    private final StyleRegistry styles;
    private final LegendService legends;
    private final RasterRenderService renderer;
    private final ProductCatalogService catalog;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final JsonMapper json;

    /** Creates the controller. */
    public StylesController(StyleRegistry styles, LegendService legends, RasterRenderService renderer,
            ProductCatalogService catalog, ReadCallerResolver callers, SourceVisibility visibility, JsonMapper json) {
        this.styles = styles;
        this.legends = legends;
        this.renderer = renderer;
        this.catalog = catalog;
        this.callers = callers;
        this.visibility = visibility;
        this.json = json;
    }

    /** {@code listStyles}: every registered style version, by name then version. */
    @GetMapping("/v1/styles")
    @Operation(operationId = "listStyles", tags = "Products")
    public ResponseEntity<String> listStyles(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int max = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        String after = decode(cursor);
        ReadCaller caller = callers.resolve(request);
        List<Map<String, Object>> items = new ArrayList<>();
        String last = null;
        boolean more = false;
        for (StyleDefinition style : styles.all()) {
            if (after != null && compare(style.ref(), after) <= 0) {
                continue;
            }
            if (items.size() == max) {
                more = true;
                break;
            }
            items.add(style.toMap(styles.isCurrent(style)));
            last = style.ref();
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("items", items);
        page.put("nextCursor", more && last != null ? CURSOR_PREFIX + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(last.getBytes(StandardCharsets.UTF_8)) : null);
        return document(json.writeValueAsString(page), caller.publicCache(), request);
    }

    /** {@code getStyle}: one version; a bare name is the current version. */
    @GetMapping("/v1/styles/{styleId}")
    @Operation(operationId = "getStyle", tags = "Products")
    public ResponseEntity<String> getStyle(@PathVariable String styleId, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        StyleRegistry.Resolved resolved = styles.resolve(styleId);
        return document(json.writeValueAsString(resolved.style().toMap(styles.isCurrent(resolved.style()))),
                caller.publicCache(), request);
    }

    /** {@code getProductLegend}: the JSON legend in a style and unit system. */
    @GetMapping("/v1/products/{productId}/legend")
    @Operation(operationId = "getProductLegend", tags = "Products")
    public ResponseEntity<String> getProductLegend(
            @PathVariable String productId,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String units,
            HttpServletRequest request) {
        boolean imperial = "imperial".equals(QueryParams.oneOf("units", units, List.of("metric", "imperial"), "metric"));
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        StyleRegistry.Resolved resolved = renderer.style(spec, style);
        String text = json.writeValueAsString(legends.legend(spec, resolved.style(), imperial));
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        String etag = RasterCachePolicy.etagOf(body);
        HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(
                RasterTilesController.versioned(style, resolved),
                publicFor(caller, product), spec), etag, null);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(RasterTilesController.JSON).body(text);
    }

    /** {@code getProductLegendPng}: the legend drawn with Java2D. */
    @GetMapping("/v1/products/{productId}/legend.png")
    @Operation(operationId = "getProductLegendPng", tags = "Products")
    public ResponseEntity<byte[]> getProductLegendPng(
            @PathVariable String productId,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String units,
            @RequestParam(required = false) String width,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) String theme,
            HttpServletRequest request) {
        boolean imperial = "imperial".equals(QueryParams.oneOf("units", units, List.of("metric", "imperial"), "metric"));
        int w = QueryParams.intInRange("width", width, 64, 2048, LegendService.DEFAULT_WIDTH);
        boolean vertical = "vertical".equals(QueryParams.oneOf("orientation", orientation,
                List.of("horizontal", "vertical"), "horizontal"));
        boolean dark = "dark".equals(QueryParams.oneOf("theme", theme, List.of("dark", "light"), "light"));
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        StyleRegistry.Resolved resolved = renderer.style(spec, style);
        String etag = RasterCachePolicy.etag(spec.id(), String.valueOf(spec.hashCode()), resolved.ref(),
                String.valueOf(imperial), String.valueOf(w), String.valueOf(vertical), String.valueOf(dark));
        HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(
                RasterTilesController.versioned(style, resolved),
                publicFor(caller, product), spec), etag, null);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        byte[] png = legends.png(spec, resolved.style(), imperial, w, vertical, dark);
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(png);
    }

    private boolean publicFor(ReadCaller caller, ProductRegistry.Registered product) {
        return caller.publicCache() && !visibility.isPrivate(product.sourceId());
    }

    private ResponseEntity<String> document(String text, boolean publicCache, HttpServletRequest request) {
        String etag = ETags.forDocument(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(ETags.cacheControl(publicCache));
        headers.set(HttpHeaders.VARY, RasterCachePolicy.VARY);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(RasterTilesController.JSON).body(text);
    }

    /** Orders refs by name, then numeric version. */
    static int compare(String a, String b) {
        String[] pa = a.split("@");
        String[] pb = b.split("@");
        int byName = pa[0].compareTo(pb[0]);
        if (byName != 0) {
            return byName;
        }
        return Integer.compare(Integer.parseInt(pa[1]), Integer.parseInt(pb[1]));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            if (!cursor.startsWith(CURSOR_PREFIX)) {
                throw new IllegalArgumentException("prefix");
            }
            String ref = new String(Base64.getUrlDecoder().decode(cursor.substring(CURSOR_PREFIX.length())),
                    StandardCharsets.UTF_8);
            if (!ref.matches("^[a-z0-9-]+@[0-9]{1,6}$")) {
                throw new IllegalArgumentException("shape");
            }
            return ref;
        } catch (IllegalArgumentException e) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_CURSOR,
                    "The cursor is not one this endpoint issued.");
        }
    }
}
