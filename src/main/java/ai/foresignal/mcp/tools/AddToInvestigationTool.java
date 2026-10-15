package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.InvestigationService;
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

/** {@code add_to_investigation} (agent, prompt 101): pins an item, taking its snapshot now. */
@Component
public class AddToInvestigationTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "investigationId", string("The investigation id.", 1, 26),
            "itemType", string("event, alert, record, anomaly, place, briefing_run or note.", 1, 24),
            "ref", string("The referenced id.", 1, 256),
            "seq", integer("The version seq to pin, for a record.", 0, 9_000_000_000L, null),
            "note", string("An optional note.", 1, 4000)));

    private final InvestigationService investigations;
    private final JsonMapper json;

    /** Creates the tool. */
    public AddToInvestigationTool(InvestigationService investigations, JsonMapper json) {
        this.investigations = investigations;
        this.json = json;
    }

    @Override
    public String name() {
        return "add_to_investigation";
    }

    @Override
    public String title() {
        return "Add to investigation";
    }

    @Override
    public String description() {
        return "Adds an item to an investigation, snapshotting it now so the evidence survives retention.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The added item");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "investigationId");
        var item = investigations.addItem(id, args, context.actor());
        return ToolResult.json(json.valueToTree(Map.of("id", item.getId(), "itemType", item.getItemType(),
                "ref", item.getRef(), "addedAt", item.getAddedAt().toString())));
    }
}
