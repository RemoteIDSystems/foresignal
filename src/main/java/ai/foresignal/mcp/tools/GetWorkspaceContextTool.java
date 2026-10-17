package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.WorkspacePresence;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_workspace_context} (read, prompt 129): a workspace's shared context (selection, focus, time range,
 * viewport, active situation and hypothesis set) with its version and the connected clients, so an agent knows what
 * the operator is looking at. Without an id, the caller's workspaces.
 */
@RestrictedRead("workspaces are visible to their owner and members only")
@Component
public class GetWorkspaceContextTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "workspaceId", string("The workspace id (ULID); omit to list your workspaces.")));

    private final WorkspaceService workspaces;
    private final WorkspacePresence presence;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetWorkspaceContextTool(WorkspaceService workspaces, WorkspacePresence presence, ObjectCallers callers,
            JsonMapper json) {
        this.workspaces = workspaces;
        this.presence = presence;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_workspace_context";
    }

    @Override
    public String title() {
        return "Get workspace context";
    }

    @Override
    public String description() {
        return "Returns a workspace's shared context: the selected objects, focus, time range, viewport, active"
                + " situation and hypothesis set, its version and the clients following it. Without workspaceId,"
                + " lists your workspaces. Read it to know what the operator is looking at.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The workspace context");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Workspaces need a token.");
        }
        String id = McpArgs.text(args, "workspaceId");
        if (id == null) {
            return ToolResult.json(json.valueToTree(workspaces.list(caller, null, 50)));
        }
        Map<String, Object> out = new LinkedHashMap<>(WorkspaceService.contextState(workspaces.get(id, caller)));
        out.put("clients", presence.clients(id));
        return ToolResult.json(json.valueToTree(out));
    }
}
