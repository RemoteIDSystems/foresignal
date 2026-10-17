package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.precursor.PrecursorService;
import ai.foresignal.precursor.PrecursorViews;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_precursor} (read, prompt 120): one precursor with its windows, lifecycle and history. */
@RestrictedRead
@Component
public class GetPrecursorTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("precursorId", string("The precursor id.")), "precursorId");

    private final PrecursorService precursors;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetPrecursorTool(PrecursorService precursors, JsonMapper json) {
        this.precursors = precursors;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_precursor";
    }

    @Override
    public String title() {
        return "Get precursor";
    }

    @Override
    public String description() {
        return "Returns one precursor: its antecedent and consequent, lag bounds, the discovery and validation windows"
                + " (raw and adjusted p, verdict, reverse test), the batch size of its false-discovery family, status"
                + " and history. An association in stored data, not causation.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The precursor");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(PrecursorViews.precursor(precursors.get(
                McpArgs.requiredText(args, "precursorId")))));
    }
}
