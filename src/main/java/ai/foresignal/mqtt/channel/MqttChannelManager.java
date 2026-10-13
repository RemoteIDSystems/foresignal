package ai.foresignal.mqtt.channel;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.notify.ChannelChangedEvent;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.MqttDeliveryGateway;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.MqttChannel;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Keeps one supervised broker connection per enabled {@code mqtt} channel (prompt 060) and publishes their
 * deliveries ({@link MqttDeliveryGateway}).
 *
 * <ul>
 *   <li><b>Sync:</b> at startup and after every {@link ChannelChangedEvent}. A new or enabled channel gets a
 *       connection ({@code channel:<id>}, client id {@code foresignal-{installId}-{channelIdShort}}, status
 *       topic {@code {prefix}/status}); a changed setting or secret reopens it; a disabled or deleted channel
 *       closes it ({@code offline}, then DISCONNECT) after removing its Home Assistant device.
 *   <li><b>On every connect</b> (after {@code online}): Home Assistant discovery, then every retained state
 *       ({@link HaDiscoveryPublisher}, {@link MqttStatePublisher}).
 *   <li><b>Deliveries:</b> wait up to {@code connectTimeout} for a connection still being made, then publish
 *       and wait {@code publishTimeout} for the PUBACK (QoS 1) or write (QoS 0). Anything else is an
 *       {@link IOException}: the delivery worker retries on its ladder.
 *   <li><b>Republish</b> ({@code POST /v1/channels/{id}/mqtt/republish}): states and discovery again,
 *       asynchronously.
 * </ul>
 */
@Component
public class MqttChannelManager implements MqttDeliveryGateway {

    private static final Logger log = LoggerFactory.getLogger(MqttChannelManager.class);

    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final MqttConnections connections;
    private final MqttChannelRegistry registry;
    private final MqttStatePublisher state;
    private final HaDiscoveryPublisher discovery;
    private final InstallIdService installId;
    private final MqttProperties properties;
    private final ExecutorService work = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the manager. */
    public MqttChannelManager(NotificationChannelRepository channels, ChannelService channelService,
            MqttConnections connections, MqttChannelRegistry registry, MqttStatePublisher state,
            HaDiscoveryPublisher discovery, InstallIdService installId, MqttProperties properties) {
        this.channels = channels;
        this.channelService = channelService;
        this.connections = connections;
        this.registry = registry;
        this.state = state;
        this.discovery = discovery;
        this.installId = installId;
        this.properties = properties;
    }

    /** Opens the connections of the enabled {@code mqtt} channels. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.MQTT)
    public void start() {
        try {
            resync();
        } catch (RuntimeException e) {
            log.error("MQTT channels could not be started", e);
        }
    }

    /** Follows a created, replaced or deleted channel once its transaction committed. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onChannelChanged(ChannelChangedEvent event) {
        boolean known = registry.find(event.channelId()).isPresent();
        if (!known && !MqttChannel.ID.equals(event.type())) {
            return;
        }
        try {
            sync(event.channelId());
        } catch (RuntimeException e) {
            log.error("MQTT channel {} could not be synchronized", event.channelId(), e);
        }
    }

    /** Synchronizes every channel: opens the missing connections and closes those without an enabled channel. */
    public synchronized void resync() {
        Set<UUID> wanted = new HashSet<>();
        for (NotificationChannelEntity e : channels.findAll()) {
            if (MqttChannel.ID.equals(e.getType())) {
                wanted.add(e.getId());
                sync(e.getId());
            }
        }
        for (MqttChannelRuntime runtime : registry.all()) {
            if (!wanted.contains(runtime.channelId())) {
                sync(runtime.channelId());
            }
        }
    }

    /** Brings the connection of {@code channelId} in line with the stored channel. */
    public synchronized void sync(UUID channelId) {
        Optional<NotificationChannelEntity> stored = channels.findById(channelId);
        MqttChannelRuntime current = registry.find(channelId).orElse(null);
        if (stored.isEmpty() || !MqttChannel.ID.equals(stored.get().getType()) || !stored.get().isEnabled()) {
            if (current != null) {
                teardown(current, true);
            }
            return;
        }
        NotificationChannelEntity e = stored.get();
        Map<String, String> secrets = channelService.secrets(e);
        MqttChannelConfig config = MqttChannelConfig.of(e.getConfig());
        String fingerprint = fingerprint(e.getConfig(), secrets);
        if (current != null && current.fingerprint().equals(fingerprint) && !current.connection().closed()) {
            return;
        }
        Map<String, String> platforms = Map.of();
        if (current != null) {
            MqttChannelConfig old = current.config();
            boolean sameDevice = old.haDiscovery() && config.haDiscovery() && old.haPrefix().equals(config.haPrefix());
            if (old.haDiscovery() && !sameDevice) {
                discovery.remove(current, old);
            } else if (sameDevice) {
                platforms = current.discoveryPlatforms();
            }
            teardown(current, false);
        }
        open(channelId, config, secrets, fingerprint, platforms);
    }

