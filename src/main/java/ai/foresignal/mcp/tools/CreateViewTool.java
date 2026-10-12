package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.view.SavedViewMapper;
import ai.foresignal.view.SavedViewService;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code create_view} (agent): creates a saved view with the full REST validation, as {@code createView}. */
@Component
public class CreateViewTool implements ForesignalMcpTool {

    /** The schema of a {@code SavedViewInput}. */
    public static final Map<String, Object> VIEW_SCHEMA = JsonSchemas.openObject(
            "A saved view, as SavedViewInput in /v1/openapi.json: slug (lower-case), name, sources, kinds, placeIds,"
                    + " bbox [minLon,minLat,maxLon,maxLat], area (GeoJSON polygon), conditions (as in watches, at most 20;"
                    + " {op: inWatchlist, value: <watchlist id>} without a path matches each source's identity field),"
                    + " timeField, includeExpired, fields (projection such as payload.mag).",
            Map.of(), "slug", "name");

    static final Map<String, Object> INPUT = object(props("view", VIEW_SCHEMA), "view");

    private final SavedViewService views;
    private final SavedViewMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreateViewTool(SavedViewService views, SavedViewMapper mapper, McpOutputShaper shaper) {
        this.views = views;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "create_view";
    }

    @Override
    public String title() {
        return "Create a saved view";
    }

    @Override
    public String description() {
        return "Creates a saved view: a named record filter that records, map tiles and streams render the same way,"
                + " for example every aircraft whose icao24 is on a watchlist inside the home place. Needs an agent"
                + " token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created view");
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
        return ToolResult.json(shaper.object(shaper.objectTree(mapper.view(views.create(args.get("view")))),
                Detail.SHORT));
    }
}
