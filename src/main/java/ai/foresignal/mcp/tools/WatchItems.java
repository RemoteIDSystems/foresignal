package ai.foresignal.mcp.tools;

import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Shared handling of the watch tools: a watch is a trigger rule (prompt 017), shown in its short or
 * full {@code TriggerRule} form, and written through {@link TriggerRuleService} with the REST
 * validation ({@code RuleValidator}).
 */
@Component
public class WatchItems {

    /** Members of a short watch, in order. */
    static final List<String> SHORT_MEMBERS = List.of("id", "name", "enabled", "mode", "severity", "sources", "kinds",
            "tags", "conditions", "snoozedUntil", "lastFiredAt", "alertCount", "openAlertCount", "lastError");

    /**
     * The schema of a watch definition ({@code TriggerRuleInput}), described for a model. Members
     * not listed here are still accepted and validated exactly as on {@code POST /v1/triggers}.
     */
    static final Map<String, Object> RULE_SCHEMA = rule();

    private final TriggerMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the helper. */
    public WatchItems(TriggerMapper mapper, McpOutputShaper shaper) {
        this.mapper = mapper;
        this.shaper = shaper;
    }

    /** A watch at {@code detail}. */
    public ObjectNode item(TriggerRuleService.RuleView view, Detail detail) {
        ObjectNode full = shaper.objectTree(mapper.rule(view));
        if (detail == Detail.LONG) {
            return full;
        }
        ObjectNode out = shaper.object();
        for (String member : SHORT_MEMBERS) {
            JsonNode value = full.get(member);
            if (value != null && !value.isNull()) {
                out.set(member, value);
            }
        }
        if (full.hasNonNull("geofence") || full.hasNonNull("near")) {
            out.put("spatial", true);
        }
        return out;
    }

    private static Map<String, Object> rule() {
        Map<String, Object> condition = JsonSchemas.openObject("One payload condition", JsonSchemas.props(
                "path", JsonSchemas.string("Dot path into the record payload, for example mag or properties.depth."),
                "op", JsonSchemas.enumeration("Comparison: > >= < <= take numbers; == and != also strings and booleans;"
                        + " in/notIn a non-empty array; exists a boolean; startsWith a string; matches a simple Java"
                        + " regex (no backreferences or lookaround).", null,
                        ">", ">=", "<", "<=", "==", "!=", "in", "notIn", "exists", "startsWith", "matches",
                        "inWatchlist", "notInWatchlist"),
                "value", Map.of("description", "The operand: a number, string, boolean or array as op requires.")),
                "path", "op", "value");
        Map<String, Object> window = JsonSchemas.openObject("Recurring local-time windows in which the watch is active",
                JsonSchemas.props(
                        "timezone", JsonSchemas.string("IANA timezone, for example America/Chicago."),
                        "windows", Map.of("type", "array", "minItems", 1, "description",
                                "Each {days: [MON..SUN], start: HH:mm, end: HH:mm}; end < start spans midnight.",
                                "items", Map.of("type", "object"))),
                "timezone", "windows");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", JsonSchemas.string("A short name for the watch.", 1, 200));
        props.put("enabled", JsonSchemas.bool("Whether the watch runs (default true).", null));
        props.put("sources", JsonSchemas.stringArray("Source ids to watch (from list_sources), at most 50.", 50));
        props.put("kinds", JsonSchemas.stringArray("Record kinds to watch (from list_kinds), at most 50.", 50));
        props.put("conditions", Map.of("type", "array", "items", condition,
                "description", "Payload conditions, all of which must hold (AND)."));
        props.put("mode", JsonSchemas.enumeration("match: every matching version. enter: when a record starts matching"
                + " (or enters the geofence). exit: when it stops. Edge modes need a geofence or a condition.",
                "match", "match", "enter", "exit"));
        props.put("geofence", Map.of("type", "object", "description", "A GeoJSON Polygon or MultiPolygon (or a"
                + " LineString with bufferMeters) in WGS84; records inside it match."));
        props.put("bufferMeters", JsonSchemas.number("Corridor half-width when geofence is a line, in meters.", 1, 50_000));
        props.put("hysteresisMeters", JsonSchemas.number("Edge band in meters for enter/exit, so jitter along the"
                + " boundary does not fire (0–5000).", 0, 5_000));
        props.put("reappearAsEnter", JsonSchemas.bool("Fire enter when a record reappears after expiring (default true"
                + " with a geofence).", null));
        props.put("severity", JsonSchemas.enumeration("Severity of the alerts it raises.", "warning",
                "info", "warning", "critical"));
        props.put("cooldown", JsonSchemas.string("Quiet period per record after an alert, for example 15m, 12h or P1D."));
        props.put("messageTemplate", JsonSchemas.string("Alert text with placeholders such as {{name}}, {{title}},"
                + " {{payload.mag}}.", 1, 1000));
        props.put("tags", JsonSchemas.stringArray("Lower-case tags copied to alerts, at most 16.", 16));
        props.put("activeFrom", JsonSchemas.dateTime("The watch is inactive before this time."));
        props.put("activeUntil", JsonSchemas.dateTime("The watch disables itself after this time."));
        props.put("activeWindow", window);
        return JsonSchemas.openObject("A watch (trigger rule), as TriggerRuleInput in /v1/openapi.json. Required: name,"
                + " and at least one of sources, kinds, conditions or geofence to narrow it. Validated exactly as"
                + " POST /v1/triggers; every problem is reported at once.", props, "name");
    }
}
