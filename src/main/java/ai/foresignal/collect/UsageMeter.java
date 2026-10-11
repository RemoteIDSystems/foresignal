package ai.foresignal.collect;

import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Counts what each source spends, per minute (FR-9, Architecture §5.3).
 *
 * <p>Counting in memory and flushing once a minute is deliberate: the ingest hot path can record
 * tens of thousands of events a minute, and a database write per event would make the accounting
 * more expensive than the work. {@link LongAdder} is used rather than {@code AtomicLong} because
 * these counters are written from many threads and read rarely, which is exactly what it is for.
 *
 * <p>Reads merge persisted rows with the counters not yet flushed, so a caller never sees usage
 * briefly drop to zero at the start of a minute.
 */
@Component
public class UsageMeter {

    private static final Logger log = LoggerFactory.getLogger(UsageMeter.class);

    static final long FLUSH_INTERVAL_MS = 60_000L;

    /**
     * Accumulates both request and ingest counters for one (source, minute).
     *
     * <p>Every field is a {@link LongAdder} so a flush can drain them without blocking recorders.
     */
    static final class Counters {

        final LongAdder requests = new LongAdder();
        final LongAdder bytes = new LongAdder();
        final LongAdder errors = new LongAdder();
        final LongAdder refused = new LongAdder();
        final LongAdder recordsSeen = new LongAdder();
        final LongAdder versions = new LongAdder();
        final LongAdder liveUpdates = new LongAdder();
        final LongAdder touches = new LongAdder();
        final LongAdder late = new LongAdder();
        final LongAdder skipped = new LongAdder();
        final LongAdder rejected = new LongAdder();
        final LongAdder versionBytes = new LongAdder();
        final LongAdder clamped = new LongAdder();
        final LongAdder triggerEdges = new LongAdder();

        UsageTotalsWindow snapshot() {
            return new UsageTotalsWindow(
                    requests.sum(),
                    bytes.sum(),
                    errors.sum(),
                    refused.sum(),
                    new IngestCounts(
                            recordsSeen.sum(),
                            versions.sum(),
                            liveUpdates.sum(),
                            touches.sum(),
                            late.sum(),
                            skipped.sum(),
                            rejected.sum(),
                            versionBytes.sum(),
                            clamped.sum(),
                            triggerEdges.sum()));
        }

        boolean isEmpty() {
            return snapshot().equals(UsageTotalsWindow.ZERO);
        }
    }

    /** Identifies one minute's counters for one source. */
    record UsageKey(String sourceId, Instant minute) {}

    private final JdbcTemplate jdbc;
    private final Clock clock;

    /**
     * Live counters.
     *
     * <p>A flush removes each key it drains rather than clearing the map, so an increment arriving
     * during a flush either lands in the snapshot or in a freshly created entry — never in a bucket
     * that is about to be discarded.
     */
    private final ConcurrentHashMap<UsageKey, Counters> live = new ConcurrentHashMap<>();

    /** Failed flushes since the last success. */
    private final AtomicInteger consecutiveFlushFailures = new AtomicInteger();

    /** The most recent flush error, cleared on recovery. */
    private volatile String lastFlushError;

