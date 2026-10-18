package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
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
 * {@code list_chronicles} (read, prompt 131): the chronicles the caller owns or that are shared with it, as
 * {@code GET /v1/chronicles}, with status, capture span, storage against the cap and unread thread items.
 */
@RestrictedRead("returns chronicle metadata the caller may see (130 access lists), no captured records")
@Component
public class ListChroniclesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", JsonSchemas.enumeration("Only chronicles in this status.", null,
                    ChronicleVocabulary.STATUSES.toArray(String[]::new)),
            "scope", JsonSchemas.enumeration("Owned, shared with you, or both.", "all", "mine", "shared", "all"),
            "situationId", string("Only chronicles linked to this situation."),
            "q", string("A name match."),
            "cursor", string("The nextCursor of the previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)));

    private final ChronicleService chronicles;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListChroniclesTool(ChronicleService chronicles, ObjectCallers callers, JsonMapper json) {
        this.chronicles = chronicles;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_chronicles";
    }

    @Override
    public String title() {
        return "List chronicles";
    }

    @Override
    public String description() {
        return "Lists chronicles (named monitoring threads that record their own history) you own or that are shared"
                + " with you: status (proposed, active, paused, stopped, archived), layers, capture span, bytes used"
                + " against the size cap, and your unread thread items. Read a thread with get_chronicle_thread and"
                + " replay a moment with get_chronicle_state.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A ChroniclePage");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Listing chronicles needs a token.");
        }
        String status = McpArgs.text(args, "status");
        if (status != null && !ChronicleVocabulary.isStatus(status)) {
            throw new McpToolException("status must be one of " + ChronicleVocabulary.STATUSES);
        }
        String scope = McpArgs.text(args, "scope");
        Map<String, Object> page = chronicles.list(caller, status, scope == null ? "all" : scope,
                McpArgs.text(args, "situationId"), McpArgs.text(args, "q"), McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))));
        return ToolResult.json(json.valueToTree(page));
    }
}
