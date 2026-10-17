package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.collab.ShareService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code share_object} (agent, prompt 130): shares a work product with principals, groups or the instance and
 * notifies them, as {@code POST /v1/objects/{ref}/share}. The object's TLP label bounds the targets
 * ({@code TLP_VIOLATION}); the share is audited.
 */
@Component
public class ShareObjectTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The work product {type}:{id} (investigation, workspace, saved search, note, attachment,"
                    + " evidence item or shared tag)."),
            "grantees", JsonSchemas.stringArray("principal:{id or handle}, group:{id} or instance.", 100),
            "role", JsonSchemas.enumeration("The role granted.", "viewer", "viewer", "commenter", "editor"),
            "message", string("A note to the recipients.", 0, 2000),
            "expiresAt", JsonSchemas.dateTime("When the grants end; omit for no end.")), "ref", "grantees");

    private final ShareService shares;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public ShareObjectTool(ShareService shares, ObjectCallers callers, JsonMapper json) {
        this.shares = shares;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "share_object";
    }

    @Override
    public String title() {
        return "Share a work product";
    }

    @Override
    public String description() {
        return "Shares one of your work products (you must be its owner or an editor) with principals, groups or the"
                + " whole instance, and notifies them. The TLP label decides who may receive it: RED only named"
                + " principals, AMBER+STRICT and AMBER the owner's organization, GREEN the instance, CLEAR anyone.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The ObjectAcl after the share");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Sharing needs a token.");
        }
        ObjectNode body = json.createObjectNode();
        ArrayNode grantees = body.putArray("grantees");
        McpArgs.strings(args, "grantees").forEach(grantees::add);
        String role = McpArgs.text(args, "role");
        if (role != null) {
            body.put("role", role);
        }
        String message = McpArgs.text(args, "message");
        if (message != null) {
            body.put("message", message);
        }
        String expiresAt = McpArgs.text(args, "expiresAt");
        if (expiresAt != null) {
            body.put("expiresAt", expiresAt);
        }
        return ToolResult.json(json.valueToTree(shares.share(McpArgs.requiredText(args, "ref"), body, caller,
                "mcp:share_object")));
    }
}
