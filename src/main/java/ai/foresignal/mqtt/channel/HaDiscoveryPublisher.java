package ai.foresignal.mqtt.channel;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.visibility.SourceBecamePrivateEvent;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes Home Assistant MQTT discovery for {@code mqtt} channels with {@code haDiscovery: true}
 * (prompt 060); the payload is {@link HaDiscoveryPayloads}.
 *
 * <ul>
 *   <li><b>When:</b> on every connect; when Home Assistant's birth message {@code online} arrives on
 *       {@code {haPrefix}/status} (subscribed), after a random 0–5 s delay so many devices do not answer at
 *       once; and 5 s after the last rule or source change (debounced).
 *   <li><b>Retain:</b> only with {@code haRetainDiscovery}. Home Assistant prefers discovery re-published
 *       on its birth message, because retained configs can leave ghost entities.
 *   <li><b>Removal:</b> a component that leaves the set is published reduced to {@code {p}}, then the config
 *       goes out without it. Turning {@code haDiscovery} off, disabling or deleting the channel publishes an
 *       empty retained payload on the device topic.
 * </ul>
 *
 * <p>Sources are listed when they are not private and not disabled.
 */
@Component
public class HaDiscoveryPublisher {

    private static final Logger log = LoggerFactory.getLogger(HaDiscoveryPublisher.class);

    /** The debounce of rule and source changes. */
    public static final Duration DEBOUNCE = Duration.ofSeconds(5);

    /** The longest random delay after Home Assistant's birth message. */
    public static final Duration BIRTH_JITTER = Duration.ofSeconds(5);

    /** How long one discovery publish waits for its PUBACK. */
    static final long PUBLISH_WAIT_MS = 5_000;

