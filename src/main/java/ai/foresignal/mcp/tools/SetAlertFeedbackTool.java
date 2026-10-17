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
import ai.foresignal.trigger.Feedback;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code set_alert_feedback} (agent; prompt 115): sets an alert's quality feedback ({@code setAlertFeedback}) in
 * any state, with an optional note.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class SetAlertFeedbackTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "feedback", enumeration("How useful the alert was.", null, "useful", "noise", "late", "false_positive"),
            "note", string("A note recorded on the alert (at most 2000 characters).", 1, AlertService.MAX_NOTE)),
            "alertId", "feedback");

    private final AlertService alerts;
    private final AlertItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public SetAlertFeedbackTool(AlertService alerts, AlertItems items, McpOutputShaper shaper) {
        this.alerts = alerts;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "set_alert_feedback";
    }

    @Override
    public String title() {
        return "Give alert feedback";
    }

    @Override
    public String description() {
        return "Records how useful an alert was (useful, noise, late, false_positive), in any state, with an optional"
                + " note. Feedback drives the rule's quality view and tuning suggestions. Needs an agent token.";
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
        String feedback;
        try {
            feedback = Feedback.fromWire(McpArgs.requiredText(args, "feedback")).wire();
        } catch (IllegalArgumentException e) {
            return ToolResult.error("feedback " + e.getMessage());
        }
        items.visible(id, context.excludedSources(), context.restriction());
        AlertView alert = alerts.setFeedback(id, new AlertService.Transition(McpArgs.text(args, "note"), feedback));
        return ToolResult.json(shaper.object(items.item(alert, Detail.SHORT), Detail.SHORT));
    }
}
