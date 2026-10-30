package ai.foresignal.briefing;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Breaking-news special editions (prompt NP-006): listens to the {@link AlertBus} and, when an alert fires that an
 * enabled edition's {@code breakingNews} names, runs that edition at once through {@link BriefingService#runBreaking}.
 *
 * <p>An alert matches an edition when all of these hold:
 *
 * <ul>
 *   <li>it is a new alert ({@code fired}), neither suppressed by a snooze nor an exercise replay (115);
 *   <li>the edition is enabled and its {@code breakingNews.enabled} is true;
 *   <li>its rule is in {@code breakingNews.ruleIds}, or, when that is empty, in the scope's {@code ruleIds};
 *   <li>its severity is at or above {@code breakingNews.minSeverity};
 *   <li>for a user-owned edition (NP-003), the rule is shared or the edition owner's own: one user's alert never
 *       fires another user's edition.
 * </ul>
 *
 * <p>The cooldown ({@code cooldownMinutes}) is claimed with one conditional update, so a burst of alerts, on one
 * instance or several, coalesces into one breaking run per window. Left out in edge mode, with the briefings.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class BreakingNewsListener implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(BreakingNewsListener.class);

    private final AlertBus bus;
    private final BriefingRepository briefings;
    private final BriefingService service;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<UserRepository> users;
    private Subscription subscription;

    /** Creates the listener. */
    public BreakingNewsListener(AlertBus bus, BriefingRepository briefings, BriefingService service, JdbcTemplate jdbc,
            ObjectProvider<UserRepository> users) {
        this.bus = bus;
        this.briefings = briefings;
        this.service = service;
        this.jdbc = jdbc;
        this.users = users;
    }

    /** Subscribes to the alert bus with the other alert consumers. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.NOTIFY_ROUTER)
    public synchronized void start() {
        if (subscription == null) {
            subscription = bus.subscribe("breaking-news", this, AlertBus.DEFAULT_QUEUE);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @Override
    public void onAlert(AlertEvent event) {
        try {
            fire(event);
        } catch (RuntimeException e) {
            log.warn("Breaking-news editions for alert {} could not be started: {}", event.alert().id(), e.toString());
        }
    }

    /**
     * Starts the breaking runs {@code event} fires.
     *
     * @return one future per started run (tests await them)
     */
    public List<Future<BriefingRunEntity>> fire(AlertEvent event) {
        List<Future<BriefingRunEntity>> started = new ArrayList<>();
        AlertView alert = event.alert();
        if (!AlertEvent.FIRED.equals(event.type()) || event.suppressed() || alert.suppressed() || alert.exercise()
                || alert.ruleId() == null) {
            return started;
        }
        List<UUID> candidates = jdbc.queryForList("SELECT id FROM briefing WHERE enabled AND breaking_news IS NOT NULL"
                + " AND (breaking_news ->> 'enabled') = 'true' ORDER BY created_at, id", UUID.class);
        for (UUID id : candidates) {
            BriefingEntity briefing = briefings.findById(id).orElse(null);
            if (briefing == null) {
                continue;
            }
            BriefingInput input = BriefingInput.of(briefing);
            BriefingInput.BreakingNews config = input.edition().breakingNews();
            if (!matches(config, input.scope(), alert) || !ownerMayUse(briefing, alert)) {
                continue;
            }
            Optional<Future<BriefingRunEntity>> run = service.runBreaking(id, BreakingTrigger.of(alert),
                    Duration.ofMinutes(config.cooldownMinutes()));
            run.ifPresent(started::add);
        }
        return started;
    }

    /** True when {@code alert} fires an edition with {@code config} and {@code scope} (rule and severity). */
    static boolean matches(BriefingInput.BreakingNews config, BriefingScope scope, AlertView alert) {
        if (config == null || !config.enabled()) {
            return false;
        }
        List<UUID> rules = config.ruleIds().isEmpty() ? (scope.ruleIds() == null ? List.of() : scope.ruleIds())
                : config.ruleIds();
        if (!rules.contains(alert.ruleId())) {
            return false;
        }
        try {
            return Severity.fromWire(alert.severity()).compareTo(Severity.fromWire(config.minSeverity())) >= 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** True when the alert's rule is shared, the edition is no user's, or the rule is the edition owner's own. */
    boolean ownerMayUse(BriefingEntity briefing, AlertView alert) {
        if (alert.ownerPrincipalId() == null || briefing.getOwnerUserId() == null) {
            return true;
        }
        UserRepository repo = users.getIfAvailable();
        return repo != null && repo.findById(briefing.getOwnerUserId())
                .map(u -> alert.ownerPrincipalId().equals(u.getPrincipalId())).orElse(false);
    }
}
