package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code watch_for_unusual_activity} (agent, prompt 100): creates an anomaly rule over sources or
 * kinds and, optionally, a group and an area. The reply states the warm-up period, because the
 * baseline learns over weeks before it can alert.
 */
@ai.foresignal.security.restrict.RestrictedRead("creates an anomaly watch; returns the rule, no record content")
@Component
public class WatchForUnusualActivityTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "name", string("A name for the watch.", 1, 200),
            "sources", stringArray("Sources to watch (at least one of sources or kinds).", 20),
            "kinds", stringArray("Kinds to watch.", 20),
            "place", string("A place id to watch.", 1, 64),
            "groupBy", string("null, source, place, admin1, country, h3:<res> or a payload path.", 1, 128),
            "direction", enumeration("up, down or both.", "up", "up", "down", "both"),
            "sensitivity", enumeration("low, medium or high.", "medium", "low", "medium", "high")));

    private final TriggerRuleService rules;
    private final JsonMapper json;

    /** Creates the tool. */
    public WatchForUnusualActivityTool(TriggerRuleService rules, JsonMapper json) {
        this.rules = rules;
        this.json = json;
    }

    @Override
    public String name() {
        return "watch_for_unusual_activity";
    }

    @Override
    public String title() {
        return "Watch for unusual activity";
    }

    @Override
    public String description() {
        return "Creates an anomaly rule: it learns a coverage-aware baseline per slot over several weeks, then"
                + " alerts when a bucket is unusual. Warm-up is at least 8 weeks (or 14 days in hour-of-day mode);"
                + " until then results are 'learning'. A coverage drop never alerts.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created rule");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<String> sources = McpArgs.strings(args, "sources");
        List<String> kinds = McpArgs.strings(args, "kinds");
        if (sources.isEmpty() && kinds.isEmpty()) {
            throw new ai.foresignal.api.error.ValidationException("Provide sources or kinds.");
        }
        Map<String, Object> window = new LinkedHashMap<>();
        window.put("window", "1h");
        window.put("direction", McpArgs.text(args, "direction") == null ? "up" : McpArgs.text(args, "direction"));
        window.put("sensitivity",
                McpArgs.text(args, "sensitivity") == null ? "medium" : McpArgs.text(args, "sensitivity"));
        String groupBy = McpArgs.text(args, "groupBy");
        if (groupBy != null) {
            window.put("groupBy", groupBy);
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("name", McpArgs.text(args, "name") == null ? "Unusual activity" : McpArgs.text(args, "name"));
        input.put("sources", sources);
        input.put("kinds", kinds);
        input.put("type", "anomaly");
        input.put("window", window);
        input.put("autoResolve", Map.of("onInverseEdge", true));
        String place = McpArgs.text(args, "place");
        if (place != null) {
            input.put("near", Map.of("placeIds", List.of(place), "radiusKm", 50));
        }
        TriggerRuleService.RuleView view = rules.create(json.valueToTree(input));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", view.rule().getId().toString());
        out.put("type", "anomaly");
        out.put("warmUpWeeks", 8);
        out.put("message", "Anomaly rule created. It learns for at least 8 weeks (or 14 days in hour-of-day mode)"
                + " before it can alert; until then its state is 'learning'.");
        return ToolResult.json(json.valueToTree(out));
    }
}
