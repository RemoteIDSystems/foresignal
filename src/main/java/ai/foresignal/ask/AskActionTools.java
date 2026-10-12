package ai.foresignal.ask;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.llm.client.ToolSpec;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.tools.WatchItems;
import ai.foresignal.trigger.AlertService;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code propose_*} tools of Ask (prompt 028; invariant EGR-3). They are the only way a model can
 * ask for a change, and they change nothing: each validates its payload exactly as the REST operation
 * would (through its {@link AskActionHandler}) and returns {@code {actionId, status}}; the action is
 * stored {@code pending} with the answer and executed only on confirmation, or at once when an
 * {@code agent} caller set {@code allowActions}.
 *
 * <ul>
 *   <li>{@code propose_watch}: create a watch, or replace one when {@code watchId} is given;
 *   <li>{@code propose_briefing}: create a briefing (offered only once a {@code create_briefing}
 *       handler exists, prompt 029);
 *   <li>{@code propose_ack}: acknowledge an alert;
 *   <li>{@code propose_alert_status}: resolve, snooze or unsnooze an alert;
 *   <li>{@code propose_snooze}: snooze an alert (prompt 033, offered once a {@code snooze_alert}
 *       handler exists);
 *   <li>{@code propose_view}: create a saved view (prompt 036, offered once a {@code create_view}
 *       handler exists).
 * </ul>
 * These tools are never MCP tools: an MCP agent with {@code agent} scope calls the write tools
 * directly.
 */
@Component
public class AskActionTools {

    /** The status a proposal returns to the model. */
    public static final String PENDING_CONFIRMATION = "pending_confirmation";

    static final String REASON_NOTE = "Why you propose it, in one sentence shown to the person who confirms it.";

    private static final Map<String, Object> PROPOSE_WATCH = object(props(
            "watchId", uuid("Only to change an existing watch (from list_watches): its id. Omit to create a new watch."),
            "rule", WatchItems.RULE_SCHEMA,
            "reason", string(REASON_NOTE, 1, 300)), "rule");

    private static final Map<String, Object> PROPOSE_BRIEFING = object(props(
            "briefing", openObject("The briefing definition (BriefingInput): name, schedule, timezone, scope, mode,"
                    + " instructions, channelIds.", Map.of()),
            "reason", string(REASON_NOTE, 1, 300)), "briefing");

