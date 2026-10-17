package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.export.ExportJobEntity;
import ai.foresignal.raster.export.ExportJobService;
import ai.foresignal.raster.export.RasterExportService;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Envelope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Raster exports (OpenAPI tag {@code Products}, prompt 108; Weather-Imagery-Design §11): submit an
 * MBTiles/PMTiles/COG export over frames and a box, then poll and download. All async; the files live
 * in {@code ${FORESIGNAL_DATA_DIR}/exports/} for 48 hours.
 *
 * <p>Left out in edge mode (prompt 113: exports).
 */
@RestController
@Tag(name = "Products")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class ExportsController {

    private final ProductRegistry registry;
    private final ProductCatalogService catalog;
    private final ExportJobService jobs;
    private final PlaceService places;
    private final ReadCallerResolver callers;
    private final ai.foresignal.raster.ProductUrls urls;
    private final ai.foresignal.export.ExportJobService bulk;

    /** Creates the controller. */
    public ExportsController(ProductRegistry registry, ProductCatalogService catalog, ExportJobService jobs,
            PlaceService places, ReadCallerResolver callers, ai.foresignal.raster.ProductUrls urls,
            ai.foresignal.export.ExportJobService bulk) {
        this.registry = registry;
        this.catalog = catalog;
        this.jobs = jobs;
        this.places = places;
        this.callers = callers;
        this.urls = urls;
        this.bulk = bulk;
    }

    /** {@code createProductExport}: submit an export, 202 with the job. */
    @PostMapping("/v1/products/{productId}/exports")
    @Operation(operationId = "createProductExport", tags = "Products")
    public ResponseEntity<java.util.Map<String, Object>> createProductExport(@PathVariable String productId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = catalog.visible(productId, callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        ai.foresignal.raster.render.RasterRenderService.requireRendered(spec);
        catalog.requireAvailable(product);
        RasterExportService.Request export = parse(body, spec);
        ExportJobEntity job = jobs.submitProductExport(product, export, tokenId(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobs.view(job, urls.base(request)));
    }

    /**
     * {@code getExport}: the job status, of a raster export or place pack (108) or of an export,
     * snapshot or pack (111). A 111 job is visible only to the token that created it and to admin;
     * any other caller gets {@code 404}.
     */
    @GetMapping("/v1/exports/{exportId}")
    @Operation(operationId = "getExport", tags = "Products")
    public java.util.Map<String, Object> getExport(@PathVariable String exportId, HttpServletRequest request) {
        if (bulk.exists(exportId)) {
            return bulk.view(bulk.visible(exportId, ExportCallers.tokenId(request), ExportCallers.admin(request, callers)),
                    urls.base(request));
        }
        return jobs.view(jobs.get(exportId), urls.base(request));
    }

    /** {@code downloadExportFile}: the file bytes. */
    @GetMapping("/v1/exports/{exportId}/files/{fileName}")
    @Operation(operationId = "downloadExportFile", tags = "Products")
    public ResponseEntity<FileSystemResource> downloadExportFile(@PathVariable String exportId,
            @PathVariable String fileName) {
        Path file = jobs.file(exportId, fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType(fileName));
        headers.setContentDispositionFormData("attachment", fileName);
        try {
            headers.setContentLength(Files.size(file));
        } catch (IOException e) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.UNKNOWN_EXPORT, "The file is gone.");
        }
        return ResponseEntity.ok().headers(headers).body(new FileSystemResource(file));
    }

    private RasterExportService.Request parse(JsonNode body, ProductSpec spec) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a ProductExportRequest object");
        }
        String format = text(body.get("format"));
        if (!List.of("mbtiles", "pmtiles", "cog").contains(format)) {
            throw ValidationException.forField("format", "must be mbtiles, pmtiles or cog");
        }
        List<String> frameKeys = new ArrayList<>();
        String single = text(body.get("frameKey"));
        if (single != null) {
            frameKeys.add(single);
        }
        JsonNode keys = body.get("frameKeys");
        if (keys != null && keys.isArray()) {
            if (keys.size() > 24) {
                throw ValidationException.forField("frameKeys", "at most 24");
            }
            for (JsonNode k : keys) {
                frameKeys.add(k.asString());
            }
        }
        if (frameKeys.isEmpty()) {
            frameKeys.add(ai.foresignal.raster.FrameResolver.LATEST);
        }
        double[] bbox = box(body);
        int minZoom = intOrDefault(body.get("minZoom"), 0, 0, 24, "minZoom");
        int nativeMax = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 8
                : spec.spatial().nativeMaxZoom();
        int maxZoom = intOrDefault(body.get("maxZoom"), Math.min(nativeMax, 8), minZoom, Math.min(nativeMax, 24),
                "maxZoom");
        TileMatrixSet tms = TileMatrixSets.resolve(text(body.get("tms")));
        // MBTiles/PMTiles are XYZ WebMercator archives: refuse WorldCRS84Quad up front (400)
        RasterExportService.requireSupportedTms(format, tms);
        String style = text(body.get("style"));
        return new RasterExportService.Request(format, frameKeys, bbox, minZoom, maxZoom, style, tms);
    }

    private double[] box(JsonNode body) {
        JsonNode bbox = body.get("bbox");
        if (bbox != null && bbox.isArray() && bbox.size() == 4) {
            return new double[] {bbox.get(0).asDouble(), bbox.get(1).asDouble(), bbox.get(2).asDouble(),
                bbox.get(3).asDouble()};
        }
        String placeId = text(body.get("placeId"));
        if (placeId != null) {
            PlaceView place = places.get(placeId);
            double radiusKm = body.has("radiusKm") ? body.get("radiusKm").asDouble() : 150;
            Envelope e = place.area() != null && !place.area().isEmpty() ? place.area().getEnvelopeInternal()
                    : new Envelope(place.geometry().getEnvelopeInternal());
            double dLat = radiusKm / 111.32;
            double dLon = radiusKm / (111.32 * Math.max(0.01, Math.cos(Math.toRadians((e.getMinY() + e.getMaxY()) / 2))));
            return new double[] {e.getMinX() - dLon, e.getMinY() - dLat, e.getMaxX() + dLon, e.getMaxY() + dLat};
        }
        return new double[] {-180, -85, 180, 85};
    }

    private static int intOrDefault(JsonNode node, int fallback, int min, int max, String field) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        int value = node.asInt();
        if (value < min || value > max) {
            throw ValidationException.forField(field, "must be between " + min + " and " + max);
        }
        return value;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }

    private static MediaType mediaType(String fileName) {
        if (fileName.endsWith(".mbtiles")) {
            return MediaType.parseMediaType("application/vnd.sqlite3");
        }
        if (fileName.endsWith(".pmtiles")) {
            return MediaType.parseMediaType("application/vnd.pmtiles");
        }
        if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")) {
            return MediaType.parseMediaType("image/tiff; application=geotiff; profile=cloud-optimized");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
