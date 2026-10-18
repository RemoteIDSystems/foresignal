package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.correlation.CorrelationViews;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code compare_claim} (read; prompt 137): the social–physical comparison of a claim, or every comparison of a
 * narrative, with physical evidence, detectability, lead times and revisions.
 */
@Component
@RestrictedRead("comparisons are derived intelligence objects; their physical evidence refs are resolved separately")
public class CompareClaimTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "claimId", JsonSchemas.string("Claim id (from get_claims)"),
            "narrativeId", JsonSchemas.string("Narrative id: every comparison of its claims")));

    private final CorrelationViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public CompareClaimTool(CorrelationViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "compare_claim";
    }

    @Override
    public String title() {
        return "Compare a social claim with physical evidence";
    }

    @Override
    public String description() {
        return "Returns how a social claim (or every claim of a narrative) compares with independent physical evidence"
                + " in the same place and window: corroborated (with observed lead times), unobserved (no corresponding"
                + " change although coverage could have seen it, never 'false'), contradicted, insufficient-coverage or"
                + " pending, with the matching evidence, detectability and every revision. Give claimId or narrativeId.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Comparison or comparisons");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String claimId = ListNewsStoriesTool.text(args, "claimId");
        String narrativeId = ListNewsStoriesTool.text(args, "narrativeId");
        if ((claimId == null) == (narrativeId == null)) {
            return ToolResult.error("give exactly one of claimId or narrativeId");
        }
        try {
            Map<String, Object> out = new LinkedHashMap<>();
            if (claimId != null) {
                Map<String, Object> c = views.byClaim(claimId);
                out.put("items", c == null ? List.of() : List.of(c));
            } else {
                out.put("items", views.byNarrative(narrativeId));
            }
            out.put("nextCursor", null);
            return ToolResult.json(json.valueToTree(out));
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
