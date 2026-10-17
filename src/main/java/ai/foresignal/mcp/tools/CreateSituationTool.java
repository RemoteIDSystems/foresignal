package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationThemes;
import ai.foresignal.situation.SituationViews;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_situation} (agent, prompt 118): a situation created by an agent ({@code origin=agent}); initial
 * members are verified against their pinned seq. Status and severity stay derived.
 */
@RestrictedRead
@Component
public class CreateSituationTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "title", string("A short title.", 1, 300),
            "theme", enumeration("The theme.", null, SituationThemes.ALL.toArray(String[]::new)),
            "regionCodes", stringArray("040 region codes.", 50),
            "topicIds", stringArray("117 attention topic ids.", 20),
            "members", memberArray()), "title", "theme");

    private final SituationService situations;
    private final SituationViews views;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public CreateSituationTool(SituationService situations, SituationViews views, IntelVisibility visibility,
            JsonMapper json) {
        this.situations = situations;
        this.views = views;
        this.visibility = visibility;
        this.json = json;
    }

    /** The schema of an array of {@code SituationMemberInput}. */
    static Map<String, Object> memberArray() {
        Map<String, Object> item = object(props(
                "memberType", enumeration("The member type.", null, "intel_event", "finding", "alert", "anomaly",
                        "record", "investigation"),
                "ref", string("The member id, or sourceId/externalId for records and anomalies."),
                "seq", JsonSchemas.nonNegative("The record version seq to pin (required for records and anomalies)."),
                "role", enumeration("core or context.", "core", "core", "context")), "memberType", "ref");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "array");
        out.put("description", "Initial members, each verified by its pinned seq.");
        out.put("items", item);
        out.put("maxItems", 500);
        return out;
    }

    @Override
    public String name() {
        return "create_situation";
    }

    @Override
    public String title() {
        return "Create situation";
    }

    @Override
    public String description() {
        return "Creates a situation (a persistent storyline) from a title, a theme and optional verified members."
                + " Status, severity and the summary are derived; nothing a model supplies sets them.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created situation");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        var situation = situations.create(args, context.actor(), "agent", visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(views.full(situation)));
    }
}
