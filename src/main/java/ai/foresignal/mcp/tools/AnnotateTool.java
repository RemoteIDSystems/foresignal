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
import ai.foresignal.object.AnnotationService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code annotate} (read, prompt 129): adds a note to any object; a verdict (confirmed, rejected, needs-review,
 * duplicate-of) needs agent scope, exactly as {@code createAnnotation}. Verdicts feed precision metrics; a rejected
 * verdict dismisses a finding or intel event only with {@code applyStatus: true}.
 */
@RestrictedRead("annotations of private objects are private")
@Component
public class AnnotateTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The annotated object {type}:{id}."),
            "kind", JsonSchemas.enumeration("note, or verdict (agent scope).", "note", "note", "verdict"),
            "verdict", JsonSchemas.enumeration("The verdict of a verdict.", null, "confirmed", "rejected",
                    "needs-review", "duplicate-of"),
            "duplicateOfRef", string("The original, for duplicate-of."),
            "body", string("Markdown text (at most 10,000 characters).", 0, 10_000),
            "tags", JsonSchemas.stringArray("Free tags.", 20),
            "applyStatus", JsonSchemas.bool("With a rejected verdict: also dismiss the finding or intel event.",
                    false)), "ref");

    private final AnnotationService annotations;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public AnnotateTool(AnnotationService annotations, ObjectCallers callers, JsonMapper json) {
        this.annotations = annotations;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "annotate";
    }

    @Override
    public String title() {
        return "Annotate an object";
    }

    @Override
    public String description() {
        return "Adds a note or a verdict to any Foresignal object. Notes need a token; verdicts (confirmed, rejected,"
                + " needs-review, duplicate-of) need agent scope and teach Foresignal which findings, events and"
                + " situations were right. Use applyStatus only when you mean to dismiss a rejected finding or event.";
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
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Annotating needs a token.");
        }
        ObjectRef ref = ObjectRef.parse(McpArgs.requiredText(args, "ref"));
        ObjectNode body = (ObjectNode) args.deepCopy();
        body.remove("ref");
        if (!body.has("kind")) {
            body.put("kind", "note");
        }
        return ToolResult.json(json.valueToTree(AnnotationService.view(annotations.create(ref, body,
                new ResolveContext(caller, null)))));
    }
}
