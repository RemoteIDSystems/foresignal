package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.entity.EntityService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * MCP {@code get_entity} (prompt 098): one canonical entity by id or key, with its open records
 * capped at 20.
 */
@Component
public class GetEntityTool implements ForesignalMcpTool {

    private static final int OPEN_RECORDS_CAP = 20;

    static final Map<String, Object> INPUT = object(props(
            "id", string("The entity id (from find_entity).", 1, 64),
            "key", string("Or an exact <keyType>:<value>, for example icao24:ae1234.", 3, 128),
            "detail", detail()));

    private final EntityService entities;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetEntityTool(EntityService entities, McpOutputShaper shaper) {
        this.entities = entities;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_entity";
    }

    @Override
    public String title() {
        return "Get an entity";
    }

    @Override
    public String description() {
        return "Returns one canonical entity (aircraft, vessel, satellite or station) with its deterministic keys,"
                + " aliases and open records per source (capped at " + OPEN_RECORDS_CAP + "). Give id or key.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The entity, its keys and its open records");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String id = McpArgs.text(args, "id");
        String key = McpArgs.text(args, "key");
        if (id == null && key == null) {
            throw new McpToolException("Give either id or key.");
        }
        Map<String, Object> entity = id != null
                ? entities.get(id, context.excludedSources())
                : entities.resolve(key, null, context.excludedSources());
        if (entity == null) {
            throw new McpToolException("No entity found. Check the key with find_entity.");
        }
        String entityId = (String) entity.get("id");
        Map<String, Object> records = entities.records(entityId, null, false, context.excludedSources(),
                context.restriction());
        List<?> items = records == null ? List.of() : (List<?>) records.get("items");
        List<JsonNode> capped = new ArrayList<>();
        for (int i = 0; i < Math.min(items.size(), OPEN_RECORDS_CAP); i++) {
            capped.add(shaper.objectTree(items.get(i)));
        }
        Map<String, Object> out = new LinkedHashMap<>(entity);
        out.put("records", capped);
        out.put("recordsTotal", items.size());
        return ToolResult.json(shaper.object(shaper.objectTree(out), detail));
    }
}
