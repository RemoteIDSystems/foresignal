package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.report.ReportService;
import ai.foresignal.report.ReportVocabulary;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_reports} (read, prompt 132): the reports the caller owns, authors or that are shared with it, as
 * {@code GET /v1/reports}, with status, TLP, template, source and published revision.
 */
@RestrictedRead("returns report metadata the caller may see (130 access lists); blocks hold only pinned envelopes")
@Component
public class ListReportsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", JsonSchemas.enumeration("Only reports in this status.", null,
                    ReportVocabulary.STATUSES.toArray(String[]::new)),
            "scope", JsonSchemas.enumeration("Owned or authored, shared with you, or both.", "all", "mine", "shared",
                    "all"),
            "sourceRef", string("Only reports built from this object reference."),
            "cursor", string("The nextCursor of the previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)));

    private final ReportService reports;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListReportsTool(ReportService reports, ObjectCallers callers, JsonMapper json) {
        this.reports = reports;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_reports";
    }

    @Override
    public String title() {
        return "List reports";
    }

    @Override
    public String description() {
        return "Lists analyst reports you own, author or that are shared with you: status (draft, review, published,"
                + " retracted), TLP label, template, the source they were built from and the latest published revision."
                + " Read one with get_report.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A ReportPage");
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
            throw new McpToolException("Listing reports needs a token.");
        }
        String status = McpArgs.text(args, "status");
        if (status != null && !ReportVocabulary.isStatus(status)) {
            throw new McpToolException("status must be one of " + ReportVocabulary.STATUSES);
        }
        String scope = McpArgs.text(args, "scope");
        String sourceRef = McpArgs.text(args, "sourceRef");
        if (sourceRef != null && !ObjectRef.isValid(sourceRef)) {
            throw new McpToolException("sourceRef must be an object reference such as situation:01J…");
        }
        Map<String, Object> page = reports.list(caller, status, scope == null ? "all" : scope, sourceRef,
                McpArgs.text(args, "cursor"), Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))));
        return ToolResult.json(json.valueToTree(page));
    }
}
