package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.briefing.BriefingService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_briefings} (read): the scheduled briefings, as {@code listBriefings} (prompt 029). */
@Component
public class ListBriefingsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    private final BriefingService briefings;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListBriefingsTool(BriefingService briefings, McpOutputShaper shaper) {
        this.briefings = briefings;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_briefings";
    }

    @Override
    public String title() {
        return "List briefings";
    }

    @Override
    public String description() {
        return "Lists the scheduled briefings (morning brief, weekly summaries): name, cron schedule and timezone,"
                + " scope (sources, kinds, bbox), mode (digest or llm), channels, next and last run. Use"
                + " get_briefing_run with a briefing id to read its latest run.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Briefings");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<ObjectNode> items = briefings.list().stream().map(shaper::objectTree).toList();
        return ToolResult.json(shaper.list(items, items.size(), false, detail, null));
    }
}
