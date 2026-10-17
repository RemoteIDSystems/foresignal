package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.BaseRateViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_base_rate} (read, prompt 121): the outside view for a reference class, the stored rate with that
 * definition or one computed on the spot (cached 24 h), with its interval, sample size, sources and caveats.
 */
@RestrictedRead("base rates are derived statistics, no record content")
@Component
public class GetBaseRateTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "family", string("The family: a signal family plus subtype, e.g. conflict:battles, seismic:m5,"
                    + " disaster:gdacs-orange+."),
            "regionCodes", stringArray("040 region codes (exclusive with regionClass); omit both for worldwide.", 300),
            "regionClass", string("A region class: continent:{AF|AN|AS|EU|NA|OC|SA} or"
                    + " income:{low|lower-middle|upper-middle|high}."),
            "window", enumeration("The window length.", "P28D", "P7D", "P28D", "P90D"),
            "condition", openObject("Optional: P(event in window | antecedent family occurred within withinDays"
                    + " before it).", props("family", string("The antecedent family."), "withinDays",
                            integer("Days before the window, 1 to 365.", 1, 365, null)), "family", "withinDays")),
            "family");

    private final BaseRateService baseRates;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetBaseRateTool(BaseRateService baseRates, JsonMapper json) {
        this.baseRates = baseRates;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_base_rate";
    }

    @Override
    public String title() {
        return "Get base rate";
    }

    @Override
    public String description() {
        return "Returns the base rate (outside view) for a reference class: events per window, the share of windows"
                + " with at least one event (pAtLeastOne) and its 95% Jeffreys interval, the number of windows and"
                + " events, the period, the source mix and licenses, and caveats (SMALL_SAMPLE, LOW_REPORTING_DENSITY,"
                + " MIXED_SOURCES, LOW_DETECTABILITY). Start every probability from it and state it with its interval"
                + " and n; show inside-view adjustments as adjustments.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The base rate");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Map<String, Object> definition = args == null || args.isNull() ? new LinkedHashMap<>()
                : json.readValue(json.writeValueAsString(args), new TypeReference<LinkedHashMap<String, Object>>() {});
        definition.putIfAbsent("window", "P28D");
        return ToolResult.json(json.valueToTree(BaseRateViews.view(baseRates.query(definition), baseRates,
                EgressPath.MCP)));
    }
}
