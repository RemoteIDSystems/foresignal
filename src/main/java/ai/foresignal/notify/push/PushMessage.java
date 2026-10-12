package ai.foresignal.notify.push;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The transport-neutral content of one push (prompt 031), built once per notification by
 * {@link PushContentFactory} and stored with each {@code push_dispatch} row so a retry re-sends
 * exactly what the first attempt sent. Each transport renders it in its own format and language
 * ({@link PushPayloads} for Web Push).
 *
 * @param event {@code alert}, {@code briefing}, {@code system} or {@code verify}
 * @param id the persisted notification id (018); the alert id when no notification row exists
 * @param alertId the alert, or null
 * @param ruleId the rule, or null
 * @param severity {@code info}, {@code warning} or {@code critical}
 * @param title the title
 * @param shortTitle at most 40 characters
 * @param body plain text, at most 1000 characters
 * @param linkApi the API link, or null
 * @param linkApp the app deep link ({@code foresignal://…}), or null
 * @param linkWeb the https web link, or null
 * @param tags the rule tags
 * @param tag the OS notification tag, so repeats for one entity replace each other
 * @param topic the RFC 8030 {@code Topic}, or null
 * @param silent whether the notification should be silent
 * @param ts the creation time, epoch milliseconds
 * @param subscriptionId the subscription being verified ({@code verify} only)
 * @param code the verification code ({@code verify} only)
 * @param badge the app badge (unread count), or null; filled by prompt 033
 * @param category the native notification category ({@code FORESIGNAL_<EVENT>}), or null (032)
 */
public record PushMessage(
        String event,
        Long id,
        Long alertId,
        UUID ruleId,
        String severity,
        String title,
        String shortTitle,
        String body,
        String linkApi,
        String linkApp,
        String linkWeb,
        List<String> tags,
        String tag,
        String topic,
        boolean silent,
        long ts,
        UUID subscriptionId,
        String code,
        Integer badge,
        String category) {

    /** The verification event. */
    public static final String VERIFY = "verify";

    /** Requires the event; copies the tags. */
    public PushMessage {
        Objects.requireNonNull(event, "event");
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    /** A message without a badge or category (the 031 form). */
    public PushMessage(String event, Long id, Long alertId, UUID ruleId, String severity, String title,
            String shortTitle, String body, String linkApi, String linkApp, String linkWeb, List<String> tags,
            String tag, String topic, boolean silent, long ts, UUID subscriptionId, String code) {
        this(event, id, alertId, ruleId, severity, title, shortTitle, body, linkApi, linkApp, linkWeb, tags, tag, topic,
                silent, ts, subscriptionId, code, null, null);
    }

    /** The native category of an event: {@code FORESIGNAL_ALERT}, {@code FORESIGNAL_BRIEFING}, … */
    public static String categoryOf(String event) {
        return "FORESIGNAL_" + (event == null ? "SYSTEM" : event.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * The verification push of a new subscription.
     *
     * @param baseUrl Foresignal's public base URL, the {@code navigate} target of a declarative
     *     verification notification
     */
    public static PushMessage verify(UUID subscriptionId, String code, long ts, String baseUrl) {
        return new PushMessage(VERIFY, null, null, null, "info", "Confirm Foresignal notifications", null,
                "Verification code: " + code, baseUrl, null, null, List.of(), "foresignal-verify-" + subscriptionId, null,
                false, ts, subscriptionId, code);
    }

    /** The stored form ({@code push_dispatch.content}). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put("id", id);
        out.put("alertId", alertId);
        out.put("ruleId", ruleId == null ? null : ruleId.toString());
        out.put("severity", severity);
        out.put("title", title);
        out.put("shortTitle", shortTitle);
        out.put("body", body);
        out.put("linkApi", linkApi);
        out.put("linkApp", linkApp);
        out.put("linkWeb", linkWeb);
        out.put("tags", new ArrayList<>(tags));
        out.put("tag", tag);
        out.put("topic", topic);
        out.put("silent", silent);
        out.put("ts", ts);
        out.put("badge", badge);
        out.put("category", category);
        return out;
    }

    /** Reads the stored form. */
    public static PushMessage fromMap(Map<String, Object> map) {
        List<String> tags = new ArrayList<>();
        if (map.get("tags") instanceof List<?> list) {
            list.forEach(t -> tags.add(String.valueOf(t)));
        }
        Object rule = map.get("ruleId");
        return new PushMessage(String.valueOf(map.get("event")), number(map.get("id")), number(map.get("alertId")),
                rule == null ? null : UUID.fromString(String.valueOf(rule)), text(map.get("severity")),
                text(map.get("title")), text(map.get("shortTitle")), text(map.get("body")), text(map.get("linkApi")),
                text(map.get("linkApp")), text(map.get("linkWeb")), tags, text(map.get("tag")), text(map.get("topic")),
                Boolean.TRUE.equals(map.get("silent")), number(map.get("ts")) == null ? 0 : number(map.get("ts")), null,
                null, number(map.get("badge")) == null ? null : number(map.get("badge")).intValue(),
                text(map.get("category")));
    }

    private static Long number(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
