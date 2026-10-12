package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.nonNegative;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_changes} (read): the change feed after a seq, as {@code listChanges}. A
 * {@code 410 RESYNC_REQUIRED} becomes a tool error telling the agent to re-query current state.
 */
@Component
public class GetChangesTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 500;

    static final Map<String, Object> INPUT = object(props(
            "after", nonNegative("Return changes with seq greater than this: 0 the first time, then the lastSeq of the"
                    + " previous call, or the watermarkSeq of query_records."),
            "source", stringArray("Only changes of these source ids.", 50),
            "kind", stringArray("Only changes of these kinds.", 50),
            "limit", integer("Most changes to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, 100L),
            "detail", detail()), "after");

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetChangesTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_changes";
    }

    @Override
    public String title() {
        return "Get changes since a seq";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns every new record version after a seq, in commit order, so an agent can"
                + " follow what changed since it last looked. Pass lastSeq back as after next time; hasMore=true"
                + " means call again at once. If the feed no longer reaches back that far the tool says so: then"
                + " re-query current state with query_records and continue from its watermarkSeq.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Changes in seq order; lastSeq, hasMore, watermarkSeq and purgeHorizon as on"
                + " GET /v1/changes");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        long after = McpArgs.requiredLong(args, "after");
        List<String> sources = items.visibleSources(McpArgs.strings(args, "source"), context.excludedSources());
        int limit = McpArgs.integer(args, "limit", 100);
        ChangesQuery query = new ChangesQuery(after, sources, McpArgs.strings(args, "kind"), List.of(), null,
                detail == Detail.LONG, limit, FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                context.excludedSources());
        Slices.ChangeSlice slice;
        try {
            slice = queries.changes(query);
        } catch (ForesignalException e) {
            if (ErrorCodes.RESYNC_REQUIRED.equals(e.getCode())) {
                throw new McpToolException("RESYNC_REQUIRED: the change feed no longer reaches back to seq " + after
                        + " (purge horizon " + e.getProperties().get("purgeHorizon") + "); retention removed records"
                        + " in between. Re-query current state with query_records and continue get_changes from its"
                        + " watermarkSeq (" + e.getProperties().get("watermarkSeq") + ").");
            }
            throw e;
        }
        List<ObjectNode> changes = slice.rows().stream().map(row -> items.change(row, detail)).toList();
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("lastSeq", slice.lastSeq());
        extra.put("hasMore", slice.hasMore());
        extra.put("watermarkSeq", slice.watermarkSeq());
        extra.put("purgeHorizon", slice.purgeHorizon());
        ObjectNode out = shaper.list(changes, changes.size(), slice.hasMore(), detail, extra);
        ArrayNode kept = (ArrayNode) out.get("items");
        if (kept.size() < changes.size()) {
            // The size cap dropped changes: resume right after the last one returned, never past it.
            long resume = kept.isEmpty() ? after : kept.get(kept.size() - 1).path("seq").asLong(after);
            out.put("lastSeq", resume);
            out.put("hasMore", true);
        }
        return ToolResult.json(out);
    }
}
