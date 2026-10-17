package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
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
 * {@code upsert_analysis_series} (agent, prompt 119): creates or replaces an operator row of the analysis-series
 * registry; auto-registration never removes it. Watch pairs set here drive live pre-event signals.
 */
@RestrictedRead
@Component
public class UpsertAnalysisSeriesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("Series reference (series:, rollup: or rule:)."),
            "enabled", bool("Whether the detectors run on it.", true),
            "detectors", enumArray("Detectors.", AnalysisSeriesService.DETECTORS.toArray(String[]::new)),
            "frequency", enumeration("Analysis frequency.", null, AnalysisSeriesService.FREQUENCIES
                    .toArray(String[]::new)),
            "family", string("Signal family of the registry."),
            "regionCode", string("040 region code."),
            "watchPairs", watchPairs()), "ref");

    private final AnalysisSeriesService registry;
    private final JsonMapper json;

    /** Creates the tool. */
    public UpsertAnalysisSeriesTool(AnalysisSeriesService registry, JsonMapper json) {
        this.registry = registry;
        this.json = json;
    }

    /** The schema of an array of {@code WatchPair}. */
    static Map<String, Object> watchPairs() {
        Map<String, Object> item = object(props(
                "eventFamily", string("Signal family of the qualifying events."),
                "regionCodes", JsonSchemas.stringArray("Event regions (empty = any).", 50),
                "minEffect", JsonSchemas.number("Minimum |CAR| in standard deviations.", 0.1, 20),
                "releaseLagDays", JsonSchemas.integer("Publication lag of the series, days.", 0, 60, null)),
                "eventFamily");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "array");
        out.put("description", "Event-study watch pairs.");
        out.put("items", item);
        out.put("maxItems", 50);
        return out;
    }

    @Override
    public String name() {
        return "upsert_analysis_series";
    }

    @Override
    public String title() {
        return "Upsert analysis series";
    }

    @Override
    public String description() {
        return "Registers a series for the change-point detectors and live event studies (upsert by ref) as an"
                + " operator row, with its frequency, family, region and watch pairs.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The analysis series");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.valueToTree(AnalysisSeriesService.view(registry.upsert(args).row())));
    }
}
