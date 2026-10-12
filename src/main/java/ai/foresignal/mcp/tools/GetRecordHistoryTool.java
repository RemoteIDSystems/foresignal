package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_record_history} (read): a record's persisted versions in seq order, as
 * {@code listRecordVersions}, each with its {@code persistReason}.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetRecordHistoryTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 200;

    static final Map<String, Object> INPUT = object(props(
            "source", string("The record's source id.", 1, 200),
            "externalId", string("The record's id within its source.", 1, 500),
            "since", dateTime("Only versions observed at or after this RFC 3339 time."),
            "until", dateTime("Only versions observed before this RFC 3339 time."),
            "limit", integer("Most versions to return, oldest first (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, 20L),
            "detail", detail()), "source", "externalId");

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetRecordHistoryTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_record_history";
    }

    @Override
    public String title() {
        return "Get a record's history";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists one record's stored versions, oldest first, each with seq, version,"
                + " observedAt and persistReason (why it was kept: FIRST, SIGNIFICANT, CHANGE, SAMPLE, KEYFRAME,"
                + " TRIGGER_EDGE, TOMBSTONE…). Only persisted versions exist; live updates between them are not history.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Versions in seq order");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String source = McpArgs.requiredText(args, "source");
        String externalId = McpArgs.requiredText(args, "externalId");
        items.visibleSources(List.of(source), context.excludedSources());
        Instant since = McpArgs.instant(args, "since");
        Instant until = McpArgs.instant(args, "until");
        int limit = McpArgs.integer(args, "limit", 20);
        Slices.VersionSlice slice = queries.versions(source, externalId, since, until, null, limit, false,
                        context.excludedSources(), context.restriction())
                .orElseThrow(() -> new McpToolException("No record " + source + ":" + externalId + "."));
        return ToolResult.json(shaper.list(items.items(slice.rows(), RecordMapper.Shape.VERSION, detail),
                slice.rows().size(), slice.nextCursor() != null, detail, null));
    }
}
