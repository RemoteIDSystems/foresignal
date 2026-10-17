package ai.foresignal.mcp.tools;

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
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationViews;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code merge_situations} (agent, prompt 118): merges two situations; the older id survives. */
@RestrictedRead
@Component
public class MergeSituationsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "situationId", string("One situation."),
            "intoId", string("The other situation."),
            "reason", string("Why they are one storyline (stored in both logs).")), "situationId", "intoId");

    private final SituationService situations;
    private final SituationViews views;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public MergeSituationsTool(SituationService situations, SituationViews views, IntelVisibility visibility,
            JsonMapper json) {
        this.situations = situations;
        this.views = views;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "merge_situations";
    }

    @Override
    public String title() {
        return "Merge situations";
    }

    @Override
    public String description() {
        return "Merges two situations that are one storyline: the older keeps its id and receives the members, the"
                + " other is closed with mergedInto set. Both logs record the merge.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The surviving situation");
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
    public ToolResult call(ToolContext context, JsonNode args) {
        var survivor = situations.merge(McpArgs.requiredText(args, "situationId"), McpArgs.requiredText(args, "intoId"),
                null, McpArgs.text(args, "reason"), context.actor(), visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(views.full(survivor)));
    }
}
