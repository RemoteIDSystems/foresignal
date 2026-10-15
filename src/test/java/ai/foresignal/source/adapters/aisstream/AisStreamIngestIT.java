package ai.foresignal.source.adapters.aisstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.FetchLogWriter;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.collect.StreamingSupervisor;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.ingest.QaFlags;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * {@code aisstream} end to end (prompt 063): a fake stream on loopback, the real adapter from the registry, a
 * real {@link StreamingSupervisor} and {@code PolicyAwareRecordSink}, and the real writer, under the SAMPLED
 * TRACK policy of Core-Data-Model §5.3 (vessels).
 *
 * <p>The supervisor is built here with a 200 ms flush and a 100 ms ladder, as in {@code StreamingSupervisorIT},
 * so short timings do not leak into other contexts. Message times follow the {@link MutableClock}; each step
 * waits for its version before the next is sent, so the sink's per-flush coalescing never merges two steps.
 * Every test uses its own MMSIs, because the adapter's vessel state is a singleton of the context.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
@Timeout(value = 120, unit = TimeUnit.SECONDS)
class AisStreamIngestIT {

    private static final String ID = AisStreamAdapter.ID;
    private static final Instant T0 = Instant.parse("2026-10-13T12:10:00Z");
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final DateTimeFormatter TIME_UTC =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS '+0000 UTC'", Locale.ROOT).withZone(ZoneOffset.UTC);

