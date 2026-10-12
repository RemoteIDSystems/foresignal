package ai.foresignal.security.pairing;

import ai.foresignal.collect.ClockTimeMeter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.SynchronizationStrategy;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The claim limits of {@code POST /v1/pairing/claim} (prompt 023, Bucket4j): at most
 * {@value #PER_IP_PER_MINUTE} attempts per minute per client IP and {@value #GLOBAL_PER_HOUR} per
 * hour across all clients. Every attempt counts, successful or not. The client IP is the one
 * Tomcat's {@code RemoteIpValve} resolved, so it is the real client only behind a configured
 * trusted proxy ({@code FORESIGNAL_TRUSTED_PROXIES}).
 */
@Component
public class PairingRateLimiter {

    /** Attempts per minute per client IP. */
    public static final int PER_IP_PER_MINUTE = 5;

    /** Attempts per hour across every client. */
    public static final int GLOBAL_PER_HOUR = 50;

    /** Per-IP buckets kept before idle ones are dropped. */
    static final int MAX_TRACKED_IPS = 10_000;

    /**
     * The outcome of one attempt.
     *
     * @param allowed true when the attempt may proceed
     * @param retryAfterSeconds when refused, how long to wait
     */
    public record Decision(boolean allowed, long retryAfterSeconds) {}

    private final ClockTimeMeter time;
    private final Map<String, Bucket> perIp = new ConcurrentHashMap<>();
    private volatile Bucket global;

    /** Creates the limiter on {@code clock}. */
    public PairingRateLimiter(Clock clock) {
        this.time = new ClockTimeMeter(clock);
        this.global = newGlobal();
    }

    /** Counts an attempt from {@code ip} and decides whether it may proceed. */
    public Decision attempt(String ip) {
        String key = ip == null ? "unknown" : ip;
        if (perIp.size() > MAX_TRACKED_IPS) {
            perIp.clear();
        }
        Bucket bucket = perIp.computeIfAbsent(key, k -> newPerIp());
        ConsumptionProbe local = bucket.tryConsumeAndReturnRemaining(1);
        if (!local.isConsumed()) {
            return new Decision(false, seconds(local.getNanosToWaitForRefill()));
        }
        ConsumptionProbe all = global.tryConsumeAndReturnRemaining(1);
        if (!all.isConsumed()) {
            return new Decision(false, seconds(all.getNanosToWaitForRefill()));
        }
        return new Decision(true, 0);
    }

    /** Forgets every bucket (tests). */
    public void reset() {
        perIp.clear();
        global = newGlobal();
    }

    private Bucket newPerIp() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(PER_IP_PER_MINUTE)
                        .refillIntervally(PER_IP_PER_MINUTE, Duration.ofMinutes(1)).build())
                .withCustomTimePrecision(time)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }

    private Bucket newGlobal() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(GLOBAL_PER_HOUR)
                        .refillIntervally(GLOBAL_PER_HOUR, Duration.ofHours(1)).build())
                .withCustomTimePrecision(time)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }

    private static long seconds(long nanos) {
        return Math.max(1, (nanos + 999_999_999L) / 1_000_000_000L);
    }
}
