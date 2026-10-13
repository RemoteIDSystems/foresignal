package ai.foresignal.notify;

import ai.foresignal.notify.action.ActionMinter;
import ai.foresignal.notify.inbox.InboxService;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends due deliveries (prompt 018).
 *
 * <p>Every poll ({@code foresignal.notify.poll-interval}, 2 s) claims up to 50 due rows with
 * {@code FOR UPDATE SKIP LOCKED} in a short lease transaction, then sends them on virtual threads,
 * one thread per channel so a channel's messages stay in order. For each delivery:
 *
 * <ol>
 *   <li>a missing or disabled channel fails it;
 *   <li>the global pause and quiet hours are checked again (they may have started since it was
 *       enqueued); a delivery released from either together with others of its channel becomes one
 *       digest;
 *   <li>the channel's Bucket4j bucket must have a token, else it is rescheduled without counting an
 *       attempt;
 *   <li>the message is built (channel templates for a fired alert, a digest, or the stored update),
 *       rendered by the type, and sent. A fired alert is rendered from the <em>latest</em> alert's
 *       enrichment summary (prompt 030), not the copy taken when it was routed, so a send that
 *       waited for the summary includes it.
 * </ol>
 *
 * <p>An {@code update} delivery of an {@code alert.fired} notification itself (rather than of a
 * lifecycle update naming it) is the enrichment edit of prompt 030: it edits the original message
 * into the re-rendered one with the summary. When the edit cannot be made it is settled with a note
 * and nothing is sent, so a channel never receives a second copy of the alert.
 *
 * <p>A channel whose type fans out ({@link ChannelCapabilities#fanOut()}, the built-in Push channel
 * of prompt 031) is handed to its {@link FanOutDelivery} after the deferral check, instead of being
 * rendered and rate-limited here. Quiet hours on such a channel follow its
 * {@link QuietHoursMode}: defer (as above), downgrade (sent now, flagged) or drop (settled with a
 * note).
 *
 * <p>Prompt 033: a send held for presence ({@code deferred_reason=presence}) is settled
 * {@code suppressed} instead of sent when its alert was acknowledged, snoozed or resolved, or its
 * notification read on a desktop, meanwhile. A firing alert's first send (and its enrichment edit)
 * carries action capabilities minted at this point ({@link ActionMinter}): types with buttons render
 * them from {@link ChannelSettings#actions()}, other types get confirm-page links in the body, and the
 * Push channel passes them to its fan-out. Quiet hours never hold back a critical escalation delivery.
 *
 * <p>Prompt 055: before rendering a fired alert's send, {@link AlertMapService#attach} adds the alert's
 * static map (an image on the five image types, a link elsewhere), or holds the send for up to 8 s while
 * the map is made. A destination that refuses the image ({@link ChannelType#attachmentRefused}) gets the
 * text at once, and the delivery records {@code text-only}; a type's {@link ChannelType#followUps} are sent
 * after a successful send.
 *
 * <p>Outcomes: 2xx is delivered and the type's {@link ChannelType#extractRef} is stored; a partial
 * success (Apprise 424) is delivered with a warning and never retried; 429 honors
 * {@code Retry-After} and counts as an attempt at most once per 10 minutes; any other failure
 * counts an attempt and retries after 10 s, 1 min, 5 min, 30 min and 2 h, then fails for good and
 * raises a {@code system.delivery_failed} event (never sent to the failing channel). Errors are
 * redacted of the channel's secrets and cut to 500 characters.
 */
@Component
public class DeliveryWorker {

    /** The egress policy deciding private payloads (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.EgressPolicy egress;

    /** The map attachments of geolocated alerts (prompt 055); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AlertMapService alertMaps;


    private static final Logger log = LoggerFactory.getLogger(DeliveryWorker.class);

    /** The retry ladder after attempt 1, 2, 3, 4 and 5. */
    public static final List<Duration> LADDER = List.of(Duration.ofSeconds(10), Duration.ofMinutes(1),
            Duration.ofMinutes(5), Duration.ofMinutes(30), Duration.ofHours(2));

    /** Attempts after which a delivery fails for good. */
    public static final int MAX_ATTEMPTS = LADDER.size() + 1;

    /** A 429 counts as an attempt at most this often. */
    static final Duration RETRY_AFTER_COUNT_INTERVAL = Duration.ofMinutes(10);

    /** The wait after a 429 without a usable {@code Retry-After}. */
    static final Duration DEFAULT_RETRY_AFTER = Duration.ofSeconds(60);

    /** The longest {@code Retry-After} honored. */
    static final Duration MAX_RETRY_AFTER = Duration.ofHours(2);

    private final DeliveryStore deliveries;
    private final NotificationStore notifications;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final ChannelTypeRegistry types;
    private final ChannelSender sender;
    private final ChannelRateLimiter limiter;
    private final NotificationService service;
    private final NotificationRouter router;
    private final NotifyProperties properties;
    private final AlertStore alerts;
    private final List<FanOutDelivery> fanOuts;
    private final ActionMinter minter;
    private final InboxService inbox;
    private final Clock clock;

    /** Creates the worker. */
    public DeliveryWorker(DeliveryStore deliveries, NotificationStore notifications,
            NotificationChannelRepository channels, ChannelService channelService, ChannelTypeRegistry types,
            ChannelSender sender, ChannelRateLimiter limiter, NotificationService service, NotificationRouter router,
            NotifyProperties properties, AlertStore alerts, List<FanOutDelivery> fanOuts, ActionMinter minter,
            InboxService inbox, Clock clock) {
        this.minter = minter;
        this.inbox = inbox;
        this.deliveries = deliveries;
        this.notifications = notifications;
        this.channels = channels;
        this.channelService = channelService;
        this.types = types;
        this.sender = sender;
        this.limiter = limiter;
        this.service = service;
        this.router = router;
        this.properties = properties;
        this.alerts = alerts;
        this.fanOuts = fanOuts == null ? List.of() : List.copyOf(fanOuts);
        this.clock = clock;
    }

    /** The scheduled poll; does nothing when {@code foresignal.notify.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.notify.poll-interval:2s}",
            initialDelayString = "${foresignal.notify.poll-interval:2s}")
    public void poll() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            runOnce();
        } catch (RuntimeException e) {
            log.error("Notification delivery poll failed", e);
        }
    }

    /**
     * Claims due deliveries and processes them, returning when all are done.
     *
     * @return how many deliveries were claimed
     */
    public int runOnce() {
        Instant now = clock.instant();
        List<NotificationDeliveryEntity> claimed = deliveries.claim(now, properties.batchSize(),
                now.plus(properties.lease()));
        if (claimed.isEmpty()) {
            return 0;
        }
        Map<UUID, List<NotificationDeliveryEntity>> byChannel = new LinkedHashMap<>();
        for (NotificationDeliveryEntity d : claimed) {
            byChannel.computeIfAbsent(d.getChannelId(), k -> new ArrayList<>()).add(d);
        }
        AtomicInteger processed = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (Map.Entry<UUID, List<NotificationDeliveryEntity>> entry : byChannel.entrySet()) {
                futures.add(pool.submit(() -> processChannel(entry.getKey(), entry.getValue(), processed)));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Notification delivery failed unexpectedly", e);
                }
            }
        }
        return claimed.size();
    }

    private void processChannel(UUID channelId, List<NotificationDeliveryEntity> batch, AtomicInteger processed) {
        Optional<NotificationChannelEntity> channel = channels.findById(channelId);
        Set<Long> skip = new HashSet<>();
        if (channel.isPresent() && channel.get().isEnabled() && releasedTogether(batch)) {
            skip.addAll(deliveries.foldReleased(channelId, batch.stream().map(NotificationDeliveryEntity::getId).toList(),
                    clock.instant()));
        }
        for (NotificationDeliveryEntity d : batch) {
            if (skip.contains(d.getId())) {
                continue;
            }
            try {
                // Reload: folding may have turned it into a digest.
                NotificationDeliveryEntity current = deliveries.find(d.getId()).orElse(null);
                if (current != null) {
                    process(current, channel.orElse(null));
                }
            } catch (RuntimeException e) {
                log.error("Delivery {} failed unexpectedly; releasing it for a retry", d.getId(), e);
                deliveries.reschedule(d.getId(), clock.instant().plus(LADDER.getFirst()));
            }
            processed.incrementAndGet();
        }
    }

    private static boolean releasedTogether(List<NotificationDeliveryEntity> batch) {
        return batch.stream().anyMatch(d -> d.getDeferredReason() != null
                && DeferredReason.RELEASED_AS_DIGEST.contains(d.getDeferredReason())
                && "alert".equals(d.getEvent()));
    }

    /** Processes one claimed delivery. */
    void process(NotificationDeliveryEntity d, NotificationChannelEntity channel) {
        if (channel == null) {
            deliveries.fail(d.getId(), "channel deleted");
            return;
        }
        if (!channel.isEnabled()) {
            deliveries.fail(d.getId(), "channel disabled");
            return;
        }
        Optional<ChannelType> maybeType = types.find(channel.getType());
        if (maybeType.isEmpty()) {
            deliveries.fail(d.getId(), "channel type " + channel.getType() + " is not available");
            return;
        }
        ChannelType type = maybeType.get();
        NotificationMessage stored = notifications.find(d.getNotificationId()).orElse(null);
        if (stored == null) {
            deliveries.fail(d.getId(), "notification " + d.getNotificationId() + " is gone");
            return;
        }
        Instant now = clock.instant();
        ZoneId zone = channelService.zone(channel.getTimezone());
        if (DeferredReason.PRESENCE.equals(d.getDeferredReason()) && DeliveryKind.SEND.equals(d.getKind())) {
            Optional<String> why = presenceSuppression(stored, now);
            if (why.isPresent()) {
                deliveries.suppress(d.getId(), why.get(), now);
                return;
            }
        }
        NotificationMessage message = message(d, stored, channel, zone, now);
        Optional<Deferral.Decision> deferral = Deferral.check(message.severity(),
                QuietHours.fromMap(channel.getQuietHours()), zone, service.pause(), now);
        if (deferral.isPresent() && d.getEscalationStep() != null && "critical".equals(message.severity())
                && DeferredReason.QUIET_HOURS.equals(deferral.get().reason())) {
            // Quiet hours never hold back a critical escalation (prompt 033).
            deferral = Optional.empty();
        }
        FanOutDelivery fanOut = type.capabilities().fanOut() ? fanOutFor(channel.getType()) : null;
        boolean downgrade = false;
        if (deferral.isPresent() && fanOut != null && DeferredReason.QUIET_HOURS.equals(deferral.get().reason())) {
            String mode = QuietHoursMode.of(channel.getConfig());
            if (QuietHoursMode.DROP.equals(mode)) {
                deliveries.delivered(d.getId(), d.getKind(), null, false, "dropped during quiet hours", now);
                return;
            }
            if (QuietHoursMode.DOWNGRADE.equals(mode)) {
                downgrade = true;
                deferral = Optional.empty();
            }
        }
        if (deferral.isPresent()) {
            deliveries.defer(d.getId(), deferral.get().until(), deferral.get().reason());
            return;
        }
        if (alertMaps != null) {
            // Prompt 055: the alert's map, as an image or a link; held briefly while it is being made.
            AlertMapService.Outcome map = alertMaps.attach(d, stored, message, channel, type, now);
            if (map.pending()) {
                deliveries.reschedule(d.getId(), map.retryAt());
                return;
            }
            message = map.message();
        }
        if (fanOut != null) {
            fanOut.fanOut(d, message, channel, downgrade, now, minter.forDelivery(d, stored, channel));
            return;
        }
        if (type.capabilities().fanOut()) {
            deliveries.fail(d.getId(), "no fan-out handler for channel type " + channel.getType());
            return;
        }
        Duration wait = limiter.tryAcquire(channel.getId(), type.rateLimit());
        if (!wait.isZero()) {
            deliveries.reschedule(d.getId(), now.plus(wait));
            return;
        }
        List<NotificationAction> actions = minter.forDelivery(d, stored, channel);
        ChannelSettings settings = channelService.settingsFor(channel, d.getId(), now).withActions(actions);
        message = ActionMinter.withLinks(message, channel, actions);
        String kind = d.getKind();
        PreparedRequest request;
        boolean edited = false;
        boolean enrichmentEdit = DeliveryKind.UPDATE.equals(kind) && stored.updateOf() == null;
        if (enrichmentEdit) {
            String ref = originalRef(channel.getId(), stored.id());
            Optional<PreparedRequest> update = channel.isEditInPlace() && ref != null
                    ? type.renderUpdate(message, ref, settings) : Optional.empty();
            if (update.isEmpty()) {
                deliveries.delivered(d.getId(), kind, null, false,
                        "the original message cannot be edited; the summary was not sent separately", now);
                return;
            }
            request = update.get();
            edited = true;
        } else if (DeliveryKind.UPDATE.equals(kind) || DeliveryKind.FOLLOWUP.equals(kind)) {
            String ref = originalRef(channel.getId(), stored.updateOf());
            for (PreparedRequest pre : type.beforeUpdate(message, ref, settings)) {
                try {
                    ChannelResponse response = sender.send(pre);
                    if (!response.success()) {
                        log.warn("Pre-update request of delivery {} answered HTTP {}", d.getId(), response.status());
                    }
                } catch (IOException | RuntimeException e) {
                    log.warn("Pre-update request of delivery {} failed: {}", d.getId(),
                            Redactor.redact(e.getMessage(), settings.secrets().values()));
                }
            }
            Optional<PreparedRequest> update = DeliveryKind.UPDATE.equals(kind) && channel.isEditInPlace()
                    ? type.renderUpdate(message, ref, settings) : Optional.empty();
            if (update.isPresent()) {
                request = update.get();
                edited = true;
            } else {
                kind = DeliveryKind.FOLLOWUP;
                request = type.render(message, settings);
            }
        } else {
            request = type.render(message, settings);
        }
        ChannelResponse response;
        try {
            response = sender.send(request);
        } catch (IOException | RuntimeException e) {
            failed(d, channel, settings, e.getClass().getSimpleName() + ": " + e.getMessage(), now);
            return;
        }
        if (!response.success() && message.image() != null && type.attachmentRefused(response)) {
            // Prompt 055: the destination refused the image (ntfy 413, attachments disabled); send the text.
            NotificationMessage.Attachment image = message.image();
            NotificationMessage text = message.withAttachments(List.of());
            if (image.url() != null) {
                text = AlertMapService.withLink(text, image.url());
            }
            if (alertMaps != null) {
                alertMaps.refused(d);
            }
            message = text;
            try {
                response = sender.send(type.render(text, settings));
            } catch (IOException | RuntimeException e) {
                failed(d, channel, settings, e.getClass().getSimpleName() + ": " + e.getMessage(), now);
                return;
            }
        }
        if (response.success()) {
            deliveries.delivered(d.getId(), kind, type.extractRef(response).orElse(null), edited, null, now);
            sendFollowUps(d, type, message, settings);
        } else if (type.partialSuccess(response)) {
            deliveries.delivered(d.getId(), kind, type.extractRef(response).orElse(null), edited,
                    Redactor.error("warning: HTTP " + response.status() + " (some targets failed; not retried): "
                            + response.body(), settings.secrets().values()), now);
        } else if (response.status() == 429) {
            Duration after = retryAfter(response, now);
            String error = Redactor.error("HTTP 429: " + response.body(), settings.secrets().values());
            boolean[] exhausted = {false};
            deliveries.update(d.getId(), row -> {
                Instant counted = row.getRetryAfterCountedAt();
                if (counted == null || !counted.plus(RETRY_AFTER_COUNT_INTERVAL).isAfter(now)) {
                    row.setAttempts(row.getAttempts() + 1);
                    row.setRetryAfterCountedAt(now);
                }
                row.setLastError(error);
                row.setLeasedUntil(null);
                if (row.getAttempts() >= MAX_ATTEMPTS) {
                    row.setStatus(ai.foresignal.store.DeliveryStatus.FAILED);
                    row.setNextAttemptAt(null);
                    exhausted[0] = true;
                } else {
                    row.setNextAttemptAt(now.plus(after));
                }
            });
            if (exhausted[0]) {
                permanentlyFailed(d, channel, error);
            }
        } else {
            String status = response.status() >= 300 && response.status() < 400
                    ? "HTTP " + response.status() + " (redirect not followed)"
                    : "HTTP " + response.status();
            failed(d, channel, settings, status + ": " + response.body(), now);
        }
    }

    /** Sends a type's follow-up requests after a successful send (prompt 055); failures are only logged. */
    private void sendFollowUps(NotificationDeliveryEntity d, ChannelType type, NotificationMessage message,
            ChannelSettings settings) {
        for (PreparedRequest followUp : type.followUps(message, settings)) {
            try {
                ChannelResponse response = sender.send(followUp);
                if (!response.success()) {
                    log.warn("Follow-up request of delivery {} answered HTTP {}", d.getId(), response.status());
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Follow-up request of delivery {} failed: {}", d.getId(),
                        Redactor.redact(e.getMessage(), settings.secrets().values()));
            }
        }
    }

    /**
     * Why a presence-deferred send must not go out (prompt 033): its alert was acknowledged, resolved
     * or snoozed meanwhile, or the notification was read on a desktop; empty to send it.
     */
    Optional<String> presenceSuppression(NotificationMessage stored, Instant now) {
        try {
            if (stored.alertId() != null) {
                Optional<AlertView> alert = alerts.find(stored.alertId());
                if (alert.isPresent()) {
                    AlertView a = alert.get();
                    if (!"firing".equals(a.state())) {
                        return Optional.of("suppressed: alert " + a.state() + " while deferred for presence");
                    }
                    if (a.snoozedUntil() != null && a.snoozedUntil().isAfter(now)) {
                        return Optional.of("suppressed: alert snoozed while deferred for presence");
                    }
                }
            }
            if (stored.id() != null && inbox.readOnDesktop(stored.id())) {
                return Optional.of("suppressed: read on a desktop while deferred for presence");
            }
        } catch (RuntimeException e) {
            log.warn("Could not check presence suppression of notification {}; sending it", stored.id(), e);
        }
        return Optional.empty();
    }

    private FanOutDelivery fanOutFor(String channelType) {
        for (FanOutDelivery handler : fanOuts) {
            if (handler.handles(channelType)) {
                return handler;
            }
        }
        return null;
    }

    /** The message to send for {@code d}: a digest, a fired alert with the channel's templates, or as stored. */
    NotificationMessage message(NotificationDeliveryEntity d, NotificationMessage stored, NotificationChannelEntity channel,
            ZoneId zone, Instant now) {
        if (DeliveryKind.DIGEST.equals(d.getKind())) {
            List<NotificationMessage> items = new ArrayList<>();
            items.add(withTemplates(stored, channel, zone, now));
            for (NotificationDeliveryEntity folded : deliveries.foldedInto(d.getId())) {
                notifications.find(folded.getNotificationId())
                        .ifPresent(m -> items.add(withTemplates(m, channel, zone, now)));
            }
            if (items.size() > 1) {
                return DigestBuilder.build(items, Math.max(1, channel.getBacklogCap()));
            }
            return items.getFirst();
        }
        return withTemplates(stored, channel, zone, now);
    }

    private NotificationMessage withTemplates(NotificationMessage m, NotificationChannelEntity channel, ZoneId zone,
            Instant now) {
        if (!NotificationMessage.ALERT.equals(m.event()) || !"alert.fired".equals(m.subtype())) {
            return privacy(m, channel);
        }
        Map<String, Object> data = withLatestSummary(m);
        AlertTemplates.Text text = AlertTemplates.render(channel.getTitleTemplate(), channel.getBodyTemplate(), data,
                m.url(), zone, now, ChannelUnits.imperial(channel.getConfig()));
        return privacy(m.withText(text.title(), text.body()), channel);
    }

    /**
     * {@code m} as {@code channel} may receive it (prompt 039): a message about a private-source record
     * carries only the rule name, severity and link unless the channel has {@code privatePayload: true};
     * a fan-out push type (Web Push, APNs, FCM) never carries it.
     */
    NotificationMessage privacy(NotificationMessage m, NotificationChannelEntity channel) {
        String sourceId = PrivatePayloads.sourceId(m);
        if (sourceId == null || egress == null) {
            return m;
        }
        boolean push = types.find(channel.getType()).map(t -> t.capabilities().fanOut()).orElse(false);
        boolean allowed = push ? egress.allowsPushPayload(sourceId)
                : egress.allowsNotificationPayload(sourceId, PrivatePayloads.optedIn(channel.getConfig()));
        return allowed ? m : PrivatePayloads.redact(m);
    }

    /**
     * The notification's alert JSON with the alert's current enrichment summary (prompt 030), read at
     * send time; the stored copy when the alert has none or is gone.
     */
    Map<String, Object> withLatestSummary(NotificationMessage m) {
        if (m.alertId() == null) {
            return m.data();
        }
        Optional<AlertView> alert;
        try {
            alert = alerts.find(m.alertId());
        } catch (RuntimeException e) {
            log.warn("Could not read alert {} for its summary: {}", m.alertId(), e.getMessage());
            return m.data();
        }
        if (alert.isEmpty() || alert.get().summary() == null) {
            return m.data();
        }
        Map<String, Object> data = m.data() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(m.data());
        data.put("summary", alert.get().summary());
        return data;
    }

    private String originalRef(UUID channelId, Long originalNotificationId) {
        if (originalNotificationId == null) {
            return null;
        }
        return deliveries.deliveryOf(channelId, originalNotificationId)
                .filter(d -> d.getFoldedInto() == null)
                .map(NotificationDeliveryEntity::getProviderMessageRef)
                .orElse(null);
    }

    private void failed(NotificationDeliveryEntity d, NotificationChannelEntity channel, ChannelSettings settings,
            String error, Instant now) {
        String redacted = Redactor.error(error, settings.secrets().values());
        boolean[] exhausted = {false};
        deliveries.update(d.getId(), row -> {
            row.setAttempts(row.getAttempts() + 1);
            row.setLastError(redacted);
            row.setLeasedUntil(null);
            if (row.getAttempts() >= MAX_ATTEMPTS) {
                row.setStatus(ai.foresignal.store.DeliveryStatus.FAILED);
                row.setNextAttemptAt(null);
                exhausted[0] = true;
            } else {
                row.setNextAttemptAt(now.plus(LADDER.get(row.getAttempts() - 1)));
            }
        });
        if (exhausted[0]) {
            permanentlyFailed(d, channel, redacted);
        }
    }

    private void permanentlyFailed(NotificationDeliveryEntity d, NotificationChannelEntity channel, String error) {
        if (DeliveryKind.DIGEST.equals(d.getKind())) {
            deliveries.failFolded(d.getId(), "digest delivery " + d.getId() + " failed");
        }
        router.deliveryFailed(channel, d, error);
    }

    /** The {@code Retry-After} wait (seconds or an HTTP date), else 60 s, at most 2 h, at least 1 s. */
    static Duration retryAfter(ChannelResponse response, Instant now) {
        Optional<String> header = response.header("Retry-After");
        Duration out = DEFAULT_RETRY_AFTER;
        if (header.isPresent()) {
            String value = header.get().strip();
            try {
                out = Duration.ofMillis((long) (Double.parseDouble(value) * 1000));
            } catch (NumberFormatException e) {
                try {
                    out = Duration.between(now, DateTimeFormatter.RFC_1123_DATE_TIME.parse(value, Instant::from));
                } catch (DateTimeParseException ignored) {
                    out = DEFAULT_RETRY_AFTER;
                }
            }
        }
        if (out.compareTo(Duration.ofSeconds(1)) < 0) {
            out = Duration.ofSeconds(1);
        }
        return out.compareTo(MAX_RETRY_AFTER) > 0 ? MAX_RETRY_AFTER : out;
    }
}