    private static final Map<String, Object> PROPOSE_ACK = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "note", string("A note recorded on the alert (at most 2000 characters).", 1, AlertService.MAX_NOTE),
            "feedback", enumeration("How useful the alert was.", null, "useful", "noise", "late", "false_positive"),
            "reason", string(REASON_NOTE, 1, 300)), "alertId");

    private static final Map<String, Object> PROPOSE_ALERT_STATUS = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "action", enumeration("resolve: close the alert. snooze: suppress repeats for the same watch and record"
                    + " until a time. unsnooze: end a snooze.", null, "resolve", "snooze", "unsnooze"),
            "until", dateTime("snooze only: when the snooze ends (RFC 3339, in the future). Give until or for."),
            "for", string("snooze only: how long, for example 2h or PT30M. Give until or for.", 1, 40),
            "note", string("resolve only: a note recorded on the alert.", 1, AlertService.MAX_NOTE),
            "feedback", enumeration("resolve only: how useful the alert was.", null,
                    "useful", "noise", "late", "false_positive"),
            "reason", string(REASON_NOTE, 1, 300)), "alertId", "action");

    private static final Map<String, Object> PROPOSE_SNOOZE = object(props(
            "alertId", JsonSchemas.integer("The alert id (from list_alerts).", 1, Long.MAX_VALUE, null),
            "until", dateTime("When the snooze ends (RFC 3339, in the future). Give until or for."),
            "for", string("How long, for example 1h or PT30M. Give until or for.", 1, 40),
            "reason", string(REASON_NOTE, 1, 300)), "alertId");

    private static final Map<String, Object> PROPOSE_VIEW = object(props(
            "view", ai.foresignal.mcp.tools.CreateViewTool.VIEW_SCHEMA,
            "reason", string(REASON_NOTE, 1, 300)), "view");

    private static final String CONFIRM_NOTE = " Nothing changes now: the proposal is shown to the person, who confirms"
            + " or rejects it. Returns {actionId, status}.";

    private final Map<String, AskActionHandler> handlers;
    private final JsonSchemaValidator validator;
    private final JsonMapper json;

    /**
     * Creates the tools.
     *
     * @throws IllegalStateException when two handlers share a type
     */
    public AskActionTools(List<AskActionHandler> handlers, JsonSchemaValidator validator, JsonMapper json) {
        Map<String, AskActionHandler> byType = new LinkedHashMap<>();
        for (AskActionHandler handler : handlers) {
            if (byType.put(handler.type(), handler) != null) {
                throw new IllegalStateException("Two Ask action handlers have the type " + handler.type());
            }
        }
        this.handlers = Map.copyOf(byType);
        this.validator = validator;
        this.json = json;
    }

    /** The handler of {@code type}, or null. */
    public AskActionHandler handler(String type) {
        return handlers.get(type);
    }

    /** The tools, in a fixed order. */
    public List<ToolSpec> specs() {
        List<ToolSpec> out = new ArrayList<>();
        out.add(spec("propose_watch", "Proposes a watch (a trigger rule that raises an alert when records match), or"
                + " a change to an existing one when watchId is given. The definition is validated exactly as the REST"
                + " API validates it." + CONFIRM_NOTE, PROPOSE_WATCH));
        if (handlers.containsKey("create_briefing")) {
            out.add(spec("propose_briefing", "Proposes a scheduled briefing." + CONFIRM_NOTE, PROPOSE_BRIEFING));
        }
        out.add(spec("propose_ack", "Proposes acknowledging an alert, with an optional note and feedback." + CONFIRM_NOTE,
                PROPOSE_ACK));
        out.add(spec("propose_alert_status", "Proposes resolving, snoozing or unsnoozing an alert." + CONFIRM_NOTE,
                PROPOSE_ALERT_STATUS));
        if (handlers.containsKey("create_view")) {
            out.add(spec("propose_view", "Proposes a saved view: a named record filter (sources, kinds, places,"
                    + " conditions) that records, map tiles and streams render." + CONFIRM_NOTE, PROPOSE_VIEW));
        }
        if (handlers.containsKey("snooze_alert")) {
            out.add(spec("propose_snooze", "Proposes snoozing an alert until a time or for a while, which also stops"
                    + " its escalation." + CONFIRM_NOTE, PROPOSE_SNOOZE));
        }
        return out;
    }

    /** True when {@code name} is one of these tools (whether or not it is offered). */
    public boolean isActionTool(String name) {
        return name != null && name.startsWith("propose_") && schema(name) != null;
    }

    /**
     * Validates a proposal.
     *
     * @param tool the tool the model called
     * @param args its arguments
     * @param caller who asked
     * @return the validated proposal, with a new action id
     * @throws McpToolException when the arguments do not match the tool
     * @throws ai.foresignal.api.error.ForesignalException when the payload is not valid (as on REST)
     */
    public ProposedAction propose(String tool, JsonNode args, AskCaller caller) {
        Map<String, Object> schema = schema(tool);
        if (schema == null || (tool.equals("propose_briefing") && !handlers.containsKey("create_briefing"))
                || (tool.equals("propose_snooze") && !handlers.containsKey("snooze_alert"))
                || (tool.equals("propose_view") && !handlers.containsKey("create_view"))) {
            throw new McpToolException("Unknown tool: " + tool);
        }
        JsonNode arguments = args == null || args.isNull() ? json.createObjectNode() : args;
        @SuppressWarnings("unchecked")
        Map<String, Object> asMap = arguments.isObject() ? json.convertValue(arguments, Map.class) : Map.of();
        JsonSchemaValidator.ValidationResponse validation = validator.validate(schema, asMap);
        if (!arguments.isObject() || !validation.valid()) {
            throw new McpToolException("Invalid arguments for " + tool + ": "
                    + (arguments.isObject() ? validation.errorMessage() : "the arguments must be a JSON object."));
        }
        String type;
        ObjectNode payload = ((ObjectNode) arguments).deepCopy();
        payload.remove("reason");
        switch (tool) {
            case "propose_watch" -> type = payload.hasNonNull("watchId") ? "update_watch" : "create_watch";
            case "propose_briefing" -> {
                type = "create_briefing";
                JsonNode briefing = payload.get("briefing");
                payload = briefing instanceof ObjectNode object ? object.deepCopy() : json.createObjectNode();
            }
            case "propose_ack" -> type = "acknowledge_alert";
            case "propose_snooze" -> type = "snooze_alert";
            case "propose_view" -> {
                type = "create_view";
                JsonNode view = payload.get("view");
                payload = view instanceof ObjectNode object ? object.deepCopy() : json.createObjectNode();
            }
            default -> type = "alert_status";
        }
        AskActionHandler handler = handlers.get(type);
        if (handler == null) {
            throw new McpToolException("The action " + type + " is not available.");
        }
        JsonNode validated = handler.validate(payload, caller);
        String description = handler.describe(validated);
        JsonNode reason = arguments.get("reason");
        if (reason != null && reason.isString() && !reason.asString().isBlank()) {
            description = description + ": " + reason.asString().strip();
        }
        return new ProposedAction(UUID.randomUUID(), type, description, validated);
    }

    private static Map<String, Object> schema(String tool) {
        return switch (tool) {
            case "propose_watch" -> PROPOSE_WATCH;
            case "propose_briefing" -> PROPOSE_BRIEFING;
            case "propose_ack" -> PROPOSE_ACK;
            case "propose_alert_status" -> PROPOSE_ALERT_STATUS;
            case "propose_snooze" -> PROPOSE_SNOOZE;
            case "propose_view" -> PROPOSE_VIEW;
            default -> null;
        };
    }

    private ToolSpec spec(String name, String description, Map<String, Object> schema) {
        return new ToolSpec(name, description, json.valueToTree(schema));
    }
}
