package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code create_place} (agent): creates a named place with the full REST validation, as {@code createPlace}. */
@Component
public class CreatePlaceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("place", PlaceItems.PLACE_SCHEMA), "place");

    private final PlaceService places;
    private final PlaceItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreatePlaceTool(PlaceService places, PlaceItems items, McpOutputShaper shaper) {
        this.places = places;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "create_place";
    }

    @Override
    public String title() {
        return "Create a place";
    }

    @Override
    public String description() {
        return "Creates a named place that watches, views and briefings can refer to by id: a point with a radius"
                + " (shape point, radiusKm), a polygon, or a route with a buffer (bufferM). The place's area is"
                + " derived on save. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created place (compact)");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(shaper.object(items.item(places.create(args.get("place")), context, Detail.SHORT),
                Detail.SHORT));
    }
}
