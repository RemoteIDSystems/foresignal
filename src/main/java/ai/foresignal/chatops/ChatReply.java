package ai.foresignal.chatops;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A reply to a chat (prompt 061). The text is Foresignal markdown ({@code MarkdownRenderer}), which each
 * connector renders with the 018 formatter of its channel type (Telegram HTML, Matrix HTML).
 *
 * @param markdown the text; summaries and links only, never raw payloads or secrets
 * @param buttons rows of buttons; connectors without buttons ignore them
 * @param replyTo the message replied to, or null
 */
public record ChatReply(String markdown, List<List<ChatButton>> buttons, String replyTo) {

    /** Requires the text; copies the buttons and drops empty rows. */
    public ChatReply {
        Objects.requireNonNull(markdown, "markdown");
        List<List<ChatButton>> rows = new ArrayList<>();
        if (buttons != null) {
            for (List<ChatButton> row : buttons) {
                if (row != null && !row.isEmpty()) {
                    rows.add(List.copyOf(row));
                }
            }
        }
        buttons = List.copyOf(rows);
    }

    /** A text reply to {@code replyTo}. */
    public static ChatReply text(String markdown, String replyTo) {
        return new ChatReply(markdown, List.of(), replyTo);
    }

    /** True when the reply carries buttons. */
    public boolean hasButtons() {
        return !buttons.isEmpty();
    }
}
