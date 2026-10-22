package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.SeriesEgress;
import ai.foresignal.api.SeriesQueryService;
import ai.foresignal.api.dto.SeriesInfoDto;
import ai.foresignal.api.dto.SeriesPointDto;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_series} (read, prompt 093): the energy and markets series catalog, or the points of one series.
 * Compact output with a {@code *_total} count and a {@code truncated} flag.
 *
 * <p>Reads are filtered by the caller's excluded sources and restriction (prompt 035), exactly as
 * {@code query_records}: on the Ask path the excluded sources include those the LLM destination may not receive
 * ({@code EgressPolicy.llmExcludedSources}, folded into the tool caller by {@code AskService}). A series of a source
 * the caller may not read is reported as missing. A source {@code EgressPolicy} refuses to send the caller on
 * {@code MCP} (a {@code LOCAL_ONLY} source for a caller not allowed local egress, EGR-2) is excluded too
 * ({@link SeriesEgress}).
 */
@RestrictedRead("visibility: excluded sources and RestrictionClamp")
@Component
public class GetSeriesTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 500;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 100;

    static final Map<String, Object> INPUT = object(props(
            "source", string("Source id (from list_sources). Give it with series to fetch one series' points."),
            "series", string("Series id, for example agsi:DE or eia930:ERCO:D. With source, its points are returned."),
            "q", string("Catalog text filter on series id, name and area (catalog mode only).", 1, 200),
            "from", dateTime("Points with periodStart at or after this RFC 3339 time (points mode only)."),
            "to", dateTime("Points with periodStart before this RFC 3339 time (points mode only)."),
            "limit", integer("Most entries to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, (long) DEFAULT_LIMIT),
            "detail", detail()));

    private final SeriesQueryService series;
    private final McpOutputShaper shaper;
    private final SeriesEgress egress;

    /** Creates the tool. */
    public GetSeriesTool(SeriesQueryService series, McpOutputShaper shaper, SeriesEgress egress) {
        this.series = series;
        this.shaper = shaper;
        this.egress = egress;
    }

    @Override
    public String name() {
        return "get_series";
    }

    @Override
    public String title() {
        return "Get energy and markets series";
    }

    @Override
    public String description() {
        return "Lists the energy and markets time series (oil prices, US grid demand, EU gas storage, monthly"
                + " generation, GB carbon intensity…) with their unit and newest value, or, when source and series"
                + " are given, one series' data points in period order (each with period, value, unit and status;"
                + " version > 1 means the point was revised). Series ids look like eia:RWTC, agsi:DE,"
                + " eia930:ERCO:D or carbon:gb:national.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Series catalog entries or one series' points");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String source = McpArgs.text(args, "source");
        String seriesId = McpArgs.text(args, "series");
        int limit = McpArgs.integer(args, "limit", DEFAULT_LIMIT);
        SeriesQueryService.Visibility visibility = egress.visibility(context.readCaller(), context.excludedSources(),
                context.restriction(), EgressPath.MCP);
        if (source != null && seriesId != null) {
            return ToolResult.json(points(source, seriesId, args, limit, visibility));
        }
        return ToolResult.json(catalog(source, McpArgs.text(args, "q"), limit, detail, visibility));
    }

    private ObjectNode points(String source, String seriesId, JsonNode args, int limit,
            SeriesQueryService.Visibility visibility) {
        SeriesInfoDto info = series.info(source, seriesId, visibility)
                .orElseThrow(() -> new McpToolException("No series '" + seriesId + "' in source '" + source + "'."));
        Instant from = McpArgs.instant(args, "from");
        Instant to = McpArgs.instant(args, "to");
        List<SeriesPointDto> points = series.points(source, info, from, to, null, limit, visibility).page().items();
        List<JsonNode> items = new ArrayList<>();
        for (SeriesPointDto point : points) {
            items.add(shaper.objectTree(point));
        }
        return shaper.list(items, points.size(), points.size() >= limit, Detail.SHORT,
                Map.of("series", shaper.objectTree(info), "pointsTotal", points.size()));
    }

    private ObjectNode catalog(String source, String q, int limit, Detail detail,
            SeriesQueryService.Visibility visibility) {
        List<String> sources = source == null ? List.of() : List.of(source);
        SeriesQueryService.Catalog page = series.list(sources, q, null, limit, visibility);
        List<JsonNode> items = new ArrayList<>();
        for (SeriesInfoDto info : page.items()) {
            items.add(shaper.objectTree(info));
        }
        return shaper.list(items, page.items().size(), page.nextCursor() != null, detail,
                Map.of("seriesTotal", page.items().size()));
    }
}
