package ai.foresignal.collect;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.Budget;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Decides whether a source may send another request (FR-8, G2).
 *
 * <p>Two limits per source, in one bucket so they are consumed atomically: a greedy per-minute
 * refill, and a per-day allowance that resets at UTC midnight. Keeping them in one bucket matters —
 * with two buckets, a request that passed the minute check but failed the day check would have
 * silently spent a minute token.
 *
 * <p>A refused request is <strong>not queued</strong>. Queueing would turn a budget into a delay,
 * and from the upstream's side the guarantee would no longer hold.
 *
 * <p>After a restart the daily bucket is seeded from what {@link UsageMeter} already recorded
 * today, so restarting cannot be used — accidentally or otherwise — to reset the allowance.
 */
@Component
public class BudgetGovernor {

    private static final Logger log = LoggerFactory.getLogger(BudgetGovernor.class);

    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

    private final SourceService sources;
    private final UsageMeter usage;
    private final Clock clock;
    private final ClockTimeMeter timeMeter;

    /** One bucket per source, created on first use. */
    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    public BudgetGovernor(SourceService sources, UsageMeter usage, Clock clock) {
        this.sources = sources;
        this.usage = usage;
        this.clock = clock;
        this.timeMeter = new ClockTimeMeter(clock);
    }

    /**
     * Takes one request from the source's budget.
     *
     * @return false when the budget is spent, in which case no request may be sent
     */
    public boolean tryConsume(String sourceId) {
        return entry(sourceId).bucket.tryConsume(1);
    }

    /**
     * Takes one request and charges {@code expectedBytes} against the byte budget in advance.
     *
     * <p>Charging before the transfer is what makes a byte budget meaningful for large objects: a
     * single 200 MB range read would otherwise be discovered to be over budget only after it had
     * been paid for. The estimate comes from a known size (an S3 {@code Size}, a GRIB {@code .idx}
     * offset) and is corrected by {@link #correctByteCharge} once the real size is known.
     *
     * @return false when either the request budget or the byte budget is spent
     */
    public boolean tryConsume(String sourceId, long expectedBytes) {
        Entry entry = entry(sourceId);
        if (entry.budget.hasByteBudget() && expectedBytes > 0) {
            long remaining = remainingBytesToday(sourceId, entry);
            if (expectedBytes > remaining) {
                log.debug(
                        "Refusing {}: {} bytes would exceed the remaining {} of its {} MB/day budget",
                        sourceId,
                        expectedBytes,
                        remaining,
                        entry.budget.mbPerDay());
                return false;
            }
        }
        if (!entry.bucket.tryConsume(1)) {
            return false;
        }
        if (expectedBytes > 0) {
            entry.chargedBytes.add(expectedBytes);
        }
        return true;
    }

    /**
     * Replaces an advance byte charge with the actual transferred size.
     *
     * <p>Called after a transfer completes so the day's byte accounting reflects reality rather than
     * the estimate; the authoritative figure remains what {@link UsageMeter} recorded.
     */
    public void correctByteCharge(String sourceId, long expectedBytes, long actualBytes) {
        Entry entry = buckets.get(sourceId);
        if (entry == null) {
            return;
        }
        entry.chargedBytes.add(actualBytes - expectedBytes);
    }

    /** What the source has left. */
    public BudgetStatus status(String sourceId) {
        Entry entry = entry(sourceId);
        long usedToday = usage.requestsToday(sourceId);
        long[] perBandwidth =
                entry.bucket.asVerbose().getAvailableTokens().getDiagnostics()
                        .getAvailableTokensPerEachBandwidth();
        long minuteRemaining = perBandwidth.length > 0 ? perBandwidth[0] : 0;
        long dayRemaining = perBandwidth.length > 1 ? perBandwidth[1] : 0;

        Double megabytesUsed =
                entry.budget.hasByteBudget()
                        ? (double) usage.bytesToday(sourceId) / BYTES_PER_MEGABYTE
                        : null;

        return new BudgetStatus(
                sourceId,
                entry.budget.perMinute(),
                entry.budget.perDay(),
                usedToday,
                Math.max(0, dayRemaining),
                Math.max(0, minuteRemaining),
                entry.budget.mbPerDay(),
                megabytesUsed);
    }

