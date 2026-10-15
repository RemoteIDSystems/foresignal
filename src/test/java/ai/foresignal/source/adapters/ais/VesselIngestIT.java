package ai.foresignal.source.adapters.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.codec.ais.AisMessage;
import ai.foresignal.codec.ais.NmeaWriter;
import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.FetchLogWriter;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.collect.StreamingSupervisor;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Vessel ingest end to end (prompt 085): {@code local-ais-1} reading AIVDM lines (written with the shared encoder) from
 * a loopback server standing in for AIS-catcher, through the real {@link StreamingSupervisor}, policy sink and writer,
 * under the SAMPLED TRACK policy of Core-Data-Model §5.3: FIRST, a KEYFRAME after 1,200 s, a SAMPLE after a 300 m move,
 * SIGNIFICANT on a destination change, and every version in the TRACK tier.
 *
 * <p>The supervisor has a 200 ms flush and a 100 ms ladder (as in {@code AisStreamIngestIT}); the source's time base is
 * RECEIVED, so the {@link MutableClock} sets the observation times. Each step waits for its version before the next.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
@Timeout(value = 120, unit = TimeUnit.SECONDS)
class VesselIngestIT {

    private static final String ID = "local-ais-1";
    private static final Instant T0 = Instant.parse("2026-10-13T12:10:00Z");
    private static final Duration WAIT = Duration.ofSeconds(20);

    private static ServerSocket server;
    private static final BlockingQueue<Socket> ACCEPTED = new LinkedBlockingQueue<>();

    @Autowired private SourceRegistry registry;
    @Autowired private SourceService sources;
    @Autowired private FetchContextFactory contexts;
    @Autowired private HealthTracker health;
    @Autowired private IngestQueue queue;
    @Autowired private FetchLogWriter fetchLog;
    @Autowired private ForesignalVersion version;
    @Autowired private MeterRegistry meters;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    private final NmeaWriter writer = new NmeaWriter();
    private StreamingSupervisor supervisor;
    private Socket peer;

    @BeforeAll
    static void startServer() throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    ACCEPTED.add(server.accept());
                } catch (IOException e) {
                    return;
                }
            }
        });
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.close();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        clock.setInstant(T0);
        sources.update(ID, new SourceSettingsUpdate(false, null,
                Map.of(LocalAisAdapter.ENDPOINT, "tcp://127.0.0.1:" + server.getLocalPort()), Budget.of(600, 100_000),
                null, false, null, false));
        CollectorProperties.Stream stream = new CollectorProperties.Stream(
                Duration.ofMillis(200), 2000, 50_000, Duration.ofSeconds(1), Duration.ofSeconds(1),
                List.of(Duration.ofMillis(100)), Duration.ofMinutes(10), Duration.ofHours(1));
        CollectorProperties properties = new CollectorProperties(
                false, Duration.ofSeconds(2), Duration.ofSeconds(10), Duration.ofSeconds(30), stream);
        supervisor = new StreamingSupervisor(
                registry, sources, contexts, health, queue, fetchLog, version, meters, properties, clock);
        supervisor.start(ID);
        peer = ACCEPTED.poll(10, TimeUnit.SECONDS);
        assertThat(peer).as("local-ais-1 connected to the loopback AIS-catcher").isNotNull();
    }

    @AfterEach
    void tearDown() throws IOException {
        supervisor.stopAll(Duration.ofSeconds(10));
        if (peer != null) {
            peer.close();
        }
    }

    // ---------- harness ----------

    private void send(AisMessage message) {
        try {
            OutputStream out = peer.getOutputStream();
            for (String line : writer.write(message, "A")) {
                out.write((line + "\r\n").getBytes(StandardCharsets.US_ASCII));
            }
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AisMessage.ClassAPosition position(int mmsi, double lat, double lon) {
        return new AisMessage.ClassAPosition(1, 0, mmsi, 0, 0, 10.0, true, lon, lat, 90.0, 90, 0, 0, false, 0);
    }

    private static AisMessage.StaticVoyage voyage(int mmsi, String destination) {
        return new AisMessage.StaticVoyage(0, mmsi, 0, 9_134_270, "LAXY7", "NORDIC STAR", 70,
                new AisMessage.Dimension(100, 20, 8, 8), 1, null, 7.5, destination, false);
    }

    private int versions(String mmsi, PersistReason reason) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ? and persist_reason = ?",
                Integer.class, ID, mmsi, reason.code());
        return count == null ? 0 : count;
    }

    /** {@code meters} north of {@code lat}, on the 1/10,000-minute grid. */
    private static double north(double lat, double meters) {
        return Math.round((lat + meters / 111_195.0) * 600_000.0) / 600_000.0;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("FIRST, then the same position after the 1,200 s keyframe is a KEYFRAME, all in the TRACK tier")
    void keyframe() {
        send(position(257_200_001, 59.9, 10.5));
        await().atMost(WAIT).until(() -> versions("257200001", PersistReason.FIRST) == 1);

        clock.advance(Duration.ofSeconds(600));
        send(position(257_200_001, 59.9, 10.5));
        clock.advance(Duration.ofSeconds(601));
        send(position(257_200_001, 59.9, 10.5));

        // An unchanged entity past its keyframe persists a HEARTBEAT, not a KEYFRAME (Core-Data-Model
        // §5.2 row 8); the version is still written.
        await().atMost(WAIT).until(() -> versions("257200001", PersistReason.KEYFRAME)
                + versions("257200001", PersistReason.HEARTBEAT) == 1);
        List<Short> tiers = jdbc.queryForList(
                "select history_tier from record_version where source_id = ? and external_id = ?", Short.class, ID,
                "257200001");
        assertThat(tiers).hasSize(2).containsOnly(HistoryTier.TRACK.code());
    }

    @Test
    @DisplayName("a 300 m move after 40 s is a SAMPLE")
    void sample() {
        send(position(257_200_002, 59.9, 10.5));
        await().atMost(WAIT).until(() -> versions("257200002", PersistReason.FIRST) == 1);

        clock.advance(Duration.ofSeconds(40));
        send(position(257_200_002, north(59.9, 300), 10.5));

        await().atMost(WAIT).until(() -> versions("257200002", PersistReason.SAMPLE) == 1);
    }

    @Test
    @DisplayName("a destination change is SIGNIFICANT; the head carries the merged static data")
    void significant() {
        send(voyage(257_200_003, "BERGEN"));
        send(position(257_200_003, 59.9, 10.5));
        await().atMost(WAIT).until(() -> versions("257200003", PersistReason.FIRST) == 1);
        assertThat(jdbc.queryForObject(
                "select payload ->> 'destination' from record_head where source_id = ? and external_id = ?",
                String.class, ID, "257200003")).isEqualTo("BERGEN");
        assertThat(jdbc.queryForObject(
                "select payload ->> 'name' from record_head where source_id = ? and external_id = ?",
                String.class, ID, "257200003")).isEqualTo("NORDIC STAR");

        clock.advance(Duration.ofSeconds(5));
        send(voyage(257_200_003, "OSLO"));

        await().atMost(WAIT).until(() -> versions("257200003", PersistReason.SIGNIFICANT) == 1);
        assertThat(jdbc.queryForObject(
                "select payload ->> 'destination' from record_head where source_id = ? and external_id = ?",
                String.class, ID, "257200003")).isEqualTo("OSLO");
    }
}
