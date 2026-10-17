package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.cycle.AssessmentEntity;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code list_assessments} (read, prompt 128): assessments newest first, as id, cadence, status and headline. */
@RestrictedRead("assessments are derived analysis; private ones need private access")
@Component
public class ListAssessmentsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "cadence", JsonSchemas.enumeration("Cadence filter.", null,
                    CycleVocabulary.ASSESSMENT_CADENCES.toArray(String[]::new)),
            "situationId", string("Only on-demand assessments of this situation."),
            "since", dateTime("Created at or after."),
            "limit", JsonSchemas.integer("Most items.", 1, 100, 20L)));

    private final AssessmentService assessments;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListAssessmentsTool(AssessmentService assessments, IntelVisibility visibility, JsonMapper json) {
        this.assessments = assessments;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_assessments";
    }

    @Override
    public String title() {
        return "List assessments";
    }

    @Override
    public String description() {
        return "Lists analysis assessments newest first: id, cadence, period, status and headline. Use get_assessment"
                + " for the judgments and sections.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Assessments");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String cadence = McpArgs.text(args, "cadence");
        if (cadence != null && !CycleVocabulary.ASSESSMENT_CADENCES.contains(cadence)) {
            throw new ai.foresignal.mcp.McpToolException("cadence must be daily, weekly, monthly or on-demand");
        }
        int limit = Math.max(1, Math.min(100, McpArgs.integer(args, "limit", 20)));
        AssessmentService.Page<AssessmentEntity> page = assessments.list(cadence, McpArgs.text(args, "situationId"),
                McpArgs.instant(args, "since"), null, limit, visibility.seesPrivate(context.caller()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (AssessmentEntity a : page.items()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("cadence", a.getCadence());
            item.put("situationId", a.getSituationId());
            item.put("periodStart", a.getPeriodStart().toString());
            item.put("periodEnd", a.getPeriodEnd().toString());
            item.put("status", a.getStatus());
            item.put("headline", a.getHeadline());
            item.put("createdAt", a.getCreatedAt().toString());
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", items.size());
        out.put("truncated", page.nextCursor() != null);
        return ToolResult.json(json.valueToTree(out));
    }
}
