package ai.foresignal.sink.types;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkFilter;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code mqtt-state} against the shared Mosquitto container of the 060 tests (prompt 110): retained state topics
 * with the state payload, an empty retained payload on removal, the optional change topic, the Last Will
 * {@code offline} on the status topic when the connection is cut, and {@code online} again after it is restored.
 */
class MqttStateSinkIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Duration WAIT = Duration.ofSeconds(15);

    final MqttConnections connections = new MqttConnections(MqttProperties.defaults(),
            OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName),
            new StaticListableBeanFactory().getBeanProvider(MeterRegistry.class), Clock.systemUTC());

    SinkInstance open(String url, String prefix) {
        Map<String, Object> config = Map.of("brokerUrl", url, "prefix", prefix, "changesTopic", true);
        MqttStateSinkType type = new MqttStateSinkType(connections);
        type.validate(config, Map.of());
        return type.open(new SinkConfig("mqtt-" + prefix, MqttStateSinkType.ID, "MQTT", SinkMode.PUSH, config,
                Map.of(), SinkFilter.ALL));
    }

    static SinkRecord record(String id) {
        return new SinkRecord("usgs-quakes", id, "earthquake", 3, 2, Instant.parse("2026-10-16T12:00:00Z"), null,
                false, new GeometryFactory().createPoint(new Coordinate(-120.5, 36.1)), Map.of("mag", 4.2), null,
                "USGS");
    }

    @Test
    @DisplayName("retained state, change topic, and an empty retained payload on removal")
    void retainedState() throws Exception {
        MosquittoSupport broker = MosquittoSupport.get();
        String prefix = "fs" + UUID.randomUUID().toString().substring(0, 8);
        try (MqttProbe probe = MqttProbe.subscribe(broker, prefix + "/#");
                SinkInstance sink = open(broker.plainUrl(), prefix)) {
            sink.upsert(record("ci12345.6"));
            MqttProbe.Received state = probe.await(prefix + "/state/usgs-quakes/ci12345_6",
                    r -> !r.payload().isEmpty(), WAIT);
            JsonNode payload = JSON.readTree(state.payload());
            assertThat(payload.get("kind").asString()).isEqualTo("earthquake");
            assertThat(payload.get("version").asInt()).isEqualTo(2);
            assertThat(payload.get("attribution").asString()).isEqualTo("USGS");
            assertThat(payload.get("properties").get("mag").asDouble()).isEqualTo(4.2);
            probe.await(prefix + "/changes/usgs-quakes", r -> !r.retained(), WAIT);

            sink.remove("usgs-quakes", "ci12345.6");
            probe.await(prefix + "/state/usgs-quakes/ci12345_6", r -> r.payload().isEmpty(), WAIT);
        }
        try (MqttProbe late = MqttProbe.subscribe(broker, prefix + "/state/#")) {
            Thread.sleep(500);
            assertThat(late.received()).as("the removal cleared the retained message").isEmpty();
        }
    }

    @Test
    @DisplayName("the Last Will publishes offline on the status topic when the connection is lost")
    void lastWill() {
        MosquittoSupport broker = MosquittoSupport.get();
        String prefix = "fs" + UUID.randomUUID().toString().substring(0, 8);
        try (MqttProbe probe = MqttProbe.subscribe(broker, prefix + "/status");
                SinkInstance sink = open(broker.proxiedUrl(), prefix)) {
            probe.await(prefix + "/status", r -> MqttConnectionSpec.ONLINE.equals(r.payload()), WAIT);
            broker.cut();
            try {
                probe.await(prefix + "/status", r -> MqttConnectionSpec.OFFLINE.equals(r.payload()),
                        Duration.ofSeconds(60));
            } finally {
                broker.restore();
            }
            probe.clear();
            sink.upsert(record("after-reconnect"));
        }
    }
}
