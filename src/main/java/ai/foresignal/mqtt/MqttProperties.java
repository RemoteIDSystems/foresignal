package ai.foresignal.mqtt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * MQTT client settings ({@code foresignal.mqtt.*}, prompt 060).
 *
 * @param scheduled whether the periodic summary and health refreshes run on their own; tests turn it off
 *     and call the publishers directly
 * @param keepAlive the MQTT keepalive
 * @param sessionExpiry the MQTT 5 session expiry interval
 * @param publishTimeout how long a delivery waits for its PUBACK (QoS 1) or write (QoS 0)
 * @param connectTimeout how long a channel test waits for a connection that is still being made
 */
@ConfigurationProperties("foresignal.mqtt")
public record MqttProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("60s") Duration keepAlive,
        @DefaultValue("1h") Duration sessionExpiry,
        @DefaultValue("15s") Duration publishTimeout,
        @DefaultValue("5s") Duration connectTimeout) {

    /** Fills missing values with the defaults. */
    public MqttProperties {
        keepAlive = keepAlive == null ? Duration.ofSeconds(60) : keepAlive;
        sessionExpiry = sessionExpiry == null ? Duration.ofHours(1) : sessionExpiry;
        publishTimeout = publishTimeout == null ? Duration.ofSeconds(15) : publishTimeout;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
    }

    /** The production defaults. */
    public static MqttProperties defaults() {
        return new MqttProperties(true, Duration.ofSeconds(60), Duration.ofHours(1), Duration.ofSeconds(15),
                Duration.ofSeconds(5));
    }
}
