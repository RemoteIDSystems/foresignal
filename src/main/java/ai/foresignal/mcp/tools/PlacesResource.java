package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.ForesignalMcpResource;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ArrayNode;

/**
 * The resource {@code foresignal://places} (prompt 036, always authenticated): names, shapes and
 * radii; coordinates only for agent and admin tokens (the {@code detail=long} form).
 */
@Component
public class PlacesResource implements ForesignalMcpResource {

    private final PlaceService places;
    private final PlaceItems items;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the resource. */
    public PlacesResource(PlaceService places, PlaceItems items, tools.jackson.databind.json.JsonMapper json) {
        this.places = places;
        this.items = items;
        this.json = json;
    }

    @Override
    public String uri() {
        return "foresignal://places";
    }

    @Override
    public String name() {
        return "places";
    }

    @Override
    public String description() {
        return "Foresignal's named places with id, name, shape, radius or buffer, kind and tags (coordinates only for"
                + " agent tokens).";
    }

    @Override
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public String read(ToolContext context) {
        Scope scope = context.caller().scope();
        Detail detail = scope != null && scope.includes(Scope.AGENT) ? Detail.LONG : Detail.SHORT;
        ArrayNode out = json.createArrayNode();
        places.all().forEach(p -> out.add(items.item(p, context, detail)));
        return out.toString();
    }
}
