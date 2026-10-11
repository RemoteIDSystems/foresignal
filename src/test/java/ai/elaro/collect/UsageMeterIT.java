package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.elaro.test.IntegrationTest;
import ai.elaro.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The meter against the real {@code source_usage} table.
 *
 * <p>The accumulating upsert and the live-plus-persisted merge are the two things that cannot be
 * tested without a database: the first is entirely in the {@code on conflict} clause, and the
 * second is only interesting when half the answer is already on disk. The daily budget seed after a
 * restart depends on both being right, so they are asserted here rather than against a fake.
 *
 * <p>The meter is constructed rather than autowired so the test owns the clock — the per-minute
 * bucketing is the thing being tested, and a real clock would make it depend on when the suite ran.
 */
@IntegrationTest
class UsageMeterIT {

    private static final String SOURCE = "usage-test";
    private static final String OTHER = "usage-test-other";

    /** Mid-day, mid-minute: a minute boundary at a round hour would hide a truncation bug. */
    private static final Instant START = Instant.parse("2026-03-01T12:34:17Z");

    private static final Instant MINUTE = START.truncatedTo(ChronoUnit.MINUTES);

    @Autowired private JdbcTemplate jdbc;

    private MutableClock clock;
    private UsageMeter meter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        meter = new UsageMeter(jdbc, clock);
    }

    // ---------- helpers ----------

    private List<Map<String, Object>> rows() {
        return jdbc.queryForList(
                "select * from source_usage where source_id = ? order by minute", SOURCE);
    }

    private Map<String, Object> onlyRow() {
        List<Map<String, Object>> rows = rows();
        assertThat(rows).hasSize(1);
        return rows.get(0);
    }

    private static long column(Map<String, Object> row, String name) {
        return ((Number) row.get(name)).longValue();
    }

    private static IngestCounts ingest(long recordsSeen, long versions, long touches) {
        return new IngestCounts(recordsSeen, versions, 0, touches, 0, 0, 0, 0);
    }

    // ---------- flushing ----------

    @Nested
    @DisplayName("flush")
    class Flush {

        @Test
        @DisplayName("records are written to the minute they happened in")
        void writesOneRowPerMinute() {
            meter.record(SOURCE, 100, true);
            meter.record(SOURCE, 50, false);
            meter.recordRefused(SOURCE);
            meter.flush();

            Map<String, Object> row = onlyRow();
            assertThat(row.get("minute")).isEqualTo(java.sql.Timestamp.from(MINUTE));
            assertThat(column(row, "requests")).isEqualTo(2);
            assertThat(column(row, "bytes")).isEqualTo(150);
            assertThat(column(row, "errors")).isEqualTo(1);
            assertThat(column(row, "refused")).isEqualTo(1);
        }

        @Test
        @DisplayName("a second flush of the same minute adds to the row rather than replacing it")
        void upsertAccumulates() {
            meter.record(SOURCE, 100, true);
            meter.flush();

            // Same minute, so the second flush hits the on-conflict path.
            meter.record(SOURCE, 400, false);
            meter.recordRefused(SOURCE);
            meter.flush();

            Map<String, Object> row = onlyRow();
            assertThat(column(row, "requests")).isEqualTo(2);
            assertThat(column(row, "bytes")).isEqualTo(500);
            assertThat(column(row, "errors")).isEqualTo(1);
            assertThat(column(row, "refused")).isEqualTo(1);
        }

        @Test
        @DisplayName("crossing a minute writes a new row instead of accumulating")
        void newMinuteIsANewRow() {
            meter.record(SOURCE, 10, true);
            clock.advance(Duration.ofMinutes(1));
            meter.record(SOURCE, 20, true);
            meter.flush();

            List<Map<String, Object>> rows = rows();
            assertThat(rows).hasSize(2);
            assertThat(column(rows.get(0), "bytes")).isEqualTo(10);
            assertThat(column(rows.get(1), "bytes")).isEqualTo(20);
        }

        @Test
        @DisplayName("each source gets its own row")
        void sourcesAreSeparate() {
            meter.record(SOURCE, 10, true);
            meter.record(OTHER, 20, true);
            meter.flush();

            assertThat(onlyRow()).isNotNull();
            assertThat(jdbc.queryForObject(
                            "select bytes from source_usage where source_id = ?", Long.class, OTHER))
                    .isEqualTo(20);
        }

        @Test
        @DisplayName("ingest counters accumulate on the same per-minute row")
        void ingestCountersAccumulate() {
            meter.record(SOURCE, 2048, true);
            meter.recordIngest(SOURCE, new IngestCounts(100, 20, 5, 70, 3, 1, 1, 4096));
            meter.recordIngest(SOURCE, new IngestCounts(50, 10, 1, 39, 0, 0, 0, 1024));
            meter.flush();

            Map<String, Object> row = onlyRow();
            assertThat(column(row, "requests")).isEqualTo(1);
            assertThat(column(row, "records_seen")).isEqualTo(150);
            assertThat(column(row, "versions")).isEqualTo(30);
            assertThat(column(row, "live_updates")).isEqualTo(6);
            assertThat(column(row, "touches")).isEqualTo(109);
            assertThat(column(row, "late")).isEqualTo(3);
            assertThat(column(row, "skipped")).isEqualTo(1);
            assertThat(column(row, "rejected")).isEqualTo(1);
            assertThat(column(row, "version_bytes")).isEqualTo(5120);
        }

        @Test
        @DisplayName("an ingest batch with no counts is not worth a row")
        void zeroIngestIsIgnored() {
            meter.recordIngest(SOURCE, IngestCounts.ZERO);
            meter.recordIngest(SOURCE, null);
            meter.flush();

            assertThat(rows()).isEmpty();
        }

        @Test
        @DisplayName("nothing recorded writes nothing")
        void emptyFlushIsANoOp() {
            meter.flush();
            assertThat(rows()).isEmpty();
        }

        @Test
        @DisplayName("flushed counters are not written twice")
        void flushDrainsTheLiveCounters() {
            meter.record(SOURCE, 100, true);
            meter.flush();
            meter.flush();

            assertThat(column(onlyRow(), "requests")).isEqualTo(1);
        }

        @Test
        @DisplayName("the shutdown hook flushes the final minute")
        void shutdownFlushes() {
            meter.record(SOURCE, 77, true);
            meter.flushOnShutdown();

            assertThat(column(onlyRow(), "bytes")).isEqualTo(77);
        }

        @Test
        @DisplayName("a negative byte count cannot decrease the total")
        void negativeBytesAreClampedToZero() {
            meter.record(SOURCE, -5_000, true);
            meter.flush();

            Map<String, Object> row = onlyRow();
            assertThat(column(row, "requests")).isEqualTo(1);
            assertThat(column(row, "bytes")).isZero();
        }
    }

    // ---------- queries ----------

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("totals merge persisted rows with counters not yet flushed")
        void mergesLiveAndPersisted() {
            meter.record(SOURCE, 100, true);
            meter.flush();

            // Unflushed, and in a later minute so it cannot be mistaken for the persisted row.
            clock.advance(Duration.ofMinutes(1));
            meter.record(SOURCE, 25, false);
            meter.recordRefused(SOURCE);

            UsageTotalsWindow totals = meter.lastHour(SOURCE);
            assertThat(totals.requests()).isEqualTo(2);
            assertThat(totals.bytes()).isEqualTo(125);
            assertThat(totals.errors()).isEqualTo(1);
            assertThat(totals.refused()).isEqualTo(1);
        }

        @Test
        @DisplayName("a flush does not change what a query reports")
        void flushIsInvisibleToReaders() {
            meter.record(SOURCE, 100, true);
            meter.recordIngest(SOURCE, ingest(10, 2, 8));
            UsageTotalsWindow before = meter.lastHour(SOURCE);

            meter.flush();

            assertThat(meter.lastHour(SOURCE)).isEqualTo(before);
        }

        @Test
        @DisplayName("requestsToday counts the UTC day and seeds the budget after a restart")
        void requestsTodayIsTheUtcDay() {
            // Yesterday, which must not count.
            clock.setInstant(START.minus(Duration.ofDays(1)));
            meter.record(SOURCE, 10, true);
            meter.flush();

            // Earlier today, flushed.
            clock.setInstant(START.truncatedTo(ChronoUnit.DAYS).plus(Duration.ofHours(1)));
            meter.record(SOURCE, 10, true);
            meter.record(SOURCE, 10, true);
            meter.flush();

            // Now, unflushed.
            clock.setInstant(START);
            meter.record(SOURCE, 10, true);

            assertThat(meter.requestsToday(SOURCE)).isEqualTo(3);
            assertThat(meter.bytesToday(SOURCE)).isEqualTo(30);

            // A new meter over the same database sees the same figure: this is the restart case.
            UsageMeter restarted = new UsageMeter(jdbc, clock);
            assertThat(restarted.requestsToday(SOURCE)).isEqualTo(2);
        }

        @Test
        @DisplayName("a request in this minute counts towards today even though it is not yet flushed")
        void todayIncludesTheCurrentMinute() {
            meter.record(SOURCE, 1, true);
            assertThat(meter.requestsToday(SOURCE)).isEqualTo(1);
        }

        @Test
        @DisplayName("lastHour excludes anything older than an hour")
        void lastHourIsAWindow() {
            clock.setInstant(START.minus(Duration.ofHours(2)));
            meter.record(SOURCE, 1_000, true);
            clock.setInstant(START.minus(Duration.ofMinutes(30)));
            meter.record(SOURCE, 7, true);
            meter.flush();
            clock.setInstant(START);

            UsageTotalsWindow totals = meter.lastHour(SOURCE);
            assertThat(totals.requests()).isEqualTo(1);
            assertThat(totals.bytes()).isEqualTo(7);
        }

        @Test
        @DisplayName("totals cover [from, to): the start is included and the end is not")
        void totalsWindowIsHalfOpen() {
            meter.record(SOURCE, 5, true);
            clock.advance(Duration.ofMinutes(2));
            meter.record(SOURCE, 5, true);
            meter.flush();

            assertThat(meter.totals(SOURCE, MINUTE, MINUTE.plus(Duration.ofMinutes(2))).requests())
                    .isEqualTo(1);
            assertThat(meter.totals(SOURCE, MINUTE, MINUTE.plus(Duration.ofMinutes(3))).requests())
                    .isEqualTo(2);
            assertThat(meter.totals(SOURCE, MINUTE.plusSeconds(60), MINUTE.plus(Duration.ofMinutes(3)))
                            .requests())
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("one source's usage never appears in another's totals")
        void totalsAreScopedToTheSource() {
            meter.record(OTHER, 999, true);
            meter.flush();
            meter.record(OTHER, 999, true);

            assertThat(meter.lastHour(SOURCE)).isEqualTo(UsageTotalsWindow.ZERO);
            assertThat(meter.requestsToday(SOURCE)).isZero();
        }

        @Test
        @DisplayName("a source with no usage at all reports zero rather than failing")
        void unknownSourceIsZero() {
            assertThat(meter.lastHour("never-used")).isEqualTo(UsageTotalsWindow.ZERO);
            assertThat(meter.series("never-used", MINUTE, MINUTE.plusSeconds(60), UsageResolution.MINUTE))
                    .isEmpty();
        }

        @Test
        @DisplayName("the refusal rate is the share of attempts the budget declined")
        void refusalRate() {
            meter.record(SOURCE, 1, true);
            meter.record(SOURCE, 1, true);
            meter.record(SOURCE, 1, true);
            meter.recordRefused(SOURCE);
            meter.flush();

            assertThat(meter.lastHour(SOURCE).refusalRate()).isEqualTo(0.25);
        }
    }

    // ---------- series ----------

    @Nested
    @DisplayName("series")
    class Series {

        /** Three minutes, two hours apart, spanning two UTC days. */
        private void recordAcrossDays() {
            clock.setInstant(Instant.parse("2026-03-01T10:00:00Z"));
            meter.record(SOURCE, 100, true);
            clock.setInstant(Instant.parse("2026-03-01T10:30:00Z"));
            meter.record(SOURCE, 200, false);
            clock.setInstant(Instant.parse("2026-03-02T23:00:00Z"));
            meter.record(SOURCE, 400, true);
            meter.flush();
            clock.setInstant(Instant.parse("2026-03-03T00:00:00Z"));
        }

        private List<UsageSeriesPoint> series(UsageResolution resolution) {
            return meter.series(
                    SOURCE,
                    Instant.parse("2026-03-01T00:00:00Z"),
                    Instant.parse("2026-03-03T00:00:00Z"),
                    resolution);
        }

        @Test
        @DisplayName("minute resolution keeps each minute separate")
        void minuteBuckets() {
            recordAcrossDays();

            List<UsageSeriesPoint> points = series(UsageResolution.MINUTE);
            assertThat(points).hasSize(3);
            assertThat(points.get(0).bucketStart()).isEqualTo(Instant.parse("2026-03-01T10:00:00Z"));
            assertThat(points.get(0).totals().bytes()).isEqualTo(100);
            assertThat(points.get(2).totals().bytes()).isEqualTo(400);
        }

        @Test
        @DisplayName("hour resolution sums the minutes within each hour")
        void hourBuckets() {
            recordAcrossDays();

            List<UsageSeriesPoint> points = series(UsageResolution.HOUR);
            assertThat(points).hasSize(2);
            assertThat(points.get(0).bucketStart()).isEqualTo(Instant.parse("2026-03-01T10:00:00Z"));
            assertThat(points.get(0).totals().requests()).isEqualTo(2);
            assertThat(points.get(0).totals().bytes()).isEqualTo(300);
            assertThat(points.get(0).totals().errors()).isEqualTo(1);
            assertThat(points.get(1).bucketStart()).isEqualTo(Instant.parse("2026-03-02T23:00:00Z"));
        }

        @Test
        @DisplayName("day resolution sums whole UTC days")
        void dayBuckets() {
            recordAcrossDays();

            List<UsageSeriesPoint> points = series(UsageResolution.DAY);
            assertThat(points).hasSize(2);
            assertThat(points.get(0).bucketStart()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
            assertThat(points.get(0).totals().requests()).isEqualTo(2);
            assertThat(points.get(1).bucketStart()).isEqualTo(Instant.parse("2026-03-02T00:00:00Z"));
            assertThat(points.get(1).totals().requests()).isEqualTo(1);
        }

        @Test
        @DisplayName("unflushed counters land in the same bucket as the persisted ones")
        void mergesLiveIntoTheRightBucket() {
            clock.setInstant(Instant.parse("2026-03-01T10:00:00Z"));
            meter.record(SOURCE, 100, true);
            meter.flush();

            // A different minute of the same hour, left unflushed.
            clock.setInstant(Instant.parse("2026-03-01T10:45:00Z"));
            meter.record(SOURCE, 50, true);
            meter.recordIngest(SOURCE, ingest(9, 1, 8));

            List<UsageSeriesPoint> hours =
                    meter.series(
                            SOURCE,
                            Instant.parse("2026-03-01T00:00:00Z"),
                            Instant.parse("2026-03-02T00:00:00Z"),
                            UsageResolution.HOUR);

            assertThat(hours).hasSize(1);
            assertThat(hours.get(0).bucketStart()).isEqualTo(Instant.parse("2026-03-01T10:00:00Z"));
            assertThat(hours.get(0).totals().requests()).isEqualTo(2);
            assertThat(hours.get(0).totals().bytes()).isEqualTo(150);
            assertThat(hours.get(0).totals().ingest().recordsSeen()).isEqualTo(9);
        }

        @Test
        @DisplayName("the window bounds apply to live counters too")
        void liveCountersRespectTheWindow() {
            meter.record(SOURCE, 100, true);

            assertThat(meter.series(SOURCE, MINUTE, MINUTE.plusSeconds(60), UsageResolution.MINUTE))
                    .hasSize(1);
            assertThat(meter.series(SOURCE, MINUTE.plusSeconds(60), MINUTE.plusSeconds(120),
                            UsageResolution.MINUTE))
                    .isEmpty();
        }

        @Test
        @DisplayName("quiet minutes are left out rather than returned as zeros")
        void onlyNonEmptyBucketsAppear() {
            meter.record(SOURCE, 1, true);
            clock.advance(Duration.ofMinutes(10));
            meter.record(SOURCE, 1, true);
            meter.flush();

            assertThat(meter.series(SOURCE, MINUTE, MINUTE.plus(Duration.ofHours(1)),
                            UsageResolution.MINUTE))
                    .hasSize(2);
        }
    }

    // ---------- concurrency ----------

    @Nested
    @DisplayName("concurrency")
    class Concurrency {

        private static final int THREADS = 8;
        private static final int PER_THREAD = 500;

        @Test
        @DisplayName("no increment is lost when records and flushes overlap")
        void recordDuringFlushLosesNothing() throws Exception {
            AtomicBoolean recording = new AtomicBoolean(true);
            CountDownLatch go = new CountDownLatch(1);
            List<Future<?>> recorders = new ArrayList<>();

            try (ExecutorService pool = Executors.newFixedThreadPool(THREADS + 1)) {
                for (int t = 0; t < THREADS; t++) {
                    recorders.add(
                            pool.submit(
                                    () -> {
                                        go.await();
                                        for (int i = 0; i < PER_THREAD; i++) {
                                            meter.record(SOURCE, 1, true);
                                        }
                                        return null;
                                    }));
                }
                // Flushes while the recorders run: this is the window where clearing the map rather
                // than removing each key would drop counts on the floor.
                Future<?> flusher =
                        pool.submit(
                                () -> {
                                    go.await();
                                    while (recording.get()) {
                                        meter.flush();
                                        Thread.yield();
                                    }
                                    return null;
                                });

                go.countDown();
                for (Future<?> recorder : recorders) {
                    recorder.get(60, TimeUnit.SECONDS);
                }
                recording.set(false);
                flusher.get(60, TimeUnit.SECONDS);
            }

            meter.flush();

            long expected = (long) THREADS * PER_THREAD;
            assertThat(column(onlyRow(), "requests")).isEqualTo(expected);
            assertThat(meter.requestsToday(SOURCE)).isEqualTo(expected);
        }

        @Test
        @DisplayName("concurrent recorders on one minute sum exactly")
        void concurrentRecordsSumExactly() throws Exception {
            CountDownLatch go = new CountDownLatch(1);
            try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
                for (int t = 0; t < THREADS; t++) {
                    pool.submit(
                            () -> {
                                go.await();
                                for (int i = 0; i < PER_THREAD; i++) {
                                    meter.record(SOURCE, 10, i % 5 == 0);
                                    meter.recordRefused(SOURCE);
                                    meter.recordIngest(SOURCE, IngestCounts.seen(1));
                                }
                                return null;
                            });
                }
                go.countDown();
                pool.shutdown();
                assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();
            }

            long calls = (long) THREADS * PER_THREAD;
            UsageTotalsWindow live = meter.lastHour(SOURCE);
            assertThat(live.requests()).isEqualTo(calls);
            assertThat(live.bytes()).isEqualTo(calls * 10);
            assertThat(live.errors()).isEqualTo(calls * 4 / 5);
            assertThat(live.refused()).isEqualTo(calls);
            assertThat(live.ingest().recordsSeen()).isEqualTo(calls);

            meter.flush();
            assertThat(column(onlyRow(), "requests")).isEqualTo(calls);
        }
    }
}
