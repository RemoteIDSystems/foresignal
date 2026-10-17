package ai.foresignal.notify;

import ai.foresignal.notify.inbox.PresenceService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

/**
 * Persists notifications and enqueues their deliveries (prompt 018).
 *
 * <p>{@link #publish} writes the {@code notification} row first, always, even when no channel takes
 * the message (prompt 033's inbox reads this table), then creates one {@code send} delivery per
 * accepting channel. Each delivery's first due time accounts for, in order:
 *
 * <ol>
 *   <li>the enrichment wait ({@code wait_for_enrichment_seconds} on a rule with {@code enrich=true},
 *       {@code deferred_reason=enrichment});
 *   <li>opt-in grouping ({@code group_window_seconds > 0}): the first alert of a rule holds for the
 *       window ({@code grouping}); later alerts of that rule in the window are folded into it;
 *   <li>presence (prompt 033): with {@code defer_while_active_seconds > 0}, a non-critical
 *       {@code alert.fired} waits that long while any desktop client is active
 *       ({@code deferred_reason=presence}); the worker suppresses it if the alert is acknowledged,
 *       snoozed or resolved, or the notification read on a desktop, before it is released. Stale
 *       presence fails open; critical alerts and system events are never deferred;
 *   <li>the global pause, then the channel's quiet hours ({@link Deferral}).
 * </ol>
 *
 * <p>{@link #publishEscalation} (prompt 033) persists an escalation message and sends it to the
 * step's channels whatever their filters, with quiet hours applying except to critical alerts.
 *
 * <p>After an alert send the channel's backlog cap is applied: the oldest pending sends beyond the
 * cap are folded into one digest ("+N more alerts"). {@link #publishUpdate} handles alert lifecycle
 * changes: one {@code update} (edit in place) or {@code followup} per channel that received the
 * original. Lifecycle updates and system events are never folded into a digest.
 */
@Service
public class NotificationService {

    private final NotificationStore store;
    private final NotificationChannelRepository channels;
    private final NotificationDeliveryRepository deliveries;
    private final DeliveryStore deliveryStore;
    private final ChannelService channelService;
    private final ChannelTypeRegistry types;
    private final AppSettingService settings;
    private final InstallIdService installId;
    private final PresenceService presence;
    private final Clock clock;

    /** Creates the service. */
    public NotificationService(NotificationStore store, NotificationChannelRepository channels,
            NotificationDeliveryRepository deliveries, DeliveryStore deliveryStore, ChannelService channelService,
            ChannelTypeRegistry types, AppSettingService settings, InstallIdService installId, PresenceService presence,
            Clock clock) {
        this.presence = presence;
        this.store = store;
        this.channels = channels;
        this.deliveries = deliveries;
        this.deliveryStore = deliveryStore;
        this.channelService = channelService;
        this.types = types;
        this.settings = settings;
        this.installId = installId;
        this.clock = clock;
    }

    /**
     * Persists {@code message} and enqueues it for every enabled channel that accepts it and passes
     * {@code extraFilter} (for briefings, 029, and system events).
     *
     * @return the persisted message, with its id
     */
    public NotificationMessage publish(NotificationMessage message, Predicate<NotificationChannel> extraFilter) {
        Predicate<NotificationChannel> filter = extraFilter == null ? c -> true : extraFilter;
        Instant now = clock.instant();
        return store.write(writer -> {
            NotificationMessage persisted = writer.persist(fill(message, now));
            NotificationSettings pause = pause();
            for (NotificationChannelEntity entity : channels.findByEnabledTrue()) {
                if (types.find(entity.getType()).isEmpty()) {
                    continue;
                }
                NotificationChannel channel = NotificationChannel.of(entity, List.of());
                if (channel.accepts(persisted) && filter.test(channel)) {
                    enqueueSend(entity, channel, persisted, pause, now);
                }
            }
            return persisted;
        });
    }

