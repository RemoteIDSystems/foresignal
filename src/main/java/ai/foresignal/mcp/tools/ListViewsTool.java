package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewMapper;
import ai.foresignal.view.SavedViewService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_views} (read): the saved views' definitions, as {@code listViews}. */
@Component
public class ListViewsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    static final List<String> SHORT_MEMBERS = List.of("id", "slug", "name", "sources", "kinds", "placeIds", "version");

    private final SavedViewService views;
    private final SavedViewMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListViewsTool(SavedViewService views, SavedViewMapper mapper, McpOutputShaper shaper) {
        this.views = views;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_views";
    }

    @Override
    public String title() {
        return "List saved views";
    }

    @Override
    public String description() {
        return "Lists the saved views: named record filters (sources, kinds, places, conditions) that get_view_records"
                + " renders. detail=long includes conditions, bbox, area and the field projection.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Saved views");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<ObjectNode> out = views.all().stream().map(v -> item(v, detail)).toList();
        return ToolResult.json(shaper.list(out, out.size(), false, detail, null));
    }

    private ObjectNode item(SavedViewEntity view, Detail detail) {
        ObjectNode full = shaper.objectTree(mapper.view(view));
        if (detail == Detail.LONG) {
            return full;
        }
        ObjectNode out = shaper.object();
        SHORT_MEMBERS.forEach(m -> {
            if (full.has(m)) {
                out.set(m, full.get(m));
            }
        });
        out.put("conditions", full.path("conditions").size());
        return out;
    }
}
