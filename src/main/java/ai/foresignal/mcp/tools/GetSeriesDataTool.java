package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_series_data} (read, prompt 119): a {@code series:}, {@code rollup:} or {@code rule:} series through the
 * series reader, with {@code knownAt}, coverage flags and gaps; raw values of {@code LOCAL_ONLY} series withheld from
 * callers not allowed local egress.
 */
@RestrictedRead
@Component
public class GetSeriesDataTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("Series reference: series:{sourceId}/{seriesId}, rollup:{hour|day}/{sourceId|*}/{kind}/"
                    + "{region}, or rule:{ruleId}/{groupKey}."),
            "from", dateTime("Period start (inclusive)."),
            "to", dateTime("Period end (exclusive)."),
            "asOf", dateTime("Knowledge cutoff: only points known by then.")), "ref");

    private final SeriesReader reader;
    private final AnalyticsAccess access;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetSeriesDataTool(SeriesReader reader, AnalyticsAccess access, JsonMapper json) {
        this.reader = reader;
        this.access = access;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_series_data";
    }

    @Override
    public String title() {
        return "Get series data";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Reads a series (market and attention series, region event counts from rollups, or an"
                + " anomaly rule group) as points with knownAt and coverage flags, plus explicit gaps. A gap means no"
                + " data or no coverage, never zero activity.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The series data");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        SeriesRef ref = SeriesRef.parse(McpArgs.requiredText(args, "ref"));
        SeriesReader.Meta meta = reader.meta(ref);
        boolean withheld = access.checkSeries(ref, meta, context.readCaller(), context.excludedSources(),
                context.restriction(), EgressPath.MCP);
        return ToolResult.json(json.valueToTree(reader.read(ref, McpArgs.instant(args, "from"),
                McpArgs.instant(args, "to"), McpArgs.instant(args, "asOf")).view(withheld)));
    }
}
