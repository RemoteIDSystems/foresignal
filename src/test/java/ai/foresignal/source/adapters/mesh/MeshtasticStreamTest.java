package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.source.spi.NormalizedRecord;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MeshtasticStream} over the fixtures (prompt 083): the per-node merge into complete snapshots (a telemetry
 * message keeps the last position), the gateway as observer with RSSI and SNR, text on the bus and as records only with
 * {@code includeText}, and the drop counters.
 */
class MeshtasticStreamTest {

    private static final Instant RECEIVED = Instant.parse("2026-10-14T00:00:00Z");

    private record Run(List<NormalizedRecord> records, List<MeshTextMessage> messages, MeshtasticStream stream) {}

    private static Run run(String sourceId, String fixture, boolean includeText, boolean publish) {
        MeshMessageBus bus = new MeshMessageBus();
        List<MeshTextMessage> messages = new ArrayList<>();
        bus.subscribe(messages::add);
        SimpleMeterRegistry meters = new SimpleMeterRegistry();
        MeshtasticStream stream = new MeshtasticStream("meshtastic-local", includeText, publish ? bus : null,
                new SlidingWindowLimiter(1_000, Duration.ofMinutes(1)), new MeshNodeStates(), () -> meters);
        List<NormalizedRecord> records = new ArrayList<>();
        for (MqttInbound message : MeshFixtures.mqtt(sourceId, fixture)) {
            records.addAll(stream.onMessage(message, RECEIVED));
        }
        return new Run(records, messages, stream);
    }

    @Test
    @DisplayName("the private-broker capture: 11 node snapshots of 3 nodes, 3 bus messages, protobuf and unknown dropped")
    void privateBroker() {
        Run run = run("meshtastic-local", "private-broker-5min.tsv", false, true);
        assertThat(run.records()).hasSize(11).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(MeshRecords.NODE_KIND);
            assertThat(r.payload().values()).doesNotContainNull();
            assertThat(r.observers()).extracting(o -> o.receiverId()).containsExactly("!a0b1c2d3");
        });
        assertThat(run.records()).extracting(NormalizedRecord::externalId).containsOnly("!a0b1c2d3", "!12345678",
                "!aabbccdd");
        assertThat(run.messages()).hasSize(3);
        assertThat(run.stream().drops()).containsEntry("protobuf", 1L).containsEntry("unknown-type", 1L).hasSize(2);

        NormalizedRecord hiker = run.records().stream().filter(r -> r.externalId().equals("!12345678"))
                .reduce((a, b) -> b).orElseThrow();
        assertThat(hiker.payload()).containsEntry("longName", "Synthetic Hiker").containsEntry("shortName", "SHIK")
                .containsEntry("role", "CLIENT").containsEntry("hwModel", "9").containsEntry("batteryLevel", 11)
                .containsEntry("lowBattery", true).containsEntry("positionPrecisionBits", 16)
                .containsEntry("lastHeardAt", Instant.ofEpochSecond(1791900270).toString());
        assertThat(hiker.geometry().getCoordinate().y).isEqualTo(37.781);
        assertThat(hiker.quality().accuracyMeters()).isEqualTo(364.8);
        assertThat(hiker.observers().get(0).rssi()).isEqualTo(-97.0);
        assertThat(hiker.observers().get(0).snr()).isEqualTo(5.75);
        assertThat(hiker.observedAt()).isEqualTo(Instant.ofEpochSecond(1791900270));

        // The telemetry-only snapshot still carries the earlier position (adapter rule 7).
        NormalizedRecord telemetry = run.records().get(5);
        assertThat(telemetry.payload()).containsEntry("batteryLevel", 12).containsEntry("longName", "Synthetic Hiker");
        assertThat(telemetry.geometry()).isNotNull();

        NormalizedRecord base = run.records().get(run.records().size() - 1);
        assertThat(base.externalId()).isEqualTo("!a0b1c2d3");
        assertThat(base.payload()).containsEntry("role", "ROUTER").containsEntry("positionPrecisionBits", 32)
                .containsEntry("lowBattery", false);
        assertThat(base.quality().accuracyMeters()).as("32 bits is exact").isNull();
    }

    @Test
    @DisplayName("text: direct messages have no channel and an addressee; records only with includeText")
    void text() {
        Run without = run("meshtastic-local", "private-broker-5min.tsv", false, true);
        MeshTextMessage direct = without.messages().get(1);
        assertThat(direct.direct()).isTrue();
        assertThat(direct.toId()).isEqualTo("!a0b1c2d3");
        MeshTextMessage report = without.messages().get(2);
        assertThat(report.channelIndex()).isEqualTo(1);
        assertThat(report.channelName()).isEqualTo("Ops");
        assertThat(report.text()).isEqualTo("#E synthetic report");

        Run with = run("meshtastic-local", "private-broker-5min.tsv", true, true);
        List<NormalizedRecord> texts = with.records().stream().filter(r -> r.kind().equals(MeshRecords.MESSAGE_KIND))
                .toList();
        assertThat(texts).extracting(NormalizedRecord::externalId).containsExactly("!12345678:1001", "!12345678:1002",
                "!a0b1c2d3:1003");
        assertThat(texts.get(0).payload()).containsEntry("text", "synthetic hello").containsEntry("channelIndex", 0)
                .containsEntry("fromId", "!12345678").doesNotContainKey("toId");
        assertThat(texts.get(0).observedAt()).isEqualTo(Instant.ofEpochSecond(1791900090));
    }

    @Test
    @DisplayName("the public sample: 37 snapshots of 26 nodes; nothing published to the bus by the public source")
    void publicSample() {
        Run run = run("meshtastic-mqtt", "public-json-sample.tsv", false, false);
        assertThat(run.records()).hasSize(37);
        assertThat(run.records().stream().map(NormalizedRecord::externalId).distinct()).hasSize(26);
        assertThat(run.messages()).isEmpty();
        assertThat(run.stream().drops()).containsEntry("malformed", 17L).containsEntry("unknown-type", 3L)
                .containsEntry("topic", 1L);
        assertThat(run.records()).allSatisfy(r -> assertThat(r.payload().values()).doesNotContainNull());
    }

    @Test
    @DisplayName("a missing timestamp falls back to the receive time; the merge map is bounded")
    void fallbacks() {
        MeshtasticStream stream = new MeshtasticStream("x", false, null, new SlidingWindowLimiter(10, Duration.ofMinutes(1)),
                new MeshNodeStates(2), null);
        List<NormalizedRecord> records = stream.onMessage(new MqttInbound("msh/x/2/json/c/!1",
                "{\"from\":5,\"type\":\"nodeinfo\",\"payload\":{\"longname\":\"A\"}}".getBytes(), false), RECEIVED);
        assertThat(records.get(0).observedAt()).isEqualTo(RECEIVED);
        MeshNodeStates states = new MeshNodeStates(2);
        states.update("a", "meshtastic", s -> s);
        states.update("b", "meshtastic", s -> s);
        states.update("c", "meshtastic", s -> s);
        assertThat(states.size()).isEqualTo(2);
        assertThat(states.contains("a")).isFalse();
        assertThat(MeshRecords.accuracyMeters(10)).isEqualTo(23_345.5);
        assertThat(MeshRecords.accuracyMeters(0)).isNull();
        assertThat(MeshRecords.meshtasticRole(42)).isEqualTo("42");
        assertThat(MeshRecords.meshtasticRole(null)).isNull();
    }
}
