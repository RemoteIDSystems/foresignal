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
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code reject_analyst_proposal} (agent, prompt 102). */
@Component
public class RejectAnalystProposalTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The proposal id.", 1, 26)), "id");

    private final AnalystExecutionService execution;
    private final JsonMapper json;

    /** Creates the tool. */
    public RejectAnalystProposalTool(AnalystExecutionService execution, JsonMapper json) {
        this.execution = execution;
        this.json = json;
    }

    @Override
    public String name() {
        return "reject_analyst_proposal";
    }

    @Override
    public String title() {
        return "Reject analyst proposal";
    }

    @Override
    public String description() {
        return "Rejects a pending analyst proposal.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The rejected proposal");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            return ToolResult.json(json.valueToTree(
                    AnalystViews.proposal(execution.reject(McpArgs.requiredText(args, "id"), context.actor()))));
        } catch (RuntimeException e) {
            throw new McpToolException(e.getMessage());
        }
    }
}