    /**
     * Persists a lifecycle update ({@link NotificationMessage#updateOf()} names the original) and
     * enqueues an {@code update} or {@code followup} for each channel that received the original:
     * {@code update} when the type edits in place, the channel has {@code edit_in_place} on and the
     * original delivery stored a provider reference; otherwise {@code followup}.
     *
     * @return the persisted message
     */
    public NotificationMessage publishUpdate(NotificationMessage message) {
        Instant now = clock.instant();
        return store.write(writer -> {
            NotificationMessage persisted = writer.persist(fill(message, now));
            if (persisted.updateOf() == null) {
                return persisted;
            }
            NotificationSettings pause = pause();
            Set<UUID> done = new HashSet<>();
            for (NotificationDeliveryEntity original : deliveries.findByNotificationIdOrderByIdAsc(persisted.updateOf())) {
                if (original.getStatus() == DeliveryStatus.FAILED || original.getStatus() == DeliveryStatus.SUPPRESSED
                        || !done.add(original.getChannelId())) {
                    continue;
                }
                Optional<NotificationChannelEntity> entity = channels.findById(original.getChannelId());
                if (entity.isEmpty() || !entity.get().isEnabled() || types.find(entity.get().getType()).isEmpty()) {
                    continue;
                }
                if (!types.get(entity.get().getType()).acceptsUpdate(persisted.subtype())) {
                    // Prompt 084: mesh channels take only the resolution, never acknowledgements or snoozes.
                    continue;
                }
                enqueueUpdate(entity.get(), original, persisted, pause, now);
            }
            return persisted;
        });
    }

    /**
     * Persists an escalation message of an alert (prompt 033) and enqueues one delivery, marked with
     * {@code escalationStep}, per enabled channel among {@code channelIds}, regardless of the
     * channels' event, severity and rule filters. Quiet hours and the pause apply, except to
     * critical alerts.
     *
     * @return the persisted message
     */
    public NotificationMessage publishEscalation(NotificationMessage message, Collection<UUID> channelIds, int step) {
        Instant now = clock.instant();
        return store.write(writer -> {
            NotificationMessage persisted = writer.persist(fill(message, now));
            NotificationSettings pause = pause();
            Set<UUID> done = new HashSet<>();
            for (UUID channelId : channelIds) {
                if (!done.add(channelId)) {
                    continue;
                }
                Optional<NotificationChannelEntity> entity = channels.findById(channelId);
                if (entity.isEmpty() || !entity.get().isEnabled() || types.find(entity.get().getType()).isEmpty()) {
                    continue;
                }
                NotificationDeliveryEntity d = delivery(entity.get(), persisted, now);
                d.setEscalationStep(step);
                Instant due = now;
                String reason = null;
                if (!"critical".equals(persisted.severity())) {
                    Optional<Deferral.Decision> deferral = quietHoursOverride(entity.get(), Deferral.check(
                            persisted.severity(), QuietHours.fromMap(entity.get().getQuietHours()),
                            channelService.zone(entity.get().getTimezone()), pause, now));
                    if (deferral.isPresent()) {
                        due = deferral.get().until();
                        reason = deferral.get().reason();
                    }
                }
                d.setNextAttemptAt(due);
                d.setDeferredReason(reason);
                deliveries.save(d);
            }
            return persisted;
        });
    }

    private boolean desktopActive(Instant now) {
        try {
            return presence.desktopActive(now);
        } catch (RuntimeException e) {
            // Fail open: deliver everywhere.
            return false;
        }
    }

    private NotificationMessage fill(NotificationMessage m, Instant now) {
        NotificationMessage out = m;
        if (m.origin() == null || m.createdAt() == null) {
            out = new NotificationMessage(m.id(), m.event(), m.subtype(), m.title(), m.shortTitle(), m.bodyMarkdown(),
                    m.severity(), m.url(), m.links(), m.data(), m.tags(), m.attachments(), m.updateOf(),
                    m.origin() == null ? installId.installId() : m.origin(),
                    m.createdAt() == null ? now : m.createdAt(), m.alertId(), m.ruleId(), m.briefingRunId(),
                    m.exercise());
        }
        return out;
    }

