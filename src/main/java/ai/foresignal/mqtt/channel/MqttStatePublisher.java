package ai.foresignal.mqtt.channel;

import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.stats.glance.GlanceService;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * The retained state topics of every {@code mqtt} channel (prompt 060). These are not deliveries: they are
 * published straight on the channel's connection, and all of them again after every reconnect.
 *
 * <ul>
 *   <li>{@code {prefix}/rules/{ruleId}/state} {@code {state, alertId, severity, lastFiredAt}}: {@code firing}
 *       while the rule has an unresolved alert, {@code resolved} once all are resolved, {@code idle} before
 *       its first alert. Published when an alert fires, is acknowledged or resolves; a rule that is deleted
 *       gets an empty retained payload, which clears the topic.
 *   <li>{@code {prefix}/sources/{sourceId}/health} {@code {status, reason, lastSuccessAt, lastDataAt}} on
 *       {@code SourceHealthChangedEvent} and every 10 minutes; private sources are never published. Off with
 *       {@code publishHealth: false}.
 *   <li>{@code {prefix}/summary}: the full {@code GlanceService} view (the channel is admin-configured),
 *       checked every {@code summaryInterval} and published only when its content changed. The ETag ignores
 *       {@code serverTime}, which changes on every read. Off with {@code publishSummary: false}.
 * </ul>
 *
 * <p>Suppressed alerts (recorded under a snooze) change no rule state.
 */
