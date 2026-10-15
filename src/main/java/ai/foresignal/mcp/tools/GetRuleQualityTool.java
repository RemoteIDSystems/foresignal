package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_rule_quality} (read, prompt 103). */
@RestrictedRead
@Component
public class GetRuleQualityTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("ruleId", string("The rule id.", 1, 64)), "ruleId");

    private final AlertQualityService quality;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetRuleQualityTool(AlertQualityService quality, JsonMapper json) {
        this.quality = quality;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_rule_quality";
    }

    @Override
    public String title() {
        return "Rule quality";
    }

    @Override
    public String description() {
        return "Alert quality for one rule: precision with a Wilson interval, time-to-ack and time-to-resolve,"
                + " flapping, duplicates and the severity distribution. Precision is null with too few labels.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Rule quality");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String ruleId = McpArgs.requiredText(args, "ruleId");
        return ToolResult.json(json.valueToTree(
                quality.forRule(UUID.fromString(ruleId), Instant.now().minus(java.time.Duration.ofDays(30)))));
    }
}
