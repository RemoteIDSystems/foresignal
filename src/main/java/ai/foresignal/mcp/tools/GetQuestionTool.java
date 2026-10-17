package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.forecast.ForecastService;
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
 * {@code get_question} (read, prompt 122): one question with its verbatim criteria and resolver, the latest forecast
 * of each forecaster, the ensemble and its ICD term, and the scores once resolved.
 */
@RestrictedRead("ledger questions hold numbers and refs, no record content; private questions need private access")
@Component
public class GetQuestionTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("questionId", string("The question id (ULID).")),
            "questionId");

    private final ForecastService ledger;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetQuestionTool(ForecastService ledger, IntelVisibility visibility, JsonMapper json) {
        this.ledger = ledger;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_question";
    }

    @Override
    public String title() {
        return "Get forecast question";
    }

    @Override
    public String description() {
        return "Returns a forecast question: its resolution criteria (quote them verbatim, misreading them is the"
                + " commonest forecasting error), the machine resolver, the latest forecast per forecaster, the"
                + " ensemble with its ICD 203 term, and the resolution and scores once resolved.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The question");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "questionId");
        return ToolResult.json(json.valueToTree(ledger.view(ledger.get(id, visibility.seesPrivate(context.caller())))));
    }
}
