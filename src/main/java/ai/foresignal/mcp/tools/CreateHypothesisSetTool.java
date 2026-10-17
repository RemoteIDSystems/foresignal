package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.forecast.JsonValues;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_hypothesis_set} (agent, prompt 125): creates a hypothesis set with its hypotheses, indicators (and their
 * watch rules), key assumptions and ledger question, as {@code POST /v1/hypotheses/sets} does (origin {@code agent}).
 */
@RestrictedRead
@Component
public class CreateHypothesisSetTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = openObject("A HypothesisSetInput.", props(
            "question", string("The key question.", 1, 300),
            "description", string("What the set is about."),
            "kind", enumeration("exclusive (priors sum to 1 with exactly one 'other') or independent.", null,
                    HypothesisVocabulary.KINDS.toArray(String[]::new)),
            "cadence", enumeration("Review cadence.", null, HypothesisVocabulary.CADENCES.toArray(String[]::new)),
            "horizon", string("The date the question resolves by (YYYY-MM-DD)."),
            "situationId", string("A linked situation (ULID)."),
            "questionId", string("An existing ledger question (ULID)."),
            "regionCodes", stringArray("ISO 3166 region codes.", 50),
            "themes", stringArray("Themes.", 32),
            "private", bool("Private set.", false),
            "hypotheses", Map.of("type", "array", "minItems", 2, "items", Map.of("type", "object"),
                    "description", "HypothesisInput items: label, statement, type, prior, priorBasis {type, ref,"
                            + " rationale}, ledgerOutcome; speculative links also linkedFindingIds,"
                            + " coincidenceBaseline {placeboRate, method} and alternatives (labels)."),
            "indicators", Map.of("type", "array", "items", Map.of("type", "object"),
                    "description", "IndicatorInput items: description, signal, signalFamily, credibility, relevance,"
                            + " expectedBy, lr {hypothesis label: {class, direction, value?, rationale?}}, absenceLr."),
            "assumptions", Map.of("type", "array", "items", Map.of("type", "object"),
                    "description", "KeyAssumptionInput items: text, linchpin, rationale, wouldBeWrongIf, status."),
            "ledgerQuestion", openObject("Ledger question to create (needs horizon): resolver {type, params},"
                    + " resolutionCriteria, category.", props())),
            "question", "kind", "cadence", "hypotheses");

    private final HypothesisService service;
    private final JsonMapper json;

    /** Creates the tool. */
    public CreateHypothesisSetTool(HypothesisService service, JsonMapper json) {
        this.service = service;
        this.json = json;
    }

    @Override
    public String name() {
        return "create_hypothesis_set";
    }

    @Override
    public String title() {
        return "Create hypothesis set";
    }

    @Override
    public String description() {
        return "Creates a rolling hypothesis set: a key question, competing hypotheses (an exclusive set has exactly"
                + " one 'other' and priors summing to 1, each prior with its outside-view basis), indicators that watch"
                + " for themselves, key assumptions and an optional ledger question. Probabilities then move only"
                + " through revisions. Call get_base_rate and find_analogs first for the priors.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The hypothesis set");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        HypothesisSetEntity set = service.create(HypothesisInputs.set(JsonValues.map(args)), "agent",
                context.actor());
        return ToolResult.json(json.valueToTree(service.view(set, true)));
    }
}
