package ai.foresignal.mqtt;

import java.util.Objects;

/**
 * What one supervised connection needs (prompt 060).
 *
 * @param key the owner key, unique among connections ({@code channel:<uuid>})
 * @param label the metric tag value ({@code foresignal.mqtt.connected{channel=<label>}})
 * @param clientId the MQTT client identifier
 * @param endpoint the broker
 * @param protocolVersion {@code 5} or {@code 3.1.1}
 * @param username the user name, or null
 * @param password the password, or null
 * @param caCertPem extra trusted CA certificates in PEM, or null for the system trust store only
 * @param statusTopic the availability topic: Last Will {@code offline} and {@code online} on connect,
 *     both retained at QoS 1; null (prompt 083, a subscribing source) for no Will and no availability publishes at
 *     all, so the connection never publishes anything
 * @param options the session behavior (prompt 083); {@link MqttSessionOptions#defaults()} when null
 */
public record MqttConnectionSpec(String key, String label, String clientId, MqttEndpoint endpoint,
        String protocolVersion, String username, String password, String caCertPem, String statusTopic,
        MqttSessionOptions options) {

    /** MQTT 5. */
    public static final String V5 = "5";

    /** MQTT 3.1.1. */
    public static final String V311 = "3.1.1";

    /** The availability payload published on connect. */
    public static final String ONLINE = "online";

    /** The availability payload of the Last Will and of a graceful shutdown. */
    public static final String OFFLINE = "offline";

    /**
     * Requires the key, client id and endpoint, and the status topic unless the session is a non-reconnecting
     * subscriber; defaults the version to 5 and the options to {@link MqttSessionOptions#defaults()}.
     */
    public MqttConnectionSpec {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(clientId, "clientId");
        Objects.requireNonNull(endpoint, "endpoint");
        options = options == null ? MqttSessionOptions.defaults() : options;
        if (statusTopic == null && options.autoReconnect()) {
            throw new NullPointerException("statusTopic (only a non-reconnecting subscriber may omit it)");
        }
        protocolVersion = V311.equals(protocolVersion) ? V311 : V5;
        label = label == null ? key : label;
    }

    /** A publisher connection with prompt 060's session (its original constructor). */
    public MqttConnectionSpec(String key, String label, String clientId, MqttEndpoint endpoint, String protocolVersion,
            String username, String password, String caCertPem, String statusTopic) {
        this(key, label, clientId, endpoint, protocolVersion, username, password, caCertPem, statusTopic, null);
    }

    /** True when the connection publishes availability on a status topic (and so has a Last Will). */
    public boolean hasStatusTopic() {
        return statusTopic != null;
    }

    /** True for MQTT 3.1.1. */
    public boolean v3() {
        return V311.equals(protocolVersion);
    }

    /** {@code this} without the password and CA, for logs. */
    @Override
    public String toString() {
        return "MqttConnectionSpec[key=" + key + ", clientId=" + clientId + ", endpoint=" + endpoint
                + ", protocolVersion=" + protocolVersion + ", username=" + (username == null ? "none" : "set")
                + ", options=" + options + "]";
    }
}
