package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Acknowledges alerts by Matrix reaction (prompt 033, opt-in {@code actions.reactions: true}).
 *
 * <p>One virtual thread per such channel long-polls the client-server {@code /sync} with a filter of
 * the channel's room and {@code m.reaction} timeline events only, storing {@code next_batch} in
 * {@code app_setting['matrix.<channelId>.since']}. The first sync only records where to start, so
 * old reactions are never replayed. A ✅ reaction ({@code m.annotation} with key ✅) from a user in
 * the channel's {@code allowedSenders} on a message Foresignal sent (the delivery's
 * {@code provider_message_ref} event id) acknowledges that alert as
 * {@code action:matrix:<channelName>}. An empty {@code allowedSenders} acknowledges nothing.
 */
@Component
public class MatrixReactionPoller {

    private static final Logger log = LoggerFactory.getLogger(MatrixReactionPoller.class);

    /** The acknowledging reaction keys (with and without the emoji presentation selector). */
    static final Set<String> ACK_KEYS = Set.of("✅", "✅️");

    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelSender sender;
    private final AppSettingService settings;
    private final NamedParameterJdbcTemplate jdbc;
    private final AlertStore alerts;
    private final AlertService alertService;
    private final ActionProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<UUID, Thread> loops = new ConcurrentHashMap<>();
    private volatile boolean stopped;

    /** Creates the poller. */
    public MatrixReactionPoller(NotificationChannelRepository channels, ChannelService channelService,
            ChannelSender sender, AppSettingService settings, NamedParameterJdbcTemplate jdbc, AlertStore alerts,
            AlertService alertService, ActionProperties properties, JsonMapper json, Clock clock) {
        this.channels = channels;
        this.channelService = channelService;
        this.sender = sender;
        this.settings = settings;
        this.jdbc = jdbc;
        this.alerts = alerts;
        this.alertService = alertService;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** The {@code app_setting} key of a channel's sync token. */
    public static String sinceKey(UUID channelId) {
        return "matrix." + channelId + ".since";
    }

    /** True when {@code channel} acknowledges by reaction. */
    static boolean wanted(NotificationChannelEntity channel) {
        if (!MatrixChannel.ID.equals(channel.getType()) || !channel.isEnabled()) {
            return false;
        }
        ActionsConfig config = ActionsConfig.of(channel.getType(), channel.getConfig());
        return config.enabled() && config.reactions();
    }

    /** Starts loops for wanted channels and stops the others. */
    @Scheduled(fixedDelayString = "${foresignal.actions.reconcile-interval:30s}", initialDelayString = "5s")
    public void reconcile() {
        if (!properties.scheduled() || stopped) {
            return;
        }
        Set<UUID> wanted = new TreeSet<>();
        for (NotificationChannelEntity channel : channels.findByEnabledTrue()) {
            if (wanted(channel)) {
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
            loops.computeIfAbsent(id, k -> Thread.ofVirtual().name("matrix-sync-" + k).start(() -> loop(k)));
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
                pollOnce(channelId, properties.longPoll().toMillis());
                backoff = 5_000;
            } catch (RuntimeException | IOException e) {
                log.warn("Matrix sync of channel {} failed: {}; retrying in {} s", channelId, e.getMessage(),
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
     * One {@code /sync} of {@code channelId} with a long-poll of {@code timeoutMillis}.
     *
     * @return how many alerts were acknowledged
     * @throws IOException when the homeserver cannot be reached
     */
    public int pollOnce(UUID channelId, long timeoutMillis) throws IOException {
        NotificationChannelEntity channel = channels.findById(channelId).orElse(null);
        if (channel == null || !wanted(channel)) {
            return 0;
        }
        ChannelSettings s = channelService.settingsFor(channel, 0, clock.instant());
        String room = s.config("roomId", "");
        Optional<String> since = settings.get(sinceKey(channelId), String.class);
        String url = ChannelSettings.trimSlash(s.config("homeserverUrl", "")) + "/_matrix/client/v3/sync?filter="
                + encode(json.writeValueAsString(filter(room))) + "&timeout=" + (since.isEmpty() ? 0 : timeoutMillis)
                + since.map(token -> "&since=" + encode(token)).orElse("");
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + s.secret("accessToken"));
        ChannelResponse response = sender.send(new PreparedRequest("GET", url, headers,
                PreparedRequest.Bytes.text("application/json", "")));
        if (!response.success()) {
            throw new IOException("/sync answered HTTP " + response.status());
        }
        JsonNode body = json.readTree(response.body());
        int acknowledged = 0;
        if (since.isPresent()) {
            List<String> allowed = s.list("allowedSenders");
            for (JsonNode event : body.path("rooms").path("join").path(room).path("timeline").path("events")) {
                if (react(channel, event, allowed)) {
                    acknowledged++;
                }
            }
        }
        String next = body.path("next_batch").asString("");
        if (!next.isEmpty()) {
            settings.put(sinceKey(channelId), next);
        }
        return acknowledged;
    }

    /** Acknowledges the alert behind a ✅ reaction from an allowed sender; true when it did. */
    boolean react(NotificationChannelEntity channel, JsonNode event, List<String> allowedSenders) {
        if (!"m.reaction".equals(event.path("type").asString()) || !allowedSenders.contains(event.path("sender").asString())) {
            return false;
        }
        JsonNode relates = event.path("content").path("m.relates_to");
        if (!"m.annotation".equals(relates.path("rel_type").asString())
                || !ACK_KEYS.contains(relates.path("key").asString())) {
            return false;
        }
        String target = relates.path("event_id").asString("");
        List<Long> alertIds = jdbc.queryForList("""
                select alert_id from notification_delivery
                 where channel_id = :channel and provider_message_ref = :ref and alert_id is not null
                 order by id limit 1
                """, new MapSqlParameterSource("channel", channel.getId()).addValue("ref", target), Long.class);
        if (alertIds.isEmpty()) {
            return false;
        }
        Optional<AlertView> alert = alerts.find(alertIds.getFirst());
        if (alert.isEmpty() || !AlertState.FIRING.wire().equals(alert.get().state())) {
            return false;
        }
        String actor = "action:matrix:" + channel.getName();
        alertService.acknowledge(alert.get().id(), AlertService.Transition.NONE,
                actor.length() > 200 ? actor.substring(0, 200) : actor);
        return true;
    }

    /** The sync filter: the room's {@code m.reaction} timeline events only. */
    static Map<String, Object> filter(String room) {
        Map<String, Object> none = Map.of("types", List.of());
        Map<String, Object> timeline = new LinkedHashMap<>();
        timeline.put("types", List.of("m.reaction"));
        timeline.put("limit", 50);
        Map<String, Object> roomFilter = new LinkedHashMap<>();
        roomFilter.put("rooms", List.of(room));
        roomFilter.put("timeline", timeline);
        roomFilter.put("state", none);
        roomFilter.put("ephemeral", none);
        roomFilter.put("account_data", none);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("presence", none);
        out.put("account_data", none);
        out.put("room", roomFilter);
        return out;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
