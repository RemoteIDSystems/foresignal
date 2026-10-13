package ai.foresignal.mqtt.channel;

import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.types.WebhookChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * The JSON payloads of the {@code mqtt} channel's event messages (prompt 060), as UTF-8 bytes of at most
 * {@link #MAX_BYTES}.
 *
 * <ul>
 *   <li><b>alert fired</b> ({@code {prefix}/alerts/{severity}}): {@code {event_type, id, ruleId, ruleName,
 *       severity, state, title, shortTitle, body, url, sourceId, externalId, kind, firedAt, point, tags,
 *       near?}}. {@code event_type} is the severity, which satisfies Home Assistant's {@code event}
 *       entity. {@code point} is {@code [lon, lat]} of a point geometry, else null; {@code near} appears
 *       only when the alert has a nearest place. A private-source alert that the channel may not carry
 *       arrives already reduced to the rule name, severity and link (prompt 039), so the record fields are
 *       null.
 *   <li><b>alert updates</b> ({@code {prefix}/alerts/updates}): {@code {id, state, resolvedAt,
 *       acknowledgedBy}}.
 *   <li><b>briefing run</b> ({@code {prefix}/briefings/{briefingId}}): {@code {runId, title, at, url,
 *       status, markdown}}, the markdown cut to {@link #MAX_BRIEFING_MARKDOWN} bytes with a link to the run.
 *   <li><b>system event</b> ({@code {prefix}/system/events}): the {@code NotificationMessage} JSON.
 *   <li><b>test</b> ({@code {prefix}/test}): {@code {event: "test", title, body, at}}.
 * </ul>
 *
 * <p>A channel {@code payloadTemplate} replaces every event payload except the test one; it has the
 * webhook {@code bodyTemplate} placeholders. A payload over 256 KB is shortened: the markdown or body first,
 * then, as a last resort, {@code {truncated: true, title, url}}.
 */
public final class MqttPayloads {

    /** The largest payload. */
    public static final int MAX_BYTES = 256 * 1024;

    /** The largest briefing markdown, in UTF-8 bytes. */
    public static final int MAX_BRIEFING_MARKDOWN = 16 * 1024;

    static final JsonMapper JSON = JsonMapper.builder().build();

    private MqttPayloads() {}

    /** The payload of {@code message} for its event, or its {@code template} rendering. */
    public static byte[] event(NotificationMessage message, String template) {
        if (template != null && !template.isBlank() && !NotificationMessage.TEST.equals(message.event())) {
            return templated(message, template);
        }
        Map<String, Object> payload = switch (message.event()) {
            case NotificationMessage.ALERT -> isAlertUpdate(message) ? alertUpdate(message) : alertFired(message);
            case NotificationMessage.BRIEFING -> briefing(message);
            case NotificationMessage.TEST -> test(message);
            default -> NotificationJson.toMap(message);
        };
        return fit(payload, message);
    }

    /** True when the message is an alert lifecycle update (acknowledged, snoozed, resolved). */
    public static boolean isAlertUpdate(NotificationMessage message) {
        return NotificationMessage.ALERT.equals(message.event()) && message.subtype() != null
                && message.subtype().startsWith("alert.") && !"alert.fired".equals(message.subtype())
                && !"alert.digest".equals(message.subtype());
    }

    /** The alert-fired payload. */
    public static Map<String, Object> alertFired(NotificationMessage m) {
        Map<String, Object> data = m.data() == null ? Map.of() : m.data();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event_type", m.severity());
        out.put("id", m.alertId() != null ? m.alertId() : data.get("alertId"));
        out.put("ruleId", m.ruleId() != null ? m.ruleId().toString() : data.get("ruleId"));
        out.put("ruleName", data.get("ruleName"));
        out.put("severity", m.severity());
        out.put("state", data.getOrDefault("state", "firing"));
        out.put("title", m.title());
        out.put("shortTitle", m.shortTitle());
        out.put("body", plain(m.bodyMarkdown()));
        out.put("url", m.url());
        out.put("sourceId", data.get("sourceId"));
        out.put("externalId", data.get("externalId"));
        out.put("kind", data.get("kind"));
        out.put("firedAt", data.get("firedAt"));
        out.put("point", point(data.get("record")));
        out.put("tags", m.tags());
        String near = near(data.get("region"));
        if (near != null) {
            out.put("near", near);
        }
        return out;
    }

    /** The alert-update payload. */
    public static Map<String, Object> alertUpdate(NotificationMessage m) {
        Map<String, Object> data = m.data() == null ? Map.of() : m.data();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.alertId() != null ? m.alertId() : data.get("alertId"));
        out.put("state", data.get("state"));
        out.put("resolvedAt", data.get("resolvedAt"));
        out.put("acknowledgedBy", data.get("acknowledgedBy"));
        return out;
    }

    /** The briefing payload, markdown cut to {@link #MAX_BRIEFING_MARKDOWN} bytes. */
    public static Map<String, Object> briefing(NotificationMessage m) {
        Map<String, Object> data = m.data() == null ? Map.of() : m.data();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("runId", m.briefingRunId() != null ? m.briefingRunId().toString() : data.get("id"));
        out.put("title", m.title());
        Object at = data.get("finishedAt") != null ? data.get("finishedAt") : data.get("startedAt");
        out.put("at", at != null ? at : (m.createdAt() == null ? null : m.createdAt().toString()));
        out.put("url", m.url());
        out.put("status", data.get("status"));
        Object markdown = data.get("contentMarkdown");
        out.put("markdown", cutMarkdown(markdown instanceof String s ? s : m.bodyMarkdown(), MAX_BRIEFING_MARKDOWN,
                m.url()));
        return out;
    }

    /** The test payload. */
    public static Map<String, Object> test(NotificationMessage m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", "test");
        out.put("title", m.title());
        out.put("body", plain(m.bodyMarkdown()));
        out.put("at", m.createdAt() == null ? null : m.createdAt().toString());
        return out;
    }

    /** The briefing id a briefing message belongs to ({@code data.briefingId}), or null. */
    public static String briefingId(NotificationMessage m) {
        Object id = m.data() == null ? null : m.data().get("briefingId");
        return id == null ? null : String.valueOf(id);
    }

    /** True when {@code template}, rendered with a probe message, is valid JSON. */
    public static boolean templateRendersJson(String template) {
        try {
            JSON.readTree(WebhookChannel.renderTemplate(template,
                    NotificationMessage.builder("test", "test", "t", "info").build()));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * {@code markdown} cut to at most {@code maxBytes} UTF-8 bytes on a character boundary, followed by a
     * "Read more" link when cut; null stays null.
     */
    public static String cutMarkdown(String markdown, int maxBytes, String url) {
        if (markdown == null || markdown.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return markdown;
        }
        String suffix = "\n\n… [Read more](" + (url == null ? "" : url) + ")";
        int budget = Math.max(0, maxBytes - suffix.getBytes(StandardCharsets.UTF_8).length);
        return prefixBytes(markdown, budget) + suffix;
    }

    /** {@code payload} as JSON bytes of at most {@link #MAX_BYTES}, its long texts shortened first. */
    static byte[] fit(Map<String, Object> payload, NotificationMessage m) {
        byte[] bytes = JSON.writeValueAsBytes(payload);
        if (bytes.length <= MAX_BYTES) {
            return bytes;
        }
        Map<String, Object> copy = new LinkedHashMap<>(payload);
        for (String key : List.of("markdown", "body", "bodyMarkdown")) {
            if (copy.get(key) instanceof String text) {
                int over = bytes.length - MAX_BYTES;
                int keep = Math.max(0, text.getBytes(StandardCharsets.UTF_8).length - over - 256);
                copy.put(key, cutMarkdown(text, keep, m.url()));
                bytes = JSON.writeValueAsBytes(copy);
                if (bytes.length <= MAX_BYTES) {
                    return bytes;
                }
            }
        }
        Map<String, Object> minimal = new LinkedHashMap<>();
        minimal.put("truncated", true);
        minimal.put("title", m.title());
        minimal.put("url", m.url());
        return JSON.writeValueAsBytes(minimal);
    }

    private static byte[] templated(NotificationMessage message, String template) {
        byte[] bytes = WebhookChannel.renderTemplate(template, message).getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_BYTES) {
            return bytes;
        }
        String body = message.bodyMarkdown() == null ? "" : message.bodyMarkdown();
        int keep = Math.max(0, body.getBytes(StandardCharsets.UTF_8).length - (bytes.length - MAX_BYTES) - 256);
        bytes = WebhookChannel.renderTemplate(template, message.withText(message.title(),
                cutMarkdown(body, keep, message.url()))).getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_BYTES) {
            return bytes;
        }
        Map<String, Object> minimal = new LinkedHashMap<>();
        minimal.put("truncated", true);
        minimal.put("title", message.title());
        minimal.put("url", message.url());
        return JSON.writeValueAsBytes(minimal);
    }

    private static String plain(String markdown) {
        return markdown == null ? null : MarkdownRenderer.render(markdown, MarkdownRenderer.Format.PLAIN);
    }

    /** {@code [lon, lat]} of the snapshot's point geometry, else null. */
    static List<Object> point(Object record) {
        if (record instanceof Map<?, ?> r && r.get("geometry") instanceof Map<?, ?> g && "Point".equals(g.get("type"))
                && g.get("coordinates") instanceof List<?> c && c.size() >= 2) {
            return List.of(c.get(0), c.get(1));
        }
        return null;
    }

    /** The region's {@code near.text}, or null. */
    static String near(Object region) {
        if (region instanceof Map<?, ?> r && r.get("near") instanceof Map<?, ?> n && n.get("text") != null) {
            return String.valueOf(n.get("text"));
        }
        return null;
    }

    private static String prefixBytes(String text, int maxBytes) {
        StringBuilder out = new StringBuilder();
        int used = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (used + size > maxBytes) {
                break;
            }
            out.appendCodePoint(cp);
            used += size;
            i += Character.charCount(cp);
        }
        return out.toString();
    }
}
