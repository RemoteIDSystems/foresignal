package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code sample_weather_along_track} (read, prompt 106): samples weather products at each point of a
 * track, each at its own time, with per-point statuses. The points come from the request, or from a
 * record's stored track (037). Future points use the newest complete run (or the pinned {@code run});
 * a {@code maxGap} marks a value stale when the chosen frame is older than it.
 */
@RestrictedRead
@Component
public class SampleWeatherAlongTrackTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "record", string("A record {sourceId}/{externalId}; its stored track is sampled (needs a token).", 3, 256),
            "points", stringArray("Points as {lon,lat,time} (at most 500), instead of record.", SampleService.MAX_TRACK_POINTS),
            "products", stringArray("Product ids (from list_products); at least one.", SampleService.MAX_PRODUCTS),
            "from", string("Track start (ISO-8601) with record.", 1, 64),
            "to", string("Track end (ISO-8601) with record.", 1, 64),
            "maxGap", string("Mark a value stale when its frame is older than this ISO-8601 duration.", 1, 32),
            "run", string("The forecast run to sample future points from (latest or an ISO-8601 time).", 1, 64),
            "units", enumeration("metric (default) or imperial.", "metric", "metric", "imperial"),
            "detail", detail()));

    private final SampleService samples;
    private final WeatherToolSupport support;
    private final McpOutputShaper shaper;
    private final TrackService tracks;

    /** Creates the tool. */
    public SampleWeatherAlongTrackTool(SampleService samples, WeatherToolSupport support, McpOutputShaper shaper,
            TrackService tracks) {
        this.samples = samples;
        this.support = support;
        this.shaper = shaper;
        this.tracks = tracks;
    }

    @Override
    public String name() {
        return "sample_weather_along_track";
    }

    @Override
    public String title() {
        return "Sample the weather along a track";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Samples weather products at each point of a track (given as points, or from a"
                + " record's stored track), each point at its own time, returning a per-point status and the values"
                + " with their validity, run and status. Future points use a forecast run. Anything but ok is"
                + " unknown, never clear.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Per-point samples along the track");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<String> products = McpArgs.strings(args, "products");
        if (products == null || products.isEmpty()) {
            throw new McpToolException("name at least one product");
        }
        String record = McpArgs.text(args, "record");
        JsonNode pointsNode = args.get("points");
        List<SampleService.TrackPoint> points;
        if (record != null) {
            points = trackPoints(record, args, context);
        } else if (pointsNode != null && pointsNode.isArray() && !pointsNode.isEmpty()) {
            points = requestPoints(pointsNode);
        } else {
            throw new McpToolException("give record, or points");
        }
        boolean imperial = "imperial".equals(McpArgs.text(args, "units"));
        Duration maxGap = duration(McpArgs.text(args, "maxGap"));
        String run = McpArgs.text(args, "run");
        Map<String, Object> result = samples.sampleTrack(points, products, maxGap, run, imperial,
                context.excludedSources());
        ObjectNode out = support.tree(result);
        if (record != null) {
            out.put("record", record);
        }
        return ToolResult.json(shaper.object(out, detail));
    }

    private List<SampleService.TrackPoint> requestPoints(JsonNode pointsNode) {
        if (pointsNode.size() > SampleService.MAX_TRACK_POINTS) {
            throw new McpToolException("at most " + SampleService.MAX_TRACK_POINTS + " points");
        }
        Instant now = samples.now();
        List<SampleService.TrackPoint> out = new ArrayList<>();
        for (JsonNode p : pointsNode) {
            if (p == null || !p.isObject() || !p.has("lon") || !p.has("lat")) {
                throw new McpToolException("each point must be {lon, lat, time?}");
            }
            double lon = p.get("lon").asDouble();
            double lat = p.get("lat").asDouble();
            Instant time = p.has("time") && !p.get("time").isNull() ? Instant.parse(p.get("time").asString()) : now;
            out.add(new SampleService.TrackPoint(lon, lat, time));
        }
        return out;
    }

    private List<SampleService.TrackPoint> trackPoints(String record, JsonNode args, ToolContext context) {
        int slash = record.indexOf('/');
        if (slash <= 0 || slash == record.length() - 1) {
            throw new McpToolException("record must be {sourceId}/{externalId}");
        }
        TrackQuery query = new TrackQuery(record.substring(0, slash), record.substring(slash + 1),
                McpArgs.instant(args, "from"), McpArgs.instant(args, "to"), null, SampleService.MAX_TRACK_POINTS, null,
                true, List.of(), context.excludedSources(), context.restriction());
        TrackResult result = tracks.track(query);
        List<SampleService.TrackPoint> out = new ArrayList<>();
        List<List<double[]>> coordinates = result.coordinates();
        List<List<Long>> times = result.times();
        for (int s = 0; s < coordinates.size() && out.size() < SampleService.MAX_TRACK_POINTS; s++) {
            List<double[]> segment = coordinates.get(s);
            List<Long> segmentTimes = s < times.size() ? times.get(s) : List.of();
            for (int k = 0; k < segment.size() && out.size() < SampleService.MAX_TRACK_POINTS; k++) {
                double[] c = segment.get(k);
                Instant time = k < segmentTimes.size() ? Instant.ofEpochMilli(segmentTimes.get(k)) : samples.now();
                out.add(new SampleService.TrackPoint(c[0], c[1], time));
            }
        }
        if (out.isEmpty()) {
            throw new McpToolException("record " + record + " has no track points");
        }
        return out;
    }

    private static Duration duration(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Duration.parse(text);
        } catch (RuntimeException e) {
            throw new McpToolException("maxGap must be an ISO-8601 duration");
        }
    }
}
