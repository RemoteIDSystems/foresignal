package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystRunner;
import ai.foresignal.analyst.AnalystService;
import ai.foresignal.analyst.AnalystViews;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code run_analyst} (agent, prompt 102): triggers a run now. */
@Component
public class RunAnalystTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("profileId", string("The profile id.", 1, 26)),
            "profileId");

    private final AnalystService analyst;
    private final AnalystRunner runner;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunAnalystTool(AnalystService analyst, AnalystRunner runner, JsonMapper json) {
        this.analyst = analyst;
        this.runner = runner;
        this.json = json;
    }

    @Override
    public String name() {
        return "run_analyst";
    }

    @Override
    public String title() {
        return "Run analyst";
    }

    @Override
    public String description() {
        return "Triggers an analyst profile run now and returns the queued run. Spending path: it uses the"
                + " configured LLM and its budget.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued analyst run");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean costBearing() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        AnalystProfileEntity profile = analyst.getProfile(McpArgs.requiredText(args, "profileId"));
        if (profile.getRunningSince() != null) {
            throw new McpToolException("Analyst profile is already running.");
        }
        try {
            var run = runner.queued(profile);
            profile.setRunningSince(run.getStartedAt());
            Thread.ofVirtual().name("analyst-mcp-" + profile.getId())
                    .start(() -> runner.executeQueued(profile, run.getId()));
            return ToolResult.json(json.valueToTree(AnalystViews.run(run)));
        } catch (ConflictException e) {
            throw new McpToolException(e.getMessage());
        }
    }
}
