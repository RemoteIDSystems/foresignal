package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.narrative.NarrativeViews;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_narrative} (read; prompt 136): one narrative with its claims, media fingerprints, coordination indicators,
 * linked news stories and situations, and its merge and split history.
 */
@Component
@RestrictedRead("narratives are aggregates; their member posts are read with listNarrativePosts under the restriction")
public class GetNarrativeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "narrativeId", JsonSchemas.string("Narrative id (or narrative:{id})")), "narrativeId");

    private final NarrativeViews views;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetNarrativeTool(NarrativeViews views, ObjectCallers callers, JsonMapper json) {
        this.views = views;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_narrative";
    }

    @Override
    public String title() {
        return "Get a social narrative";
    }

    @Override
    public String description() {
        return "Returns one social narrative: its claims (what, where, when, who; reports, never facts) with Admiralty"
                + " grades, shared media fingerprints, coordination indicators (statistics against null models, never"
                + " labels on accounts), linked news stories and situations, and history. Earliest posts are the"
                + " earliest Foresignal observed, never originators.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Narrative");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = ListNewsStoriesTool.text(args, "narrativeId");
        if (id == null) {
            return ToolResult.error("narrativeId is required");
        }
        if (id.startsWith("narrative:")) {
            id = id.substring("narrative:".length());
        }
        try {
            return ToolResult.json(json.valueToTree(views.detail(id,
                    new ResolveContext(callers.of(context.caller()), null))));
        } catch (NotFoundException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
