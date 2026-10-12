package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.store.NotificationChannelEntity;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Handles updates that {@link TelegramCallbackPoller} receives for one Telegram channel (prompt 033).
 * Declare an implementation as a bean: 033's {@link ActionCallbackHandler} executes action buttons;
 * prompt 061 adds bot commands the same way. The poller asks every handler which update types it
 * needs ({@code allowed_updates}) and whether it wants a channel polled at all.
 */
public interface TelegramUpdateHandler {

    /** The Bot API update types this handler needs, such as {@code callback_query} or {@code message}. */
    Set<String> updateTypes();

    /** True when this handler needs {@code channel}'s updates (for example, its actions are enabled). */
    boolean wants(NotificationChannelEntity channel);

    /**
     * Handles one update; returns true when it consumed it (later handlers are skipped).
     *
     * @param channel the channel
     * @param settings its decrypted settings
     * @param update the Bot API {@code Update} object
     */
    boolean handle(NotificationChannelEntity channel, ChannelSettings settings, JsonNode update);
}
