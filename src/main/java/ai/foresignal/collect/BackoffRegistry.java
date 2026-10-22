package ai.foresignal.collect;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Holds a source back after an upstream asks us to slow down (FR-12).
 *
 * <p>Honoring {@code Retry-After} is the single most important piece of upstream etiquette Foresignal
 * can implement: it is the upstream telling us exactly what it wants, and ignoring it is how
 * projects get blocked. When there is no header, backoff grows exponentially from 30 seconds,
 * capped at an hour, so a persistent outage settles into hourly probes rather than hammering.
 *
 * <p>Persisted to {@code source_health.backoff_until}, so a restart during a backoff does not
 * release the brake. Prompt 006 builds the rest of that table.
 */
@Component
public class BackoffRegistry {

    private static final Logger log = LoggerFactory.getLogger(BackoffRegistry.class);

    /** First step when the upstream gives no Retry-After. */
    public static final Duration BASE_BACKOFF = Duration.ofSeconds(30);

    /** Ceiling for any backoff, however it was derived. */
    public static final Duration MAX_BACKOFF = Duration.ofHours(1);

    /**
     * Floor for a Retry-After we honor.
     *
     * <p>An upstream asking us back in under 5 seconds is almost certainly a misconfigured proxy;
     * obeying literally would be close to no backoff at all.
     */
    public static final Duration MIN_RETRY_AFTER = Duration.ofSeconds(5);

    /** Doubling past this would overflow long before it mattered, and is pointless anyway. */
    private static final int MAX_EXPONENT = 16;

    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Consecutive throttles per source, driving the exponent. */
    private final Map<String, AtomicInteger> consecutive = new ConcurrentHashMap<>();

    /** Cached backoff deadlines, so the hot path does not query on every request. */
    private final Map<String, Instant> until = new ConcurrentHashMap<>();

    /** Sources whose current backoff is a halt (prompt 064). */
    private final java.util.Set<String> halted = ConcurrentHashMap.newKeySet();

