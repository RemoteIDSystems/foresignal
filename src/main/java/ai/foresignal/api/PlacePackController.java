package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.export.ExportJobEntity;
import ai.foresignal.raster.export.ExportJobService;
import ai.foresignal.raster.export.PlacePackService;
import ai.foresignal.security.restrict.RequestPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The place offline pack (OpenAPI tag {@code Places}, prompt 108; Weather-Imagery-Design §11):
 * {@code createPlacePack} submits an async ZIP of place weather, forecast series, alerts, legends, a
 * loop GIF and one PMTiles per product. Capped at 50 MB (413 {@code PACK_TOO_LARGE}). An empty or
 * absent {@code products} list means the default set; the same input from the same token within 10
 * minutes answers the pack already built.
 */
@RestController
@Tag(name = "Places")
public class PlacePackController {

    private final PlaceService places;
    private final ExportJobService jobs;
    private final ReadCallerResolver callers;
    private final ProductUrls urls;

    /** Creates the controller. */
    public PlacePackController(PlaceService places, ExportJobService jobs, ReadCallerResolver callers,
            ProductUrls urls) {
        this.places = places;
        this.jobs = jobs;
        this.callers = callers;
        this.urls = urls;
    }

    /** {@code createPlacePack}: submit the pack, 202 with the job. */
    @PostMapping("/v1/places/{placeId}/pack")
    @Operation(operationId = "createPlacePack", tags = "Places")
    @ApiResponse(responseCode = "202", description = "Job accepted")
    public ResponseEntity<Map<String, Object>> createPlacePack(@PathVariable String placeId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        places.get(placeId);
        List<String> products = new ArrayList<>();
        int frames = 12;
        double radiusKm = PlacePackService.DEFAULT_RADIUS_KM;
        int maxZoom = PlacePackService.DEFAULT_MAX_ZOOM;
        if (body != null && body.isObject()) {
            JsonNode p = body.get("products");
            if (p != null && p.isArray()) {
                for (JsonNode n : p) {
                    products.add(n.asString());
                }
            }
            if (body.has("frames")) {
                int f = body.get("frames").asInt();
                if (f < 1 || f > 12) {
                    throw ValidationException.forField("frames", "must be between 1 and 12");
                }
                frames = f;
            }
            if (body.has("radiusKm")) {
                double r = body.get("radiusKm").asDouble();
                if (!(r > 0)) {
                    throw ValidationException.forField("radiusKm", "must be greater than 0");
                }
                radiusKm = r;
            }
            if (body.has("maxZoom")) {
                int z = body.get("maxZoom").asInt();
                if (z < 0 || z > 8) {
                    throw ValidationException.forField("maxZoom", "must be between 0 and 8");
                }
                maxZoom = z;
            }
        }
        // the build runs later on a worker: carry the caller's visibility (private and restricted sources)
        List<String> excluded = new ArrayList<>(callers.excludedSources(request));
        PlacePackService.Request pack = new PlacePackService.Request(placeId, products, frames, radiusKm, maxZoom,
                excluded);
        ExportJobEntity job = jobs.submitPlacePack(placeId, pack, tokenId(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobs.view(job, urls.base(request)));
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }
}
