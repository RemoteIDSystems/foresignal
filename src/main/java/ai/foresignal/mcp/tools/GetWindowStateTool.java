package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.window.RuleType;
import ai.foresignal.trigger.window.WindowStateEntity;
import ai.foresignal.trigger.window.WindowSpec;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code get_window_state} (read, prompt 100): per-group state, value and reason of a windowed rule. */
@ai.foresignal.security.restrict.RestrictedRead("window counts and states per rule group: group keys and counts, no record payloads")
@Component
public class GetWindowStateTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("ruleId", uuid("The rule id.")), "ruleId");

    private final TriggerRuleRepository rules;
    private final ai.foresignal.trigger.window.WindowEngine engine;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetWindowStateTool(TriggerRuleRepository rules, ai.foresignal.trigger.window.WindowEngine engine,
            McpOutputShaper shaper) {
        this.rules = rules;
        this.engine = engine;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_window_state";
    }

    @Override
    public String title() {
        return "Window state";
    }

    @Override
    public String description() {
        return "The per-group state of a windowed (count, absence or anomaly) rule: active, clear, unknown or"
                + " learning, with the last value and why it is unknown. 'unknown' means the input was not covered,"
                + " not that nothing happened.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Per-group window state");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        UUID ruleId = McpArgs.requiredUuid(args, "ruleId");
        TriggerRuleEntity rule = rules.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("No such rule: " + ruleId));
        RuleType type = RuleType.fromWire(rule.getType());
        WindowSpec spec = WindowSpec.fromStored(rule.getWindowSpec());
        List<ObjectNode> items = new ArrayList<>();
        for (WindowStateEntity state : engine.windowState(ruleId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("groupKey", state.getGroupKey());
            item.put("state", state.getState());
            item.put("lastValue", state.getLastValue());
            if (spec != null && type == RuleType.COUNT && spec.threshold() != null) {
                item.put("threshold", spec.threshold());
            }
            item.put("reason", state.getReason());
            items.add(shaper.objectTree(item));
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
