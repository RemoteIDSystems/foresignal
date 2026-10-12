package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code reverse_geocode} (read, prompt 040): the {@code RegionTags} of a point, as
 * {@code GET /v1/reference/reverse}. Uses only the loaded reference data, never an online geocoder;
 * returns no records.
 */
@Component
public class ReverseGeocodeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "lon", number("Longitude in degrees (WGS84)", -180, 180),
            "lat", number("Latitude in degrees (WGS84)", -90, 90)), "lon", "lat");

    private final RegionTagger tagger;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ReverseGeocodeTool(RegionTagger tagger, McpOutputShaper shaper) {
        this.tagger = tagger;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "reverse_geocode";
    }

    @Override
    public String title() {
        return "Reverse-geocode a point";
    }

    @Override
    public String description() {
        return "Returns the region context of a point: country (ISO2, null at sea), first-level region (ISO 3166-2,"
                + " e.g. US-TX), US county (FIPS5), sea area, and the nearest populated place with distance and"
                + " bearing as text such as \"12 km NNE of Austin, TX\". Computed offline from Natural Earth and"
                + " GeoNames data (place names CC BY 4.0, GeoNames); fields are null when the reference data is not"
                + " loaded.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("RegionTags");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        double lon = coordinate(args, "lon", 180);
        double lat = coordinate(args, "lat", 90);
        return ToolResult.json(shaper.tree(tagger.tag(lon, lat).toMap()));
    }

    private static double coordinate(JsonNode args, String name, double limit) {
        JsonNode node = args == null ? null : args.get(name);
        if (node == null || !node.isNumber()) {
            throw new McpToolException("The argument '" + name + "' must be a number.");
        }
        double value = node.asDouble();
        if (!Double.isFinite(value) || Math.abs(value) > limit) {
            throw new McpToolException("The argument '" + name + "' must be between -" + (int) limit + " and "
                    + (int) limit + ".");
        }
        return value;
    }
}
