package ai.foresignal.mqtt.channel;

import java.util.UUID;

/**
 * The topic tree of an {@code mqtt} channel (prompt 060), and the sanitizing of segments built from ids or
 * names: {@code +}, {@code #}, {@code /}, NUL and control characters become {@code _}.
 *
 * <pre>
 * {prefix}/status                      online | offline (retained; Last Will)
 * {prefix}/alerts/{severity}           alert fired (not retained, message expiry)
 * {prefix}/alerts/updates              alert acknowledged, snoozed or resolved
 * {prefix}/briefings/{briefingId}      latest briefing run (retained)
 * {prefix}/system/events               system events
 * {prefix}/rules/{ruleId}/state        rule state (retained)
 * {prefix}/sources/{sourceId}/health   source health (retained)
 * {prefix}/summary                     the glance (retained)
 * {prefix}/test                        channel test
 * {haPrefix}/device/foresignal_{installId}/config   Home Assistant discovery
 * </pre>
 */
public final class MqttTopics {

    private MqttTopics() {}

    /** {@code segment} with {@code + # /}, NUL and control characters replaced by {@code _}; never empty. */
    public static String segment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return "_";
        }
        StringBuilder out = new StringBuilder(segment.length());
        segment.codePoints().forEach(cp -> {
            if (cp == '+' || cp == '#' || cp == '/' || cp == 0 || Character.isISOControl(cp)) {
                out.append('_');
            } else {
                out.appendCodePoint(cp);
            }
        });
        return out.toString();
    }

    /** {@code {prefix}/status}. */
    public static String status(String prefix) {
        return prefix + "/status";
    }

    /** {@code {prefix}/alerts/{severity}}. */
    public static String alerts(String prefix, String severity) {
        return prefix + "/alerts/" + segment(severity);
    }

    /** {@code {prefix}/alerts/updates}. */
    public static String alertUpdates(String prefix) {
        return prefix + "/alerts/updates";
    }

    /** {@code {prefix}/briefings/{briefingId}}. */
    public static String briefing(String prefix, String briefingId) {
        return prefix + "/briefings/" + segment(briefingId);
    }

    /** {@code {prefix}/system/events}. */
    public static String systemEvents(String prefix) {
        return prefix + "/system/events";
    }

    /** {@code {prefix}/rules/{ruleId}/state}. */
    public static String ruleState(String prefix, UUID ruleId) {
        return prefix + "/rules/" + segment(ruleId.toString()) + "/state";
    }

    /** {@code {prefix}/sources/{sourceId}/health}. */
    public static String sourceHealth(String prefix, String sourceId) {
        return prefix + "/sources/" + segment(sourceId) + "/health";
    }

    /** {@code {prefix}/summary}. */
    public static String summary(String prefix) {
        return prefix + "/summary";
    }

    /** {@code {prefix}/test}. */
    public static String test(String prefix) {
        return prefix + "/test";
    }

    /** {@code {haPrefix}/status}, where Home Assistant publishes its birth message. */
    public static String haStatus(String haPrefix) {
        return haPrefix + "/status";
    }

    /** {@code {haPrefix}/device/foresignal_{installId}/config}. */
    public static String haDevice(String haPrefix, String installId) {
        return haPrefix + "/device/foresignal_" + segment(installId) + "/config";
    }
}
