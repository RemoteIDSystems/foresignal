package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.notify.escalation.EscalationPolicyService;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_escalation_policies} (read, prompt 033): every escalation policy with its steps,
 * repeat, stop conditions and how many watches use it.
 */
@Component
public class ListEscalationPoliciesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props());

    private final EscalationPolicyService policies;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListEscalationPoliciesTool(EscalationPolicyService policies, JsonMapper json) {
        this.policies = policies;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_escalation_policies";
    }

    @Override
    public String title() {
        return "List escalation policies";
    }

    @Override
    public String description() {
        return "Lists the escalation policies: which channels an unacknowledged alert reaches after how many minutes,"
                + " the optional repeat, what stops it (acknowledgement always; resolve and snooze per policy) and"
                + " how many watches use each.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Escalation policies");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        java.util.List<Map<String, Object>> list = policies.list();
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("items", list);
        out.put("total", list.size());
        out.put("truncated", false);
        return ToolResult.json(json.valueToTree(out));
    }
}
