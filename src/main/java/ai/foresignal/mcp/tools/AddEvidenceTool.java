package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
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
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code add_evidence} (agent, prompt 125): adds ACH evidence to a hypothesis set, as
 * {@code POST /v1/hypotheses/sets/{setId}/evidence} does; manual evidence whose ratings carry likelihood records an
 * {@code evidence-added} revision.
 */
@RestrictedRead
@Component
public class AddEvidenceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "setId", string("The hypothesis set id (ULID)."),
            "source", enumeration("Where the evidence comes from.", null,
                    HypothesisVocabulary.INPUT_EVIDENCE_SOURCES.toArray(String[]::new)),
            "ref", string("Object reference (record:{source}/{externalId}, finding:{id}, ...); required unless"
                    + " manual."),
            "description", string("What the evidence says.", 1, 2000),
            "credibility", enumeration("H, M or L.", null, "H", "M", "L"),
            "relevance", enumeration("H, M or L.", null, "H", "M", "L"),
            "ratings", openObject("Per hypothesis id: CC, C, N, NA, I or II (required for manual evidence).",
                    props()),
            "citations", stringArray("Object references.", 100)),
            "setId", "source", "description", "credibility", "relevance");

    private final HypothesisService service;
    private final IntelVisibility visibility;
    private final JsonMapper json;
    private final ai.foresignal.collab.EvidenceItemService evidenceItems;
    private final ai.foresignal.object.ObjectCallers callers;

    /** Creates the tool. */
    public AddEvidenceTool(HypothesisService service, IntelVisibility visibility, JsonMapper json,
            ai.foresignal.collab.EvidenceItemService evidenceItems, ai.foresignal.object.ObjectCallers callers) {
        this.evidenceItems = evidenceItems;
        this.callers = callers;
        this.service = service;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "add_evidence";
    }

    @Override
    public String title() {
        return "Add ACH evidence";
    }

    @Override
    public String description() {
        return "Adds evidence to a hypothesis set's ACH matrix. Re-reports of the same evidence group attach to the"
                + " existing row and add no weight. Manual evidence is rated per hypothesis (CC, C, N, NA, I, II); its"
                + " ratings move the probabilities through a recorded evidence-added revision.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The evidence row");
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
        HypothesisInputs.EvidenceDraft draft = evidenceItems.citable(HypothesisInputs.evidence(body),
                callers.of(context.caller()));
        return ToolResult.json(json.valueToTree(HypothesisViews.evidence(service.addEvidence(setId,
                draft, context.actor(), visibility.seesPrivate(context.caller())), false)));
    }
}
