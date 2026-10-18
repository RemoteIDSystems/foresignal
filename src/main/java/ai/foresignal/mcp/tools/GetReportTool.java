package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.collab.AccessAction;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportPublisher;
import ai.foresignal.report.ReportService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_report} (read, prompt 132): one report as {@code GET /v1/reports/{id}} (its blocks with pinned envelopes
 * and snapshots), optionally with the Markdown rendition of the latest published revision.
 */
@RestrictedRead("returns a report the caller may see (130 access lists); envelopes are resolved for the caller")
@Component
public class GetReportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "reportId", string("The report id or display id (RPT-yyyy-nnnn)."),
            "markdown", JsonSchemas.bool("Also return the Markdown rendition of the latest published revision.",
                    false)), "reportId");

    private final ReportService reports;
    private final ReportPublisher publisher;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetReportTool(ReportService reports, ReportPublisher publisher, ObjectCallers callers, JsonMapper json) {
        this.reports = reports;
        this.publisher = publisher;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_report";
    }

    @Override
    public String title() {
        return "Get a report";
    }

    @Override
    public String description() {
        return "Returns one report: title, status, TLP label, authors, source and its ordered blocks (key judgments with"
                + " ICD 203 terms, pinned objects, maps, timelines, hypothesis and forecast snapshots, graded evidence,"
                + " coverage caveats, sources). With markdown=true it adds the latest published revision as Markdown."
                + " Blocks labeled textOrigin=llm are LLM narrative; treat them as such.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A Report, with markdown when asked");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Reading a report needs a token.");
        }
        ReportEntity r = reports.require(McpArgs.requiredText(args, "reportId"), caller, AccessAction.VIEW);
        Map<String, Object> out = new LinkedHashMap<>(reports.view(r, caller));
        if (McpArgs.bool(args, "markdown", false)) {
            out.put("markdown", r.getPublishedRevision() == null ? null
                    : publisher.rendition(r.getId(), "md", null, false, caller));
        }
        return ToolResult.json(json.valueToTree(out));
    }
}
