package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationMapper;
import ai.foresignal.situation.SituationService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** {@code link_to_situation} (agent, prompt 118): adds a verified member to a situation, logged with the actor. */
@RestrictedRead
@Component
public class LinkToSituationTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "situationId", string("The situation id."),
            "memberType", enumeration("The member type.", null, "intel_event", "finding", "alert", "anomaly",
                    "record", "investigation"),
            "ref", string("The member id, or sourceId/externalId for records and anomalies."),
            "seq", JsonSchemas.nonNegative("The record version seq to pin (required for records and anomalies)."),
            "role", enumeration("core or context.", "core", "core", "context"),
            "moveFromSituationId", string("Move the member from this situation (logged on both).")),
            "situationId", "memberType", "ref");

    private final SituationService situations;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public LinkToSituationTool(SituationService situations, IntelVisibility visibility, JsonMapper json) {
        this.situations = situations;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "link_to_situation";
    }

    @Override
    public String title() {
        return "Link to situation";
    }

    @Override
    public String description() {
        return "Adds an existing intelligence event, finding, alert, anomaly, record or investigation to a situation"
                + " as a pinned member (records are verified by seq). The situation's status and severity are"
                + " re-derived; the join reason records the agent.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The member and whether it was added");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String situationId = McpArgs.requiredText(args, "situationId");
        ObjectNode body = (ObjectNode) args.deepCopy();
        body.remove("situationId");
        var result = situations.addMember(situationId, body, context.actor(), "agent",
                visibility.seesPrivate(context.caller()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("added", result.created());
        out.put("member", SituationMapper.member(result.member()));
        return ToolResult.json(json.valueToTree(out));
    }
}
