package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code list_satellite_passes} (read, prompt 049, Weather-Imagery-Design §6.9): the passes of the
 * operator's SatDump stations, newest first, optionally of one station, since a time and of one status.
 * Compact output of 4 KB or less, {@code detail=long} up to 50 KB.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class ListSatellitePassesTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    static final int MAX_LIMIT = 50;

    /** The default {@code limit}. */
    static final int DEFAULT_LIMIT = 20;

    /** Rows read before the status filter, so a filtered list can still fill. */
    static final int STATUS_SCAN = 200;

    static final Map<String, Object> INPUT = object(props(
            "station", string("The station name (the part after satdump- in the source id), for example home; omit"
                    + " for every station.", 1, 64),
            "since", dateTime("Only passes whose AOS is at or after this RFC 3339 time, for example"
                    + " 2026-10-12T00:00:00Z."),
            "status", enumeration("Only passes in this status: receiving (in progress), recorded (LOS seen, products"
                    + " pending), decoded (products joined) or failed (no products 20 min after LOS although the"
                    + " station had lock).", null, "receiving", "recorded", "decoded", "failed"),
            "limit", integer("Most passes to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, (long) DEFAULT_LIMIT),
            "detail", detail()));

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;
    private final SatellitePasses passes;

    /** Creates the tool. */
    public ListSatellitePassesTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper,
            SatellitePasses passes) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
        this.passes = passes;
    }

    @Override
    public String name() {
        return "list_satellite_passes";
    }

    @Override
    public String title() {
        return "List satellite passes";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists satellite passes received by the operator's own SatDump ground stations,"
                + " newest AOS first: satellite, AOS, LOS, max elevation, status, RF statistics and decoded products"
                + " (with thumbnail media ids). Use get_satellite_pass for one pass with its thumbnail image.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Satellite passes");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<String> sources = passes.sources(McpArgs.text(args, "station"), context.excludedSources());
        int limit = McpArgs.integer(args, "limit", DEFAULT_LIMIT);
        String status = McpArgs.text(args, "status");
        if (sources.isEmpty()) {
            return ToolResult.json(shaper.list(List.of(), 0, false, detail, Map.of()));
        }
        CurrentQuery query = new CurrentQuery(
                sources,
                List.of(SatellitePasses.KIND),
                List.of(),
                null,
                null,
                McpArgs.instant(args, "since"),
                null,
                TimeField.EVENT,
                null,
                false,
                ListOrder.OBSERVED,
                null,
                null,
                status == null ? limit : STATUS_SCAN,
                FieldSelection.ALL,
                GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON,
                false,
                context.excludedSources()).withRestriction(context.restriction());
        Slices.RecordSlice slice = queries.list(query);
        List<RecordRow> rows = new ArrayList<>();
        for (RecordRow row : slice.rows()) {
            if (row.tombstone() || (status != null && !status.equals(passes.status(row)))) {
                continue;
            }
            rows.add(row);
        }
        rows.sort((a, b) -> {
            Instant ta = a.eventTime() == null ? Instant.EPOCH : a.eventTime();
            Instant tb = b.eventTime() == null ? Instant.EPOCH : b.eventTime();
            return tb.compareTo(ta);
        });
        boolean more = rows.size() > limit || slice.nextCursor() != null;
        List<RecordRow> kept = rows.subList(0, Math.min(limit, rows.size()));
        return ToolResult.json(shaper.list(items.items(kept, RecordMapper.Shape.CURRENT, detail), kept.size(), more,
                detail, Map.of()));
    }
}
