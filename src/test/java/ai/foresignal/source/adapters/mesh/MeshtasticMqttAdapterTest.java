package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * {@code meshtastic-mqtt} and {@code meshtastic-local} against a real Mosquitto broker in Testcontainers (prompt 083,
 * the 060 {@link MosquittoSupport} broker): exactly the configured subscriptions, one budget unit per CONNECT and per
 * SUBSCRIBE, nodeinfo and position merged into one snapshot, protobuf topics dropped, the own node's text on the
 * {@link MeshMessageBus}, and a lost connection reported to the supervisor instead of being retried.
 */
class MeshtasticMqttAdapterTest {

    private final MosquittoSupport broker = MosquittoSupport.get();
    private final MqttConnections connections = new MqttConnections(MqttProperties.defaults(),
            OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName),
            new StaticListableBeanFactory().getBeanProvider(MeterRegistry.class), Clock.systemUTC());
    private final List<StreamHandle> handles = new CopyOnWriteArrayList<>();

    @AfterEach
    void close() {
        handles.forEach(StreamHandle::close);
        connections.closeAll();
    }

    private static String root() {
        return "msh/T" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Test
    @DisplayName("public: only <root>/2/json/# per root; CONNECT plus each SUBSCRIBE charged; nodeinfo + position merge")
    void publicSubscriptionsAndMerge() throws Exception {
        String root = root();
        String extra = root();
        MeshtasticMqttAdapter adapter = new MeshtasticMqttAdapter(connections, () -> "itest083", new MeshMessageBus(),
                null, broker.plainUrl());
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(), ValidatedParams.of(Map.of(
                MeshtasticMqttAdapter.ROOT, root, MeshtasticMqttAdapter.EXTRA_ROOTS, List.of(extra),
                MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 2)));
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        handles.add(adapter.start(ctx, records::add));
        assertThat(ctx.charged.get()).isEqualTo(3);

        try (MqttProbe publisher = MqttProbe.subscribe(broker, "unused/" + UUID.randomUUID())) {
            publisher.publish(root + "/2/json/LongFast/!a0b1c2d3", "{\"from\":305419896,\"type\":\"nodeinfo\","
                    + "\"timestamp\":1791900000,\"payload\":{\"longname\":\"Synthetic Hiker\",\"shortname\":\"SHIK\","
                    + "\"hardware\":9,\"role\":0}}");
            await().atMost(Duration.ofSeconds(10)).until(() -> records.size() == 1);
            publisher.publish(root + "/2/json/LongFast/!a0b1c2d3", "{\"from\":305419896,\"type\":\"position\","
                    + "\"timestamp\":1791900060,\"payload\":{\"latitude_i\":377800000,\"longitude_i\":-1224300000,"
                    + "\"precision_bits\":16}}");
            // Outside the subscriptions: protobuf, another root's JSON, a sibling tree.
            publisher.publish(root + "/2/e/LongFast/!a0b1c2d3", "{\"from\":1,\"type\":\"text\",\"payload\":{\"text\":\"x\"}}");
            publisher.publish(root() + "/2/json/LongFast/!a0b1c2d3", "{\"from\":2,\"type\":\"nodeinfo\",\"payload\":{}}");
            publisher.publish(root + "/2/map/!a0b1c2d3", "{}");
            publisher.publish(extra + "/2/json/MediumFast/!11111111", "{\"from\":3,\"type\":\"nodeinfo\",\"payload\":"
                    + "{\"longname\":\"Synthetic Extra\"}}");
            await().atMost(Duration.ofSeconds(10)).until(() -> records.size() == 3);
            Thread.sleep(1_000);
        }
        assertThat(records).hasSize(3);
        NormalizedRecord merged = records.stream().filter(r -> r.externalId().equals("!12345678")).reduce((a, b) -> b)
                .orElseThrow();
        assertThat(merged.payload()).containsEntry("longName", "Synthetic Hiker").containsEntry("role", "CLIENT")
                .containsEntry("positionPrecisionBits", 16);
        assertThat(merged.geometry()).isNotNull();
        assertThat(merged.quality().accuracyMeters()).isEqualTo(364.8);
        assertThat(merged.observers()).extracting(o -> o.receiverId()).containsExactly("!a0b1c2d3");
        assertThat(records).extracting(NormalizedRecord::externalId).contains("!00000003")
                .doesNotContain("!00000001", "!00000002");
        assertThat(ctx.bytes.get()).isPositive();
    }

    @Test
    @DisplayName("local: the operator's broker with credentials, one subscription per channel, text on the bus")
    void localChannelsAndBus() throws Exception {
        String root = root();
        MeshMessageBus bus = new MeshMessageBus();
        List<MeshTextMessage> heard = new CopyOnWriteArrayList<>();
        bus.subscribe(heard::add);
        MeshtasticLocalAdapter adapter = new MeshtasticLocalAdapter(connections, () -> "itest083", bus, null);
        URI url = URI.create(broker.passwordUrl());
        ValidatedParams params = new ValidatedParams(Map.of(MeshtasticLocalAdapter.BROKER_URL, URI.create("mqtt://"
                + url.getHost() + ":" + url.getPort()), MeshtasticLocalAdapter.ROOT, root,
                MeshtasticLocalAdapter.CHANNELS, List.of("Ops")),
                Map.of(MeshtasticLocalAdapter.BROKER_URL, InetAddress.getByName(url.getHost())));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(), params)
                .withCredentials(Map.of(MeshtasticLocalAdapter.USERNAME, MosquittoSupport.USER,
                        MeshtasticLocalAdapter.PASSWORD, MosquittoSupport.PASSWORD));
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        handles.add(adapter.start(ctx, records::add));
        assertThat(ctx.charged.get()).isEqualTo(2);

        try (MqttProbe publisher = MqttProbe.subscribe(broker, "unused/" + UUID.randomUUID())) {
            publisher.publish(root + "/2/json/Ops/!a0b1c2d3", "{\"channel\":1,\"from\":305419896,\"to\":4294967295,"
                    + "\"id\":7,\"type\":\"text\",\"timestamp\":1791900000,\"payload\":{\"text\":\"#E synthetic\"}}");
            publisher.publish(root + "/2/json/LongFast/!a0b1c2d3", "{\"from\":1,\"type\":\"nodeinfo\",\"payload\":{}}");
            await().atMost(Duration.ofSeconds(10)).until(() -> heard.size() == 1);
            Thread.sleep(1_000);
        }
        assertThat(heard).singleElement().satisfies(m -> {
            assertThat(m.sourceId()).isEqualTo(MeshtasticLocalAdapter.ID);
            assertThat(m.channelIndex()).isEqualTo(1);
            assertThat(m.channelName()).isEqualTo("Ops");
            assertThat(m.fromId()).isEqualTo("!12345678");
            assertThat(m.text()).isEqualTo("#E synthetic");
        });
        assertThat(records).as("includeText is off and LongFast is not subscribed").isEmpty();
    }

    @Test
    @DisplayName("a lost connection is reported through onClose, never retried by the connection itself")
    void dropReported() throws Exception {
        MeshtasticMqttAdapter adapter = new MeshtasticMqttAdapter(connections, () -> "itest083b", new MeshMessageBus(),
                null, broker.proxiedUrl());
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(),
                ValidatedParams.of(Map.of(MeshtasticMqttAdapter.ROOT, root())));
        StreamHandle handle = adapter.start(ctx, record -> {});
        handles.add(handle);
        List<Throwable> closed = new CopyOnWriteArrayList<>();
        handle.onClose(closed::add);
        try {
            broker.cut();
            await().atMost(Duration.ofSeconds(30)).until(() -> !closed.isEmpty());
        } finally {
            broker.restore();
        }
        assertThat(handle.isRunning()).isFalse();
        Thread.sleep(3_000);
        assertThat(ctx.charged.get()).as("no further CONNECT was paid for").isEqualTo(2);
    }
}
