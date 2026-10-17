package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.hypothesis.HypothesisAnalysis;
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
 * {@code get_ach_matrix} (read, prompt 125): the ACH matrix of a set, an audit view of evidence against hypotheses
 * with weighted inconsistency scores.
 */
@RestrictedRead("the ACH matrix holds ratings, weights and refs, no record content; private sets need private access")
@Component
public class GetAchMatrixTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("setId", string("The hypothesis set id (ULID).")), "setId");

    private final HypothesisAnalysis analysis;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetAchMatrixTool(HypothesisAnalysis analysis, IntelVisibility visibility, JsonMapper json) {
        this.analysis = analysis;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_ach_matrix";
    }

    @Override
    public String title() {
        return "Get ACH matrix";
    }

    @Override
    public String description() {
        return "Returns the analysis-of-competing-hypotheses matrix of a set: evidence rows rated CC, C, N, NA, I or II"
                + " per hypothesis with their weights, the inconsistency score per hypothesis, the least inconsistent"
                + " hypothesis and the non-diagnostic rows. An audit view: probabilities come from the updater.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The ACH matrix");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(analysis.matrix(McpArgs.requiredText(args, "setId"),
                visibility.seesPrivate(context.caller()))));
    }
}
