package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_hypothesis_set} (read, prompt 125): one set with its hypotheses (probability, term, confidence and change
 * since the previous revision), indicators, key assumptions and the linked ledger question with its ensemble.
 */
@RestrictedRead("hypothesis sets hold judgments, numbers and refs, no record content; private sets need private"
        + " access")
@Component
public class GetHypothesisSetTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("setId", string("The hypothesis set id (ULID).")), "setId");

    private final HypothesisService service;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetHypothesisSetTool(HypothesisService service, IntelVisibility visibility, JsonMapper json) {
        this.service = service;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_hypothesis_set";
    }

    @Override
    public String title() {
        return "Get hypothesis set";
    }

    @Override
    public String description() {
        return "Returns a hypothesis set: its key question, each hypothesis with its probability, ICD 203 term,"
                + " analytic confidence and change since the previous revision, the indicators with their likelihood"
                + " ratios and states, the key assumptions, and the linked ledger question. A speculative link is"
                + " never presented without its alternatives and coincidence baseline.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The hypothesis set");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "setId");
        return ToolResult.json(json.valueToTree(service.view(service.get(id,
                visibility.seesPrivate(context.caller())), true)));
    }
}
