package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.model.FieldService;
import ai.foresignal.raster.model.VectorTileService;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Locale;
import java.util.Map;
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
 * Wind fields and barb vector tiles (OpenAPI tag {@code Products}, prompt 051;
 * Weather-Imagery-Design §6.4), all {@code read}:
 * <ul>
 *   <li>{@code getProductFieldManifest}: the God's Eye View {@code schemaVersion: 1} manifest of a
 *       {@code gfs} frame, with a content-addressed {@code dataUrl};
 *   <li>{@code getProductFieldData}: the planar little-endian Float32 u/v data, precompressed, sent
 *       with {@code Content-Encoding: gzip} ({@code 406 NOT_ACCEPTABLE} only when the client refuses
 *       gzip);
 *   <li>{@code getProductVectorTile}: the barb MVT of {@code gfs} or {@code hrrr}.
 * </ul>
 * Frame tokens, runs ({@code 400 RUN_REQUIRED} for a concrete forecast time without {@code run}) and
 * the cache rule are those of the raster tiles ({@link RasterCachePolicy}): a frame key with its
 * {@code ~sha8}, a pinned run and a complete frame make a URL immutable. Nothing here ever causes an
 * upstream fetch.
 */
@RestController
@Tag(name = "Products")
public class ModelFieldsController {

    static final MediaType JSON = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
    static final MediaType MVT = MediaType.parseMediaType("application/vnd.mapbox-vector-tile");

    private final ProductCatalogService catalog;
    private final FrameResolver resolver;
    private final FieldService fields;
    private final VectorTileService vectors;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public ModelFieldsController(ProductCatalogService catalog, FrameResolver resolver, FieldService fields,
            VectorTileService vectors, ReadCallerResolver callers, SourceVisibility visibility, ProductUrls urls,
            JsonMapper json, Clock clock) {
        this.catalog = catalog;
        this.resolver = resolver;
        this.fields = fields;
        this.vectors = vectors;
        this.callers = callers;
        this.visibility = visibility;
        this.urls = urls;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getProductFieldManifest}: the God's Eye View manifest of one frame's wind field. */
    @GetMapping("/v1/products/{productId}/fields/{frameKey}.json")
    @Operation(operationId = "getProductFieldManifest", tags = "Products")
    public ResponseEntity<String> getProductFieldManifest(
            @PathVariable String productId,
            @PathVariable String frameKey,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String res,
            @RequestParam(required = false) String bbox,
            HttpServletRequest request) {
        String resolution = FieldService.resolution(res);
        double[] box = FieldService.parseBox(bbox);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        FieldService.requireFields(spec);
        String base = urls.base(request);
        FrameResolver.Resolution resolution2 = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                k -> ProductUrls.product(base, spec.id()) + "/fields/" + k + ".json" + query(request));
        if (resolution2 instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken(), ".json");
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution2;
        RasterFrameRow frame = found.frame();
        FieldService.FieldData data = fields.data(spec, frame, resolution, box);
        Map<String, Object> manifest = fields.manifest(base, spec, product.sourceId(), frame, resolution, data);
        String text = json.writeValueAsString(manifest);
        String etag = RasterCachePolicy.etagOf(text.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(found.contentAddressed(), publicFor(caller, product), spec), etag,
                frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(JSON).body(text);
    }

