package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
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

/** {@code update_intel_event_status} (agent, prompt 101): title, status, severity or notes. */
@Component
public class UpdateIntelEventStatusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "id", string("The event id.", 1, 26),
            "status", string("emerging, active, resolved or dismissed.", 1, 16),
            "notes", string("A note.", 1, 4000),
            "version", integer("The version you last saw, for optimistic locking.", 0, 1_000_000, null)));

    private final IntelEventService events;
    private final JsonMapper json;

    /** Creates the tool. */
    public UpdateIntelEventStatusTool(IntelEventService events, JsonMapper json) {
        this.events = events;
        this.json = json;
    }

    @Override
    public String name() {
        return "update_intel_event_status";
    }

    @Override
    public String title() {
        return "Update intelligence event";
    }

    @Override
    public String description() {
        return "Updates an intelligence event's status, title or notes. Supply the version you last saw to guard"
                + " against a concurrent edit (409 EVENT_VERSION_CONFLICT).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The updated event");
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
        String id = McpArgs.requiredText(args, "id");
        var event = events.patch(id, args, context.actor());
        return ToolResult.json(json.valueToTree(IntelEventMapper.summary(event)));
    }
}
