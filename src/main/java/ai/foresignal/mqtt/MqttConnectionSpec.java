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
 *     both retained at QoS 1
 */
public record MqttConnectionSpec(String key, String label, String clientId, MqttEndpoint endpoint,
        String protocolVersion, String username, String password, String caCertPem, String statusTopic) {

    /** MQTT 5. */
    public static final String V5 = "5";

    /** MQTT 3.1.1. */
    public static final String V311 = "3.1.1";

    /** The availability payload published on connect. */
    public static final String ONLINE = "online";

    /** The availability payload of the Last Will and of a graceful shutdown. */
    public static final String OFFLINE = "offline";

    /** Requires the key, client id, endpoint and status topic; defaults the version to 5. */
    public MqttConnectionSpec {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(clientId, "clientId");
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(statusTopic, "statusTopic");
        protocolVersion = V311.equals(protocolVersion) ? V311 : V5;
        label = label == null ? key : label;
    }

    /** True for MQTT 3.1.1. */
    public boolean v3() {
        return V311.equals(protocolVersion);
    }

    /** {@code this} without the password and CA, for logs. */
    @Override
    public String toString() {
        return "MqttConnectionSpec[key=" + key + ", clientId=" + clientId + ", endpoint=" + endpoint
                + ", protocolVersion=" + protocolVersion + ", username=" + (username == null ? "none" : "set") + "]";
    }
}
