package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.enumArray;

import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code create_intel_event} (agent, prompt 101): an operator or agent finding from verified members. */
@Component
public class CreateIntelEventTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "type", string("The event type.", 1, 64),
            "title", string("A short title.", 1, 512),
            "severity", enumArray("info, warning or critical.", "info", "warning", "critical"),
            "members", string("Members: an array of {memberType, sourceId, externalId, seq}.", 1, 200)));

    private final IntelEventService events;
    private final JsonMapper json;

    /** Creates the tool. */
    public CreateIntelEventTool(IntelEventService events, JsonMapper json) {
        this.events = events;
        this.json = json;
    }

    @Override
    public String name() {
        return "create_intel_event";
    }

    @Override
    public String title() {
        return "Create intelligence event";
    }

    @Override
    public String description() {
        return "Creates an intelligence event from one or more existing record versions. Confidence is computed by"
                + " the detector; a client-supplied confidence is rejected.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created event");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        var event = events.create(args, context.actor(), "agent");
        return ToolResult.json(json.valueToTree(IntelEventMapper.summary(event)));
    }
}
