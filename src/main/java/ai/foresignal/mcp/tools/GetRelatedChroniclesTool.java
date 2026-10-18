package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.network.ChronicleNetworkService;
import ai.foresignal.chronicle.network.NetworkVocabulary;
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
 * {@code get_related_chronicles} (read, prompt 134): the links of a chronicle at the caller's disclosure level, as
 * {@code GET /v1/chronicles/{id}/related}: full links (shared canonical events and entities) to chronicles the caller
 * can see, opaque links (strength band, dimensions, time overlap; title and owner only if revealed) to chronicles that
 * are only discoverable to it.
 */
@RestrictedRead("envelopes are resolved for the caller; opaque links carry no content")
@Component
public class GetRelatedChroniclesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "chronicleId", string("The chronicle id or display id (CHR-yyyy-nnnn)."),
            "minStrength", JsonSchemas.enumeration("Only links at least this strong.", null,
                    NetworkVocabulary.STRENGTHS.toArray(String[]::new)),
            "includeDismissed", JsonSchemas.bool("Include dismissed links.", false),
            "cursor", string("The nextCursor of the previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)), "chronicleId");

    private final ChronicleNetworkService network;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetRelatedChroniclesTool(ChronicleNetworkService network, ObjectCallers callers, JsonMapper json) {
        this.network = network;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_related_chronicles";
    }

    @Override
    public String title() {
        return "Get related chronicles";
    }

    @Override
    public String description() {
        return "Returns the chronicles related to one of yours, found by comparing fingerprints of what they captured"
                + " (never their contents). A full link (both chronicles visible to you) lists the shared canonical"
                + " events and entities; an opaque link only says a related thread exists, with its strength band,"
                + " overlapping dimensions and time overlap (title and owner only if its owner revealed them); ask for"
                + " access with POST /v1/chronicle-network/links/{linkId}/request-access. Never infer more from an"
                + " opaque link. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A ChronicleRelatedPage");
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
            throw new McpToolException("Reading related chronicles needs a token.");
        }
        String min = McpArgs.text(args, "minStrength");
        if (min != null && !NetworkVocabulary.STRENGTHS.contains(min)) {
            throw new McpToolException("minStrength must be low, medium or high");
        }
        Map<String, Object> page = network.related(McpArgs.requiredText(args, "chronicleId"), caller, min,
                McpArgs.bool(args, "includeDismissed", false), McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))));
        return ToolResult.json(json.valueToTree(page));
    }
}
