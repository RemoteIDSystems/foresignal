package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_question} (agent, prompt 122): opens a resolvable forecast question with verbatim criteria and a
 * machine resolver, as {@code POST /v1/forecasts/questions} does (origin {@code agent}); an existing {@code dedupKey}
 * returns the existing question.
 */
@RestrictedRead
@Component
public class CreateQuestionTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "title", string("The question, ending in a question mark.", 1, 300),
            "type", enumeration("The question type.", Ledger.BINARY, Ledger.TYPES.toArray(String[]::new)),
            "outcomes", openObject("Labels for multiple/ordered questions; unit, lower, upper for numeric ones.",
                    props()),
            "resolutionCriteria", string("Exactly what counts as yes (shown verbatim to every forecaster).", 1,
                    2000),
            "resolver", openObject("The machine resolver: event-exists {selector, area?, window}, count-threshold"
                    + " {selector|ref, area?, window, op, threshold}, series-value {ref, period|window, aggregate,"
                    + " op?, threshold?}, alert-fired {ruleId, window, area?}, situation-status {situationId, status?,"
                    + " minSeverity?}, hypothesis-outcome {setId} or manual.", props("type", enumeration("The"
                    + " resolver type.", "event-exists", Ledger.RESOLVER_TYPES.toArray(String[]::new))), "type"),
            "opensAt", JsonSchemas.dateTime("When it opens (default: now)."),
            "closesAt", JsonSchemas.dateTime("When forecasts stop."),
            "resolveBy", JsonSchemas.dateTime("When it resolves."),
            "category", string("conflict, maritime, markets, weather-impact, activity, …", 1, 64),
            "tags", JsonSchemas.stringArray("Tags.", 32),
            "links", openObject("Objects it is about: situationId, hypothesisSetId, precursorId, findingId,"
                    + " seriesRef, outlookId.", props()),
            "dedupKey", string("Returns the existing question with this key instead of opening a second."),
            "private", JsonSchemas.bool("Private question.", false)),
            "title", "type", "resolutionCriteria", "resolver", "closesAt", "resolveBy", "category");

    private final ForecastService ledger;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the tool. */
    public CreateQuestionTool(ForecastService ledger, JsonMapper json, Clock clock) {
        this.ledger = ledger;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "create_question";
    }

    @Override
    public String title() {
        return "Create forecast question";
    }

    @Override
    public String description() {
        return "Opens a resolvable forecast question with verbatim resolution criteria and a machine resolver (or"
                + " manual). Baseline forecasters forecast it automatically. Prefer machine resolvers over manual, and"
                + " a referenceFamily selector or region codes so base rates and detectability apply.";
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
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ForecastInputs.QuestionDraft draft = ForecastInputs.question(args, clock.instant());
        ForecastService.Opened opened = ledger.openQuestion(draft, "agent", context.actor());
        Map<String, Object> out = new LinkedHashMap<>(ledger.view(opened.question()));
        out.put("created", opened.created());
        return ToolResult.json(json.valueToTree(out));
    }
}