    private static final LoopbackWebSocketServer SERVER = LoopbackWebSocketServer.start(false);

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(AisStreamAdapter.URL_PROPERTY, () -> SERVER.uri().toString());
    }

    @AfterAll
    static void stopServer() {
        SERVER.close();
    }

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

    private StreamingSupervisor supervisor;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        sources.update(ID, new SourceSettingsUpdate(false, null, null, Budget.of(600, 100_000), null, false, null, false));
        sources.setCredentials(ID, Map.of(AisStreamAdapter.API_KEY, "ingest-test-key"));
        CollectorProperties.Stream stream = new CollectorProperties.Stream(
                Duration.ofMillis(200), 2000, 50_000, Duration.ofSeconds(1), Duration.ofSeconds(1),
                List.of(Duration.ofMillis(100)), Duration.ofMinutes(10), Duration.ofHours(1));
        CollectorProperties properties = new CollectorProperties(
                false, Duration.ofSeconds(2), Duration.ofSeconds(10), Duration.ofSeconds(30), stream);
        supervisor = new StreamingSupervisor(
                registry, sources, contexts, health, queue, fetchLog, version, meters, properties, clock);
        int subscriptions = SERVER.received().size();
        supervisor.start(ID);
        SERVER.awaitReceived(subscriptions + 1, WAIT);
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll(Duration.ofSeconds(10));
    }

    // ---------- harness ----------

    private String time() {
        return TIME_UTC.format(clock.instant());
    }

    private void sendPosition(String mmsi, double lat, double lon, double sog) {
        SERVER.sendBinary(String.format(Locale.ROOT,
                "{\"MessageType\":\"PositionReport\",\"MetaData\":{\"MMSI\":%s,\"latitude\":%.6f,\"longitude\":%.6f,"
                        + "\"time_utc\":\"%s\"},\"Message\":{\"PositionReport\":{\"UserID\":%s,\"Latitude\":%.6f,"
                        + "\"Longitude\":%.6f,\"Sog\":%.1f,\"Cog\":0.0,\"TrueHeading\":0,\"NavigationalStatus\":0}}}",
                mmsi, lat, lon, time(), mmsi, lat, lon, sog));
    }

    private void sendStatic(String mmsi, String destination) {
        SERVER.sendBinary(String.format(Locale.ROOT,
                "{\"MessageType\":\"ShipStaticData\",\"MetaData\":{\"MMSI\":%s,\"time_utc\":\"%s\"},"
                        + "\"Message\":{\"ShipStaticData\":{\"UserID\":%s,\"Name\":\"TEST VESSEL@@@@\",\"CallSign\":\"TV1\","
                        + "\"ImoNumber\":9074729,\"Type\":70,\"Dimension\":{\"A\":100,\"B\":20,\"C\":10,\"D\":10},"
                        + "\"Eta\":{\"Month\":0,\"Day\":0,\"Hour\":24,\"Minute\":60},\"MaximumStaticDraught\":6.5,"
                        + "\"Destination\":\"%s\"}}}",
                mmsi, time(), mmsi, destination));
    }

    private int versions(String mmsi, PersistReason reason) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ? and persist_reason = ?",
                Integer.class, ID, mmsi, reason.code());
        return count == null ? 0 : count;
    }

    private int allVersions(String mmsi) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ?", Integer.class, ID, mmsi);
        return count == null ? 0 : count;
    }

    /** {@code meters} north of {@code lat}. */
    private static double north(double lat, double meters) {
        return lat + meters / 111_195.0;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("the first snapshot of a vessel is FIRST, with its MMSI, time and STREAM fetch_log row")
    void first() {
        sendPosition("235100001", 50.80, -1.30, 0.0);

        await().atMost(WAIT).until(() -> versions("235100001", PersistReason.FIRST) == 1);
        Instant observed = jdbc.queryForObject(
                "select observed_at from record_live where source_id = ? and external_id = ?",
                java.sql.Timestamp.class, ID, "235100001").toInstant();
        assertThat(observed).isEqualTo(T0);
        await().atMost(WAIT).until(() -> !jdbc.queryForList(
                "select id from fetch_log where source_id = ? and trigger = 'STREAM'", ID).isEmpty());
    }

    @Test
    @DisplayName("a 300 m move after 40 s is a SAMPLE")
    void sample() {
        sendPosition("235100002", 50.80, -1.30, 0.0);
        await().atMost(WAIT).until(() -> versions("235100002", PersistReason.FIRST) == 1);

        clock.advance(Duration.ofSeconds(40));
        sendPosition("235100002", north(50.80, 300), -1.30, 0.0);

        await().atMost(WAIT).until(() -> versions("235100002", PersistReason.SAMPLE) == 1);
        assertThat(allVersions("235100002")).isEqualTo(2);
    }

    @Test
    @DisplayName("a destination change is SIGNIFICANT")
    void significant() {
        sendStatic("235100003", "COWES");
        sendPosition("235100003", 50.80, -1.30, 0.0);
        await().atMost(WAIT).until(() -> versions("235100003", PersistReason.FIRST) == 1);
        String destination = jdbc.queryForObject(
                "select payload ->> 'destination' from record_head where source_id = ? and external_id = ?",
                String.class, ID, "235100003");
        assertThat(destination).isEqualTo("COWES");

        clock.advance(Duration.ofSeconds(5));
        sendStatic("235100003", "RYDE");

        await().atMost(WAIT).until(() -> versions("235100003", PersistReason.SIGNIFICANT) == 1);
        assertThat(jdbc.queryForObject(
                "select payload ->> 'destination' from record_head where source_id = ? and external_id = ?",
                String.class, ID, "235100003")).isEqualTo("RYDE");
    }

    @Test
    @DisplayName("a sentinel position keeps the last geometry with POSITION_STALE")
    void sentinelCarriedForward() {
        sendPosition("235100004", 50.80, -1.30, 0.0);
        await().atMost(WAIT).until(() -> versions("235100004", PersistReason.FIRST) == 1);

        // The sentinel does not change the payload, so the live row updates on the touch throttle
        // (touchMin = clamp(ttl/10, 30 s, 1 h) = 3 min for a 30 min TTL), not immediately.
        clock.advance(Duration.ofSeconds(200));
        sendPosition("235100004", 91.0, 181.0, 0.0);

        await().atMost(WAIT).until(() -> {
            Integer flags = jdbc.queryForObject(
                    "select qa_flags from record_live where source_id = ? and external_id = ?",
                    Integer.class, ID, "235100004");
            return flags != null && (flags & QaFlags.POSITION_STALE) != 0;
        });
        Map<String, Object> live = jdbc.queryForMap(
                "select ST_Y(geom) as lat, observed_at from record_live where source_id = ? and external_id = ?",
                ID, "235100004");
        assertThat(((Number) live.get("lat")).doubleValue()).isEqualTo(50.80, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(((java.sql.Timestamp) live.get("observed_at")).toInstant()).isEqualTo(T0.plusSeconds(200));
    }

    @Test
    @DisplayName("silence past the 30-minute TTL expires the vessel; its return is REAPPEAR")
    void reappear() {
        sendPosition("235100005", 50.80, -1.30, 0.0);
        await().atMost(WAIT).until(() -> versions("235100005", PersistReason.FIRST) == 1);
        int subscriptions = SERVER.received().size();

        // Past the TTL, and past the 300 s silence: the watchdog recycles the connection and the supervisor
        // reconnects on its (test) ladder before the vessel comes back.
        clock.advance(Duration.ofMinutes(31));
        SERVER.awaitReceived(subscriptions + 1, WAIT);
        sendPosition("235100005", 50.80, -1.30, 0.0);

        await().atMost(WAIT).until(() -> versions("235100005", PersistReason.REAPPEAR) == 1);
    }
}
