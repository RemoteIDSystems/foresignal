package ai.foresignal.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * One outbound PUBLISH.
 *
 * @param topic the topic, without wildcards
 * @param payload the payload; empty clears a retained topic
 * @param qos 0 or 1
 * @param retain whether the broker keeps it as the topic's last value
 * @param messageExpirySeconds the MQTT 5 message expiry interval, or null for none (ignored on 3.1.1)
 * @param contentType the MQTT 5 content type, or null (ignored on 3.1.1)
 */
public record MqttMessage(String topic, byte[] payload, int qos, boolean retain, Long messageExpirySeconds,
        String contentType) {

    /** The content type of every JSON payload Foresignal publishes. */
    public static final String JSON = "application/json";

    /** Requires a topic; a null payload becomes empty. */
    public MqttMessage {
        Objects.requireNonNull(topic, "topic");
        payload = payload == null ? new byte[0] : payload;
        if (qos < 0 || qos > 1) {
            throw new IllegalArgumentException("qos must be 0 or 1");
        }
    }

    /** A retained QoS 1 message with a UTF-8 text payload and no expiry. */
    public static MqttMessage retained(String topic, String text, String contentType) {
        return new MqttMessage(topic, text.getBytes(StandardCharsets.UTF_8), 1, true, null, contentType);
    }

    /** The payload as UTF-8 text (tests, logging). */
    public String text() {
        return new String(payload, StandardCharsets.UTF_8);
    }
}
