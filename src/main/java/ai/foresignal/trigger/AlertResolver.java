package ai.foresignal.trigger;

import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The time-driven part of the alert lifecycle (prompt 017), every 60 s:
 *
 * <ul>
 *   <li>{@code autoResolve.onExpiry}: resolves open alerts whose entity's live row has expired
 *       ({@code record_live.valid_until} before now) or whose head is gone ({@code resolved_by =
 *       expiry});
 *   <li>{@code autoResolve.staleAfter}: resolves open alerts whose entity was last observed longer
 *       ago than that ({@code stale});
 *   <li>disables rules past {@code activeUntil} and clears expired rule snoozes, publishing
 *       {@link TriggerRulesChangedEvent}.
 * </ul>
 *
 * <p>"Now" is the application clock, like the read paths.
 */
@Component
public class AlertResolver {

    private static final Logger log = LoggerFactory.getLogger(AlertResolver.class);

    /** How often it runs. */
    public static final long INTERVAL_MS = 60_000;

    private final TriggerRuleRepository rules;
    private final RuleSpecs specs;
    private final AlertStore alerts;
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the resolver. */
    public AlertResolver(
            TriggerRuleRepository rules,
            RuleSpecs specs,
            AlertStore alerts,
            JdbcTemplate jdbc,
            ApplicationEventPublisher events,
            Clock clock) {
        this.rules = rules;
        this.specs = specs;
        this.alerts = alerts;
        this.jdbc = jdbc;
        this.events = events;
        this.clock = clock;
    }

    /** The scheduled run. */
    @Scheduled(fixedDelay = INTERVAL_MS, initialDelay = INTERVAL_MS)
    public void scheduled() {
        try {
            runOnce();
        } catch (RuntimeException e) {
            log.error("Alert resolver run failed", e);
        }
    }

    /**
     * One run.
     *
     * @return the number of alerts resolved
     */
    public int runOnce() {
        Instant now = clock.instant();
        int resolved = 0;
        for (TriggerRuleEntity entity : rules.findAll()) {
            AutoResolve auto = AutoResolve.fromStorage(entity.getAutoResolve());
            if (auto == null || !auto.needsPeriodicPass()) {
                continue;
            }
            RuleSpec rule;
            try {
                rule = specs.compile(entity);
            } catch (RuntimeException e) {
                continue;
            }
            if (auto.onExpiry()) {
                List<Long> ids = alerts.expiredOpen(rule.id(), now);
                resolved += alerts.write(w -> w.resolveIds(ids, "expiry", now)).size();
            }
            if (auto.staleAfter() != null) {
                List<Long> ids = alerts.staleOpen(rule.id(), now.minus(auto.staleAfter()));
                resolved += alerts.write(w -> w.resolveIds(ids, "stale", now)).size();
            }
        }
        List<String> disabled = jdbc.queryForList(
                "UPDATE trigger_rule SET enabled = false, updated_at = ? WHERE enabled AND active_until IS NOT NULL"
                        + " AND active_until <= ? RETURNING id::text",
                String.class, AlertStore.utc(now), AlertStore.utc(now));
        List<String> unsnoozed = jdbc.queryForList(
                "UPDATE trigger_rule SET snoozed_until = NULL WHERE snoozed_until IS NOT NULL AND snoozed_until <= ?"
                        + " RETURNING id::text",
                String.class, AlertStore.utc(now));
        if (!disabled.isEmpty()) {
            log.info("Disabled {} trigger rule(s) past activeUntil: {}", disabled.size(), disabled);
        }
        if (!disabled.isEmpty() || !unsnoozed.isEmpty()) {
            events.publishEvent(new TriggerRulesChangedEvent(null));
        }
        return resolved;
    }
}
