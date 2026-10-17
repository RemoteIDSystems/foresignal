package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingViews;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_finding} (read, prompt 119): one analytic finding in evidence, inference and caveats sections. */
@RestrictedRead
@Component
public class GetFindingTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("findingId", string("The finding id.")), "findingId");

    private final FindingService findings;
    private final AnalyticsAccess access;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetFindingTool(FindingService findings, AnalyticsAccess access, IntelVisibility visibility,
            JsonMapper json) {
        this.findings = findings;
        this.access = access;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_finding";
    }

    @Override
    public String title() {
        return "Get analytic finding";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one statistical finding: evidence (series and pinned members), inference"
                + " (detector, statistics, base rate, explanation) and caveats. Quote the base rate and caveats with"
                + " any claim; the finding says how unusual a pattern is, not why it happened.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The finding");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        AnalyticFindingEntity f = findings.visible(McpArgs.requiredText(args, "findingId"),
                visibility.seesPrivate(context.caller()));
        return ToolResult.json(json.valueToTree(FindingViews.full(f, findings.linkedSituation(f).orElse(null),
                access.withheld(f, context.readCaller(), EgressPath.MCP))));
    }
}
