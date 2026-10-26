package ai.foresignal.egress;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.system.OperationalAlertService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The health of each egress route (prompt R2-018), kept in memory: whether the last call through it got through, its
 * latency, the exit IP the last test observed, and the consecutive failures.
 *
 * <p>A request whose connection through a route fails (the proxy refuses, the tunnel cannot be set up, the bound
 * address is gone) counts a failure; any success through the route, for any source, clears them. At
 * {@value #FAILURE_THRESHOLD} consecutive failures the route is <em>unhealthy</em>: the selector skips it until
 * {@code backoffUntil} (30 s, doubling to an hour), after which the next pull may try it again, and the operational
 * alert {@code system.egress_unhealthy} is raised (prompt R2-014); the next success raises
 * {@code system.egress_recovered}.
 */
@Component
public class EgressHealthRegistry {

    private static final Logger log = LoggerFactory.getLogger(EgressHealthRegistry.class);

    /** Consecutive failures that make a route unhealthy. */
    public static final int FAILURE_THRESHOLD = 3;

    /**
     * One route's health.
     *
     * @param reachable whether the last call through it got through, or null before any
     * @param exitIp the public address the last test observed, or null
     * @param latencyMs the last call's latency, or null
     * @param lastCheckedAt when the last call through it finished, or null
     * @param consecutiveFailures failures since the last success
     * @param backoffUntil skipped until then, or null
     * @param lastError the last failure, or null
     */
    public record State(Boolean reachable, String exitIp, Double latencyMs, Instant lastCheckedAt,
            int consecutiveFailures, Instant backoffUntil, String lastError) {

        static final State UNKNOWN = new State(null, null, null, null, 0, null, null);
    }

    private final Map<UUID, State> states = new ConcurrentHashMap<>();
    private final ObjectProvider<OperationalAlertService> alerts;
    private final Clock clock;

    /** Creates the registry. */
    public EgressHealthRegistry(ObjectProvider<OperationalAlertService> alerts, Clock clock) {
        this.alerts = alerts;
        this.clock = clock;
    }

    /** The state of {@code routeId}. */
    public State state(UUID routeId) {
        return states.getOrDefault(routeId, State.UNKNOWN);
    }

    /** True unless the route is held off as unhealthy. */
    public boolean usable(UUID routeId) {
        Instant until = state(routeId).backoffUntil();
        return until == null || !clock.instant().isBefore(until);
    }

    /**
     * Records a call that got through {@code route}.
     *
     * @param latency how long it took, or null
     * @param exitIp the exit IP it observed (a test call), or null to keep the last one
     */
    public synchronized void success(EgressRoute route, Duration latency, String exitIp) {
        State previous = state(route.id());
        states.put(route.id(), new State(true, exitIp != null ? exitIp : previous.exitIp(),
                latency == null ? previous.latencyMs() : latency.toNanos() / 1e6, clock.instant(), 0, null, null));
        if (previous.consecutiveFailures() >= FAILURE_THRESHOLD) {
            log.info("Egress route {} ({}) is healthy again", route.name(), route.id());
            report(route, "ok", null);
        }
    }

    /** Records a call whose connection through {@code route} failed. */
    public synchronized void failure(EgressRoute route, String reason) {
        State previous = state(route.id());
        int failures = previous.consecutiveFailures() + 1;
        Instant until = null;
        if (failures >= FAILURE_THRESHOLD) {
            Duration hold = BackoffRegistry.throttleDelay(failures - FAILURE_THRESHOLD + 1, Optional.empty());
            until = clock.instant().plus(hold);
            log.warn("Egress route {} ({}) is unhealthy after {} failures; skipped until {}: {}", route.name(),
                    route.id(), failures, until, reason);
        }
        states.put(route.id(), new State(false, previous.exitIp(), previous.latencyMs(), clock.instant(), failures,
                until, reason));
        if (failures >= FAILURE_THRESHOLD) {
            report(route, "warn", reason);
        }
    }

    /** Forgets {@code routeId} (deleted or replaced) and clears its alert. */
    public void forget(EgressRoute route) {
        states.remove(route.id());
        report(route, "ok", null);
    }

    /** The {@code EgressHealth} view of {@code routeId}, without the per-source holds. */
    public Map<String, Object> view(UUID routeId) {
        State s = state(routeId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reachable", Boolean.TRUE.equals(s.reachable()));
        out.put("exitIp", s.exitIp());
        out.put("latencyMs", s.latencyMs());
        out.put("lastCheckedAt", s.lastCheckedAt());
        out.put("consecutiveFailures", s.consecutiveFailures());
        out.put("backoffUntil", s.backoffUntil() != null && s.backoffUntil().isAfter(clock.instant())
                ? s.backoffUntil() : null);
        out.put("lastError", s.lastError());
        return out;
    }

    /** Drops every state (tests). */
    void clear() {
        states.clear();
    }

    private void report(EgressRoute route, String level, String reason) {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("routeId", route.id());
        data.put("name", route.name());
        data.put("type", route.type());
        path.report("egress:" + route.id(), level, (now, previous) -> "ok".equals(now)
                ? new OperationalAlertService.Alert("system.egress_recovered", "info",
                        "Egress route " + route.name() + " is working again",
                        "A call through egress route " + route.name() + " got through.",
                        "/v1/egress/" + route.id() + "/health", data)
                : new OperationalAlertService.Alert("system.egress_unhealthy", "warning",
                        "Egress route " + route.name() + " is unhealthy",
                        "Calls through egress route " + route.name() + " failed " + FAILURE_THRESHOLD
                                + " times in a row; it is skipped until it recovers. Last error: " + reason,
                        "/v1/egress/" + route.id() + "/health", data));
    }
}