    /**
     * Rebuilds a source's bucket when its budget changes, keeping today's usage.
     *
     * <p>Rebuilt from scratch with {@code initialTokens = perDay - usedToday} rather than through
     * {@code replaceConfiguration}, because that applies a proportional or additive token
     * inheritance strategy; seeding from the recorded usage is exact, and exactness is the point.
     */
    @EventListener
    public void onSettingsChanged(SourceSettingsChangedEvent event) {
        Entry existing = buckets.get(event.sourceId());
        Budget after = event.after() == null ? null : event.after().budget();
        if (existing == null || after == null || existing.budget.equals(after)) {
            return;
        }
        buckets.put(event.sourceId(), newEntry(event.sourceId(), after));
        log.info(
                "Rebuilt the budget for {}: {}/min, {}/day{}",
                event.sourceId(),
                after.perMinute(),
                after.perDay(),
                after.hasByteBudget() ? ", " + after.mbPerDay() + " MB/day" : "");
    }

    /** Discards a source's bucket, so the next use rebuilds it from current settings. */
    public void invalidate(String sourceId) {
        buckets.remove(sourceId);
    }

    /** The number of buckets currently held. For tests and diagnostics. */
    int bucketCount() {
        return buckets.size();
    }

    private Entry entry(String sourceId) {
        return buckets.computeIfAbsent(
                sourceId, id -> newEntry(id, sources.effectiveBudget(id)));
    }

    private Entry newEntry(String sourceId, Budget budget) {
        long usedToday = usage.requestsToday(sourceId);
        long initialDayTokens = Math.max(0, budget.perDay() - usedToday);

        Bandwidth perMinute =
                Bandwidth.builder()
                        .capacity(budget.perMinute())
                        .refillGreedy(budget.perMinute(), Duration.ofMinutes(1))
                        .build();
        Bandwidth perDay =
                Bandwidth.builder()
                        .capacity(budget.perDay())
                        .refillIntervallyAligned(budget.perDay(), Duration.ofDays(1), nextUtcMidnight())
                        .initialTokens(initialDayTokens)
                        .build();

        LocalBucket bucket =
                Bucket.builder()
                        .addLimit(perMinute)
                        .addLimit(perDay)
                        .withCustomTimePrecision(timeMeter)
                        .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                        .build();

        if (usedToday > 0) {
            log.debug(
                    "Seeded {}'s daily budget with {} of {} already used today",
                    sourceId,
                    usedToday,
                    budget.perDay());
        }
        return new Entry(budget, bucket);
    }

    private long remainingBytesToday(String sourceId, Entry entry) {
        Long bytesPerDay = entry.budget.bytesPerDay();
        if (bytesPerDay == null) {
            return Long.MAX_VALUE;
        }
        long spent = usage.bytesToday(sourceId) + Math.max(0, entry.chargedBytes.sum());
        return Math.max(0, bytesPerDay - spent);
    }

    /** The next UTC midnight, which is when the daily allowance resets. */
    Instant nextUtcMidnight() {
        return clock.instant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    /**
     * One source's bucket and the budget it was built from.
     *
     * <p>{@code chargedBytes} tracks bytes committed in advance but not yet recorded by the meter,
     * so two large transfers started in the same minute cannot both pass a byte-budget check.
     */
    private static final class Entry {

        private final Budget budget;
        private final LocalBucket bucket;
        private final java.util.concurrent.atomic.LongAdder chargedBytes =
                new java.util.concurrent.atomic.LongAdder();

        private Entry(Budget budget, LocalBucket bucket) {
            this.budget = budget;
            this.bucket = bucket;
        }
    }
}
