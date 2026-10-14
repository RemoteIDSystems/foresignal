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

/** {@code ris-live} (prompt 092): narrow subscription, state-change emission, no firehose. */
class RisLiveAdapterTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    private static String message(String path) {
        return "{\"type\":\"ris_message\",\"data\":{\"timestamp\":1791996000.0,\"host\":\"rrc00\",\"peer\":\"64500\","
                + "\"type\":\"UPDATE\",\"announcements\":[{\"next_hop\":\"1.1.1.1\",\"prefix\":\"1.1.1.0/24\","
                + "\"path\":" + path + "}],\"withdrawals\":[]}}";
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

    @Test
    @DisplayName("subscribes only to the watched prefix, emits on change and not on an identical snapshot")
    void stream() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            RisLiveAdapter adapter = new RisLiveAdapter(server.uri().toString());
            RisTestContext ctx = new RisTestContext(adapter.descriptor(), new MutableClock(NOW),
                    ValidatedParams.of(Map.of("prefixes", List.of("1.1.1.0/24"))));
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(ctx, sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                assertThat(server.received()).anyMatch(r -> r.text().contains("ris_subscribe")
                        && r.text().contains("1.1.1.0/24"));
                assertThat(server.received()).noneMatch(r -> r.text().contains("ris_subscribe")
                        && r.text().contains("\"prefix\":\"\""));

                server.sendText(message("[64500,13335]"));
                await(() -> sink.size() == 1);
                assertThat(sink).hasSize(1);
                assertThat(sink.get(0).kind()).isEqualTo("bgp-prefix-state");
                assertThat(sink.get(0).payload()).containsEntry("visibility", 1.0).containsEntry("status", "live");

                server.sendText(message("[64500,13335]"));
                Thread.sleep(200);
                assertThat(sink).as("an unchanged snapshot does not emit").hasSize(1);
            } finally {
                handle.close();
            }
        }
    }
}