    /** {@code getProductFieldData}: planar Float32 u then v, precompressed gzip. */
    @GetMapping("/v1/products/{productId}/fields/{frameKey}.f32")
    @Operation(operationId = "getProductFieldData", tags = "Products")
    public ResponseEntity<byte[]> getProductFieldData(
            @PathVariable String productId,
            @PathVariable String frameKey,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String res,
            @RequestParam(required = false) String bbox,
            @RequestHeader(value = HttpHeaders.ACCEPT_ENCODING, required = false) String acceptEncoding,
            HttpServletRequest request) {
        String resolution = FieldService.resolution(res);
        double[] box = FieldService.parseBox(bbox);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        FieldService.requireFields(spec);
        if (!acceptsGzip(acceptEncoding)) {
            throw new ForesignalException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOT_ACCEPTABLE,
                    "Field data is served precompressed with Content-Encoding: gzip; this client refuses gzip.");
        }
        String base = urls.base(request);
        FrameResolver.Resolution resolution2 = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                k -> ProductUrls.product(base, spec.id()) + "/fields/" + k + ".f32" + query(request));
        if (resolution2 instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken(), ".f32");
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution2;
        RasterFrameRow frame = found.frame();
        FieldService.FieldData data = fields.data(spec, frame, resolution, box);
        String etag = "\"f-" + data.sha256().substring(0, 16) + "\"";
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(found.contentAddressed(), publicFor(caller, product), spec), etag,
                frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        headers.set(HttpHeaders.CONTENT_ENCODING, "gzip");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.gzip().length).body(data.gzip());
    }

    /** {@code getProductVectorTile}: a wind barb Mapbox Vector Tile. */
    @GetMapping("/v1/products/{productId}/vectors/{frameKey}/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getProductVectorTile", tags = "Products")
    public ResponseEntity<byte[]> getProductVectorTile(
            @PathVariable String productId,
            @PathVariable String frameKey,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String prefetch,
            @RequestHeader(value = RasterTilesController.PREFETCH_HEADER, required = false) String prefetchHeader,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        VectorTileService.requireWind(spec);
        RasterRenderService.checkTile(spec, z, x, y);
        String base = urls.base(request);
        FrameResolver.Resolution resolution = resolver.resolve(spec, frameKey, run, false, clock.instant(),
                k -> ProductUrls.product(base, spec.id()) + "/vectors/" + k + "/" + z + "/" + x + "/" + y + ".mvt"
                        + query(request));
        if (resolution instanceof FrameResolver.Redirect redirect) {
            return redirect(base, request, frameKey, redirect.canonicalToken(), "/");
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        String etag = RasterCachePolicy.etag(spec.id(), frame.frameKey(), runOf(frame), frame.status(), "mvt",
                VectorTileService.LAYER, z + "/" + x + "/" + y);
        HttpHeaders headers = RasterCachePolicy.headers(
                RasterCachePolicy.decide(found.contentAddressed(), publicFor(caller, product), spec), etag,
                frame.validTime());
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        byte[] tile = vectors.tile(spec, frame, z, x, y, RasterTilesController.priority(prefetch, prefetchHeader),
                RasterTilesController.clientKey(request));
        return ResponseEntity.ok().headers(headers).contentType(MVT).body(tile);
    }

    /**
     * True unless {@code Accept-Encoding} rules gzip out: absent means anything goes; {@code gzip} or
     * {@code *} with a non-zero quality accepts it; {@code gzip;q=0} (or only {@code identity})
     * refuses it.
     */
    static boolean acceptsGzip(String header) {
        if (header == null || header.isBlank()) {
            return true;
        }
        Double gzip = null;
        Double any = null;
        for (String part : header.split(",")) {
            String[] pieces = part.strip().split(";");
            String coding = pieces[0].strip().toLowerCase(Locale.ROOT);
            double q = 1.0;
            for (int k = 1; k < pieces.length; k++) {
                String p = pieces[k].strip().toLowerCase(Locale.ROOT);
                if (p.startsWith("q=")) {
                    try {
                        q = Double.parseDouble(p.substring(2));
                    } catch (NumberFormatException e) {
                        q = 0;
                    }
                }
            }
            if (coding.equals("gzip") || coding.equals("x-gzip")) {
                gzip = q;
            } else if (coding.equals("*")) {
                any = q;
            }
        }
        if (gzip != null) {
            return gzip > 0;
        }
        return any != null && any > 0;
    }

    private static <T> ResponseEntity<T> redirect(String base, HttpServletRequest request, String token,
            String canonical, String after) {
        String path = request.getRequestURI();
        int at = path.lastIndexOf("/" + token + after);
        String target = at < 0 ? path
                : path.substring(0, at + 1) + canonical + path.substring(at + 1 + token.length());
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

    private static String runOf(RasterFrameRow frame) {
        return frame.referenceTime() == null ? null : FrameKeys.runIso(frame.referenceTime());
    }
}
