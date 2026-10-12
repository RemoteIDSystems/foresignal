package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_alert} (read): one alert with the record version that matched, falling back to the
 * alert's {@code recordSnapshot} once retention has removed that version.
 */
@Component
public class GetAlertTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "detail", detail()), "alertId");

    private final AlertItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetAlertTool(AlertItems items, McpOutputShaper shaper) {
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_alert";
    }

    @Override
    public String title() {
        return "Get one alert";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one alert with its lifecycle (state, who acknowledged or resolved it,"
                + " feedback, snooze) and the record version that matched (recordFrom=version), or the snapshot"
                + " taken when it fired once that version has been purged (recordFrom=snapshot).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The alert with record and recordFrom");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        long id = McpArgs.requiredLong(args, "alertId");
        return ToolResult.json(shaper.object(items.withRecord(items.visible(id, context.excludedSources()), detail),
                detail));
    }
}
