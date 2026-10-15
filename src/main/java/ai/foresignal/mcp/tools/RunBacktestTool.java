package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.backtest.BacktestJobEntity;
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

/** {@code run_backtest} (agent, prompt 103). */
@Component
public class RunBacktestTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "type", enumeration("Job type.", "rule", "rule", "recipe", "precursor", "quality"),
            "targetId", string("The rule, recipe or precursor-test id.", 1, 64)));

    private final BacktestJobService jobs;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunBacktestTool(BacktestJobService jobs, JsonMapper json) {
        this.jobs = jobs;
        this.json = json;
    }

    @Override
    public String name() {
        return "run_backtest";
    }

    @Override
    public String title() {
        return "Run backtest";
    }

    @Override
    public String description() {
        return "Submits a backtest job over retained history. Every result carries its caveats; caveats first.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued job");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        BacktestJobEntity job = jobs.submit(McpArgs.text(args, "type"), McpArgs.text(args, "targetId"), null, args, null,
                context.actor());
        return ToolResult.json(json.valueToTree(BacktestViews.job(job)));
    }
}
