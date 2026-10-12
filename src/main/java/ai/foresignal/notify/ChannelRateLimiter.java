package ai.foresignal.notify;

import ai.foresignal.collect.ClockTimeMeter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * One Bucket4j bucket per channel, sized by the type's {@link ChannelType#rateLimit()} (prompt 018)
 * and driven by the application clock. A delivery that finds no token is rescheduled for when one
 * refills, without counting an attempt.
 */
@Component
public class ChannelRateLimiter {

    private record Entry(ChannelRateLimit limit, Bucket bucket) {}

    private final Map<UUID, Entry> buckets = new ConcurrentHashMap<>();
    private final ClockTimeMeter time;

    /** Creates the limiter. */
    public ChannelRateLimiter(Clock clock) {
        this.time = new ClockTimeMeter(clock);
    }

    /**
     * Takes one token for {@code channel}.
     *
     * @return {@link Duration#ZERO} when a token was taken, else how long until one refills
     */
    public Duration tryAcquire(UUID channel, ChannelRateLimit limit) {
        Entry entry = buckets.compute(channel, (id, current) ->
                current != null && current.limit().equals(limit) ? current : new Entry(limit, bucket(limit)));
        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(1);
        return probe.isConsumed() ? Duration.ZERO : Duration.ofNanos(Math.max(1, probe.getNanosToWaitForRefill()));
    }

    /** Forgets a channel's bucket (deleted or replaced). */
    public void forget(UUID channel) {
        buckets.remove(channel);
    }

    /** Forgets every bucket (tests). */
    public void reset() {
        buckets.clear();
    }

    private Bucket bucket(ChannelRateLimit limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(limit.capacity())
                        .refillGreedy(limit.capacity(), limit.period()).build())
                .withCustomTimePrecision(time)
                .build();
    }
}
