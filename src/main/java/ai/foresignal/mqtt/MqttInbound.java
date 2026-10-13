package ai.foresignal.mqtt;

import java.nio.charset.StandardCharsets;

/**
 * One message received on a subscription.
 *
 * @param topic its topic
 * @param payload its payload
 * @param retained whether the broker delivered it as a retained message
 */
public record MqttInbound(String topic, byte[] payload, boolean retained) {

    /** The payload as UTF-8 text. */
    public String text() {
        return new String(payload == null ? new byte[0] : payload, StandardCharsets.UTF_8);
    }
}
