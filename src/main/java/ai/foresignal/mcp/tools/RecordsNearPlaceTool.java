package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
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
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import ai.foresignal.view.RecordScopes;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code records_near_place} (read, always authenticated): current records inside named places'
 * areas (prompt 036), the same query as {@code GET /v1/records?place=}. The place's own coordinates
 * are never returned; only its name.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class RecordsNearPlaceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "placeIds", stringArray("Place ids (from list_places); records inside any of their areas match.", 20),
            "radiusKm", number("Extra buffer around the places' areas in kilometers.", 0, 500),
            "kind", stringArray("Record kinds to include; omit for every kind.", 50),
            "source", stringArray("Source ids to search; omit for every source.", 50),
            "limit", integer("Most records to return (1-" + QueryRecordsTool.MAX_LIMIT + ").", 1, QueryRecordsTool.MAX_LIMIT,
                    (long) QueryRecordsTool.DEFAULT_LIMIT),
            "detail", detail()), "placeIds");

    private final RecordQueryService queries;
    private final RecordScopes scopes;
    private final PlaceService places;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public RecordsNearPlaceTool(RecordQueryService queries, RecordScopes scopes, PlaceService places, RecordItems items,
            McpOutputShaper shaper) {
        this.queries = queries;
        this.scopes = scopes;
        this.places = places;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "records_near_place";
    }

    @Override
    public String title() {
        return "Records near a place";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists the current records (earthquakes, aircraft, alerts, outages…) inside one or more"
                + " named places' areas (a point place is its radius; a polygon or route its own area), optionally"
                + " buffered by radiusKm, newest observation first. Items are cited like query_records items.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Current records inside the places");
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
        Detail detail = Detail.of(args);
        List<String> ids = McpArgs.strings(args, "placeIds");
        if (ids.isEmpty()) {
            throw new McpToolException("placeIds must name at least one place (from list_places).");
        }
        List<PlaceView> resolved = places.resolve(ids);
        String radius = McpArgs.numberText(args, "radiusKm");
        Double radiusKm = radius == null ? null : Double.parseDouble(radius);
        ReadRestriction restriction = context.restriction().withFilter(scopes.places(ids, radiusKm));
        int limit = McpArgs.integer(args, "limit", QueryRecordsTool.DEFAULT_LIMIT);
        CurrentQuery query = new CurrentQuery(items.visibleSources(McpArgs.strings(args, "source"),
                context.excludedSources()), McpArgs.strings(args, "kind"), List.of(), null, null, null, null,
                TimeField.EFFECTIVE, null, false, ListOrder.OBSERVED, null, null, limit, FieldSelection.ALL,
                GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, context.excludedSources())
                .withRestriction(restriction);
        Slices.RecordSlice slice = queries.list(query);
        return ToolResult.json(shaper.list(items.items(slice.rows(), RecordMapper.Shape.CURRENT, detail),
                slice.rows().size(), slice.nextCursor() != null, detail,
                Map.of("places", resolved.stream().map(PlaceView::name).toList(), "watermarkSeq", slice.watermarkSeq())));
    }
}
