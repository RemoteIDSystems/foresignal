package ai.foresignal.sink.types;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkSupport;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code mqtt-state} (prompt 110; STATE_MIRROR, PUSH): publishes the retained current state per record
 * to {@code {prefix}/state/{sourceId}/{externalId}} (QoS 1), with an optional non-retained
 * {@code {prefix}/changes/{sourceId}} companion. Removal publishes an empty retained payload. Reuses the
 * 060 MQTT client with an LWT on {@code {prefix}/status}.
 */
@Component
public class MqttStateSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "mqtt-state";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "MQTT state",
            "Retained per-record current state over MQTT",
            SinkKind.STATE_MIRROR, Set.of(SinkMode.PUSH),
            Map.of("type", "object", "properties", Map.of(
                    "brokerUrl", Map.of("type", "string"),
                    "prefix", Map.of("type", "string", "default", "foresignal"),
                    "changesTopic", Map.of("type", "boolean", "default", false),
                    "protocolVersion", Map.of("type", "string", "enum", List.of("5", "3.1.1"), "default", "5"))),
            List.of("username", "password", "caCertPem"), 50);

    private final MqttConnections connections;

    /** Creates the type. */
    public MqttStateSinkType(MqttConnections connections) {
        this.connections = connections;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public SinkTypeSpec spec() {
        return SPEC;
    }

    @Override
    public void validate(Map<String, Object> config, Map<String, String> secrets) {
        ai.foresignal.sink.SinkConfig validation = new SinkConfig("validation", ID, "validation", SinkMode.PUSH,
                config, secrets, ai.foresignal.sink.SinkFilter.ALL);
        String url = validation.config("brokerUrl");
        if (url == null) {
            throw ai.foresignal.api.error.ValidationException.forField("config.brokerUrl", "is required");
        }
        try {
            MqttEndpoint.parse(url);
        } catch (RuntimeException e) {
            throw ai.foresignal.api.error.ValidationException.forField("config.brokerUrl", "is not a valid MQTT URL");
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        MqttEndpoint endpoint = MqttEndpoint.parse(config.config("brokerUrl"));
        String prefix = config.config("prefix", "foresignal");
        MqttConnectionSpec spec = new MqttConnectionSpec(
                "sink:" + config.id(), config.name(), "foresignal-sink-" + SinkSupport.segment(config.id()), endpoint,
                config.config("protocolVersion", "5"), config.secrets().get("username"),
                config.secrets().get("password"), config.secrets().get("caCertPem"), prefix + "/status",
                MqttSessionOptions.defaults(), Map.of());
        MqttConnection connection = connections.open(spec, () -> {
        });
        boolean changes = config.boolConfig("changesTopic", false);
        return new Instance(connection, prefix, changes);
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final MqttConnection connection;
        private final String prefix;
        private final boolean changes;

        Instance(MqttConnection connection, String prefix, boolean changes) {
            this.connection = connection;
            this.prefix = prefix;
            this.changes = changes;
        }

        @Override
        public void upsert(SinkRecord record) {
            String topic = prefix + "/state/" + SinkSupport.segment(record.sourceId()) + "/"
                    + SinkSupport.segment(record.externalId());
            connection.publish(new MqttMessage(topic,
                    JSON.writeValueAsBytes(SinkSupport.statePayload(record)), 1, true, null, MqttMessage.JSON));
            if (changes) {
                connection.publish(new MqttMessage(prefix + "/changes/" + SinkSupport.segment(record.sourceId()),
                        JSON.writeValueAsBytes(SinkSupport.statePayload(record)), 1, false, null, MqttMessage.JSON));
            }
        }

        @Override
        public void remove(String sourceId, String externalId) {
            String topic = prefix + "/state/" + SinkSupport.segment(sourceId) + "/"
                    + SinkSupport.segment(externalId);
            connection.publish(new MqttMessage(topic, new byte[0], 1, true, null, null));
        }

        @Override
        public SinkTestResult test(SinkRecord probe) {
            long start = System.nanoTime();
            upsert(probe);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("connected", connection.connected());
            return new SinkTestResult(true, "send", "published one retained state",
                    (System.nanoTime() - start) / 1_000_000, details);
        }

        @Override
        public void close() {
            connection.close();
        }
    }
}
