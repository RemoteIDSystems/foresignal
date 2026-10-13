package ai.foresignal.chatops;

import ai.foresignal.notify.ChannelSettings;
import java.util.Objects;
import java.util.UUID;

/**
 * What a {@link ChatConnector} is started with (prompt 061): the stored channel's identity, its decrypted
 * settings and its interactive members.
 *
 * @param channelId the channel
 * @param channelName the channel's name
 * @param type {@code telegram} or {@code matrix}
 * @param settings the decrypted settings (secrets included; never logged)
 * @param interactive the interactive members of its config
 */
public record ChannelConfig(UUID channelId, String channelName, String type, ChannelSettings settings,
        InteractiveConfig interactive) {

    /** Requires every member. */
    public ChannelConfig {
        Objects.requireNonNull(channelId, "channelId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(interactive, "interactive");
    }
}
