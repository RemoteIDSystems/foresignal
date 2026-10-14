package ai.foresignal.source.adapters.seismic;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code emsc-seismic} (prompt 089): a create and an update produce records, a re-delivered version is dropped,
 * and the gap-fill merges missed events without duplicating a live one.
 */
class EmscStreamTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    private static String message(String action, String unid, String lastUpdate) {
        return "{\"action\":\"" + action + "\",\"data\":{\"type\":\"Feature\",\"id\":\"" + unid
                + "\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[2.0,1.0,10.0]},\"properties\":{"
                + "\"unid\":\"" + unid + "\",\"time\":\"2026-10-14T10:00:00.0Z\",\"lastupdate\":\"" + lastUpdate
                + "\",\"mag\":5.1,\"magtype\":\"Mww\",\"depth\":10.0,\"lat\":1.0,\"lon\":2.0,"
                + "\"auth\":\"TEST\",\"source_catalog\":\"TEST\",\"flynn_region\":\"Test Region\","
                + "\"evtype\":\"earthquake\"}}}";
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
    @DisplayName("create and update emit, a duplicate version is dropped, and the gap-fill merges once")
    void stream() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            EmscSeismicAdapter adapter = new EmscSeismicAdapter(server.uri().toString(), "http://127.0.0.1:1/fdsn");
            SeismicTestContext ctx = new SeismicTestContext(adapter.descriptor(), new MutableClock(NOW),
                    AdapterFixtures.bytes("emsc-seismic", "gapfill.json"));
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(ctx, sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                int afterGapFill = sink.size();
                assertThat(afterGapFill).as("the gap-fill merged the captured events").isGreaterThan(0);

                server.sendText(message("create", "U-test-1", "2026-10-14T11:00:00.0Z"));
                await(() -> sink.size() == afterGapFill + 1);
                assertThat(sink).hasSize(afterGapFill + 1);

                server.sendText(message("create", "U-test-1", "2026-10-14T11:00:00.0Z"));
                Thread.sleep(200);
                assertThat(sink).as("the same version is not duplicated").hasSize(afterGapFill + 1);

                server.sendText(message("update", "U-test-1", "2026-10-14T11:30:00.0Z"));
                await(() -> sink.size() == afterGapFill + 2);
                assertThat(sink).as("an update is a new version").hasSize(afterGapFill + 2);
                assertThat(sink.get(sink.size() - 1).externalId()).isEqualTo("U-test-1");
                assertThat(sink.get(sink.size() - 1).payload()).containsEntry("mag", 5.1);
            } finally {
                handle.close();
            }
            assertThat(ctx.opens()).isEqualTo(1);
        }
    }
}