    /**
     * Forces re-publication of every retained state and of discovery, asynchronously.
     *
     * @return false when the channel has no connection (not an enabled {@code mqtt} channel)
     */
    public boolean republish(UUID channelId) {
        Optional<MqttChannelRuntime> runtime = registry.find(channelId);
        if (runtime.isEmpty()) {
            return false;
        }
        work.execute(() -> onConnected(runtime.get()));
        return true;
    }

    @Override
    public ChannelResponse publish(PreparedRequest.Mqtt request) throws IOException {
        MqttChannelRuntime runtime = registry.find(request.channelId()).orElse(null);
        if (runtime == null) {
            sync(request.channelId());
            runtime = registry.find(request.channelId())
                    .orElseThrow(() -> new IOException("the channel has no MQTT connection (disabled or not mqtt)"));
        }
        awaitConnection(runtime.connection());
        MqttMessage message = request.message();
        try {
            runtime.publish(message).get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            return new ChannelResponse(200, Map.of(), message.qos() == 1 ? "PUBACK" : "written");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof IOException io) {
                throw io;
            }
            throw new IOException("the broker refused the message: " + cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new IOException("no " + (message.qos() == 1 ? "PUBACK" : "write") + " within "
                    + properties.publishTimeout().toSeconds() + " s");
        }
    }

    /** Closes every connection without removing discovery (tests). */
    public synchronized void closeAll() {
        for (MqttChannelRuntime runtime : registry.all()) {
            teardown(runtime, false);
        }
    }

    // ------------------------------------------------------------------------- internals

    private void open(UUID channelId, MqttChannelConfig config, Map<String, String> secrets, String fingerprint,
            Map<String, String> platforms) {
        MqttConnectionSpec spec;
        try {
            spec = new MqttConnectionSpec(key(channelId), channelId.toString(),
                    MqttConnections.clientId(installId.installId(), channelId), config.endpoint(),
                    config.protocolVersion(), secrets.get("username"), secrets.get("password"),
                    secrets.get("caCertPem"), MqttTopics.status(config.topicPrefix()));
        } catch (IllegalArgumentException e) {
            log.warn("MQTT channel {} has an unusable broker URL: {}", channelId, e.getMessage());
            return;
        }
        MqttConnection connection;
        try {
            connection = connections.create(spec);
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: the connection could not be created: {}", channelId, e.getMessage());
            return;
        }
        MqttChannelRuntime runtime = new MqttChannelRuntime(channelId, config, fingerprint, connection);
        runtime.discoveryPlatforms(platforms);
        connection.onConnected(() -> onConnected(runtime));
        registry.put(runtime);
        connection.start();
    }

    /** Discovery, then every retained state, unless the runtime was replaced meanwhile. */
    void onConnected(MqttChannelRuntime runtime) {
        if (registry.find(runtime.channelId()).orElse(null) != runtime) {
            return;
        }
        try {
            discovery.onConnected(runtime);
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: discovery failed", runtime.channelId(), e);
        }
        try {
            state.republishAll(runtime);
        } catch (RuntimeException e) {
            log.warn("MQTT channel {}: state publish failed", runtime.channelId(), e);
        }
    }

    private void teardown(MqttChannelRuntime runtime, boolean removeDiscovery) {
        registry.remove(runtime.channelId());
        runtime.discoveryDebounce(null);
        if (removeDiscovery && runtime.config().haDiscovery()) {
            discovery.remove(runtime, runtime.config());
        }
        connections.close(key(runtime.channelId()));
    }

    private void awaitConnection(MqttConnection connection) throws IOException {
        long deadline = System.nanoTime() + properties.connectTimeout().toNanos();
        while (!connection.connected() && System.nanoTime() < deadline && !connection.closed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("interrupted", e);
            }
        }
        if (!connection.connected()) {
            throw new IOException("not connected to the MQTT broker"
                    + (connection.lastError() == null ? "" : " (" + connection.lastError() + ")"));
        }
    }

    /** The connection key of a channel. */
    static String key(UUID channelId) {
        return "channel:" + channelId;
    }

    /** A SHA-256 over the sorted settings and secrets. */
    static String fingerprint(Map<String, Object> config, Map<String, String> secrets) {
        String text = new TreeMap<>(config == null ? Map.of() : config) + "|" + new TreeMap<>(secrets);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
