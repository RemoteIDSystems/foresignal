package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.place.PlaceMapper;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

/**
 * Shapes places for MCP (prompt 036): compact items without coordinates by default, and the full
 * {@code Place} (geometry and area) only for {@code detail=long} from an agent or admin token. A read
 * token never receives place coordinates.
 */
@Component
public class PlaceItems {

    /** The schema of a {@code PlaceInput} for the write tools. */
    public static final Map<String, Object> PLACE_SCHEMA = ai.foresignal.mcp.JsonSchemas.openObject(
            "A place, as PlaceInput in /v1/openapi.json: id (lower-case slug), name, shape (point, polygon or route),"
                    + " geometry (GeoJSON Point, Polygon/MultiPolygon or LineString in WGS84), radiusKm (point, 0.1-500,"
                    + " default 20), bufferM (route, 10-50000), timezone, kind (user, airport, asset), tags.",
            Map.of(), "id", "name", "shape", "geometry");

    private final PlaceMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the helper. */
    public PlaceItems(PlaceMapper mapper, McpOutputShaper shaper) {
        this.mapper = mapper;
        this.shaper = shaper;
    }

    /** True when {@code context}'s caller may see coordinates at {@code detail}. */
    public static boolean coordinates(ToolContext context, Detail detail) {
        Scope scope = context.caller().scope();
        return detail == Detail.LONG && scope != null && scope.includes(Scope.AGENT);
    }

    /** One place as {@code context}'s caller may see it. */
    public ObjectNode item(PlaceView place, ToolContext context, Detail detail) {
        return shaper.objectTree(coordinates(context, detail) ? mapper.place(place) : mapper.compact(place));
    }
}
