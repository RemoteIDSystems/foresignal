package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mqtt.MqttInbound;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MeshtasticJsonParser} (prompt 083): each JSON type from the private-broker fixture, unsigned and signed node
 * numbers, unknown types dropped, protobuf topics dropped without parsing, and the real public-broker sample with its
 * third-party variants.
 */
class MeshtasticJsonParserTest {

    private static MeshtasticJsonParser.Result parse(String topic, String json) {
        return MeshtasticJsonParser.parse(topic, json.getBytes(StandardCharsets.UTF_8));
    }

    private static List<MqttInbound> local() {
        return MeshFixtures.mqtt("meshtastic-local", "private-broker-5min.tsv");
    }

    @Test
    @DisplayName("nodeinfo: long and short name, hardware and role numbers, the gateway and channel from the topic")
    void nodeinfo() {
        MqttInbound first = local().get(0);
        MeshtasticPacket packet = MeshtasticJsonParser.parse(first.topic(), first.payload()).packet();
        assertThat(packet.from()).isEqualTo("!a0b1c2d3");
        assertThat(packet.gatewayId()).isEqualTo("!a0b1c2d3");
        assertThat(packet.channelName()).isEqualTo("LongFast");
        assertThat(packet.to()).isEqualTo(MeshtasticPacket.BROADCAST);
        assertThat(packet.timestamp()).isEqualTo(Instant.ofEpochSecond(1791900000));
        assertThat(packet.payload()).isEqualTo(new MeshtasticPacket.NodeInfo("Synthetic Base", "SBAS", 43, 2));
    }

    @Test
    @DisplayName("position: latitude_i and longitude_i × 1e-7, altitude and precision bits")
    void position() {
        MqttInbound message = local().get(1);
        MeshtasticPacket.Position position = (MeshtasticPacket.Position) MeshtasticJsonParser
                .parse(message.topic(), message.payload()).packet().payload();
        assertThat(position.latitude()).isEqualTo(37.7749);
        assertThat(position.longitude()).isEqualTo(-122.4194);
        assertThat(position.altitude()).isEqualTo(15);
        assertThat(position.precisionBits()).isEqualTo(32);
    }

    @Test
    @DisplayName("telemetry: the device metrics; an environment-only message has none")
    void telemetry() {
        MqttInbound device = local().get(2);
        assertThat(MeshtasticJsonParser.parse(device.topic(), device.payload()).packet().payload())
                .isEqualTo(new MeshtasticPacket.Telemetry(87, 4.05, 3.5, 1.2, 3600L));
        MqttInbound environment = local().get(10);
        MeshtasticPacket.Telemetry env = (MeshtasticPacket.Telemetry) MeshtasticJsonParser
                .parse(environment.topic(), environment.payload()).packet().payload();
        assertThat(env.empty()).isTrue();
    }

    @Test
    @DisplayName("text: broadcast, direct and on another channel, with RSSI, SNR and hops")
    void text() {
        MqttInbound broadcast = local().get(6);
        MeshtasticPacket packet = MeshtasticJsonParser.parse(broadcast.topic(), broadcast.payload()).packet();
        assertThat(packet.payload()).isEqualTo(new MeshtasticPacket.Text("synthetic hello"));
        assertThat(packet.packetId()).isEqualTo(1001L);
        assertThat(packet.rssi()).isEqualTo(-95.0);
        assertThat(packet.snr()).isEqualTo(6.25);
        assertThat(packet.hopsAway()).isEqualTo(1);
        MqttInbound direct = local().get(7);
        assertThat(MeshtasticJsonParser.parse(direct.topic(), direct.payload()).packet().to()).isEqualTo("!a0b1c2d3");
        MqttInbound ops = local().get(8);
        MeshtasticPacket opsPacket = MeshtasticJsonParser.parse(ops.topic(), ops.payload()).packet();
        assertThat(opsPacket.channelName()).isEqualTo("Ops");
        assertThat(opsPacket.channel()).isEqualTo(1);
    }

    @Test
    @DisplayName("node numbers are unsigned: values of 2^31 and more, and signed values from old firmware")
    void unsignedNodeIds() {
        MqttInbound legacy = local().get(9);
        assertThat(MeshtasticJsonParser.parse(legacy.topic(), legacy.payload()).packet().from()).isEqualTo("!aabbccdd");
        assertThat(parse("msh/x/2/json/LongFast/!00000001", "{\"from\":4294967040,\"type\":\"text\",\"payload\":"
                + "{\"text\":\"t\"},\"to\":-1}").packet()).satisfies(p -> {
                    assertThat(p.from()).isEqualTo("!ffffff00");
                    assertThat(p.to()).isEqualTo("!ffffffff");
                });
        assertThat(MeshRecords.meshtasticNodeId(2_147_483_648L)).isEqualTo("!80000000");
        assertThat(MeshRecords.meshtasticNodeId(-1)).isEqualTo("!ffffffff");
        assertThat(parse("msh/x/2/json/c/!1", "{\"from\":\"!0000abcd\",\"type\":\"nodeinfo\",\"payload\":{}}")
                .packet().from()).isEqualTo("!0000abcd");
        assertThat(parse("msh/x/2/json/c/!1", "{\"from\":\"20000017\",\"type\":\"nodeinfo\",\"payload\":{}}").drop())
                .isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
    }

