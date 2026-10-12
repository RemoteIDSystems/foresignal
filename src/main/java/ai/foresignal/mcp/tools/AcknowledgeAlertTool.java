package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
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
 * {@code acknowledge_alert} (agent): acknowledges an alert, with an optional note and feedback, as
 * {@code acknowledgeAlert}; idempotent, and a no-op on a resolved alert.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class AcknowledgeAlertTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "note", string("A note recorded on the alert (at most 2000 characters).", 1, AlertService.MAX_NOTE),
            "feedback", enumeration("How useful the alert was: useful, noise, late or false_positive.", null,
                    "useful", "noise", "late", "false_positive")), "alertId");

    private final AlertService alerts;
    private final AlertItems items;
    private final McpOutputShaper shaper;
    private final JsonMapper json;

    /** Creates the tool. */
    public AcknowledgeAlertTool(AlertService alerts, AlertItems items, McpOutputShaper shaper, JsonMapper json) {
        this.alerts = alerts;
        this.items = items;
        this.shaper = shaper;
        this.json = json;
    }

    @Override
    public String name() {
        return "acknowledge_alert";
    }

    @Override
    public String title() {
        return "Acknowledge an alert";
    }

    @Override
    public String description() {
        return "Acknowledges a firing alert so people know it is being handled, optionally with a note and feedback"
                + " (useful, noise, late, false_positive). Repeating it changes nothing; a resolved alert stays"
                + " resolved. Needs an agent token. For resolve or snooze use set_alert_status.";
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
        AlertService.Transition transition = AlertService.transition(transitionBody(json, args), false);
        AlertView alert = alerts.acknowledge(id, transition, context.actor());
        return ToolResult.json(shaper.object(items.item(alert, Detail.SHORT), Detail.SHORT));
    }

    /** The {@code AlertTransitionInput} body of {@code args}: its note and feedback. */
    static ObjectNode transitionBody(JsonMapper json, JsonNode args) {
        ObjectNode body = json.createObjectNode();
        if (args.hasNonNull("note")) {
            body.set("note", args.get("note"));
        }
        if (args.hasNonNull("feedback")) {
            body.set("feedback", args.get("feedback"));
        }
        return body;
    }
}
