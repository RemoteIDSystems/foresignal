package ai.foresignal.chatops;

import java.util.Objects;
import java.util.UUID;

/**
 * Where a command came from and where its reply goes (prompt 061).
 *
 * @param channelId the interactive channel
 * @param chatId the Telegram chat id or the Matrix room id
 * @param userId the sender (Telegram user id, Matrix MXID), or null when the platform gives none
 * @param userName the sender's display name for replies, or null
 * @param messageId the message replied to (Telegram {@code message_id}, Matrix {@code event_id}), or null
 */
public record ChatRef(UUID channelId, String chatId, String userId, String userName, String messageId) {

    /** Requires the channel and chat. */
    public ChatRef {
        Objects.requireNonNull(channelId, "channelId");
        Objects.requireNonNull(chatId, "chatId");
    }

    /** The same chat and sender, replying to nothing in particular (a follow-up). */
    public ChatRef withoutMessage() {
        return new ChatRef(channelId, chatId, userId, userName, null);
    }
}
