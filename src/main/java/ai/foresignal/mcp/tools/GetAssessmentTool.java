package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.cycle.AssessmentService;
import ai.foresignal.cycle.CycleVocabulary;
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

/**
 * {@code get_assessment} (read, prompt 128): one assessment by id, or the latest of a cadence, with its key judgments,
 * sections and markdown. Cite assessments as {@code assessment:ID}.
 */
@RestrictedRead("assessments are derived analysis; private ones need private access")
@Component
public class GetAssessmentTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "assessmentId", string("The assessment id (ULID); omit for the latest."),
            "cadence", JsonSchemas.enumeration("With no id: the latest assessment of this cadence.", null,
                    CycleVocabulary.ASSESSMENT_CADENCES.toArray(String[]::new))));

    private final AssessmentService assessments;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetAssessmentTool(AssessmentService assessments, IntelVisibility visibility, JsonMapper json) {
        this.assessments = assessments;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_assessment";
    }

    @Override
    public String title() {
        return "Get assessment";
    }

    @Override
    public String description() {
        return "Returns an analysis assessment (daily, weekly, monthly or on-demand): the bottom line of typed, cited"
                + " key judgments with probabilities and ICD 203 terms, what changed since the previous one, and its"
                + " sections and markdown. Without an id, the latest one (of a cadence when given). Read the latest"
                + " assessment before answering questions about situations, and cite it as assessment:ID.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The assessment");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        boolean seesPrivate = visibility.seesPrivate(context.caller());
        String id = McpArgs.text(args, "assessmentId");
        String cadence = McpArgs.text(args, "cadence");
        var a = id != null ? assessments.get(id, seesPrivate) : assessments.latest(cadence, seesPrivate);
        Map<String, Object> view = AssessmentService.view(a, true);
        view.put("html", null);
        return ToolResult.json(json.valueToTree(view));
    }
}
