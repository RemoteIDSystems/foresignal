package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelCapabilities;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared helpers of the built-in channel types: JSON bodies, the common headers (User-Agent and the
 * {@link OriginMarker} header), severity colors and the display time.
 */
abstract class AbstractChannelType implements ChannelType {

    /** The mapper for rendered bodies (insertion-ordered maps keep payloads stable). */
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The display pattern of times in messages. */
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzz", Locale.US);

    @Override
    public ChannelCapabilities capabilities() {
        return ChannelCapabilities.BASIC;
    }

    /** User-Agent and the origin marker. */
    static Map<String, String> baseHeaders(ChannelSettings s) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (s.userAgent() != null) {
            headers.put("User-Agent", s.userAgent());
        }
        if (s.origin() != null) {
            headers.put(OriginMarker.HEADER, s.origin());
        }
        return headers;
    }

    /** A JSON request with the base headers plus {@code extra}. */
    static PreparedRequest json(String method, String url, Map<String, String> extra, Object body, ChannelSettings s) {
        Map<String, String> headers = baseHeaders(s);
        headers.put("Content-Type", "application/json");
        if (extra != null) {
            headers.putAll(extra);
        }
        return new PreparedRequest(method, url, headers,
                PreparedRequest.Bytes.text("application/json", JSON.writeValueAsString(body)));
    }

    /** A form request with the base headers. */
    static PreparedRequest form(String url, Map<String, String> fields, Map<String, String> extra, ChannelSettings s) {
        Map<String, String> headers = baseHeaders(s);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        if (extra != null) {
            headers.putAll(extra);
        }
        return new PreparedRequest("POST", url, headers, new PreparedRequest.Form(fields));
    }

    /** An insertion-ordered map of the given key/value pairs, skipping null values. */
    static Map<String, Object> obj(Object... keyValues) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (keyValues[i + 1] != null) {
                out.put((String) keyValues[i], keyValues[i + 1]);
            }
        }
        return out;
    }

    /** The severity color as RGB: info blue, warning amber, critical red. */
    static int color(String severity) {
        return switch (severity) {
            case "critical" -> 0xDC2626;
            case "warning" -> 0xF59E0B;
            default -> 0x3B82F6;
        };
    }

    /** The severity color as {@code #RRGGBB}. */
    static String hexColor(String severity) {
        return String.format(Locale.ROOT, "#%06X", color(severity));
    }

    /** The message time (its creation, else now) formatted in the channel's zone. */
    static String displayTime(NotificationMessage m, ChannelSettings s) {
        Instant at = m.createdAt() != null ? m.createdAt() : s.now();
        ZoneId zone = s.zone() == null ? ZoneId.of("UTC") : s.zone();
        return TIME.format(at.atZone(zone));
    }

    /** The message's link buttons, with {@code url} first when no link points at it. */
    static List<NotificationMessage.Link> buttons(NotificationMessage m) {
        List<NotificationMessage.Link> out = new ArrayList<>();
        if (m.url() != null && m.links().stream().noneMatch(l -> l.url().equals(m.url()))) {
            out.add(new NotificationMessage.Link("Open", m.url()));
        }
        out.addAll(m.links());
        return out;
    }

    /** Parses a JSON response body, if it is JSON. */
    static Optional<JsonNode> parse(ChannelResponse response) {
        try {
            String body = response.body();
            return body == null || body.isBlank() ? Optional.empty() : Optional.of(JSON.readTree(body));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** A text member at {@code path} of a JSON response, if present. */
    static Optional<String> refAt(ChannelResponse response, String... path) {
        return parse(response).map(node -> {
            JsonNode current = node;
            for (String step : path) {
                current = current == null ? null : current.get(step);
            }
            if (current == null || current.isNull() || current.isContainer()) {
                return null;
            }
            String text = current.asString();
            return text == null || text.isBlank() ? null : text;
        });
    }

    /** {@code url} with {@code param=value} appended to its query. */
    static String withQuery(String url, String param) {
        return url + (url.contains("?") ? "&" : "?") + param;
    }
}
