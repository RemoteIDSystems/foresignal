package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code sample_weather} (read, prompt 052; Weather-Imagery-Design §6.9): product values at a point or place,
 * plus the nearby hazard records: NWS alerts whose area contains the point ({@code automated: false}, the
 * authoritative warnings) and ProbSevere objects within 50 km ({@code automated: true}, guidance, never a
 * warning). Every value carries {@code validTime}, {@code localTime}, {@code ageMinutes}, {@code run},
 * {@code coverage} and {@code status}; anything but {@code ok} is unknown, never clear. Compact output of 4 KB or
 * less, {@code detail=long} up to 50 KB. The hazard records are read under the caller's restrictions.
 */
@RestrictedRead
@Component
public class SampleWeatherTool implements ForesignalMcpTool {

    /** Products sampled when none are named, as far as they are installed. */
    static final List<String> DEFAULT_PRODUCTS = List.of("mrms-reflectivity", "mrms-precip-rate", "mrms-mesh-60m",
            "gfs-gust", "gfs-t2m");

    /** The ProbSevere search radius, km. */
    static final double PROBSEVERE_KM = 50;

    /** The most hazard records of each kind. */
    static final int HAZARD_LIMIT = 10;

    static final Map<String, Object> INPUT = object(props(
            "lon", number("Longitude, WGS84 degrees (with lat).", -180, 180),
            "lat", number("Latitude, WGS84 degrees (with lon).", -90, 90),
            "place", string("A place id (from list_places) instead of lon/lat; needs a token.", 1, 64),
            "products", stringArray("Product ids (from list_products); default reflectivity, rain rate, hail, GFS gust"
                    + " and temperature.", SampleService.MAX_PRODUCTS),
            "time", string("latest (default), now, an ISO-8601 time, or an offset such as -PT1H (observed) or +PT6H"
                    + " (forecast).", 1, 64),
            "units", enumeration("metric (default) or imperial.", "metric", "metric", "imperial"),
            "detail", detail()));

    private final SampleService samples;
    private final WeatherToolSupport support;
    private final RecordQueryService records;
    private final RecordItems items;
    private final McpOutputShaper shaper;
    private final ProductRegistry registry;

    /** Creates the tool. */
    public SampleWeatherTool(SampleService samples, WeatherToolSupport support, RecordQueryService records,
            RecordItems items, McpOutputShaper shaper, ProductRegistry registry) {
        this.samples = samples;
        this.support = support;
        this.records = records;
        this.items = items;
        this.shaper = shaper;
        this.registry = registry;
    }

    @Override
    public String name() {
        return "sample_weather";
    }

    @Override
    public String title() {
        return "Sample the weather at a point";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Samples weather products (radar reflectivity, rain rate, hail, model wind, gusts,"
                + " temperature, smoke) at a point or place, with each value's valid time, local time, age in minutes,"
                + " model run, coverage and status, plus nearby hazards: NWS alerts covering the point (authoritative)"
                + " and automated ProbSevere storm objects within 50 km. A status other than ok (no-coverage, no-data,"
                + " stale, unavailable) means unknown, never clear weather.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Values and nearby hazards");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        SampleService.Point point = support.point(args, context);
        List<String> products = McpArgs.strings(args, "products");
        if (products == null || products.isEmpty()) {
            products = DEFAULT_PRODUCTS.stream().filter(id -> registry.find(id).isPresent()
                    && !context.excludedSources().contains(registry.find(id).get().sourceId())).toList();
        }
        boolean imperial = "imperial".equals(McpArgs.text(args, "units"));
        TimeSpec time = TimeSpec.parse("time", McpArgs.text(args, "time"));
        ObjectNode out = support.object();
        if (point.placeId() != null) {
            out.put("placeId", point.placeId());
        } else {
            out.put("lon", point.lon());
            out.put("lat", point.lat());
        }
        out.put("timezone", point.zone().getId());
        out.put("time", time.text());
        out.put("serverTime", samples.now().toString());
        ArrayNode values = out.putArray("values");
        if (!products.isEmpty()) {
            for (ProductRegistry.Registered product : samples.resolveProducts(products, context.excludedSources())) {
                values.add(support.value(samples.value(product, point, time, null, imperial, samples.now())));
            }
        }
        ObjectNode hazards = out.putObject("hazards");
        hazards.set("nwsAlerts", hazards(context, "nws-alerts", "nws-alert", point, 0.1, false, detail));
        hazards.set("probSevere", hazards(context, "mrms-probsevere", "probsevere-object", point, PROBSEVERE_KM, true,
                detail));
        out.put("note", "NWS alerts are authoritative; ProbSevere and derived values are automated guidance, not"
                + " warnings.");
        return ToolResult.json(shaper.object(out, detail));
    }

    /** The current records of one hazard kind near the point, each flagged {@code automated}. */
    private ArrayNode hazards(ToolContext context, String source, String kind, SampleService.Point point,
            double radiusKm, boolean automated, Detail detail) {
        ArrayNode array = support.object().arrayNode();
        Set<String> excluded = context.excludedSources();
        if (excluded.contains(source)) {
            return array;
        }
        CurrentQuery query = new CurrentQuery(List.of(source), List.of(kind), List.of(), null,
                new NearFilter(point.lon(), point.lat(), radiusKm), null, null, TimeField.EFFECTIVE, null, false,
                ListOrder.OBSERVED, null, null, HAZARD_LIMIT, FieldSelection.ALL, GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON, false, excluded).withRestriction(context.restriction());
        List<RecordRow> rows = new ArrayList<>();
        for (RecordRow row : records.list(query).rows()) {
            if (!row.tombstone()) {
                rows.add(row);
            }
        }
        for (ObjectNode item : items.items(rows, RecordMapper.Shape.CURRENT, detail)) {
            item.put("automated", automated);
            array.add(item);
        }
        return array;
    }
}
