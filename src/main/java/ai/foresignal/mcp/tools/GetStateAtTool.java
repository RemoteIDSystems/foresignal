package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpTimes;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.store.query.AsOfQueryService;
import ai.foresignal.store.query.GeometryEncoding;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_state_at} (read, prompt 037): the records of a source or kind as Foresignal knew them
 * at a past instant (transaction time), through the same {@link AsOfQueryService} as
 * {@code GET /v1/records?asOf}, with {@code asOf}, {@code watermarkSeq}, {@code approximate} and
 * {@code horizon}. {@code place} narrows by named places and needs a token, as on REST.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetStateAtTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 200;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 50;

    static final Map<String, Object> INPUT = object(props(
            "source", stringArray("Source ids (from list_sources). A source or a kind is required.", 50),
            "kind", stringArray("Record kinds (from list_kinds), for example aircraft or earthquake.", 50),
            "asOf", string("The instant to replay: an RFC 3339 time such as 2026-10-12T08:00:00Z, 'now', or a"
                    + " duration back from now such as -PT2H or -2h. It is transaction time: what Foresignal had"
                    + " stored by then, never a later correction."),
            "bbox", string("Area filter as minLon,minLat,maxLon,maxLat in WGS84 degrees, applied to each record's"
                    + " position at asOf."),
            "place", stringArray("Named place ids (from list_places) to narrow to; needs a token.", 20),
            "limit", integer("Most records to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, (long) DEFAULT_LIMIT),
            "detail", detail()), "asOf");

    private final AsOfQueryService asOf;
    private final RecordItems items;
    private final McpOutputShaper shaper;
    private final ObjectProvider<ReadScopes> scopes;
    private final Clock clock;

    /** Creates the tool. */
    public GetStateAtTool(AsOfQueryService asOf, RecordItems items, McpOutputShaper shaper,
            ObjectProvider<ReadScopes> scopes, Clock clock) {
        this.asOf = asOf;
        this.items = items;
        this.shaper = shaper;
        this.scopes = scopes;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "get_state_at";
    }

    @Override
    public String title() {
        return "Records as known at a past time";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Replays the state of a source or kind as Foresignal knew it at asOf (transaction"
                + " time, to the granularity of one ingest batch): each entity at the version stored by then, valid"
                + " at that instant. Returns asOf, watermarkSeq (the last seq known then; cite it), approximate (true"
                + " when the source stores only some observations, so positions between versions are not exact)"
                + " and horizon (the earliest supported asOf; earlier is refused). Use get_track for one entity's"
                + " path over a window.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        Instant at = McpTimes.instant(args, "asOf", clock.instant(), null);
        if (at == null) {
            throw new McpToolException("asOf is required.");
        }
        List<String> sources = items.visibleSources(McpArgs.strings(args, "source"), context.excludedSources());
        List<String> kinds = McpArgs.strings(args, "kind");
        if (sources.isEmpty() && kinds.isEmpty()) {
            throw new McpToolException("Name a source or a kind: replaying everything at once is not supported.");
        }
        ReadRestriction restriction = context.restriction();
        List<String> places = McpArgs.strings(args, "place");
        if (!places.isEmpty()) {
            if (context.caller().anonymous()) {
                throw new McpToolException("place needs a token: places are never shown to anonymous callers.");
            }
            ReadScopes resolver = scopes.getIfAvailable();
            if (resolver == null) {
                throw new McpToolException("Places are not available on this server.");
            }
            restriction = resolver.narrow(restriction, new ReadScope(places, null, null, null), true);
        }
        int limit = McpArgs.integer(args, "limit", DEFAULT_LIMIT);
        AsOfQueryService.AsOfSlice slice = asOf.list(new AsOfQueryService.AsOfQuery(at, sources, kinds, List.of(),
                BboxParser.parse(McpArgs.text(args, "bbox")), null, null, limit, FieldSelection.ALL,
                GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, context.excludedSources(), restriction));
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("asOf", slice.asOf().toString());
        extra.put("watermarkSeq", slice.seq());
        extra.put("approximate", slice.approximate());
        extra.put("horizon", slice.horizon() == null ? null : slice.horizon().toString());
        return ToolResult.json(shaper.list(items.items(slice.rows(), RecordMapper.Shape.AS_OF, detail),
                slice.rows().size(), slice.nextCursor() != null, detail, extra));
    }
}
