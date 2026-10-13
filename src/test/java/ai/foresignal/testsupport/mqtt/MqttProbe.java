package ai.foresignal.testsupport.mqtt;

import org.awaitility.Awaitility;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A test subscriber (prompt 060): connects to the anonymous listener with a clean session, subscribes to a
 * filter and records every message with its retain flag and remaining message expiry. A probe that connects
 * after a retained message was published receives it with {@code retained = true}.
 */
public final class MqttProbe implements AutoCloseable {

    /**
     * One received message.
     *
     * @param topic the topic
     * @param payload the payload as UTF-8 text
     * @param retained whether the broker delivered it as retained
     * @param expirySeconds the remaining message expiry, or null
     * @param contentType the MQTT 5 content type, or null
     */
    public record Received(String topic, String payload, boolean retained, Long expirySeconds, String contentType) {}

    private final Mqtt5AsyncClient client;
    private final List<Received> received = new CopyOnWriteArrayList<>();

    private MqttProbe(String host, int port, String filter) {
        client = MqttClient.builder().useMqttVersion5().identifier("probe-" + UUID.randomUUID())
                .serverHost(host).serverPort(port).buildAsync();
        try {
            client.connectWith().cleanStart(true).send().get(10, TimeUnit.SECONDS);
            client.subscribe(Mqtt5Subscribe.builder().topicFilter(filter).qos(MqttQos.AT_LEAST_ONCE).build(),
                    this::record).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("probe could not subscribe", e);
        }
    }

    /** A probe on the shared broker's anonymous listener subscribed to {@code filter}. */
    public static MqttProbe subscribe(MosquittoSupport broker, String filter) {
        return new MqttProbe(broker.host(), broker.plainPort(), filter);
    }

    private void record(Mqtt5Publish p) {
        received.add(new Received(p.getTopic().toString(), new String(p.getPayloadAsBytes(), StandardCharsets.UTF_8),
                p.isRetain(), p.getMessageExpiryInterval().isPresent() ? p.getMessageExpiryInterval().getAsLong() : null,
                p.getContentType().map(Object::toString).orElse(null)));
    }

    /** Everything received so far. */
    public List<Received> received() {
        return List.copyOf(received);
    }

    /** Everything received on {@code topic}. */
    public List<Received> on(String topic) {
        return received.stream().filter(r -> r.topic().equals(topic)).toList();
    }

    /** Waits up to {@code timeout} for a message on {@code topic} matching {@code match}, and returns it. */
    public Received await(String topic, Predicate<Received> match, Duration timeout) {
        Received[] found = new Received[1];
        Awaitility.await().atMost(timeout).pollInterval(Duration.ofMillis(50)).until(() -> {
            for (Received r : received) {
                if (r.topic().equals(topic) && match.test(r)) {
                    found[0] = r;
                    return true;
                }
            }
            return false;
        });
        return found[0];
    }

    /** Waits up to 15 s for a message on {@code topic}. */
    public Received await(String topic) {
        return await(topic, r -> true, Duration.ofSeconds(15));
    }

    /** Publishes {@code payload} on {@code topic} at QoS 1, not retained (Home Assistant's birth message). */
    public void publish(String topic, String payload) {
        try {
            client.publish(Mqtt5Publish.builder().topic(topic).payload(payload.getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.AT_LEAST_ONCE).build()).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("probe could not publish", e);
        }
    }

    /** Forgets what was received. */
    public void clear() {
        received.clear();
    }

    @Override
    public void close() {
        try {
            client.disconnect().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            // already gone
        }
    }
}
