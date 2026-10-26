package ai.foresignal.trigger;

import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Moves or deletes a deleted user's trigger rules (prompt R2-008, the {@link UserOwnedConfigReassigner} of R2-005).
 *
 * <p>Reassigning moves the rules <em>and</em> their alerts to the target admin's principal, so the rules keep firing
 * and their history stays visible to whoever now owns them; with no target the rules and alerts become shared. Deleting
 * removes the rules; their alerts are kept, as when a rule is deleted through the API, and stay visible only to callers
 * that see every alert. Either way the engine reloads the rules ({@link TriggerRulesChangedEvent}) once the deletion
 * commits.
 */
@Component
public class TriggerRuleReassigner implements UserOwnedConfigReassigner {

    private final UserRepository users;
    private final TriggerRuleRepository rules;
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Creates the reassigner.
     *
     * @param users the user store, mapping a user to their principal
     * @param rules the rule store
     * @param jdbc the template, for the alerts
     * @param events the event publisher
     * @param clock the clock
     */
    public TriggerRuleReassigner(UserRepository users, TriggerRuleRepository rules, JdbcTemplate jdbc,
            ApplicationEventPublisher events, Clock clock) {
        this.users = users;
        this.rules = rules;
        this.jdbc = jdbc;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "triggerRules";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        String to = target == null ? null : principal(target);
        List<TriggerRuleEntity> owned = rules.findByOwnerPrincipalId(from);
        for (TriggerRuleEntity rule : owned) {
            rule.setOwnerPrincipalId(to);
            rule.setUpdatedAt(clock.instant());
        }
        rules.saveAllAndFlush(owned);
        jdbc.update("UPDATE alert SET owner_principal_id = ? WHERE owner_principal_id = ?", to, from);
        changed(owned);
        return owned.size();
    }

    @Override
    public int delete(UUID owner) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        List<TriggerRuleEntity> owned = rules.findByOwnerPrincipalId(from);
        rules.deleteAll(owned);
        rules.flush();
        changed(owned);
        return owned.size();
    }

    /** Reloads the engine once the deletion commits, so a rollback never leaves it without the rules. */
    private void changed(List<TriggerRuleEntity> owned) {
        List<UUID> ids = owned.stream().map(TriggerRuleEntity::getId).toList();
        Runnable publish = () -> ids.forEach(id -> events.publishEvent(new TriggerRulesChangedEvent(id)));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    private String principal(UUID user) {
        return users.findById(user).map(UserEntity::getPrincipalId).orElse(null);
    }
}
