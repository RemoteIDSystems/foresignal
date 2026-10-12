package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
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
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_places} (read, always authenticated): the named places, without coordinates for read tokens. */
@Component
public class ListPlacesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "kind", enumeration("Only places of this kind.", null, "user", "airport", "asset"),
            "tag", string("Only places with this tag.", 1, 64),
            "detail", detail()));

    private final PlaceService places;
    private final PlaceItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListPlacesTool(PlaceService places, PlaceItems items, McpOutputShaper shaper) {
        this.places = places;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_places";
    }

    @Override
    public String title() {
        return "List places";
    }

    @Override
    public String description() {
        return "Lists the named places (home, airports, assets): id, name, shape (point, polygon or route), radius or"
                + " buffer, kind, tags. Use a place id in records_near_place, in watches (near: {placeIds}) and in"
                + " views. Coordinates are returned only with detail=long to an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Places");
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
        String kind = McpArgs.text(args, "kind");
        String tag = McpArgs.text(args, "tag");
        List<PlaceView> all = places.all().stream()
                .filter(p -> kind == null || kind.equals(p.kind()))
                .filter(p -> tag == null || p.tags().contains(tag))
                .toList();
        List<ObjectNode> out = all.stream().map(p -> items.item(p, context, detail)).toList();
        return ToolResult.json(shaper.list(out, out.size(), false, detail, null));
    }
}
