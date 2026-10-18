package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleThreadService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.AccessAction;
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
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_chronicle_thread} (read, prompt 131): a chronicle's thread of notable items, oldest first, as
 * {@code GET /v1/chronicles/{id}/thread}: alerts, findings, situation changes, hypothesis band changes, forecast
 * resolutions, first appearances, layer edits, capture gaps, cap warnings and status changes, each with the snapshot
 * kept when it happened.
 */
@RestrictedRead("private thread items need private access; envelopes are resolved for the caller")
@Component
public class GetChronicleThreadTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "chronicleId", string("The chronicle id or display id (CHR-yyyy-nnnn)."),
            "after", JsonSchemas.dateTime("Only items that happened after this time."),
            "types", JsonSchemas.enumArray("Only these item types.",
                    ChronicleVocabulary.ITEM_TYPES.toArray(String[]::new)),
            "cursor", string("The nextCursor of the previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)), "chronicleId");

    private final ChronicleService chronicles;
    private final ChronicleThreadService thread;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetChronicleThreadTool(ChronicleService chronicles, ChronicleThreadService thread, ObjectCallers callers,
            JsonMapper json) {
        this.chronicles = chronicles;
        this.thread = thread;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_chronicle_thread";
    }

    @Override
    public String title() {
        return "Get a chronicle's thread";
    }

    @Override
    public String description() {
        return "Returns a chronicle's thread, oldest first: alerts, findings, situation changes, hypothesis band"
                + " changes, forecast resolutions, first appearances, layer edits, capture gaps, cap warnings and status"
                + " changes, each with the snapshot kept at the time and the current envelope when the object still"
                + " exists. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A ChronicleThreadPage");
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
            throw new McpToolException("Reading a chronicle needs a token.");
        }
        ChronicleEntity c = chronicles.require(McpArgs.requiredText(args, "chronicleId"), caller, AccessAction.VIEW);
        List<String> types = McpArgs.strings(args, "types");
        for (String t : types) {
            if (!ChronicleVocabulary.ITEM_TYPES.contains(t)) {
                throw new McpToolException("types must be among " + ChronicleVocabulary.ITEM_TYPES);
            }
        }
        Map<String, Object> page = thread.page(c.getId(), caller, McpArgs.instant(args, "after"), types,
                McpArgs.text(args, "cursor"), Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))));
        return ToolResult.json(json.valueToTree(page));
    }
}
