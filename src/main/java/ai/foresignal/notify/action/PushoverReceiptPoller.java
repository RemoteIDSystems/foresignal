package ai.foresignal.notify.action;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.PushoverChannel;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.trigger.AlertService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns acknowledged Pushover emergency receipts into Foresignal acknowledgements (prompt 033).
 *
 * <p>Critical alerts go to Pushover with priority 2 (018) and the returned {@code receipt} is stored
 * as the delivery's {@code provider_message_ref}. Every {@code foresignal.actions.receipt-interval}
 * (60 s; Pushover asks for no faster than every 5 s) this poller asks
 * {@code GET /1/receipts/{receipt}.json?token=…} for each receipt of a delivered alert send of the
 * last {@value #WINDOW_HOURS} hours whose alert is still firing. {@code acknowledged=1} acknowledges
 * the alert as {@code pushover:<acknowledged_by_device>}; {@code expired=1} stops polling that
 * receipt. Receipts are matched against {@code [A-Za-z0-9]{1,64}} before they are put in a URL.
 */
@Component
public class PushoverReceiptPoller {

    private static final Logger log = LoggerFactory.getLogger(PushoverReceiptPoller.class);

    /** Pushover emergency retries last at most 3 h ({@code expire} ≤ 10800 s); 018 sends 3600 s. */
    static final long WINDOW_HOURS = 3;

    private static final Pattern RECEIPT = Pattern.compile("^[A-Za-z0-9]{1,64}$");

    private final NamedParameterJdbcTemplate jdbc;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelSender sender;
    private final AlertService alerts;
    private final ActionProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final Set<String> finished = ConcurrentHashMap.newKeySet();

    /** Creates the poller. */
    public PushoverReceiptPoller(NamedParameterJdbcTemplate jdbc, NotificationChannelRepository channels,
            ChannelService channelService, ChannelSender sender, AlertService alerts, ActionProperties properties,
            JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.channels = channels;
        this.channelService = channelService;
        this.sender = sender;
        this.alerts = alerts;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** The scheduled poll. */
    @Scheduled(fixedDelayString = "${foresignal.actions.receipt-interval:60s}", initialDelayString = "30s")
    public void poll() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            pollOnce();
        } catch (RuntimeException e) {
            log.warn("Pushover receipt poll failed: {}", e.toString());
        }
    }

    /**
     * Checks every open receipt once.
     *
     * @return how many alerts were acknowledged
     */
    public int pollOnce() {
        Instant now = clock.instant();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select d.provider_message_ref as receipt, d.alert_id, d.channel_id
                  from notification_delivery d
                  join notification_channel c on c.id = d.channel_id
                  join alert a on a.id = d.alert_id
                 where c.type = 'pushover' and c.enabled and d.status = 'DELIVERED'
                   and d.provider_message_ref is not null and d.delivered_at > :since and a.state = 'firing'
                 order by d.id
                """, new MapSqlParameterSource("since", Timestamp.from(now.minus(Duration.ofHours(WINDOW_HOURS)))));
        int acknowledged = 0;
        for (Map<String, Object> row : rows) {
            String receipt = String.valueOf(row.get("receipt"));
            if (finished.contains(receipt) || !RECEIPT.matcher(receipt).matches()) {
                continue;
            }
            Optional<NotificationChannelEntity> channel = channels.findById((UUID) row.get("channel_id"));
            if (channel.isEmpty()) {
                continue;
            }
            try {
                if (check(channel.get(), receipt, ((Number) row.get("alert_id")).longValue(), now)) {
                    acknowledged++;
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Pushover receipt check failed: {}", e.toString());
            }
        }
        return acknowledged;
    }

    private boolean check(NotificationChannelEntity channel, String receipt, long alertId, Instant now)
            throws IOException {
        ChannelSettings s = channelService.settingsFor(channel, 0, now);
        String url = PushoverChannel.apiBase(s) + "/1/receipts/" + receipt + ".json?token="
                + URLEncoder.encode(s.secret("appToken") == null ? "" : s.secret("appToken"), StandardCharsets.UTF_8);
        ChannelResponse response = sender.send(new PreparedRequest("GET", url, new LinkedHashMap<>(),
                PreparedRequest.Bytes.text("application/json", "")));
        if (!response.success()) {
            if (response.status() == 400 || response.status() == 404) {
                finished.add(receipt);
            }
            return false;
        }
        JsonNode body = json.readTree(response.body());
        if (body.path("acknowledged").asInt() == 1) {
            finished.add(receipt);
            String device = body.path("acknowledged_by_device").asString("");
            alerts.acknowledge(alertId, AlertService.Transition.NONE,
                    "pushover:" + (device.isBlank() ? "unknown" : device));
            return true;
        }
        if (body.path("expired").asInt() == 1) {
            finished.add(receipt);
        }
        return false;
    }
}
