package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.raster.render.WebpSupport;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMTS 1.0.0 for raster products (OpenAPI tag {@code Products}, prompt 108;
 * Weather-Imagery-Design §11). One layer per product (each style a {@code Style}), both tile matrix
 * sets, and a {@code Time} dimension ({@code current} plus concrete frame times, newest 200); forecast
 * products add {@code RefTime}. A concrete time resolves to its frame and redirects to the
 * content-addressed tile URL, so caching stays correct; {@code current} redirects to the {@code latest}
 * alias (short cache). Capabilities are generated per request with an ETag and {@code max-age=60}.
 *
 * <p>When reached through {@code /v1/k/{key}/wmts/…} every generated URL carries that key prefix
 * ({@link PublicUrls#apiRoot()}), so QGIS and Cesium work without headers.
 */
@RestController
@Tag(name = "Products")
public class WmtsController {

    static final MediaType XML = MediaType.APPLICATION_XML;
    private static final int MAX_TIME_VALUES = 200;
    /** The deepest tile matrix advertised in each tile matrix set. */
    static final int MAX_MATRIX = 24;

    private final ProductRegistry registry;
    private final ProductCatalogService catalog;
    private final FrameResolver resolver;
    private final RasterFrameQueries frames;
    private final RasterRenderService renderer;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final AppSettingService settings;
    private final WebpSupport webp;
    private final Clock clock;

    /** Creates the controller. */
    public WmtsController(ProductRegistry registry, ProductCatalogService catalog, FrameResolver resolver,
            RasterFrameQueries frames, RasterRenderService renderer, ReadCallerResolver callers,
            SourceVisibility visibility, AppSettingService settings, WebpSupport webp, Clock clock) {
        this.registry = registry;
        this.catalog = catalog;
        this.resolver = resolver;
        this.frames = frames;
        this.renderer = renderer;
        this.callers = callers;
        this.visibility = visibility;
        this.settings = settings;
        this.webp = webp;
        this.clock = clock;
    }

    /** {@code getWmtsCapabilities}: the {@code WMTSCapabilities.xml} document. */
    @GetMapping("/v1/wmts/1.0.0/WMTSCapabilities.xml")
    @Operation(operationId = "getWmtsCapabilities", tags = "Products")
    public ResponseEntity<String> getWmtsCapabilities(HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        String xml = capabilities(caller, PublicUrls.of(settings, request));
        return capabilitiesResponse(xml, request);
    }

    /**
     * The capabilities response: an ETag over the document and {@code private, max-age=60} with
     * {@code Vary: Authorization}, because the layer list depends on the caller's visible sources (a
     * shared cache must never hand one caller's document to another).
     */
    static ResponseEntity<String> capabilitiesResponse(String xml, HttpServletRequest request) {
        String etag = RasterCachePolicy.etagOf(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("private, max-age=60");
        headers.set(HttpHeaders.VARY, RasterCachePolicy.VARY);
        headers.setETag(etag);
        if (RasterCachePolicy.notModified(request, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(XML).body(xml);
    }

    /** {@code getWmtsKvp}: WMTS 1.0.0 KVP, {@code REQUEST=GetCapabilities} or {@code GetTile}. */
    @GetMapping("/v1/wmts")
    @Operation(operationId = "getWmtsKvp", tags = "Products")
    public ResponseEntity<String> getWmtsKvp(
            @RequestParam(value = "SERVICE", required = false) String service,
            @RequestParam(value = "REQUEST", required = false) String wmtsRequest,
            @RequestParam(value = "VERSION", required = false) String version,
            @RequestParam(value = "LAYER", required = false) String layer,
            @RequestParam(value = "STYLE", required = false) String style,
            @RequestParam(value = "FORMAT", required = false) String format,
            @RequestParam(value = "TILEMATRIXSET", required = false) String tileMatrixSet,
            @RequestParam(value = "TILEMATRIX", required = false) String tileMatrix,
            @RequestParam(value = "TILEROW", required = false) String tileRow,
            @RequestParam(value = "TILECOL", required = false) String tileCol,
            @RequestParam(value = "TIME", required = false) String time,
            @RequestParam(value = "REFTIME", required = false) String refTime,
            HttpServletRequest request) {
        if (service != null && !"WMTS".equalsIgnoreCase(service.strip())) {
            throw ValidationException.forField("SERVICE", "must be WMTS");
        }
        if ("GetCapabilities".equalsIgnoreCase(wmtsRequest)) {
            ReadCaller caller = callers.resolve(request);
            String xml = capabilities(caller, PublicUrls.of(settings, request));
            return capabilitiesResponse(xml, request);
        }
        if ("GetTile".equalsIgnoreCase(wmtsRequest)) {
            if (layer == null || tileMatrix == null || tileRow == null || tileCol == null) {
                throw ValidationException.forField("LAYER", "GetTile needs LAYER, TILEMATRIX, TILEROW and TILECOL");
            }
            String fmt = mediaFormat(format);
            return tile(layer, style, time == null ? "current" : time, tileMatrixSet, tileMatrix, tileRow, tileCol,
                    fmt, refTime, request);
        }
        throw ValidationException.forField("REQUEST", "must be GetCapabilities or GetTile");
    }

    /** {@code getWmtsTile}: a WMTS RESTful tile, redirecting to the content-addressed tile. */
    @GetMapping("/v1/wmts/1.0.0/{layer}/{style}/{time}/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}.{format}")
    @Operation(operationId = "getWmtsTile", tags = "Products")
    public ResponseEntity<String> getWmtsTile(
            @PathVariable String layer,
            @PathVariable String style,
            @PathVariable String time,
            @PathVariable String tileMatrixSetId,
            @PathVariable String tileMatrix,
            @PathVariable int tileRow,
            @PathVariable int tileCol,
            @PathVariable String format,
            @RequestParam(value = "RefTime", required = false) String refTime,
            HttpServletRequest request) {
        return tile(layer, style, time, tileMatrixSetId, tileMatrix, String.valueOf(tileRow), String.valueOf(tileCol),
                mediaFormat(format), refTime, request);
    }

    /**
     * The shared tile handler: resolve the frame and 301 to the canonical content-addressed tile. The
     * WMTS row ({@code TileRow}, y) comes before the column ({@code TileCol}, x), matching both the KVP
     * and the RESTful callers; the redirect target is {@code …/{z}/{x}/{y}}.
     */
    ResponseEntity<String> tile(String layer, String style, String time, String tileMatrixSetId,
            String tileMatrix, String tileRowText, String tileColText, String format, String refTime,
            HttpServletRequest request) {
        if ("webp".equals(format) && !webp.available()) {
            throw new ForesignalException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.FORMAT_UNAVAILABLE,
                    webp.reason() == null ? "WebP is unavailable" : webp.reason());
        }
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(layer, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        TileMatrixSet set = TileMatrixSets.resolve(tileMatrixSetId);
        int z = parseInt("TILEMATRIX", tileMatrix);
        int row = parseInt("TILEROW", tileRowText);
        int col = parseInt("TILECOL", tileColText);
        RasterRenderService.checkTile(spec, set, z, col, row);
        String token = time == null || time.isBlank() || "current".equals(time) ? FrameResolver.LATEST : time.strip();
        FrameResolver.Resolution resolution = resolver.resolve(spec, token, refTime, false, clock.instant(),
                k -> k);
        if (resolution instanceof FrameResolver.Redirect redirect) {
            resolution = resolver.resolve(spec, redirect.canonicalToken(), refTime, false, clock.instant(), k -> k);
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        PublicUrls urls = PublicUrls.of(settings, request);
        StringBuilder target = new StringBuilder(urls.api("/v1/products/" + spec.id() + "/tiles/"))
                .append(frame.frameKey()).append('/').append(set.id()).append('/').append(z).append('/').append(col)
                .append('/').append(row).append('.').append(format);
        if (style != null && !style.isBlank() && !"default".equals(style)) {
            target.append("?style=").append(java.net.URLEncoder.encode(style,
                    java.nio.charset.StandardCharsets.UTF_8));
        }
        HttpHeaders headers = new HttpHeaders();
        long age = found.contentAddressed() ? RasterCachePolicy.IMMUTABLE_SECONDS : 60;
        headers.setCacheControl("public, max-age=" + age);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).headers(headers)
                .location(URI.create(target.toString())).build();
    }

    /** The capabilities document. */
    String capabilities(ReadCaller caller, PublicUrls urls) {
        Set<String> excluded = callers.excludedSources(caller);
        StringBuilder xml = new StringBuilder(32 * 1024);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Capabilities xmlns=\"http://www.opengis.net/wmts/1.0\"")
                .append(" xmlns:ows=\"http://www.opengis.net/ows/1.1\"")
                .append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.0.0\">\n");
        xml.append("  <ows:ServiceIdentification><ows:Title>Foresignal</ows:Title>")
                .append("<ows:ServiceType>OGC WMTS</ows:ServiceType>")
                .append("<ows:ServiceTypeVersion>1.0.0</ows:ServiceTypeVersion></ows:ServiceIdentification>\n");
        xml.append("  <ows:OperationsMetadata>")
                .append("<ows:Operation name=\"GetCapabilities\"><ows:DCP><ows:HTTP><ows:Get xlink:href=\"")
                .append(esc(urls.api("/v1/wmts"))).append("\"/></ows:HTTP></ows:DCP></ows:Operation>")
                .append("<ows:Operation name=\"GetTile\"><ows:DCP><ows:HTTP><ows:Get xlink:href=\"")
                .append(esc(urls.api("/v1/wmts"))).append("\"/></ows:HTTP></ows:DCP></ows:Operation>")
                .append("</ows:OperationsMetadata>\n");
        xml.append("  <Contents>\n");
        for (ProductRegistry.Registered product : registry.all()) {
            if (excluded.contains(product.sourceId()) || product.spec().isExternal()) {
                continue;
            }
            ProductSpec spec = product.spec();
            appendLayer(xml, urls, spec);
        }
        for (TileMatrixSet set : TileMatrixSets.ALL) {
            appendTileMatrixSet(xml, set);
        }
        xml.append("  </Contents>\n</Capabilities>\n");
        return xml.toString();
    }

    private void appendLayer(StringBuilder xml, PublicUrls urls, ProductSpec spec) {
        xml.append("    <Layer>\n");
        xml.append("      <ows:Title>").append(esc(spec.title())).append("</ows:Title>\n");
        xml.append("      <ows:Identifier>").append(esc(spec.id())).append("</ows:Identifier>\n");
        List<Double> bounds = spec.spatial() == null ? null : spec.spatial().bounds();
        xmlnsBox(xml, bounds);
        List<String> styles = spec.styles().isEmpty() ? List.of("default") : spec.styles();
        String defaultStyle = RasterRenderService.defaultStyle(spec);
        for (String style : styles) {
            xml.append("      <Style isDefault=\"").append(style.equals(defaultStyle) || (defaultStyle == null
                    && style.equals(styles.get(0)))).append("\"><ows:Identifier>")
                    .append(esc(style)).append("</ows:Identifier></Style>\n");
        }
        xml.append("      <Format>image/png</Format>\n");
        if (webp.available()) {
            xml.append("      <Format>image/webp</Format>\n");
        }
        appendTimeDimension(xml, spec);
        for (TileMatrixSet set : TileMatrixSets.ALL) {
            xml.append("      <TileMatrixSetLink><TileMatrixSet>").append(set.id()).append("</TileMatrixSet>");
            appendLimits(xml, spec, set, bounds);
            xml.append("</TileMatrixSetLink>\n");
        }
        String template = urls.api("/v1/wmts/1.0.0/" + spec.id() + "/{Style}/{Time}/{TileMatrixSet}/{TileMatrix}"
                + "/{TileRow}/{TileCol}.png");
        xml.append("      <ResourceURL format=\"image/png\" resourceType=\"tile\" template=\"")
                .append(esc(template)).append("\"/>\n");
        if (webp.available()) {
            xml.append("      <ResourceURL format=\"image/webp\" resourceType=\"tile\" template=\"")
                    .append(esc(template.substring(0, template.length() - 4) + ".webp")).append("\"/>\n");
        }
        xml.append("    </Layer>\n");
    }

    private void appendTimeDimension(StringBuilder xml, ProductSpec spec) {
        List<String> times = frameTimes(spec);
        xml.append("      <Dimension><ows:Identifier>Time</ows:Identifier><Default>current</Default>");
        for (String t : times) {
            xml.append("<Value>").append(esc(t)).append("</Value>");
        }
        xml.append("</Dimension>\n");
        if (spec.isForecast()) {
            xml.append("      <Dimension><ows:Identifier>RefTime</ows:Identifier><Default>latest</Default>");
            for (RasterFrameQueries.RunSummary run : frames.runs(spec.id())) {
                xml.append("<Value>").append(esc(FrameKeys.runIso(run.run()))).append("</Value>");
            }
            xml.append("</Dimension>\n");
        }
    }

    /** The newest complete frame times of a product, newest first, capped at {@value #MAX_TIME_VALUES}. */
    private List<String> frameTimes(ProductSpec spec) {
        Instant now = clock.instant();
        List<RasterFrameRow> rows = spec.isForecast()
                ? latestRunFrames(spec)
                : frames.page(spec.id(), null, now, null, false, null, null, MAX_TIME_VALUES);
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        for (RasterFrameRow row : rows) {
            String t = row.validTime().toString();
            if (seen.add(t) && out.size() < MAX_TIME_VALUES) {
                out.add(t);
            }
        }
        return out;
    }

    private List<RasterFrameRow> latestRunFrames(ProductSpec spec) {
        try {
            Instant run = resolver.selectRun(spec, FrameResolver.RUN_LATEST, false);
            return frames.runFrames(spec.id(), run, false);
        } catch (ForesignalException e) {
            return List.of();
        }
    }

    /**
     * The layer's {@code TileMatrixSetLimits}: only the product's native zoom range (min zoom to native
     * max zoom) and, within each, the rows and columns its bounds cover. Tiles outside them are answered
     * {@code 400 TILE_OUT_OF_RANGE}, so a client that honors the limits never requests one.
     */
    static void appendLimits(StringBuilder xml, ProductSpec spec, TileMatrixSet set, List<Double> bounds) {
        int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0
                : spec.spatial().nativeMaxZoom();
        max = Math.min(max, MAX_MATRIX);
        xml.append("<TileMatrixSetLimits>");
        for (int z = Math.max(0, min); z <= max; z++) {
            int[] range = bounds == null || bounds.size() < 4
                    ? new int[] {0, 0, set.tilesX(z) - 1, set.tilesY(z) - 1}
                    : set.tileRange(z, bounds.get(0), bounds.get(1), bounds.get(2), bounds.get(3));
            int minCol = Math.max(0, range[0]);
            int maxCol = Math.min(set.tilesX(z) - 1, range[2]);
            if (bounds != null && bounds.size() >= 4 && bounds.get(0) > bounds.get(2)) {
                // across the antimeridian: advertise the whole row
                minCol = 0;
                maxCol = set.tilesX(z) - 1;
            }
            int minRow = Math.max(0, range[1]);
            int maxRow = Math.min(set.tilesY(z) - 1, range[3]);
            xml.append("<TileMatrixLimits><TileMatrix>").append(z).append("</TileMatrix>")
                    .append("<MinTileRow>").append(minRow).append("</MinTileRow>")
                    .append("<MaxTileRow>").append(maxRow).append("</MaxTileRow>")
                    .append("<MinTileCol>").append(minCol).append("</MinTileCol>")
                    .append("<MaxTileCol>").append(maxCol).append("</MaxTileCol>")
                    .append("</TileMatrixLimits>");
        }
        xml.append("</TileMatrixSetLimits>");
    }

    private static void xmlnsBox(StringBuilder xml, List<Double> bounds) {
        if (bounds == null || bounds.size() < 4) {
            return;
        }
        xml.append("      <ows:WGS84BoundingBox><ows:LowerCorner>").append(bounds.get(0)).append(' ')
                .append(bounds.get(1)).append("</ows:LowerCorner><ows:UpperCorner>").append(bounds.get(2)).append(' ')
                .append(bounds.get(3)).append("</ows:UpperCorner></ows:WGS84BoundingBox>\n");
    }

    private static void appendTileMatrixSet(StringBuilder xml, TileMatrixSet set) {
        xml.append("    <TileMatrixSet><ows:Identifier>").append(set.id()).append("</ows:Identifier>");
        xml.append("<ows:SupportedCRS>").append(esc(set.crs())).append("</ows:SupportedCRS>\n");
        boolean mercator = "WebMercatorQuad".equals(set.id());
        for (int z = 0; z <= MAX_MATRIX; z++) {
            double cellSize = mercator ? 156543.03392804097 / (1 << z) : 0.703125 / (1 << z);
            double scale = mercator ? 559082264.0287178 / (1 << z) : 279541132.0143589 / (1 << z);
            double originX = mercator ? -20037508.342789244 : -180.0;
            double originY = mercator ? 20037508.342789244 : 90.0;
            int matrixWidth = mercator ? (1 << z) : (2 << z);
            int matrixHeight = mercator ? (1 << z) : (1 << z);
            xml.append("      <TileMatrix><ows:Identifier>").append(z).append("</ows:Identifier>")
                    .append("<ScaleDenominator>").append(scale).append("</ScaleDenominator>")
                    .append("<TopLeftCorner>").append(originX).append(' ').append(originY).append("</TopLeftCorner>")
                    .append("<TileWidth>256</TileWidth><TileHeight>256</TileHeight>")
                    .append("<MatrixWidth>").append(matrixWidth).append("</MatrixWidth>")
                    .append("<MatrixHeight>").append(matrixHeight).append("</MatrixHeight>")
                    .append("</TileMatrix>\n");
            if (z >= MAX_MATRIX) {
                break;
            }
        }
        xml.append("    </TileMatrixSet>\n");
    }

    private static String mediaFormat(String format) {
        if (format == null) {
            return "png";
        }
        String f = format.strip().toLowerCase(java.util.Locale.ROOT);
        if (f.contains("webp")) {
            return "webp";
        }
        if (f.contains("png")) {
            return "png";
        }
        throw ValidationException.forField("FORMAT", "must be image/png or image/webp");
    }

    private static int parseInt(String name, String raw) {
        try {
            return Integer.parseInt(raw.strip());
        } catch (RuntimeException e) {
            throw ValidationException.forField(name, "must be an integer");
        }
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
