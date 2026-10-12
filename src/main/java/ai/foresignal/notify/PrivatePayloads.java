package ai.foresignal.notify;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The minimal form of a notification about a private-source record (prompt 039, §2 "Alerts"): only the
 * rule name, the severity and a link leave Foresignal, unless the channel opted in with
 * {@code config.privatePayload: true} (an admin setting per channel). Fan-out push types (Web Push,
 * APNs, FCM) never carry the payload, whatever the channel says.
 */
public final class PrivatePayloads {

    /** The channel config member that opts a channel in to private payloads. */
    public static final String CONFIG_KEY = "privatePayload";

    private PrivatePayloads() {}

    /** The source id a message is about ({@code data.sourceId}), or null. */
    public static String sourceId(NotificationMessage message) {
        Object value = message.data() == null ? null : message.data().get("sourceId");
        return value instanceof String s ? s : null;
    }

    /** True when {@code config} has {@code privatePayload: true}. */
    public static boolean optedIn(Map<String, Object> config) {
        return config != null && Boolean.TRUE.equals(config.get(CONFIG_KEY));
    }

    /**
     * {@code message} reduced to the rule name, severity and link: the title names the rule, the body is
     * the link, and {@code data} keeps only identifiers and {@code private: true}. The record snapshot,
     * message, summary and citations are dropped.
     */
    public static NotificationMessage redact(NotificationMessage message) {
        Map<String, Object> data = message.data() == null ? Map.of() : message.data();
        Object rule = data.get("ruleName");
        String ruleName = rule instanceof String s && !s.isBlank() ? s : "Alert";
        Map<String, Object> kept = new LinkedHashMap<>();
        for (String key : new String[] {"alertId", "ruleId", "ruleName", "severity", "state"}) {
            if (data.containsKey(key)) {
                kept.put(key, data.get(key));
            }
        }
        kept.put("private", true);
        String title = ruleName + " (" + message.severity() + ")";
        String body = message.url() == null ? "A private-source alert. Open Foresignal for details."
                : "A private-source alert. Details: " + message.url();
        return NotificationMessage.builder(message.event(), message.subtype(), title, message.severity())
                .id(message.id())
                .shortTitle(ruleName)
                .body(body)
                .url(message.url())
                .links(message.links())
                .data(kept)
                .tags(message.tags())
                .updateOf(message.updateOf())
                .origin(message.origin())
                .createdAt(message.createdAt())
                .alertId(message.alertId())
                .ruleId(message.ruleId())
                .briefingRunId(message.briefingRunId())
                .build();
    }
}
