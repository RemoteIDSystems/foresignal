package ai.foresignal.source.adapters.seismic;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code emsc-seismic} (prompt 089): a create and an update produce records, a re-delivered version is dropped,
 * and the gap-fill merges missed events without duplicating a live one: on a reconnect it reads from the cursor, pages
 * by offset, treats 204 as empty, and while it has not completed the cursor stays put.
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

    /** One GeoJSON event feature as the FDSN gap-fill returns it. */
    private static String feature(String unid, String lastUpdate) {
        String message = message("create", unid, lastUpdate);
        return message.substring(message.indexOf("\"data\":") + 7, message.length() - 1);
    }

    private static UpstreamResponse collection(List<String> features) {
        String body = "{\"type\":\"FeatureCollection\",\"features\":[" + String.join(",", features) + "]}";
        return new UpstreamResponse(200, Map.of(), body.getBytes(StandardCharsets.UTF_8), "application/json", false,
                false);
    }

    private static String param(URI uri, String name) {
        for (String pair : uri.getRawQuery().split("&")) {
            if (pair.startsWith(name + "=")) {
                return pair.substring(name.length() + 1);
            }
        }
        return null;
    }

    @Test
    @DisplayName("a reconnect gap-fills from the cursor and merges without duplicating what the stream delivered")
    void reconnectGapFillWithoutDuplicates() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            EmscSeismicAdapter adapter = new EmscSeismicAdapter(server.uri().toString(), "http://127.0.0.1:1/fdsn");
            AtomicReference<List<String>> served = new AtomicReference<>(List.of(
                    feature("E-1", "2026-10-14T10:30:00.0Z"), feature("E-2", "2026-10-14T10:40:00.0Z")));
            SeismicTestContext ctx = new SeismicTestContext(adapter.descriptor(), new MutableClock(NOW),
                    uri -> collection(served.get()));
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();

            StreamHandle first = adapter.start(ctx, sink::add);
            server.awaitConnection(Duration.ofSeconds(5));
            assertThat(sink).extracting(NormalizedRecord::externalId).containsExactly("E-1", "E-2");
            server.sendText(message("create", "E-3", "2026-10-14T11:00:00.0Z"));
            await(() -> sink.size() == 3);
            first.close();
            assertThat(ctx.stateValues()).containsEntry(EmscSeismicAdapter.CURSOR_KEY, NOW.toString())
                    .doesNotContainKey(EmscSeismicAdapter.GAP_KEY);

            // The reconnect's gap-fill returns what was already delivered plus one missed event.
            served.set(List.of(feature("E-2", "2026-10-14T10:40:00.0Z"), feature("E-3", "2026-10-14T11:00:00.0Z"),
                    feature("E-4", "2026-10-14T11:50:00.0Z")));
            StreamHandle second = adapter.start(ctx, sink::add);
            try {
                assertThat(sink).extracting(NormalizedRecord::externalId).containsExactly("E-1", "E-2", "E-3", "E-4");
                URI request = ctx.requests().getLast();
                assertThat(param(request, "updatedafter")).isEqualTo("2026-10-14T11:55:00Z");
                assertThat(param(request, "orderby")).isEqualTo("time-asc");
                assertThat(param(request, "offset")).isEqualTo("1");
            } finally {
                second.close();
            }
        }
    }

    @Test
    @DisplayName("a failed gap-fill keeps the cursor where it was, even while the stream delivers newer events")
    void failedGapFillHoldsCursor() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            EmscSeismicAdapter adapter = new EmscSeismicAdapter(server.uri().toString(), "http://127.0.0.1:1/fdsn");
            AtomicReference<Boolean> failing = new AtomicReference<>(true);
            SeismicTestContext ctx = new SeismicTestContext(adapter.descriptor(), new MutableClock(NOW), uri -> {
                if (failing.get()) {
                    throw new IllegalStateException("FDSN unavailable");
                }
                return collection(List.of(feature("M-1", "2026-10-14T10:00:00.0Z")));
            });
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle first = adapter.start(ctx, sink::add);
            server.awaitConnection(Duration.ofSeconds(5));
            server.sendText(message("create", "S-1", "2026-10-14T13:00:00.0Z"));
            await(() -> sink.size() == 1);
            first.close();
            assertThat(sink).extracting(NormalizedRecord::externalId).containsExactly("S-1");
            assertThat(ctx.stateValues()).as("the gap is not filled, so the cursor does not move")
                    .doesNotContainKey(EmscSeismicAdapter.CURSOR_KEY)
                    .containsEntry(EmscSeismicAdapter.GAP_KEY, "2026-10-14T09:55:00Z|1|2026-10-14T12:00:00Z");

            failing.set(false);
            StreamHandle second = adapter.start(ctx, sink::add);
            try {
                assertThat(param(ctx.requests().getLast(), "updatedafter")).as("the unfilled gap is read again")
                        .isEqualTo("2026-10-14T09:55:00Z");
                assertThat(sink).extracting(NormalizedRecord::externalId).containsExactly("S-1", "M-1");
                assertThat(ctx.stateValues()).containsEntry(EmscSeismicAdapter.CURSOR_KEY, NOW.toString())
                        .doesNotContainKey(EmscSeismicAdapter.GAP_KEY);
            } finally {
                second.close();
            }
        }
    }

    @Test
    @DisplayName("the gap-fill pages by offset until a short page")
    void gapFillPages() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            EmscSeismicAdapter adapter = new EmscSeismicAdapter(server.uri().toString(), "http://127.0.0.1:1/fdsn");
            SeismicTestContext ctx = new SeismicTestContext(adapter.descriptor(), new MutableClock(NOW), uri -> {
                int offset = Integer.parseInt(param(uri, "offset"));
                int count = offset == 1 ? EmscSeismicAdapter.GAP_LIMIT : 2;
                List<String> features = new java.util.ArrayList<>();
                for (int i = 0; i < count; i++) {
                    features.add(feature("P-" + (offset + i), "2026-10-14T11:00:00.0Z"));
                }
                return collection(features);
            });
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(ctx, sink::add);
            try {
                assertThat(sink).hasSize(EmscSeismicAdapter.GAP_LIMIT + 2);
                assertThat(ctx.requests()).extracting(u -> param(u, "offset")).containsExactly("1", "501");
            } finally {
                handle.close();
            }
        }
    }

    @Test
    @DisplayName("HTTP 204 from the gap-fill is an empty, complete page")
    void gapFillNoContent() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            EmscSeismicAdapter adapter = new EmscSeismicAdapter(server.uri().toString(), "http://127.0.0.1:1/fdsn");
            SeismicTestContext ctx = new SeismicTestContext(adapter.descriptor(), new MutableClock(NOW),
                    uri -> new UpstreamResponse(204, Map.of(), new byte[0], null, false, false));
            StreamHandle handle = adapter.start(ctx, record -> { });
            try {
                assertThat(ctx.stateValues()).containsEntry(EmscSeismicAdapter.CURSOR_KEY, NOW.toString());
            } finally {
                handle.close();
            }
        }
    }
}
