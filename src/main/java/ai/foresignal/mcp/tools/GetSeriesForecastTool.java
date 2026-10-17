package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.outlook.series.SeriesForecastEntity;
import ai.foresignal.outlook.series.SeriesForecastService;
import ai.foresignal.outlook.series.SeriesForecastViews;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_series_forecast} (read, prompt 126): a series' latest method-selected quantile forecasts for 1, 7 and 28
 * days with the selected method and its backtest; {@code LOCAL_ONLY} series in standardized units for callers
 * without local egress.
 */
@RestrictedRead
@Component
public class GetSeriesForecastTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The series reference (series:, rollup: or rule:)."),
            "horizon", JsonSchemas.enumeration("Only this horizon in days.", null, "1", "7", "28")), "ref");

    private final SeriesForecastService series;
    private final SeriesReader reader;
    private final AnalyticsAccess access;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetSeriesForecastTool(SeriesForecastService series, SeriesReader reader, AnalyticsAccess access,
            JsonMapper json) {
        this.series = series;
        this.reader = reader;
        this.access = access;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_series_forecast";
    }

    @Override
    public String title() {
        return "Get series forecast";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns Foresignal's statistical forecast of a series: quantiles p05–p95 for 1, 7 and"
                + " 28 days ahead from the method a rolling-origin backtest selected, with the backtest's pinball"
                + " losses and the ledger questions that score it. Quote quantiles as ranges, not point predictions.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The forecast and its model");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        SeriesRef ref = SeriesRef.parse(McpArgs.requiredText(args, "ref"));
        String h = McpArgs.text(args, "horizon");
        Integer horizon = h == null ? null : Integer.valueOf(ai.foresignal.api.support.QueryParams.oneOf("horizon",
                h, List.of("1", "7", "28"), null));
        boolean withheld = access.checkSeries(ref, reader.meta(ref), context.readCaller(), context.excludedSources(),
                context.restriction(), EgressPath.MCP);
        List<SeriesForecastEntity> rows = series.latest(ref.format());
        Map<String, Object> out = new LinkedHashMap<>(SeriesForecastViews.forecast(rows, horizon,
                withheld && rows.get(0).isLocalOnly(), series.questionIds(ref.format())));
        out.put("model", SeriesForecastViews.model(series.model(ref.format()), withheld && rows.get(0).isLocalOnly()));
        return ToolResult.json(json.valueToTree(out));
    }
}
