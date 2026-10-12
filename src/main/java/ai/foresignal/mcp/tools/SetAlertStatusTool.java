package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
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
 * {@code set_alert_status} (agent): drives the alert lifecycle of prompt 017 through
 * {@link AlertService}, with the same validation as the REST operations: {@code ack}
 * ({@code acknowledgeAlert}), {@code resolve} ({@code resolveAlert}), {@code snooze}
 * ({@code snoozeAlert}, {@code until} or {@code for}) and {@code unsnooze} ({@code unsnoozeAlert}).
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class SetAlertStatusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "action", enumeration("ack: acknowledge. resolve: close the alert (terminal; the next match fires a new"
                    + " alert). snooze: suppress repeat alerts for the same watch and record until a time. unsnooze:"
                    + " end a snooze.", null, "ack", "resolve", "snooze", "unsnooze"),
            "until", dateTime("snooze only: when the snooze ends, an RFC 3339 time in the future. Give until or for."),
            "for", string("snooze only: how long to snooze, for example 2h, 30m or PT2H. Give until or for.", 1, 40),
            "feedback", enumeration("ack and resolve only: how useful the alert was.", null,
                    "useful", "noise", "late", "false_positive"),
            "note", string("ack and resolve only: a note recorded on the alert (at most 2000 characters).", 1,
                    AlertService.MAX_NOTE)), "alertId", "action");

    private final AlertService alerts;
    private final AlertItems items;
    private final McpOutputShaper shaper;
    private final JsonMapper json;

    /** Creates the tool. */
    public SetAlertStatusTool(AlertService alerts, AlertItems items, McpOutputShaper shaper, JsonMapper json) {
        this.alerts = alerts;
        this.items = items;
        this.shaper = shaper;
        this.json = json;
    }

    @Override
    public String name() {
        return "set_alert_status";
    }

    @Override
    public String title() {
        return "Change an alert's status";
    }

    @Override
    public String description() {
        return "Moves an alert through its lifecycle: ack (acknowledge), resolve (close; the next match raises a new"
                + " alert), snooze (suppress repeats for the same watch and record until a time, given as until or"
                + " for) or unsnooze. ack and resolve take an optional note and feedback. Needs an agent token.";
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
        String action = McpArgs.requiredText(args, "action");
        items.visible(id, context.excludedSources(), context.restriction());
        boolean transition = action.equals("ack") || action.equals("resolve");
        boolean snooze = action.equals("snooze");
        if (!transition && (args.hasNonNull("note") || args.hasNonNull("feedback"))) {
            throw new McpToolException("note and feedback apply to ack and resolve only.");
        }
        if (!snooze && (args.hasNonNull("until") || args.hasNonNull("for"))) {
            throw new McpToolException("until and for apply to snooze only.");
        }
        AlertView alert = switch (action) {
            case "ack" -> alerts.acknowledge(id, transition(args), context.actor());
            case "resolve" -> alerts.resolve(id, transition(args), context.actor());
            case "snooze" -> alerts.snooze(id, snoozeBody(args));
            case "unsnooze" -> alerts.unsnooze(id);
            default -> throw new McpToolException("action must be ack, resolve, snooze or unsnooze.");
        };
        return ToolResult.json(shaper.object(items.item(alert, Detail.SHORT), Detail.SHORT));
    }

    private AlertService.Transition transition(JsonNode args) {
        return AlertService.transition(AcknowledgeAlertTool.transitionBody(json, args), false);
    }

    /** The {@code SnoozeInput} body: {@code until} or {@code for}, validated by {@code Snoozes}. */
    private ObjectNode snoozeBody(JsonNode args) {
        ObjectNode body = json.createObjectNode();
        if (args.hasNonNull("until")) {
            body.set("until", args.get("until"));
        }
        if (args.hasNonNull("for")) {
            body.set("for", args.get("for"));
        }
        return body;
    }
}
