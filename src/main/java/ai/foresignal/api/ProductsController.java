package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * The product catalog and frames (OpenAPI tag {@code Products}, prompt 042): {@code listProducts},
 * {@code getProduct}, {@code listProductFrames} and {@code getLatestProductFrame}.
 *
 * <p>Every operation needs the {@code read} scope, open while {@code security.openReads} is on.
 * Products of sources the caller may not read (private sources, restricted tokens) are invisible
 * ({@code 404 UNKNOWN_PRODUCT}). Responses are fully expanded: clients never build time strings.
 *
 * <p>Caching: the catalog and a product carry a strong {@code ETag} and answer {@code 304} on
 * {@code If-None-Match}; the {@code latest} alias gets {@code max-age = min(cadence / 2, 60 s)} plus
 * an {@code ETag} and {@code Foresignal-Frame-Time}. {@code public} only when {@code openReads}
 * covers the path for this caller and the product's source is not private; otherwise
 * {@code private}. No request here ever causes an upstream fetch.
 *
 * <p>Left out in edge mode (prompt 113: raster products).
 */
@RestController
@Tag(name = "Products")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class ProductsController {

    /** The valid time of the frame an alias resolved to. */
    public static final String FRAME_TIME_HEADER = "Foresignal-Frame-Time";

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 500;

    /** {@code Vary} of every response: it depends on the caller and the encoding. */
    static final String VARY = "Accept-Encoding, Authorization";

    private final ProductCatalogService catalog;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public ProductsController(ProductCatalogService catalog, ReadCallerResolver callers, SourceVisibility visibility,
            ProductUrls urls, JsonMapper json, Clock clock) {
        this.catalog = catalog;
        this.callers = callers;
        this.visibility = visibility;
        this.urls = urls;
        this.json = json;
        this.clock = clock;
    }

    /** {@code listProducts}: the catalog page, filtered; ETag and 304. */
    @GetMapping("/v1/products")
    @Operation(operationId = "listProducts", tags = "Products")
    public ResponseEntity<String> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "false") boolean compact,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        if (category != null && !ProductSpec.CATEGORIES.contains(category)) {
            throw ValidationException.forField("category", "must be one of " + ProductSpec.CATEGORIES);
        }
        if (kind != null && !ProductSpec.KINDS.contains(kind)) {
            throw ValidationException.forField("kind", "must be one of " + ProductSpec.KINDS);
        }
        if (q != null && q.length() > 200) {
            throw ValidationException.forField("q", "must be at most 200 characters");
        }
        BboxFilter box = bbox == null || bbox.isBlank() ? null : BboxParser.parse(bbox);
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        Map<String, Object> page = catalog.listProducts(
                new ProductCatalogService.ProductQuery(category, kind, box, source, q, compact), excluded, cursor,
                limit(limit), urls.base(request));
        // A page that lists a private source's product is never publicly cacheable.
        boolean anyPrivate = page.get("items") instanceof java.util.List<?> items && items.stream()
                .anyMatch(item -> item instanceof Map<?, ?> m && visibility.isPrivate(String.valueOf(m.get("source"))));
        return document(page, caller.publicCache() && !anyPrivate, ifNoneMatch);
    }

    /** {@code getProduct}: one product with every catalog field; ETag and 304. */
    @GetMapping("/v1/products/{productId}")
    @Operation(operationId = "getProduct", tags = "Products")
    public ResponseEntity<String> getProduct(
            @PathVariable String productId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        Map<String, Object> body = catalog.product(product, urls.base(request), false, clock.instant());
        return document(body, publicFor(caller, product), ifNoneMatch);
    }

    /** {@code listProductFrames}: a cursor page of frames, newest first, with a prefetch hint. */
    @GetMapping("/v1/products/{productId}/frames")
    @Operation(operationId = "listProductFrames", tags = "Products")
    public ResponseEntity<String> listProductFrames(
            @PathVariable String productId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String run,
            @RequestParam(required = false, defaultValue = "false") boolean includePartial,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        Map<String, Object> page = catalog.listFrames(product, time("from", from), time("to", to), run, includePartial,
                cursor, limit(limit), urls.base(request));
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(ETags.cacheControl(publicFor(caller, product)));
        headers.set(HttpHeaders.VARY, VARY);
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(json.writeValueAsString(page));
    }

    /** {@code getLatestProductFrame}: the {@code latest} alias, short max-age, ETag and 304. */
    @GetMapping("/v1/products/{productId}/frames/latest")
    @Operation(operationId = "getLatestProductFrame", tags = "Products")
    public ResponseEntity<String> getLatestProductFrame(
            @PathVariable String productId,
            @RequestParam(required = false) String run,
            @RequestParam(required = false, defaultValue = "false") boolean includePartial,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        RasterFrameRow frame = catalog.latestFrame(product, run, includePartial);
        String base = urls.base(request);
        String etag = "\"f-" + frame.frameKey() + (frame.referenceTime() == null ? ""
                : "-" + FrameKeys.timeKey(frame.referenceTime())) + "-" + frame.status() + "-"
                + Integer.toHexString(base.hashCode()) + "\"";
        Duration maxAge = ProductCatalogService.aliasMaxAge(product.spec());
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl((publicFor(caller, product) ? "public" : "private") + ", max-age=" + maxAge.toSeconds());
        headers.set(HttpHeaders.VARY, VARY);
        headers.set(FRAME_TIME_HEADER, frame.validTime().toString());
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON)
                .body(json.writeValueAsString(catalog.frame(product.spec(), frame, base)));
    }

    private static final MediaType JSON = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private boolean publicFor(ReadCaller caller, ProductRegistry.Registered product) {
        return caller.publicCache() && !visibility.isPrivate(product.sourceId());
    }

    private ResponseEntity<String> document(Map<String, Object> body, boolean publicCache, String ifNoneMatch) {
        String text = json.writeValueAsString(body);
        String etag = ETags.forDocument(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(ETags.cacheControl(publicCache));
        headers.set(HttpHeaders.VARY, VARY);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(text);
    }

    private static int limit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw ValidationException.forField("limit", "must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    private static Instant time(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.strip()).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value.strip().toUpperCase(Locale.ROOT));
            } catch (DateTimeParseException ignored) {
                throw ValidationException.forField(field, "must be an ISO-8601 date-time");
            }
        }
    }
}
