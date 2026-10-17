package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisViews;
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
 * {@code review_hypothesis_set} (agent, prompt 125): records a review revision with a rationale, as
 * {@code POST /v1/hypotheses/sets/{setId}/review} does.
 */
@RestrictedRead
@Component
public class ReviewHypothesisSetTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "setId", string("The hypothesis set id (ULID)."),
            "cause", enumeration("review (default) or operator.", "review", "review", "operator"),
            "rationale", string("What changed the judgment since the previous revision (ICD 203 standard 7).", 1,
                    8000),
            "probabilities", openObject("Direct probabilities per hypothesis id (logged).", props()),
            "lr", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                    "LR changes: {indicatorId, hypothesisId, likelihood {class, direction, value?, rationale?}}."),
            "assumptions", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                    "Assumption status changes: {assumptionId, status, rationale?}."),
            "exceedCap", bool("Allow a speculative link above its cap (logged and labeled).", false),
            "citations", stringArray("Object references.", 100),
            "version", Map.of("type", "integer", "description", "The set version (optimistic lock).")),
            "setId", "rationale");

    private final HypothesisService service;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ReviewHypothesisSetTool(HypothesisService service, IntelVisibility visibility, JsonMapper json) {
        this.service = service;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "review_hypothesis_set";
    }

    @Override
    public String title() {
        return "Review hypothesis set";
    }

    @Override
    public String description() {
        return "Records a review of a hypothesis set as a revision with your rationale: optionally direct probability"
                + " changes, LR changes and assumption status changes. The rationale must pass the ICD 203 lint (no"
                + " likelihood term and confidence level in one sentence, terms that match their numbers).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The revision");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String setId = McpArgs.requiredText(args, "setId");
        ObjectNode body = ((ObjectNode) args).deepCopy();
        body.remove("setId");
        return ToolResult.json(json.valueToTree(HypothesisViews.revision(service.review(setId,
                HypothesisInputs.review(body), context.actor(), visibility.seesPrivate(context.caller())))));
    }
}
