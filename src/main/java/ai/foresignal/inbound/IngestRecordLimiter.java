package ai.foresignal.inbound;

import ai.foresignal.collect.ClockTimeMeter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The records-per-minute cap of push sources (prompt 024): a Bucket4j bucket per source sized
 * {@code records_per_minute} (default 6,000, at most 60,000), consumed for a whole request at once.
 *
 * <p>Separate from the request budget of {@code BudgetGovernor}: a single request may carry 10,000
 * records, so a per-request limit alone would not bound the write load.
 */
@Component
public class IngestRecordLimiter {

    private final ClockTimeMeter time;
    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    /** Creates the limiter. */
    public IngestRecordLimiter(Clock clock) {
        this.time = new ClockTimeMeter(clock);
    }

    /**
     * Takes {@code records} tokens from the source's bucket.
     *
     * @param sourceId the source
     * @param perMinute the source's current cap
     * @param records the request's record count
     * @return 0 when consumed, otherwise the seconds to wait before retrying (at least 1)
     */
    public long tryConsume(String sourceId, int perMinute, int records) {
        if (records <= 0) {
            return 0;
        }
        Entry entry = buckets.compute(sourceId, (id, existing) ->
                existing != null && existing.perMinute == perMinute ? existing : new Entry(perMinute, bucket(perMinute)));
        if (records > perMinute) {
            // Larger than the whole bucket: it can never pass; ask for a full minute.
            return 60;
        }
        ConsumptionProbe probe = entry.bucket.tryConsumeAndReturnRemaining(records);
        if (probe.isConsumed()) {
            return 0;
        }
        return Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
    }

    /** Forgets a deleted source's bucket. */
    public void forget(String sourceId) {
        buckets.remove(sourceId);
    }

    private LocalBucket bucket(int perMinute) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1)).build())
                .withCustomTimePrecision(time)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }

    private record Entry(int perMinute, LocalBucket bucket) {}
}
