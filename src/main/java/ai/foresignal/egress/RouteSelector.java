package ai.foresignal.egress;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * Picks the egress route of a pull (prompt R2-018). Pure policy: the caller supplies the usable routes, the source's
 * assignment and its per-route throttles; this class keeps only the round-robin counters and the
 * rotate-on-rate-limit choices.
 *
 * <p>The rules, in order:
 * <ol>
 *   <li>{@code direct}: no route, for every source, pins included. This is the behavior before R2-018.
 *   <li>A <strong>pinned</strong> source uses its route; while that route is throttled for it the source waits, and
 *       while the route is unusable (disabled, unhealthy, unavailable on this host) the pull fails: a pin (a
 *       geo-locked source on an in-region exit) is never moved to another exit.
 *   <li>A source <strong>excluded</strong> from rotation uses the default route the same way, or goes direct when
 *       there is no default route.
 *   <li>Otherwise the rotation policy picks among the usable routes the source is not throttled on. A route that
 *       throttled this source is skipped for it, never retried early; when every usable route has throttled it, the
 *       source waits for the earliest to expire. With no usable route at all the pull fails rather than going out
 *       direct, so turning rotation on never silently exposes the server's own address.
 * </ol>
 * Within one pull the route chosen first is kept while it stays usable, so a multi-request pull leaves from one
 * address.
 */
public class RouteSelector {

    /** What a pull should do. */
    public sealed interface Selection {}

    /** Leave from the server's own address. */
    public record Direct() implements Selection {}

    /**
     * Leave through {@code routeId}.
     *
     * @param routeId the route
     */
    public record Routed(UUID routeId) implements Selection {}

    /**
     * Do not send yet: every route this source may use has throttled it.
     *
     * @param until when the earliest throttle ends
     */
    public record Wait(Instant until) implements Selection {}

    /**
     * Do not send: no route this source may use is usable.
     *
     * @param reason why, for the fetch log
     */
    public record Unavailable(String reason) implements Selection {}

    /**
     * A source's assignment.
     *
     * @param routeId the pinned route, or null
     * @param excluded whether it stays out of rotation
     */
    public record Assignment(UUID routeId, boolean excluded) {

        /** No pin, in rotation. */
        public static final Assignment NONE = new Assignment(null, false);
    }

    private final Map<String, AtomicLong> turns = new ConcurrentHashMap<>();
    private final Map<String, UUID> stuck = new ConcurrentHashMap<>();
    private final IntUnaryOperator random;

    /** Creates a selector with a thread-local random source. */
    public RouteSelector() {
        this(bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    /**
     * Creates a selector with {@code random} (tests).
     *
     * @param random returns a value in {@code [0, bound)}
     */
    public RouteSelector(IntUnaryOperator random) {
        this.random = random;
    }

    /**
     * Picks the route of one request of a pull.
     *
     * @param sourceId the source
     * @param settings the policy
     * @param pool the usable routes, in a stable order (enabled, available on this host, not held off as unhealthy)
     * @param assignment the source's assignment
     * @param current the route this pull already uses, or null for its first request
     * @param throttledUntil when the source's throttle on a route ends, or null when it is not throttled there
     */
    public Selection select(String sourceId, EgressSettings settings, List<UUID> pool, Assignment assignment,
            UUID current, Function<UUID, Instant> throttledUntil) {
        if (settings.direct()) {
            return new Direct();
        }
        Assignment a = assignment == null ? Assignment.NONE : assignment;
        if (a.routeId() != null) {
            return fixed(a.routeId(), pool, throttledUntil, "pinned");
        }
        if (a.excluded()) {
            return settings.defaultRoute() == null ? new Direct()
                    : fixed(settings.defaultRoute(), pool, throttledUntil, "default");
        }
        if (pool.isEmpty()) {
            return new Unavailable("No egress route is usable (enabled, available on this host and healthy); rotation"
                    + " is " + settings.rotation() + ", so the pull is not sent direct");
        }
        List<UUID> open = new ArrayList<>();
        Instant earliest = null;
        for (UUID id : pool) {
            Instant until = throttledUntil.apply(id);
            if (until == null) {
                open.add(id);
            } else if (earliest == null || until.isBefore(earliest)) {
                earliest = until;
            }
        }
        if (EgressSettings.PER_SOURCE_STICKY.equals(settings.rotation())) {
            UUID mine = pool.get(Math.floorMod(sourceId.hashCode(), pool.size()));
            Instant until = throttledUntil.apply(mine);
            return until == null ? new Routed(mine) : new Wait(until);
        }
        if (open.isEmpty()) {
            return new Wait(Objects.requireNonNull(earliest));
        }
        if (current != null && open.contains(current)) {
            return new Routed(current);
        }
        return switch (settings.rotation()) {
            case EgressSettings.RANDOM -> new Routed(open.get(random.applyAsInt(open.size())));
            case EgressSettings.ROTATE_ON_RATE_LIMIT -> new Routed(rotateOnRateLimit(sourceId, pool, open));
            default -> {
                long turn = turns.computeIfAbsent(sourceId, id -> new AtomicLong()).getAndIncrement();
                yield new Routed(open.get((int) Math.floorMod(turn, open.size())));
            }
        };
    }

    /** Forgets the per-source choices (a route was deleted or the policy changed). */
    public void reset() {
        turns.clear();
        stuck.clear();
    }

    private UUID rotateOnRateLimit(String sourceId, List<UUID> pool, List<UUID> open) {
        UUID previous = stuck.get(sourceId);
        if (previous != null && open.contains(previous)) {
            return previous;
        }
        UUID next = open.getFirst();
        if (previous != null && pool.contains(previous)) {
            int from = pool.indexOf(previous);
            next = open.stream()
                    .min(Comparator.comparingInt(id -> Math.floorMod(pool.indexOf(id) - from - 1, pool.size())))
                    .orElse(next);
        }
        stuck.put(sourceId, next);
        return next;
    }

    private static Selection fixed(UUID routeId, List<UUID> pool, Function<UUID, Instant> throttledUntil,
            String role) {
        if (!pool.contains(routeId)) {
            return new Unavailable("The " + role + " egress route " + routeId + " is not usable (deleted, disabled,"
                    + " unavailable on this host or unhealthy); the pull is not moved to another exit");
        }
        Instant until = throttledUntil.apply(routeId);
        return until == null ? new Routed(routeId) : new Wait(until);
    }
}
