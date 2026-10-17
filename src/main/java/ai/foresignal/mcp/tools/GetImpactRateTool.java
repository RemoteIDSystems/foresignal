package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.impact.ImpactPairs;
import ai.foresignal.impact.rate.ImpactRateService;
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
 * {@code get_impact_rate} (read, prompt 127): the historical conditional rate P(impact | hazard class) of a built-in
 * impact pair in a region class, with its 95% interval and sample size; without a region class, the rate in every
 * continent class.
 */
@RestrictedRead
@Component
public class GetImpactRateTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "hazardClass", string("Hazard class, e.g. tropical-wind-34, tornado, excessive-rain."),
            "impactFamily", string("Impact family, e.g. outage, ground-stop, river-flood."),
            "regionClass", string("Region class, e.g. continent:NA; omit for every continent class.")),
            "hazardClass", "impactFamily");

    private final ImpactRateService rates;
    private final ImpactPairs pairs;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetImpactRateTool(ImpactRateService rates, ImpactPairs pairs, JsonMapper json) {
        this.rates = rates;
        this.pairs = pairs;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_impact_rate";
    }

    @Override
    public String title() {
        return "Get impact rate";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns how often an impact (an outage, a ground stop, a river at moderate flood) was"
                + " reported in the same admin1 region after a hazard class in Foresignal's own history, with a 95%"
                + " interval and n. Pairs with fewer than 20 hazard occurrences are insufficient: no rate is given and"
                + " none should be assumed.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The conditional rates of the pair");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String hazardClass = McpArgs.requiredText(args, "hazardClass");
        String impactFamily = McpArgs.requiredText(args, "impactFamily");
        String regionClass = McpArgs.text(args, "regionClass");
        if (pairs.hazardClass(hazardClass) == null) {
            throw ValidationException.forField("hazardClass", "is not a hazard class of the built-in pairs");
        }
        if (pairs.family(impactFamily) == null) {
            throw ValidationException.forField("impactFamily", "is not an impact family of the built-in pairs");
        }
        if (regionClass != null && !RegionClasses.valid(regionClass)) {
            throw ValidationException.forField("regionClass", "must be continent:XX or income:band");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", rates.list(hazardClass, impactFamily, regionClass).stream()
                .map(ImpactRateService.ImpactRate::view).toList());
        return ToolResult.json(json.valueToTree(out));
    }
}
