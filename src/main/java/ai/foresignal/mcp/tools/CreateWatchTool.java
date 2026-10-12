package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code create_watch} (agent): creates a trigger rule with the full REST validation, as {@code createTrigger}. */
@Component
public class CreateWatchTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("rule", WatchItems.RULE_SCHEMA), "rule");

    private final TriggerRuleService rules;
    private final WatchItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreateWatchTool(TriggerRuleService rules, WatchItems items, McpOutputShaper shaper) {
        this.rules = rules;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "create_watch";
    }

    @Override
    public String title() {
        return "Create a watch";
    }

    @Override
    public String description() {
        return "Creates a watch (trigger rule) that raises an alert whenever a record matches: for example every"
                + " earthquake of magnitude 5 or more (kinds [earthquake], conditions [{path: mag, op: '>=', value:"
                + " 5}]) or every aircraft entering a polygon (mode enter, geofence). The definition is validated as"
                + " on the REST API and every problem is reported at once. Try it first with test_watch after"
                + " creating it disabled (enabled false) if unsure. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created watch with its id");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        TriggerRuleService.RuleView view = rules.create(args.get("rule"));
        return ToolResult.json(shaper.object(items.item(view, Detail.LONG), Detail.SHORT));
    }
}
