package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.quality.TuningWorkflowService;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code apply_tuning_suggestion} (agent; prompt 115): applies an open tuning suggestion through the normal rule
 * update (same validation, same events) and returns the tuning log entry, which can be undone. An MCP agent with
 * agent scope is the caller that allows actions; inside Ask a model can only propose this (EGR-3).
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class ApplyTuningSuggestionTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ruleId", uuid("The rule id"),
            "suggestionId", string("The suggestion id (from get_tuning_suggestions)", 1, 64),
            "note", string("A note recorded in the tuning log", 1, TuningWorkflowService.MAX_NOTE)),
            "ruleId", "suggestionId");

    private final TuningWorkflowService tuning;
    private final JsonMapper json;

    /** Creates the tool. */
    public ApplyTuningSuggestionTool(TuningWorkflowService tuning, JsonMapper json) {
        this.tuning = tuning;
        this.json = json;
    }

    @Override
    public String name() {
        return "apply_tuning_suggestion";
    }

    @Override
    public String title() {
        return "Apply a tuning suggestion";
    }

    @Override
    public String description() {
        return "Applies an open tuning suggestion's patch, shown verbatim in get_tuning_suggestions, as an ordinary"
                + " rule update and records it in the tuning log (undoable while the rule is unchanged). Refuses a"
                + " suggestion whose rule changed since it was generated. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A TuningLogEntry");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            return ToolResult.json(json.valueToTree(TuningWorkflowService.view(tuning.apply(
                    McpArgs.requiredUuid(args, "ruleId"), McpArgs.requiredText(args, "suggestionId"),
                    McpArgs.text(args, "note"), context.actor()))));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
