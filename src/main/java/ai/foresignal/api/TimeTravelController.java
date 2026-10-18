package ai.foresignal.api;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.RawJson;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.api.support.TimeTravelCache;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.frames.FrameItem;
import ai.foresignal.store.frames.FramesQuery;
import ai.foresignal.store.frames.HistoryFrames;
import ai.foresignal.store.frames.HistoryFramesService;
import ai.foresignal.store.timeline.Timeline;
import ai.foresignal.store.timeline.TimelineService;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * Time travel (prompt 037; OpenAPI tag {@code History}): {@code getRecordTrack},
 * {@code getHistoryFrames} and {@code getTimeline}. asOf tiles live in {@link TilesController}.
 *
 * <p>Every operation needs the {@code read} scope (open while {@code security.openReads} is on),
 * honors the caller's restriction and the {@code place=}/{@code view=}/{@code inWatchlist=} filters
 * (prompts 035, 036), and never shows private sources to callers that fail the private gate.
 */
@RestController
@Tag(name = "History")
public class TimeTravelController {

    /** The GeoJSON media type of tracks. */
    public static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    /** The CZML media type of {@code format=czml} tracks (prompt 141). */
    public static final MediaType CZML = MediaType.parseMediaType(ai.foresignal.czml.CzmlWriter.MEDIA_TYPE);

    /** The ndjson media type of frames. */
    public static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson");

    /** {@code Vary} of tracks. */
    static final String VARY = "Accept-Encoding, Authorization";

    private final TrackService tracks;
    private final HistoryFramesService frames;
    private final TimelineService timelines;
    private final ReadCallerResolver callers;
    private final SourceRegistry registry;
    private final JsonMapper json;

    /** Builds CZML tracks (prompt 141); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.czml.CzmlService czml;

    /** Creates the controller. */
    public TimeTravelController(TrackService tracks, HistoryFramesService frames, TimelineService timelines,
            ReadCallerResolver callers, SourceRegistry registry, JsonMapper json) {
        this.tracks = tracks;
        this.frames = frames;
        this.timelines = timelines;
        this.callers = callers;
        this.registry = registry;
        this.json = json;
    }

    // ------------------------------------------------------------------ getRecordTrack

    /**
     * {@code getRecordTrack}: the simplified track of one entity as a GeoJSON {@code Feature} with
     * {@code MultiLineString} geometry, or with {@code format=czml} (prompt 141) a CZML document whose clock spans
     * the window and whose one entity carries the samples, the current title and the kind's color. Strong ETag;
     * {@code immutable} caching only for closed past windows ({@link TimeTravelCache}, keyed on {@code to}).
     */
    @GetMapping("/v1/records/{sourceId}/{externalId}/track")
    @Operation(operationId = "getRecordTrack", tags = "History")
    @RestrictedRead("versions outside the restriction break the track; an entity wholly outside is 404")
    public ResponseEntity<String> getRecordTrack(
            @PathVariable String sourceId,
            @PathVariable String externalId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String tolerance,
            @RequestParam(required = false) String maxPoints,
            @RequestParam(required = false) String gap,
            @RequestParam(required = false) String includeLive,
            @RequestParam(required = false) List<String> fields,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        String fmt = QueryParams.oneOf("format", format, List.of("json", "geojson", "czml"), "json");
        if ("czml".equals(fmt) && czml == null) {
            throw ValidationException.forField("format", "czml is not available in this context");
        }
        ReadCaller caller = callers.resolveScoped(request);
        TrackQuery query = new TrackQuery(sourceId, externalId, QueryParams.instant("from", from),
                QueryParams.instant("to", to), tolerance(tolerance),
                QueryParams.intInRange("maxPoints", maxPoints, 2, TrackService.MAX_POINTS, TrackService.DEFAULT_MAX_POINTS),
                duration("gap", gap), QueryParams.bool("includeLive", includeLive, true),
                QueryParams.multi("fields", fields, TrackService.MAX_FIELDS), callers.excludedSources(caller),
                caller.restriction());
        TrackResult track = tracks.track(query);
        boolean asCzml = "czml".equals(fmt);
        String body = asCzml
                // format=czml (prompt 141): the document packet over [from, to] and one entity packet.
                ? json.writeValueAsString(czml.trackDocument(track, czml.currentTitle(sourceId, externalId,
                        callers.excludedSources(caller), caller.restriction()), null))
                : json.writeValueAsString(feature(track));
        String etag = ETags.forDocument(body);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(TimeTravelCache.cacheControl(track.immutable(),
                caller.publicCache() && track.publicAllowed()));
        headers.set(HttpHeaders.VARY, VARY);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(asCzml ? CZML : GEO_JSON).body(body);
    }

