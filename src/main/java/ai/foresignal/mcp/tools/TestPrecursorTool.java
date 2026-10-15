package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
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

/** {@code test_precursor} (agent, prompt 103). */
@Component
public class TestPrecursorTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The precursor test id.", 1, 26)), "id");

    private final BacktestJobService jobs;
    private final JsonMapper json;

    /** Creates the tool. */
    public TestPrecursorTool(BacktestJobService jobs, JsonMapper json) {
        this.jobs = jobs;
        this.json = json;
    }

    @Override
    public String name() {
        return "test_precursor";
    }

    @Override
    public String title() {
        return "Test precursor";
    }

    @Override
    public String description() {
        return "Runs a precursor test over retained history: does X tend to precede Y within T? Association in"
                + " stored data; not causation. Quote the caveats.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued precursor job");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        BacktestJobEntity job = jobs.submit("precursor", McpArgs.requiredText(args, "id"), null, args, null,
                context.actor());
        return ToolResult.json(json.valueToTree(BacktestViews.job(job)));
    }
}
