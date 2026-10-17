package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.collab.CommentService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code comment} (read, prompt 130): starts a comment thread on an object ({@code ref}) or replies in one
 * ({@code replyTo}); {@code @handle} mentions notify the principals who can see the object. Commenting on a work
 * product needs the commenter role.
 */
@Component
public class CommentTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The object to comment on {type}:{id} (starts a thread)."),
            "replyTo", string("An annotation id to reply to (instead of ref)."),
            "body", string("Markdown text (at most 10,000 characters); @handle mentions a principal.", 1, 10_000),
            "private", JsonSchemas.bool("Make the comment private (039).", false)), "body");

    private final CommentService comments;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public CommentTool(CommentService comments, ObjectCallers callers, JsonMapper json) {
        this.comments = comments;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "comment";
    }

    @Override
    public String title() {
        return "Comment on an object";
    }

    @Override
    public String description() {
        return "Adds a comment to any Foresignal object, or replies in an existing thread with replyTo. Mention"
                + " colleagues with @handle: only those who can see the object are notified; the others come back in"
                + " droppedMentions.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The annotation");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Commenting needs a token.");
        }
        String replyTo = McpArgs.text(args, "replyTo");
        String ref = McpArgs.text(args, "ref");
        ObjectNode body = json.createObjectNode();
        body.put("body", McpArgs.requiredText(args, "body"));
        body.put("private", McpArgs.bool(args, "private", false));
        if (replyTo != null) {
            return ToolResult.json(json.valueToTree(comments.reply(replyTo, body, caller)));
        }
        if (ref == null) {
            throw new McpToolException("Give ref (a new thread) or replyTo (a reply).");
        }
        body.put("kind", "note");
        return ToolResult.json(json.valueToTree(comments.create(ObjectRef.parse(ref), body, caller)));
    }
}
