package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.model.TimelineService;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.security.visibility.SourceVisibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * The cross-product timeline (OpenAPI tag {@code Products}, operation {@code getProductTimeline},
 * prompt 051; Weather-Imagery-Design §6.2), {@code read}. The literal {@code /v1/products/timeline}
 * route wins over {@code /v1/products/{id}}, and {@code timeline} is a reserved product id.
 *
 * <p>The document names concrete frames, so it gets the short alias lifetime with an ETag
 * ({@link RasterCachePolicy}); {@code public} only when {@code openReads} covers the caller and no
 * product's source is private.
 */
@RestController
@Tag(name = "Products")
public class ProductTimelineController {

    static final MediaType JSON = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final TimelineService timelines;
    private final ProductCatalogService catalog;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public ProductTimelineController(TimelineService timelines, ProductCatalogService catalog,
            ReadCallerResolver callers, SourceVisibility visibility, ProductUrls urls, JsonMapper json, Clock clock) {
        this.timelines = timelines;
        this.catalog = catalog;
        this.callers = callers;
        this.visibility = visibility;
        this.urls = urls;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getProductTimeline}. */
    @GetMapping("/v1/products/timeline")
    @Operation(operationId = "getProductTimeline", tags = "Products")
    public ResponseEntity<String> getProductTimeline(
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String past,
            @RequestParam(required = false) String future,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        TimelineService.TimelineRequest timelineRequest = new TimelineService.TimelineRequest(
                QueryParams.multi("products", products, TimelineService.MAX_PRODUCTS),
                blankToNull(mode), blankToNull(past), blankToNull(future),
                QueryParams.instant("from", from), QueryParams.instant("to", to));
        Map<String, Object> doc = timelines.timeline(timelineRequest, callers.excludedSources(caller),
                urls.base(request), clock.instant());
        boolean publicCache = caller.publicCache();
        if (doc.get("products") instanceof List<?> ids) {
            for (Object id : ids) {
                publicCache &= !visibility.isPrivate(catalog.visible(String.valueOf(id),
                        callers.excludedSources(caller)).sourceId());
            }
        }
        String text = json.writeValueAsString(doc);
        String etag = RasterCachePolicy.etagOf(text.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(false, publicCache, null), etag, null);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(text);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
