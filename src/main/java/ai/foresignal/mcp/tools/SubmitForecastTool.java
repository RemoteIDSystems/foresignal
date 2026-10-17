package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastViews;
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
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code submit_forecast} (agent, prompt 122): records an immutable forecast on an open question under the caller's
 * {@code operator:{handle}} forecaster (or a named {@code operator} or {@code external} forecaster), as
 * {@code POST /v1/forecasts/questions/{id}/forecasts} does.
 */
@RestrictedRead
@Component
public class SubmitForecastTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "questionId", string("The question id (ULID)."),
            "probability", number("Binary questions: the probability of yes, 0 to 1.", 0, 1),
            "distribution", openObject("Multiple/ordered questions: outcome label → probability, summing to 1.",
                    props()),
            "quantiles", openObject("Numeric questions: p05, p10, p25, p50, p75, p90, p95 (p05, p25, p50, p75 and p95"
                    + " required), non-decreasing.", props()),
            "rationale", string("Why: start from the base rate, then the adjustments (Markdown)."),
            "forecasterId", string("Another registered operator or external forecaster (default: your own).")),
            "questionId");

    private final ForecastService ledger;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public SubmitForecastTool(ForecastService ledger, IntelVisibility visibility, JsonMapper json) {
        this.ledger = ledger;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "submit_forecast";
    }

    @Override
    public String title() {
        return "Submit forecast";
    }

    @Override
    public String description() {
        return "Records an immutable forecast on an open question; it supersedes your previous one and is scored when"
                + " the question resolves. Read the question first (get_question) and the base rate (get_base_rate);"
                + " give a probability with base-rate reasoning, and never a probability outside what the evidence"
                + " supports.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The recorded forecast");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        if (context.caller().token() == null) {
            throw new ValidationException("Submitting a forecast needs a token.");
        }
        String id = McpArgs.requiredText(args, "questionId");
        boolean seesPrivate = visibility.seesPrivate(context.caller());
        ForecastQuestionEntity q = ledger.get(id, seesPrivate);
        ObjectNode body = ((ObjectNode) args).deepCopy();
        body.remove("questionId");
        ForecastEntity f = ledger.submit(id, ForecastInputs.forecast(body, q), context.caller().token(), seesPrivate);
        return ToolResult.json(json.valueToTree(ForecastViews.forecast(f)));
    }
}