    @Test
    @DisplayName("an unknown type is dropped as UNKNOWN_TYPE for the caller to count")
    void unknownType() {
        MqttInbound detection = local().get(11);
        assertThat(MeshtasticJsonParser.parse(detection.topic(), detection.payload()).drop())
                .isEqualTo(MeshtasticJsonParser.Drop.UNKNOWN_TYPE);
        assertThat(parse("msh/US/2/json/LongFast/!1", "{\"from\":1,\"type\":\"traceroute\",\"payload\":{}}").drop())
                .isEqualTo(MeshtasticJsonParser.Drop.UNKNOWN_TYPE);
    }

    @Test
    @DisplayName("a /2/e/ or /2/map/ topic is dropped by its topic, without its body ever being parsed")
    void protobufTopicIgnored() {
        MqttInbound protobuf = local().get(14);
        assertThat(protobuf.topic()).contains("/2/e/");
        assertThat(new String(protobuf.payload(), StandardCharsets.UTF_8)).doesNotStartWith("{");
        assertThat(MeshtasticJsonParser.parse(protobuf.topic(), protobuf.payload()).drop())
                .isEqualTo(MeshtasticJsonParser.Drop.PROTOBUF_TOPIC);
        // A body that would parse as JSON is still never looked at on a protobuf topic.
        assertThat(parse("msh/US/2/map/LongFast/!1", "{\"from\":1,\"type\":\"text\",\"payload\":{\"text\":\"x\"}}")
                .drop()).isEqualTo(MeshtasticJsonParser.Drop.PROTOBUF_TOPIC);
        assertThat(MeshtasticJsonParser.isProtobufTopic("msh/US/2/e/LongFast/!1")).isTrue();
        assertThat(MeshtasticJsonParser.isProtobufTopic("msh/US/2/json/LongFast/!1")).isFalse();
    }

    @Test
    @DisplayName("other topics and malformed bodies are dropped")
    void malformed() {
        assertThat(parse("msh/US/2/json/LongFast", "{\"from\":1,\"type\":\"text\"}").drop())
                .isEqualTo(MeshtasticJsonParser.Drop.NOT_JSON_TOPIC);
        assertThat(parse("msh/US/stat/!1", "{}").drop()).isEqualTo(MeshtasticJsonParser.Drop.NOT_JSON_TOPIC);
        assertThat(parse("msh/US/2/json/c/!1", "not json").drop()).isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
        assertThat(parse("msh/US/2/json/c/!1", "[1,2]").drop()).isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
        assertThat(parse("msh/US/2/json/c/!1", "{\"type\":\"text\"}").drop()).isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
        assertThat(parse("msh/US/2/json/c/!1", "{\"from\":1,\"type\":\"\"}").drop())
                .isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
        assertThat(parse("msh/US/2/json/c/!1", "{\"from\":1,\"type\":\"text\",\"payload\":{}}").drop())
                .isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
        assertThat(MeshtasticJsonParser.parse("msh/US/2/json/c/!1",
                new byte[MeshtasticJsonParser.MAX_BODY_BYTES + 1]).drop()).isEqualTo(MeshtasticJsonParser.Drop.MALFORMED);
    }

    @Test
    @DisplayName("a zero position and zero RSSI/SNR are absent; a missing timestamp is null")
    void zeroes() {
        MeshtasticPacket packet = parse("msh/US/2/json/c/!1", "{\"from\":1,\"type\":\"position\",\"rssi\":0,\"snr\":0,"
                + "\"timestamp\":0,\"payload\":{\"latitude_i\":0,\"longitude_i\":0}}").packet();
        assertThat(packet.rssi()).isNull();
        assertThat(packet.snr()).isNull();
        assertThat(packet.timestamp()).isNull();
        assertThat(((MeshtasticPacket.Position) packet.payload()).latitude()).isNull();
    }

    @Test
    @DisplayName("the real public-broker sample: 59 messages, firmware JSON parsed, third-party variants dropped")
    void publicSample() {
        List<MqttInbound> messages = MeshFixtures.mqtt("meshtastic-mqtt", "public-json-sample.tsv");
        assertThat(messages).hasSize(59);
        Map<String, Long> outcome = messages.stream().map(m -> MeshtasticJsonParser.parse(m.topic(), m.payload()))
                .collect(Collectors.groupingBy(r -> r.packet() != null ? r.packet().type() : r.drop().name(),
                        Collectors.counting()));
        assertThat(outcome).containsEntry("NOT_JSON_TOPIC", 1L).containsEntry("UNKNOWN_TYPE", 3L)
                .containsEntry("MALFORMED", 17L).containsEntry("text", 1L);
        assertThat(outcome.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(59);
    }
}
