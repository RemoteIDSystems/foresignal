package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
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

/** {@code get_situation} (read, prompt 118): one situation, evidence first, the brief labeled speculation. */
@RestrictedRead
@Component
public class GetSituationTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The situation id.")), "id");

    private final SituationService situations;
    private final SituationViews views;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetSituationTool(SituationService situations, SituationViews views, IntelVisibility visibility,
            JsonMapper json) {
        this.situations = situations;
        this.views = views;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_situation";
    }

    @Override
    public String title() {
        return "Get situation";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one situation: evidence (members grouped by signal family with their join"
                + " reasons and the corroboration roll-up), inference (status, severity and the deterministic summary)"
                + " and speculation (the optional LLM brief, labeled). Present the evidence first and quote the"
                + " corroboration share; cite members.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("One situation");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "id");
        return ToolResult.json(json.valueToTree(views.full(situations.visible(id,
                visibility.seesPrivate(context.caller())))));
    }
}
