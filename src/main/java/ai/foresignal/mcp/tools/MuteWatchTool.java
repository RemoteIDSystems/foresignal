package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code mute_watch} (agent, prompt 033): mutes a watch ({@code muteTrigger}) for 1 h to 7 d: its
 * alerts are still recorded but not notified, and escalations of its open alerts stop.
 */
@Component
public class MuteWatchTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "watchId", uuid("The watch id (from list_watches)."),
            "until", dateTime("When the mute ends (RFC 3339, 1 h to 7 d ahead). Give until or for."),
            "for", string("How long, for example 4h or 2d (1h to 7d). Give until or for.", 1, 40)), "watchId");

    private final TriggerRuleService rules;
    private final WatchItems items;
    private final JsonMapper json;

    /** Creates the tool. */
    public MuteWatchTool(TriggerRuleService rules, WatchItems items, JsonMapper json) {
        this.rules = rules;
        this.items = items;
        this.json = json;
    }

    @Override
    public String name() {
        return "mute_watch";
    }

    @Override
    public String title() {
        return "Mute a watch";
    }

    @Override
    public String description() {
        return "Mutes a watch for 1 hour to 7 days: its alerts are still recorded but nobody is notified, and"
                + " escalations of its open alerts stop. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The watch after the change");
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
        ObjectNode body = json.createObjectNode();
        if (args.hasNonNull("until")) {
            body.set("until", args.get("until"));
        }
        if (args.hasNonNull("for")) {
            body.set("for", args.get("for"));
        }
        TriggerRuleService.RuleView view = rules.mute(McpArgs.requiredUuid(args, "watchId"), body);
        return ToolResult.json(items.item(view, Detail.SHORT));
    }
}
