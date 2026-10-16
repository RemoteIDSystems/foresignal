package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ris-live} (prompt 092): narrow, well-formed subscriptions (per host when asked), state-change emission,
 * CIDR more-specifics, host filtering and the no-data record when the stream drops.
 */
class RisLiveAdapterTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** A RIS Live UPDATE in the manual's shape: the path on the message, prefixes per announcement. */
    private static String message(String host, String path, String... prefixes) {
        StringBuilder list = new StringBuilder();
        for (String prefix : prefixes) {
            list.append(list.isEmpty() ? "" : ",").append('"').append(prefix).append('"');
        }
        return "{\"type\":\"ris_message\",\"data\":{\"timestamp\":1791996000.0,\"host\":\"" + host + "\","
                + "\"peer\":\"192.0.2.1\",\"peer_asn\":\"64500\",\"type\":\"UPDATE\",\"path\":" + path + ","
                + "\"announcements\":[{\"next_hop\":\"192.0.2.1\",\"prefixes\":[" + list + "]}],\"withdrawals\":[]}}";
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20);
        }
    }

    private static RisTestContext context(RisLiveAdapter adapter, Map<String, Object> params) {
        return new RisTestContext(adapter.descriptor(), new MutableClock(NOW), ValidatedParams.of(params));
    }

    @Test
    @DisplayName("subscribes only to the watched prefix with valid JSON, emits on change and not on an identical snapshot")
    void stream() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(context(adapter, Map.of("prefixes", List.of("1.1.1.0/24"))),
                    sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.awaitReceived(2, Duration.ofSeconds(5));
                List<JsonNode> subscribes = server.received().stream().map(r -> JSON.readTree(r.text()))
                        .filter(n -> "ris_subscribe".equals(n.path("type").asString(""))).toList();
                assertThat(subscribes).hasSize(1);
                assertThat(subscribes.get(0).path("data").path("prefix").asString()).isEqualTo("1.1.1.0/24");
                assertThat(subscribes.get(0).path("data").path("moreSpecific").asBoolean()).isTrue();
                assertThat(subscribes.get(0).path("data").has("host")).isFalse();
                assertThat(server.received()).anyMatch(r -> r.text().contains("RIS_PEER_STATE"));

                server.sendText(message("rrc00", "[64500,3356,13335]", "1.1.1.0/24"));
                await(() -> sink.size() == 1);
                assertThat(sink).hasSize(1);
                assertThat(sink.get(0).kind()).isEqualTo("bgp-prefix-state");
                assertThat(sink.get(0).payload()).containsEntry("visibility", 1.0).containsEntry("status", "live");
                assertThat(sink.get(0).payload().get("origins"))
                        .isEqualTo(List.of(Map.of("asn", 13335, "asSet", false)));

                server.sendText(message("rrc00", "[64500,3356,13335]", "1.1.1.0/24"));
                Thread.sleep(200);
                assertThat(sink).as("an unchanged snapshot does not emit").hasSize(1);
            } finally {
                handle.close();
            }
        }
    }

    @Test
    @DisplayName("a more-specific is matched by CIDR, a text look-alike is not, and the origin is checked")
    void moreSpecificsAndOrigin() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(context(adapter, Map.of("prefixes", List.of("192.168.1.0/24"),
                    "expectedOrigins", "192.168.1.0/24=13335")), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.sendText(message("rrc00", "[64500,13335]", "192.168.1.0/24"));
                await(() -> sink.size() == 1);
                assertThat(sink.get(0).payload()).containsEntry("unexpectedOrigin", false);

                server.sendText(message("rrc00", "[64500,64666]", "192.168.10.0/24"));
                Thread.sleep(200);
                assertThat(sink).as("192.168.10.0/24 is not under 192.168.1.0/24").hasSize(1);

                server.sendText(message("rrc00", "[64500,64666]", "192.168.1.128/25"));
                await(() -> sink.size() == 2);
                assertThat(sink.get(1).payload().get("moreSpecifics")).isEqualTo(List.of("192.168.1.128/25"));

                server.sendText(message("rrc00", "[64500,64666]", "192.168.1.0/24"));
                await(() -> sink.size() == 3);
                assertThat(sink.get(2).payload()).containsEntry("unexpectedOrigin", true);
            } finally {
                handle.close();
            }
        }
    }

    @Test
    @DisplayName("hosts: one subscription per prefix and host, and other collectors' messages are ignored")
    void hosts() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(context(adapter, Map.of("prefixes", List.of("1.1.1.0/24"),
                    "hosts", List.of("rrc01", "rrc03"))), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.awaitReceived(3, Duration.ofSeconds(5));
                List<String> hosts = server.received().stream().map(r -> JSON.readTree(r.text()))
                        .filter(n -> "ris_subscribe".equals(n.path("type").asString("")))
                        .map(n -> n.path("data").path("host").asString()).toList();
                assertThat(hosts).containsExactlyInAnyOrder("rrc01", "rrc03");

                server.sendText(message("rrc00", "[64500,13335]", "1.1.1.0/24"));
                Thread.sleep(200);
                assertThat(sink).as("rrc00 is not a configured host").isEmpty();
                server.sendText(message("rrc01", "[64500,13335]", "1.1.1.0/24"));
                await(() -> sink.size() == 1);
                assertThat(sink).hasSize(1);
            } finally {
                handle.close();
            }
        }
    }

    @Test
    @DisplayName("when the stream drops, each prefix is emitted with status no-data and its last visibility")
    void noDataOnDisconnect() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(context(adapter, Map.of("prefixes", List.of("1.1.1.0/24"))),
                    sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.sendText(message("rrc00", "[64500,13335]", "1.1.1.0/24"));
                await(() -> sink.size() == 1);
                server.closeAll(1011, "going away");
                await(() -> sink.size() == 2);
                assertThat(sink).hasSize(2);
                assertThat(sink.get(1).payload()).containsEntry("status", "no-data").containsEntry("visibility", 1.0);
                assertThat(handle.isRunning()).isFalse();
            } finally {
                handle.close();
            }
        }
    }

    @Test
    @DisplayName("subscription JSON is serialized, so a hostile prefix cannot inject fields, and invalid prefixes are dropped")
    void subscribeSerialization() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            StreamHandle handle = adapter.start(context(adapter, Map.of("prefixes",
                    List.of("1.1.1.0/24", "1.1.1.0/24\",\"moreSpecific\":false,\"x\":\"", "2001:DB8::/32"))), r -> { });
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.awaitReceived(3, Duration.ofSeconds(5));
                List<JsonNode> subscribes = server.received().stream().map(r -> JSON.readTree(r.text()))
                        .filter(n -> "ris_subscribe".equals(n.path("type").asString(""))).toList();
                assertThat(subscribes).extracting(n -> n.path("data").path("prefix").asString())
                        .containsExactly("1.1.1.0/24", "2001:db8:0:0:0:0:0:0/32");
                assertThat(subscribes).allMatch(n -> n.path("data").path("moreSpecific").asBoolean());
            } finally {
                handle.close();
            }
        }
    }
}
