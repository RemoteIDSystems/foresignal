package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.nonNegative;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_infra_node} (read, prompt 104): one facility with its in and out edges. */
@RestrictedRead
@Component
public class GetInfraNodeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", nonNegative("The node id.")), "id");

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetInfraNodeTool(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_infra_node";
    }

    @Override
    public String title() {
        return "Infrastructure node";
    }

    @Override
    public String description() {
        return "One infrastructure facility with its incoming and outgoing dependency edges. Derived from open"
                + " data; not authoritative; heuristic links may be wrong.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("One infrastructure node");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        long id = McpArgs.requiredLong(args, "id");
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, facility_type AS \"facilityType\", name, source_id AS \"sourceId\","
                        + " external_id AS \"externalId\", licence, ST_AsText(point) AS point"
                        + " FROM infra_node WHERE id = ?",
                id);
        if (rows.isEmpty()) {
            throw new McpToolException(ErrorCodes.UNKNOWN_INFRA_NODE + ": no such node " + id);
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>(rows.get(0));
        out.put("outEdges", jdbc.queryForList(
                "SELECT id, to_node AS \"toNode\", relation, method, label, score FROM infra_edge WHERE from_node = ?",
                id));
        out.put("inEdges", jdbc.queryForList(
                "SELECT id, from_node AS \"fromNode\", relation, method, label, score FROM infra_edge WHERE to_node = ?",
                id));
        return ToolResult.json(json.valueToTree(out));
    }
}
