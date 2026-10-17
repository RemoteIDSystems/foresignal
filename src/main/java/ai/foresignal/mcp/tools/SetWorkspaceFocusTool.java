package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code set_workspace_focus} (agent, prompt 129): points every client following a workspace at an object (sets
 * {@code focusRef} and adds it to {@code selectedRefs}), so the operator sees what the agent is talking about. The
 * agent needs the owner's or an editor's role on the workspace; the patch is broadcast as {@code workspace.context}
 * with {@code originClientId} {@code mcp:{session}}.
 */
@RestrictedRead("the focused object must be visible to the agent")
@Component
public class SetWorkspaceFocusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "workspaceId", string("The workspace id (ULID)."),
            "ref", string("The object to focus, {type}:{id}.")), "workspaceId", "ref");

    private final WorkspaceService workspaces;
    private final ObjectService objects;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public SetWorkspaceFocusTool(WorkspaceService workspaces, ObjectService objects, ObjectCallers callers,
            JsonMapper json) {
        this.workspaces = workspaces;
        this.objects = objects;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "set_workspace_focus";
    }

    @Override
    public String title() {
        return "Set workspace focus";
    }

    @Override
    public String description() {
        return "Points every open client of a workspace at an object: sets the workspace's focusRef and selects it."
                + " Use it to show the operator what you mean on the globe, the analyst workstation and the other apps"
                + " following the workspace.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The new workspace context state");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        ObjectRef ref = ObjectRef.parse(McpArgs.requiredText(args, "ref"));
        String canonical = objects.get(ref, new ResolveContext(caller, null)).ref();
        String origin = "mcp:" + (context.sessionId() == null ? "session" : context.sessionId());
        var w = workspaces.setFocus(McpArgs.requiredText(args, "workspaceId"), ObjectRef.parse(canonical), origin,
                caller);
        return ToolResult.json(json.valueToTree(WorkspaceService.contextState(w)));
    }
}
