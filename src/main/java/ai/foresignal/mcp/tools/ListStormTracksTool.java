package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.adapters.wx.WxStormTracksGenerator;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code list_storm_tracks} (read, prompt 105; Weather-Imagery-Design §6.9): the active TITAN-lite
 * storm tracks near a point or place, compactly: cell summary, motion, trend, lightning-jump flag and
 * nowcast leads, each with its valid time, run (none: tracks are observations), age in minutes and
 * status. Automated guidance, never a warning.
 */
@RestrictedRead
@Component
public class ListStormTracksTool implements ForesignalMcpTool {

    /** The default search radius, km. */
    static final double DEFAULT_RADIUS_KM = 50;

    /** The most tracks returned. */
    static final int LIMIT = 50;

    static final Map<String, Object> INPUT = object(props(
            "lon", number("Longitude, WGS84 degrees (with lat).", -180, 180),
            "lat", number("Latitude, WGS84 degrees (with lon).", -90, 90),
            "place", string("A place id (from list_places) instead of lon/lat; needs a token.", 1, 64),
            "radiusKm", number("Search radius, km (default 50, max 250).", 1, 250),
            "detail", detail()));

    private final RecordQueryService records;
    private final SampleService samples;
    private final WeatherToolSupport support;
    private final McpOutputShaper shaper;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the tool. */
    public ListStormTracksTool(RecordQueryService records, SampleService samples, WeatherToolSupport support,
            McpOutputShaper shaper, tools.jackson.databind.json.JsonMapper json) {
        this.records = records;
        this.samples = samples;
        this.support = support;
        this.shaper = shaper;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_storm_tracks";
    }

    @Override
    public String title() {
        return "List storm tracks";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists active TITAN-lite storm tracks near a point or place: each carries its current"
                + " cell (max dBZ, area, hail, flash rate), motion, max-dBZ trend, a GLM lightning-jump flag and the"
                + " +15/+30/+60-minute nowcast leads. Automated guidance, not a warning.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Active storm tracks near the point");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        SampleService.Point point = support.point(args, context);
        double radiusKm = DEFAULT_RADIUS_KM;
        String radiusText = McpArgs.numberText(args, "radiusKm");
        if (radiusText != null) {
            try {
                radiusKm = Double.parseDouble(radiusText);
            } catch (NumberFormatException e) {
                radiusKm = DEFAULT_RADIUS_KM;
            }
        }
        radiusKm = Math.max(1, Math.min(250, radiusKm));
        ObjectNode out = support.object();
        if (point.placeId() != null) {
            out.put("placeId", point.placeId());
        } else {
            out.put("lon", point.lon());
            out.put("lat", point.lat());
        }
        out.put("timezone", point.zone().getId());
        out.put("serverTime", samples.now().toString());
        ArrayNode tracks = out.putArray("tracks");
        Set<String> excluded = context.excludedSources();
        if (!excluded.contains(WxStormTracksGenerator.ID)) {
            CurrentQuery query = new CurrentQuery(List.of(WxStormTracksGenerator.ID),
                    List.of(WxStormTracksGenerator.TRACK_KIND), List.of(), null,
                    new NearFilter(point.lon(), point.lat(), radiusKm), null, null, TimeField.EFFECTIVE, null, false,
                    ListOrder.OBSERVED, null, null, LIMIT, FieldSelection.ALL, GeometryOptions.DEFAULT,
                    GeometryEncoding.GEOJSON, false, excluded).withRestriction(context.restriction());
            for (RecordRow row : records.list(query).rows()) {
                Map<String, Object> payload = payloadOf(row);
                if (!row.tombstone() && "active".equals(payload.get("status"))) {
                    tracks.add(compact(row, payload));
                }
            }
        }
        return ToolResult.json(shaper.object(out, detail));
    }

    private Map<String, Object> payloadOf(RecordRow row) {
        if (row.payload() == null || row.payload().isBlank()) {
            return Map.of();
        }
        try {
            return json.readValue(row.payload(),
                    new tools.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (tools.jackson.core.JacksonException e) {
            return Map.of();
        }
    }

    private ObjectNode compact(RecordRow row, Map<String, Object> payload) {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("trackId", row.externalId());
        item.put("status", String.valueOf(payload.getOrDefault("status", "active")));
        if (payload.get("cell") != null) {
            item.put("cell", payload.get("cell"));
        }
        if (payload.get("motion") != null) {
            item.put("motion", payload.get("motion"));
        }
        if (payload.get("trend") != null) {
            item.put("trend", payload.get("trend"));
        }
        if (payload.get("lightningJump") != null) {
            item.put("lightningJump", payload.get("lightningJump"));
        }
        if (payload.get("nowcast") instanceof List<?> list) {
            List<Integer> leads = new java.util.ArrayList<>();
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> m && m.get("leadMinutes") instanceof Number n) {
                    leads.add(n.intValue());
                }
            }
            item.put("nowcast", leads);
            item.put("nowcastMethod", "linear-advection");
        }
        if (row.observedAt() != null) {
            item.put("validTime", row.observedAt().toString());
            item.put("ageMinutes", Duration.between(row.observedAt(), samples.now()).toMinutes());
        }
        item.put("automated", true);
        item.put("run", null);
        return support.tree(item);
    }
}
