package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code get_intel_event} (read, prompt 101): the full event, evidence first, speculation labelled. */
@RestrictedRead
@Component
public class GetIntelEventTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The event id.")), "id");

    private final IntelEventService events;
    private final IntelEventMapper mapper;
    private final IntelVisibility visibility;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the tool. */
    public GetIntelEventTool(IntelEventService events, IntelEventMapper mapper, IntelVisibility visibility,
            tools.jackson.databind.json.JsonMapper json) {
        this.events = events;
        this.visibility = visibility;
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_intel_event";
    }

    @Override
    public String title() {
        return "Get intelligence event";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one intelligence event with its evidence (pinned members), its"
                + " inference (type, severity, confidence, geometry) and its speculation (alternatives, any LLM"
                + " narrative, always labelled). Cite members; confidence is not calibrated.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("One intelligence event");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "id");
        var event = events.visible(id, visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(mapper.full(event, events.members(id), events.log(id))));
    }
}
