package ai.foresignal.source.adapters.meteoalarm;

import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Converts a MeteoAlarm JSON alert (the {@code alert} member of a {@code warnings[]} entry) to prompt 067's
 * {@link CapAlert} model, so the MeteoAlarm family reuses the CAP parser's threads and record mapping
 * (prompt 089).
 *
 * <p>The JSON is CAP 1.2 as JSON: single-valued elements are strings, repeatable ones arrays; a missing
 * repeatable element becomes an empty list. An unreadable time is null, except {@code sent}, which is
 * required.
 */
public final class MeteoAlarmJsonAdapter {

    private MeteoAlarmJsonAdapter() {}

    /**
     * The CAP model of one MeteoAlarm JSON alert.
     *
     * @throws UpstreamFormatException when the node is not an object or {@code sent} is missing
     */
    public static CapAlert convert(JsonNode alert) {
        if (alert == null || !alert.isObject()) {
            throw new UpstreamFormatException("A MeteoAlarm warning has no alert object", "");
        }
        OffsetDateTime sent = time(alert, "sent");
        if (sent == null) {
            throw new UpstreamFormatException("A MeteoAlarm alert has no sent time",
                    text(alert, "identifier"));
        }
        List<CapAlert.Info> infos = new ArrayList<>();
        JsonNode info = alert.get("info");
        if (info != null && info.isArray()) {
            for (JsonNode one : info) {
                infos.add(info(one));
            }
        } else if (info != null && info.isObject()) {
            infos.add(info(info));
        }
        return new CapAlert(
                "1.2",
                text(alert, "identifier"),
                text(alert, "sender"),
                sent,
                text(alert, "status"),
                text(alert, "msgType"),
                text(alert, "source"),
                text(alert, "scope"),
                text(alert, "restriction"),
                text(alert, "addresses"),
                texts(alert.get("code")),
                text(alert, "note"),
                references(alert.get("references")),
                text(alert, "incidents"),
                infos);
    }

    private static CapAlert.Info info(JsonNode node) {
        return new CapAlert.Info(
                orDefault(text(node, "language"), "en-GB"),
                texts(node.get("category")),
                text(node, "event"),
                texts(node.get("responseType")),
                text(node, "urgency"),
                text(node, "severity"),
                text(node, "certainty"),
                text(node, "audience"),
                namedValues(node.get("eventCode")),
                time(node, "effective"),
                time(node, "onset"),
                time(node, "expires"),
                text(node, "senderName"),
                text(node, "headline"),
                text(node, "description"),
                text(node, "instruction"),
                text(node, "web"),
                text(node, "contact"),
                namedValues(node.get("parameter")),
                resources(node.get("resource")),
                areas(node.get("area")));
    }

    private static List<CapAlert.Area> areas(JsonNode node) {
        List<CapAlert.Area> out = new ArrayList<>();
        if (node == null) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode one : node) {
                out.add(area(one));
            }
        } else if (node.isObject()) {
            out.add(area(node));
        }
        return out;
    }

    private static CapAlert.Area area(JsonNode node) {
        return new CapAlert.Area(
                text(node, "areaDesc"),
                texts(node.get("polygon")),
                texts(node.get("circle")),
                namedValues(node.get("geocode")),
                number(node, "altitude"),
                number(node, "ceiling"));
    }

    private static List<CapAlert.Resource> resources(JsonNode node) {
        List<CapAlert.Resource> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode one : node) {
                out.add(new CapAlert.Resource(text(one, "resourceDesc"), text(one, "mimeType"),
                        longValue(one, "size"), text(one, "uri"), text(one, "digest")));
            }
        }
        return out;
    }

    private static List<CapAlert.Reference> references(JsonNode node) {
        List<CapAlert.Reference> out = new ArrayList<>();
        if (node == null) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode one : node) {
                if (one.isString()) {
                    String[] parts = one.asString().split(",", 3);
                    out.add(new CapAlert.Reference(part(parts, 0), part(parts, 1), parseTime(part(parts, 2))));
                } else if (one.isObject()) {
                    out.add(new CapAlert.Reference(text(one, "sender"), text(one, "identifier"),
                            time(one, "sent")));
                }
            }
        } else if (node.isString()) {
            String[] parts = node.asString().split(",", 3);
            out.add(new CapAlert.Reference(part(parts, 0), part(parts, 1), parseTime(part(parts, 2))));
        }
        return out;
    }

    private static List<CapAlert.NamedValue> namedValues(JsonNode node) {
        List<CapAlert.NamedValue> out = new ArrayList<>();
        if (node == null) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode one : node) {
                if (one.isObject()) {
                    out.add(new CapAlert.NamedValue(text(one, "valueName"), text(one, "value")));
                }
            }
        } else if (node.isObject()) {
            out.add(new CapAlert.NamedValue(text(node, "valueName"), text(node, "value")));
        }
        return out;
    }

    private static List<String> texts(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode one : node) {
                if (one.isString()) {
                    out.add(one.asString());
                }
            }
        } else if (node.isString()) {
            out.add(node.asString());
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static String part(String[] parts, int index) {
        return index < parts.length ? parts[index] : null;
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        try {
            return Double.parseDouble(value.asString().strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asLong();
        }
        try {
            return Long.parseLong(value.asString().strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static OffsetDateTime time(JsonNode node, String field) {
        return parseTime(text(node, field));
    }

    private static OffsetDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
