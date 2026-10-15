package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.backtest.TuningSuggester;
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

/** {@code get_tuning_suggestions} (read, prompt 103). */
@RestrictedRead
@Component
public class GetTuningSuggestionsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("ruleId", string("The rule id.", 1, 64)), "ruleId");

    private final TuningSuggester suggester;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetTuningSuggestionsTool(TuningSuggester suggester, JsonMapper json) {
        this.suggester = suggester;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_tuning_suggestions";
    }

    @Override
    public String title() {
        return "Tuning suggestions";
    }

    @Override
    public String description() {
        return "Deterministic tuning suggestions for one rule, each with a ready patch. They are never applied"
                + " automatically; backtest a patch before applying it.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Tuning suggestions");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String ruleId = McpArgs.requiredText(args, "ruleId");
        return ToolResult.json(json.valueToTree(Map.of("suggestions",
                suggester.forRule(UUID.fromString(ruleId), Instant.now().minus(java.time.Duration.ofDays(30))))));
    }
}
