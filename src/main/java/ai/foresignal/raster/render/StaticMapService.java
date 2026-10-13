package ai.foresignal.raster.render;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.FrameChoice;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns a static-map request into a {@link MapSpec} and renders it (prompt 055; Weather-Imagery-Design
 * §6.6): {@code GET /v1/maps/static}, the MCP tool {@code get_map_image} and the alert attachments all go
 * through here, so they agree on boxes, frames, styles and the cache rule.
 *
 * <ul>
 *   <li><b>Box:</b> {@code bbox}; else {@code center} with {@code radiusKm} (default {@value #DEFAULT_RADIUS_KM}
 *       km); else the alert's record geometry, centered on its envelope with the radius as a minimum; else
 *       the place, with {@code radiusKm}. A place adds its marker and range rings at half and the full
 *       radius.
 *   <li><b>Frames:</b> {@code time} is {@code latest} (default), {@code now}, an ISO-8601 time or an offset
 *       ({@link TimeSpec}), resolved per product by {@link SampleService#choose}. A missing or stale frame
 *       becomes a labelled note on the map, never an omission. Two cases are errors instead: a concrete
 *       ISO time for a forecast product without {@code run} ({@code 400 RUN_REQUIRED}), and a concrete ISO
 *       time at which an enabled product has no frame ({@code 404 UNKNOWN_FRAME}).
 *   <li><b>Cache rule</b> (design §6.1): immutable only when every frame is pinned (a concrete time, a
 *       complete current frame, a pinned run for forecasts and a versioned style for every product) and no
 *       place is drawn; otherwise a short max-age. The ETag covers the frame keys (with their content
 *       hashes), styles and the whole spec.
 * </ul>
 */
@Service
public class StaticMapService {

    /** The default radius around a center, alert or place, km. */
    public static final double DEFAULT_RADIUS_KM = 50;

    /** The default width, pixels. */
    public static final int DEFAULT_WIDTH = 800;

    /** The smallest width, pixels. */
    public static final int MIN_WIDTH = 64;

    /** The smallest height, pixels. */
    public static final int MIN_HEIGHT = 48;

    /** The most products on one map. */
    public static final int MAX_PRODUCTS = 4;

    /** Changes when the drawing changes, so ETags of old renders stop matching. */
    static final String RENDER_VERSION = "map@1";

    private static final GeoJsonReader GEOJSON = new GeoJsonReader();

    private final SampleService samples;
    private final RasterRenderService rasters;
    private final MapRenderer renderer;
    private final JsonMapper json;

    /** Creates the service. */
    public StaticMapService(SampleService samples, RasterRenderService rasters, MapRenderer renderer, JsonMapper json) {
        this.samples = samples;
        this.rasters = rasters;
        this.renderer = renderer;
        this.json = json;
    }

    /**
     * A request, as the endpoint and the MCP tool parse it.
     *
     * @param products product ids in drawing order (may be empty: a basemap-only map)
     * @param styles styles in product order, or empty for the defaults
     * @param bbox {@code w,s,e,n}, or null
     * @param center {@code lon,lat}, or null
     * @param radiusKm radius around the center, alert or place, or null
     * @param time the time expression, or null for {@code latest}
     * @param run the forecast run selector, or null
     * @param width pixels, or null for {@value #DEFAULT_WIDTH}
     * @param height pixels, or null for width × 0.75
     * @param alert the alert (already checked visible to the caller), or null
     * @param place the place (already checked visible to the caller), or null
     * @param dark the dark theme
     * @param imperial imperial units
     */
    public record Request(List<String> products, List<String> styles, double[] bbox, double[] center, Double radiusKm,
            String time, String run, Integer width, Integer height, AlertView alert, PlaceView place, boolean dark,
            boolean imperial) {

        /** Copies the lists. */
        public Request {
            products = products == null ? List.of() : List.copyOf(products);
            styles = styles == null ? List.of() : List.copyOf(styles);
        }
    }

    /**
     * A request resolved and ready to draw.
     *
     * @param spec what to draw
     * @param immutable true when the response may be cached as immutable
     * @param etag the strong ETag
     * @param frameTime the first drawn frame's valid time, or null
     * @param sourceIds the sources of the products and the alert (the cache scope depends on them)
     */
    public record Prepared(MapSpec spec, boolean immutable, String etag, Instant frameTime, Set<String> sourceIds) {}

    /**
     * Resolves a request.
     *
     * @param excludedSources sources the caller may not read
     * @throws ForesignalException {@code 404 UNKNOWN_PRODUCT}, {@code 404 UNKNOWN_FRAME},
     *     {@code 400 RUN_REQUIRED}, {@code 400 UNKNOWN_STYLE} or {@code 400 VALIDATION_FAILED}
     */
    public Prepared prepare(Request request, Set<String> excludedSources, Instant now) {
        int width = request.width() == null ? DEFAULT_WIDTH : request.width();
        if (width < MIN_WIDTH || width > MapSpec.MAX_WIDTH) {
            throw ValidationException.forField("width", "must be between " + MIN_WIDTH + " and " + MapSpec.MAX_WIDTH);
        }
        int height = request.height() == null ? MapSpec.defaultHeight(width) : request.height();
        if (height < MIN_HEIGHT || height > MapSpec.MAX_WIDTH) {
            throw ValidationException.forField("height", "must be between " + MIN_HEIGHT + " and " + MapSpec.MAX_WIDTH);
        }
        if (request.products().size() > MAX_PRODUCTS) {
            throw ValidationException.forField("products", "at most " + MAX_PRODUCTS + " products per map");
        }
        if (request.styles().size() > request.products().size()) {
            throw ValidationException.forField("style", "give at most one style per product, in products order");
        }
        TimeSpec time = TimeSpec.parse("time", request.time());
        String run = request.run() == null || request.run().isBlank() ? null : request.run().strip();

        MapSpec.Alert alert = null;
        Geometry alertGeometry = null;
        if (request.alert() != null) {
            alertGeometry = alertGeometry(request.alert(), json).orElse(null);
            if (alertGeometry != null) {
                alert = new MapSpec.Alert(alertGeometry, request.alert().severity(), request.alert().ruleName());
            }
        }
        double radius = request.radiusKm() == null ? DEFAULT_RADIUS_KM : request.radiusKm();
        if (radius < 1 || radius > 2000) {
            throw ValidationException.forField("radiusKm", "must be between 1 and 2000");
        }
        double[] box = box(request, alertGeometry, radius);
        MapSpec.Place place = null;
        List<Double> rings = List.of();
        ZoneId zone = ZoneOffset.UTC;
        if (request.place() != null) {
            Point p = point(request.place());
            place = new MapSpec.Place(request.place().id(), request.place().name(), p.getX(), p.getY());
            rings = List.of(round1(radius / 2), round1(radius));
            zone = zone(request.place().timezone());
        }

        List<MapSpec.Layer> layers = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        boolean pinned = time.kind() == TimeSpec.Kind.AT && request.place() == null;
        List<String> etagParts = new ArrayList<>();
        etagParts.add(RENDER_VERSION);
        Instant frameTime = null;
        List<String> ids = request.products().stream().distinct().toList();
        for (int k = 0; k < ids.size(); k++) {
            ProductRegistry.Registered product = samples.visible(ids.get(k), excludedSources);
            ProductSpec spec = product.spec();
            RasterRenderService.requireRendered(spec);
            String styleParam = k < request.styles().size() ? request.styles().get(k) : null;
            StyleRegistry.Resolved style = rasters.style(spec, styleParam);
            if (spec.isForecast() && time.kind() == TimeSpec.Kind.AT && run == null) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.RUN_REQUIRED, "Product " + spec.id()
                        + " is a forecast; a concrete time needs run=latest, previous or a run time.");
            }
            FrameChoice choice = samples.choose(product, time, run, now);
            boolean disabled = samples.unavailableReason(product).isPresent();
            if (!choice.hasFrame() && time.kind() == TimeSpec.Kind.AT && !disabled) {
                throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                        "Product " + spec.id() + " has no frame at or before " + time.target(now) + ".");
            }
            MapSpec.FrameStatus status = !choice.hasFrame() ? MapSpec.FrameStatus.UNAVAILABLE
                    : choice.status() == null ? MapSpec.FrameStatus.OK : MapSpec.FrameStatus.STALE;
            RasterFrameRow frame = choice.frame();
            layers.add(new MapSpec.Layer(spec, style, frame, status, choice.text()));
            sources.add(product.sourceId());
            boolean styleVersioned = styleParam != null && !styleParam.isBlank() && style.explicitVersion();
            boolean runPinned = !spec.isForecast() || run != null && !FrameResolver.RUN_LATEST.equals(run)
                    && !FrameResolver.RUN_PREVIOUS.equals(run);
            pinned &= frame != null && frame.complete() && status == MapSpec.FrameStatus.OK && styleVersioned
                    && runPinned;
            etagParts.add(spec.id());
            etagParts.add(frame == null ? "none:" + choice.text() : frame.frameKey() + "|" + frame.status() + "|"
                    + (frame.referenceTime() == null ? "" : FrameKeys.runIso(frame.referenceTime())));
            etagParts.add(status.name());
            etagParts.add(style.ref());
            if (frameTime == null && frame != null) {
                frameTime = frame.validTime();
            }
        }
        if (request.alert() != null) {
            sources.add(request.alert().sourceId());
        }
        MapSpec spec = new MapSpec(box, width, height, layers, alert, place, rings, request.dark(), request.imperial(),
                zone);
        etagParts.add(LoopService.boxText(box));
        etagParts.add(width + "x" + height);
        etagParts.add(request.dark() ? "dark" : "light");
        etagParts.add(request.imperial() ? "imperial" : "metric");
        etagParts.add(alert == null ? "-" : request.alert().id() + ":" + request.alert().severity() + ":"
                + alertGeometry.toText().hashCode());
        etagParts.add(request.place() == null ? "-" : request.place().id() + "@" + request.place().version());
        etagParts.add(zone.getId());
        if (!pinned) {
            // The time expression is part of what was asked; the frame keys above carry what it resolved to.
            etagParts.add(time.text());
        }
        String etag = RasterCachePolicy.etag(etagParts.toArray(String[]::new));
        return new Prepared(spec, pinned, etag, frameTime, Set.copyOf(sources));
    }

    /**
     * Renders a prepared map on the render pool.
     *
     * @throws ForesignalException {@code 503 RENDER_BUSY}
     */
    public MapRenderer.Rendered render(Prepared prepared, String client) {
        return renderer.render(prepared.spec(), prepared.etag(), client);
    }

    /** Draws a spec on the calling thread (alert attachments, which run off the request path). */
    public MapRenderer.Rendered draw(MapSpec spec) {
        return renderer.draw(spec);
    }

    /** The box of a request (see the class comment). */
    static double[] box(Request request, Geometry alertGeometry, double radius) {
        boolean hasBox = request.bbox() != null;
        boolean hasCenter = request.center() != null;
        if (hasBox && hasCenter) {
            throw ValidationException.forField("bbox", "give bbox or center, not both");
        }
        if (hasBox) {
            if (request.radiusKm() != null) {
                throw ValidationException.forField("radiusKm", "applies to center, alert or place, not bbox");
            }
            return checkBox(request.bbox());
        }
        if (hasCenter) {
            double[] c = request.center();
            SampleService.checkPoint(c[0], c[1]);
            return around(c[0], c[1], radius);
        }
        if (alertGeometry != null) {
            Envelope e = alertGeometry.getEnvelopeInternal();
            double cx = (e.getMinX() + e.getMaxX()) / 2;
            double cy = (e.getMinY() + e.getMaxY()) / 2;
            double[] b = around(cx, cy, radius);
            double mx = (e.getMaxX() - e.getMinX()) * 0.1;
            double my = (e.getMaxY() - e.getMinY()) * 0.1;
            return new double[] {Math.max(-180, Math.min(b[0], e.getMinX() - mx)), Math.max(-90, Math.min(b[1],
                e.getMinY() - my)), Math.min(180, Math.max(b[2], e.getMaxX() + mx)), Math.min(90, Math.max(b[3],
                e.getMaxY() + my))};
        }
        if (request.place() != null) {
            Point p = point(request.place());
            return around(p.getX(), p.getY(), radius);
        }
        if (request.alert() != null) {
            throw ValidationException.forField("alert", "the alert has no geometry; give bbox, center or place");
        }
        throw ValidationException.forField("bbox", "give bbox, center (with radiusKm), alert or place");
    }

    /**
     * Checks a {@code w,s,e,n} box.
     *
     * @throws ValidationException (400 {@code VALIDATION_FAILED}) for a box outside WGS84 or empty
     */
    public static double[] checkBox(double[] b) {
        if (b == null || b.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        for (double v : b) {
            if (!Double.isFinite(v)) {
                throw ValidationException.forField("bbox", "must be four finite numbers");
            }
        }
        if (b[0] < -180 || b[2] > 180 || b[0] > 180 || b[2] < -180 || b[1] < -90 || b[3] > 90 || b[1] >= b[3]
                || b[0] == b[2]) {
            throw ValidationException.forField("bbox", "must be a WGS84 box with minLat < maxLat");
        }
        return b.clone();
    }

    /** A box of {@code km} around a point (spherical degrees, clamped to the globe). */
    public static double[] around(double lon, double lat, double km) {
        double dLat = km / 111.32;
        double dLon = km / (111.32 * Math.max(0.01, Math.cos(Math.toRadians(lat))));
        return new double[] {Math.max(-180, lon - dLon), Math.max(-90, lat - dLat), Math.min(180, lon + dLon),
            Math.min(90, lat + dLat)};
    }

    /** The geometry in an alert's record snapshot ({@code record_snapshot.geometry}), if any. */
    public static Optional<Geometry> alertGeometry(AlertView alert, JsonMapper json) {
        if (alert == null || alert.recordSnapshot() == null) {
            return Optional.empty();
        }
        try {
            JsonNode node = json.readTree(alert.recordSnapshot());
            JsonNode geometry = node == null ? null : node.get("geometry");
            if (geometry == null || !geometry.isObject()) {
                return Optional.empty();
            }
            Geometry g = GEOJSON.read(geometry);
            return g == null || g.isEmpty() ? Optional.empty() : Optional.of(g);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Point point(PlaceView place) {
        Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
        return g instanceof Point p ? p : g.getInteriorPoint();
    }

    private static ZoneId zone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException e) {
            return ZoneOffset.UTC;
        }
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
