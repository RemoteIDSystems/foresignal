package ai.foresignal.source.adapters.usgs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.CollectorScheduler;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.HealthService;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/**
 * {@code usgs-earthquakes} end to end on the v1.2 pipeline: real fetches through the scheduler, the
 * fetch executor and the real ingest writer, against WireMock serving the recorded fixtures
 * (Core-Data-Model §17: records are never seeded with raw SQL).
 *
 * <p>Each test enables the source, lets the scheduler make the first fetch, then disables it so no
 * further scheduled run interferes, and drives later fetches with
 * {@link FetchExecutor#execute(String, FetchTrigger) execute(id, MANUAL)}, which runs while the
 * source is disabled. The budget is raised for the test, because the descriptor's 4 per minute
 * would refuse the later fetches.
 *
 * <p>This context turns scheduling on and points the adapter at WireMock through
 * {@value UsgsEarthquakesAdapter#BASE_URL_PROPERTY}, so it is its own cached context.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
@TestPropertySource(properties = "foresignal.collector.scheduled=true")
class UsgsIngestIT extends WireMockSupport {

    private static final String ID = UsgsEarthquakesAdapter.ID;
    private static final String FEED_PATH = "/earthquakes/feed/v1.0/summary/all_day.geojson";
    private static final String LAST_MODIFIED = "Sun, 11 Oct 2026 22:32:45 GMT";

    /** Just after the capture, so every fixture's {@code updated} is in the past. */
    private static final Instant NOW = Instant.parse("2026-10-11T22:33:00Z");

    private static final int FEATURES = 25;

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(UsgsEarthquakesAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired private CollectorScheduler scheduler;
    @Autowired private FetchExecutor executor;
    @Autowired private SourceService sources;
    @Autowired private HealthService health;
    @Autowired private BackoffRegistry backoff;
    @Autowired private ChangeBus changeBus;
    @Autowired private MeterRegistry meters;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        backoff.onSuccess(ID);
        if (!scheduler.isRunning()) {
            scheduler.start();
        }
        // The cleaner truncated source_settings; let the scheduler notice before the test starts.
        scheduler.reconcileNow();
        await().atMost(Duration.ofSeconds(10))
                .until(() -> scheduler.state().scheduledSources().isEmpty() && executor.activeFetches() == 0);
        jdbc.update("delete from fetch_log");
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(ID);
    }

    // ---------- harness ----------

    /** Serves {@code fixture} for every request, with the capture's {@code Last-Modified}. */
    private static void serve(String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(FEED_PATH))
                .atPriority(5)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withHeader("Last-Modified", LAST_MODIFIED)
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    /**
     * Enables the source with a raised budget, waits for the scheduler's first fetch of
     * {@code all_day.geojson} to be written, then disables it.
     */
    private void firstScheduledFetch(boolean thenDisable) {
        serve("all_day.geojson");
        sources.update(ID, new SourceSettingsUpdate(
                true, null, Map.of(UsgsEarthquakesAdapter.FEED_PARAM, "all_day"), Budget.of(600, 100_000),
                null, false, null, false));
        await().atMost(Duration.ofSeconds(15)).until(() -> fetchRows(null) >= 1);
        if (thenDisable) {
            disable();
        }
    }

    private void disable() {
        sources.update(ID, SourceSettingsUpdate.enabled(false));
        await().atMost(Duration.ofSeconds(10))
                .until(() -> scheduler.state().scheduledSources().isEmpty() && executor.activeFetches() == 0);
    }

    private FetchOutcome fetchNow() {
        return executor.execute(ID, FetchTrigger.MANUAL);
    }

    private int fetchRows(FetchStatus status) {
        Integer count = status == null
                ? jdbc.queryForObject("select count(*) from fetch_log where source_id = ?", Integer.class, ID)
                : jdbc.queryForObject("select count(*) from fetch_log where source_id = ? and status = ?",
                        Integer.class, ID, status.name());
        return count == null ? 0 : count;
    }

    private int versions(PersistReason reason) {
        Integer count = reason == null
                ? jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class, ID)
                : jdbc.queryForObject("select count(*) from record_version where source_id = ? and persist_reason = ?",
                        Integer.class, ID, reason.code());
        return count == null ? 0 : count;
    }

    private Instant liveInstant(String column, String externalId) {
        Timestamp value = jdbc.queryForObject(
                "select " + column + " from record_live where source_id = ? and external_id = ?",
                Timestamp.class, ID, externalId);
        return value == null ? null : value.toInstant();
    }

    private Instant lastDataAt() {
        Timestamp value = jdbc.queryForObject(
                "select last_data_at from source_health where source_id = ?", Timestamp.class, ID);
        return value == null ? null : value.toInstant();
    }

    private double throttledTouches() {
        return meters.counter("foresignal.ingest.records.touches_throttled", "source", ID).count();
    }

    // ---------- tests ----------

    @Test
    @DisplayName("enabling the source: the first fetch writes every quake as FIRST, one OK row, and health is healthy")
    void firstFetch() {
        firstScheduledFetch(false);

        assertThat(versions(PersistReason.FIRST)).isEqualTo(FEATURES);
        assertThat(versions(null)).isEqualTo(FEATURES);
        assertThat(fetchRows(null)).isEqualTo(1);
        Map<String, Object> row = jdbc.queryForMap(
                "select status, trigger, versions_written, records_seen, licence, http_status from fetch_log"
                        + " where source_id = ?", ID);
        assertThat(row.get("status")).isEqualTo("OK");
        assertThat(row.get("trigger")).isEqualTo("SCHEDULED");
        assertThat(row.get("versions_written")).isEqualTo(FEATURES);
        assertThat(row.get("records_seen")).isEqualTo(FEATURES);
        assertThat(row.get("licence")).isEqualTo("US-PD");
        assertThat(row.get("http_status")).isEqualTo(200);

        assertThat(health.health(ID).status()).isEqualTo(HealthStatus.HEALTHY);

        Timestamp eventTime = jdbc.queryForObject(
                "select event_time from record_head where source_id = ? and external_id = ?",
                Timestamp.class, ID, "aka2026uekvdg");
        assertThat(eventTime.toInstant()).isEqualTo(Instant.ofEpochMilli(1791757094767L));
        assertThat(liveInstant("observed_at", "aka2026uekvdg")).isEqualTo(Instant.ofEpochMilli(1791757136341L));
        String title = jdbc.queryForObject(
                "select title from record_head where source_id = ? and external_id = ?",
                String.class, ID, "aka2026uekvdg");
        assertThat(title).isEqualTo("M 0.7 - 73 km WNW of Beluga, Alaska");

        disable();
    }

    @Test
    @DisplayName("a revised quake is exactly one version 2 with SIGNIFICANT, prev_seq set and event_time unchanged")
    void revision() {
        firstScheduledFetch(true);
        Map<String, Object> first = jdbc.queryForMap(
                "select seq, event_time from record_version where source_id = ? and external_id = ? and version = 1",
                ID, "aka2026uekklg");

        serve("revised.geojson");
        FetchOutcome outcome = fetchNow();

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.persisted()).isEqualTo(1);
        assertThat(outcome.byReason().get(PersistReason.SIGNIFICANT)).isEqualTo(1);
        assertThat(versions(null)).isEqualTo(FEATURES + 1);
        assertThat(versions(PersistReason.SIGNIFICANT)).isEqualTo(1);

        Map<String, Object> second = jdbc.queryForMap(
                "select version, prev_seq, event_time, persist_reason, payload::text as payload from record_version"
                        + " where source_id = ? and external_id = ? and version = 2",
                ID, "aka2026uekklg");
        assertThat(((Number) second.get("version")).intValue()).isEqualTo(2);
        assertThat(((Number) second.get("persist_reason")).shortValue()).isEqualTo(PersistReason.SIGNIFICANT.code());
        assertThat(((Number) second.get("prev_seq")).longValue()).isEqualTo(((Number) first.get("seq")).longValue());
        assertThat(second.get("event_time")).isEqualTo(first.get("event_time"));
        assertThat((String) second.get("payload")).contains("\"reviewed\"").contains("2.5");
    }

    @Test
    @DisplayName("identical content is a throttled touch with no version and no change event; past 1 h it is a TOUCH")
    void unchangedRepoll() {
        firstScheduledFetch(true);
        AtomicInteger changeBatches = new AtomicInteger();
        Subscription subscription = changeBus.subscribe("usgs-ingest-it", new ChangeListener() {
            @Override
            public void onBatch(ChangeBatch batch) {
                changeBatches.incrementAndGet();
            }

            @Override
            public void onGap(long lastDeliveredSeq) {
                changeBatches.incrementAndGet();
            }
        }, 16);
        try {
            long watermark = changeBus.lastPublishedSeq();
            double throttledBefore = throttledTouches();
            Instant receivedBefore = liveInstant("last_received_at", "aka2026uekvdg");

            FetchOutcome throttled = fetchNow();

            assertThat(throttled.success()).isTrue();
            assertThat(throttled.persisted()).isZero();
            assertThat(throttled.touchesThrottled()).isEqualTo(FEATURES);
            assertThat(throttled.touched()).isZero();
            assertThat(throttledTouches()).isEqualTo(throttledBefore + FEATURES);
            assertThat(versions(null)).isEqualTo(FEATURES);
            assertThat(changeBus.lastPublishedSeq()).isEqualTo(watermark);
            assertThat(liveInstant("last_received_at", "aka2026uekvdg")).isEqualTo(receivedBefore);

            clock.advance(Duration.ofMinutes(61));
            FetchOutcome touched = fetchNow();

            assertThat(touched.persisted()).isZero();
            assertThat(touched.touched()).isEqualTo(FEATURES);
            assertThat(versions(null)).isEqualTo(FEATURES);
            assertThat(changeBus.lastPublishedSeq()).isEqualTo(watermark);
            assertThat(liveInstant("last_received_at", "aka2026uekvdg"))
                    .isEqualTo(clock.instant().truncatedTo(ChronoUnit.MICROS))
                    .isAfter(receivedBefore);
            assertThat(changeBatches.get()).isZero();
        } finally {
            subscription.close();
        }
    }

    @Test
    @DisplayName("a re-fetch where only updated moved writes no version and advances record_live.observed_at")
    void updatedOnly() {
        firstScheduledFetch(true);
        Instant before = liveInstant("observed_at", "us6000u1i6");
        assertThat(before).isEqualTo(Instant.ofEpochMilli(1791693785627L));

        serve("updated-only.geojson");
        FetchOutcome outcome = fetchNow();

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.persisted()).isZero();
        assertThat(versions(null)).isEqualTo(FEATURES);
        // Two hours later is past touchMin (1 h without a TTL), so the observation is a TOUCH.
        assertThat(outcome.touched()).isEqualTo(1);
        assertThat(liveInstant("observed_at", "us6000u1i6")).isEqualTo(Instant.ofEpochMilli(1791700985627L));
        assertThat(liveInstant("observed_at", "aka2026uekvdg")).isEqualTo(Instant.ofEpochMilli(1791757136341L));
    }

    @Test
    @DisplayName("a 304 writes a NOT_MODIFIED row and leaves last_data_at unchanged")
    void notModified() {
        firstScheduledFetch(true);
        Instant dataAt = lastDataAt();
        assertThat(dataAt).isNotNull();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(FEED_PATH))
                .atPriority(1)
                .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                .willReturn(aResponse().withStatus(304).withHeader("Last-Modified", LAST_MODIFIED)));
        clock.advance(Duration.ofMinutes(5));

        FetchOutcome outcome = fetchNow();

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(outcome.recordsSeen()).isZero();
        assertThat(fetchRows(FetchStatus.NOT_MODIFIED)).isEqualTo(1);
        assertThat(lastDataAt()).isEqualTo(dataAt);
        assertThat(versions(null)).isEqualTo(FEATURES);
        WIREMOCK.verify(1, WireMock.getRequestedFor(urlEqualTo(FEED_PATH))
                .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED)));
    }
}
