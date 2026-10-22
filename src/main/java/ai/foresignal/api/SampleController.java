package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.sample.AreaStatsService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.SampleStatus;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Duration;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Point, batch, series and area sampling of stored raster frames (OpenAPI tag {@code Sampling}, prompt 052;
 * Weather-Imagery-Design §6.5): {@code getSample}, {@code postSample}, {@code getProductSeries} and
 * {@code postProductStats}, all {@code read} (the POSTs read too). A {@code place} needs a token even while
 * {@code openReads} is on: places are never anonymous (prompt 036). Sampling never triggers an upstream
 * fetch; every value carries its valid time, age, run and status, and anything but {@code ok} is unknown,
 * never clear.
 */
@RestController
@Tag(name = "Sampling")
public class SampleController {

    private static final List<String> UNITS = List.of("metric", "imperial");

    /** The oldest live position still accepted before a sample is {@code stale-position}, minutes. */
    static final long STALE_POSITION_MINUTES = 15;

    private final SampleService samples;
    private final AreaStatsService stats;
    private final PlaceService places;
    private final ReadCallerResolver callers;
    private final RecordQueryService records;
    private final TrackService tracks;

    /** Creates the controller. */
    public SampleController(SampleService samples, AreaStatsService stats, PlaceService places,
            ReadCallerResolver callers, RecordQueryService records, TrackService tracks) {
        this.samples = samples;
        this.stats = stats;
        this.places = places;
        this.callers = callers;
        this.records = records;
        this.tracks = tracks;
    }

    /** {@code getSample}. */
    @GetMapping("/v1/sample")
    @Operation(operationId = "getSample", tags = "Sampling")
    public Map<String, Object> getSample(
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String units,
            @RequestParam(required = false) String record,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        List<String> ids = QueryParams.multi("products", products, SampleService.MAX_PRODUCTS);
        if (record != null && !record.isBlank()) {
            RecordPosition position = recordPosition(record, caller);
            SampleService.Point point = new SampleService.Point(position.lon(), position.lat(), null, ZoneOffset.UTC);
            Map<String, Object> out = samples.sample(point, ids, TimeSpec.parse("time", time), null, imperial(units),
                    callers.excludedSources(caller));
            boolean stale = position.stale(samples.now());
            out.put("position", position.toMap());
            if (stale) {
                markStalePosition(out);
            }
            return out;
        }
        SampleService.Point point = point(lon, lat, place, request);
        return samples.sample(point, ids, TimeSpec.parse("time", time), null, imperial(units),
                callers.excludedSources(caller));
    }

