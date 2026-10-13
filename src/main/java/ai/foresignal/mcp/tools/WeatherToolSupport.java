package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.SampleValue;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Shared plumbing of the weather MCP tools (prompt 052; Weather-Imagery-Design §6.9): where to sample (a
 * coordinate, or a place, which needs a token) and the compact form of a sampled value, which always carries
 * {@code validTime}, {@code localTime}, {@code ageMinutes}, {@code run}, {@code coverage} and {@code status}
 * (with the no-coverage text). Prompt 056 adds {@code citationId} ({@code wx:{productId}@{frameKey}}, forecasts
 * {@code wx:{productId}@{run}/{step}}), which Ask answers cite.
 */
@Component
public class WeatherToolSupport {

    private final PlaceService places;
    private final JsonMapper json;

    /** Creates the support. */
    public WeatherToolSupport(PlaceService places, JsonMapper json) {
        this.places = places;
        this.json = json;
    }

    /**
     * The point of {@code args}: {@code place}, or {@code lon} and {@code lat}.
     *
     * @throws McpToolException when neither or both are given, the place is unknown, or an anonymous caller names
     *     a place
     */
    public SampleService.Point point(JsonNode args, ToolContext context) {
        String placeId = McpArgs.text(args, "place");
        boolean hasLon = args != null && args.has("lon") && !args.get("lon").isNull();
        boolean hasLat = args != null && args.has("lat") && !args.get("lat").isNull();
        if (placeId != null && (hasLon || hasLat)) {
            throw new McpToolException("Give lon and lat, or place, not both.");
        }
        if (placeId != null) {
            if (context.caller().anonymous()) {
                throw new McpToolException("Sampling a place needs a token: places are never anonymous.");
            }
            PlaceView place = places.find(placeId).orElseThrow(() ->
                    new McpToolException("No place has the id '" + placeId + "'. Use list_places."));
            Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
            Point p = g instanceof Point point ? point : g.getInteriorPoint();
            return new SampleService.Point(p.getX(), p.getY(), place.id(), zone(place.timezone()));
        }
        if (!hasLon || !hasLat) {
            throw new McpToolException("Give lon and lat (WGS84 degrees), or place (a place id).");
        }
        double lon = args.get("lon").asDouble();
        double lat = args.get("lat").asDouble();
        if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
            throw new McpToolException("lon must be within -180..180 and lat within -90..90.");
        }
        return new SampleService.Point(lon, lat, null, ZoneOffset.UTC);
    }

    /** The zone of a place, else UTC. */
    static ZoneId zone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException e) {
            return ZoneOffset.UTC;
        }
    }

    /** The compact form of one sampled value. */
    public ObjectNode value(SampleValue v) {
        ObjectNode n = json.createObjectNode();
        n.put("productId", v.productId());
        if (v.value() != null) {
            n.put("value", v.value());
        }
        if (v.u() != null) {
            n.put("u", v.u());
            n.put("v", v.v());
        }
        if (v.derived() != null) {
            n.set("derived", json.valueToTree(v.derived()));
        }
        n.put("units", v.units());
        n.put("label", v.label());
        n.put("validTime", v.validTime() == null ? null : v.validTime().toString());
        n.put("localTime", v.localTime());
        if (v.ageMinutes() == null) {
            n.putNull("ageMinutes");
        } else {
            n.put("ageMinutes", v.ageMinutes());
        }
        n.put("run", v.run());
        n.put("coverage", v.coverage());
        n.put("status", v.status().wireValue());
        if (v.statusText() != null) {
            n.put("statusText", v.statusText());
        }
        String citationId = ai.foresignal.weather.WeatherCitations.of(v.productId(), v.frameKey(), v.run(), v.step());
        if (citationId != null) {
            n.put("citationId", citationId);
            n.put("frameKey", v.frameKey());
            if (v.step() != null) {
                n.put("step", v.step());
            }
        }
        return n;
    }

    /** The JSON tree of a map. */
    public ObjectNode tree(Map<String, Object> map) {
        return (ObjectNode) json.valueToTree(map);
    }

    /** A new empty object. */
    public ObjectNode object() {
        return json.createObjectNode();
    }
}
