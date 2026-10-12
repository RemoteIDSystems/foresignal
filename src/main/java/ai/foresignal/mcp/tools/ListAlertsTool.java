package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.stringArray;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_alerts} (read): alerts newest first, filtered as {@code listAlerts}. */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class ListAlertsTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 200;

    static final Map<String, Object> INPUT = object(props(
            "status", enumArray("Only alerts in these states: firing (new), acknowledged, resolved. Omit for all.",
                    "firing", "acknowledged", "resolved"),
            "severity", enumeration("Only alerts of this severity or higher: info < warning < critical.", null,
                    "info", "warning", "critical"),
            "ruleId", uuid("Only alerts raised by this watch (its id from list_watches)."),
            "source", stringArray("Only alerts on records of these source ids.", 50),
            "since", dateTime("Only alerts fired at or after this RFC 3339 time."),
            "limit", integer("Most alerts to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, 20L),
            "detail", detail()));

    private final AlertService alerts;
    private final AlertItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListAlertsTool(AlertService alerts, AlertItems items, McpOutputShaper shaper) {
        this.alerts = alerts;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_alerts";
    }

    @Override
    public String title() {
        return "List alerts";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists alerts raised by watches, newest first, with severity, state (firing,"
                + " acknowledged, resolved), the watch, the record (source:externalId) and the seq of the version"
                + " that matched. Alerts recorded under a snooze are left out. Use get_alert for one alert with its"
                + " record, and set_alert_status to acknowledge, resolve or snooze.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Alerts newest first");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        UUID rule = McpArgs.uuid(args, "ruleId");
        String severity = McpArgs.text(args, "severity");
        int limit = McpArgs.integer(args, "limit", 20);
        AlertQuery query = new AlertQuery(rule == null ? Set.of() : Set.of(rule), McpArgs.strings(args, "source"),
                List.of(), McpArgs.instant(args, "since"), null, McpArgs.strings(args, "status"), List.of(),
                severity == null ? null : Severity.fromWire(severity), null, false, context.excludedSources(), context.restriction());
        List<AlertView> page = alerts.list(query, null, limit + 1);
        boolean more = page.size() > limit;
        List<ObjectNode> out = page.stream().limit(limit).map(alert -> items.item(alert, detail)).toList();
        return ToolResult.json(shaper.list(out, out.size(), more, detail, null));
    }
}
