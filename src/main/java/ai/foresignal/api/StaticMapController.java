package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.render.MapRenderer;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.StaticMapService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getStaticMap} (OpenAPI tag {@code Maps}, prompt 055, {@code read}; Weather-Imagery-Design §6.6): a
 * basemap-free PNG from Natural Earth and Foresignal's own frames, with the alert geometry, the place and
 * its range rings, frame valid times, legend, attribution and the footer "Automated — not an official
 * warning".
 *
 * <ul>
 *   <li>A {@code place} needs a token even while {@code openReads} is on (places are never anonymous); an
 *       unknown one is {@code 404 UNKNOWN_PLACE}.
 *   <li>An {@code alert} the caller may not read (a private source, a token restriction, a sensitive
 *       alert) is reported like an unknown alert ({@code 404 NOT_FOUND}), as {@code getAlert} does.
 *   <li>Headers: {@code Foresignal-Image-Bounds} (the map area's {@code w,s,e,n}), the cache rule of
 *       {@link RasterCachePolicy} (immutable only when every frame is pinned), the ETag checked before
 *       rendering, and {@code Foresignal-Frame-Time} on non-immutable responses.
 *   <li>The render runs on the render pool within 2 s, else {@code 503 RENDER_BUSY} with
 *       {@code Retry-After: 1}.
 * </ul>
 * Nothing here fetches anything upstream: no client request ever causes an upstream fetch.
 *
 * <p>Left out in edge mode (prompt 113: raster rendering (static maps)).
 */
@RestController
@Tag(name = "Maps")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class StaticMapController {

    private final StaticMapService maps;
    private final AlertService alerts;
    private final PlaceService places;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final Clock clock;

    /** Creates the controller. */
    public StaticMapController(StaticMapService maps, AlertService alerts, PlaceService places,
            ReadCallerResolver callers, SourceVisibility visibility, Clock clock) {
        this.maps = maps;
        this.alerts = alerts;
        this.places = places;
        this.callers = callers;
        this.visibility = visibility;
        this.clock = clock;
    }

    /** {@code getStaticMap}. */
    @GetMapping("/v1/maps/static")
    @Operation(operationId = "getStaticMap", tags = "Maps")
    @RestrictedRead("the alert is read under the caller's restriction; outside it is 404")
    public ResponseEntity<byte[]> getStaticMap(
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) String center,
            @RequestParam(required = false) String radiusKm,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String width,
            @RequestParam(required = false) String height,
            @RequestParam(required = false) String alert,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) List<String> style,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String units,
            HttpServletRequest request) {
        List<String> productIds = QueryParams.multi("products", products, StaticMapService.MAX_PRODUCTS);
        List<String> styles = ordered(style);
        Integer w = blank(width) ? null : QueryParams.intInRange("width", width, StaticMapService.MIN_WIDTH,
                ai.foresignal.raster.render.MapSpec.MAX_WIDTH, StaticMapService.DEFAULT_WIDTH);
        Integer h = blank(height) ? null : QueryParams.intInRange("height", height, StaticMapService.MIN_HEIGHT,
                ai.foresignal.raster.render.MapSpec.MAX_WIDTH, StaticMapService.MIN_HEIGHT);
        Double radius = QueryParams.doubleInRange("radiusKm", radiusKm, 1, 2000);
        boolean dark = "dark".equals(QueryParams.oneOf("theme", theme, List.of("light", "dark"), "light"));
        boolean imperial = "imperial".equals(QueryParams.oneOf("units", units, List.of("metric", "imperial"),
                "metric"));
        double[] box = blank(bbox) ? null : numbers("bbox", bbox, 4);
        double[] point = blank(center) ? null : numbers("center", center, 2);
        ReadCaller caller = callers.resolve(request);
        AlertView alertView = blank(alert) ? null : visibleAlert(alert.strip(), caller);
        PlaceView placeView = null;
        if (!blank(place)) {
            if (!ReadCallerResolver.tokenPresent(request)) {
                throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                        "A map of a place needs a token: places are never anonymous.");
            }
            placeView = places.get(place.strip());
        }
        StaticMapService.Request mapRequest = new StaticMapService.Request(productIds, styles, box, point, radius,
                blank(time) ? null : time.strip(), blank(run) ? null : run.strip(), w, h, alertView, placeView, dark,
                imperial);
        StaticMapService.Prepared prepared = maps.prepare(mapRequest, callers.excludedSources(caller), clock.instant());
        boolean publicCache = caller.publicCache() && placeView == null
                && prepared.sourceIds().stream().noneMatch(visibility::isPrivate);
        RasterCachePolicy.Decision decision = RasterCachePolicy.decide(prepared.immutable(), publicCache, null);
        HttpHeaders headers = RasterCachePolicy.headers(decision, prepared.etag(), prepared.frameTime());
        if (RasterCachePolicy.notModified(request, prepared.etag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        MapRenderer.Rendered rendered = maps.render(prepared, RasterTilesController.clientKey(request));
        headers.set(RasterCachePolicy.IMAGE_BOUNDS_HEADER, rendered.boundsHeader());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(rendered.png());
    }

    /** The alert if the caller may read it; otherwise reported like an unknown alert. */
    private AlertView visibleAlert(String alertId, ReadCaller caller) {
        long id;
        try {
            id = Long.parseLong(alertId);
        } catch (NumberFormatException e) {
            throw NotFoundException.of("Alert", alertId);
        }
        AlertView view = alerts.get(id);
        if (callers.excludedSources(caller).contains(view.sourceId())
                || !RestrictionClamp.testSnapshot(caller.restriction(), view.sourceId(), view.kind(),
                        view.recordSnapshot(), view.sensitive())) {
            throw NotFoundException.of("Alert", alertId);
        }
        return view;
    }

    /** {@code n} comma-separated finite numbers. */
    static double[] numbers(String field, String raw, int n) {
        String[] parts = raw.split(",");
        if (parts.length != n) {
            throw ValidationException.forField(field, n == 4 ? "must be minLon,minLat,maxLon,maxLat" : "must be lon,lat");
        }
        double[] out = new double[n];
        for (int k = 0; k < n; k++) {
            try {
                out[k] = Double.parseDouble(parts[k].strip());
            } catch (NumberFormatException e) {
                throw ValidationException.forField(field, "must be " + n + " numbers");
            }
            if (!Double.isFinite(out[k])) {
                throw ValidationException.forField(field, "must be " + n + " finite numbers");
            }
        }
        return out;
    }

    /** The styles in order, duplicates kept (one per product). */
    static List<String> ordered(List<String> raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        for (String entry : raw) {
            if (entry == null) {
                continue;
            }
            for (String part : entry.split(",")) {
                if (!part.isBlank()) {
                    out.add(part.strip());
                }
            }
        }
        return out;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