@Component
public class MqttStatePublisher implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(MqttStatePublisher.class);

    /** The rule states. */
    public static final String FIRING = "firing";
    /** A rule whose alerts are all resolved. */
    public static final String RESOLVED = "resolved";
    /** A rule without alerts. */
    public static final String IDLE = "idle";

    /** How long a state publish waits for its PUBACK before the next one. */
    static final long PUBLISH_WAIT_MS = 5_000;

    private final MqttChannelRegistry registry;
    private final AlertBus bus;
    private final AlertStore alerts;
    private final TriggerRuleRepository rules;
    private final HealthService health;
    private final EgressPolicy egress;
    private final GlanceService glance;
    private final MqttProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private volatile Subscription subscription;

    /** Creates the publisher. */
    public MqttStatePublisher(MqttChannelRegistry registry, AlertBus bus, AlertStore alerts,
            TriggerRuleRepository rules, HealthService health, EgressPolicy egress, GlanceService glance,
            MqttProperties properties, JsonMapper json, Clock clock) {
        this.registry = registry;
        this.bus = bus;
        this.alerts = alerts;
        this.rules = rules;
        this.health = health;
        this.egress = egress;
        this.glance = glance;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** Subscribes to the alert bus before the trigger engine starts. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("mqtt-state", this, AlertBus.DEFAULT_QUEUE);
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
        // Exercise alerts (prompt 115) never change the published real state.
        if (event.suppressed() || event.alert().ruleId() == null || event.alert().exercise()) {
            return;
        }
        switch (event.type()) {
            case AlertEvent.FIRED, AlertEvent.ACKNOWLEDGED, AlertEvent.RESOLVED -> {
                List<MqttChannelRuntime> runtimes = registry.all();
                if (runtimes.isEmpty()) {
                    return;
                }
                Map<String, Object> state = ruleState(event.alert().ruleId());
                for (MqttChannelRuntime runtime : runtimes) {
                    publishRuleState(runtime, event.alert().ruleId(), state);
                }
            }
            default -> { }
        }
    }

    @Override
    public void onGap() {
        log.warn("The MQTT state publisher missed alert events; rule states are refreshed on the next reconnect"
                + " or republish");
    }

    /** Clears the retained state of deleted rules and publishes {@code idle} for new ones. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onRulesChanged(TriggerRulesChangedEvent event) {
        List<MqttChannelRuntime> runtimes = registry.all();
        if (runtimes.isEmpty()) {
            return;
        }
        Set<UUID> current = new HashSet<>();
        for (TriggerRuleEntity rule : rules.findAll()) {
            current.add(rule.getId());
        }
        for (MqttChannelRuntime runtime : runtimes) {
            reconcileRules(runtime, current);
        }
    }

    /** Publishes the health of a source that changed status, unless it is private. */
    @EventListener
    public void onSourceHealthChanged(SourceHealthChangedEvent event) {
        if (egress.isPrivate(event.sourceId())) {
            return;
        }
        for (MqttChannelRuntime runtime : registry.all()) {
            if (runtime.config().publishHealth()) {
                try {
                    publishHealth(runtime, health.health(event.sourceId()));
                } catch (RuntimeException e) {
                    log.debug("Health of {} not published: {}", event.sourceId(), e.getMessage());
                }
            }
        }
    }

    /** The 10-minute full health refresh. */
    @Scheduled(fixedDelayString = "${foresignal.mqtt.health-refresh:10m}",
            initialDelayString = "${foresignal.mqtt.health-refresh:10m}")
    public void scheduledHealthRefresh() {
        if (properties.scheduled()) {
            refreshHealth();
        }
    }

    /** The summary check, every 5 s; each channel publishes when its interval passed and the content changed. */
    @Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public void scheduledSummaries() {
        if (properties.scheduled()) {
            publishSummaries();
        }
    }

    /** Publishes every non-private source's health on every channel with {@code publishHealth}. */
    public void refreshHealth() {
        for (MqttChannelRuntime runtime : registry.all()) {
            if (runtime.config().publishHealth() && runtime.connection().connected()) {
                publishAllHealth(runtime);
            }
        }
    }

    /** Publishes the summary of every channel whose interval passed, when it changed. */
    public void publishSummaries() {
        Instant now = clock.instant();
        for (MqttChannelRuntime runtime : registry.all()) {
            if (runtime.config().publishSummary() && runtime.connection().connected()
                    && !now.isBefore(runtime.nextSummaryAt())) {
                runtime.nextSummaryAt(now.plus(runtime.config().summaryInterval()));
                publishSummary(runtime, false);
            }
        }
    }

    /**
     * Re-publishes every retained state of {@code runtime}: all rule states, the health of every non-private
     * source (with {@code publishHealth}) and the summary (with {@code publishSummary}).
     */
    public void republishAll(MqttChannelRuntime runtime) {
        Set<UUID> current = new HashSet<>();
        List<TriggerRuleEntity> all = rules.findAll();
        Map<UUID, AlertStore.RuleStats> stats = alerts.stats(all.stream().map(TriggerRuleEntity::getId).toList());
        for (TriggerRuleEntity rule : all) {
            current.add(rule.getId());
            publishRuleState(runtime, rule.getId(), ruleState(rule.getId(), stats.get(rule.getId())));
        }
        reconcileRules(runtime, current);
        if (runtime.config().publishHealth()) {
            publishAllHealth(runtime);
        }
        if (runtime.config().publishSummary()) {
            runtime.nextSummaryAt(clock.instant().plus(runtime.config().summaryInterval()));
            publishSummary(runtime, true);
        }
    }

    /** The current state payload of {@code ruleId}. */
    public Map<String, Object> ruleState(UUID ruleId) {
        return ruleState(ruleId, alerts.stats(List.of(ruleId)).get(ruleId));
    }

    private Map<String, Object> ruleState(UUID ruleId, AlertStore.RuleStats stats) {
        Optional<AlertView> latest = Optional.empty();
        if (stats != null && stats.alertCount() > 0) {
            AlertQuery query = new AlertQuery(Set.of(ruleId), null, null, null, null, null, null, null, null, false,
                    null);
            latest = alerts.list(query, null, 1).stream().findFirst();
        }
        return ruleStatePayload(stats == null ? 0 : stats.alertCount(), stats == null ? 0 : stats.openAlertCount(),
                latest.orElse(null), stats == null ? null : stats.lastFiredAt());
    }

    /**
     * {@code {state, alertId, severity, lastFiredAt}}: {@code firing} with unresolved alerts, {@code resolved}
     * when every alert is resolved, {@code idle} without alerts.
     */
    static Map<String, Object> ruleStatePayload(long alertCount, long openCount, AlertView latest, Instant lastFiredAt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("state", alertCount == 0 ? IDLE : openCount > 0 ? FIRING : RESOLVED);
        out.put("alertId", latest == null ? null : latest.id());
        out.put("severity", latest == null ? null : latest.severity());
        out.put("lastFiredAt", lastFiredAt == null ? null : lastFiredAt.toString());
        return out;
    }

    /** {@code {status, reason, lastSuccessAt, lastDataAt}}. */
    static Map<String, Object> healthPayload(SourceHealthView view) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", view.status() == null ? null : view.status().value());
        out.put("reason", view.statusReason());
        out.put("lastSuccessAt", view.lastSuccessAt() == null ? null : view.lastSuccessAt().toString());
        out.put("lastDataAt", view.lastDataAt() == null ? null : view.lastDataAt().toString());
        return out;
    }

    /**
     * The ETag of a glance JSON without its {@code serverTime}, so an unchanged glance is not re-published.
     */
    @SuppressWarnings("unchecked")
    String summaryEtag(String glanceJson) {
        try {
            Map<String, Object> map = new LinkedHashMap<>(json.readValue(glanceJson, Map.class));
            map.remove("serverTime");
            return ETags.forDocument(json.writeValueAsString(map));
        } catch (RuntimeException e) {
            return ETags.forDocument(glanceJson);
        }
    }

    private void reconcileRules(MqttChannelRuntime runtime, Set<UUID> current) {
        for (UUID published : Set.copyOf(runtime.publishedRules())) {
            if (!current.contains(published)) {
                String topic = MqttTopics.ruleState(runtime.config().topicPrefix(), published);
                if (send(runtime, new MqttMessage(topic, new byte[0], 1, true, null, null))) {
                    runtime.publishedRules().remove(published);
                }
            }
        }
        for (UUID id : current) {
            if (!runtime.publishedRules().contains(id)) {
                publishRuleState(runtime, id, ruleState(id));
            }
        }
    }

    private void publishRuleState(MqttChannelRuntime runtime, UUID ruleId, Map<String, Object> state) {
        String topic = MqttTopics.ruleState(runtime.config().topicPrefix(), ruleId);
        if (send(runtime, MqttMessage.retained(topic, json.writeValueAsString(state), MqttMessage.JSON))) {
            runtime.publishedRules().add(ruleId);
        }
    }

    private void publishAllHealth(MqttChannelRuntime runtime) {
        try {
            for (SourceHealthView view : health.all(Optional.empty())) {
                if (!egress.isPrivate(view.sourceId())) {
                    publishHealth(runtime, view);
                }
            }
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: health refresh failed: {}", runtime.channelId(), e.getMessage());
        }
    }

    private void publishHealth(MqttChannelRuntime runtime, SourceHealthView view) {
        String topic = MqttTopics.sourceHealth(runtime.config().topicPrefix(), view.sourceId());
        send(runtime, MqttMessage.retained(topic, json.writeValueAsString(healthPayload(view)), MqttMessage.JSON));
    }

    private void publishSummary(MqttChannelRuntime runtime, boolean force) {
        String body;
        try {
            ReadCaller admin = new ReadCaller(Scope.ADMIN, true, false, ReadRestriction.NONE, true);
            body = glance.json(new GlanceService.Request(admin, Set.of(), null));
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: summary unavailable: {}", runtime.channelId(), e.getMessage());
            return;
        }
        String etag = summaryEtag(body);
        if (!force && etag.equals(runtime.summaryEtag())) {
            return;
        }
        if (send(runtime, MqttMessage.retained(MqttTopics.summary(runtime.config().topicPrefix()), body,
                MqttMessage.JSON))) {
            runtime.summaryEtag(etag);
        }
    }

    /** Publishes and waits briefly; false when it did not go out (logged at debug). */
    private boolean send(MqttChannelRuntime runtime, MqttMessage message) {
        if (!runtime.connection().connected()) {
            return false;
        }
        CompletableFuture<Void> sent = runtime.publish(message);
        try {
            sent.get(PUBLISH_WAIT_MS, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.debug("MQTT channel {}: {} not published: {}", runtime.channelId(), message.topic(), e.getMessage());
            return false;
        }
    }
}
