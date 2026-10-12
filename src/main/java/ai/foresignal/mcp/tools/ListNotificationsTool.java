package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.notify.inbox.InboxService;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.Severity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_notifications} (read, prompt 033): the newest inbox notifications (alerts, briefings,
 * system events) with the live alert state. An agent has no device, so items carry no read state
 * and listing marks nothing read.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class ListNotificationsTool implements ForesignalMcpTool {

    /** The device key of MCP listings: a key no client can produce, so nothing is ever read. */
    static final String DEVICE = "mcp:none";

    static final Map<String, Object> INPUT = object(props(
            "events", enumArray("Only these events.", "alert", "briefing", "system"),
            "minSeverity", enumeration("Only at or above this severity.", null, "info", "warning", "critical"),
            "before", JsonSchemas.integer("Only notifications with a smaller id (paging).", 1, Long.MAX_VALUE, null),
            "limit", JsonSchemas.integer("How many to return.", 1, 100, 20L)));

    private final InboxService inbox;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListNotificationsTool(InboxService inbox, JsonMapper json) {
        this.inbox = inbox;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_notifications";
    }

    @Override
    public String title() {
        return "List notifications";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists the newest notifications Foresignal sent (alerts, briefings and system"
                + " events such as a failing source), newest first, with each alert's live state (firing, acknowledged,"
                + " resolved). Page with before.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Notifications, newest first");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<String> events = McpArgs.strings(args, "events");
        String min = McpArgs.text(args, "minSeverity");
        int limit = Math.max(1, Math.min(100, McpArgs.integer(args, "limit", 20)));
        List<InboxService.Item> items = inbox.list(DEVICE, new InboxService.Query(null, McpArgs.optionalLong(args, "before"),
                events, min == null ? null : Severity.fromWire(min), false, limit), context.excludedSources(), context.restriction());
        List<Map<String, Object>> out = items.stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>(item.toMap());
            map.remove("read");
            return map;
        }).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", out);
        result.put("total", out.size());
        result.put("truncated", items.size() == limit);
        result.put("nextBefore", items.size() == limit ? items.getLast().id() : null);
        return ToolResult.json(json.valueToTree(result));
    }
}
