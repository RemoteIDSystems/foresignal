package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.outlook.index.IndexFitService;
import ai.foresignal.outlook.index.IndexViews;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code fit_index} (agent, prompt 126): starts a weight fit of a regional index as a 103 job; the fitted weights are
 * proposed, never applied unless the index sets {@code autoApplyFittedWeights}. No LLM is used.
 */
@RestrictedRead
@Component
public class FitIndexTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("indexId", string("Regional index id.")), "indexId");

    private final IndexFitService fits;
    private final JsonMapper json;

    /** Creates the tool. */
    public FitIndexTool(IndexFitService fits, JsonMapper json) {
        this.fits = fits;
        this.json = json;
    }

    @Override
    public String name() {
        return "fit_index";
    }

    @Override
    public String title() {
        return "Fit regional index weights";
    }

    @Override
    public String description() {
        return "Fits a regional index's component weights to outcomes (regularized NB regression with rolling-origin"
                + " validation) and reports the out-of-sample skill against the seasonal-naive outlook and the proposed"
                + " weights. Returns the queued fit; read it with get_index. Weights are only proposed: apply them with"
                + " POST /v1/indices/{id}/weights. One queued or running job per token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued fit");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String tokenId = context.caller().currentToken().map(t -> t.id().toString()).orElse(null);
        return ToolResult.json(json.valueToTree(IndexViews.fit(fits.submit(McpArgs.requiredText(args, "indexId"),
                tokenId))));
    }
}
