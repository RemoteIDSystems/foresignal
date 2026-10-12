package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Invariant SSE-1 under load (prompt 022 added this class, which the registry names): clients that
 * resume while batches keep committing cross the replay→live switch with no duplicate and no missing
 * {@code change}. The hub registers a client before its replay query and dedupes by seq
 * (Core-Data-Model §10.2); this test races that switch on purpose.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class SseReplayLiveBoundaryIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final int BATCHES = 300;
    static final int CLIENTS = 8;

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SseHub hub;
    @Autowired ChangeBus changeBus;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;

    private final List<SseTestClient> streams = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        settings.put(SettingKeys.STREAM_LIMITS, new StreamLimits(0, 0, 0, 0, 0, 0, 0, CLIENTS + 2));
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        settings.invalidateCache();
    }

    @Test
    @DisplayName("clients resuming mid-burst receive every change after their cursor exactly once, in seq order")
    void resumeDuringABurst() throws Exception {
        AtomicBoolean producing = new AtomicBoolean(true);
        AtomicReference<Throwable> producerError = new AtomicReference<>();
        Thread producer = Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < BATCHES; i++) {
                    records.ingest(STANDARD, NormalizedRecord.builder("b" + i, STANDARD_KIND).observedAt(T)
                            .point(i % 180, 0).put("i", i).build());
                }
            } catch (Throwable t) {
                producerError.set(t);
            } finally {
                producing.set(false);
            }
        });

        // Connect while batches commit; each resumes a few changes back, so its replay overlaps the
        // live events published while it runs.
        List<Long> cursors = new ArrayList<>();
        while (changeBus.lastPublishedSeq() < 20) {
            Thread.sleep(5);
        }
        for (int c = 0; c < CLIENTS; c++) {
            long after = Math.max(0, changeBus.lastPublishedSeq() - 5 - c);
            cursors.add(after);
            SseTestClient client = SseTestClient.connect("http://localhost:" + port,
                    "/v1/changes/stream?source=" + STANDARD + "&live=false&after=" + after, Map.of());
            assertThat(client.status()).isEqualTo(200);
            streams.add(client);
            Thread.sleep(15);
        }
        producer.join(60_000);
        assertThat(producerError.get()).isNull();
        assertThat(producing.get()).isFalse();
        long last = changeBus.lastPublishedSeq();

        for (int c = 0; c < CLIENTS; c++) {
            long after = cursors.get(c);
            List<Long> expected = jdbc.queryForList(
                    "SELECT seq FROM record_version WHERE source_id = ? AND seq > ? AND seq <= ? ORDER BY seq",
                    Long.class, STANDARD, after, last);
            List<Long> received = new ArrayList<>();
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (received.size() < expected.size() && System.nanoTime() < deadline) {
                Event event = streams.get(c).next(Duration.ofMillis(500));
                if (event == null) {
                    continue;
                }
                assertThat(event.event()).as("client %d sees no reset or resync", c).isNotIn("reset", "resync");
                if (event.event().equals("change")) {
                    received.add(Long.parseLong(event.id()));
                }
            }
            received.addAll(streams.get(c).collect("change", Duration.ofMillis(300)).stream()
                    .map(e -> Long.parseLong(e.id())).toList());
            assertThat(received).as("client %d after %d", c, after).containsExactlyElementsOf(expected);
        }
    }
}
