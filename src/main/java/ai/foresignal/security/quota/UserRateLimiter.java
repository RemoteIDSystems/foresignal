package ai.foresignal.security.quota;

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
 * One Bucket4j bucket per user (prompt R2-026), modeled on {@code ChannelRateLimiter}: {@code apiRateLimitPerMinute}
 * tokens refilled greedily over a minute and driven by the application clock, so a user's sessions and own API tokens
 * draw on one budget. A bucket is rebuilt when the user's limit changes.
 *
 * <p>It also counts each user's requests in the current clock minute, limited or not, for {@code getMyQuota}.
 */
@Component
public class UserRateLimiter {

    private record Entry(int perMinute, Bucket bucket) {}

    private static final class Window {
        private long minute = Long.MIN_VALUE;
        private long count;

        synchronized void add(long now) {
            if (now != minute) {
                minute = now;
                count = 0;
            }
            count++;
        }

        synchronized long at(long now) {
            return now == minute ? count : 0;
        }
    }

    private final Map<UUID, Entry> buckets = new ConcurrentHashMap<>();
    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();
    private final ClockTimeMeter time;
    private final Clock clock;

    /**
     * Creates the limiter.
     *
     * @param clock the clock
     */
    public UserRateLimiter(Clock clock) {
        this.clock = clock;
        this.time = new ClockTimeMeter(clock);
    }

    /**
     * Counts one request of {@code user}, whether or not it is limited.
     *
     * @param user the user
     */
    public void record(UUID user) {
        windows.computeIfAbsent(user, u -> new Window()).add(currentMinute());
    }

    /**
     * The requests {@code user} made in the current clock minute.
     *
     * @param user the user
     * @return the count
     */
    public long requestsThisMinute(UUID user) {
        Window w = windows.get(user);
        return w == null ? 0 : w.at(currentMinute());
    }

    /**
     * Takes one token for {@code user}.
     *
     * @param user the user
     * @param perMinute the user's {@code apiRateLimitPerMinute}; {@code 0} refuses every request
     * @return {@link Duration#ZERO} when a token was taken, else how long until one refills
     */
    public Duration tryAcquire(UUID user, int perMinute) {
        if (perMinute <= 0) {
            return Duration.ofMinutes(1);
        }
        Entry entry = buckets.compute(user, (id, current) ->
                current != null && current.perMinute() == perMinute ? current : new Entry(perMinute,
                        bucket(perMinute)));
        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(1);
        return probe.isConsumed() ? Duration.ZERO : Duration.ofNanos(Math.max(1, probe.getNanosToWaitForRefill()));
    }

    /** Forgets every bucket and count (tests). */
    public void reset() {
        buckets.clear();
        windows.clear();
    }

    private long currentMinute() {
        return clock.millis() / 60_000L;
    }

    private Bucket bucket(int perMinute) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1))
                        .build())
                .withCustomTimePrecision(time)
                .build();
    }
}