    /** Creates a meter writing to {@code source_usage} through {@code jdbc}. */
    public UsageMeter(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // ---------- recording ----------

    /** Records one request that was sent. */
    public void record(String sourceId, long bytes, boolean success) {
        Counters counters = counters(sourceId);
        counters.requests.increment();
        counters.bytes.add(Math.max(0, bytes));
        if (!success) {
            counters.errors.increment();
        }
    }

    /** Records a request the budget declined to send. */
    public void recordRefused(String sourceId) {
        counters(sourceId).refused.increment();
    }

    /** Records what an ingest batch did. */
    public void recordIngest(String sourceId, IngestCounts counts) {
        if (counts == null || counts.isZero()) {
            return;
        }
        Counters counters = counters(sourceId);
        counters.recordsSeen.add(counts.recordsSeen());
        counters.versions.add(counts.versions());
        counters.liveUpdates.add(counts.liveUpdates());
        counters.touches.add(counts.touches());
        counters.late.add(counts.late());
        counters.skipped.add(counts.skipped());
        counters.rejected.add(counts.rejected());
        counters.versionBytes.add(counts.versionBytes());
        counters.clamped.add(counts.clamped());
        counters.triggerEdges.add(counts.triggerEdges());
    }

    private Counters counters(String sourceId) {
        return live.computeIfAbsent(
                new UsageKey(sourceId, currentMinute()), key -> new Counters());
    }

    private Instant currentMinute() {
        return clock.instant().truncatedTo(ChronoUnit.MINUTES);
    }

    // ---------- flushing ----------

    /** Writes the live counters to {@code source_usage} and clears them. */
    @Scheduled(fixedDelay = FLUSH_INTERVAL_MS)
    public void flush() {
        List<Object[]> batch = new ArrayList<>();

        // Drain by removing each key: a concurrent increment then creates a new Counters for the
        // same key and is picked up by the next flush, rather than being lost.
        for (UsageKey key : List.copyOf(live.keySet())) {
            Counters counters = live.remove(key);
            if (counters == null) {
                continue;
            }
            UsageTotalsWindow totals = counters.snapshot();
            if (totals.equals(UsageTotalsWindow.ZERO)) {
                continue;
            }
            IngestCounts ingest = totals.ingest();
            batch.add(
                    new Object[] {
                        key.sourceId(),
                        Timestamp.from(key.minute()),
                        totals.requests(),
                        totals.bytes(),
                        totals.errors(),
                        totals.refused(),
                        ingest.recordsSeen(),
                        ingest.versions(),
                        ingest.liveUpdates(),
                        ingest.touches(),
                        ingest.late(),
                        ingest.skipped(),
                        ingest.rejected(),
                        ingest.versionBytes(),
                        ingest.clamped(),
                        ingest.triggerEdges()
                    });
        }

        if (batch.isEmpty()) {
            return;
        }
        try {
            jdbc.batchUpdate(UPSERT_SQL, batch);
            onFlushSucceeded();
        } catch (RuntimeException e) {
            // Losing a minute of accounting is better than failing the scheduler thread, but it
            // must be visible: usage drives the daily budget seed after a restart, so lost counts
            // silently widen tomorrow's allowance. The usageMeter health indicator reports it.
            onFlushFailed(batch.size(), e);
        }
    }

    private void onFlushSucceeded() {
        int previous = consecutiveFlushFailures.getAndSet(0);
        if (previous > 0) {
            lastFlushError = null;
            log.info("Usage flush recovered after {} consecutive failure(s)", previous);
        }
    }

    private void onFlushFailed(int rows, RuntimeException e) {
        lastFlushError = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
        int failures = consecutiveFlushFailures.incrementAndGet();
        if (failures == 1) {
            // Once per streak: a database outage would otherwise log a stack trace every minute.
            log.warn(
                    "Could not flush {} usage row(s); those counts are lost and the daily budget seed"
                            + " will be low until flushes recover",
                    rows,
                    e);
        } else {
            log.debug("Usage flush failure #{} ({} row(s) lost)", failures, rows, e);
        }
    }

    /**
     * Flushes that have failed in a row since the last successful one.
     *
     * <p>Zero while flushing works. Read by the {@code usageMeter} health indicator.
     */
    public int consecutiveFlushFailures() {
        return consecutiveFlushFailures.get();
    }

    /**
     * The last flush error as {@code ExceptionClass: message}, unredacted, or null when the latest
     * flush succeeded. Callers that expose it must redact it.
     */
    public String lastFlushError() {
        return lastFlushError;
    }

    /** Flushes on shutdown, so the final minute is not lost. */
    @PreDestroy
    public void flushOnShutdown() {
        flush();
    }

    private static final String UPSERT_SQL =
            """
            insert into source_usage
                (source_id, minute, requests, bytes, errors, refused,
                 records_seen, versions, live_updates, touches, late, skipped, rejected, version_bytes,
                 clamped, trigger_edges)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            on conflict (source_id, minute) do update set
                requests      = source_usage.requests      + excluded.requests,
                bytes         = source_usage.bytes         + excluded.bytes,
                errors        = source_usage.errors        + excluded.errors,
                refused       = source_usage.refused       + excluded.refused,
                records_seen  = source_usage.records_seen  + excluded.records_seen,
                versions      = source_usage.versions      + excluded.versions,
                live_updates  = source_usage.live_updates  + excluded.live_updates,
                touches       = source_usage.touches       + excluded.touches,
                late          = source_usage.late          + excluded.late,
                skipped       = source_usage.skipped       + excluded.skipped,
                rejected      = source_usage.rejected      + excluded.rejected,
                version_bytes = source_usage.version_bytes + excluded.version_bytes,
                clamped       = source_usage.clamped       + excluded.clamped,
                trigger_edges = source_usage.trigger_edges + excluded.trigger_edges
            """;

    // ---------- queries ----------

    /**
     * Requests already sent today, UTC, including counters not yet flushed.
     *
     * <p>This is what seeds the daily bucket after a restart, so a restart cannot reset the
     * allowance.
     */
    public long requestsToday(String sourceId) {
        return totals(sourceId, startOfUtcDay(), clock.instant().plusSeconds(1)).requests();
    }

    /** Response bytes received today, UTC, including unflushed counters. */
    public long bytesToday(String sourceId) {
        return totals(sourceId, startOfUtcDay(), clock.instant().plusSeconds(1)).bytes();
    }

    /** Totals for the last hour. */
    public UsageTotalsWindow lastHour(String sourceId) {
        Instant now = clock.instant();
        return totals(sourceId, now.minus(1, ChronoUnit.HOURS), now.plusSeconds(1));
    }

    /** Totals over {@code [from, to)}, merging persisted rows with live counters. */
    public UsageTotalsWindow totals(String sourceId, Instant from, Instant to) {
        UsageTotalsWindow persisted = persistedTotals(sourceId, from, to);
        UsageTotalsWindow unflushed = liveTotals(sourceId, from, to);
        return persisted.plus(unflushed);
    }

    /**
     * A time series over {@code [from, to)} at {@code resolution}.
     *
     * <p>Only non-empty buckets appear; a caller wanting a dense series fills the gaps, which is
     * cheaper than returning thousands of zero rows for a quiet source.
     */
    public List<UsageSeriesPoint> series(
            String sourceId, Instant from, Instant to, UsageResolution resolution) {

        Map<Instant, UsageTotalsWindow> buckets = new LinkedHashMap<>();
        String sql =
                """
                select date_trunc(?, minute) as bucket,
                       sum(requests), sum(bytes), sum(errors), sum(refused),
                       sum(records_seen), sum(versions), sum(live_updates), sum(touches),
                       sum(late), sum(skipped), sum(rejected), sum(version_bytes),
                       sum(clamped), sum(trigger_edges)
                  from source_usage
                 where source_id = ? and minute >= ? and minute < ?
                 group by bucket
                 order by bucket
                """;
        jdbc.query(
                sql,
                rs -> {
                    buckets.put(rs.getTimestamp(1).toInstant(), readTotals(rs, 2));
                },
                resolution.sqlField(),
                sourceId,
                Timestamp.from(from),
                Timestamp.from(to));

        // Merge the unflushed counters into their buckets.
        live.forEach(
                (key, counters) -> {
                    if (!key.sourceId().equals(sourceId)
                            || key.minute().isBefore(from)
                            || !key.minute().isBefore(to)) {
                        return;
                    }
                    Instant bucket = truncate(key.minute(), resolution);
                    buckets.merge(bucket, counters.snapshot(), UsageTotalsWindow::plus);
                });

        return buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new UsageSeriesPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * The earliest minute in {@code [from, to)} with any usage for the source, persisted or live.
     *
     * <p>Health uses it to average storage over the span a source has actually been running, rather
     * than diluting a two-day-old source's rate over seven days.
     */
    public Optional<Instant> earliestMinute(String sourceId, Instant from, Instant to) {
        Timestamp persisted =
                jdbc.query(
                        "select min(minute) from source_usage where source_id = ? and minute >= ? and minute < ?",
                        rs -> rs.next() ? rs.getTimestamp(1) : null,
                        sourceId,
                        Timestamp.from(from),
                        Timestamp.from(to));
        Instant earliest = persisted == null ? null : persisted.toInstant();
        for (UsageKey key : live.keySet()) {
            if (!key.sourceId().equals(sourceId)
                    || key.minute().isBefore(from)
                    || !key.minute().isBefore(to)) {
                continue;
            }
            if (earliest == null || key.minute().isBefore(earliest)) {
                earliest = key.minute();
            }
        }
        return Optional.ofNullable(earliest);
    }

    private UsageTotalsWindow persistedTotals(String sourceId, Instant from, Instant to) {
        String sql =
                """
                select coalesce(sum(requests), 0), coalesce(sum(bytes), 0),
                       coalesce(sum(errors), 0), coalesce(sum(refused), 0),
                       coalesce(sum(records_seen), 0), coalesce(sum(versions), 0),
                       coalesce(sum(live_updates), 0), coalesce(sum(touches), 0),
                       coalesce(sum(late), 0), coalesce(sum(skipped), 0),
                       coalesce(sum(rejected), 0), coalesce(sum(version_bytes), 0),
                       coalesce(sum(clamped), 0), coalesce(sum(trigger_edges), 0)
                  from source_usage
                 where source_id = ? and minute >= ? and minute < ?
                """;
        UsageTotalsWindow result =
                jdbc.query(
                        sql,
                        rs -> rs.next() ? readTotals(rs, 1) : UsageTotalsWindow.ZERO,
                        sourceId,
                        Timestamp.from(from),
                        Timestamp.from(to));
        return result == null ? UsageTotalsWindow.ZERO : result;
    }

    private UsageTotalsWindow liveTotals(String sourceId, Instant from, Instant to) {
        UsageTotalsWindow total = UsageTotalsWindow.ZERO;
        for (Map.Entry<UsageKey, Counters> entry : live.entrySet()) {
            UsageKey key = entry.getKey();
            if (!key.sourceId().equals(sourceId)
                    || key.minute().isBefore(from)
                    || !key.minute().isBefore(to)) {
                continue;
            }
            total = total.plus(entry.getValue().snapshot());
        }
        return total;
    }

    private static UsageTotalsWindow readTotals(java.sql.ResultSet rs, int firstColumn)
            throws java.sql.SQLException {
        int i = firstColumn;
        long requests = rs.getLong(i++);
        long bytes = rs.getLong(i++);
        long errors = rs.getLong(i++);
        long refused = rs.getLong(i++);
        IngestCounts ingest =
                new IngestCounts(
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i++),
                        rs.getLong(i));
        return new UsageTotalsWindow(requests, bytes, errors, refused, ingest);
    }

    private static Instant truncate(Instant minute, UsageResolution resolution) {
        return switch (resolution) {
            case MINUTE -> minute.truncatedTo(ChronoUnit.MINUTES);
            case HOUR -> minute.truncatedTo(ChronoUnit.HOURS);
            case DAY -> minute.truncatedTo(ChronoUnit.DAYS);
        };
    }

    private Instant startOfUtcDay() {
        return clock.instant().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    /** Discards the live counters without writing them. For tests only. */
    void clearLive() {
        live.clear();
    }
}