    private final MqttChannelRegistry registry;
    private final TriggerRuleRepository rules;
    private final HealthService health;
    private final EgressPolicy egress;
    private final InstallIdService installId;
    private final ForesignalVersion version;
    private final ChannelService channels;
    private final JsonMapper json;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("mqtt-discovery").factory());

    /** Creates the publisher. */
    public HaDiscoveryPublisher(MqttChannelRegistry registry, TriggerRuleRepository rules, HealthService health,
            EgressPolicy egress, InstallIdService installId, ForesignalVersion version, ChannelService channels,
            JsonMapper json) {
        this.registry = registry;
        this.rules = rules;
        this.health = health;
        this.egress = egress;
        this.installId = installId;
        this.version = version;
        this.channels = channels;
        this.json = json;
    }

    /** Stops the scheduler. */
    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    /** On connect: subscribes to Home Assistant's status and publishes discovery, when enabled. */
    public void onConnected(MqttChannelRuntime runtime) {
        if (!runtime.config().haDiscovery()) {
            return;
        }
        runtime.connection().subscribe(MqttTopics.haStatus(runtime.config().haPrefix()),
                inbound -> onHaStatus(runtime, inbound));
        publishNow(runtime);
    }

    /** Home Assistant came online: discovery again after a random delay of up to 5 s. */
    void onHaStatus(MqttChannelRuntime runtime, MqttInbound inbound) {
        if (!"online".equals(inbound.text().strip())) {
            return;
        }
        long delay = ThreadLocalRandom.current().nextLong(BIRTH_JITTER.toMillis() + 1);
        scheduler.schedule(() -> publishIfCurrent(runtime), delay, TimeUnit.MILLISECONDS);
    }

    /** Rules changed: discovery again after the debounce. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onRulesChanged(TriggerRulesChangedEvent event) {
        scheduleAll();
    }

    /** A source appeared, disappeared or was redefined: discovery again after the debounce. */
    @EventListener
    public void onSourceRegistryChanged(SourceRegistryChangedEvent event) {
        scheduleAll();
    }

    /** A source was enabled, disabled or made private: discovery again after the debounce. */
    @EventListener
    public void onSourceSettingsChanged(SourceSettingsChangedEvent event) {
        scheduleAll();
    }

    /** A source became private: discovery again after the debounce. */
    @EventListener
    public void onSourceBecamePrivate(SourceBecamePrivateEvent event) {
        scheduleAll();
    }

    /** Schedules a debounced publish for every channel with discovery. */
    public void scheduleAll() {
        for (MqttChannelRuntime runtime : registry.all()) {
            if (runtime.config().haDiscovery()) {
                schedule(runtime);
            }
        }
    }

    /** Schedules a publish {@link #DEBOUNCE} from now, replacing a pending one. */
    public void schedule(MqttChannelRuntime runtime) {
        runtime.discoveryDebounce(scheduler.schedule(() -> publishIfCurrent(runtime), DEBOUNCE.toMillis(),
                TimeUnit.MILLISECONDS));
    }

    /**
     * Publishes the device config now: removed components first as {@code {p}}, then the full config.
     *
     * @return true when the config went out
     */
    public boolean publishNow(MqttChannelRuntime runtime) {
        if (!runtime.config().haDiscovery() || !runtime.connection().connected()) {
            return false;
        }
        try {
            Map<String, Object> device = HaDiscoveryPayloads.device(input(runtime));
            Map<String, String> platforms = HaDiscoveryPayloads.platforms(device);
            Map<String, String> removed = HaDiscoveryPayloads.removed(runtime.discoveryPlatforms(), platforms);
            String topic = topic(runtime);
            boolean retain = runtime.config().haRetainDiscovery();
            if (!removed.isEmpty()) {
                send(runtime, topic, json.writeValueAsString(HaDiscoveryPayloads.withRemoved(device, removed)), retain);
            }
            if (send(runtime, topic, json.writeValueAsString(device), retain)) {
                runtime.discoveryPlatforms(platforms);
                return true;
            }
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: discovery not published: {}", runtime.channelId(), e.getMessage());
        }
        return false;
    }

    /**
     * Removes the device from Home Assistant: an empty retained payload on the device topic of
     * {@code config}, on {@code runtime}'s connection.
     */
    public void remove(MqttChannelRuntime runtime, MqttChannelConfig config) {
        if (!runtime.connection().connected()) {
            return;
        }
        String topic = MqttTopics.haDevice(config.haPrefix(), installId.installId());
        try {
            runtime.publish(new MqttMessage(topic, new byte[0], 1, true, null, null))
                    .get(PUBLISH_WAIT_MS, TimeUnit.MILLISECONDS);
            runtime.discoveryPlatforms(Map.of());
        } catch (Exception e) {
            log.warn("MQTT channel {}: discovery removal not confirmed: {}", runtime.channelId(), e.getMessage());
        }
    }

    /** The discovery input of {@code runtime}. */
    HaDiscoveryPayloads.Input input(MqttChannelRuntime runtime) {
        List<HaDiscoveryPayloads.Rule> ruleList = new ArrayList<>();
        for (TriggerRuleEntity rule : rules.findAll()) {
            ruleList.add(new HaDiscoveryPayloads.Rule(rule.getId(), rule.getName(), rule.getSeverity(), rule.isEnabled()));
        }
        List<String> sources = new ArrayList<>();
        try {
            for (SourceHealthView view : health.all(Optional.empty())) {
                if (view.status() != HealthStatus.DISABLED && !egress.isPrivate(view.sourceId())) {
                    sources.add(view.sourceId());
                }
            }
        } catch (RuntimeException e) {
            log.debug("Sources unavailable for discovery: {}", e.getMessage());
        }
        String base;
        try {
            base = channels.serverSettings().normalizedPublicBaseUrl();
        } catch (RuntimeException e) {
            base = null;
        }
        MqttChannelConfig c = runtime.config();
        return new HaDiscoveryPayloads.Input(installId.installId(), version.version(), base, c.topicPrefix(), ruleList,
                sources, c.haMaxRules(), c.haMaxSources());
    }

    private void publishIfCurrent(MqttChannelRuntime runtime) {
        if (registry.find(runtime.channelId()).orElse(null) == runtime) {
            publishNow(runtime);
        }
    }

    private String topic(MqttChannelRuntime runtime) {
        return MqttTopics.haDevice(runtime.config().haPrefix(), installId.installId());
    }

    private boolean send(MqttChannelRuntime runtime, String topic, String body, boolean retain) {
        try {
            runtime.publish(new MqttMessage(topic, body.getBytes(java.nio.charset.StandardCharsets.UTF_8), 1, retain,
                    null, MqttMessage.JSON)).get(PUBLISH_WAIT_MS, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("MQTT channel {}: discovery publish failed: {}", runtime.channelId(), e.getMessage());
            return false;
        }
    }
}
