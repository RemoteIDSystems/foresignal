package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.security.Scope;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code weather_forecast} (read, prompt 052; Weather-Imagery-Design §6.9): a 3-hourly point forecast to
 * 120 h from the stored GFS and HRRR runs (NWS joins in prompt 053). Each provider carries its {@code run},
 * {@code status} and {@code coverage}; each step its {@code validTime}, {@code localTime} and
 * {@code ageMinutes} (negative for the future). Compact output of 4 KB or less, {@code detail=long} up to
 * 50 KB. Reads stored runs only; returns no records. Prompt 056 adds each step's {@code citationId}
 * ({@code wx:{provider}@{run}/{step}}; NWS steps count from the forecast's {@code updatedAt}).
 */
@Component
public class WeatherForecastTool implements ForesignalMcpTool {

    /** Steps per provider in compact output, so two providers fit in 4 KB. */
    static final int SHORT_STEPS = 10;

    /** The default and largest horizon, hours. */
    static final int MAX_HOURS = 120;

    /** The model products compared for the disagreement block (prompt 105). */
    static final List<String> DISAGREEMENT_PRODUCTS = List.of("gfs-gust", "hrrr-gust", "ifs-gust", "gfs-t2m",
            "ifs-t2m", "ifs-precip-24h");

    static final Map<String, Object> INPUT = object(props(
            "lon", number("Longitude, WGS84 degrees (with lat).", -180, 180),
            "lat", number("Latitude, WGS84 degrees (with lon).", -90, 90),
            "place", string("A place id (from list_places) instead of lon/lat; needs a token.", 1, 64),
            "hours", integer("How far ahead, hours (3–120).", 3, MAX_HOURS, (long) MAX_HOURS),
            "detail", detail()));

    private final ForecastSeriesService forecasts;
    private final SampleService samples;
    private final WeatherToolSupport support;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public WeatherForecastTool(ForecastSeriesService forecasts, SampleService samples, WeatherToolSupport support,
            McpOutputShaper shaper) {
        this.forecasts = forecasts;
        this.samples = samples;
        this.support = support;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "weather_forecast";
    }

    @Override
    public String title() {
        return "Point weather forecast";
    }

    @Override
    public String description() {
        return "Returns a 3-hourly forecast at a point or place to 120 h from the stored GFS and HRRR model runs, plus"
                + " the official NWS gridpoint forecast (to 72 h, with precipitation probability and sky cover) for a"
                + " place:"
                + " temperature (°C), wind speed (m/s) and direction (from, degrees), gusts, precipitation per step (mm)"
                + " and HRRR smoke (µg/m³), each step with its valid and local time. Each provider gives its run time and"
                + " status (ok, stale, no-coverage or unavailable; anything but ok is unknown).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The forecast by provider");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        SampleService.Point point = support.point(args, context);
        int hours = Math.max(3, Math.min(MAX_HOURS, McpArgs.integer(args, "hours", MAX_HOURS)));
        Instant now = samples.now();
        Instant until = now.plus(Duration.ofHours(hours));
        ObjectNode out = support.object();
        if (point.placeId() != null) {
            out.put("placeId", point.placeId());
        } else {
            out.put("lon", point.lon());
            out.put("lat", point.lat());
        }
        out.put("timezone", point.zone().getId());
        out.put("serverTime", now.toString());
        ArrayNode providers = out.putArray("providers");
        for (Map<String, Object> series : forecasts.series(point.placeId(), point.lon(), point.lat(),
                context.excludedSources())) {
            ObjectNode p = providers.addObject();
            String provider = String.valueOf(series.get("provider"));
            p.put("provider", provider);
            p.put("run", (String) series.get("run"));
            p.put("status", String.valueOf(series.get("status")));
            p.put("coverage", "gfs".equals(provider) || "ifs".equals(provider) ? "global"
                    : "nws".equals(provider) ? "place" : "CONUS");
            if (series.get("updatedAt") != null) {
                p.put("updatedAt", String.valueOf(series.get("updatedAt")));
            }
            ArrayNode items = p.putArray("items");
            int kept = 0;
            for (Map<String, Object> item : (List<Map<String, Object>>) series.get("items")) {
                Instant t = Instant.parse((String) item.get("time"));
                if (t.isAfter(until) || t.atZone(java.time.ZoneOffset.UTC).getHour() % 3 != 0) {
                    continue;
                }
                if (kept >= SHORT_STEPS && detail == Detail.SHORT) {
                    p.put("truncated", true);
                    p.put("notice", "Compact output keeps the first " + SHORT_STEPS + " steps; ask for detail=long"
                            + " for the rest.");
                    break;
                }
                kept++;
                ObjectNode step = support.tree(item);
                step.remove("time");
                step.put("validTime", t.toString());
                step.put("localTime", t.atZone(point.zone()).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString());
                step.put("ageMinutes", SampleService.ageMinutes(t, now));
                // Prompt 056: wx:{provider}@{run}/{step} (GFS, HRRR), wx:nws@{updatedAt}/{hours} (NWS).
                String run = (String) series.get("run");
                String base = run != null ? run : series.get("updatedAt") == null ? null : String.valueOf(series.get("updatedAt"));
                if (base != null) {
                    try {
                        step.put("citationId", ai.foresignal.weather.WeatherCitations.forecast(provider, base,
                                ai.foresignal.weather.WeatherCitations.stepHours(Instant.parse(base), t)));
                    } catch (RuntimeException e) {
                        // an unparseable run time leaves the step without a citation id
                    }
                }
                items.add(step);
            }
        }
        // Prompt 105: the model-disagreement block when the model products can be sampled.
        try {
            Map<String, Object> sample = samples.sample(point, DISAGREEMENT_PRODUCTS, TimeSpec.NOW, null, false,
                    context.excludedSources());
            if (sample.get("disagreement") instanceof Map<?, ?> disagreement) {
                out.set("disagreement", support.tree((Map<String, Object>) disagreement));
            }
        } catch (RuntimeException e) {
            // Some model products are not installed; the forecast is still served.
        }
        return ToolResult.json(shaper.object(out, detail));
    }
}
