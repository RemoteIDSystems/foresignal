package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code delete_place} (agent): deletes a place, as {@code deletePlace}. */
@Component
public class DeletePlaceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "placeId", string("The place id (from list_places).", 1, 64),
            "force", bool("Delete even when watches, views or briefings use it; the watches are disabled.", false)),
            "placeId");

    private final PlaceService places;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public DeletePlaceTool(PlaceService places, McpOutputShaper shaper) {
        this.places = places;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "delete_place";
    }

    @Override
    public String title() {
        return "Delete a place";
    }

    @Override
    public String description() {
        return "Deletes a named place. When watches, views or briefings use it the call fails with PLACE_IN_USE and"
                + " lists them, unless force is true, which disables those watches. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("{deleted: true, placeId}");
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
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "placeId");
        places.delete(id, McpArgs.bool(args, "force", false));
        ObjectNode out = shaper.object();
        out.put("deleted", true);
        out.put("placeId", id);
        return ToolResult.json(out);
    }
}
