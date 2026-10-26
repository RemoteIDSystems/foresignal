package ai.foresignal.system;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.security.user.PermissionResolver;
import ai.foresignal.security.user.Permissions;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserStatus;
import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * The one path every operational alert takes (prompt R2-014): a subsystem reports the level of a named condition
 * ({@link #report}) and an alert is published only when the level changes, so one breach fires once. Entering
 * {@code warn} or {@code critical} publishes the condition's alert; returning to {@code ok} publishes one recovery
 * notice. The edge state is kept in memory: after a restart a condition that is still breached fires again once.
 *
 * <p>The alert is a {@link SystemEvent} routed by the {@code alertChannels} of the admin's
 * {@link ResourceThresholds}: when it names channels, to exactly those; when it is empty, to every channel subscribed
 * to {@code system} and to the personal channels of every active user who holds {@code audit.read}.
 */
@Service
public class OperationalAlertService {

    private static final Logger log = LoggerFactory.getLogger(OperationalAlertService.class);

    /**
     * What an alert says at one level.
     *
     * @param subtype the notification subtype, for example {@code system.disk_critical}
     * @param severity {@code info}, {@code warning} or {@code critical}
     * @param title the title
     * @param body the markdown body
     * @param path the API path it links to, or null
     * @param data structured details
     */
    public record Alert(String subtype, String severity, String title, String body, String path,
            Map<String, Object> data) {}

    /** Says what to publish for a level change. */
    @FunctionalInterface
    public interface Wording {

        /**
         * The alert for a change.
         *
         * @param level the new level: {@code ok} (a recovery), {@code warn} or {@code critical}
         * @param previous the previous level
         * @return the alert
         */
        Alert at(String level, String previous);
    }

    private final ApplicationEventPublisher events;
    private final ResourceThresholdsService thresholds;
    private final NotificationChannelRepository channels;
    private final UserRepository users;
    private final PermissionResolver permissions;
    private final Map<String, String> levels = new ConcurrentHashMap<>();

    /**
     * Creates the service.
     *
     * @param events the event publisher
     * @param thresholds the admin's thresholds ({@code alertChannels})
     * @param channels the channels
     * @param users the users
     * @param permissions resolves who holds {@code audit.read}
     */
    public OperationalAlertService(ApplicationEventPublisher events, ResourceThresholdsService thresholds,
            NotificationChannelRepository channels, UserRepository users, PermissionResolver permissions) {
        this.events = events;
        this.thresholds = thresholds;
        this.channels = channels;
        this.users = users;
        this.permissions = permissions;
    }

    /**
     * Reports the current level of a condition and publishes an alert when it changed.
     *
     * @param key the condition, for example {@code disk}
     * @param level {@code ok}, {@code warn} or {@code critical}
     * @param wording what to publish
     * @return true when an alert was published
     */
    public synchronized boolean report(String key, String level, Wording wording) {
        String now = level == null ? "ok" : level;
        String previous = levels.getOrDefault(key, "ok");
        if (now.equals(previous)) {
            return false;
        }
        levels.put(key, now);
        Alert alert = wording.at(now, previous);
        try {
            events.publishEvent(new SystemEvent(alert.subtype(), alert.severity(), alert.title(), alert.body(),
                    alert.path(), alert.data(), targets()));
        } catch (RuntimeException e) {
            log.warn("Could not publish operational alert {}", alert.subtype(), e);
        }
        return true;
    }

    /**
     * The level last reported for a condition.
     *
     * @param key the condition
     * @return its level, {@code ok} when never reported
     */
    public String level(String key) {
        return levels.getOrDefault(key, "ok");
    }

    /** Forgets every edge state (tests). */
    public synchronized void reset() {
        levels.clear();
    }

    /** Where an operational alert goes now. */
    SystemEvent.Targets targets() {
        ResourceThresholds t = thresholds.get();
        if (!t.alertChannels().isEmpty()) {
            return SystemEvent.Targets.only(t.alertChannels().stream().map(UUID::fromString).toList());
        }
        Set<UUID> personal = new LinkedHashSet<>();
        for (UserEntity user : users.findAll()) {
            if (user.getStatus() != UserStatus.ACTIVE || user.getPrincipalId() == null
                    || !permissions.forUser(user.getId()).keys().contains(Permissions.AUDIT_READ.key())) {
                continue;
            }
            for (NotificationChannelEntity channel : channels.findByOwnerPrincipalId(user.getPrincipalId())) {
                if (channel.isEnabled()) {
                    personal.add(channel.getId());
                }
            }
        }
        return SystemEvent.Targets.subscribersAnd(personal);
    }
}
