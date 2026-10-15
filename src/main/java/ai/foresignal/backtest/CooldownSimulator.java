package ai.foresignal.backtest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Simulates a rule's cooldown, hourly cap and active window over matches in seq order (prompt 103),
 * the same semantics as prompt 017's {@code AlertPass}: cooldown on {@code observed_at}, per entity
 * ({@code record}) or per rule, a sliding hour cap and the active window.
 */
public final class CooldownSimulator {

    private CooldownSimulator() {}

    /** One match. */
    public record Match(long seq, String entity, Instant observedAt) {}

    /** The outcome. */
    public record Result(List<Match> fired, int cooldownSuppressed, int capSuppressed, int windowSuppressed) {}

    /** Runs the simulation. */
    public static Result run(List<Match> matches, Duration cooldown, boolean ruleScope, Integer maxAlertsPerHour,
            Predicate<Instant> activeAt) {
        Duration pause = cooldown == null ? Duration.ZERO : cooldown;
        List<Match> fired = new ArrayList<>();
        Map<String, Instant> lastByScope = new HashMap<>();
        List<Instant> hour = new ArrayList<>();
        int cooldownSuppressed = 0;
        int capSuppressed = 0;
        int windowSuppressed = 0;
        for (Match match : matches) {
            Instant at = match.observedAt();
            if (at == null) {
                continue;
            }
            if (activeAt != null && !activeAt.test(at)) {
                windowSuppressed++;
                continue;
            }
            String key = ruleScope ? "rule" : match.entity();
            Instant last = lastByScope.get(key);
            if (last != null && !pause.isZero() && at.isBefore(last.plus(pause))) {
                cooldownSuppressed++;
                continue;
            }
            hour.removeIf(t -> Duration.between(t, at).compareTo(Duration.ofHours(1)) > 0);
            if (maxAlertsPerHour != null && hour.size() >= maxAlertsPerHour) {
                capSuppressed++;
                continue;
            }
            fired.add(match);
            lastByScope.put(key, at);
            hour.add(at);
        }
        return new Result(fired, cooldownSuppressed, capSuppressed, windowSuppressed);
    }
}
