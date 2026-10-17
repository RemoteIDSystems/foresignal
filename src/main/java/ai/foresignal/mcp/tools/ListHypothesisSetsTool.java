package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisViews;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_hypothesis_sets} (read, prompt 125): hypothesis sets with each hypothesis's probability, ICD 203 term
 * and confidence, as {@code GET /v1/hypotheses/sets} lists them.
 */
@RestrictedRead("hypothesis sets hold judgments, numbers and refs, no record content; private sets need private"
        + " access")
@Component
public class ListHypothesisSetsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", enumeration("Only sets with this status.", null,
                    HypothesisVocabulary.SET_STATUSES.toArray(String[]::new)),
            "cadence", enumeration("Only sets with this review cadence.", null,
                    HypothesisVocabulary.CADENCES.toArray(String[]::new)),
            "situationId", string("Only sets linked to this situation (ULID)."),
            "region", string("Only sets covering this ISO 3166 region code."),
            "limit", integer("How many, 1 to 200.", 1, 200, 50L)));

    private final HypothesisService service;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListHypothesisSetsTool(HypothesisService service, IntelVisibility visibility, JsonMapper json) {
        this.service = service;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_hypothesis_sets";
    }

    @Override
    public String title() {
        return "List hypothesis sets";
    }

    @Override
    public String description() {
        return "Lists rolling hypothesis sets (newest first): each key question with its competing hypotheses, their"
                + " probabilities, ICD 203 terms and analytic confidence, and the set's status and cadence. Read one"
                + " with get_hypothesis_set for indicators, assumptions and the linked ledger question.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of hypothesis sets");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        HypothesisService.Page<HypothesisSetEntity> page = service.list(McpArgs.text(args, "status"),
                McpArgs.text(args, "cadence"), McpArgs.text(args, "situationId"), McpArgs.text(args, "region"), null,
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))),
                visibility.seesPrivate(context.caller()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (HypothesisSetEntity set : page.items()) {
            items.add(service.view(set, false));
        }
        return ToolResult.json(json.valueToTree(HypothesisViews.page(items, page.nextCursor())));
    }
}
