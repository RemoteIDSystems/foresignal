package ai.foresignal.chatops;

import java.time.Instant;

/**
 * Receives what a {@link ChatConnector} reads from its chat platform (prompt 061). The connector has
 * already dropped its own messages, edits and stale messages, and normalized {@code /cmd@BotName}.
 */
public interface CommandSink {

    /**
     * A text message.
     *
     * @param ref where it came from
     * @param text the message text, exactly as sent
     * @param sentAt when the platform says it was sent
     */
    void onMessage(ChatRef ref, String text, Instant sentAt);

    /**
     * A button press (Telegram {@code callback_query}) whose data is a chat-command token.
     *
     * @param ref where the button was pressed, and by whom
     * @param callbackRef the platform's id of the press, for {@link ChatConnector#answerCallback}
     * @param token the button's token
     */
    void onCallback(ChatRef ref, String callbackRef, String token);
}
