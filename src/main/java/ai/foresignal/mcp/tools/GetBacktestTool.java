package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_backtest} (agent, prompt 103). */
@Component
public class GetBacktestTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The job id.", 1, 26)), "id");

    private final BacktestJobService jobs;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetBacktestTool(BacktestJobService jobs, JsonMapper json) {
        this.jobs = jobs;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_backtest";
    }

    @Override
    public String title() {
        return "Get backtest";
    }

    @Override
    public String description() {
        return "Returns a backtest job: status, progress, caveats and result. Quote the caveats when reporting.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("One job");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(BacktestViews.job(jobs.get(McpArgs.requiredText(args, "id")))));
    }
}
