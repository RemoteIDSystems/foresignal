package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.GridStore;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.RasterPipeline;
import ai.foresignal.raster.render.DataTileEncoder;
import ai.foresignal.raster.render.LoopService;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.raster.render.TransparentTile;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.raster.render.WebpSupport;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Raster tiles, TileJSON, single images and animation loops (OpenAPI tag {@code Products}, prompt
 * 045; Weather-Imagery-Design §6.3): {@code getProductTile}, {@code getProductTileJson},
 * {@code getProductImage} and {@code getProductLoop}, all {@code read}.
 *
 * <p>Every response follows {@link RasterCachePolicy}: content-addressed URLs are immutable, anything
 * else gets a short max-age, an ETag checked before rendering, and {@code Foresignal-Frame-Time}.
 * Renders go through the {@link RenderQueue} (503 {@code RENDER_BUSY} after 1.5 s). An ISO-extended
 * time in a path answers 301 to its time key. External products answer 400 ("fetch externalTemplate
 * directly"). Nothing here ever causes an upstream fetch.
 *
 * <p>Left out in edge mode (prompt 113: raster products and rendering).
 */
@RestController
@Tag(name = "Products")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class RasterTilesController {

    static final Pattern TILE_ROW = Pattern.compile("^([0-9]{1,8})(@2x)?$");
    static final String PREFETCH_HEADER = "Foresignal-Prefetch";
    static final MediaType JSON = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
    static final MediaType JPEG = MediaType.IMAGE_JPEG;

    private final ProductCatalogService catalog;
    private final FrameResolver resolver;
    private final RasterRenderService renderer;
    private final LoopService loops;
    private final GridStore grids;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final PlaceService places;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final Clock clock;
    private final WebpSupport webp;

    /** Creates the controller. */
    public RasterTilesController(ProductCatalogService catalog, FrameResolver resolver, RasterRenderService renderer,
            LoopService loops, GridStore grids, ReadCallerResolver callers, SourceVisibility visibility,
            PlaceService places, ProductUrls urls, JsonMapper json, Clock clock, WebpSupport webp) {
        this.catalog = catalog;
        this.resolver = resolver;
        this.renderer = renderer;
        this.loops = loops;
        this.grids = grids;
        this.callers = callers;
        this.visibility = visibility;
        this.places = places;
        this.urls = urls;
        this.json = json;
        this.clock = clock;
        this.webp = webp;
    }

    static final MediaType WEBP = MediaType.parseMediaType("image/webp");

    /** {@code getProductTile}: a WebMercatorQuad PNG tile, 256 px or {@code @2x} 512 px. */
    @GetMapping("/v1/products/{productId}/tiles/{frameKey}/{z}/{x}/{y}.png")
    @Operation(operationId = "getProductTile", tags = "Products")
    public ResponseEntity<byte[]> getProductTile(
            @PathVariable String productId, @PathVariable String frameKey, @PathVariable int z, @PathVariable int x,
            @PathVariable String y, @RequestParam(required = false) String style,
            @RequestParam(required = false) String run, @RequestParam(required = false) String empty,
            @RequestParam(required = false) String onExpired, @RequestParam(required = false) String prefetch,
            @RequestHeader(value = PREFETCH_HEADER, required = false) String prefetchHeader,
            HttpServletRequest request) {
        return serveTile(productId, frameKey, null, z, x, y, "png", null, style, run, empty, onExpired, prefetch,
                prefetchHeader, request);
    }

    /** {@code getProductTileWebp}: the same tile as WebP (prompt 108). */
    @GetMapping("/v1/products/{productId}/tiles/{frameKey}/{z}/{x}/{y}.webp")
    @Operation(operationId = "getProductTileWebp", tags = "Products")
    public ResponseEntity<byte[]> getProductTileWebp(
            @PathVariable String productId, @PathVariable String frameKey, @PathVariable int z, @PathVariable int x,
            @PathVariable String y, @RequestParam(required = false) String style,
            @RequestParam(required = false) String run, @RequestParam(required = false) String empty,
            @RequestParam(required = false) String onExpired, @RequestParam(required = false) String prefetch,
            @RequestHeader(value = PREFETCH_HEADER, required = false) String prefetchHeader,
            HttpServletRequest request) {
        return serveTile(productId, frameKey, null, z, x, y, "webp", null, style, run, empty, onExpired, prefetch,
                prefetchHeader, request);
    }

    /** {@code getProductTileTms}: a tile in a named tile matrix set, PNG or WebP (prompt 108). */
    @GetMapping("/v1/products/{productId}/tiles/{frameKey}/{tileMatrixSetId}/{z}/{x}/{y}.{format}")
    @Operation(operationId = "getProductTileTms", tags = "Products")
    public ResponseEntity<byte[]> getProductTileTms(
            @PathVariable String productId, @PathVariable String frameKey, @PathVariable String tileMatrixSetId,
            @PathVariable int z, @PathVariable int x, @PathVariable String y,
            @PathVariable String format, @RequestParam(required = false) String style,
            @RequestParam(required = false) String run, @RequestParam(required = false) String empty,
            @RequestParam(required = false) String onExpired, @RequestParam(required = false) String prefetch,
            @RequestParam(required = false) String tileSize,
            @RequestHeader(value = PREFETCH_HEADER, required = false) String prefetchHeader,
            HttpServletRequest request) {
        return serveTile(productId, frameKey, tileMatrixSetId, z, x, y, format, tileSize, style, run, empty, onExpired,
                prefetch, prefetchHeader, request);
    }

    /** {@code getProductDataTile}: band 0's values as Terrain-RGB style RGB (prompt 108). */
    @GetMapping("/v1/products/{productId}/data/{frameKey}/{z}/{x}/{y}.png")
    @Operation(operationId = "getProductDataTile", tags = "Products")
    public ResponseEntity<byte[]> getProductDataTile(
            @PathVariable String productId, @PathVariable String frameKey, @PathVariable int z, @PathVariable int x,
            @PathVariable String y, @RequestParam(required = false) String encoding,
            @RequestParam(required = false) String tms, @RequestParam(required = false) String run,
            HttpServletRequest request) {
        String enc = QueryParams.oneOf("encoding", encoding, List.of("custom", "terrarium"), "custom");
        Matcher row = TILE_ROW.matcher(y);
        if (!row.matches() || row.group(2) != null) {
            throw ValidationException.forField("y", "must be a tile row");
        }
        int tileY = Integer.parseInt(row.group(1));
        TileMatrixSet set = TileMatrixSets.resolve(tms);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        RasterRenderService.checkTile(spec, set, z, x, tileY);
        boolean signed = spec.dtype() == RasterDtype.I16;
        DataTileEncoder.Encoding choice;
        if ("terrarium".equals(enc)) {
            if (spec.dtype() == null || !spec.dtype().isInteger()) {
                throw ValidationException.forField("encoding", "terrarium needs an integer product");
            }
            choice = DataTileEncoder.Encoding.terrarium();
        } else {
            choice = signed ? DataTileEncoder.Encoding.signedCustom() : DataTileEncoder.Encoding.unsignedCustom();
        }
        String base = urls.base(request);
        FrameResolver.Resolution resolution = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                k -> ProductUrls.product(base, spec.id()) + "/data/" + k + "/" + z + "/" + x + "/" + y + ".png"
                        + query(request));
        if (resolution instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken());
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        boolean contentAddressed = found.contentAddressed();
        String etag = RasterCachePolicy.etag(spec.id(), frame.frameKey(), runOf(frame), frame.status(), "data",
                set.id() + "/" + z + "/" + x + "/" + tileY + "/" + choice.encoding());
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(contentAddressed, publicFor(caller, product), spec), etag, frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        byte[] bytes = renderer.dataTile(spec, frame, set, z, x, tileY, 256, choice,
                priority(null, null), clientKey(request));
        if (bytes == TileCache.EMPTY || bytes.length == 0) {
            return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG)
                    .body(TransparentTile.png());
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(bytes);
    }

    /** The shared tile body. */
    private ResponseEntity<byte[]> serveTile(String productId, String frameKey, String tmsId, int z, int x, String y,
            String format, String tileSize, String style, String run, String empty, String onExpired, String prefetch,
            String prefetchHeader, HttpServletRequest request) {
        if (!"png".equals(format) && !"webp".equals(format)) {
            throw ValidationException.forField("format", "must be png or webp");
        }
        if ("webp".equals(format) && !webp.available()) {
            throw new ForesignalException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.FORMAT_UNAVAILABLE,
                    webp.reason() == null ? "WebP is unavailable" : webp.reason());
        }
        Matcher row = TILE_ROW.matcher(y);
        if (!row.matches()) {
            throw ValidationException.forField("y", "must be a tile row, optionally suffixed @2x");
        }
        int tileY = Integer.parseInt(row.group(1));
        int scale = row.group(2) == null ? 1 : 2;
        int size = scale == 2 || "512".equals(tileSize == null ? null : tileSize.strip()) ? 512 : 256;
        if (tileSize != null && !tileSize.isBlank() && !"256".equals(tileSize.strip()) && !"512".equals(tileSize.strip())) {
            throw ValidationException.forField("tileSize", "must be 256 or 512");
        }
        TileMatrixSet tms = TileMatrixSets.resolve(tmsId);
        boolean empty204 = "204".equals(QueryParams.oneOf("empty", empty, List.of("204"), null));
        boolean expiredTransparent = QueryParams.oneOf("onExpired", onExpired, List.of("transparent"), null) != null;
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        RasterRenderService.checkTile(spec, tms, z, x, tileY);
        StyleRegistry.Resolved resolvedStyle = renderer.style(spec, style);
        boolean publicCache = publicFor(caller, product);
        String base = urls.base(request);
        String tmsSegment = tmsId == null || tmsId.isBlank() ? "" : tms.id() + "/";
        FrameResolver.Resolution resolution;
        try {
            resolution = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                    k -> ProductUrls.product(base, spec.id()) + "/tiles/" + k + "/" + tmsSegment + z + "/" + x + "/" + y
                            + "." + format + query(request));
        } catch (ForesignalException e) {
            if (expiredTransparent && ErrorCodes.FRAME_EXPIRED.equals(e.getCode())) {
                HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(false, publicCache, spec),
                        TransparentTile.ETAG, null);
                headers.set(RasterCachePolicy.FRAME_EXPIRED_HEADER, "true");
                return transparent(headers, empty204, request, format);
            }
            throw e;
        }
        if (resolution instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken());
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        boolean contentAddressed = found.contentAddressed() && versioned(style, resolvedStyle);
        RasterCachePolicy.Decision decision = RasterCachePolicy.decide(contentAddressed, publicCache, spec);
        String etag = RasterCachePolicy.etag(spec.id(), frame.frameKey(), runOf(frame), frame.status(),
                resolvedStyle.ref(), tms.id() + "/" + z + "/" + x + "/" + tileY + "/" + size + "/" + format);
        HttpHeaders headers = RasterCachePolicy.headers(decision, etag, frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        byte[] bytes = renderer.tile(spec, frame, resolvedStyle, tms, z, x, tileY, size, format,
                priority(prefetch, prefetchHeader), clientKey(request));
        if (bytes == TileCache.EMPTY || bytes.length == 0) {
            HttpHeaders emptyHeaders = RasterCachePolicy.headers(decision, TransparentTile.ETAG, frame.validTime());
            return transparent(emptyHeaders, empty204, request, format);
        }
        return ResponseEntity.ok().headers(headers)
                .contentType("webp".equals(format) ? WEBP : MediaType.IMAGE_PNG).body(bytes);
    }

    private ResponseEntity<byte[]> transparent(HttpHeaders headers, boolean empty204, HttpServletRequest request) {
        return transparent(headers, empty204, request, "png");
    }

    private ResponseEntity<byte[]> transparent(HttpHeaders headers, boolean empty204, HttpServletRequest request,
            String format) {
        if (empty204) {
            return ResponseEntity.noContent().headers(headers).build();
        }
        if (RasterCachePolicy.notModified(request, TransparentTile.ETAG)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(TransparentTile.png());
    }

    /** {@code getProductTileJson}: TileJSON 3.0.0 with a concrete content-addressed template. */
    @GetMapping("/v1/products/{productId}/tilejson")
    @Operation(operationId = "getProductTileJson", tags = "Products")
    public ResponseEntity<String> getProductTileJson(
            @PathVariable String productId,
            @RequestParam(required = false) String frameKey,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String includePartial,
            HttpServletRequest request) {
        boolean partial = QueryParams.bool("includePartial", includePartial, false);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        StyleRegistry.Resolved resolvedStyle = renderer.style(spec, style);
        String base = urls.base(request);
        String token = frameKey == null || frameKey.isBlank() ? FrameResolver.LATEST : frameKey.strip();
        UnaryOperator<String> link = k -> ProductUrls.tileJson(base, spec.id()) + "?frameKey=" + k
                + "&style=" + resolvedStyle.ref() + (run == null ? "" : "&run=" + run);
        FrameResolver.Resolution resolution = resolver.resolve(spec, token, run, partial, clock.instant(), link);
        if (resolution instanceof FrameResolver.Redirect redirect) {
            resolution = resolver.resolve(spec, redirect.canonicalToken(), run, partial, clock.instant(), link);
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        Map<String, Object> doc = tileJson(base, product, frame, resolvedStyle);
        String text = json.writeValueAsString(doc);
        boolean contentAddressed = found.contentAddressed() && versioned(style, resolvedStyle);
        String etag = RasterCachePolicy.etagOf(text.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(contentAddressed, publicFor(caller, product), spec), etag, frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(text);
    }

    /** The TileJSON document. */
    Map<String, Object> tileJson(String base, ProductRegistry.Registered product, RasterFrameRow frame,
            StyleRegistry.Resolved style) {
        ProductSpec spec = product.spec();
        List<Double> bounds = grids.find(frame.gridId()).map(g -> RasterPipeline.bounds(spec, g))
                .orElse(List.of(-180.0, -WebMercator.MAX_LAT, 180.0, WebMercator.MAX_LAT));
        double south = Math.max(-WebMercator.MAX_LAT, bounds.get(1));
        double north = Math.min(WebMercator.MAX_LAT, bounds.get(3));
        int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0 : spec.spatial().nativeMaxZoom();
        double west = bounds.get(0);
        double east = bounds.get(2);
        double centerLon = west <= east ? (west + east) / 2 : ((west + east + 360) / 2 > 180
                ? (west + east + 360) / 2 - 360 : (west + east + 360) / 2);
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("tilejson", "3.0.0");
        doc.put("name", spec.title());
        doc.put("description", spec.legendSummary() != null ? spec.legendSummary() : spec.title());
        doc.put("attribution", spec.attribution() == null ? null : spec.attribution().text());
        doc.put("scheme", "xyz");
        doc.put("tiles", List.of(ProductUrls.tiles(base, spec, frame, style.ref())));
        doc.put("minzoom", min);
        doc.put("maxzoom", max);
        doc.put("bounds", List.of(west, south, east, north));
        doc.put("center", List.of(round(centerLon), round((south + north) / 2), (double) min));
        doc.put("tileSize", WebMercator.TILE);
        doc.put("foresignal:tiles2x", List.of(ProductUrls.tiles2x(base, spec, frame, style.ref())));
        doc.put("foresignal:frameKey", frame.frameKey());
        doc.put("foresignal:style", style.ref());
        doc.put("foresignal:run", runOf(frame));
        doc.put("foresignal:expiresAt", frame.expiresAt() == null ? null : frame.expiresAt().toString());
        doc.put("foresignal:dataTiles", dataTileJson(base, spec, frame));
        return doc;
    }

    /** The data-tile block of a TileJSON document (prompt 108): template and `raster-dem` metadata. */
    static Map<String, Object> dataTileJson(String base, ProductSpec spec, RasterFrameRow frame) {
        boolean signed = spec.dtype() == RasterDtype.I16;
        boolean integer = spec.dtype() != null && spec.dtype().isInteger();
        String template = ProductUrls.product(base, spec.id()) + "/data/" + frame.frameKey() + "/{z}/{x}/{y}.png"
                + "?encoding={encoding}";
        Map<String, Object> custom = new LinkedHashMap<>();
        custom.put("encoding", "custom");
        custom.put("redFactor", 256.0 * spec.scale());
        custom.put("greenFactor", spec.scale());
        custom.put("blueFactor", 0.0);
        custom.put("baseShift", signed ? spec.offset() - 32768.0 * spec.scale() : spec.offset());
        custom.put("units", spec.units());
        custom.put("nodata", spec.nodata());
        Map<String, Object> encodings = new LinkedHashMap<>();
        encodings.put("custom", custom);
        if (integer) {
            Map<String, Object> terrarium = new LinkedHashMap<>();
            terrarium.put("encoding", "terrarium");
            terrarium.put("units", spec.units());
            terrarium.put("nodata", spec.nodata());
            encodings.put("terrarium", terrarium);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("template", template);
        out.put("tileSize", WebMercator.TILE);
        out.put("encodings", encodings);
        out.put("units", spec.units());
        out.put("nodata", spec.nodata());
        return out;
    }

    /** {@code getProductImage}: one frame over a box, PNG or JPEG, up to 2048 px a side. */
    @GetMapping("/v1/products/{productId}/image/{frameKey}.{format}")
    @Operation(operationId = "getProductImage", tags = "Products")
    public ResponseEntity<byte[]> getProductImage(
            @PathVariable String productId,
            @PathVariable String frameKey,
            @PathVariable String format,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String width,
            @RequestParam(required = false) String height,
            @RequestParam(required = false) String crs,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String prefetch,
            @RequestHeader(value = PREFETCH_HEADER, required = false) String prefetchHeader,
            HttpServletRequest request) {
        String fmt = QueryParams.oneOf("format", format, List.of("png", "jpg", "webp"), null);
        if ("webp".equals(fmt) && !webp.available()) {
            throw new ForesignalException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.FORMAT_UNAVAILABLE,
                    webp.reason() == null ? "WebP is unavailable" : webp.reason());
        }
        boolean mercator = !"EPSG:4326".equals(QueryParams.oneOf("crs", crs, List.of("EPSG:3857", "EPSG:4326"),
                "EPSG:3857"));
        if (bbox == null || bbox.isBlank()) {
            throw ValidationException.forField("bbox", "is required (minLon,minLat,maxLon,maxLat)");
        }
        double[] box = LoopService.snap(parseBox(bbox), mercator);
        Integer w = width == null || width.isBlank() ? null
                : QueryParams.intInRange("width", width, 1, RasterRenderService.MAX_IMAGE_SIDE, 0);
        Integer h = height == null || height.isBlank() ? null
                : QueryParams.intInRange("height", height, 1, RasterRenderService.MAX_IMAGE_SIDE, 0);
        int[] size = imageSize(box, w, h, mercator);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        StyleRegistry.Resolved resolvedStyle = renderer.style(spec, style);
        String base = urls.base(request);
        FrameResolver.Resolution resolution = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                k -> ProductUrls.product(base, spec.id()) + "/image/" + k + "." + fmt + query(request));
        if (resolution instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken());
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        RasterRenderService.ImageRequest image = new RasterRenderService.ImageRequest(box[0], box[1], box[2], box[3],
                size[0], size[1], mercator, "jpg".equals(fmt));
        boolean contentAddressed = found.contentAddressed() && versioned(style, resolvedStyle);
        String etag = RasterCachePolicy.etag(spec.id(), frame.frameKey(), runOf(frame), frame.status(),
                resolvedStyle.ref(), RasterRenderService.imageKey(spec, frame, resolvedStyle, image).region());
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(contentAddressed, publicFor(caller, product), spec), etag, frame.validTime());
        headers.set(RasterCachePolicy.IMAGE_BOUNDS_HEADER, image.boundsHeader());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        byte[] bytes = renderer.image(spec, frame, resolvedStyle, image, fmt, priority(prefetch, prefetchHeader),
                clientKey(request));
        MediaType type = "webp".equals(fmt) ? WEBP : "jpg".equals(fmt) ? JPEG : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().headers(headers).contentType(type).body(bytes);
    }

    /** {@code getProductLoop}: a snapped box and up to 24 {@code /image} frame URLs. */
    @GetMapping("/v1/products/loop")
    @Operation(operationId = "getProductLoop", tags = "Products")
    public ResponseEntity<String> getProductLoop(
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) String past,
            @RequestParam(required = false) String future,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) String radiusKm,
            @RequestParam(required = false) String frames,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String width,
            @RequestParam(required = false) String crs,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String run,
            HttpServletRequest request) {
        QueryParams.oneOf("crs", crs, List.of("EPSG:3857"), "EPSG:3857");
        boolean hasBox = bbox != null && !bbox.isBlank();
        boolean hasPlace = place != null && !place.isBlank();
        if (hasBox == hasPlace) {
            throw ValidationException.forField("bbox", "give exactly one of bbox or place");
        }
        Double radius = QueryParams.doubleInRange("radiusKm", radiusKm, 1, 2000);
        if (radius != null && !hasPlace) {
            throw ValidationException.forField("radiusKm", "applies only with place");
        }
        double[] box;
        if (hasPlace) {
            if (!ReadCallerResolver.tokenPresent(request)) {
                throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                        "A loop around a place needs a token: places are never anonymous.");
            }
            box = placeBox(places.get(place.strip()), radius);
        } else {
            box = parseBox(bbox);
        }
        ReadCaller caller = callers.resolve(request);
        LoopService.LoopRequest loopRequest = new LoopService.LoopRequest(
                QueryParams.multi("products", products, 2), blankToNull(past), blankToNull(future), box,
                frames == null || frames.isBlank() ? null : QueryParams.intInRange("frames", frames, 1,
                        LoopService.MAX_FRAMES, LoopService.DEFAULT_FRAMES),
                QueryParams.instant("from", from), QueryParams.instant("to", to),
                width == null || width.isBlank() ? null : QueryParams.intInRange("width", width, LoopService.MIN_WIDTH,
                        LoopService.MAX_WIDTH, LoopService.DEFAULT_WIDTH),
                blankToNull(style), blankToNull(run));
        Map<String, Object> loop = loops.loop(loopRequest, callers.excludedSources(caller), urls.base(request),
                clock.instant());
        boolean publicCache = caller.publicCache() && !hasPlace;
        if (loop.get("products") instanceof List<?> ids) {
            for (Object id : ids) {
                publicCache &= !visibility.isPrivate(catalog.visible(String.valueOf(id),
                        callers.excludedSources(caller)).sourceId());
            }
        }
        String text = json.writeValueAsString(loop);
        String etag = RasterCachePolicy.etagOf(text.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(false, publicCache, null), etag, null);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(text);
    }

    /** {@code getProductLoopGif}: an animated GIF of the same frames as {@code /loop} (prompt 108). */
    @GetMapping("/v1/products/loop.gif")
    @Operation(operationId = "getProductLoopGif", tags = "Products")
    public ResponseEntity<byte[]> getProductLoopGif(
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) String past,
            @RequestParam(required = false) String future,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) String radiusKm,
            @RequestParam(required = false) String frames,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String width,
            @RequestParam(required = false) String crs,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String delay,
            HttpServletRequest request) {
        QueryParams.oneOf("crs", crs, List.of("EPSG:3857"), "EPSG:3857");
        int delayMs = delay == null || delay.isBlank() ? ai.foresignal.raster.render.GifEncoder.DEFAULT_DELAY_MS
                : QueryParams.intInRange("delay", delay, ai.foresignal.raster.render.GifEncoder.MIN_DELAY_MS,
                        ai.foresignal.raster.render.GifEncoder.MAX_DELAY_MS,
                        ai.foresignal.raster.render.GifEncoder.DEFAULT_DELAY_MS);
        boolean hasBox = bbox != null && !bbox.isBlank();
        boolean hasPlace = place != null && !place.isBlank();
        if (hasBox == hasPlace) {
            throw ValidationException.forField("bbox", "give exactly one of bbox or place");
        }
        Double radius = QueryParams.doubleInRange("radiusKm", radiusKm, 1, 2000);
        if (radius != null && !hasPlace) {
            throw ValidationException.forField("radiusKm", "applies only with place");
        }
        double[] box;
        if (hasPlace) {
            if (!ReadCallerResolver.tokenPresent(request)) {
                throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                        "A loop around a place needs a token: places are never anonymous.");
            }
            box = placeBox(places.get(place.strip()), radius);
        } else {
            box = parseBox(bbox);
        }
        ReadCaller caller = callers.resolve(request);
        LoopService.LoopRequest loopRequest = new LoopService.LoopRequest(QueryParams.multi("products", products, 2),
                blankToNull(past), blankToNull(future), box,
                frames == null || frames.isBlank() ? null
                        : QueryParams.intInRange("frames", frames, 1, LoopService.MAX_FRAMES, LoopService.DEFAULT_FRAMES),
                QueryParams.instant("from", from), QueryParams.instant("to", to),
                width == null || width.isBlank() ? null
                        : QueryParams.intInRange("width", width, LoopService.MIN_WIDTH,
                                ai.foresignal.raster.render.GifEncoder.MAX_WIDTH, LoopService.DEFAULT_WIDTH),
                blankToNull(style), blankToNull(run));
        Map<String, Object> loop = loops.loop(loopRequest, callers.excludedSources(caller), urls.base(request),
                clock.instant());
        List<RasterRenderService.LoopFrame> loopFrames = new java.util.ArrayList<>();
        RasterRenderService.ImageRequest image = null;
        boolean contentAddressed = true;
        if (loop.get("frames") instanceof List<?> items) {
            int w = ((Number) loop.get("width")).intValue();
            int h = ((Number) loop.get("height")).intValue();
            @SuppressWarnings("unchecked")
            List<Number> b = (List<Number>) loop.get("bounds");
            double[] bounds = {b.get(0).doubleValue(), b.get(1).doubleValue(), b.get(2).doubleValue(),
                b.get(3).doubleValue()};
            for (Object raw : items) {
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) raw;
                String productId = String.valueOf(item.get("productId"));
                String frameKey = String.valueOf(item.get("frameKey"));
                String itemRun = item.get("run") == null ? null : String.valueOf(item.get("run"));
                contentAddressed &= FrameKeys.parseFrameKey(frameKey).contentAddressed();
                ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
                ProductSpec spec = product.spec();
                StyleRegistry.Resolved resolvedStyle = renderer.style(spec,
                        loop.get("products") instanceof List<?> p && p.size() == 1 ? style : null);
                FrameResolver.Resolution resolution = resolver.resolve(spec, frameKey, itemRun, false, clock.instant(),
                        k -> ProductUrls.product(urls.base(request), spec.id()) + "/image/" + k + ".png");
                if (resolution instanceof FrameResolver.Found found) {
                    image = new RasterRenderService.ImageRequest(bounds[0], bounds[1], bounds[2], bounds[3], w, h,
                            true, false);
                    loopFrames.add(new RasterRenderService.LoopFrame(spec, found.frame(), resolvedStyle));
                }
            }
        }
        if (loopFrames.isEmpty()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                    "No frames are available for this loop.");
        }
        // through the render queue (prefetch priority, RENDER_BUSY) and cached for content-addressed sets
        byte[] gif = renderer.loopGif(loopFrames, image, delayMs, contentAddressed, clientKey(request));
        String etag = RasterCachePolicy.etagOf(gif);
        HttpHeaders headers = RasterCachePolicy.headers(RasterCachePolicy.decide(contentAddressed, false, null), etag,
                null);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_GIF).body(gif);
    }

    /** A box around a place: a point's radius (or {@code radiusKm}), else its area grown by {@code radiusKm}. */
    static double[] placeBox(PlaceView place, Double radiusKm) {
        if ("point".equals(place.shape()) || place.area() == null) {
            Point center = place.geometry().getCentroid();
            double km = radiusKm != null ? radiusKm : place.radiusKm() != null ? place.radiusKm() : 50.0;
            return around(center.getX(), center.getY(), km);
        }
        Envelope e = place.area().getEnvelopeInternal();
        if (radiusKm == null) {
            return new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
        }
        double[] sw = around(e.getMinX(), e.getMinY(), radiusKm);
        double[] ne = around(e.getMaxX(), e.getMaxY(), radiusKm);
        return new double[] {sw[0], sw[1], ne[2], ne[3]};
    }

    /** A box of {@code km} around a point (spherical degrees, clamped to the globe). */
    static double[] around(double lon, double lat, double km) {
        double dLat = km / 111.32;
        double dLon = km / (111.32 * Math.max(0.01, Math.cos(Math.toRadians(lat))));
        return new double[] {Math.max(-180, lon - dLon), Math.max(-90, lat - dLat), Math.min(180, lon + dLon),
            Math.min(90, lat + dLat)};
    }

    /** Parses {@code minLon,minLat,maxLon,maxLat}. */
    static double[] parseBox(String bbox) {
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        double[] out = new double[4];
        for (int k = 0; k < 4; k++) {
            try {
                out[k] = Double.parseDouble(parts[k].strip());
            } catch (NumberFormatException e) {
                throw ValidationException.forField("bbox", "must be four numbers");
            }
            if (!Double.isFinite(out[k])) {
                throw ValidationException.forField("bbox", "must be four finite numbers");
            }
        }
        if (out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90 || out[1] >= out[3] || out[0] == out[2]) {
            throw ValidationException.forField("bbox", "must be a WGS84 box with minLat < maxLat");
        }
        return out;
    }

    /** The output size: given sides, or the box's aspect from the given (or default 1024) width. */
    static int[] imageSize(double[] box, Integer width, Integer height, boolean mercator) {
        int max = RasterRenderService.MAX_IMAGE_SIDE;
        if (width != null && height != null) {
            return new int[] {width, height};
        }
        double east = box[2] < box[0] ? box[2] + 360 : box[2];
        double w = mercator ? WebMercator.x(east) - WebMercator.x(box[0]) : east - box[0];
        double h = mercator ? WebMercator.y(box[3]) - WebMercator.y(box[1]) : box[3] - box[1];
        if (height != null) {
            int computed = (int) Math.max(1, Math.round(height * w / h));
            if (computed > max) {
                throw ValidationException.forField("width", "would be " + computed + " px; at most " + max);
            }
            return new int[] {computed, height};
        }
        int base = width != null ? width : 1024;
        int computed = (int) Math.max(1, Math.round(base * h / w));
        if (computed > max) {
            if (width != null) {
                throw ValidationException.forField("height", "would be " + computed + " px; at most " + max);
            }
            return new int[] {(int) Math.max(1, Math.round(base * (double) max / computed)), max};
        }
        return new int[] {base, computed};
    }

    private ResponseEntity<byte[]> redirect(String base, HttpServletRequest request, String token, String canonical) {
        String path = request.getRequestURI();
        int at = path.lastIndexOf("/" + token);
        String target = at < 0 ? path : path.substring(0, at + 1) + canonical + path.substring(at + 1 + token.length());
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(base + target + query(request))).build();
    }

    private static String query(HttpServletRequest request) {
        String q = request.getQueryString();
        return q == null || q.isEmpty() ? "" : "?" + q;
    }

    private boolean publicFor(ReadCaller caller, ProductRegistry.Registered product) {
        return caller.publicCache() && !visibility.isPrivate(product.sourceId());
    }

    /**
     * The fairness key: the tile key ({@code tilekey:<id>}, prompt 050), the feed key or the token
     * (the bound principal's {@link RequestPrincipal#clientKey()}), else the client IP.
     */
    static String clientKey(HttpServletRequest request) {
        String key = RequestPrincipal.of(request).map(RequestPrincipal::clientKey).orElse(null);
        return key != null ? key : "ip:" + request.getRemoteAddr();
    }

    /** Prefetch when {@code ?prefetch=1} or {@code Foresignal-Prefetch: 1}. */
    static RenderQueue.Priority priority(String prefetch, String header) {
        if (prefetch != null && !prefetch.isBlank() && !"1".equals(prefetch.strip())) {
            throw ValidationException.forField("prefetch", "must be 1");
        }
        return "1".equals(prefetch == null ? null : prefetch.strip()) || "1".equals(header == null ? null : header.strip())
                ? RenderQueue.Priority.PREFETCH : RenderQueue.Priority.INTERACTIVE;
    }

    /**
     * True when the URL itself names a style version ({@code style=name@v}); the product's default
     * applied to a URL without {@code style} does not make the URL content-addressed.
     */
    static boolean versioned(String param, StyleRegistry.Resolved resolved) {
        return param != null && !param.isBlank() && resolved.explicitVersion();
    }

    private static String runOf(RasterFrameRow frame) {
        return frame.referenceTime() == null ? null : FrameKeys.runIso(frame.referenceTime());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static double round(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }
}
