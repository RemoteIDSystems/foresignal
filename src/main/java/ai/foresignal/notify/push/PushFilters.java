package ai.foresignal.notify.push;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.trigger.Severity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Which notifications a push target receives (OpenAPI {@code PushFilters}, prompt 031; shared with
 * the native devices of 032).
 *
 * @param events the events received ({@code alert}, {@code briefing}, {@code system})
 * @param ruleIds the rules whose alerts are received, or null for every rule
 * @param minSeverity the lowest severity received
 * @param tags any-of rule tags; empty means no tag filter
 */
public record PushFilters(List<String> events, List<UUID> ruleIds, String minSeverity, List<String> tags) {

    /** Every event a push target can receive. */
    public static final List<String> ALL_EVENTS = List.of("alert", "briefing", "system");

    static final Set<String> MEMBERS = Set.of("events", "ruleIds", "minSeverity", "tags");

    static final Set<String> SEVERITIES = Set.of("info", "warning", "critical");

    /** The most tags a filter may list. */
    public static final int MAX_TAGS = 50;

    /** Fills defaults: every event, every rule, {@code info}, no tag filter. */
    public PushFilters {
        events = events == null || events.isEmpty() ? ALL_EVENTS : List.copyOf(events);
        ruleIds = ruleIds == null ? null : List.copyOf(ruleIds);
        minSeverity = minSeverity == null ? "info" : minSeverity;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    /** The defaults. */
    public static PushFilters all() {
        return new PushFilters(null, null, null, null);
    }

    /**
     * True when a target with these filters receives {@code message}: its event is listed, its
     * severity is at or above {@link #minSeverity()}, and for alerts the rule passes {@link #ruleIds()};
     * when {@link #tags()} is non-empty the message must carry at least one of them.
     */
    public boolean matches(NotificationMessage message) {
        if (!events.contains(message.event())) {
            return false;
        }
        if (Severity.fromWire(message.severity()).compareTo(Severity.fromWire(minSeverity)) < 0) {
            return false;
        }
        if (NotificationMessage.ALERT.equals(message.event()) && ruleIds != null
                && (message.ruleId() == null || !ruleIds.contains(message.ruleId()))) {
            return false;
        }
        return tags.isEmpty() || message.tags().stream().anyMatch(tags::contains);
    }

    /** The stored and API form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("events", new ArrayList<>(events));
        out.put("ruleIds", ruleIds == null ? null : ruleIds.stream().map(UUID::toString).toList());
        out.put("minSeverity", minSeverity);
        out.put("tags", new ArrayList<>(tags));
        return out;
    }

    /** Reads the stored form; the defaults for null. */
    public static PushFilters fromMap(Map<String, Object> map) {
        if (map == null) {
            return all();
        }
        List<String> events = strings(map.get("events"));
        List<UUID> rules = null;
        if (map.get("ruleIds") instanceof List<?> list) {
            rules = new ArrayList<>();
            for (Object item : list) {
                rules.add(UUID.fromString(String.valueOf(item)));
            }
        }
        Object severity = map.get("minSeverity");
        return new PushFilters(events, rules, severity == null ? null : String.valueOf(severity), strings(map.get("tags")));
    }

    /**
     * Parses a {@code PushFilters} body, adding problems under {@code field}.
     *
     * @return the filters (the defaults when {@code node} is null)
     */
    public static PushFilters parse(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return all();
        }
        if (!node.isObject()) {
            errors.add(new FieldError(field, "must be an object"));
            return all();
        }
        for (String member : node.propertyNames()) {
            if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(field + "." + member, "is not a PushFilters member"));
            }
        }
        List<String> events = new ArrayList<>();
        JsonNode eventsNode = node.get("events");
        if (eventsNode != null && !eventsNode.isNull()) {
            if (!eventsNode.isArray()) {
                errors.add(new FieldError(field + ".events", "must be an array"));
            } else {
                for (JsonNode item : eventsNode) {
                    if (!item.isString() || !ALL_EVENTS.contains(item.asString())) {
                        errors.add(new FieldError(field + ".events", "supported events are alert, briefing and system"));
                    } else if (!events.contains(item.asString())) {
                        events.add(item.asString());
                    }
                }
            }
        }
        List<UUID> rules = null;
        JsonNode rulesNode = node.get("ruleIds");
        if (rulesNode != null && !rulesNode.isNull()) {
            rules = new ArrayList<>();
            if (!rulesNode.isArray()) {
                errors.add(new FieldError(field + ".ruleIds", "must be an array of rule ids or null"));
            } else {
                for (JsonNode item : rulesNode) {
                    try {
                        rules.add(UUID.fromString(item.asString()));
                    } catch (RuntimeException e) {
                        errors.add(new FieldError(field + ".ruleIds", "'" + item + "' is not a rule id"));
                    }
                }
            }
        }
        String severity = null;
        JsonNode severityNode = node.get("minSeverity");
        if (severityNode != null && !severityNode.isNull()) {
            if (!severityNode.isString() || !SEVERITIES.contains(severityNode.asString())) {
                errors.add(new FieldError(field + ".minSeverity", "must be info, warning or critical"));
            } else {
                severity = severityNode.asString();
            }
        }
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = node.get("tags");
        if (tagsNode != null && !tagsNode.isNull()) {
            if (!tagsNode.isArray() || tagsNode.size() > MAX_TAGS) {
                errors.add(new FieldError(field + ".tags", "must be an array of at most " + MAX_TAGS + " tags"));
            } else {
                for (JsonNode item : tagsNode) {
                    if (!item.isString() || item.asString().isBlank() || item.asString().length() > 100) {
                        errors.add(new FieldError(field + ".tags", "each tag must be a non-blank string of at most 100 characters"));
                    } else {
                        tags.add(item.asString());
                    }
                }
            }
        }
        return new PushFilters(events, rules, severity, tags);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            list.forEach(v -> out.add(String.valueOf(v)));
        }
        return out;
    }
}
