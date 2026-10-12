package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_watches} (read): the trigger rules, as {@code listTriggers}. */
@Component
public class ListWatchesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "tag", string("Only watches with this tag.", 1, 32),
            "enabled", bool("Only enabled (true) or disabled (false) watches; omit for both.", null),
            "detail", detail()));

    private final TriggerRuleService rules;
    private final WatchItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListWatchesTool(TriggerRuleService rules, WatchItems items, McpOutputShaper shaper) {
        this.rules = rules;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_watches";
    }

    @Override
    public String title() {
        return "List watches";
    }

    @Override
    public String description() {
        return "Lists the watches (trigger rules): what each watches (sources, kinds, conditions, geofence), its"
                + " mode and severity, whether it is enabled or snoozed, and how many alerts it has raised. Use"
                + " detail=long for the full definition before update_watch.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Watches");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<ObjectNode> out = rules.list(McpArgs.text(args, "tag"), McpArgs.optionalBool(args, "enabled")).stream()
                .map(view -> items.item(view, detail))
                .toList();
        return ToolResult.json(shaper.list(out, out.size(), false, detail, null));
    }
}
