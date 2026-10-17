package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.finding.FindingViews;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** {@code list_findings} (read, prompt 119): analytic findings newest first, each with its base rate and caveats. */
@RestrictedRead
@Component
public class ListFindingsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "type", enumArray("Finding types.", FindingService.TYPES.toArray(String[]::new)),
            "severity", enumArray("Severities.", FindingSeverities.ALL.toArray(String[]::new)),
            "status", enumArray("Statuses (default every status).", FindingService.STATUSES.toArray(String[]::new)),
            "region", stringArray("040 region codes, e.g. YE or US-TX.", 50),
            "ref", string("Only findings over this series reference."),
            "since", dateTime("Only findings detected at or after this time."),
            "situationId", string("Only findings linked to this situation."),
            "limit", integer("Page size.", 1, 100, 25L),
            "cursor", string("The nextCursor of the previous page.")));

    private final FindingService findings;
    private final AnalyticsAccess access;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListFindingsTool(FindingService findings, AnalyticsAccess access, IntelVisibility visibility,
            JsonMapper json) {
        this.findings = findings;
        this.access = access;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_findings";
    }

    @Override
    public String title() {
        return "List analytic findings";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists statistical findings (change points, pre-event signals, unexplained moves,"
                + " co-occurrences) newest first. Always quote a finding's base rate and caveats; a pre-event signal is"
                + " never evidence of wrongdoing. Use get_finding for one finding's evidence and inference.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of analytic findings");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, tools.jackson.databind.JsonNode args) {
        List<String> types = check("type", McpArgs.strings(args, "type"), FindingService.TYPES);
        List<String> severities = check("severity", McpArgs.strings(args, "severity"), FindingSeverities.ALL);
        List<String> statuses = check("status", McpArgs.strings(args, "status"), FindingService.STATUSES);
        String ref = McpArgs.text(args, "ref");
        FindingService.Page page = findings.list(new FindingService.ListQuery(types, severities, statuses,
                McpArgs.strings(args, "region"), ref == null ? null : SeriesRef.parse(ref).format(),
                McpArgs.instant(args, "since"), McpArgs.text(args, "situationId"), McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(100, McpArgs.integer(args, "limit", 25)))),
                visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(FindingViews.page(page, f -> FindingViews.full(f,
                findings.linkedSituation(f).orElse(null), access.withheld(f, context.readCaller(), EgressPath.MCP)))));
    }

    static List<String> check(String field, List<String> values, List<String> allowed) {
        for (String value : values) {
            if (!allowed.contains(value)) {
                throw ValidationException.forField(field, "must be one of " + allowed);
            }
        }
        return values;
    }
}
