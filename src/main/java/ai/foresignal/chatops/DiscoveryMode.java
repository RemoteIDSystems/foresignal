package ai.foresignal.chatops;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The {@code discoveryMode} window of an interactive channel (prompt 061): while it is open, {@code /whoami}
 * from any chat answers with only that chat's id and the sender's id, so the operator can copy them into
 * {@code allowedChats}.
 *
 * <p>The window opens when a channel with {@code discoveryMode: true} is first seen, or saved again, and lasts
 * {@link #WINDOW}. It is kept in {@code app_setting['chatops.<channelId>.discovery']}, so a restart does not
 * reopen it. When it has passed, discovery turns itself off: the channel's stored {@code discoveryMode} is set
 * to false and the window is marked expired.
 */
@Component
public class DiscoveryMode {

    /** How long discovery mode stays on. */
    public static final Duration WINDOW = Duration.ofMinutes(10);

    private final AppSettingService settings;
    private final NotificationChannelRepository channels;
    private final Clock clock;

    /** Creates the component. */
    public DiscoveryMode(AppSettingService settings, NotificationChannelRepository channels, Clock clock) {
        this.settings = settings;
        this.channels = channels;
        this.clock = clock;
    }

    /** The {@code app_setting} key of a channel's window. */
    public static String key(UUID channelId) {
        return "chatops." + channelId + ".discovery";
    }

    /**
     * The stored window of a channel.
     *
     * @param until when it closes
     * @param expired true once it closed (discovery stays off until the channel is saved again)
     */
    public record Window(Instant until, boolean expired) {}

    /**
     * When discovery mode of {@code config} ends, or null when it is off. Opens the window on first use and
     * turns discovery off once it has passed.
     */
    @Transactional
    public Instant activeUntil(ChannelConfig config) {
        if (!config.interactive().discoveryMode()) {
            return null;
        }
        Instant now = clock.instant();
        Optional<Window> stored = settings.get(key(config.channelId()), Window.class);
        if (stored.isPresent() && stored.get().expired()) {
            return null;
        }
        if (stored.isEmpty()) {
            Window opened = new Window(now.plus(WINDOW), false);
            settings.put(key(config.channelId()), opened);
            return opened.until();
        }
        if (!stored.get().until().isAfter(now)) {
            turnOff(config.channelId(), stored.get().until());
            return null;
        }
        return stored.get().until();
    }

    /** When discovery mode of {@code channelId} ends without opening or closing anything, or null. */
    public Instant peek(UUID channelId, boolean discoveryMode) {
        if (!discoveryMode) {
            return null;
        }
        return settings.get(key(channelId), Window.class)
                .filter(w -> !w.expired() && w.until().isAfter(clock.instant()))
                .map(Window::until)
                .orElse(null);
    }

    /** Restarts the window after the channel was saved: it gets a fresh {@link #WINDOW} from now. */
    public void reset(UUID channelId) {
        if (settings.get(key(channelId), Window.class).isPresent()) {
            settings.put(key(channelId), new Window(clock.instant().plus(WINDOW), false));
        }
    }

    private void turnOff(UUID channelId, Instant until) {
        settings.put(key(channelId), new Window(until, true));
        Optional<NotificationChannelEntity> channel = channels.findById(channelId);
        if (channel.isPresent() && channel.get().getConfig() != null
                && Boolean.TRUE.equals(channel.get().getConfig().get("discoveryMode"))) {
            Map<String, Object> config = new LinkedHashMap<>(channel.get().getConfig());
            config.put("discoveryMode", false);
            channel.get().setConfig(config);
            channels.save(channel.get());
        }
    }
}
