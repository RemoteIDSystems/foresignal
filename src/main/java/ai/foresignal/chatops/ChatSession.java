package ai.foresignal.chatops;

import java.util.Objects;

/**
 * One running interactive channel as the router sees it (prompt 061): its configuration and the connector
 * that replies.
 *
 * @param config the channel
 * @param connector its connector
 */
public record ChatSession(ChannelConfig config, ChatConnector connector) {

    /** Requires both members. */
    public ChatSession {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(connector, "connector");
    }
}
