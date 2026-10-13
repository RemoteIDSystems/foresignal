package ai.foresignal.notify;

import java.io.IOException;

/**
 * Publishes an MQTT delivery on its channel's supervised connection (prompt 060). Implemented by the
 * {@code mqtt} channel's connection manager; {@link ChannelSender} calls it for {@link PreparedRequest.Mqtt}
 * bodies so MQTT deliveries keep the 018 pipeline (rows, rate limit, retry ladder).
 */
public interface MqttDeliveryGateway {

    /**
     * Publishes {@code request} and waits for the PUBACK (QoS 1) or the write (QoS 0).
     *
     * @return status 200 with body {@code PUBACK} or {@code written}
     * @throws IOException when the channel has no connection, the broker is not connected, refuses the
     *     message or does not acknowledge it in time
     */
    ChannelResponse publish(PreparedRequest.Mqtt request) throws IOException;
}
