package ai.foresignal.notify.push;

import ai.foresignal.collect.ClockTimeMeter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * One Bucket4j bucket per push-service host (prompt 031), {@code foresignal.push.per-host-rate} per
 * second (default 20), on the application clock. A push that finds no token waits without counting
 * an attempt.
 */
@Component
public class PushHostLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ClockTimeMeter time;
    private final int perSecond;

    /** Creates the limiter. */
    public PushHostLimiter(Clock clock, PushProperties properties) {
        this.time = new ClockTimeMeter(clock);
        this.perSecond = properties.perHostRate();
    }

    /**
     * Takes one token for {@code host}.
     *
     * @return {@link Duration#ZERO} when a token was taken, else how long until one refills
     */
    public Duration tryAcquire(String host) {
        Bucket bucket = buckets.computeIfAbsent(host.toLowerCase(Locale.ROOT), h -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perSecond).refillGreedy(perSecond, Duration.ofSeconds(1)).build())
                .withCustomTimePrecision(time)
                .build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return probe.isConsumed() ? Duration.ZERO : Duration.ofNanos(Math.max(1, probe.getNanosToWaitForRefill()));
    }

    /** Forgets every bucket (tests). */
    public void reset() {
        buckets.clear();
    }
}
