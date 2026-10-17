package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code list_analysis_series} (read, prompt 119): the analysis-series registry the detectors run on. */
@RestrictedRead
@Component
public class ListAnalysisSeriesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "family", stringArray("Signal families.", 20),
            "region", stringArray("040 region codes.", 50),
            "enabled", bool("Only enabled (true) or disabled (false) rows.", null),
            "limit", integer("Page size.", 1, 500, 100L),
            "cursor", string("The nextCursor of the previous page.")));

    private final AnalysisSeriesService registry;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListAnalysisSeriesTool(AnalysisSeriesService registry, JsonMapper json) {
        this.registry = registry;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_analysis_series";
    }

    @Override
    public String title() {
        return "List analysis series";
    }

    @Override
    public String description() {
        return "Lists the analysis series the change-point and event-study detectors run on, with their frequency,"
                + " signal family, region and event-study watch pairs.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of analysis series");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(AnalysisSeriesService.view(registry.list(
                McpArgs.strings(args, "family"), McpArgs.strings(args, "region"), McpArgs.optionalBool(args, "enabled"),
                McpArgs.text(args, "cursor"), Math.max(1, Math.min(500, McpArgs.integer(args, "limit", 100)))))));
    }
}
