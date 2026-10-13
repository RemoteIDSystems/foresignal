package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.sample.AreaStatsService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.security.Scope;
import ai.foresignal.source.spi.Geo;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code area_weather_stats} (read, prompt 052; Weather-Imagery-Design §6.9): wraps
 * {@code POST /v1/products/{id}/stats} — min, max, mean, p95, coverage and the area above each threshold of one
 * product over a place or a bounding box. Carries {@code validTime}, {@code localTime}, {@code ageMinutes},
 * {@code run}, {@code coverage} and {@code status}; a non-ok status carries no statistics. Returns no records.
 * Prompt 056 adds {@code citationId} ({@code wx:{productId}@{frameKey}}, forecasts {@code wx:{productId}@{run}/{step}}).
 */
@Component
public class AreaWeatherStatsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "product", string("The product id (from list_products), for example mrms-reflectivity.", 1, 64),
            "place", string("A place id (from list_places): the place's area; needs a token.", 1, 64),
            "bbox", string("Instead of place: minLon,minLat,maxLon,maxLat in WGS84 degrees.", 7, 100),
            "time", string("latest (default), now, an ISO-8601 time, or an offset such as -PT1H or +PT6H.", 1, 64),
            "thresholds", Map.of("type", "array", "items", Map.of("type", "number"), "maxItems",
                    AreaStatsService.MAX_THRESHOLDS, "description",
                    "Thresholds in the requested units; the area at or above each is returned, km²."),
            "units", enumeration("metric (default) or imperial.", "metric", "metric", "imperial"),
            "detail", detail()), "product");

    private final AreaStatsService stats;
    private final SampleService samples;
    private final PlaceService places;
    private final WeatherToolSupport support;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public AreaWeatherStatsTool(AreaStatsService stats, SampleService samples, PlaceService places,
            WeatherToolSupport support, McpOutputShaper shaper) {
        this.stats = stats;
        this.samples = samples;
        this.places = places;
        this.support = support;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "area_weather_stats";
    }

    @Override
    public String title() {
        return "Weather statistics over an area";
    }

    @Override
    public String description() {
        return "Computes statistics of one weather product over a place's area or a bounding box: min, max, mean, 95th"
                + " percentile, coverage percent and the area (km², on the sphere) at or above each threshold, for"
                + " example the area of 50 dBZ reflectivity. At most 2,000,000 km². A status other than ok means"
                + " unknown and carries no statistics.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Area statistics");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        ProductRegistry.Registered product = samples.visible(McpArgs.requiredText(args, "product"),
                context.excludedSources());
        String placeId = McpArgs.text(args, "place");
        String bbox = McpArgs.text(args, "bbox");
        if ((placeId == null) == (bbox == null)) {
            throw new McpToolException("Give exactly one of place and bbox.");
        }
        Geometry area;
        ZoneId zone = java.time.ZoneOffset.UTC;
        if (placeId != null) {
            if (context.caller().anonymous()) {
                throw new McpToolException("A place needs a token: places are never anonymous.");
            }
            PlaceView place = places.find(placeId).orElseThrow(() ->
                    new McpToolException("No place has the id '" + placeId + "'. Use list_places."));
            area = place.area();
            zone = WeatherToolSupport.zone(place.timezone());
        } else {
            area = box(bbox);
        }
        List<Double> thresholds = new ArrayList<>();
        JsonNode t = args == null ? null : args.get("thresholds");
        if (t != null && t.isArray()) {
            t.forEach(n -> thresholds.add(n.asDouble()));
        }
        Map<String, Object> result = new LinkedHashMap<>(stats.stats(product, area,
                TimeSpec.parse("time", McpArgs.text(args, "time")), null, thresholds,
                "imperial".equals(McpArgs.text(args, "units"))));
        Object valid = result.get("validTime");
        result.put("localTime", valid == null ? null : Instant.parse(valid.toString()).atZone(zone).toOffsetDateTime()
                .truncatedTo(ChronoUnit.SECONDS).toString());
        result.put("coverage", product.spec().spatial() == null ? null : product.spec().spatial().coverage());
        // Prompt 056: the citation id Ask answers use for this value.
        String citationId = ai.foresignal.weather.WeatherCitations.of(product.id(),
                result.get("frameKey") == null ? null : result.get("frameKey").toString(),
                result.get("run") == null ? null : result.get("run").toString(), step(result, product));
        if (citationId != null) {
            result.put("citationId", citationId);
        }
        if (detail == Detail.SHORT) {
            result.remove("attribution");
        }
        ObjectNode out = support.tree(result);
        if (placeId != null) {
            out.put("placeId", placeId);
        }
        return ToolResult.json(shaper.object(out, detail));
    }

    /** The forecast step of a stats result in hours, or null for an observation. */
    private static Integer step(Map<String, Object> result, ProductRegistry.Registered product) {
        Object run = result.get("run");
        Object valid = result.get("validTime");
        if (run == null || valid == null) {
            return null;
        }
        try {
            return ai.foresignal.weather.WeatherCitations.stepHours(Instant.parse(run.toString()),
                    Instant.parse(valid.toString()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The polygon of {@code minLon,minLat,maxLon,maxLat}. */
    static Geometry box(String bbox) {
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw new McpToolException("bbox must be minLon,minLat,maxLon,maxLat.");
        }
        double[] v = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                v[i] = Double.parseDouble(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            throw new McpToolException("bbox must be four numbers: minLon,minLat,maxLon,maxLat.");
        }
        if (v[0] >= v[2] || v[1] >= v[3] || v[0] < -180 || v[2] > 180 || v[1] < -90 || v[3] > 90) {
            throw new McpToolException("bbox must be a valid minLon,minLat,maxLon,maxLat box.");
        }
        return Geo.factory().toGeometry(new Envelope(v[0], v[2], v[1], v[3]));
    }
}
