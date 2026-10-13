package ai.foresignal.chatops;

import java.util.Objects;

/**
 * One reply button (prompt 061): Telegram renders it as an inline-keyboard button whose
 * {@code callback_data} is the opaque {@code token} (at most 64 bytes).
 *
 * @param label the text on the button
 * @param token the {@code chatops_pending} token the press resolves
 */
public record ChatButton(String label, String token) {

    /** Requires both members. */
    public ChatButton {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(token, "token");
    }
}
