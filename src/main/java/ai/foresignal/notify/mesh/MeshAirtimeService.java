package ai.foresignal.notify.mesh;

import ai.foresignal.notify.ChannelChangedEvent;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The airtime state of the mesh channels as the API and the operator see it (prompt 084).
 *
 * <ul>
 *   <li>{@link #airtime}: {@code GET /v1/channels/{channelId}/airtime} ({@code ChannelAirtime}): the governor's
 *       window plus the deliveries waiting for airtime ({@code queued}).
 *   <li>Backlog alarm: every minute, a channel whose oldest airtime-deferred delivery is older than an hour raises
 *       one {@code system.mesh_airtime_backlog} event (warning) per episode; the episode ends when its backlog clears.
 *   <li>Lifecycle: a disabled, deleted or changed mesh channel is detached from its companion link, and a deleted
 *       one's governor is forgotten.
 * </ul>
 */
@Component
public class MeshAirtimeService {

    private static final Logger log = LoggerFactory.getLogger(MeshAirtimeService.class);

    /** The subtype of the backlog event. */
    public static final String BACKLOG_EVENT = "system.mesh_airtime_backlog";

    /** A backlog older than this raises the event. */
    public static final Duration BACKLOG_AGE = Duration.ofHours(1);

    private final NotificationChannelRepository channels;
    private final AirtimeGovernors governors;
    private final MeshCoreSender meshcore;
    private final NamedParameterJdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final NotifyProperties properties;
    private final Clock clock;
    private final Set<UUID> alarmed = new HashSet<>();

    /**
     * The window of one channel (OpenAPI {@code ChannelAirtime}).
     *
     * @param windowSeconds the window length
     * @param usedSeconds the estimated airtime in the window
     * @param limitSeconds the operator's share of the window
     * @param dutyCyclePercent the operator's share in percent
     * @param queued deliveries waiting for airtime
     * @param foldedLastHour alerts folded into digest lines in the window
     * @param droppedLastHour transmissions dropped in the window
     */
    public record ChannelAirtime(long windowSeconds, double usedSeconds, double limitSeconds, double dutyCyclePercent,
            int queued, int foldedLastHour, int droppedLastHour) {}

    /**
     * Creates the service.
     *
     * @param channels the stored channels
     * @param governors the per-channel governors
     * @param meshcore the MeshCore sender, detached on changes
     * @param jdbc reads the airtime-deferred deliveries
     * @param events publishes the backlog event
     * @param properties {@code scheduled} turns the backlog check off in tests
     * @param clock the clock
     */
    @Autowired
    public MeshAirtimeService(NotificationChannelRepository channels, AirtimeGovernors governors,
            MeshCoreSender meshcore, NamedParameterJdbcTemplate jdbc, ApplicationEventPublisher events,
            NotifyProperties properties, Clock clock) {
        this.channels = channels;
        this.governors = governors;
        this.meshcore = meshcore;
        this.jdbc = jdbc;
        this.events = events;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * The airtime window of {@code channel}.
     *
     * @param type the channel's mesh type
     * @throws IllegalArgumentException when the channel's settings do not validate
     */
    public ChannelAirtime airtime(NotificationChannelEntity channel, MeshChannelType type) {
        AirtimeGovernor.Snapshot s = governors.forChannel(channel, type).governor().snapshot(clock.instant());
        Integer queued = jdbc.queryForObject("""
                select count(*) from notification_delivery
                 where channel_id = :c and status = 'PENDING' and deferred_reason = :reason
                """, new MapSqlParameterSource("c", channel.getId()).addValue("reason", DeferredReason.AIRTIME),
                Integer.class);
        return new ChannelAirtime(s.windowSeconds(), round(s.usedSeconds()), round(s.limitSeconds()),
                s.dutyCyclePercent(), queued == null ? 0 : queued, s.foldedLastHour(), s.droppedLastHour());
    }

    /** The scheduled backlog check; does nothing when {@code foresignal.notify.scheduled} is false. */
    @Scheduled(fixedDelayString = "60s", initialDelayString = "60s")
    public void poll() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            checkBacklog();
        } catch (RuntimeException e) {
            log.warn("The mesh airtime backlog check failed", e);
        }
    }

    /**
     * Raises the backlog event for every channel whose oldest airtime-deferred delivery is older than
     * {@link #BACKLOG_AGE}, once per episode.
     *
     * @return the channels alarmed by this run
     */
    public synchronized List<UUID> checkBacklog() {
        Instant now = clock.instant();
        Map<UUID, Instant> oldest = new LinkedHashMap<>();
        jdbc.query("""
                select channel_id, min(created_at) as oldest from notification_delivery
                 where status = 'PENDING' and deferred_reason = :reason
                 group by channel_id
                """, new MapSqlParameterSource("reason", DeferredReason.AIRTIME), rs -> {
                    Timestamp at = rs.getTimestamp("oldest");
                    oldest.put(rs.getObject("channel_id", UUID.class), at == null ? now : at.toInstant());
                });
        alarmed.retainAll(oldest.keySet());
        List<UUID> raised = new java.util.ArrayList<>();
        for (Map.Entry<UUID, Instant> e : oldest.entrySet()) {
            Duration age = Duration.between(e.getValue(), now);
            if (age.compareTo(BACKLOG_AGE) <= 0 || !alarmed.add(e.getKey())) {
                continue;
            }
            String name = channels.findById(e.getKey()).map(NotificationChannelEntity::getName).orElse("a mesh channel");
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("channelId", e.getKey().toString());
            data.put("oldestDeferredAt", e.getValue().toString());
            events.publishEvent(new SystemEvent(BACKLOG_EVENT, "warning", "Mesh airtime backlog on " + name,
                    "Alerts for this mesh channel have waited for airtime for over an hour (since " + e.getValue()
                            + "). They will go out as one digest line when the duty-cycle window allows; consider a"
                            + " higher minSeverity or a faster preset.",
                    "/v1/channels/" + e.getKey() + "/airtime", data));
            raised.add(e.getKey());
        }
        return raised;
    }

    /** Detaches a disabled, deleted or changed mesh channel, once its transaction committed. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onChannelChanged(ChannelChangedEvent event) {
        if (!MeshChannels.TYPES.contains(event.type())) {
            return;
        }
        meshcore.detach(event.channelId());
        if (event.deleted()) {
            governors.forget(event.channelId());
        }
    }

    /** The governor's state of {@code channelId}, if one was built (tests, diagnostics). */
    public Optional<AirtimeGovernor.Snapshot> snapshot(UUID channelId) {
        return governors.find(channelId).map(e -> e.governor().snapshot(clock.instant()));
    }

    private static double round(double seconds) {
        return Math.round(seconds * 1000.0) / 1000.0;
    }
}
