package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.impact.ImpactQueryService;
import ai.foresignal.impact.ImpactViews;
import ai.foresignal.impact.hazard.HazardForecast;
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
 * {@code list_projected_impacts} (read, prompt 127): the current {@code projected-impact} findings, highest
 * probability first within each hazard; with {@code findingId}, one projection with its inputs, exposed assets,
 * dependency paths, labels and ledger question.
 */
@RestrictedRead
@Component
public class ListProjectedImpactsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "findingId", string("A projection's finding id; returns that projection in full."),
            "hazardType", JsonSchemas.enumeration("Hazard type filter.", null,
                    HazardForecast.TYPES.toArray(String[]::new)),
            "region", string("Country or admin1 code, e.g. US or US-TX."),
            "since", dateTime("Only projections detected at or after this time."),
            "minProbability", JsonSchemas.number("Minimum pImpact.", 0, 1),
            "cursor", string("Cursor from a previous page."),
            "limit", JsonSchemas.integer("Page size.", 1, 200, 50L)));

    private final ImpactQueryService impacts;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListProjectedImpactsTool(ImpactQueryService impacts, IntelVisibility visibility, JsonMapper json) {
        this.impacts = impacts;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_projected_impacts";
    }

    @Override
    public String title() {
        return "List projected impacts";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns Foresignal's projected impacts of hazard forecasts: P(impact) = P(hazard at the"
                + " asset or region, from the producer's published probabilities) × P(impact | hazard class, from"
                + " history), with intervals, sample sizes, exposed assets and dependency paths. Every projection is"
                + " modelled; heuristic links may be wrong; it is not an official warning. Always quote that label,"
                + " the interval and n with the number.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of projected impacts, or one projection");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        boolean seesPrivate = visibility.seesPrivate(context.caller());
        String findingId = McpArgs.text(args, "findingId");
        if (findingId != null) {
            return ToolResult.json(json.valueToTree(ImpactViews.projected(impacts.get(findingId, seesPrivate),
                    true)));
        }
        String hazardType = McpArgs.text(args, "hazardType");
        if (hazardType != null && !HazardForecast.TYPES.contains(hazardType)) {
            throw ValidationException.forField("hazardType", "must be one of " + HazardForecast.TYPES);
        }
        String region = McpArgs.text(args, "region");
        if (region != null && !ImpactQueryService.REGION.matcher(region).matches()) {
            throw ValidationException.forField("region", "'" + region + "' is not a country or admin1 code");
        }
        String min = McpArgs.numberText(args, "minProbability");
        Double minProbability = min == null ? null : Double.valueOf(min);
        if (minProbability != null && (minProbability < 0 || minProbability > 1)) {
            throw ValidationException.forField("minProbability", "must be between 0 and 1");
        }
        ImpactQueryService.Page<AnalyticFindingEntity> page = impacts.list(new ImpactQueryService.Query(hazardType,
                region, McpArgs.instant(args, "since"), minProbability, McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50)))), seesPrivate);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(f -> ImpactViews.projected(f, false)).toList());
        out.put("nextCursor", page.nextCursor());
        return ToolResult.json(json.valueToTree(out));
    }
}
