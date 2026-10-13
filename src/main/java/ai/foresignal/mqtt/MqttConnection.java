package ai.foresignal.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One supervised MQTT connection (prompt 060), an {@code Mqtt5AsyncClient} or, for
 * {@code protocolVersion=3.1.1}, an {@code Mqtt3AsyncClient}.
 *
 * <ul>
 *   <li><b>Session:</b> keepalive 60 s, clean start off, session expiry 1 h (MQTT 5; 3.1.1 keeps a
 *       persistent session with {@code cleanSession=false}).
 *   <li><b>Availability:</b> the Last Will is {@code offline} on the status topic, retained, QoS 1. Every
 *       (re)connect publishes {@code online} retained before anything else; {@link #close} publishes
 *       {@code offline} before a normal DISCONNECT, which does not trigger the Will.
 *   <li><b>Reconnect:</b> every lost connection or failed attempt is retried after {@link MqttBackoff}
 *       (1 s doubling to 120 s with jitter), unless the connection was closed. The outbound URL policy is
 *       asked again before each attempt, so a host that the policy now refuses is not retried.
 *   <li><b>Subscriptions</b> are remembered and sent again after each connect; inbound messages reach their
 *       handlers on the callback executor, never on the client's I/O thread. The connected callbacks run
 *       there too.
 *   <li><b>Publishing</b> fails at once when the client is not connected, so the caller's retry ladder,
 *       not the client's offline queue, decides when a message goes out.
 * </ul>
 */
public class MqttConnection {

    private static final Logger log = LoggerFactory.getLogger(MqttConnection.class);

    /** How long {@link #close} waits for the {@code offline} PUBACK and for the DISCONNECT. */
    static final Duration CLOSE_WAIT = Duration.ofSeconds(2);

    private final MqttConnectionSpec spec;
    private final MqttProperties properties;
    private final Supplier<String> policy;
    private final Executor callbacks;
    private final DoubleSupplier random;
    private final Clock clock;
    private final List<Runnable> onConnected = new CopyOnWriteArrayList<>();
    private final Map<String, Consumer<MqttInbound>> subscriptions = new ConcurrentHashMap<>();
    private final Mqtt5AsyncClient v5;
    private final Mqtt3AsyncClient v3;
    private volatile boolean closed;
    private volatile boolean started;
    private volatile String lastError;
    private volatile Instant connectedAt;

    /**
     * Builds the client; nothing connects until {@link #start}.
     *
     * @param spec the connection
     * @param properties keepalive, session expiry and timeouts
     * @param policy the outbound URL policy for the broker host: a refusal reason, or null when allowed
     * @param callbacks where connected callbacks and inbound messages run
     * @param random the jitter source, uniform in [0, 1)
     * @param clock the clock of {@link #connectedAt()}
     */
    public MqttConnection(MqttConnectionSpec spec, MqttProperties properties, Supplier<String> policy,
            Executor callbacks, DoubleSupplier random, Clock clock) {
        this.spec = spec;
        this.properties = properties;
        this.policy = policy == null ? () -> null : policy;
        this.callbacks = callbacks;
        this.random = random;
        this.clock = clock;
        MqttClientBuilder builder = MqttClient.builder()
                .identifier(spec.clientId())
                .serverHost(spec.endpoint().host())
                .serverPort(spec.endpoint().port());
        if (spec.endpoint().tls()) {
            TrustManagerFactory trust = MqttTls.trustManagers(spec.caCertPem());
            builder = trust == null ? builder.sslWithDefaultConfig()
                    : builder.sslConfig().trustManagerFactory(trust).applySslConfig();
        }
        if (spec.endpoint().websocket()) {
            builder = spec.endpoint().path().isEmpty() ? builder.webSocketWithDefaultConfig()
                    : builder.webSocketConfig().serverPath(spec.endpoint().path()).applyWebSocketConfig();
        }
        builder = builder.addConnectedListener(context -> onConnect())
                .addDisconnectedListener(this::disconnected);
        if (spec.v3()) {
            this.v3 = builder.useMqttVersion3().buildAsync();
            this.v5 = null;
            v3.publishes(MqttGlobalPublishFilter.SUBSCRIBED,
                    p -> dispatch(p.getTopic().toString(), p.getPayloadAsBytes(), p.isRetain()), callbacks);
        } else {
            this.v5 = builder.useMqttVersion5().buildAsync();
            this.v3 = null;
            v5.publishes(MqttGlobalPublishFilter.SUBSCRIBED,
                    p -> dispatch(p.getTopic().toString(), p.getPayloadAsBytes(), p.isRetain()), callbacks);
        }
    }

    /** The connection's spec. */
    public MqttConnectionSpec spec() {
        return spec;
    }

    /**
     * Runs {@code callback} after every (re)connect, once {@code online} is published. Register before
     * {@link #start} to see the first connect.
     */
    public void onConnected(Runnable callback) {
        onConnected.add(callback);
    }

    /** Connects; failures are retried on the backoff schedule. */
    public synchronized void start() {
        if (started || closed) {
            return;
        }
        started = true;
        String denied = policy.get();
        if (denied != null) {
            lastError = denied;
            log.warn("MQTT connection {} not started: {}", spec.key(), denied);
            return;
        }
        CompletableFuture<?> connecting = spec.v3() ? v3.connect(connect3()) : v5.connect(connect5());
        connecting.whenComplete((ack, error) -> {
            if (error != null) {
                lastError = describe(error);
            }
        });
    }

    /** True while connected. */
    public boolean connected() {
        MqttClient client = spec.v3() ? v3 : v5;
        return !closed && client.getState().isConnected();
    }

    /** The last connection or protocol error, or null. */
    public String lastError() {
        return lastError;
    }

    /** When the current connection was made, or null while disconnected. */
    public Instant connectedAt() {
        return connectedAt;
    }

    /**
     * Publishes {@code message}.
     *
     * @return completes on PUBACK (QoS 1) or once written (QoS 0); fails at once with an
     *     {@link IOException} when not connected, and with the broker's reason when it refuses
     */
    public CompletableFuture<Void> publish(MqttMessage message) {
        if (!connected()) {
            return CompletableFuture.failedFuture(new IOException("not connected to the MQTT broker"
                    + (lastError == null ? "" : " (" + lastError + ")")));
        }
        MqttQos qos = message.qos() == 0 ? MqttQos.AT_MOST_ONCE : MqttQos.AT_LEAST_ONCE;
        try {
            if (spec.v3()) {
                Mqtt3Publish publish = Mqtt3Publish.builder().topic(message.topic()).payload(message.payload())
                        .qos(qos).retain(message.retain()).build();
                return v3.publish(publish).thenApply(p -> null);
            }
            Mqtt5PublishBuilder.Complete builder = Mqtt5Publish.builder().topic(message.topic())
                    .payload(message.payload()).qos(qos).retain(message.retain());
            if (message.messageExpirySeconds() != null) {
                builder = builder.messageExpiryInterval(message.messageExpirySeconds());
            }
            if (message.contentType() != null) {
                builder = builder.contentType(message.contentType())
                        .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8);
            }
            return v5.publish(builder.build()).thenApply(result -> {
                if (result.getError().isPresent()) {
                    throw new java.util.concurrent.CompletionException(result.getError().get());
                }
                return null;
            });
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Subscribes to {@code filter} at QoS 1 now (when connected) and after every reconnect.
     *
     * @param handler called on the callback executor for each matching message
     */
    public void subscribe(String filter, Consumer<MqttInbound> handler) {
        subscriptions.put(filter, handler);
        if (connected()) {
            sendSubscribe(filter);
        }
    }

    /**
     * Closes the connection: publishes {@code offline} retained (when connected), sends a normal DISCONNECT
     * so the broker does not publish the Will, and stops reconnecting. Waits at most two seconds for each.
     */
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        MqttClient client = spec.v3() ? v3 : v5;
        boolean wasConnected = client.getState().isConnected();
        try {
            if (wasConnected) {
                MqttQos qos = MqttQos.AT_LEAST_ONCE;
                byte[] offline = MqttConnectionSpec.OFFLINE.getBytes(StandardCharsets.UTF_8);
                CompletableFuture<?> sent = spec.v3()
                        ? v3.publish(Mqtt3Publish.builder().topic(spec.statusTopic()).payload(offline).qos(qos)
                                .retain(true).build())
                        : v5.publish(Mqtt5Publish.builder().topic(spec.statusTopic()).payload(offline).qos(qos)
                                .retain(true).build());
                sent.get(CLOSE_WAIT.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.debug("MQTT connection {}: offline status not confirmed: {}", spec.key(), e.getMessage());
        }
        try {
            (spec.v3() ? v3.disconnect() : v5.disconnect()).get(CLOSE_WAIT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.debug("MQTT connection {}: disconnect: {}", spec.key(), e.getMessage());
        }
        connectedAt = null;
    }

    /** True once {@link #close} was called. */
    public boolean closed() {
        return closed;
    }

    // ------------------------------------------------------------------------- internals

    private Mqtt5Connect connect5() {
        Mqtt5ConnectBuilder builder = Mqtt5Connect.builder()
                .keepAlive((int) properties.keepAlive().toSeconds())
                .cleanStart(false)
                .sessionExpiryInterval(properties.sessionExpiry().toSeconds())
                .willPublish(Mqtt5Publish.builder().topic(spec.statusTopic())
                        .payload(MqttConnectionSpec.OFFLINE.getBytes(StandardCharsets.UTF_8))
                        .qos(MqttQos.AT_LEAST_ONCE).retain(true).build());
        Mqtt5SimpleAuth auth = auth5();
        if (auth != null) {
            builder = builder.simpleAuth(auth);
        }
        return builder.build();
    }

    private Mqtt5SimpleAuth auth5() {
        byte[] password = spec.password() == null ? null : spec.password().getBytes(StandardCharsets.UTF_8);
        if (spec.username() != null) {
            Mqtt5SimpleAuthBuilder.Complete builder = Mqtt5SimpleAuth.builder().username(spec.username());
            return password == null ? builder.build() : builder.password(password).build();
        }
        return password == null ? null : Mqtt5SimpleAuth.builder().password(password).build();
    }

    private Mqtt3Connect connect3() {
        Mqtt3ConnectBuilder builder = Mqtt3Connect.builder()
                .keepAlive((int) properties.keepAlive().toSeconds())
                .cleanSession(false)
                .willPublish(Mqtt3Publish.builder().topic(spec.statusTopic())
                        .payload(MqttConnectionSpec.OFFLINE.getBytes(StandardCharsets.UTF_8))
                        .qos(MqttQos.AT_LEAST_ONCE).retain(true).build());
        if (spec.username() != null) {
            var auth = Mqtt3SimpleAuth.builder().username(spec.username());
            builder = builder.simpleAuth(spec.password() == null ? auth.build()
                    : auth.password(spec.password().getBytes(StandardCharsets.UTF_8)).build());
        }
        return builder.build();
    }

    private void onConnect() {
        connectedAt = clock.instant();
        lastError = null;
        callbacks.execute(() -> {
            try {
                publish(MqttMessage.retained(spec.statusTopic(), MqttConnectionSpec.ONLINE, null))
                        .get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("MQTT connection {}: online status not confirmed: {}", spec.key(), e.getMessage());
            }
            for (String filter : subscriptions.keySet()) {
                sendSubscribe(filter);
            }
            for (Runnable callback : onConnected) {
                try {
                    callback.run();
                } catch (RuntimeException e) {
                    log.warn("MQTT connection {}: a connected callback failed", spec.key(), e);
                }
            }
        });
    }

    private void disconnected(MqttClientDisconnectedContext context) {
        connectedAt = null;
        if (context.getCause() != null) {
            lastError = describe(context.getCause());
        }
        if (closed || context.getSource() == MqttDisconnectSource.USER) {
            return;
        }
        String denied = policy.get();
        if (denied != null) {
            lastError = denied;
            log.warn("MQTT connection {} not retried: {}", spec.key(), denied);
            return;
        }
        int attempt = context.getReconnector().getAttempts() + 1;
        Duration delay = MqttBackoff.delay(attempt, random.getAsDouble());
        log.info("MQTT connection {} lost ({}); reconnect attempt {} in {} ms", spec.key(), lastError, attempt,
                delay.toMillis());
        context.getReconnector().reconnect(true).delay(delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void sendSubscribe(String filter) {
        CompletableFuture<?> sent = spec.v3()
                ? v3.subscribe(Mqtt3Subscribe.builder().topicFilter(filter).qos(MqttQos.AT_LEAST_ONCE).build())
                : v5.subscribe(Mqtt5Subscribe.builder().topicFilter(filter).qos(MqttQos.AT_LEAST_ONCE).build());
        sent.whenComplete((ack, error) -> {
            if (error != null) {
                log.warn("MQTT connection {}: subscribe to {} failed: {}", spec.key(), filter, error.getMessage());
            }
        });
    }

    private void dispatch(String topic, byte[] payload, boolean retained) {
        MqttInbound inbound = new MqttInbound(topic, payload, retained);
        subscriptions.forEach((filter, handler) -> {
            if (matches(filter, topic)) {
                try {
                    handler.accept(inbound);
                } catch (RuntimeException e) {
                    log.warn("MQTT connection {}: handler of {} failed", spec.key(), filter, e);
                }
            }
        });
    }

    /** True when {@code topic} matches the subscription {@code filter} ({@code +} and {@code #} wildcards). */
    public static boolean matches(String filter, String topic) {
        String[] f = filter.split("/", -1);
        String[] t = topic.split("/", -1);
        for (int i = 0; i < f.length; i++) {
            if ("#".equals(f[i])) {
                return true;
            }
            if (i >= t.length) {
                return false;
            }
            if (!"+".equals(f[i]) && !f[i].equals(t[i])) {
                return false;
            }
        }
        return f.length == t.length;
    }

    private static String describe(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause
                && (cause instanceof java.util.concurrent.CompletionException
                        || cause instanceof java.util.concurrent.ExecutionException)) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