    public BackoffRegistry(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Records a throttle and sets the backoff deadline.
     *
     * @param retryAfter the upstream's own request, if it made one
     * @return when the source may be fetched again
     */
    public Instant onThrottled(String sourceId, Optional<Duration> retryAfter) {
        int attempt = consecutive.computeIfAbsent(sourceId, id -> new AtomicInteger()).incrementAndGet();
        Duration delay = retryAfter.map(BackoffRegistry::clampRetryAfter).orElseGet(() -> exponential(attempt));
        Instant deadline = clock.instant().plus(delay);
        until.put(sourceId, deadline);
        persist(sourceId, deadline);
        log.warn(
                "{} is in backoff for {}s (throttle #{}, {})",
                sourceId,
                delay.toSeconds(),
                attempt,
                retryAfter.isPresent() ? "honoring Retry-After" : "exponential");
        return deadline;
    }

    /** Clears any backoff after a successful request. */
    public void onSuccess(String sourceId) {
        AtomicInteger attempts = consecutive.remove(sourceId);
        Instant previous = until.remove(sourceId);
        boolean wasHalted = halted.remove(sourceId);
        if (previous != null || wasHalted || (attempts != null && attempts.get() > 0)) {
            persist(sourceId, null);
        }
    }

    /**
     * Holds a source for {@code hold} because the upstream's usage policy says to stop and alert a human
     * (prompt 064, {@code UpstreamHaltException}). Unlike {@link #onThrottled} the hold is not clamped to
     * {@link #MAX_BACKOFF}, and it is marked as a halt so that only {@link #clearHalt} (the operator's
     * fetch-now) or its expiry releases it.
     *
     * @param hold how long to hold the source
     * @return when the source may be fetched again
     */
    public Instant halt(String sourceId, Duration hold) {
        Duration delay = hold == null || hold.isNegative() || hold.isZero() ? Duration.ofHours(24) : hold;
        Instant deadline = clock.instant().plus(delay);
        until.put(sourceId, deadline);
        halted.add(sourceId);
        persist(sourceId, deadline, true);
        log.warn("{} is halted for {}s; automatic retries stopped until the operator fetches it manually",
                sourceId, delay.toSeconds());
        return deadline;
    }

    /** True while {@code sourceId} is held by {@link #halt} and the hold has not expired. */
    public boolean isHalted(String sourceId) {
        if (!isInBackoff(sourceId)) {
            return false;
        }
        if (halted.contains(sourceId)) {
            return true;
        }
        try {
            Boolean stored = jdbc.query("select backoff_halted from source_health where source_id = ?",
                    rs -> rs.next() ? (Boolean) rs.getObject(1) : null, sourceId);
            if (Boolean.TRUE.equals(stored)) {
                halted.add(sourceId);
                return true;
            }
        } catch (RuntimeException e) {
            log.debug("Could not read the halt flag of {}", sourceId, e);
        }
        return false;
    }

    /**
     * Releases a halt (prompt 064): the operator investigated and asked for a fetch now. Does nothing to a
     * plain backoff, which keeps honoring the upstream's own request.
     *
     * @return true when a halt was released
     */
    public boolean clearHalt(String sourceId) {
        if (!isHalted(sourceId)) {
            return false;
        }
        halted.remove(sourceId);
        until.remove(sourceId);
        consecutive.remove(sourceId);
        persist(sourceId, null);
        log.info("{}: the halt was released by a manual fetch", sourceId);
        return true;
    }

    /** True while the source must not be fetched. */
    public boolean isInBackoff(String sourceId) {
        Instant deadline = backoffUntil(sourceId);
        return deadline != null && deadline.isAfter(clock.instant());
    }

    /** When the source may be fetched again, or null when it is not in backoff. */
    public Instant backoffUntil(String sourceId) {
        Instant cached = until.get(sourceId);
        if (cached != null) {
            if (cached.isAfter(clock.instant())) {
                return cached;
            }
            // Expired: drop it so a later call does not keep re-reading a stale deadline.
            until.remove(sourceId, cached);
            return null;
        }
        return loadPersisted(sourceId);
    }

    /** Consecutive throttles recorded for the source. */
    public int consecutiveThrottles(String sourceId) {
        AtomicInteger attempts = consecutive.get(sourceId);
        return attempts == null ? 0 : attempts.get();
    }

    /** Forgets the cached state, so the next read comes from the database. For tests. */
    void clearCache() {
        consecutive.clear();
        until.clear();
        halted.clear();
    }

    // ---------- delay arithmetic ----------

    /** {@code 30s × 2^(attempt-1)}, capped at an hour. */
    static Duration exponential(int attempt) {
        int exponent = Math.min(Math.max(attempt, 1) - 1, MAX_EXPONENT);
        Duration delay = BASE_BACKOFF.multipliedBy(1L << exponent);
        return delay.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : delay;
    }

    /** Clamps an upstream's request to {@code [5s, 1h]}. */
    static Duration clampRetryAfter(Duration retryAfter) {
        if (retryAfter == null || retryAfter.isNegative()) {
            return MIN_RETRY_AFTER;
        }
        if (retryAfter.compareTo(MIN_RETRY_AFTER) < 0) {
            return MIN_RETRY_AFTER;
        }
        return retryAfter.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : retryAfter;
    }

    /**
     * Parses a {@code Retry-After} header: delta-seconds or an HTTP-date (RFC 9110).
     *
     * <p>Also accepts the bare-seconds form OpenSky sends in
     * {@code X-Rate-Limit-Retry-After-Seconds}.
     */
    public Optional<Duration> parseRetryAfter(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Optional.empty();
        }
        String trimmed = headerValue.trim();
        try {
            long seconds = Long.parseLong(trimmed);
            return Optional.of(Duration.ofSeconds(Math.max(0, seconds)));
        } catch (NumberFormatException ignored) {
            // Not delta-seconds; try the HTTP-date form.
        }
        try {
            ZonedDateTime when = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME);
            Duration delay = Duration.between(clock.instant(), when.toInstant());
            return Optional.of(delay.isNegative() ? Duration.ZERO : delay);
        } catch (DateTimeParseException e) {
            log.debug("Could not parse Retry-After '{}'; falling back to exponential backoff", trimmed);
            return Optional.empty();
        }
    }

    // ---------- persistence ----------

    /**
     * Writes the deadline in its own short transaction.
     *
     * <p>Separate from any surrounding transaction on purpose: a backoff must survive even when the
     * fetch that triggered it goes on to fail and roll back. Losing it would mean retrying
     * immediately against an upstream that just asked us to stop.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void persist(String sourceId, Instant deadline) {
        persist(sourceId, deadline, false);
    }

    /** Writes the deadline and the halt flag (prompt 064) in their own short transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void persist(String sourceId, Instant deadline, boolean halt) {
        try {
            jdbc.update(
                    """
                    insert into source_health (source_id, consecutive_failures, recent_record_counts,
                                               adapter_state, backoff_until, backoff_halted)
                    values (?, 0, '[]'::jsonb, '{}'::jsonb, ?, ?)
                    on conflict (source_id) do update set backoff_until = excluded.backoff_until,
                                                          backoff_halted = excluded.backoff_halted
                    """,
                    sourceId,
                    deadline == null ? null : java.sql.Timestamp.from(deadline),
                    halt);
        } catch (RuntimeException e) {
            // The in-memory deadline still holds for this process, so a write failure degrades
            // durability rather than politeness.
            log.error("Could not persist the backoff deadline for {}", sourceId, e);
        }
    }

    private Instant loadPersisted(String sourceId) {
        try {
            java.sql.Timestamp stored =
                    jdbc.query(
                            "select backoff_until from source_health where source_id = ?",
                            rs -> rs.next() ? rs.getTimestamp(1) : null,
                            sourceId);
            if (stored == null) {
                return null;
            }
            Instant deadline = stored.toInstant();
            if (!deadline.isAfter(clock.instant())) {
                return null;
            }
            until.put(sourceId, deadline);
            return deadline;
        } catch (RuntimeException e) {
            log.debug("Could not read the backoff deadline for {}", sourceId, e);
            return null;
        }
    }
}
