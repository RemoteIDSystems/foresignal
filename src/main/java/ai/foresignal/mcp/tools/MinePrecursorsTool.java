package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.precursor.PrecursorService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code mine_precursors} (agent, prompt 120): starts a precursor mining run (or, with {@code mode=retest}, the
 * library re-test) now as a 103 job ({@code type=precursor-mining}); read the result with {@code get_backtest}. No LLM
 * is used.
 */
@RestrictedRead
@Component
public class MinePrecursorsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "mode", enumeration("mine (generate, test, FDR-adjust, validate) or retest (the library re-test).", "mine",
                    "mine", "retest")));

    private final PrecursorService precursors;
    private final JsonMapper json;

    /** Creates the tool. */
    public MinePrecursorsTool(PrecursorService precursors, JsonMapper json) {
        this.precursors = precursors;
        this.json = json;
    }

    @Override
    public String name() {
        return "mine_precursors";
    }

    @Override
    public String title() {
        return "Mine precursors";
    }

    @Override
    public String description() {
        return "Starts precursor mining now: candidate pairs are generated, tested on a discovery window, adjusted for"
                + " multiple testing and confirmed on later data. Returns the 103 job; only one run at a time"
                + " (PRECURSOR_MINING_IN_PROGRESS).";
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
        String mode = "retest".equals(McpArgs.text(args, "mode")) ? "retest" : "mine";
        String tokenId = context.caller().currentToken().map(t -> t.id().toString()).orElse(null);
        BacktestJobEntity job = precursors.submit(mode, "manual", tokenId);
        return ToolResult.json(json.valueToTree(BacktestViews.job(job)));
    }
}
