package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code snooze_alert} (agent, prompt 033): snoozes an alert ({@code snoozeAlert}): repeats for the
 * same watch and record are recorded suppressed until the snooze ends, and its escalation stops.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class SnoozeAlertTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts or list_notifications).", 1, Long.MAX_VALUE, null),
            "until", dateTime("When the snooze ends, an RFC 3339 time in the future. Give until or for."),
            "for", string("How long, for example 1h, 30m or PT2H. Give until or for.", 1, 40)), "alertId");

    private final AlertService alerts;
    private final AlertItems items;
    private final McpOutputShaper shaper;
    private final JsonMapper json;

    /** Creates the tool. */
    public SnoozeAlertTool(AlertService alerts, AlertItems items, McpOutputShaper shaper, JsonMapper json) {
        this.alerts = alerts;
        this.items = items;
        this.shaper = shaper;
        this.json = json;
    }

    @Override
    public String name() {
        return "snooze_alert";
    }

    @Override
    public String title() {
        return "Snooze an alert";
    }

    @Override
    public String description() {
        return "Snoozes an alert until a time (until) or for a while (for): repeats for the same watch and record are"
                + " recorded but not notified, and its escalation stops. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The alert after the change");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        long id = McpArgs.requiredLong(args, "alertId");
        items.visible(id, context.excludedSources(), context.restriction());
        ObjectNode body = json.createObjectNode();
        if (args.hasNonNull("until")) {
            body.set("until", args.get("until"));
        }
        if (args.hasNonNull("for")) {
            body.set("for", args.get("for"));
        }
        AlertView alert = alerts.snooze(id, body);
        return ToolResult.json(shaper.object(items.item(alert, Detail.SHORT), Detail.SHORT));
    }
}
