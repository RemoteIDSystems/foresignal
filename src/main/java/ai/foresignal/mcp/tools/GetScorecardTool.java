package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_scorecard} (read, prompt 122): Brier, log score, RPS, CRPS, time-averaged Brier, the Murphy
 * decomposition, skill against the out-of-sample base rate and the bias monitor per forecaster.
 */
@RestrictedRead("scorecards are statistics, no record content")
@Component
public class GetScorecardTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "forecaster", string("One forecaster id (default: all), e.g. ensemble, base-rate, operator:jsmith."),
            "category", string("One category (default: all)."),
            "from", dateTime("Questions resolved at or after this time."),
            "to", dateTime("Questions resolved before this time.")));

    private final ScoringService scoring;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetScorecardTool(ScoringService scoring, JsonMapper json) {
        this.scoring = scoring;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_scorecard";
    }

    @Override
    public String title() {
        return "Get forecast scorecard";
    }

    @Override
    public String description() {
        return "Returns the forecast scorecard: per forecaster the resolved count, mean Brier, log score, RPS, CRPS,"
                + " time-averaged Brier, Murphy decomposition, skill against the out-of-sample base rate (with a paired"
                + " bootstrap interval) and the bias monitor (mean forecast minus observed frequency, share of hedged"
                + " forecasts). Under 20 resolved questions the label is insufficient: say so when quoting it.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The scorecard");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String from = McpArgs.text(args, "from");
        String to = McpArgs.text(args, "to");
        return ToolResult.json(json.valueToTree(scoring.scorecard(blank(McpArgs.text(args, "forecaster")),
                blank(McpArgs.text(args, "category")), from == null ? null : Instant.parse(from),
                to == null ? null : Instant.parse(to))));
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