    /** The {@code Track} feature of {@code track}. */
    static Map<String, Object> feature(TrackResult track) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("sourceId", track.sourceId());
        properties.put("externalId", track.externalId());
        properties.put("kind", track.kind());
        properties.put("from", track.from().toString());
        properties.put("to", track.to().toString());
        properties.put("times", track.times());
        properties.put("values", track.values());
        properties.put("live", track.live());
        properties.put("pointsScanned", track.pointsScanned());
        properties.put("pointsReturned", track.pointsReturned());
        properties.put("toleranceMeters", track.toleranceMeters());
        properties.put("segments", track.segments());
        properties.put("approximate", track.approximate());
        properties.put("downsampled", track.downsampled());
        properties.put("truncated", track.truncated());
        properties.put("watermarkSeq", track.watermarkSeq());
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        if (track.coordinates().isEmpty()) {
            feature.put("geometry", null);
        } else {
            Map<String, Object> geometry = new LinkedHashMap<>();
            geometry.put("type", "MultiLineString");
            geometry.put("coordinates", track.coordinates());
            feature.put("geometry", geometry);
        }
        feature.put("properties", properties);
        return feature;
    }

    // ------------------------------------------------------------------ getHistoryFrames

    /**
     * {@code getHistoryFrames}: the asOf keyframe at {@code from} plus one delta frame per
     * {@code step} up to {@code to}, as JSON or ndjson (the keyframe line first, then one line per
     * frame).
     */
    @GetMapping("/v1/history/frames")
    @Operation(operationId = "getHistoryFrames", tags = "History")
    @RestrictedRead("restriction and place= judge each version inside the SQL; leaving it is a 'left' removal")
    public ResponseEntity<Object> getHistoryFrames(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String step,
            @RequestParam(required = false) String maxFrames,
            @RequestParam(required = false) String geometry,
            @RequestParam(required = false) String format,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String fmt = QueryParams.oneOf("format", format, List.of("json", "ndjson"), "json");
        String shape = QueryParams.oneOf("geometry", geometry, List.of("point", "full"), "point");
        ReadCaller caller = callers.resolveScoped(request);
        ReadRestriction restriction = caller.restriction();
        List<String> sources = knownSources(QueryParams.multi("source", source), callers.hiddenSources(caller));
        List<String> kinds = QueryParams.multi("kind", kind);
        // An absent filter becomes the restriction's (frames need a source or kind filter).
        if (sources.isEmpty() && kinds.isEmpty()) {
            sources = Objects.requireNonNullElse(RestrictionClamp.clampSources(sources, restriction), sources);
            kinds = Objects.requireNonNullElse(RestrictionClamp.clampKinds(kinds, restriction), kinds);
        }
        Instant fromAt = required("from", QueryParams.instant("from", from));
        Instant toAt = required("to", QueryParams.instant("to", to));
        Duration stepValue = required("step", duration("step", step));
        int frameCap = QueryParams.intInRange("maxFrames", maxFrames, 1, HistoryFramesService.MAX_FRAMES,
                HistoryFramesService.MAX_FRAMES);
        HistoryFrames result = frames.frames(new FramesQuery(sources, kinds, BboxParser.parse(bbox), fromAt, toAt,
                stepValue, frameCap, "full".equals(shape), callers.excludedSources(caller), restriction));
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache());
        if ("ndjson".equals(fmt)) {
            response.setStatus(200);
            response.setContentType(NDJSON.toString());
            headers.forEach((name, values) -> values.forEach(v -> response.addHeader(name, v)));
            try (OutputStream out = response.getOutputStream()) {
                Map<String, Object> head = envelope(result);
                head.remove("frames");
                writeLine(out, head);
                for (HistoryFrames.Frame frame : result.frames()) {
                    writeLine(out, frame(frame));
                }
            }
            return null;
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON).body(envelope(result));
    }

    private void writeLine(OutputStream out, Object value) throws IOException {
        out.write(json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8));
        out.write('\n');
    }

    /** The {@code HistoryFrames} envelope. */
    static Map<String, Object> envelope(HistoryFrames result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("asOf", result.asOf().toString());
        body.put("step", Durations.format(result.step()));
        body.put("watermarkSeq", result.watermarkSeq());
        body.put("horizon", result.horizon() == null ? null : result.horizon().toString());
        body.put("approximate", result.approximate());
        body.put("keyframe", items(result.keyframe()));
        List<Map<String, Object>> list = new ArrayList<>();
        for (HistoryFrames.Frame frame : result.frames()) {
            list.add(frame(frame));
        }
        body.put("frames", list);
        return body;
    }

    /** One {@code HistoryFrame}. */
    static Map<String, Object> frame(HistoryFrames.Frame frame) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("t", frame.t().toString());
        out.put("upserts", items(frame.upserts()));
        List<Map<String, Object>> removals = new ArrayList<>();
        for (HistoryFrames.Removal removal : frame.removals()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", removal.id());
            r.put("reason", removal.reason());
            r.put("left", removal.left());
            removals.add(r);
        }
        out.put("removals", removals);
        return out;
    }

    private static List<Map<String, Object>> items(List<FrameItem> items) {
        List<Map<String, Object>> out = new ArrayList<>(items.size());
        for (FrameItem item : items) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", item.id());
            m.put("kind", item.kind());
            m.put("title", item.title());
            m.put("observedAt", item.observedAt().toString());
            m.put("version", item.version());
            if (item.geometry() != null) {
                m.put("geometry", RawJson.of(item.geometry()));
            } else {
                m.put("lon", item.lon());
                m.put("lat", item.lat());
            }
            m.put("props", RawJson.of(item.props() == null ? "{}" : item.props()));
            out.add(m);
        }
        return out;
    }

    // ------------------------------------------------------------------ getTimeline

    /** {@code getTimeline}: the event-density histogram for scrubbers. */
    @GetMapping("/v1/timeline")
    @Operation(operationId = "getTimeline", tags = "History")
    @RestrictedRead("restriction and place= are part of the count SQL")
    public ResponseEntity<Object> getTimeline(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String timeField,
            @RequestParam(required = false) String measure,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String includeKeyframes,
            @RequestParam(required = false) String includeAlerts,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        ReadRestriction restriction = caller.restriction();
        List<String> sources = knownSources(QueryParams.multi("source", source), callers.hiddenSources(caller));
        List<String> kinds = QueryParams.multi("kind", kind);
        BboxFilter box = BboxParser.parse(bbox);
        Instant fromAt = required("from", QueryParams.instant("from", from));
        Instant toAt = required("to", QueryParams.instant("to", to));
        Duration width = bucket == null || bucket.isBlank() || "auto".equalsIgnoreCase(bucket.strip())
                ? null
                : duration("bucket", bucket);
        TimeField axis = TimeField.parse(timeField, TimeField.OBSERVED);
        String measureValue = QueryParams.oneOf("measure", measure, List.of("versions", "entities"), "versions");
        String group = QueryParams.oneOf("groupBy", groupBy, List.of("none", "kind", "source"), "none");
        Timeline timeline = timelines.timeline(sources, kinds, box, fromAt, toAt, width, axis, measureValue, group,
                QueryParams.bool("includeKeyframes", includeKeyframes, false),
                QueryParams.bool("includeAlerts", includeAlerts, false), callers.excludedSources(caller), restriction);
        return ResponseEntity.ok()
                .headers(ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(timeline(timeline));
    }

    /** The {@code Timeline} document. */
    static Map<String, Object> timeline(Timeline t) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", t.from().toString());
        body.put("to", t.to().toString());
        body.put("bucket", Durations.format(t.bucket()));
        body.put("timeField", t.timeField());
        body.put("measure", t.measure());
        body.put("groupBy", t.groupBy());
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (Timeline.Bucket b : t.buckets()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("t", b.t().toString());
            m.put("total", b.total());
            m.put("counts", b.counts());
            buckets.add(m);
        }
        body.put("buckets", buckets);
        if (t.alerts() != null) {
            List<Map<String, Object>> alerts = new ArrayList<>();
            for (Timeline.AlertBucket a : t.alerts()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("t", a.t().toString());
                m.put("info", a.info());
                m.put("warning", a.warning());
                m.put("critical", a.critical());
                alerts.add(m);
            }
            body.put("alerts", alerts);
        }
        body.put("maxBucket", t.maxBucket());
        body.put("approximate", t.approximate());
        body.put("retainedFrom", t.retainedFrom() == null ? null : t.retainedFrom().toString());
        body.put("backend", t.backend());
        return body;
    }

    // ------------------------------------------------------------------ parsing

    /** {@code tolerance}: {@code auto} (null) or a non-negative number of meters. */
    static Double tolerance(String raw) {
        if (raw == null || raw.isBlank() || "auto".equalsIgnoreCase(raw.strip())) {
            return null;
        }
        return QueryParams.doubleInRange("tolerance", raw, 0, 1_000_000);
    }

    /** A duration ({@code 10m}, {@code PT10M}), or null when absent. */
    static Duration duration(String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            Duration value = Durations.parse(raw.strip());
            if (value.isZero() || value.isNegative()) {
                throw ValidationException.forField(name, "must be a positive duration");
            }
            return value;
        } catch (InvalidInputException e) {
            throw ValidationException.forField(name, "'" + raw + "' is not a duration such as 10m or PT10M");
        }
    }

    private static <T> T required(String name, T value) {
        if (value == null) {
            throw ValidationException.forField(name, "is required");
        }
        return value;
    }

    private List<String> knownSources(List<String> sources, Set<String> hidden) {
        for (String id : sources) {
            registry.descriptor(id);
            if (hidden.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        return sources;
    }
}
