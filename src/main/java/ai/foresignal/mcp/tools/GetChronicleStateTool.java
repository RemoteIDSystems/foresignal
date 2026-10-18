package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleReplayService;
import ai.foresignal.chronicle.ChronicleService;
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
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_chronicle_state} (read, prompt 131): what a chronicle recorded at a moment, as
 * {@code GET /v1/chronicles/{id}/state?at=}: per layer, the latest captured version of each entity observed and known
 * by then and still valid, from chronicle storage only (no look-ahead, independent of global retention).
 */
@RestrictedRead("excluded sources and the read restriction are inside the replay SQL")
@Component
public class GetChronicleStateTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "chronicleId", string("The chronicle id or display id (CHR-yyyy-nnnn)."),
            "at", JsonSchemas.dateTime("The moment to replay."),
            "layer", string("One layer id to page through (default: the first page of every layer)."),
            "cursor", string("The nextCursor of a layer page."),
            "limit", JsonSchemas.integer("Items per layer.", 1, 200, 50L)), "chronicleId", "at");

    private final ChronicleService chronicles;
    private final ChronicleReplayService replay;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetChronicleStateTool(ChronicleService chronicles, ChronicleReplayService replay, ObjectCallers callers,
            JsonMapper json) {
        this.chronicles = chronicles;
        this.replay = replay;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_chronicle_state";
    }

    @Override
    public String title() {
        return "Replay a chronicle at a moment";
    }

    @Override
    public String description() {
        return "Returns what a chronicle recorded at a moment: per layer, the latest captured version of each entity"
                + " observed and known by then and still valid, read from the chronicle's own storage (so it works long"
                + " after global retention removed the data), with caveats for capture gaps. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A ChronicleState");
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
        Instant at = McpArgs.instant(args, "at");
        if (at == null) {
            throw new McpToolException("at is required (an RFC 3339 date-time)");
        }
        Map<String, Object> state = replay.state(c, at, McpArgs.text(args, "layer"), null, McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))),
                new ChronicleReplayService.Reader(context.excludedSources(), context.restriction()));
        return ToolResult.json(json.valueToTree(state));
    }
}