    private void enqueueSend(NotificationChannelEntity entity, NotificationChannel channel, NotificationMessage m,
            NotificationSettings pause, Instant now) {
        NotificationDeliveryEntity d = delivery(entity, m, now);
        boolean fired = NotificationMessage.ALERT.equals(m.event()) && "alert.fired".equals(m.subtype());
        Instant due = now;
        String reason = null;
        if (fired && channel.waitForEnrichmentSeconds() > 0 && m.data() != null
                && Boolean.TRUE.equals(m.data().get("enrich"))) {
            due = now.plusSeconds(channel.waitForEnrichmentSeconds());
            reason = DeferredReason.ENRICHMENT;
        }
        if (fired && channel.groupWindowSeconds() > 0 && m.ruleId() != null) {
            Optional<Long> holder = deliveryStore.groupingHolder(entity.getId(), m.ruleId());
            if (holder.isPresent()) {
                NotificationDeliveryEntity held = deliveries.findById(holder.get()).orElseThrow();
                held.setKind(DeliveryKind.DIGEST);
                deliveries.save(held);
                d.setStatus(DeliveryStatus.DELIVERED);
                d.setFoldedInto(held.getId());
                d.setNote("folded into delivery " + held.getId());
                d.setDeliveredAt(now);
                d.setNextAttemptAt(null);
                deliveries.save(d);
                return;
            }
            Instant end = now.plusSeconds(channel.groupWindowSeconds());
            if (end.isAfter(due)) {
                due = end;
                reason = DeferredReason.GROUPING;
            }
        }
        if (fired && channel.deferWhileActiveSeconds() > 0 && !"critical".equals(m.severity())
                && desktopActive(now)) {
            Instant until = now.plusSeconds(channel.deferWhileActiveSeconds());
            if (until.isAfter(due)) {
                due = until;
                reason = DeferredReason.PRESENCE;
            }
        }
        Optional<Deferral.Decision> deferral = quietHoursOverride(entity, Deferral.check(m.severity(),
                channel.quietHours(), channelService.zone(entity.getTimezone()), pause, due));
        if (deferral.isPresent() && deferral.get().until().isAfter(due)) {
            due = deferral.get().until();
            reason = deferral.get().reason();
        }
        d.setNextAttemptAt(due);
        d.setDeferredReason(reason);
        deliveries.saveAndFlush(d);
        if (NotificationMessage.ALERT.equals(m.event()) && !m.isUpdate()) {
            deliveryStore.foldBacklog(entity.getId(), Math.max(1, entity.getBacklogCap()), now);
        }
    }

    private void enqueueUpdate(NotificationChannelEntity entity, NotificationDeliveryEntity original,
            NotificationMessage m, NotificationSettings pause, Instant now) {
        NotificationDeliveryEntity d = delivery(entity, m, now);
        ChannelType type = types.get(entity.getType());
        boolean edit = type.capabilities().editInPlace() && entity.isEditInPlace() && original.getFoldedInto() == null
                && original.getProviderMessageRef() != null;
        d.setKind(edit ? DeliveryKind.UPDATE : DeliveryKind.FOLLOWUP);
        Instant due = now;
        String reason = null;
        Optional<Deferral.Decision> deferral = quietHoursOverride(entity, Deferral.check(m.severity(),
                QuietHours.fromMap(entity.getQuietHours()), channelService.zone(entity.getTimezone()), pause, now));
        if (deferral.isPresent()) {
            due = deferral.get().until();
            reason = deferral.get().reason();
        }
        d.setNextAttemptAt(due);
        d.setDeferredReason(reason);
        deliveries.save(d);
    }

    /**
     * Drops a quiet-hours deferral on a fan-out channel whose {@link QuietHoursMode} is not
     * {@code defer} (prompt 031): the worker then downgrades or drops the send when it is claimed.
     */
    private Optional<Deferral.Decision> quietHoursOverride(NotificationChannelEntity entity,
            Optional<Deferral.Decision> deferral) {
        if (deferral.isEmpty() || !DeferredReason.QUIET_HOURS.equals(deferral.get().reason())) {
            return deferral;
        }
        boolean fanOut = types.find(entity.getType()).map(t -> t.capabilities().fanOut()).orElse(false);
        if (fanOut && !QuietHoursMode.DEFER.equals(QuietHoursMode.of(entity.getConfig()))) {
            return Optional.empty();
        }
        return deferral;
    }

    private static NotificationDeliveryEntity delivery(NotificationChannelEntity entity, NotificationMessage m,
            Instant now) {
        NotificationDeliveryEntity d = new NotificationDeliveryEntity(entity.getId(), Objects.requireNonNull(m.id()),
                m.event(), now);
        d.setAlertId(m.alertId());
        d.setBriefingRunId(m.briefingRunId());
        return d;
    }

    /** The global pause setting. */
    public NotificationSettings pause() {
        try {
            return settings.getOrDefault(SettingKeys.NOTIFICATIONS, NotificationSettings.class,
                    NotificationSettings::defaults);
        } catch (RuntimeException e) {
            return NotificationSettings.defaults();
        }
    }
}
