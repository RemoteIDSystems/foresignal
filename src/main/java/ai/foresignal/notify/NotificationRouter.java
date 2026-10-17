package ai.foresignal.notify;

import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.RuleStormEvent;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns alert lifecycle events and system events into notifications (prompt 018).
 *
 * <ul>
 *   <li><b>Alerts</b> ({@link AlertBus}, events flagged {@code suppressed} skipped): {@code fired}
 *       becomes {@code alert.fired} (persisted but routed to no channel while the rule is muted,
 *       prompt 033); {@code acknowledged}, {@code snoozed} and {@code resolved}
 *       become {@code alert.acknowledged}, {@code alert.snoozed} and {@code alert.resolved} updates of
 *       the fired notification, whose text shows the new state ("✅ Resolved 14:02 CDT — …", the old
 *       body struck through). {@code updated} (feedback, unsnooze) notifies nobody. {@code enriched}
 *       (prompt 030) adds the summary to the stored notification, brings sends waiting for it forward
 *       and edits messages already sent where the channel can ({@link EnrichmentDeliveries}).
 *   <li><b>System events:</b> a source turning {@code failing} ({@code system.source_failing}), a
 *       rule hitting its storm cap ({@code system.rule_storm}) and a delivery failing for good
 *       ({@code system.delivery_failed}, never sent to the channel that failed, and not routed at all
 *       when the failed message was itself a delivery failure, so two broken channels cannot ping-pong).
 * </ul>
 *
 * <p>Every message is persisted whether or not a channel takes it. Links point at
 * {@code <publicBaseUrl>/v1/alerts/{id}}; the base URL is the {@code server} setting's
 * {@code publicBaseUrl}, default {@value #DEFAULT_BASE_URL}. The stored default text uses the server
 * timezone; the worker re-renders fired alerts with each channel's templates and timezone.
 *
 * <p>A bus gap (the listener's queue overflowed) loses those events for notification purposes; it
 * is logged, and the alerts stay visible in the API and the alert stream.
 */
@Component
public class NotificationRouter implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationRouter.class);

    /** The base URL of links when the {@code server} setting has none. */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm zzz", Locale.US);

    private final AlertBus bus;
    private final NotificationService service;
    private final NotificationStore store;
    private final ChannelService channels;
    private final TriggerRuleRepository rules;
    private final EnrichmentDeliveries enrichment;
    private final JsonMapper json;
    private final Clock clock;
    private volatile Subscription subscription;

    /** The map attachments of geolocated alerts (prompt 055); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AlertMapService alertMaps;

    /** Creates the router. */
    public NotificationRouter(AlertBus bus, NotificationService service, NotificationStore store,
            ChannelService channels, TriggerRuleRepository rules, EnrichmentDeliveries enrichment, JsonMapper json,
            Clock clock) {
        this.bus = bus;
        this.service = service;
        this.store = store;
        this.channels = channels;
        this.rules = rules;
        this.enrichment = enrichment;
        this.json = json;
        this.clock = clock;
    }

    /** Subscribes to the alert bus before the trigger engine starts. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("notify", this, AlertBus.DEFAULT_QUEUE);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @Override
    public void onAlert(AlertEvent event) {
        if (event.suppressed()) {
            return;
        }
        switch (event.type()) {
            case AlertEvent.FIRED -> fired(event.alert());
            case AlertEvent.ACKNOWLEDGED, AlertEvent.SNOOZED, AlertEvent.RESOLVED -> lifecycle(event.type(), event.alert());
            case AlertEvent.ENRICHED -> enriched(event.alert());
            default -> { }
        }
    }

    @Override
    public void onGap() {
        log.warn("The notification router missed alert events (its queue overflowed); those alerts were not notified");
    }

    /** Publishes {@code alert.fired}. */
    public NotificationMessage fired(AlertView alert) {
        Map<String, Object> data = data(alert);
        String url = alertUrl(alert.id());
        ZoneId zone = channels.zone(null);
        // Prompt 056: the stored notification keeps the weather summary line with the message.
        AlertTemplates.Text text = AlertTemplates.render(null, null, data, url, zone, clock.instant(), false, true);
        NotificationMessage message = NotificationMessage.builder(NotificationMessage.ALERT, "alert.fired", text.title(),
                        alert.severity())
                .shortTitle(alert.ruleName())
                .body(text.body())
                .url(url)
                .links(alertLinks(alert.id(), url))
                .data(data)
                .tags(alert.tags())
                .alertId(alert.id())
                .ruleId(alert.ruleId())
                .exercise(alert.exercise())
                .build();
        if (alert.exercise()) {
            // Prompt 115: an exercise alert is marked and routed only to channels that opted in or were named in
            // its run; the rule's mute (real routing state) does not apply to a rehearsal.
            java.util.List<String> named = ai.foresignal.exercise.ExerciseMarking.namedChannels(alert, json);
            NotificationMessage persisted = service.publish(ai.foresignal.exercise.ExerciseMarking.mark(message),
                    c -> ai.foresignal.exercise.ExerciseMarking.routes(c, named));
            if (alertMaps != null) {
                alertMaps.prepare(alert, persisted);
            }
            return persisted;
        }
        // A muted rule's alerts are recorded and kept in the inbox, but not routed (prompt 033).
        boolean muted = alert.ruleId() != null
                && rules.findById(alert.ruleId()).map(r -> r.mutedAt(clock.instant())).orElse(false);
        // Prompt 125: an indicator's watch rule (tag origin-hypothesis) routes to no channel by default; only a
        // channel that names the rule in its ruleIds receives its alerts. They stay in the inbox and on the streams.
        boolean indicatorWatch = alert.tags().contains(ai.foresignal.hypothesis.IndicatorWatchService.ORIGIN_TAG);
        java.util.function.Predicate<ai.foresignal.notify.NotificationChannel> filter = muted ? c -> false
                : indicatorWatch ? c -> c.ruleIds() != null && c.ruleIds().contains(alert.ruleId()) : null;
        NotificationMessage persisted = service.publish(message, filter);
        if (!muted && alertMaps != null) {
            // Prompt 055: start the alert's map now, concurrently with the enrichment wait.
            alertMaps.prepare(alert, persisted);
        }
        return persisted;
    }

    /**
     * Applies an enrichment summary (prompt 030): the stored {@code alert.fired} notification gets the
     * summary, waiting sends become due and messages already sent are edited where possible.
     */
    public EnrichmentDeliveries.Result enriched(AlertView alert) {
        Map<String, Object> data = data(alert);
        String url = alertUrl(alert.id());
        AlertTemplates.Text text = AlertTemplates.render(null, null, data, url, channels.zone(null), clock.instant(),
                false, true);
        return enrichment.summaryArrived(alert, data, text.title(), text.body());
    }

    /** Publishes {@code alert.<type>} as an update of the fired notification. */
    public NotificationMessage lifecycle(String type, AlertView alert) {
        Optional<NotificationMessage> original = store.fired(alert.id());
        Map<String, Object> data = data(alert);
        String url = alertUrl(alert.id());
        ZoneId zone = channels.zone(null);
        String baseTitle;
        String baseBody;
        if (original.isPresent()) {
            baseTitle = original.get().title();
            baseBody = original.get().bodyMarkdown();
        } else {
            AlertTemplates.Text text = AlertTemplates.render(null, null, data, url, zone, clock.instant());
            baseTitle = text.title();
            baseBody = text.body();
        }
        Instant now = clock.instant();
        String title;
        String body;
        switch (type) {
            case AlertEvent.RESOLVED -> {
                Instant at = alert.resolvedAt() != null ? alert.resolvedAt() : now;
                title = "✅ Resolved " + CLOCK.format(at.atZone(zone)) + " — " + baseTitle;
                body = "Resolved" + by(alert.resolvedBy()) + ".\n\n" + strike(baseBody);
            }
            case AlertEvent.ACKNOWLEDGED -> {
                Instant at = alert.acknowledgedAt() != null ? alert.acknowledgedAt() : now;
                title = "Acknowledged " + CLOCK.format(at.atZone(zone)) + " — " + baseTitle;
                body = "Acknowledged" + by(alert.acknowledgedBy()) + ".\n\n" + (baseBody == null ? "" : baseBody);
            }
            default -> {
                String until = alert.snoozedUntil() == null ? "" : " until " + CLOCK.format(alert.snoozedUntil().atZone(zone));
                title = "Snoozed" + until + " — " + baseTitle;
                body = "Snoozed" + until + ".\n\n" + (baseBody == null ? "" : baseBody);
            }
        }
        NotificationMessage message = NotificationMessage.builder(NotificationMessage.ALERT, "alert." + type, title,
                        alert.severity())
                .shortTitle(type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1) + ": "
                        + (alert.ruleName() == null ? "alert" : alert.ruleName()))
                .body(AlertTemplates.tidy(body))
                .url(url)
                .links(alertLinks(alert.id(), url))
                .data(data)
                .tags(alert.tags())
                .alertId(alert.id())
                .ruleId(alert.ruleId())
                .updateOf(original.map(NotificationMessage::id).orElse(null))
                .exercise(alert.exercise())
                .build();
        return service.publishUpdate(ai.foresignal.exercise.ExerciseMarking.mark(message));
    }

    /** {@code system.source_failing} when a source turns failing. */
    @EventListener
    public void onSourceHealthChanged(SourceHealthChangedEvent event) {
        if (event.current() != HealthStatus.FAILING || event.previous() == HealthStatus.FAILING) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sourceId", event.sourceId());
        data.put("previous", event.previous().value());
        data.put("status", event.current().value());
        data.put("reason", event.reason());
        String url = baseUrl() + "/v1/sources/" + event.sourceId() + "/health";
        system("system.source_failing", "warning", "Source " + event.sourceId() + " is failing",
                event.reason() == null ? "The source is failing." : MarkdownRenderer.escapeMarkdown(event.reason()),
                url, data, null);
    }

    /** {@code system.rule_storm} when a rule hits its storm cap. */
    @EventListener
    public void onRuleStorm(RuleStormEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ruleId", event.ruleId().toString());
        data.put("ruleName", event.ruleName());
        data.put("maxAlertsPerHour", event.maxAlertsPerHour());
        data.put("suppressedCount", event.suppressedCount());
        data.put("disabled", event.disabled());
        String body = "The rule exceeded " + event.maxAlertsPerHour() + " alerts per hour; further matches are dropped"
                + (event.disabled() ? " and the rule was disabled." : " until the hour passes.");
        system("system.rule_storm", "warning", "Rule " + event.ruleName() + " hit its storm cap", body,
                baseUrl() + "/v1/triggers/" + event.ruleId(), data, null);
    }

    /** A {@link SystemEvent} from another component (prompt 019: retention lag, canary failure). */
    @EventListener
    public void onSystemEvent(SystemEvent event) {
        String url = event.path() == null ? null : baseUrl() + event.path();
        system(event.subtype(), event.severity(), event.title(), event.body(), url, new LinkedHashMap<>(event.data()),
                null);
    }

    /** {@code system.delivery_failed} after a delivery's last attempt, never sent to that channel. */
    public void deliveryFailed(NotificationChannelEntity channel, NotificationDeliveryEntity delivery, String error) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channelId", channel.getId().toString());
        data.put("channelName", channel.getName());
        data.put("deliveryId", delivery.getId());
        data.put("notificationId", delivery.getNotificationId());
        data.put("error", error);
        boolean cascade = store.find(delivery.getNotificationId())
                .map(m -> "system.delivery_failed".equals(m.subtype())).orElse(false);
        UUID failing = channel.getId();
        system("system.delivery_failed", "warning", "Delivery to " + channel.getName() + " failed",
                "Delivery " + delivery.getId() + " gave up after its last retry"
                        + (error == null ? "." : ": " + MarkdownRenderer.escapeMarkdown(error)),
                baseUrl() + "/v1/channels/" + failing + "/deliveries", data,
                cascade ? c -> false : c -> !c.id().equals(failing));
    }

    /**
     * {@code system.push_provider_failed} when a native push provider's credentials stop working
     * (prompt 032). Never routed to the built-in Push channel, which may be the very path that is
     * failing.
     *
     * @param providerId the provider
     * @param type {@code apns} or {@code fcm}
     * @param error a short code-level description (no key, device token or alert text)
     */
    public void pushProviderFailed(String providerId, String type, String error) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("providerId", providerId);
        data.put("type", type);
        data.put("error", error);
        system("system.push_provider_failed", "warning", "Push provider " + providerId + " is failing",
                "Native push through " + MarkdownRenderer.escapeMarkdown(type) + " provider "
                        + MarkdownRenderer.escapeMarkdown(providerId) + " failed"
                        + (error == null ? "." : ": " + MarkdownRenderer.escapeMarkdown(error))
                        + " Check its key at /v1/push/providers.",
                baseUrl() + "/v1/push/providers", data,
                c -> !ai.foresignal.notify.push.PushChannelType.ID.equals(c.type()));
    }

    private void system(String subtype, String severity, String title, String body, String url,
            Map<String, Object> data, java.util.function.Predicate<NotificationChannel> filter) {
        try {
            service.publish(NotificationMessage.builder(NotificationMessage.SYSTEM, subtype, title, severity)
                    .body(body).url(url).data(data).build(), filter);
        } catch (RuntimeException e) {
            log.error("Could not publish system notification {}", subtype, e);
        }
    }

    /** The alert JSON stored as the notification's {@code data}. */
    Map<String, Object> data(AlertView a) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", a.id());
        data.put("ruleId", a.ruleId() == null ? null : a.ruleId().toString());
        data.put("ruleName", a.ruleName());
        data.put("sourceId", a.sourceId());
        data.put("externalId", a.externalId());
        data.put("kind", a.kind());
        data.put("seq", a.seq());
        data.put("firedAt", a.firedAt() == null ? null : a.firedAt().toString());
        data.put("observedAt", a.observedAt() == null ? null : a.observedAt().toString());
        data.put("message", a.message());
        data.put("severity", a.severity());
        data.put("state", a.state());
        data.put("tags", a.tags());
        data.put("acknowledgedAt", a.acknowledgedAt() == null ? null : a.acknowledgedAt().toString());
        data.put("acknowledgedBy", a.acknowledgedBy());
        data.put("resolvedAt", a.resolvedAt() == null ? null : a.resolvedAt().toString());
        data.put("resolvedBy", a.resolvedBy());
        data.put("snoozedUntil", a.snoozedUntil() == null ? null : a.snoozedUntil().toString());
        data.put("enrich", a.ruleId() != null && rules.findById(a.ruleId()).map(r -> r.isEnrich()).orElse(false));
        if (a.summary() != null) {
            data.put("summary", a.summary());
            data.put("enrichedAt", a.enrichedAt() == null ? null : a.enrichedAt().toString());
            Object citations = null;
            if (a.summaryCitations() != null) {
                try {
                    citations = json.readValue(a.summaryCitations(), List.class);
                } catch (RuntimeException e) {
                    citations = null;
                }
            }
            data.put("summaryCitations", citations);
        }
        Object record = null;
        if (a.recordSnapshot() != null) {
            try {
                record = json.readValue(a.recordSnapshot(), Map.class);
            } catch (RuntimeException e) {
                record = null;
            }
        }
        data.put("record", record);
        // The region tags at fire time (prompt 040), for the {{near.*}}, {{country}} … placeholders.
        Object region = null;
        if (a.region() != null) {
            try {
                region = json.readValue(a.region(), Map.class);
            } catch (RuntimeException e) {
                region = null;
            }
        }
        data.put("region", region);
        // The computed alert context (prompts 036 and 056), for {{context.weather.*}} and webhook consumers.
        Object context = null;
        if (a.context() != null) {
            try {
                context = json.readValue(a.context(), Map.class);
            } catch (RuntimeException e) {
                context = null;
            }
        }
        data.put("context", context);
        if (a.exercise()) {
            // Prompt 115: the replayed window for the exercise footer; enrichment only when the run allows it.
            data.put(ai.foresignal.exercise.ExerciseMarking.DATA_MEMBER,
                    ai.foresignal.exercise.ExerciseMarking.dataMember(a, json));
            data.put("enrich", Boolean.TRUE.equals(data.get("enrich"))
                    && ai.foresignal.exercise.ExerciseMarking.enrichAllowed(a, json));
        }
        return data;
    }

    /**
     * The links of an alert message: "Open alert" to the API URL, plus "Open in app" from the
     * {@code server} setting's {@code clientLinks.alert} template when the operator has set
     * {@code clientLinks} (prompt 023).
     */
    List<NotificationMessage.Link> alertLinks(long id, String url) {
        ServerSettings server = channels.serverSettings();
        if (server == null || server.clientLinks() == null) {
            return List.of(new NotificationMessage.Link("Open alert", url));
        }
        String app = ClientLinks.fill(server.effectiveClientLinks().alert(), Map.of("id", Long.toString(id)));
        return List.of(new NotificationMessage.Link("Open alert", url), new NotificationMessage.Link("Open in app", app));
    }

    private String alertUrl(long id) {
        return baseUrl() + "/v1/alerts/" + id;
    }

    /** The public base URL of links. */
    String baseUrl() {
        ServerSettings server = channels.serverSettings();
        String base = server.normalizedPublicBaseUrl();
        return base == null ? DEFAULT_BASE_URL : base;
    }

    private static String by(String actor) {
        return actor == null || actor.isBlank() ? "" : " by " + MarkdownRenderer.escapeMarkdown(actor);
    }

    /** Strikes every non-blank line of a markdown body through (bullets keep their marker). */
    static String strike(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String line : body.split("\n", -1)) {
            if (out.length() > 0) {
                out.append('\n');
            }
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("```")) {
                out.append(line);
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                out.append("- ~~").append(trimmed.substring(2)).append("~~");
            } else {
                out.append("~~").append(trimmed).append("~~");
            }
        }
        return out.toString();
    }
}
