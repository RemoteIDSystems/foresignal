package ai.foresignal.chatops;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.action.TelegramUpdateHandler;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Creates a fresh {@link ChatConnector} for an interactive channel of a given type (prompt 061). */
@Component
public class ChatConnectorFactory {

    private final ChannelSender sender;
    private final ChatopsCursorStore cursors;
    private final ChannelRateLimiter limiter;
    private final JsonMapper json;
    private final Clock clock;
    private final ChatopsProperties properties;
    private final ObjectProvider<TelegramUpdateHandler> handlers;
    private final NotificationChannelRepository channels;

    /** Creates the factory. */
    public ChatConnectorFactory(ChannelSender sender, ChatopsCursorStore cursors, ChannelRateLimiter limiter,
            JsonMapper json, Clock clock, ChatopsProperties properties, ObjectProvider<TelegramUpdateHandler> handlers,
            NotificationChannelRepository channels) {
        this.sender = sender;
        this.cursors = cursors;
        this.limiter = limiter;
        this.json = json;
        this.clock = clock;
        this.properties = properties;
        this.handlers = handlers;
        this.channels = channels;
    }

    /**
     * A connector for {@code type}.
     *
     * @throws IllegalArgumentException for a type that cannot be interactive
     */
    public ChatConnector create(String type) {
        if (TelegramChannel.ID.equals(type)) {
            List<TelegramUpdateHandler> list = handlers.orderedStream().toList();
            return new TelegramConnector(sender, cursors, limiter, json, clock, properties, list, channels);
        }
        if (MatrixChannel.ID.equals(type)) {
            return new MatrixConnector(sender, cursors, limiter, json, clock, properties);
        }
        throw new IllegalArgumentException("Channel type '" + type + "' cannot be interactive");
    }
}