    /**
     * {@code sampleTrack} (prompt 106): each point sampled at its own time, with per-point statuses. The
     * points come from the body, or from the 037 track of a {@code record}.
     */
    @PostMapping("/v1/sample/track")
    @Operation(operationId = "sampleTrack", tags = "Sampling")
    public Map<String, Object> postSampleTrack(@RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a SampleTrackRequest object");
        }
        List<String> products = strings(body.get("products"), "products");
        boolean imperial = imperial(text(body.get("units")));
        String run = text(body.get("run"));
        Duration maxGap = duration(body.get("maxGap"));
        String record = text(body.get("record"));
        boolean hasPoints = body.has("points") && !body.get("points").isNull();
        if (record != null && hasPoints) {
            throw ValidationException.forField("record", "give record, or points, not both");
        }
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        List<SampleService.TrackPoint> points;
        if (record != null) {
            points = trackPoints(record, body, caller, excluded);
        } else {
            points = requestPoints(body.get("points"));
        }
        Map<String, Object> out = new LinkedHashMap<>(samples.sampleTrack(points, products, maxGap, run, imperial,
                excluded));
        if (record != null) {
            out.put("record", record);
        }
        return out;
    }

    /** The points of a {@code points[]} body array. */
    private static List<SampleService.TrackPoint> requestPoints(JsonNode pointsNode) {
        if (pointsNode == null || !pointsNode.isArray() || pointsNode.isEmpty()) {
            throw ValidationException.forField("points", "name at least one point");
        }
        if (pointsNode.size() > SampleService.MAX_TRACK_POINTS) {
            throw ValidationException.forField("points",
                    "at most " + SampleService.MAX_TRACK_POINTS + " points per track");
        }
        Instant now = Instant.now();
        List<SampleService.TrackPoint> out = new ArrayList<>();
        int index = 0;
        for (JsonNode p : pointsNode) {
            if (p == null || !p.isObject() || !p.has("lon") || !p.has("lat")) {
                throw ValidationException.forField("points[" + index + "]", "must be {lon, lat, time?}");
            }
            double lon = QueryParams.doubleInRange("points[" + index + "].lon", p.get("lon").asString(), -180, 180);
            double lat = QueryParams.doubleInRange("points[" + index + "].lat", p.get("lat").asString(), -90, 90);
            Instant time = text(p.get("time")) == null ? now : QueryParams.instant("points[" + index + "].time",
                    text(p.get("time")));
            out.add(new SampleService.TrackPoint(lon, lat, time));
            index++;
        }
        return out;
    }

    /** The points of a record's 037 track, simplified to at most {@code MAX_TRACK_POINTS}. */
    private List<SampleService.TrackPoint> trackPoints(String record, JsonNode body, ReadCaller caller,
            Set<String> excluded) {
        RecordRef ref = RecordRef.parse(record);
        TrackQuery query = new TrackQuery(ref.sourceId(), ref.externalId(),
                QueryParams.instant("from", text(body.get("from"))), QueryParams.instant("to", text(body.get("to"))),
                null, SampleService.MAX_TRACK_POINTS, null, true, List.of(), excluded, caller.restriction());
        TrackResult result = tracks.track(query);
        List<SampleService.TrackPoint> out = new ArrayList<>();
        List<List<double[]>> coordinates = result.coordinates();
        List<List<Long>> times = result.times();
        for (int s = 0; s < coordinates.size(); s++) {
            List<double[]> segment = coordinates.get(s);
            List<Long> segmentTimes = s < times.size() ? times.get(s) : List.of();
            for (int k = 0; k < segment.size(); k++) {
                double[] c = segment.get(k);
                Instant time = k < segmentTimes.size() ? Instant.ofEpochMilli(segmentTimes.get(k)) : samples.now();
                out.add(new SampleService.TrackPoint(c[0], c[1], time));
                if (out.size() >= SampleService.MAX_TRACK_POINTS) {
                    return out;
                }
            }
        }
        if (out.isEmpty()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_RECORD,
                    "record " + record + " has no track points");
        }
        return out;
    }

    /** A parsed {@code sourceId/externalId}. */
    record RecordRef(String sourceId, String externalId) {

        static RecordRef parse(String record) {
            int slash = record == null ? -1 : record.indexOf('/');
            if (slash <= 0 || slash == record.length() - 1) {
                throw ValidationException.forField("record", "must be {sourceId}/{externalId}");
            }
            return new RecordRef(record.substring(0, slash), record.substring(slash + 1));
        }
    }

    /** A live position of a record. */
    record RecordPosition(double lon, double lat, Instant observedAt) {

        boolean stale(Instant now) {
            return observedAt != null
                    && Duration.between(observedAt, now).compareTo(Duration.ofMinutes(STALE_POSITION_MINUTES)) > 0;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("lon", lon);
            m.put("lat", lat);
            m.put("observedAt", observedAt == null ? null : observedAt.toString());
            m.put("ageSeconds", observedAt == null ? null : Duration.between(observedAt, Instant.now()).toSeconds());
            return m;
        }
    }

    /** The live position of a record (head ⋈ live), honoring the private/restriction gate. */
    RecordPosition recordPosition(String record, ReadCaller caller) {
        RecordRef ref = RecordRef.parse(record.strip());
        ReadRestriction restriction = caller.restriction();
        RecordRow row = records.get(ref.sourceId(), ref.externalId(), false, FieldSelection.ALL,
                GeometryOptions.DEFAULT, callers.excludedSources(caller), restriction)
                .orElseThrow(() -> new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_RECORD,
                        "no live record " + record));
        if (row.geometry() == null || row.geometry().isBlank()) {
            throw ValidationException.forField("record", record + " has no position");
        }
        Geometry geometry;
        try {
            geometry = Geo.fromGeoJson(row.geometry());
        } catch (UpstreamFormatException e) {
            throw ValidationException.forField("record", record + " has no readable position");
        }
        Point p = geometry instanceof Point point ? point : geometry.getCentroid();
        return new RecordPosition(p.getX(), p.getY(), row.observedAt());
    }

    /** Marks every value of a sample {@code stale-position}. */
    @SuppressWarnings("unchecked")
    static void markStalePosition(Map<String, Object> out) {
        Object values = out.get("values");
        if (values instanceof List<?> list) {
            for (Object v : list) {
                if (v instanceof Map<?, ?> raw) {
                    Map<String, Object> value = (Map<String, Object>) raw;
                    value.put("status", SampleStatus.STALE_POSITION.wireValue());
                    value.put("statusText", SampleStatus.STALE_POSITION.defaultText());
                }
            }
        }
    }

    /** A {@code maxGap} duration from a body value, or null. */
    private static Duration duration(JsonNode node) {
        String text = text(node);
        if (text == null) {
            return null;
        }
        try {
            return Duration.parse(text);
        } catch (RuntimeException e) {
            throw ValidationException.forField("maxGap", "must be an ISO-8601 duration");
        }
    }

    /** {@code postSample}: up to 100 points, results in request order. */
    @PostMapping("/v1/sample")
    @Operation(operationId = "postSample", tags = "Sampling")
    public Map<String, Object> postSample(@RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a SampleRequest object");
        }
        JsonNode pointsNode = body.get("points");
        if (pointsNode == null || !pointsNode.isArray() || pointsNode.isEmpty()) {
            throw ValidationException.forField("points", "name at least one point");
        }
        if (pointsNode.size() > SampleService.MAX_POINTS) {
            throw ValidationException.forField("points", "at most " + SampleService.MAX_POINTS + " points per request");
        }
        List<String> products = strings(body.get("products"), "products");
        TimeSpec time = TimeSpec.parse("time", text(body.get("time")));
        boolean imperial = imperial(text(body.get("units")));
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        samples.resolveProducts(products, excluded);
        List<Map<String, Object>> items = new ArrayList<>();
        int index = 0;
        for (JsonNode p : pointsNode) {
            if (p == null || !p.isObject()) {
                throw ValidationException.forField("points[" + index + "]", "must be {lon, lat} or {place}");
            }
            String placeId = text(p.get("place"));
            String lonText = p.has("lon") ? p.get("lon").asString() : null;
            String latText = p.has("lat") ? p.get("lat").asString() : null;
            items.add(samples.sample(point(lonText, latText, placeId, request), products, time, null, imperial, excluded));
            index++;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("serverTime", samples.now().toString());
        return out;
    }

    /** {@code getProductSeries}: at most 500 steps, observed past plus forecast future. */
    @GetMapping("/v1/products/{productId}/series")
    @Operation(operationId = "getProductSeries", tags = "Sampling")
    public Map<String, Object> getProductSeries(
            @PathVariable String productId,
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String run,
            @RequestParam(required = false) String units,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = samples.visible(productId, callers.excludedSources(caller));
        SampleService.Point point = point(lon, lat, place, request);
        Instant f = QueryParams.instant("from", from);
        Instant t = QueryParams.instant("to", to);
        return samples.series(product, point, f, t, run == null || run.isBlank() ? null : run.strip(), imperial(units));
    }

    /** {@code postProductStats}: one of geometry, place or bbox. */
    @PostMapping("/v1/products/{productId}/stats")
    @Operation(operationId = "postProductStats", tags = "Sampling")
    public Map<String, Object> postProductStats(@PathVariable String productId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a StatsRequest object");
        }
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = samples.visible(productId, callers.excludedSources(caller));
        Geometry area = statsArea(body, request);
        List<Double> thresholds = new ArrayList<>();
        JsonNode t = body.get("thresholds");
        if (t != null && !t.isNull()) {
            if (!t.isArray()) {
                throw ValidationException.forField("thresholds", "must be an array of numbers");
            }
            for (JsonNode n : t) {
                if (!n.isNumber()) {
                    throw ValidationException.forField("thresholds", "must be an array of numbers");
                }
                thresholds.add(n.asDouble());
            }
        }
        String run = text(body.get("run"));
        return stats.stats(product, area, TimeSpec.parse("time", text(body.get("time"))), run, thresholds,
                imperial(text(body.get("units"))));
    }

    // ------------------------------------------------------------------ helpers

    /** The geometry of a stats request: exactly one of geometry, place and bbox. */
    Geometry statsArea(JsonNode body, HttpServletRequest request) {
        JsonNode geometry = body.get("geometry");
        String placeId = text(body.get("place"));
        String bbox = text(body.get("bbox"));
        int given = (geometry != null && !geometry.isNull() ? 1 : 0) + (placeId != null ? 1 : 0) + (bbox != null ? 1 : 0);
        if (given != 1) {
            throw ValidationException.forField("geometry", "give exactly one of geometry, place and bbox");
        }
        if (placeId != null) {
            return requirePlace(placeId, request).area();
        }
        if (bbox != null) {
            String[] parts = bbox.split(",");
            if (parts.length != 4) {
                throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
            }
            double[] v = new double[4];
            for (int i = 0; i < 4; i++) {
                try {
                    v[i] = Double.parseDouble(parts[i].strip());
                } catch (NumberFormatException e) {
                    throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
                }
            }
            if (v[0] >= v[2] || v[1] >= v[3] || v[0] < -180 || v[2] > 180 || v[1] < -90 || v[3] > 90) {
                throw ValidationException.forField("bbox", "must be a valid minLon,minLat,maxLon,maxLat box");
            }
            return Geo.factory().toGeometry(new org.locationtech.jts.geom.Envelope(v[0], v[2], v[1], v[3]));
        }
        try {
            return Geo.fromGeoJson(geometry.toString());
        } catch (UpstreamFormatException e) {
            throw ValidationException.forField("geometry", e.getMessage());
        }
    }

    /** A sample point from lon/lat or a place. */
    SampleService.Point point(String lon, String lat, String placeId, HttpServletRequest request) {
        boolean hasPlace = placeId != null && !placeId.isBlank();
        boolean hasLonLat = (lon != null && !lon.isBlank()) || (lat != null && !lat.isBlank());
        if (hasPlace && hasLonLat) {
            throw ValidationException.forField("place", "give lon and lat, or place, not both");
        }
        if (hasPlace) {
            PlaceView place = requirePlace(placeId.strip(), request);
            Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
            Point p = g instanceof Point point ? point : g.getInteriorPoint();
            return new SampleService.Point(p.getX(), p.getY(), place.id(), zone(place.timezone()));
        }
        if (lon == null || lon.isBlank() || lat == null || lat.isBlank()) {
            throw ValidationException.forField("lon", "give lon and lat, or place");
        }
        Double x = QueryParams.doubleInRange("lon", lon, -180, 180);
        Double y = QueryParams.doubleInRange("lat", lat, -90, 90);
        return new SampleService.Point(x, y, null, ZoneOffset.UTC);
    }

    /** A place, which needs a token (401) and must exist (404). */
    PlaceView requirePlace(String placeId, HttpServletRequest request) {
        if (!ReadCallerResolver.tokenPresent(request)) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Sampling a place needs a token: places are never anonymous.");
        }
        return places.find(placeId).orElseThrow(() -> SampleService.unknownPlace(placeId));
    }

    /** The zone of a place, else UTC. */
    static ZoneId zone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException e) {
            return ZoneOffset.UTC;
        }
    }

    private static boolean imperial(String units) {
        return "imperial".equals(QueryParams.oneOf("units", units, UNITS, "metric"));
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String s = node.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static List<String> strings(JsonNode node, String field) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            throw ValidationException.forField(field, "name at least one product");
        }
        List<String> out = new ArrayList<>();
        for (JsonNode n : node) {
            if (!n.isString()) {
                throw ValidationException.forField(field, "must be an array of product ids");
            }
            out.add(n.asString());
        }
        return out;
    }
}
