package ai.foresignal.egress;

import ai.foresignal.collect.BackoffRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The per-egress backoff of prompt R2-018: a source throttled through one route (a 429, or any {@code Retry-After}) is
 * held off that route only, with exactly {@link BackoffRegistry}'s arithmetic ({@code Retry-After} honored, clamped to
 * [5 s, 1 h], escalating on repeats; without one, the exponential ladder from 30 s). The other routes stay usable for
 * it, so one egress hitting a limit does not stall the rest, and the egress that received the {@code Retry-After} is
 * the one that waits.
 *
 * <p>Persisted to {@code egress_backoff}, so a restart does not release the brake; cached in memory for the hot path.
 */
@Component
public class EgressBackoff {

    private static final Logger log = LoggerFactory.getLogger(EgressBackoff.class);

    /**
     * One held source.
     *
     * @param sourceId the source
     * @param routeId the route it is held off
     * @param until when the hold ends
     * @param consecutive the consecutive throttles behind it
     */
    public record Hold(String sourceId, UUID routeId, Instant until, int consecutive) {}

    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();
    private volatile boolean loaded;

    /** Creates the registry. */
    public EgressBackoff(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** When {@code sourceId}'s hold on {@code routeId} ends, or null when it may use the route now. */
    public Instant until(String sourceId, UUID routeId) {
        load();
        Hold hold = holds.get(key(sourceId, routeId));
        return hold == null || !hold.until().isAfter(clock.instant()) ? null : hold.until();
    }

    /**
     * Records a throttle of {@code sourceId} through {@code routeId}.
     *
     * @param retryAfter the upstream's own request, if it made one
     * @return when the source may use the route again
     */
    public Instant onThrottled(String sourceId, UUID routeId, Optional<Duration> retryAfter) {
        load();
        Hold previous = holds.get(key(sourceId, routeId));
        int attempt = previous == null ? 1 : previous.consecutive() + 1;
        Instant deadline = clock.instant().plus(BackoffRegistry.throttleDelay(attempt, retryAfter));
        Hold hold = new Hold(sourceId, routeId, deadline, attempt);
        holds.put(key(sourceId, routeId), hold);
        jdbc.update("""
                INSERT INTO egress_backoff (source_id, route_id, backoff_until, consecutive) VALUES (?, ?, ?, ?)
                ON CONFLICT (source_id, route_id) DO UPDATE SET backoff_until = EXCLUDED.backoff_until,
                    consecutive = EXCLUDED.consecutive
                """, sourceId, routeId, Timestamp.from(deadline), attempt);
        log.warn("{} is held off egress {} until {} (throttle #{}, {})", sourceId, routeId, deadline, attempt,
                retryAfter.isPresent() ? "honoring Retry-After" : "exponential");
        return deadline;
    }

    /** Clears {@code sourceId}'s hold on {@code routeId} after a successful request through it. */
    public void onSuccess(String sourceId, UUID routeId) {
        load();
        if (holds.remove(key(sourceId, routeId)) != null) {
            jdbc.update("DELETE FROM egress_backoff WHERE source_id = ? AND route_id = ?", sourceId, routeId);
        }
    }

    /** The holds on {@code routeId}, current or expired, by source. */
    public List<Hold> forRoute(UUID routeId) {
        load();
        List<Hold> out = new ArrayList<>();
        holds.values().stream().filter(h -> h.routeId().equals(routeId))
                .sorted(java.util.Comparator.comparing(Hold::sourceId)).forEach(out::add);
        return out;
    }

    /** Forgets the holds on {@code routeId} (it was deleted; the rows cascade). */
    public void forget(UUID routeId) {
        holds.values().removeIf(h -> h.routeId().equals(routeId));
    }

    /** Drops the cache, so the next read reloads it (tests). */
    void reload() {
        holds.clear();
        loaded = false;
    }

    private void load() {
        if (loaded) {
            return;
        }
        synchronized (this) {
            if (loaded) {
                return;
            }
            jdbc.query("SELECT source_id, route_id, backoff_until, consecutive FROM egress_backoff", rs -> {
                Hold hold = new Hold(rs.getString(1), rs.getObject(2, UUID.class), rs.getTimestamp(3).toInstant(),
                        rs.getInt(4));
                holds.put(key(hold.sourceId(), hold.routeId()), hold);
            });
            loaded = true;
        }
    }

    private static String key(String sourceId, UUID routeId) {
        return sourceId + "|" + routeId;
    }
}
