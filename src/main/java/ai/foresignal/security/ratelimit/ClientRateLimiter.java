package ai.foresignal.security.ratelimit;

import ai.foresignal.collect.ClockTimeMeter;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RequestPrincipal;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.SynchronizationStrategy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The per-client token buckets of the access perimeter (prompt 035, Bucket4j, no starter).
 *
 * <p>One bucket per client key, class and limit: a client key is {@code token:<id>},
 * {@code feed:<id>}, {@code ingest:<principal>} or {@code ip:<address>} (the address Tomcat's
 * {@code RemoteIpValve} resolved through the trusted proxies of prompt 023, never a raw
 * {@code X-Forwarded-For}). A bucket of {@code n} per minute holds {@code n} tokens and refills them
 * greedily over one minute, so a refused caller is told to come back when the next token is due.
 * Putting the limit into the bucket key means a changed setting or override takes effect at once; the
 * old bucket simply idles out.
 *
 * <p><b>Bounded memory.</b> A bucket idle for {@link #IDLE} has refilled completely, so dropping it
 * loses nothing; idle buckets are swept at most every {@link #SWEEP_INTERVAL}, and when
 * {@link RateLimitProperties#maxTrackedClients()} is still exceeded every bucket is dropped (the
 * limiter fails open for an instant rather than growing without bound). Concurrent stream slots are
 * counted per client and removed when they reach zero, so they are bounded by the open streams.
 *
 * <p>Time comes from the application {@link Clock} through {@link ClockTimeMeter}, so tests move it
 * with {@code MutableClock}. Every refusal increments {@code foresignal.ratelimit.rejected{class}}.
 * When {@link RateLimitProperties#enabled()} is false every call is allowed.
 *
 * <p>MCP charges its LLM-backed tools with
 * {@code tryConsume(ClientRateLimiter.clientKey(request), RateClass.EXPENSIVE)}.
 */
@Component
public class ClientRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ClientRateLimiter.class);

    /** A bucket unused this long is full again and may be dropped. */
    static final Duration IDLE = Duration.ofMinutes(2);

    /** The least time between two sweeps of idle buckets. */
    static final Duration SWEEP_INTERVAL = Duration.ofSeconds(30);

    /** {@code Retry-After} of a refusal for too many open streams: nothing refills those by time. */
    static final long CONCURRENT_RETRY_AFTER_SECONDS = 5;

    /** The meter counting refusals, tagged with {@code class}. */
    public static final String REJECTED_METER = "foresignal.ratelimit.rejected";

    /** The prefix of an IP client key. */
    public static final String IP_PREFIX = "ip:";

    /**
     * The outcome of one charge.
     *
     * @param allowed true when the request may proceed
     * @param rateClass the class that was charged (the refusing one when refused)
     * @param retryAfterSeconds when refused, the whole seconds until a retry can succeed (at least 1);
     *     0 when allowed
     */
    public record Decision(boolean allowed, RateClass rateClass, long retryAfterSeconds) {

        /** An allowed decision for {@code rateClass}. */
        public static Decision allow(RateClass rateClass) {
            return new Decision(true, rateClass, 0);
        }
    }

    /**
     * A claimed concurrent-stream slot; {@link #release()} gives it back exactly once, however many
     * times it is called.
     */
    public static final class StreamSlot {

        private final Runnable onRelease;
        private final AtomicBoolean released = new AtomicBoolean();

        StreamSlot(Runnable onRelease) {
            this.onRelease = onRelease;
        }

        /** Gives the slot back; later calls do nothing. */
        public void release() {
            if (released.compareAndSet(false, true)) {
                onRelease.run();
            }
        }
    }

    private record Held(Bucket bucket, AtomicLong lastUsedNanos) {}

    private final RateLimitSettingsService settings;
    private final RateLimitProperties properties;
    private final ClockTimeMeter time;
    private final Map<String, Held> buckets = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> openStreams = new ConcurrentHashMap<>();
    private final Map<RateClass, Counter> rejected = new EnumMap<>(RateClass.class);
    private final AtomicLong lastSweepNanos = new AtomicLong(Long.MIN_VALUE);

    /** Creates the limiter. */
    public ClientRateLimiter(RateLimitSettingsService settings, RateLimitProperties properties, Clock clock,
            MeterRegistry meters) {
        this.settings = settings;
        this.properties = properties;
        this.time = new ClockTimeMeter(clock);
        for (RateClass rateClass : RateClass.values()) {
            rejected.put(rateClass, Counter.builder(REJECTED_METER)
                    .description("Requests refused by the per-client rate limits")
                    .tag("class", rateClass.wireValue())
                    .register(meters));
        }
    }

    /** True when limits are enforced ({@code foresignal.ratelimit.enabled}). */
    public boolean enabled() {
        return properties.enabled();
    }

    /** The limits in force. */
    public RateLimitSettings settings() {
        return settings.get();
    }

    /**
     * The client key of {@code request}: the principal's {@code token:<id>} or {@code feed:<id>}, the
     * accepted token's {@code token:<id>} when no principal was bound, otherwise {@code ip:} plus the
     * resolved client address.
     */
    public static String clientKey(HttpServletRequest request) {
        String key = RequestPrincipal.of(request).map(RequestPrincipal::clientKey).orElse(null);
        if (key == null) {
            key = CurrentToken.of(request).map(t -> "token:" + t.id()).orElse(null);
        }
        return key != null ? key : ipKey(request.getRemoteAddr());
    }

    /** {@code ip:<address>}; {@code ip:unknown} when the address is missing. */
    public static String ipKey(String remoteAddr) {
        return IP_PREFIX + (remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr);
    }

    /**
     * Charges one request of {@code rateClass} to {@code clientKey} under the configured limit of
     * that class ({@link RateClass#STREAM} charges an open, not a concurrent slot). This is the entry
     * point for callers outside the filter, such as MCP's LLM-backed tools ({@link RateClass#EXPENSIVE}).
     *
     * @return the decision; always allowed when limits are disabled, or for {@link RateClass#METRICS}
     *     without a configured limit
     */
    public Decision tryConsume(String clientKey, RateClass rateClass) {
        return tryConsume(clientKey, rateClass, settings.get().perMinute(rateClass));
    }

    /**
     * Charges one request of {@code rateClass} to {@code clientKey} under an explicit limit (a token's
     * {@code maxRequestsPerMinute} override).
     *
     * @param perMinute the limit; 0 or less means unlimited
     */
    public Decision tryConsume(String clientKey, RateClass rateClass, int perMinute) {
        if (!properties.enabled() || perMinute <= 0) {
            return Decision.allow(rateClass);
        }
        long now = time.currentTimeNanos();
        sweepIfDue(now);
        String key = clientKey + '|' + rateClass.wireValue() + '|' + perMinute;
        Held held = buckets.get(key);
        if (held == null) {
            if (buckets.size() >= properties.maxTrackedClients()) {
                sweep(now);
                if (buckets.size() >= properties.maxTrackedClients()) {
                    log.warn("Rate-limit buckets exceeded {} after sweeping idle ones; dropping all",
                            properties.maxTrackedClients());
                    buckets.clear();
                }
            }
            held = buckets.computeIfAbsent(key, k -> new Held(newBucket(perMinute), new AtomicLong(now)));
        }
        held.lastUsedNanos().set(now);
        ConsumptionProbe probe = held.bucket().tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return Decision.allow(rateClass);
        }
        return reject(rateClass, seconds(probe.getNanosToWaitForRefill()));
    }

    /**
     * Claims one of {@code concurrent} stream slots for {@code clientKey}.
     *
     * @return the slot, or null when the client already holds {@code concurrent} open streams (the
     *     refusal is counted under {@link RateClass#STREAM}); a no-op slot when limits are disabled
     */
    public StreamSlot tryOpenStream(String clientKey, int concurrent) {
        if (!properties.enabled() || concurrent <= 0) {
            return new StreamSlot(() -> { });
        }
        AtomicBoolean claimed = new AtomicBoolean();
        openStreams.compute(clientKey, (k, count) -> {
            AtomicInteger c = count == null ? new AtomicInteger() : count;
            if (c.get() < concurrent) {
                c.incrementAndGet();
                claimed.set(true);
            }
            return c.get() == 0 ? null : c;
        });
        if (!claimed.get()) {
            rejected.get(RateClass.STREAM).increment();
            return null;
        }
        return new StreamSlot(() -> openStreams.computeIfPresent(clientKey,
                (k, count) -> count.decrementAndGet() <= 0 ? null : count));
    }

    /** The streams {@code clientKey} holds open (tests and diagnostics). */
    public int openStreams(String clientKey) {
        AtomicInteger count = openStreams.get(clientKey);
        return count == null ? 0 : count.get();
    }

    /** The refusal {@link Decision} for too many open streams. */
    public Decision concurrentStreamsRefused() {
        return new Decision(false, RateClass.STREAM, CONCURRENT_RETRY_AFTER_SECONDS);
    }

    /** The buckets currently held (tests and diagnostics). */
    public int trackedBuckets() {
        return buckets.size();
    }

    /** Forgets every bucket and stream slot (tests). */
    public void reset() {
        buckets.clear();
        openStreams.clear();
        lastSweepNanos.set(Long.MIN_VALUE);
    }

    private Decision reject(RateClass rateClass, long retryAfterSeconds) {
        rejected.get(rateClass).increment();
        return new Decision(false, rateClass, retryAfterSeconds);
    }

    private void sweepIfDue(long now) {
        long last = lastSweepNanos.get();
        if (last != Long.MIN_VALUE && now - last < SWEEP_INTERVAL.toNanos()) {
            return;
        }
        if (lastSweepNanos.compareAndSet(last, now)) {
            sweep(now);
        }
    }

    private void sweep(long now) {
        long idle = IDLE.toNanos();
        buckets.values().removeIf(h -> now - h.lastUsedNanos().get() >= idle);
    }

    private Bucket newBucket(int perMinute) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perMinute)
                        .refillGreedy(perMinute, Duration.ofMinutes(1)).build())
                .withCustomTimePrecision(time)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }

    /** Whole seconds, rounded up, at least 1. */
    static long seconds(long nanos) {
        return Math.max(1, (nanos + 999_999_999L) / 1_000_000_000L);
    }
}
