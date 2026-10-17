package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationSnapshots;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_situation_changes} (read, prompt 118): what changed since a day, from daily snapshots, no LLM. */
@RestrictedRead
@Component
public class GetSituationChangesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "id", string("The situation id."),
            "since", dateTime("The baseline time; the snapshot of its UTC day is compared with now.")), "id", "since");

    private final SituationService situations;
    private final SituationSnapshots snapshots;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetSituationChangesTool(SituationService situations, SituationSnapshots snapshots,
            IntelVisibility visibility, JsonMapper json) {
        this.situations = situations;
        this.snapshots = snapshots;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_situation_changes";
    }

    @Override
    public String title() {
        return "Get situation changes";
    }

    @Override
    public String description() {
        return "Returns what changed in a situation since a time: status and severity before and after, new members"
                + " by signal family and type, area change and regions added or removed, computed from the daily"
                + " snapshots without an LLM.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The changes");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "id");
        Instant since = McpArgs.instant(args, "since");
        if (since == null) {
            throw ValidationException.forField("since", "is required");
        }
        var situation = situations.visible(id, visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(snapshots.changes(situation, since)));
    }
}
