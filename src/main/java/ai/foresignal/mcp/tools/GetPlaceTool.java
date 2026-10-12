package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
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

/** {@code get_place} (read, always authenticated): one place, as {@code getPlace}. */
@Component
public class GetPlaceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "placeId", string("The place id (from list_places).", 1, 64),
            "detail", detail()), "placeId");

    private final PlaceService places;
    private final PlaceItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetPlaceTool(PlaceService places, PlaceItems items, McpOutputShaper shaper) {
        this.places = places;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_place";
    }

    @Override
    public String title() {
        return "Get a place";
    }

    @Override
    public String description() {
        return "Returns one named place: name, shape, radius or buffer, kind, tags, timezone and version. Coordinates"
                + " are returned only with detail=long to an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The place");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        return ToolResult.json(shaper.object(items.item(places.get(McpArgs.requiredText(args, "placeId")), context,
                detail), detail));
    }
}
