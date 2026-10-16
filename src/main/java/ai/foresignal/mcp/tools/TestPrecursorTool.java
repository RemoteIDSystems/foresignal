package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.nonNegative;
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

/** {@code test_precursor} (agent, prompt 103). */
@Component
public class TestPrecursorTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "id", string("The precursor test id.", 1, 26),
            "from", dateTime("Start of the range (use from/to or fromSeq/toSeq; at most 90 days; default 30 days)."),
            "to", dateTime("End of the range (default now)."),
            "fromSeq", nonNegative("Exclusive lower seq bound."),
            "toSeq", nonNegative("Inclusive upper seq bound.")), "id");

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
        String tokenId = context.caller().currentToken().map(t -> t.id().toString()).orElse(null);
        BacktestJobEntity job = jobs.submit("precursor", McpArgs.requiredText(args, "id"), null, args, null, tokenId);
        return ToolResult.json(json.valueToTree(BacktestViews.job(job)));
    }
}
