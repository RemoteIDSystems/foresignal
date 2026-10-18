package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.czml.CzmlService;
import ai.foresignal.czml.CzmlWriter;
import ai.foresignal.media.MediaDocuments;
import ai.foresignal.media.MediaRow;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpTimes;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.Scope;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_track} (read, prompt 037): the simplified path of one entity over a window, through the
 * same {@link TrackService} as {@code GET /v1/records/{sourceId}/{externalId}/track}, in a compact
 * form ({@code [lon, lat, epochSeconds]} per point, 5 decimals) plus a summary: distance in km,
 * duration, bounding box, the highest speed between two returned points, the segment count and
 * {@code approximate}. Short mode caps the points at {@value #SHORT_MAX_POINTS} so the result stays
 * within 4 KB.
 *
 * <p>Prompt 141 adds {@code format: czml}: the same track as a CZML document (the {@code GET …/track?format=czml}
 * shape) is stored in the media store as a {@link MediaDocuments document} that expires after
 * {@link MediaDocuments#DEFAULT_LIFETIME}, and the result is a link to it ({@code /v1/media/{id}}) plus the summary,
 * never the CZML inline. The media row names the entity, so callers who may not see its source cannot fetch it.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetTrackTool implements ForesignalMcpTool {

    /** The largest {@code maxPoints}. */
    public static final int MAX_POINTS = 500;

    /** The default {@code maxPoints}. */
    public static final int DEFAULT_MAX_POINTS = 200;

    /** The point cap of {@code detail=short}. */
    public static final int SHORT_MAX_POINTS = 100;

    static final Map<String, Object> INPUT = object(props(
            "sourceId", string("The source id (from list_sources), for example adsb-lol."),
            "externalId", string("The entity's id within the source (the part after the colon of a record id)."),
            "from", string("Window start: an RFC 3339 time, 'now', or a duration back from now such as -PT6H;"
                    + " default 6 hours before to."),
            "to", string("Window end, in the same forms; default now."),
            "maxPoints", integer("Most points to return (2–" + MAX_POINTS + "; detail=short returns at most "
                    + SHORT_MAX_POINTS + ").", 2, MAX_POINTS, (long) DEFAULT_MAX_POINTS),
            "detail", detail(),
            "format", enumeration("json (default): the compact track inline. czml: store the track as a CZML document"
                    + " for CesiumJS and return a link to it (expires after 24 hours) instead of the points.", "json",
                    "json", "czml")), "sourceId", "externalId");

    private final TrackService tracks;
    private final McpOutputShaper shaper;
    private final Clock clock;
    private final CzmlService czml;
    private final MediaDocuments documents;
    private final ProductUrls urls;

    /** Creates the tool. */
    public GetTrackTool(TrackService tracks, McpOutputShaper shaper, Clock clock, CzmlService czml,
            MediaDocuments documents, ProductUrls urls) {
        this.tracks = tracks;
        this.shaper = shaper;
        this.clock = clock;
        this.czml = czml;
        this.documents = documents;
        this.urls = urls;
    }

    @Override
    public String name() {
        return "get_track";
    }

    @Override
    public String title() {
        return "Track of one entity";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns where one entity (an aircraft, vessel, vehicle…) went in a window: segments"
                + " of [lon, lat, epochSeconds] points, simplified to maxPoints and split at gaps longer than the"
                + " source's TTL (at least 10 minutes), at tombstones and at the antimeridian, plus a summary"
                + " (distanceKm, durationSeconds, bbox, maxSpeedKmh between returned points, segments, approximate)."
                + " live=true means the last point is the current position, newer than any stored version."
                + " format=czml stores the track as a CZML document for CesiumJS and returns media.url (a link that"
                + " expires after 24 hours) with the summary instead of the points.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        Instant now = clock.instant();
        Instant to = McpTimes.instant(args, "to", now, now);
        Instant from = McpTimes.instant(args, "from", now, to.minus(TrackService.DEFAULT_WINDOW));
        int requested = McpArgs.integer(args, "maxPoints", DEFAULT_MAX_POINTS);
        boolean asCzml = "czml".equals(McpArgs.text(args, "format"));
        // A stored CZML document is not bound by the inline short-form cap.
        int cap = detail == Detail.SHORT && !asCzml ? SHORT_MAX_POINTS : MAX_POINTS;
        int maxPoints = Math.max(2, Math.min(requested, cap));
        String sourceId = McpArgs.requiredText(args, "sourceId");
        String externalId = McpArgs.requiredText(args, "externalId");
        TrackResult track = tracks.track(new TrackQuery(sourceId, externalId, from, to, null, maxPoints, null, true,
                List.of(), context.excludedSources(), context.restriction()));
        if (asCzml) {
            return ToolResult.json(shaper.object(czmlLink(track, context), detail));
        }
        return ToolResult.json(shaper.object(compact(track), detail));
    }

    /** Stores the track's CZML and returns the link plus the summary (prompt 141). */
    ObjectNode czmlLink(TrackResult track, ToolContext context) {
        String title = czml.currentTitle(track.sourceId(), track.externalId(), context.excludedSources(),
                context.restriction());
        byte[] bytes = czml.bytes(czml.trackDocument(track, title, null));
        MediaRow row = documents.store(new MediaDocuments.Document(bytes, CzmlWriter.MEDIA_TYPE, "czml",
                track.sourceId(), track.externalId(), null));
        ObjectNode out = compact(track);
        out.remove("segments");
        out.put("format", "czml");
        ObjectNode media = out.putObject("media");
        media.put("id", row.id());
        media.put("url", urls.storedBase() + "/v1/media/" + row.id());
        media.put("contentType", row.contentType());
        media.put("bytes", row.bytes());
        media.put("expiresAt", row.expiresAt().toString());
        return out;
    }

    /** The compact track and its summary. */
    ObjectNode compact(TrackResult track) {
        ObjectNode out = shaper.object();
        out.put("id", track.sourceId() + ":" + track.externalId());
        out.put("kind", track.kind());
        out.put("from", track.from().toString());
        out.put("to", track.to().toString());
        ObjectNode summary = out.putObject("summary");
        double distance = 0;
        double maxSpeed = 0;
        double minLon = Double.POSITIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        long first = Long.MAX_VALUE;
        long last = Long.MIN_VALUE;
        ArrayNode segments = shaper.object().arrayNode();
        for (int s = 0; s < track.coordinates().size(); s++) {
            List<double[]> coords = track.coordinates().get(s);
            List<Long> times = track.times().get(s);
            ArrayNode segment = segments.addArray();
            for (int i = 0; i < coords.size(); i++) {
                double[] c = coords.get(i);
                long t = times.get(i);
                segment.addArray().add(round(c[0])).add(round(c[1])).add(t / 1000);
                minLon = Math.min(minLon, c[0]);
                maxLon = Math.max(maxLon, c[0]);
                minLat = Math.min(minLat, c[1]);
                maxLat = Math.max(maxLat, c[1]);
                first = Math.min(first, t);
                last = Math.max(last, t);
                if (i > 0) {
                    double[] p = coords.get(i - 1);
                    double km = haversineKm(p[1], p[0], c[1], c[0]);
                    distance += km;
                    long ms = t - times.get(i - 1);
                    if (ms > 0) {
                        maxSpeed = Math.max(maxSpeed, km / (ms / 3_600_000.0));
                    }
                }
            }
        }
        summary.put("distanceKm", Math.round(distance * 100) / 100.0);
        summary.put("durationSeconds", first == Long.MAX_VALUE ? 0 : (last - first) / 1000);
        if (minLon != Double.POSITIVE_INFINITY) {
            summary.putArray("bbox").add(round(minLon)).add(round(minLat)).add(round(maxLon)).add(round(maxLat));
        }
        summary.put("maxSpeedKmh", Math.round(maxSpeed * 10) / 10.0);
        summary.put("segments", track.segments());
        summary.put("points", track.pointsReturned());
        summary.put("pointsScanned", track.pointsScanned());
        summary.put("approximate", track.approximate());
        summary.put("truncated", track.truncated());
        summary.put("live", track.live());
        summary.put("watermarkSeq", track.watermarkSeq());
        out.set("segments", segments);
        return out;
    }

    /** Great-circle distance in km. */
    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * 6371.0088 * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    private static double round(double value) {
        return Math.round(value * 100_000) / 100_000.0;
    }
}
