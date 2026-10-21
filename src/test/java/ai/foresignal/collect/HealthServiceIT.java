package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.store.SourceHealthEntity;
import ai.foresignal.store.SourceHealthRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

/**
 * The health service against the real context: health rows, {@code source_usage}, the budget
 * governor and source settings combined into one view.
 *
 * <p>The application clock is the real one here, so the assertions avoid depending on exactly when
 * a test runs: usage rows are written relative to "now", and timestamps that must not move are
 * pinned by writing them directly.
 *
 * <p>Several collaborators keep in-memory state that outlives the table truncation between tests —
 * the meter's unflushed counters, the governor's buckets, the backoff cache and the tracker's stream
 * state — so {@link #reset()} clears each of them for the fake sources.
 */
@IntegrationTest
@Import(FakeAdapters.class)
class HealthServiceIT {

    private static final String POLLING = FakeAdapters.POLLING_ID;
    private static final String STREAMING = FakeAdapters.STREAMING_ID;
    private static final String PUSH = FakeAdapters.PUSH_ID;

    @Autowired private HealthService health;
    @Autowired private HealthTracker tracker;
    @Autowired private SourceService sources;
    @Autowired private SourceRegistry registry;
    @Autowired private SourceHealthRepository healthRows;
    @Autowired private UsageMeter usage;
    @Autowired private BudgetGovernor budgets;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MeterRegistry meters;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;
    @Autowired private ApplicationContext context;

    @BeforeEach
    void reset() {
        // Drain counters left by other tests, then discard what was drained.
        usage.flush();
        jdbc.update("delete from source_usage");
        for (String id : registry.ids()) {
            budgets.invalidate(id);
            backoff.onSuccess(id);
        }
        jdbc.update("delete from source_health");
        tracker.clearStreams();
        sources.seedDefaults();
        // Free sources are seeded enabled (prompt 145); these tests start from every source off and enable what
        // they need, so DISABLED and IDLE hold only the sources each test names.
        jdbc.update("update source_settings set enabled = false");
    }

    @AfterEach
    void releaseBackoff() {
        for (String id : registry.ids()) {
            backoff.onSuccess(id);
        }
    }

    private void enable(String id) {
        sources.update(id, SourceSettingsUpdate.enabled(true));
    }

    /** Inserts one {@code source_usage} row at {@code minute}. */
    private void usageRow(
            String id,
            Instant minute,
            long recordsSeen,
            long versions,
            long late,
            long clamped,
            long triggerEdges,
            long versionBytes) {
        jdbc.update(
                """
                insert into source_usage
                    (source_id, minute, requests, bytes, errors, refused, records_seen, versions,
                     live_updates, touches, late, skipped, rejected, version_bytes, clamped, trigger_edges)
                values (?, ?, 0, 0, 0, 0, ?, ?, 0, 0, ?, 0, 0, ?, ?, ?)
                """,
                id,
                Timestamp.from(minute.truncatedTo(ChronoUnit.MINUTES)),
                recordsSeen,
                versions,
                late,
                versionBytes,
                clamped,
                triggerEdges);
    }

    // ---------- views ----------

    @Nested
    @DisplayName("a source's view")
    class Views {

