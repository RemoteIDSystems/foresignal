package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Long-polls Telegram {@code getUpdates} for every enabled Telegram channel some
 * {@link TelegramUpdateHandler} wants (prompt 033): with actions enabled, the
 * {@link ActionCallbackHandler} executes the buttons.
 *
 * <p>One virtual thread per channel loops {@code getUpdates(offset, timeout, allowed_updates)},
 * hands each update to the handlers in order, and stores the next {@code offset} in
 * {@code app_setting['telegram.<channelId>.offset']} after every batch, so a restart neither replays
 * nor loses a press. Requests go through {@link ChannelSender} (no redirects, the channel timeouts);
 * the long-poll timeout ({@code foresignal.actions.long-poll}, 10 s) stays below the request
 * timeout. Errors back off from 5 s to 60 s.
 *
 * <p><b>The bot token must be dedicated to Foresignal:</b> {@code getUpdates} does not work while a
 * webhook is set for the bot, and two pollers of one bot steal each other's updates.
 */
@Component
public class TelegramCallbackPoller {

    private static final Logger log = LoggerFactory.getLogger(TelegramCallbackPoller.class);

    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelSender sender;
    private final AppSettingService settings;
    private final List<TelegramUpdateHandler> handlers;
    private final ActionProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<UUID, Thread> loops = new ConcurrentHashMap<>();
    private volatile boolean stopped;

    /** Creates the poller. */
    public TelegramCallbackPoller(NotificationChannelRepository channels, ChannelService channelService,
            ChannelSender sender, AppSettingService settings, List<TelegramUpdateHandler> handlers,
            ActionProperties properties, JsonMapper json, Clock clock) {
        this.channels = channels;
        this.channelService = channelService;
        this.sender = sender;
        this.settings = settings;
        this.handlers = List.copyOf(handlers);
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** The {@code app_setting} key of a channel's offset. */
    public static String offsetKey(UUID channelId) {
        return "telegram." + channelId + ".offset";
    }

    /** Starts loops for wanted channels and stops loops of channels no longer wanted. */
    @Scheduled(fixedDelayString = "${foresignal.actions.reconcile-interval:30s}", initialDelayString = "5s")
    public void reconcile() {
        if (!properties.scheduled() || stopped) {
            return;
        }
        Set<UUID> wanted = new TreeSet<>();
        for (NotificationChannelEntity channel : channels.findByEnabledTrue()) {
            if (TelegramChannel.ID.equals(channel.getType()) && wanted(channel)) {
                wanted.add(channel.getId());
            }
        }
        loops.keySet().removeIf(id -> {
            if (!wanted.contains(id)) {
                loops.get(id).interrupt();
                return true;
            }
            return false;
        });
        for (UUID id : wanted) {
            loops.computeIfAbsent(id, k -> Thread.ofVirtual().name("telegram-poll-" + k).start(() -> loop(k)));
        }
    }

    /** Stops every loop. */
    @PreDestroy
    public void stop() {
        stopped = true;
        loops.values().forEach(Thread::interrupt);
        loops.clear();
    }

    private void loop(UUID channelId) {
        long backoff = 5_000;
        while (!stopped && !Thread.currentThread().isInterrupted() && loops.containsKey(channelId)) {
            try {
                pollOnce(channelId, (int) properties.longPoll().toSeconds());
                backoff = 5_000;
            } catch (RuntimeException | IOException e) {
                log.warn("Telegram polling of channel {} failed: {}; retrying in {} s", channelId, e.getMessage(),
                        backoff / 1000);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException interrupted) {
                    return;
                }
                backoff = Math.min(backoff * 2, 60_000);
            }
        }
    }

    /**
     * One {@code getUpdates} call for {@code channelId} with a long-poll of {@code timeoutSeconds}
     * (0 for a short poll), its updates handled and the offset stored.
     *
     * @return how many updates were received
     * @throws IOException when Telegram cannot be reached
     */
    public int pollOnce(UUID channelId, int timeoutSeconds) throws IOException {
        NotificationChannelEntity channel = channels.findById(channelId).orElse(null);
        if (channel == null || !channel.isEnabled() || !TelegramChannel.ID.equals(channel.getType())) {
            return 0;
        }
        ChannelSettings s = channelService.settingsFor(channel, 0, clock.instant());
        Long offset = settings.get(offsetKey(channelId), Long.class).orElse(null);
        Set<String> types = new TreeSet<>();
        handlers.stream().filter(h -> h.wants(channel)).forEach(h -> types.addAll(h.updateTypes()));
        Map<String, Object> body = new LinkedHashMap<>();
        if (offset != null) {
            body.put("offset", offset);
        }
        body.put("timeout", Math.max(0, timeoutSeconds));
        body.put("allowed_updates", List.copyOf(types));
        ChannelResponse response = sender.send(post(TelegramChannel.apiMethod(s, "getUpdates"), body));
        if (!response.success()) {
            throw new IOException("getUpdates answered HTTP " + response.status());
        }
        JsonNode result = json.readTree(response.body()).path("result");
        int count = 0;
        Long next = offset;
        for (JsonNode update : result) {
            count++;
            long id = update.path("update_id").asLong();
            next = next == null ? id + 1 : Math.max(next, id + 1);
            for (TelegramUpdateHandler handler : handlers) {
                try {
                    if (handler.wants(channel) && handler.handle(channel, s, update)) {
                        break;
                    }
                } catch (RuntimeException e) {
                    log.warn("A Telegram update handler failed on update {} of channel {}: {}", id, channelId,
                            e.toString());
                }
            }
        }
        if (next != null && !next.equals(offset)) {
            settings.put(offsetKey(channelId), next);
        }
        return count;
    }

    private boolean wanted(NotificationChannelEntity channel) {
        return handlers.stream().anyMatch(h -> h.wants(channel));
    }

    /** A JSON POST to the Bot API. */
    PreparedRequest post(String url, Object body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        return new PreparedRequest("POST", url, headers,
                PreparedRequest.Bytes.text("application/json", json.writeValueAsString(body)));
    }
}
