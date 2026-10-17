package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.outlook.regional.OutlookViews;
import ai.foresignal.outlook.regional.RegionalOutlookEntity;
import ai.foresignal.outlook.regional.RegionalOutlookService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_outlook} (read, prompt 126): a region's CAST-style count outlook for one family (six 4-week windows),
 * past windows against actuals, and the measured skill; without {@code region} and {@code family}, a page of the
 * latest outlooks (optionally filtered by one of them).
 */
@RestrictedRead
@Component
public class GetOutlookTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "region", string("040 region code (country or admin-1)."),
            "family", string("Outlook family, e.g. conflict:battles, outage, weather-severe, seismic:m4.5."),
            "limit", JsonSchemas.integer("Page size of the list form.", 1, 200, 50L)));

    private final RegionalOutlookService outlooks;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetOutlookTool(RegionalOutlookService outlooks, JsonMapper json) {
        this.outlooks = outlooks;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_outlook";
    }

    @Override
    public String title() {
        return "Get regional outlook";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns Foresignal's regional count outlook: quantiles of the event count for the next"
                + " six 4-week windows, each a scored ledger question, with the outlook's out-of-sample skill and"
                + " p10–p90 coverage and the LOW_REPORTING_DENSITY and LOW_DETECTABILITY flags. Always quote the skill"
                + " and flags with the numbers.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The outlook, or a page of outlooks");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String region = McpArgs.text(args, "region");
        String family = McpArgs.text(args, "family");
        if (region != null && family != null) {
            RegionalOutlookEntity o = outlooks.latest(region, family);
            Map<String, Object> out = OutlookViews.outlook(o, outlooks.history(region, family, 26));
            out.put("skill", outlooks.skill(region, family));
            return ToolResult.json(json.valueToTree(out));
        }
        RegionalOutlookService.Page page = outlooks.list(family, region, null, McpArgs.integer(args, "limit", 50));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(o -> OutlookViews.outlook(o, null)).toList());
        out.put("nextCursor", page.nextCursor());
        return ToolResult.json(json.valueToTree(out));
    }
}
