package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analyst.AnalystExecutionService;
import ai.foresignal.analyst.AnalystViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import ai.foresignal.security.restrict.RestrictedRead;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code confirm_analyst_proposal} (agent, prompt 102). */
@RestrictedRead("a private proposal is not found for a caller without private access; no record content")
@Component
public class ConfirmAnalystProposalTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The proposal id.", 1, 26)), "id");

    private final AnalystExecutionService execution;
    private final JsonMapper json;
    private final ai.foresignal.intel.IntelVisibility visibility;

    /** Creates the tool. */
    public ConfirmAnalystProposalTool(AnalystExecutionService execution, JsonMapper json,
            ai.foresignal.intel.IntelVisibility visibility) {
        this.visibility = visibility;
        this.execution = execution;
        this.json = json;
    }

    @Override
    public String name() {
        return "confirm_analyst_proposal";
    }

    @Override
    public String title() {
        return "Confirm analyst proposal";
    }

    @Override
    public String description() {
        return "Confirms and executes a pending analyst proposal. The analyst never deletes or disables anything.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The executed proposal");
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
    public boolean destructiveHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            return ToolResult.json(json.valueToTree(
                    AnalystViews.proposal(execution.confirm(McpArgs.requiredText(args, "id"), context.actor(),
                            visibility.seesPrivate(context.caller())))));
        } catch (RuntimeException e) {
            throw new McpToolException(e.getMessage());
        }
    }
}
