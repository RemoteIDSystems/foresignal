package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.entity.EntityService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** MCP {@code find_entity} (prompt 098): prefix search over aliases and display names, or a key. */
@Component
public class FindEntityTool implements ForesignalMcpTool {

    private static final int MAX_LIMIT = 25;

    static final Map<String, Object> INPUT = object(props(
            "q", string("A prefix (at least 2 characters) of an alias or display name.", 2, 200),
            "type", enumeration("Entity type.", null, "aircraft", "vessel", "satellite", "station"),
            "key", string("Or an exact <keyType>:<value>.", 3, 128),
            "limit", integer("Page size.", 1, MAX_LIMIT, null)));

    private final EntityService entities;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public FindEntityTool(EntityService entities, McpOutputShaper shaper) {
        this.entities = entities;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "find_entity";
    }

    @Override
    public String title() {
        return "Find an entity";
    }

    @Override
    public String description() {
        return "Finds canonical entities by alias or display-name prefix, or by an exact key. Deterministic keys"
                + " only; no fuzzy matching.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Matching entities");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String q = McpArgs.text(args, "q");
        String type = McpArgs.text(args, "type");
        String key = McpArgs.text(args, "key");
        int limit = McpArgs.integer(args, "limit", MAX_LIMIT);
        EntityService.EntityPageResult page = entities.page(
                q, type, key, null, limit, null, context.excludedSources());
        List<JsonNode> items = new ArrayList<>();
        for (Map<String, Object> entity : page.items()) {
            items.add(shaper.objectTree(compact(entity)));
        }
        return ToolResult.json(shaper.list(items, items.size(), page.nextCursor() != null, detail,
                Map.of("entitiesTotal", items.size())));
    }

    private static Map<String, Object> compact(Map<String, Object> entity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.get("id"));
        out.put("type", entity.get("type"));
        out.put("displayName", entity.get("displayName"));
        out.put("provisional", entity.get("provisional"));
        out.put("status", entity.get("status"));
        return out;
    }
}
