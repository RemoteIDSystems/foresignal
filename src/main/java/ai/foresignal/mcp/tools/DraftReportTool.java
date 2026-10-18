package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.report.ReportDrafter;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportService;
import ai.foresignal.report.ReportVocabulary;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code draft_report} (agent, prompt 132): builds and stores a deterministic report draft from a source, as
 * {@code POST /v1/reports/draft}; {@code narrative=true} adds a labeled LLM narrative block (budgeted).
 */
@RestrictedRead("returns the new draft; its envelopes are resolved for the caller (130 access lists)")
@Component
public class DraftReportTool implements ForesignalMcpTool {

    static final Map<String, Object> SOURCE = JsonSchemas.openObject("A ReportSource: type (selection, tag,"
            + " investigation, chronicle, situation, workspace), ref (the object reference, except for selection) and refs"
            + " (for selection, up to 500 references)", props(
                    "type", JsonSchemas.enumeration("The source type.", null,
                            ReportVocabulary.SOURCE_TYPES.toArray(String[]::new)),
                    "ref", string("The source object reference, e.g. situation:01J…"),
                    "refs", JsonSchemas.stringArray("The selected references (selection).", 500)), "type");

    static final Map<String, Object> INPUT = object(props(
            "source", SOURCE,
            "template", JsonSchemas.enumeration("The template.", "custom",
                    ReportVocabulary.TEMPLATES.toArray(String[]::new)),
            "title", string("A title (default: derived from the source).", 1, 300),
            "assessmentId", string("The 128 assessment a weekly-assessment report wraps."),
            "narrative", JsonSchemas.bool("Add a labeled LLM narrative block (spends LLM budget).", false)),
            "source");

    private final ReportDrafter drafter;
    private final ReportService reports;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public DraftReportTool(ReportDrafter drafter, ReportService reports, ObjectCallers callers, JsonMapper json) {
        this.drafter = drafter;
        this.reports = reports;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "draft_report";
    }

    @Override
    public String title() {
        return "Draft a report";
    }

    @Override
    public String description() {
        return "Builds a report draft from a selection, tag, investigation, chronicle, situation or workspace with a"
                + " template (situation-brief, investigation-summary, chronicle-recap, weekly-assessment, custom). The"
                + " draft is deterministic: key judgments with ICD 203 terms from linked hypothesis sets and questions,"
                + " a summary, timeline, cited objects, maps, snapshots and caveats. narrative=true adds an LLM"
                + " narrative block labeled llm with verified citations. The draft is stored for its authors to edit"
                + " and publish; nothing is sent.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The drafted Report");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous() || caller.principal() == null) {
            throw new McpToolException("Drafting a report needs a token.");
        }
        ReportEntity r = drafter.draft(args, caller);
        return ToolResult.json(json.valueToTree(reports.view(reports.get(r.getId()), caller)));
    }
}
