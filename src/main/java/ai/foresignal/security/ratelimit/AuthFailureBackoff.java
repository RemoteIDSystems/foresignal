package ai.foresignal.security.ratelimit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Backoff after repeated failed credential checks, per client IP (prompt 035).
 *
 * <p>Callers evaluate the credential <em>first</em> and report the outcome: a correct credential is
 * always accepted ({@link #recordSuccess}), so the owner behind the same NAT as an attacker is never
 * locked out. A failure ({@link #recordFailure}) answers the normal {@code 401}/{@code 410} until
 * {@value #THRESHOLD} failures fall within {@link #WINDOW}; from then on each further failure gets
 * {@code 429} with a {@code Retry-After} that doubles from 1 s to the {@link #CAP} of 300 s. A
 * success resets the address; so does a quiet period of {@link #FORGET_AFTER} without failures,
 * which is longer than the cap so that a caller honoring {@code Retry-After} still climbs to it.
 *
 * <p>Used for bearer tokens, stream and tile tickets, ingest keys, feed keys and action capabilities
 * (033). The client IP is the one Tomcat's {@code RemoteIpValve} resolved through the trusted proxies
 * of prompt 023.
 *
 * <p>Bounded memory: at most {@link RateLimitProperties#maxTrackedAddresses()} addresses; past that,
 * forgotten entries are swept, then addresses not in backoff, and as a last resort everything. Time
 * comes from the application {@link Clock}.
 *
 * <p>Metrics: {@code foresignal.auth.failures} counts every failure reported, and
 * {@code foresignal.auth.backoff.active} gauges the addresses whose {@code Retry-After} has not yet
 * passed. With {@link RateLimitProperties#authBackoffEnabled()} off, failures are still counted but
 * never answered with {@code 429}.
 */
@Component
public class AuthFailureBackoff {

    private static final Logger log = LoggerFactory.getLogger(AuthFailureBackoff.class);

    /** Failures within {@link #WINDOW} before the backoff starts. */
    public static final int THRESHOLD = 5;

    /** The window the {@link #THRESHOLD} failures must fall in. */
    public static final Duration WINDOW = Duration.ofSeconds(60);

    /** The first {@code Retry-After}. */
    public static final Duration FIRST = Duration.ofSeconds(1);

    /** The largest {@code Retry-After}. */
    public static final Duration CAP = Duration.ofSeconds(300);

    /** An address without failures for this long is forgotten (its backoff starts over). */
    public static final Duration FORGET_AFTER = Duration.ofMinutes(15);

    /** The counter of failed credential checks. */
    public static final String FAILURES_METER = "foresignal.auth.failures";

    /** The gauge of addresses in backoff. */
    public static final String ACTIVE_METER = "foresignal.auth.backoff.active";

    /** The state of one address; guarded by its own monitor. */
    private static final class Entry {
        final Deque<Instant> recent = new ArrayDeque<>();
        int level;
        Instant lastFailure = Instant.MIN;
        Instant blockedUntil = Instant.MIN;
    }

    private final Clock clock;
    private final RateLimitProperties properties;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Counter failures;

    /** Creates the backoff and registers its meters. */
    public AuthFailureBackoff(Clock clock, RateLimitProperties properties, MeterRegistry meters) {
        this.clock = clock;
        this.properties = properties;
        this.failures = Counter.builder(FAILURES_METER)
                .description("Failed credential checks (tokens, tickets, ingest keys, feed keys, action links)")
                .register(meters);
        Gauge.builder(ACTIVE_METER, this, AuthFailureBackoff::activeCount)
                .description("Client addresses currently answered with 429 after repeated failed credentials")
                .register(meters);
    }

    /**
     * Reports a failed credential check from {@code ip}.
     *
     * @param ip the client IP ({@code request.getRemoteAddr()})
     * @return empty to answer the normal refusal ({@code 401}/{@code 410}); otherwise the
     *     {@code Retry-After} of a {@code 429 RATE_LIMITED} to answer instead
     */
    public Optional<Duration> recordFailure(String ip) {
        failures.increment();
        Instant now = clock.instant();
        String key = key(ip);
        Entry entry = entries.get(key);
        if (entry == null) {
            makeRoom(now);
            entry = entries.computeIfAbsent(key, k -> new Entry());
        }
        Duration retryAfter;
        synchronized (entry) {
            if (Duration.between(entry.lastFailure, now).compareTo(FORGET_AFTER) >= 0) {
                entry.recent.clear();
                entry.level = 0;
                entry.blockedUntil = Instant.MIN;
            }
            entry.lastFailure = now;
            Instant windowStart = now.minus(WINDOW);
            while (!entry.recent.isEmpty() && !entry.recent.peekFirst().isAfter(windowStart)) {
                entry.recent.pollFirst();
            }
            if (entry.level == 0 && entry.recent.size() < THRESHOLD) {
                entry.recent.addLast(now);
                return Optional.empty();
            }
            // In backoff: every further failure doubles the wait, up to the cap.
            entry.recent.clear();
            entry.level++;
            retryAfter = delay(entry.level);
            entry.blockedUntil = now.plus(retryAfter);
        }
        return properties.authBackoffEnabled() ? Optional.of(retryAfter) : Optional.empty();
    }

    /** Reports an accepted credential from {@code ip}: the address starts over. */
    public void recordSuccess(String ip) {
        entries.remove(key(ip));
    }

    /** The {@code Retry-After} in whole seconds of {@code retryAfter} (at least 1), for the header. */
    public static long seconds(Duration retryAfter) {
        long seconds = retryAfter.getSeconds() + (retryAfter.getNano() > 0 ? 1 : 0);
        return Math.max(1, seconds);
    }

    /** The {@code Retry-After} of the {@code level}-th failure past the threshold: 1, 2, 4 … 300 s. */
    static Duration delay(int level) {
        int shift = Math.min(Math.max(level - 1, 0), 30);
        long seconds = FIRST.getSeconds() << shift;
        return seconds >= CAP.getSeconds() ? CAP : Duration.ofSeconds(seconds);
    }

    /** The addresses whose {@code Retry-After} has not yet passed (the gauge). */
    public int activeCount() {
        Instant now = clock.instant();
        int active = 0;
        for (Entry entry : entries.values()) {
            synchronized (entry) {
                if (entry.blockedUntil.isAfter(now)) {
                    active++;
                }
            }
        }
        return active;
    }

    /** The addresses tracked (tests and diagnostics). */
    public int trackedAddresses() {
        return entries.size();
    }

    /** Forgets every address (tests). */
    public void reset() {
        entries.clear();
    }

    private void makeRoom(Instant now) {
        int max = properties.maxTrackedAddresses();
        if (entries.size() < max) {
            return;
        }
        entries.values().removeIf(e -> {
            synchronized (e) {
                return Duration.between(e.lastFailure, now).compareTo(FORGET_AFTER) >= 0;
            }
        });
        if (entries.size() < max) {
            return;
        }
        entries.values().removeIf(e -> {
            synchronized (e) {
                return e.level == 0;
            }
        });
        if (entries.size() >= max) {
            log.warn("Auth-failure backoff tracked {} addresses in backoff; forgetting all of them", entries.size());
            entries.clear();
        }
    }

    private static String key(String ip) {
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }
}
