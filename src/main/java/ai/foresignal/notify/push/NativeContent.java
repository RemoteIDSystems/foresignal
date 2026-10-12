package ai.foresignal.notify.push;

import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * What a native push carries beyond ids (prompt 032), shared by APNs and FCM.
 *
 * <p>Full titles and bodies never leave the server through Apple or Google. By the device's
 * {@code contentLevel}:
 * <ul>
 *   <li>{@code minimal} (default): title "Foresignal", body "&lt;Severity&gt; alert", "Briefing
 *       ready" or "System notice";
 *   <li>{@code short} (operator opt-in per device): title "Foresignal · &lt;severity&gt;", body the
 *       alert's {@code shortTitle} (at most 40 characters), else the minimal body.
 * </ul>
 * The app's Notification Service Extension (iOS) or message handler (Android) replaces this text
 * with the details it fetches from Foresignal over the user's own network path.
 */
public final class NativeContent {

    /** The title of a minimal push. */
    public static final String TITLE = "Foresignal";

    /** The longest short title. */
    public static final int SHORT_TITLE = 40;

    /** The longest APNs collapse id, in bytes. */
    public static final int MAX_COLLAPSE_ID = 64;

    private NativeContent() {}

    /** The visible title for a device of {@code contentLevel}. */
    public static String title(PushMessage message, String contentLevel) {
        if (NativeDeviceTarget.SHORT.equals(contentLevel)) {
            return TITLE + " · " + severity(message);
        }
        return TITLE;
    }

    /** The visible body for a device of {@code contentLevel}. */
    public static String body(PushMessage message, String contentLevel) {
        if (NativeDeviceTarget.SHORT.equals(contentLevel) && message.shortTitle() != null
                && !message.shortTitle().isBlank()) {
            return Truncator.truncate(message.shortTitle().strip(), SHORT_TITLE, Truncator.Unit.CHARS,
                    Truncator.Mode.PLAIN, null);
        }
        return minimalBody(message);
    }

    /** The minimal body: "&lt;Severity&gt; alert", "Briefing ready" or "System notice". */
    public static String minimalBody(PushMessage message) {
        return switch (message.event()) {
            case "alert" -> capitalize(severity(message)) + " alert";
            case "briefing" -> "Briefing ready";
            default -> "System notice";
        };
    }

    /** The severity, {@code info} when absent. */
    public static String severity(PushMessage message) {
        return message.severity() == null ? "info" : message.severity();
    }

    /**
     * The collapse id (APNs {@code apns-collapse-id}, FCM {@code collapse_key}): {@code a-} plus the
     * 031 {@code Topic} (the first 32 base64url characters of the entity key's SHA-256), so repeats
     * about one entity replace each other. At most {@value #MAX_COLLAPSE_ID} bytes.
     */
    public static String collapseId(PushMessage message) {
        String topic = message.topic();
        if (topic == null || topic.isBlank()) {
            topic = PushTopic.derive(message.tag() != null ? message.tag() : message.event() + ":" + message.id());
        }
        String id = "a-" + topic;
        while (id.getBytes(StandardCharsets.UTF_8).length > MAX_COLLAPSE_ID) {
            id = id.substring(0, id.length() - 1);
        }
        return id;
    }

    /** The APNs {@code thread-id}: {@code foresignal-<ruleId>}, else {@code foresignal-<event>}. */
    public static String threadId(PushMessage message) {
        return "foresignal-" + (message.ruleId() != null ? message.ruleId().toString() : message.event());
    }

    /** The category: the message's, else {@code FORESIGNAL_<EVENT>}. */
    public static String category(PushMessage message) {
        return message.category() != null ? message.category() : PushMessage.categoryOf(message.event());
    }

    /** The deep link: the 023 {@code clientLinks} app link, else the web link, else null. */
    public static String link(PushMessage message) {
        return message.linkApp() != null ? message.linkApp() : message.linkWeb();
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
