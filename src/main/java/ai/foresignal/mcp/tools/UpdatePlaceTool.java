package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code update_place} (agent): replaces a place whole, as {@code updatePlace}; referencing watches follow. */
@Component
public class UpdatePlaceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "placeId", string("The place id (from list_places).", 1, 64),
            "place", PlaceItems.PLACE_SCHEMA), "placeId", "place");

    private final PlaceService places;
    private final PlaceItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public UpdatePlaceTool(PlaceService places, PlaceItems items, McpOutputShaper shaper) {
        this.places = places;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "update_place";
    }

    @Override
    public String title() {
        return "Update a place";
    }

    @Override
    public String description() {
        return "Replaces a named place whole (send every field). Watches near the place are re-materialized at once;"
                + " a stationary record never fires because of the edit. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The updated place (compact)");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean destructiveHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(shaper.object(items.item(places.update(McpArgs.requiredText(args, "placeId"),
                args.get("place")), context, Detail.SHORT), Detail.SHORT));
    }
}
