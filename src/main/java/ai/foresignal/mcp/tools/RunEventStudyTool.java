package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.eventstudy.EventStudyService;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code run_event_study} (agent, prompt 119): submits a batch event study as a 103 job ({@code type=event-study});
 * read the result with {@code get_backtest}. No LLM is used.
 */
@RestrictedRead
@Component
public class RunEventStudyTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "name", string("An optional label."),
            "selector", selector(),
            "target", string("The target series reference (series:, rollup: or rule:)."),
            "from", dateTime("Start of the event range."),
            "to", dateTime("End of the event range."),
            "asOf", dateTime("Knowledge cutoff of the replay (default now).")), "selector", "target", "from", "to");

    private final EventStudyService studies;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunEventStudyTool(EventStudyService studies, JsonMapper json) {
        this.studies = studies;
        this.json = json;
    }

    /** The schema of an {@code EventSelector}. */
    static Map<String, Object> selector() {
        Map<String, Object> out = new LinkedHashMap<>(JsonSchemas.openObject("Event selector: exactly one of"
                + " alertRuleId, alertRuleTag, intelEventType, anomalyRuleId (+ classification), situationActivated"
                + " {theme}, changePoint {family}, unexplainedMove {family} or timestamps; optional regionCodes and"
                + " minSeverity.", props(
                        "alertRuleId", string("Alerts of this rule."),
                        "alertRuleTag", string("Alerts of rules with this tag."),
                        "intelEventType", string("Intelligence event type."),
                        "anomalyRuleId", string("Anomaly rule id."),
                        "classification", string("Anomaly classification."),
                        "timestamps", JsonSchemas.stringArray("Explicit event times.", 5000),
                        "regionCodes", JsonSchemas.stringArray("040 region codes.", 50),
                        "minSeverity", string("Minimum severity."))));
        return out;
    }

    @Override
    public String name() {
        return "run_event_study";
    }

    @Override
    public String title() {
        return "Run event study";
    }

    @Override
    public String description() {
        return "Runs a batch event study: did the target series move unusually around events of a selector? Returns"
                + " the 103 job; its result has per-event abnormal paths, CAAR per window, standardized, rank and"
                + " placebo tests, and caveats. One queued or running job per token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The submitted job");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String tokenId = context.caller().currentToken().map(t -> t.id().toString()).orElse(null);
        BacktestJobEntity job = studies.submit(args, tokenId);
        return ToolResult.json(json.valueToTree(BacktestViews.job(job)));
    }
}
