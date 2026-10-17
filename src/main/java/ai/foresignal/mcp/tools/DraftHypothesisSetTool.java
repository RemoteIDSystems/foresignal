package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.hypothesis.HypothesisDrafter;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code draft_hypothesis_set} (agent, prompt 125): has the LLM draft a hypothesis set from plain language, as
 * {@code POST /v1/hypotheses/sets/draft} does. Nothing is created.
 */
@RestrictedRead
@Component
public class DraftHypothesisSetTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "text", string("What to watch, in plain language.", 1, 4000),
            "situationId", string("A situation it is about (ULID)."),
            "regionCodes", stringArray("ISO 3166 region codes.", 50)),
            "text");

    private final HypothesisDrafter drafter;
    private final JsonMapper json;

    /**
     * Creates the tool. The drafter is injected lazily: it reads through the tool catalog, which holds this tool.
     */
    public DraftHypothesisSetTool(@Lazy HypothesisDrafter drafter, JsonMapper json) {
        this.drafter = drafter;
        this.json = json;
    }

    @Override
    public String name() {
        return "draft_hypothesis_set";
    }

    @Override
    public String title() {
        return "Draft hypothesis set";
    }

    @Override
    public String description() {
        return "Has the LLM draft a hypothesis set from plain language (for example 'watch whether the Red Sea"
                + " situation spreads to Hormuz'): competing hypotheses with priors from base rates and analogs,"
                + " indicators as validated rule specs, key assumptions and a proposed resolver. The draft is returned"
                + " with warnings; nothing is created until create_hypothesis_set is called with it.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The draft");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    /** An LLM draft spends budget, so it always needs a token (invariant SEC-5, as the HTTP route does). */
    @Override
    public boolean costBearing() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(drafter.draft(HypothesisDrafter.DraftRequest.parse(args))));
    }
}
