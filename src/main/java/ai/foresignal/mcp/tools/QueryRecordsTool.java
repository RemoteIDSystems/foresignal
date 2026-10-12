package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.NearParser;
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
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code query_records} (read): current records from {@code record_head ⋈ record_live}, the same
 * query as {@code GET /v1/records} (invariant HIST-2), with the caller's private sources excluded.
 */
@Component
public class QueryRecordsTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 200;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 20;

    static final Map<String, Object> INPUT = object(props(
            "source", stringArray("Source ids to search (from list_sources); omit for every source.", 50),
            "kind", stringArray("Record kinds to include (from list_kinds), for example earthquake or aircraft;"
                    + " omit for every kind.", 50),
            "bbox", string("Area filter as minLon,minLat,maxLon,maxLat in WGS84 degrees, for example"
                    + " -125,24,-66,50. minLon > maxLon crosses the antimeridian."),
            "near", string("Center of a radius search as lon,lat in WGS84 degrees, for example -122.42,37.77."
                    + " Needs radiusKm; results are then ordered nearest first and carry distanceKm."),
            "radiusKm", number("Radius of the near search in kilometers (up to 2000).", 0.001, NearParser.MAX_RADIUS_KM),
            "since", dateTime("Only records whose timeField is at or after this RFC 3339 time, for example"
                    + " 2026-10-12T00:00:00Z."),
            "until", dateTime("Only records whose timeField is before this RFC 3339 time."),
            "timeField", enumeration("The time axis of since/until: effective (default: event time when the source"
                    + " has one, else observation time), observed, event or ingested.", "effective",
                    "effective", "observed", "event", "ingested"),
            "q", string("Case-insensitive text match on the record's searchable fields (title, place, name…).", 1, 200),
            "limit", integer("Most records to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, (long) DEFAULT_LIMIT),
            "detail", detail()));

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public QueryRecordsTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "query_records";
    }

    @Override
    public String title() {
        return "Query current records";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists the current state of records (earthquakes, aircraft, vessels, alerts,"
                + " outages…) filtered by source, kind, area (bbox or near+radiusKm), time window and text, newest"
                + " observation first (nearest first with near). Each item has id (source:externalId), seq and"
                + " version (cite these), kind, title, observedAt, live and position. live=true means the live"
                + " fields are newer than the cited version; persistedObservedAt is the cited version's time."
                + " Use get_record for one record in full and get_record_history for its past versions.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Current records; watermarkSeq is the change-feed position the snapshot is"
                + " consistent with (pass it to get_changes as after)");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<String> sources = items.visibleSources(McpArgs.strings(args, "source"), context.excludedSources());
        NearFilter near = NearParser.parse(McpArgs.text(args, "near"), McpArgs.numberText(args, "radiusKm"));
        Instant since = McpArgs.instant(args, "since");
        Instant until = McpArgs.instant(args, "until");
        if (since != null && until != null && !since.isBefore(until)) {
            throw new McpToolException("until must be after since.");
        }
        int limit = McpArgs.integer(args, "limit", DEFAULT_LIMIT);
        CurrentQuery query = new CurrentQuery(
                sources,
                McpArgs.strings(args, "kind"),
                List.of(),
                BboxParser.parse(McpArgs.text(args, "bbox")),
                near,
                since,
                until,
                TimeField.parse(McpArgs.text(args, "timeField"), TimeField.EFFECTIVE),
                McpArgs.text(args, "q"),
                false,
                near == null ? ListOrder.OBSERVED : ListOrder.DISTANCE,
                null,
                null,
                limit,
                FieldSelection.ALL,
                GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON,
                false,
                context.excludedSources());
        Slices.RecordSlice slice = queries.list(query);
        return ToolResult.json(shaper.list(items.items(slice.rows(), RecordMapper.Shape.CURRENT, detail),
                slice.rows().size(), slice.nextCursor() != null, detail,
                Map.of("watermarkSeq", slice.watermarkSeq())));
    }
}
