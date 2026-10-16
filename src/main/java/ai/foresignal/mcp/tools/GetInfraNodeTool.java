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
import java.util.Map;
import ai.foresignal.infra.InfraNodeQuery;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_infra_node} (read, prompt 104): one facility with its in and out edges. */
@RestrictedRead
@Component
public class GetInfraNodeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("id", nonNegative("The node id.")), "id");

    private final InfraNodeQuery query;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetInfraNodeTool(InfraNodeQuery query, JsonMapper json) {
        this.query = query;
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
        Map<String, Object> out;
        try {
            // Only the current build is served, so a node of a superseded build is unknown.
            out = query.node(id);
        } catch (ai.foresignal.api.error.NotFoundException e) {
            throw new McpToolException(ErrorCodes.UNKNOWN_INFRA_NODE + ": no such node " + id);
        }
        return ToolResult.json(json.valueToTree(out));
    }
}
