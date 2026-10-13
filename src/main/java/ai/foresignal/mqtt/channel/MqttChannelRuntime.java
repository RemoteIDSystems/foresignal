package ai.foresignal.mqtt.channel;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttMessage;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * The live state of one enabled {@code mqtt} channel (prompt 060): its settings, its supervised connection,
 * which retained rule states it published, the last summary ETag and the discovery components last sent.
 */
public final class MqttChannelRuntime {

    private final UUID channelId;
    private final MqttChannelConfig config;
    private final String fingerprint;
    private final MqttConnection connection;
    private final Set<UUID> publishedRules = ConcurrentHashMap.newKeySet();
    private volatile String summaryEtag;
    private volatile Instant nextSummaryAt = Instant.EPOCH;
    private volatile Map<String, String> discoveryPlatforms = Map.of();
    private ScheduledFuture<?> discoveryDebounce;

    /**
     * Creates the runtime.
     *
     * @param channelId the channel
     * @param config its settings
     * @param fingerprint a hash of its settings and secrets; a change reopens the connection
     * @param connection its connection (not necessarily started yet)
     */
    public MqttChannelRuntime(UUID channelId, MqttChannelConfig config, String fingerprint, MqttConnection connection) {
        this.channelId = channelId;
        this.config = config;
        this.fingerprint = fingerprint;
        this.connection = connection;
    }

    /** The channel. */
    public UUID channelId() {
        return channelId;
    }

    /** Its settings. */
    public MqttChannelConfig config() {
        return config;
    }

    /** The settings-and-secrets hash. */
    public String fingerprint() {
        return fingerprint;
    }

    /** Its connection. */
    public MqttConnection connection() {
        return connection;
    }

    /** Publishes {@code message} on the connection. */
    public CompletableFuture<Void> publish(MqttMessage message) {
        return connection.publish(message);
    }

    /** The rules whose retained state this channel published. */
    public Set<UUID> publishedRules() {
        return publishedRules;
    }

    /** The ETag of the last published summary, or null. */
    public String summaryEtag() {
        return summaryEtag;
    }

    /** Records the ETag of the last published summary. */
    public void summaryEtag(String etag) {
        this.summaryEtag = etag;
    }

    /** When the summary is next checked. */
    public Instant nextSummaryAt() {
        return nextSummaryAt;
    }

    /** Sets when the summary is next checked. */
    public void nextSummaryAt(Instant at) {
        this.nextSummaryAt = at;
    }

    /** The discovery components last published, key to platform. */
    public Map<String, String> discoveryPlatforms() {
        return discoveryPlatforms;
    }

    /** Records the discovery components last published. */
    public void discoveryPlatforms(Map<String, String> platforms) {
        this.discoveryPlatforms = Map.copyOf(platforms);
    }

    /** Replaces the pending discovery debounce, cancelling the previous one. */
    public synchronized void discoveryDebounce(ScheduledFuture<?> next) {
        if (discoveryDebounce != null) {
            discoveryDebounce.cancel(false);
        }
        discoveryDebounce = next;
    }
}
