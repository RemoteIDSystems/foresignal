package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * The scheduler running for real (Product Charter FR-4, G1, NFR-6; invariant SRC-6): only an
 * enabled polling source is polled, an enable or disable applies within seconds, and PUSH and
 * DERIVED sources are never fetched.
 *
 * <p>This context turns scheduling on, unlike every other test context. It shares its
 * configuration with {@code NoClientTriggeredFetchIT}, hence the MockMvc annotation.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@TestPropertySource(properties = "foresignal.collector.scheduled=true")
class CollectorSchedulerIT {

    private static final String POLL = CollectorTestSources.POLL;
    private static final String DERIVED = "collect-derived-sched";
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");

    @Autowired private CollectorScheduler scheduler;
    @Autowired private StreamingSupervisor supervisor;
    @Autowired private FetchExecutor executor;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private FakePollingAdapter fakePollingAdapter;
    @Autowired private FakeStreamingAdapter fakeStreamingAdapter;
    @Autowired private SourceService sources;
    @Autowired private SourceRegistry registry;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        backoff.onSuccess(POLL);
        if (!scheduler.isRunning()) {
            scheduler.start();
        }
        // The cleaner truncated source_settings; let the scheduler notice before resetting counters.
        scheduler.reconcileNow();
        await().atMost(Duration.ofSeconds(10)).until(() ->
                scheduler.state().scheduledSources().isEmpty()
                        && supervisor.running().isEmpty()
                        && executor.activeFetches() == 0);
        // A run that was in flight when the cleaner truncated may have written its row since.
        jdbc.update("delete from fetch_log");
        collectPollAdapter.reset();
        fakePollingAdapter.reset();
        fakeStreamingAdapter.reset();
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(POLL);
        if (registry.contains(DERIVED)) {
            registry.unregisterDynamic(DERIVED);
        }
    }

    private int fetchRows(String status) {
        Integer count = jdbc.queryForObject(
                "select count(*) from fetch_log where source_id = ? and status = ?", Integer.class, POLL, status);
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("the scheduler starts with the application when scheduling is on")
    void startedOnReady() {
        assertThat(scheduler.isRunning()).isTrue();
        assertThat(scheduler.state().running()).isTrue();
    }

    @Test
    @DisplayName("seeding leaves every real free source off here, so only the fixtures are scheduled (prompt 146)")
    void seedingKeepsRealSourcesOffline() {
        // The test profile seeds every row disabled (foresignal.sources.default-enable=false): with scheduling on, a
        // free source seeded enabled would be fetched from its real upstream.
        sources.seedDefaults();
        assertThat(sources.view(UsgsEarthquakesAdapter.ID).enabled()).isFalse();
        assertThat(jdbc.queryForObject("select count(*) from source_settings where enabled", Integer.class))
                .isZero();

        sources.update(POLL, SourceSettingsUpdate.enabled(true));

        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() >= 1);
        assertThat(scheduler.state().scheduledSources()).containsExactly(POLL);
        assertThat(supervisor.running()).isEmpty();
    }

    @Test
    @DisplayName("enabling a source through SourceService.update fetches it within 3 s")
    void enableFetchesQuickly() {
        sources.update(POLL, SourceSettingsUpdate.enabled(true));

        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() >= 1);
        assertThat(scheduler.state().scheduledSources()).containsExactly(POLL);
        await().atMost(Duration.ofSeconds(5)).until(() -> fetchRows("OK") >= 1);
        assertThat(jdbc.queryForObject("select trigger from fetch_log where source_id = ? order by id limit 1",
                String.class, POLL)).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("an enabled source keeps being fetched at its interval")
    void keepsFetching() {
        sources.update(POLL, SourceSettingsUpdate.enabled(true));

        await().atMost(Duration.ofSeconds(6)).until(() -> collectPollAdapter.fetchCount() >= 3);
    }

    @Test
    @DisplayName("disabling stops further fetches and discards the in-flight result")
    void disableStopsAndDiscards() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        collectPollAdapter.then(ctx -> {
            release.await(10, TimeUnit.SECONDS);
            return FetchResult.complete(List.of(CollectorTestSources.record("d1", T0.minusSeconds(5))));
        });
        sources.update(POLL, SourceSettingsUpdate.enabled(true));
        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() == 1);

        sources.update(POLL, SourceSettingsUpdate.enabled(false));
        await().atMost(Duration.ofSeconds(3)).until(() -> scheduler.state().scheduledSources().isEmpty());
        release.countDown();

        await().atMost(Duration.ofSeconds(5)).until(() -> fetchRows("DISCARDED") == 1);
        await().during(Duration.ofMillis(2500)).atMost(Duration.ofSeconds(4))
                .until(() -> collectPollAdapter.fetchCount() == 1);
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class, POLL))
                .isZero();
    }

    @Test
    @DisplayName("an interval change reschedules the source")
    void intervalChangeReschedules() {
        sources.update(POLL, new SourceSettingsUpdate(true, Duration.ofSeconds(60), null, null, null, false, null, false));
        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() == 1);
        await().during(Duration.ofMillis(1500)).atMost(Duration.ofSeconds(3))
                .until(() -> collectPollAdapter.fetchCount() == 1);

        sources.update(POLL, SourceSettingsUpdate.interval(Duration.ofSeconds(1)));

        await().atMost(Duration.ofSeconds(4)).until(() -> collectPollAdapter.fetchCount() >= 2);
    }

    @Test
    @DisplayName("runs are skipped while the source is in backoff, and resume after it")
    void backoffSkips() {
        backoff.onThrottled(POLL, Optional.of(Duration.ofMinutes(5)));

        sources.update(POLL, SourceSettingsUpdate.enabled(true));

        await().during(Duration.ofMillis(2500)).atMost(Duration.ofSeconds(4))
                .until(() -> collectPollAdapter.fetchCount() == 0);
        assertThat(jdbc.queryForObject("select count(*) from fetch_log where source_id = ?", Integer.class, POLL))
                .isZero();

        backoff.onSuccess(POLL);
        await().atMost(Duration.ofSeconds(4)).until(() -> collectPollAdapter.fetchCount() >= 1);
    }

    @Test
    @DisplayName("PUSH and DERIVED sources are never fetched, and disabled polling sources are left alone")
    void pushAndDerivedNeverFetched() {
        registry.registerDynamic(SourceDescriptor.builder(DERIVED)
                .name("Derived scheduling test")
                .kind("derived-thing")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(60, 1000))
                .build());
        sources.update(FakeAdapters.PUSH_ID, SourceSettingsUpdate.enabled(true));
        sources.update(DERIVED, SourceSettingsUpdate.enabled(true));
        sources.update(POLL, SourceSettingsUpdate.enabled(true));

        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() >= 1);
        await().during(Duration.ofMillis(2500)).atMost(Duration.ofSeconds(4))
                .until(() -> scheduler.state().scheduledSources().equals(List.of(POLL)));
        assertThat(supervisor.running()).isEmpty();
        assertThat(fakePollingAdapter.fetchCount()).isZero();
        assertThat(jdbc.queryForObject(
                "select count(*) from fetch_log where source_id in (?, ?)", Integer.class, FakeAdapters.PUSH_ID, DERIVED))
                .isZero();
    }

    @Test
    @DisplayName("an enabled streaming source is started, and stopped when disabled")
    void streamingStartsAndStops() {
        sources.update(FakeAdapters.STREAMING_ID, SourceSettingsUpdate.enabled(true));

        await().atMost(Duration.ofSeconds(3)).until(() -> fakeStreamingAdapter.startCount() == 1);
        assertThat(supervisor.running()).containsExactly(FakeAdapters.STREAMING_ID);
        await().atMost(Duration.ofSeconds(3)).until(() -> scheduler.state().activeStreams() == 1);

        sources.update(FakeAdapters.STREAMING_ID, SourceSettingsUpdate.enabled(false));

        await().atMost(Duration.ofSeconds(5)).until(() -> supervisor.running().isEmpty());
        assertThat(fakeStreamingAdapter.allClosed()).isTrue();
    }

    @Test
    @DisplayName("after a restart the scheduler resumes the enabled sources")
    void restartResumes() {
        sources.update(POLL, SourceSettingsUpdate.enabled(true));
        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() >= 1);

        scheduler.stop();
        assertThat(scheduler.isRunning()).isFalse();
        assertThat(scheduler.state().scheduledSources()).isEmpty();
        int stopped = collectPollAdapter.fetchCount();
        await().during(Duration.ofMillis(2000)).atMost(Duration.ofSeconds(3))
                .until(() -> collectPollAdapter.fetchCount() == stopped);

        scheduler.start();

        await().atMost(Duration.ofSeconds(3)).until(() -> collectPollAdapter.fetchCount() > stopped);
        assertThat(scheduler.state().scheduledSources()).containsExactly(POLL);
    }
}
