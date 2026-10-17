package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code find_analogs} (read, prompt 121): past situations like a given one, or past region-periods like a region's
 * recent weeks, with what happened next and, for an outcome, a distribution shrunk toward the base rate.
 */
@RestrictedRead("analogs are situation summaries and statistics; private situations need private access")
@Component
public class FindAnalogsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "situationId", string("A situation to find analogs for (or give regionCode)."),
            "regionCode", string("A 040 region code for region-period analogs (when no situationId)."),
            "from", dateTime("Region-period start (default: 14 days before to)."),
            "to", dateTime("Region-period end (default: now)."),
            "k", integer("How many analogs.", 1, 50, 10L),
            "outcome", string("Target outcome: critical, or {family}/P{n}D such as conflict:battles/P30D.")));

    private final AnalogService analogs;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public FindAnalogsTool(AnalogService analogs, IntelVisibility visibility, JsonMapper json) {
        this.analogs = analogs;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "find_analogs";
    }

    @Override
    public String title() {
        return "Find historical analogs";
    }

    @Override
    public String description() {
        return "Finds historical analogs: past situations similar to a situation (feature and summary-embedding"
                + " similarity, fused; never overlapping it in time), or past two-week periods of the same continent"
                + " similar to a region's recent weeks. Each analog says what happened next (duration, peak severity,"
                + " follow-on families within 7 and 30 days). With an outcome, returns its frequency among the analogs"
                + " shrunk toward the base rate, with the analog count and an interval. Always report countFound and"
                + " the method (feature-only when embeddings are unavailable).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The analog result");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String situationId = McpArgs.text(args, "situationId");
        int k = McpArgs.integer(args, "k", AnalogService.DEFAULT_K);
        String outcome = McpArgs.text(args, "outcome");
        if (situationId != null) {
            return ToolResult.json(json.valueToTree(analogs.forSituation(situationId, Math.max(1, Math.min(50, k)),
                    outcome, visibility.seesPrivate(context.caller()))));
        }
        String region = McpArgs.text(args, "regionCode");
        if (region == null) {
            throw ValidationException.forField("situationId", "give a situationId or a regionCode");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("regionCode", region);
        body.put("from", McpArgs.text(args, "from"));
        body.put("to", McpArgs.text(args, "to"));
        body.put("k", k);
        body.put("outcome", outcome);
        return ToolResult.json(json.valueToTree(analogs.forRegion(body)));
    }
}
