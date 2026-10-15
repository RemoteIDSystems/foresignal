package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.ExposureService;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.spi.Geo;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_exposure} (read, prompt 101): facilities, places, live entities and population inside an area. */
@RestrictedRead
@Component
public class GetExposureTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of(
            "eventId", string("An event id to use its area.", 1, 26),
            "placeId", string("A place id to use its area.", 1, 64)));

    private final ExposureService exposure;
    private final PlaceService places;
    private final IntelEventService events;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetExposureTool(ExposureService exposure, PlaceService places, IntelEventService events, JsonMapper json) {
        this.exposure = exposure;
        this.places = places;
        this.events = events;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_exposure";
    }

    @Override
    public String title() {
        return "Exposure of an area";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Counts reference facilities, the operator's places, live aircraft and vessels and,"
                + " when a population pack is installed, an estimated residential population inside an area. The"
                + " population is a model, not a casualty estimate.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Exposure of an area");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Geometry area = null;
        if (McpArgs.text(args, "eventId") != null) {
            area = events.get(McpArgs.text(args, "eventId")).getGeom();
        } else if (McpArgs.text(args, "placeId") != null) {
            area = places.geometry(McpArgs.text(args, "placeId")).orElse(null);
        } else if (args.hasNonNull("geometry")) {
            area = Geo.fromGeoJson(args.get("geometry").toString());
        }
        if (area == null) {
            throw new ai.foresignal.mcp.McpToolException("Provide eventId, placeId or geometry.");
        }
        return ToolResult.json(json.valueToTree(exposure.exposure(area, true)));
    }
}
