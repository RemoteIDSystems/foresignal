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
 * {@code get_information_value} (read, prompt 125): a set's pending indicators ranked by expected information gain,
 * each with its detection probability and whether it is a collection gap.
 */
@RestrictedRead("information value holds statistics and refs, no record content; private sets need private access")
@Component
public class GetInformationValueTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("setId", string("The hypothesis set id (ULID).")), "setId");

    private final HypothesisAnalysis analysis;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetInformationValueTool(HypothesisAnalysis analysis, IntelVisibility visibility, JsonMapper json) {
        this.analysis = analysis;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_information_value";
    }

    @Override
    public String title() {
        return "Get information value";
    }

    @Override
    public String description() {
        return "Ranks a hypothesis set's pending indicators by expected information gain (what would most change the"
                + " judgment if it appeared or failed to appear), with each one's detection probability. Indicators"
                + " that would matter but cannot currently be observed are flagged as collection gaps.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The information value");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(analysis.informationValue(McpArgs.requiredText(args, "setId"),
                visibility.seesPrivate(context.caller()))));
    }
}
