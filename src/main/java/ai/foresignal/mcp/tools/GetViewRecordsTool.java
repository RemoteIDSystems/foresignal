package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import ai.foresignal.view.RecordScopes;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_view_records} (read): the current records of a saved view, as {@code getViewRecords}. A
 * view that references a place or a watchlist needs a token.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetViewRecordsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "viewId", string("The view id or slug (from list_views).", 1, 64),
            "limit", integer("Most records to return (1-" + QueryRecordsTool.MAX_LIMIT + ").", 1, QueryRecordsTool.MAX_LIMIT,
                    (long) QueryRecordsTool.DEFAULT_LIMIT),
            "detail", detail()), "viewId");

    private final SavedViewService views;
    private final RecordScopes scopes;
    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetViewRecordsTool(SavedViewService views, RecordScopes scopes, RecordQueryService queries,
            RecordItems items, McpOutputShaper shaper) {
        this.views = views;
        this.scopes = scopes;
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_view_records";
    }

    @Override
    public String title() {
        return "Records of a saved view";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists the current records a saved view selects (its sources, kinds, places and"
                + " conditions), newest observation first. Items are cited like query_records items.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("The view's current records");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        SavedViewEntity view = views.get(McpArgs.requiredText(args, "viewId"));
        var restriction = scopes.narrow(context.restriction(),
                new ReadScope(List.of(), null, view.getId().toString(), null), !context.caller().anonymous());
        int limit = McpArgs.integer(args, "limit", QueryRecordsTool.DEFAULT_LIMIT);
        FieldSelection fields = view.getFields().isEmpty() ? FieldSelection.ALL
                : FieldSelector.parse(String.join(",", view.getFields()));
        CurrentQuery query = new CurrentQuery(List.of(), List.of(), List.of(), null, null, null, null,
                TimeField.parse(view.getTimeField(), TimeField.EFFECTIVE), null, view.isIncludeExpired(),
                ListOrder.OBSERVED, null, null, limit, fields, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false,
                context.excludedSources()).withRestriction(restriction);
        Slices.RecordSlice slice;
        try {
            slice = queries.list(query);
        } catch (IllegalStateException e) {
            throw new McpToolException("The view cannot be rendered: " + e.getMessage());
        }
        return ToolResult.json(shaper.list(items.items(slice.rows(), RecordMapper.Shape.CURRENT, detail),
                slice.rows().size(), slice.nextCursor() != null, detail,
                Map.of("view", view.getSlug(), "version", view.getVersion(), "watermarkSeq", slice.watermarkSeq())));
    }
}