        @Test
        @DisplayName("combines the health row, last-hour usage and the budget")
        void combinesUsageAndBudget() {
            enable(POLLING);
            usage.record(POLLING, 1000, true);
            usage.record(POLLING, 1000, true);
            usage.record(POLLING, 1000, true);
            usage.record(POLLING, 500, false);
            usage.recordRefused(POLLING);
            tracker.recordAttempt(POLLING);
            tracker.recordSuccess(POLLING, 12, 3, false);

            SourceHealthView view = health.health(POLLING);

            assertThat(view.sourceId()).isEqualTo(POLLING);
            assertThat(view.status()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(view.requestsLastHour()).isEqualTo(4);
            assertThat(view.bytesLastHour()).isEqualTo(3500);
            assertThat(view.refusedLastHour()).isEqualTo(1);
            assertThat(view.errorRateLastHour()).isEqualTo(0.25);
            assertThat(view.lastRecordsSeen()).isEqualTo(12);
            assertThat(view.lastRecordsChanged()).isEqualTo(3);
            assertThat(view.consecutiveFailures()).isZero();
            assertThat(view.budget().perMinute()).isEqualTo(60);
            assertThat(view.budget().perDay()).isEqualTo(10_000);
            assertThat(view.budgetUsedToday()).isEqualTo(4);
            assertThat(view.budgetRemainingToday()).isEqualTo(10_000 - 4);
            assertThat(view.budgetHoursRemaining()).isCloseTo((10_000 - 4) / 4.0, within(1e-9));
            assertThat(view.lastAttemptAt()).isNotNull();
            assertThat(view.lastSuccessAt()).isNotNull();
            assertThat(view.lastDataAt()).isEqualTo(view.lastSuccessAt());
            assertThat(view.backoffUntil()).isNull();
            assertThat(view.newestFrameAt()).isNull();
            assertThat(view.frameAgeSeconds()).isNull();
        }

        @Test
        @DisplayName("an idle source has no rates, no budget outlook and a zero error rate")
        void idle() {
            enable(POLLING);

            SourceHealthView view = health.health(POLLING);

            assertThat(view.status()).isEqualTo(HealthStatus.IDLE);
            assertThat(view.errorRateLastHour()).isZero();
            assertThat(view.budgetHoursRemaining()).isNull();
            assertThat(view.storage()).isEqualTo(SourceStorage.ZERO);
        }

        @Test
        @DisplayName("failures, the redacted error and the HTTP status reach the view; three make it failing")
        void failures() {
            enable(POLLING);
            tracker.recordAttempt(POLLING);
            tracker.recordFailure(POLLING, new UpstreamHttpException(502, "Bad gateway"));

            SourceHealthView once = health.health(POLLING);
            assertThat(once.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(once.lastHttpStatus()).isEqualTo(502);
            assertThat(once.lastErrorAt()).isNotNull();

            tracker.recordFailure(POLLING, new UpstreamHttpException(502, "Bad gateway"));
            tracker.recordFailure(POLLING, new UpstreamHttpException(502, "Bad gateway"));

            SourceHealthView thrice = health.health(POLLING);
            assertThat(thrice.status()).isEqualTo(HealthStatus.FAILING);
            assertThat(thrice.statusReason()).startsWith("3 consecutive failures: UpstreamHttpException");
            assertThat(thrice.consecutiveFailures()).isEqualTo(3);
        }

        @Test
        @DisplayName("a source in backoff is failing and reports when it may be fetched again")
        void backoff() {
            enable(POLLING);
            tracker.recordAttempt(POLLING);
            Instant until = backoff.onThrottled(POLLING, Optional.of(Duration.ofMinutes(10)));

            SourceHealthView view = health.health(POLLING);

            assertThat(view.status()).isEqualTo(HealthStatus.FAILING);
            assertThat(view.statusReason()).startsWith("in backoff until");
            assertThat(view.backoffUntil()).isEqualTo(until);
        }

        @Test
        @DisplayName("acceptance: empty results after a non-empty history are degraded with the format-change reason")
        void silentBreak() {
            enable(POLLING);
            tracker.recordAttempt(POLLING);
            for (int i = 0; i < 4; i++) {
                tracker.recordSuccess(POLLING, 25, 2, false);
            }
            tracker.recordSuccess(POLLING, 0, 0, false);

            SourceHealthView view = health.health(POLLING);

            assertThat(view.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(view.statusReason()).isEqualTo(HealthEvaluator.REASON_FORMAT_CHANGE);
            assertThat(sources.view(POLLING).health().status()).isEqualTo(HealthStatus.DEGRADED);
        }

        @Test
        @DisplayName("a 304 never moves last_data_at")
        void notModifiedKeepsLastDataAt() {
            enable(POLLING);
            tracker.recordSuccess(POLLING, 8, 2, false);
            Instant pinned = Instant.parse("2026-01-01T00:00:00Z");
            jdbc.update(
                    "update source_health set last_data_at = ? where source_id = ?",
                    Timestamp.from(pinned),
                    POLLING);

            tracker.recordSuccess(POLLING, 0, 0, true);

            SourceHealthView view = health.health(POLLING);
            assertThat(view.lastDataAt()).isEqualTo(pinned);
            assertThat(view.lastSuccessAt()).isAfter(pinned);
            assertThat(view.status()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(sources.view(POLLING).health().lastDataAt()).isEqualTo(pinned);
        }

        @Test
        @DisplayName("a connected streaming source is healthy; a clean close still counts as down")
        void streaming() {
            enable(STREAMING);
            tracker.recordStreamConnected(STREAMING);

            assertThat(health.health(STREAMING).status()).isEqualTo(HealthStatus.HEALTHY);

            tracker.recordStreamDisconnected(STREAMING, null);
            // Inside the two-minute grace a disconnected stream is still healthy.
            assertThat(health.health(STREAMING).status()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(tracker.streamState(STREAMING).connected()).isFalse();
        }

        @Test
        @DisplayName("budget refusals above 20% of attempts degrade the source without counting as failures")
        void refusals() {
            enable(POLLING);
            tracker.recordAttempt(POLLING);
            tracker.recordSuccess(POLLING, 5, 1, false);
            usage.record(POLLING, 10, true);
            usage.recordRefused(POLLING);

            SourceHealthView view = health.health(POLLING);

            assertThat(view.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(view.statusReason()).isEqualTo("budget refusals 50% in last hour");
            assertThat(view.consecutiveFailures()).isZero();
        }
    }

    // ---------- storage ----------

    @Nested
    @DisplayName("storage")
    class Storage {

        @Test
        @DisplayName("is computed from source_usage: last-hour rates and a byte rate over the span observed")
        void fromSourceUsage() {
            sources.update(POLLING, SourceSettingsUpdate.enabled(true).withRetention(Duration.ofDays(10)));
            Instant now = clock.instant();
            usageRow(POLLING, now.minus(Duration.ofMinutes(5)), 600, 60, 2, 3, 15, 100_000);
            usageRow(POLLING, now.minus(Duration.ofDays(2)), 0, 0, 0, 0, 0, 400_000);
            // Outside the 7-day window: ignored entirely.
            usageRow(POLLING, now.minus(Duration.ofDays(9)), 0, 0, 0, 0, 0, 9_000_000);

            SourceStorage storage = health.health(POLLING).storage();

            assertThat(storage.observationsPerMinute()).isEqualTo(10.0);
            assertThat(storage.versionsPerMinute()).isEqualTo(1.0);
            assertThat(storage.persistRatio()).isEqualTo(0.1);
            assertThat(storage.triggerEdgeShare()).isEqualTo(0.25);
            assertThat(storage.lateLastHour()).isEqualTo(2);
            assertThat(storage.clampedLastHour()).isEqualTo(3);
            // 500,000 bytes over a little more than two days.
            assertThat(storage.bytesPerDay()).isCloseTo(250_000L, within(1_000L));
            assertThat(storage.projectedBytes()).isEqualTo(storage.bytesPerDay() * 10);
        }

        @Test
        @DisplayName("live counters count before they are flushed")
        void liveCounters() {
            enable(POLLING);
            usage.recordIngest(POLLING, new IngestCounts(120, 30, 0, 90, 1, 0, 0, 2_000, 4, 6));

            SourceStorage storage = health.health(POLLING).storage();

            assertThat(storage.observationsPerMinute()).isEqualTo(2.0);
            assertThat(storage.versionsPerMinute()).isEqualTo(0.5);
            assertThat(storage.persistRatio()).isEqualTo(0.25);
            assertThat(storage.triggerEdgeShare()).isEqualTo(0.2);
            assertThat(storage.lateLastHour()).isEqualTo(1);
            assertThat(storage.clampedLastHour()).isEqualTo(4);
            // A first minute of data is extrapolated over the one-hour floor, not multiplied by 1,440.
            assertThat(storage.bytesPerDay()).isEqualTo(2_000L * 24);
        }
    }

    // ---------- filtering and summaries ----------

    @Nested
    @DisplayName("listing")
    class Listing {

        @Test
        @DisplayName("lists every registered source, and filters by status")
        void filters() {
            enable(POLLING);
            tracker.recordAttempt(POLLING);
            tracker.recordSuccess(POLLING, 3, 1, false);
            enable(PUSH);

            List<SourceHealthView> all = health.all(Optional.empty());
            assertThat(all).extracting(SourceHealthView::sourceId).containsExactlyInAnyOrderElementsOf(registry.ids());

            assertThat(health.all(Optional.of(Set.of(HealthStatus.HEALTHY))))
                    .extracting(SourceHealthView::sourceId)
                    .containsExactly(POLLING);
            assertThat(health.all(Optional.of(Set.of(HealthStatus.IDLE))))
                    .extracting(SourceHealthView::sourceId)
                    .containsExactly(PUSH);
            assertThat(health.all(Optional.of(Set.of(HealthStatus.DISABLED))))
                    .extracting(SourceHealthView::sourceId)
                    .contains(STREAMING)
                    .doesNotContain(POLLING, PUSH);
            assertThat(health.all(Optional.of(EnumSet.of(HealthStatus.HEALTHY, HealthStatus.IDLE))))
                    .extracting(SourceHealthView::sourceId)
                    .containsExactlyInAnyOrder(POLLING, PUSH);
            assertThat(health.all(Optional.of(Set.of(HealthStatus.FAILING)))).isEmpty();
            assertThat(health.all(null)).hasSameSizeAs(all);
        }

        @Test
        @DisplayName("source summaries and details carry status, last success, last data and budget used")
        void sourceViewsCarryHealth() {
            enable(POLLING);
            usage.record(POLLING, 100, true);
            usage.record(POLLING, 100, true);
            tracker.recordAttempt(POLLING);
            tracker.recordSuccess(POLLING, 4, 1, false);
            SourceHealthEntity row = healthRows.findById(POLLING).orElseThrow();

            SourceView view = sources.view(POLLING);

            assertThat(view.health().status()).isEqualTo(HealthStatus.HEALTHY);
            assertThat(view.health().lastSuccessAt()).isEqualTo(row.getLastSuccessAt());
            assertThat(view.health().lastDataAt()).isEqualTo(row.getLastDataAt());
            assertThat(view.health().budgetUsedToday()).isEqualTo(2);
            assertThat(sources.list())
                    .filteredOn(v -> v.id().equals(STREAMING))
                    .singleElement()
                    .satisfies(v -> assertThat(v.health().status()).isEqualTo(HealthStatus.DISABLED));
        }
    }

    // ---------- gauges ----------

    @Test
    @DisplayName("gauges report 1 for the current status and the failure streak")
    void gauges() {
        enable(POLLING);
        tracker.recordAttempt(POLLING);
        for (int i = 0; i < 3; i++) {
            tracker.recordFailure(POLLING, new UpstreamIOException("connection refused", null));
        }

        health.refreshGauges();

        assertThat(statusGauge(POLLING, HealthStatus.FAILING)).isEqualTo(1.0);
        assertThat(statusGauge(POLLING, HealthStatus.HEALTHY)).isEqualTo(0.0);
        assertThat(statusGauge(STREAMING, HealthStatus.DISABLED)).isEqualTo(1.0);
        assertThat(meters.get(HealthService.FAILURES_GAUGE).tag("source", POLLING).gauge().value())
                .isEqualTo(3.0);

        tracker.recordSuccess(POLLING, 2, 1, false);
        health.health(POLLING);

        assertThat(statusGauge(POLLING, HealthStatus.HEALTHY)).isEqualTo(1.0);
        assertThat(statusGauge(POLLING, HealthStatus.FAILING)).isEqualTo(0.0);
        assertThat(meters.get(HealthService.FAILURES_GAUGE).tag("source", POLLING).gauge().value())
                .isZero();
    }

    private double statusGauge(String source, HealthStatus status) {
        return meters.get(HealthService.STATUS_GAUGE)
                .tag("source", source)
                .tag("status", status.value())
                .gauge()
                .value();
    }

    // ---------- usage meter health ----------

    @Nested
    @DisplayName("usageMeter health indicator")
    class UsageMeterIndicator {

        @Test
        @DisplayName("is registered under the name usageMeter and is UP while flushes work")
        void registered() {
            UsageMeterHealthIndicator bean =
                    context.getBean("usageMeterHealthIndicator", UsageMeterHealthIndicator.class);

            Health result = bean.health();

            assertThat(result.getStatus()).isEqualTo(Status.UP);
            assertThat(result.getDetails()).containsEntry(UsageMeterHealthIndicator.CONSECUTIVE_FAILURES, 0);
        }

        @Test
        @DisplayName("turns DOWN on a failed flush, with the count and a redacted error, and UP after a success")
        void downThenUp() {
            String password = "db-password-123456";
            JdbcTemplate failing = spy(jdbc);
            UsageMeter flaky = new UsageMeter(failing, clock);
            MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.password", password);
            UsageMeterHealthIndicator indicator = new UsageMeterHealthIndicator(flaky, environment);
            doThrow(new DataAccessResourceFailureException("authentication failed for password " + password))
                    .when(failing)
                    .batchUpdate(anyString(), anyList());

            flaky.record(POLLING, 1, true);
            flaky.flush();
            flaky.record(POLLING, 1, true);
            flaky.flush();

            Health down = indicator.health();
            assertThat(down.getStatus()).isEqualTo(Status.DOWN);
            assertThat(down.getDetails()).containsEntry(UsageMeterHealthIndicator.CONSECUTIVE_FAILURES, 2);
            assertThat((String) down.getDetails().get(UsageMeterHealthIndicator.LAST_ERROR))
                    .startsWith("DataAccessResourceFailureException: ")
                    .doesNotContain(password)
                    .contains(UrlRedactor.MASK);
            // System health only: no source's status changes because the meter cannot flush.
            enable(POLLING);
            assertThat(health.health(POLLING).status()).isEqualTo(HealthStatus.IDLE);

            doCallRealMethod().when(failing).batchUpdate(anyString(), anyList());
            flaky.record(POLLING, 1, true);
            flaky.flush();

            Health up = indicator.health();
            assertThat(up.getStatus()).isEqualTo(Status.UP);
            assertThat(up.getDetails()).containsEntry(UsageMeterHealthIndicator.CONSECUTIVE_FAILURES, 0);
            assertThat(up.getDetails()).doesNotContainKey(UsageMeterHealthIndicator.LAST_ERROR);
        }

        @Test
        @DisplayName("a very long error is cut")
        void longError() {
            JdbcTemplate failing = spy(jdbc);
            UsageMeter flaky = new UsageMeter(failing, clock);
            UsageMeterHealthIndicator indicator = new UsageMeterHealthIndicator(flaky, new MockEnvironment());
            doThrow(new DataAccessResourceFailureException("x".repeat(5_000)))
                    .when(failing)
                    .batchUpdate(anyString(), anyList());

            flaky.record(POLLING, 1, true);
            flaky.flush();

            String error = (String) indicator.health().getDetails().get(UsageMeterHealthIndicator.LAST_ERROR);
            assertThat(error).hasSizeLessThanOrEqualTo(UsageMeterHealthIndicator.MAX_ERROR_LENGTH + 20);
        }
    }
}
