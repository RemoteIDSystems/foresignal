package ai.foresignal.notify.action;

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
 * The request limit of {@code /v1/actions/*} (prompt 033, Bucket4j): at most
 * {@value #PER_IP_PER_MINUTE} requests per minute per client IP, GET and POST together. The client
 * IP is the one Tomcat's {@code RemoteIpValve} resolved (trusted proxies, 023). Buckets are kept in
 * memory, per process.
 */
@Component
public class ActionRateLimiter {

    /** Requests per minute per client IP. */
    public static final int PER_IP_PER_MINUTE = 30;

    /** Per-IP buckets kept before they are all dropped. */
    static final int MAX_TRACKED_IPS = 10_000;

    /**
     * The outcome of one request.
     *
     * @param allowed true when it may proceed
     * @param retryAfterSeconds when refused, how long to wait
     */
    public record Decision(boolean allowed, long retryAfterSeconds) {}

    private final ClockTimeMeter time;
    private final Map<String, Bucket> perIp = new ConcurrentHashMap<>();

    /** Creates the limiter on {@code clock}. */
    public ActionRateLimiter(Clock clock) {
        this.time = new ClockTimeMeter(clock);
    }

    /** Counts a request from {@code ip}. */
    public Decision attempt(String ip) {
        if (perIp.size() > MAX_TRACKED_IPS) {
            perIp.clear();
        }
        Bucket bucket = perIp.computeIfAbsent(ip == null ? "unknown" : ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(PER_IP_PER_MINUTE)
                        .refillIntervally(PER_IP_PER_MINUTE, Duration.ofMinutes(1)).build())
                .withCustomTimePrecision(time)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return new Decision(true, 0);
        }
        return new Decision(false, Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L));
    }

    /** Forgets every bucket (tests). */
    public void reset() {
        perIp.clear();
    }
}
