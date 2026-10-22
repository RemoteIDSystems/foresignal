package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analyst.AnalystService;
import ai.foresignal.analyst.AnalystViews;
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
import tools.jackson.databind.json.JsonMapper;

/** {@code get_analyst_note} (read, prompt 102). */
@RestrictedRead
@Component
public class GetAnalystNoteTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", string("The note id.", 1, 26)), "id");

    private final AnalystService analyst;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetAnalystNoteTool(AnalystService analyst, IntelVisibility visibility, JsonMapper json) {
        this.analyst = analyst;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_analyst_note";
    }

    @Override
    public String title() {
        return "Get analyst note";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one analyst note with its typed claims. Cite the cited members;"
                + " inference and speculation are labeled and must be presented as such.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("One analyst note");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(AnalystViews.note(analyst.visibleNote(McpArgs.requiredText(args, "id"),
                visibility.seesPrivate(context.caller())))));
    }
}
