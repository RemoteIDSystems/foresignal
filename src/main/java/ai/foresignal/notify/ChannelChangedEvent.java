package ai.foresignal.notify;

import java.util.UUID;

/**
 * Published by {@link ChannelService} after a channel is created, replaced or deleted (prompt 060), so
 * components holding per-channel state (the {@code mqtt} channel's connection) can follow. Listeners use
 * {@code @TransactionalEventListener(fallbackExecution = true)} to see the committed row.
 *
 * @param channelId the channel
 * @param type the channel's type id at the time of the change
 * @param deleted true when the channel was deleted
 */
public record ChannelChangedEvent(UUID channelId, String type, boolean deleted) {}
