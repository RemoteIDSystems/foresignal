package ai.foresignal.mqtt;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.system.SystemBlockContributor;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The supervised MQTT connections (prompt 060): one per owner key, each a {@link MqttConnection}. The
 * {@code mqtt} channel opens one per enabled channel; the MQTT source adapters (083, 085) are meant to
 * open theirs here too, so {@code /v1/system} and the metrics see every connection.
 *
 * <ul>
 *   <li><b>Client id:</b> {@code foresignal-{installId}-{channelIdShort}} ({@link #clientId}), the install
 *       id's 8 characters and the first 8 hex digits of the owner's UUID.
 *   <li><b>Destination policy:</b> the broker host passes {@link OutboundUrlPolicy} profile
 *       {@code LAN_ALLOWED} before every connect attempt (private hosts are allowed, link-local, metadata
 *       and blocked addresses never). A host that does not resolve is attempted anyway: the client's own
 *       resolution fails and the attempt is retried. The client resolves the name itself right after the
 *       check, which the JVM's DNS cache answers with the checked address, as for SMTP (prompt 035).
 *   <li><b>Metric:</b> {@code foresignal.mqtt.connected{channel}} is 1 while a connection is up, 0
 *       otherwise, and is removed with the connection.
 *   <li><b>{@code /v1/system}:</b> the {@code mqtt} block {@code {connections, connected, lastError}}.
 *   <li><b>Shutdown:</b> every connection publishes {@code offline} and disconnects normally.
 * </ul>
 */
@Component
public class MqttConnections implements SystemBlockContributor {

    /** The metric name. */
    public static final String METRIC = "foresignal.mqtt.connected";

    private final MqttProperties properties;
    private final OutboundUrlPolicy policy;
    private final ObjectProvider<MeterRegistry> meters;
    private final Clock clock;
    private final Map<String, Entry> connections = new ConcurrentHashMap<>();
    private final ExecutorService callbacks = Executors.newVirtualThreadPerTaskExecutor();
    private volatile String lastClosedError;

    private record Entry(MqttConnection connection, Gauge gauge) {}

    /** Creates the registry. */
    public MqttConnections(MqttProperties properties, OutboundUrlPolicy policy, ObjectProvider<MeterRegistry> meters,
            Clock clock) {
        this.properties = properties;
        this.policy = policy;
        this.meters = meters;
        this.clock = clock;
    }

    /** {@code foresignal-{installId}-{first 8 hex digits of ownerId}}. */
    public static String clientId(String installId, UUID ownerId) {
        return "foresignal-" + installId + "-" + ownerId.toString().replace("-", "").substring(0, 8);
    }

    /**
     * Opens the connection of {@code spec}, replacing (and closing) any connection with the same key.
     * {@code onConnected} runs after every (re)connect; the connection starts before this returns.
     */
    public MqttConnection open(MqttConnectionSpec spec, Runnable onConnected) {
        MqttConnection connection = create(spec);
        if (onConnected != null) {
            connection.onConnected(onConnected);
        }
        connection.start();
        return connection;
    }

    /**
     * Registers the connection of {@code spec} without starting it, replacing (and closing) any connection
     * with the same key; the caller adds its callbacks and calls {@link MqttConnection#start()}.
     */
    public MqttConnection create(MqttConnectionSpec spec) {
        close(spec.key());
        MqttConnection connection = new MqttConnection(spec, properties, () -> denial(spec.endpoint().host()),
                callbacks, () -> ThreadLocalRandom.current().nextDouble(), clock);
        Gauge gauge = null;
        MeterRegistry registry = meters.getIfAvailable();
        if (registry != null) {
            gauge = Gauge.builder(METRIC, connection, c -> c.connected() ? 1 : 0)
                    .description("1 while the MQTT connection is up")
                    .tag("channel", spec.label())
                    .register(registry);
        }
        connections.put(spec.key(), new Entry(connection, gauge));
        return connection;
    }

    /** The connection with {@code key}, if open. */
    public Optional<MqttConnection> find(String key) {
        Entry entry = connections.get(key);
        return entry == null ? Optional.empty() : Optional.of(entry.connection());
    }

    /** Closes the connection with {@code key} gracefully ({@code offline}, then DISCONNECT), if open. */
    public void close(String key) {
        Entry entry = connections.remove(key);
        if (entry == null) {
            return;
        }
        if (entry.connection().lastError() != null) {
            lastClosedError = entry.connection().lastError();
        }
        entry.connection().close();
        MeterRegistry registry = meters.getIfAvailable();
        if (registry != null && entry.gauge() != null) {
            registry.remove(entry.gauge());
        }
    }

    /** Every open key. */
    public List<String> keys() {
        return List.copyOf(connections.keySet());
    }

    /** Closes every connection gracefully (application shutdown). */
    @PreDestroy
    public void closeAll() {
        for (String key : new ArrayList<>(connections.keySet())) {
            close(key);
        }
    }

    /**
     * The state of the connections.
     *
     * @param connections configured connections
     * @param connected currently connected
     * @param lastError the most recent error of a disconnected connection (else of any), or null
     */
    public record Snapshot(int connections, int connected, String lastError) {}

    /** The current state. */
    public Snapshot snapshot() {
        int total = 0;
        int up = 0;
        String error = null;
        for (Entry entry : connections.values()) {
            total++;
            MqttConnection c = entry.connection();
            if (c.connected()) {
                up++;
            } else if (c.lastError() != null) {
                error = c.lastError();
            }
        }
        return new Snapshot(total, up, error != null ? error : (total == 0 ? null : lastClosedError));
    }

    @Override
    public String key() {
        return "mqtt";
    }

    @Override
    public Map<String, Object> block() {
        Snapshot s = snapshot();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("connections", s.connections());
        out.put("connected", s.connected());
        out.put("lastError", s.lastError());
        return out;
    }

    /** The policy's refusal of {@code host}, or null when allowed or not resolvable right now. */
    String denial(String host) {
        OutboundUrlPolicy.Result result = policy.checkHost(host, OutboundUrlPolicy.Profile.LAN_ALLOWED);
        return result instanceof OutboundUrlPolicy.Denied d && !d.unresolved() ? d.reason() : null;
    }
}
