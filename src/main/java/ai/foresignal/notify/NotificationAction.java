package ai.foresignal.notify;

import java.time.Instant;
import java.util.Objects;

/**
 * One action button attached to a notification (prompt 033): a single-use capability URL minted for
 * one delivery (or one calling device). {@code POST url} acts; {@code GET url} only shows a confirm
 * page, so a link preview, a mail scanner or a stray click never acts.
 *
 * @param action {@code ack}, {@code snooze} or {@code mute}
 * @param label the button text ("Acknowledge", "Snooze 1h", "Mute 4h")
 * @param url {@code <publicBaseUrl>/v1/actions/eac_…}
 * @param token the capability token ({@code eac_…}), used as Telegram {@code callback_data}
 * @param duration the snooze or mute duration in shorthand ({@code 1h}), or null for ack
 * @param expiresAt when the capability expires
 */
public record NotificationAction(String action, String label, String url, String token, String duration,
        Instant expiresAt) {

    /** Acknowledge. */
    public static final String ACK = "ack";

    /** Snooze the alert. */
    public static final String SNOOZE = "snooze";

    /** Mute the alert's rule. */
    public static final String MUTE = "mute";

    /** Requires the action, label, url and token. */
    public NotificationAction {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(token, "token");
    }

    @Override
    public String toString() {
        // The URL and token are credentials: never print them.
        return "NotificationAction[" + action + "]";
    }
}
