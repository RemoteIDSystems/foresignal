package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code delete_watch} (agent): deletes a trigger rule, keeping its alerts, as {@code deleteTrigger}. */
@Component
public class DeleteWatchTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("watchId", uuid("The watch id (from list_watches).")),
            "watchId");

    private final TriggerRuleService rules;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public DeleteWatchTool(TriggerRuleService rules, McpOutputShaper shaper) {
        this.rules = rules;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "delete_watch";
    }

    @Override
    public String title() {
        return "Delete a watch";
    }

    @Override
    public String description() {
        return "Deletes a watch so it raises no more alerts; the alerts it already raised are kept. To pause a watch"
                + " instead, update it with enabled false. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("{deleted: true, watchId}");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean destructiveHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        UUID id = McpArgs.requiredUuid(args, "watchId");
        rules.delete(id);
        ObjectNode out = shaper.object();
        out.put("deleted", true);
        out.put("watchId", id.toString());
        return ToolResult.json(out);
    }
}
