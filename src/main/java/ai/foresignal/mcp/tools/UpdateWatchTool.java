package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code update_watch} (agent): replaces a trigger rule's definition, as {@code updateTrigger}. */
@Component
public class UpdateWatchTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "watchId", uuid("The watch id (from list_watches)."),
            "rule", WatchItems.RULE_SCHEMA), "watchId", "rule");

    private final TriggerRuleService rules;
    private final WatchItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public UpdateWatchTool(TriggerRuleService rules, WatchItems items, McpOutputShaper shaper) {
        this.rules = rules;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "update_watch";
    }

    @Override
    public String title() {
        return "Update a watch";
    }

    @Override
    public String description() {
        return "Replaces a watch's whole definition: members left out return to their defaults, so read it first"
                + " with list_watches detail=long and send it back changed. Validated as on the REST API. Its alerts"
                + " are kept. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The updated watch");
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
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        TriggerRuleService.RuleView view = rules.update(McpArgs.requiredUuid(args, "watchId"), args.get("rule"));
        return ToolResult.json(shaper.object(items.item(view, Detail.LONG), Detail.SHORT));
    }
}
