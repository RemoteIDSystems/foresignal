package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableTypes;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_shared_with_me} (read, prompt 130): work products shared with the calling principal (directly, through
 * a group or instance-wide), newest share first, as {@code GET /v1/shared-with-me}; {@code mine: true} lists the
 * caller's own work products instead ({@code GET /v1/my}).
 */
@RestrictedRead
@Component
public class ListSharedWithMeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "type", JsonSchemas.enumeration("Only this shareable type.", null,
                    ShareableTypes.ALL.toArray(String[]::new)),
            "mine", JsonSchemas.bool("List the work products you own instead.", false),
            "cursor", string("The nextCursor of the previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)));

    private final ShareService shares;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListSharedWithMeTool(ShareService shares, ObjectCallers callers, JsonMapper json) {
        this.shares = shares;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_shared_with_me";
    }

    @Override
    public String title() {
        return "List work shared with me";
    }

    @Override
    public String description() {
        return "Lists investigations, workspaces, saved searches, notes, attachments, evidence items and shared tags"
                + " that other principals shared with you, with your role (viewer, commenter, editor, owner), how it"
                + " reached you (principal, group, instance) and its TLP label. With mine=true lists your own.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A SharedItemPage, or an ObjectEnvelopePage with mine=true");
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
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Listing shared work needs a token.");
        }
        String type = McpArgs.text(args, "type");
        String cursor = McpArgs.text(args, "cursor");
        int limit = McpArgs.integer(args, "limit", 50);
        Map<String, Object> page = McpArgs.bool(args, "mine", false)
                ? shares.mine(caller, type, cursor, limit)
                : shares.sharedWithMe(caller, type, cursor, limit);
        return ToolResult.json(json.valueToTree(page));
    }
}
