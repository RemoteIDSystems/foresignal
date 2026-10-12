package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.nonNegative;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.trigger.TriggerBacktester;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code test_watch} (read): a backtest of a trigger rule over a time window or a seq range, as
 * {@code testTrigger}; it creates nothing.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class TestWatchTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 100;

    static final Map<String, Object> INPUT = object(props(
            "watchId", uuid("The watch id (from list_watches)."),
            "since", dateTime("Start of the time window to replay (default: 24 hours ago). Use since/until or"
                    + " fromSeq/toSeq, not both."),
            "until", dateTime("End of the time window (default: now)."),
            "fromSeq", nonNegative("Exclusive start of a seq range to replay."),
            "toSeq", nonNegative("Inclusive end of the seq range."),
            "limit", integer("Most matching versions to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, 20L),
            "simulate", bool("Also apply cooldown, snoozes and edges to count the alerts it would have raised.", false),
            "detail", detail()), "watchId");

    private final TriggerBacktester backtester;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public TestWatchTool(TriggerBacktester backtester, RecordItems items, McpOutputShaper shaper) {
        this.backtester = backtester;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "test_watch";
    }

    @Override
    public String title() {
        return "Backtest a watch";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Replays a watch over stored history (a time window, default the last 24 hours, or a"
                + " seq range) and reports how many versions matched, the first matches, and with simulate=true how"
                + " many alerts it would have raised. Changes nothing; use it to tune a watch before relying on it.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Matching versions (items) and the backtest counts");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        TriggerBacktester.Request request = new TriggerBacktester.Request(
                McpArgs.optionalLong(args, "fromSeq"),
                McpArgs.optionalLong(args, "toSeq"),
                McpArgs.instant(args, "since"),
                McpArgs.instant(args, "until"),
                McpArgs.integer(args, "limit", 20),
                McpArgs.bool(args, "simulate", false));
        TriggerBacktester.Result result = backtester.test(McpArgs.requiredUuid(args, "watchId"), request);
        List<RecordRow> visible = result.matches().stream()
                .filter(row -> !context.excludedSources().contains(row.sourceId()))
                .filter(row -> ai.foresignal.security.restrict.RestrictionClamp.testGeoJson(context.restriction(),
                        row.sourceId(), row.kind(), row.geometry()))
                .toList();
        List<ObjectNode> matches = items.items(visible, RecordMapper.Shape.VERSION, detail);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("watchId", result.ruleId().toString());
        extra.put("matchCount", result.matchCount());
        extra.put("fromSeq", result.fromSeq());
        extra.put("toSeq", result.toSeq());
        extra.put("evaluatedFrom", result.evaluatedFrom() == null ? null : result.evaluatedFrom().toString());
        extra.put("simulated", result.simulated());
        extra.put("wouldFire", result.wouldFire());
        extra.put("wouldSuppress", result.wouldSuppress());
        extra.put("truncatedByRetention", result.truncatedByRetention());
        return ToolResult.json(shaper.list(matches, matches.size(), result.truncated(), detail, extra));
    }
}
