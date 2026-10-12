package ai.foresignal.trigger;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Trigger rule management (prompt 017): create, replace, delete, list, snooze. Every change
 * publishes {@link TriggerRulesChangedEvent}, so the persist gate and the engine pick it up at once.
 *
 * <p>Takes and returns plain values, so prompt 025 can expose {@code snooze_rule} and
 * {@code test_trigger} as MCP tools without web types.
 */
@Service
public class TriggerRuleService {

    private final TriggerRuleRepository repository;
    private final RuleValidator validator;
    private final AlertStore alerts;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the service. */
    public TriggerRuleService(
            TriggerRuleRepository repository,
            RuleValidator validator,
            AlertStore alerts,
            ApplicationEventPublisher events,
            Clock clock) {
        this.repository = repository;
        this.validator = validator;
        this.alerts = alerts;
        this.events = events;
        this.clock = clock;
    }

    /**
     * A rule with its alert statistics.
     *
     * @param rule the stored rule
     * @param stats its alert statistics
     */
    public record RuleView(TriggerRuleEntity rule, AlertStore.RuleStats stats) {}

    /**
     * Every rule, oldest first.
     *
     * @param tag only rules with this tag, or null
     * @param enabled only rules with this enabled flag, or null
     */
    public List<RuleView> list(String tag, Boolean enabled) {
        List<TriggerRuleEntity> rules = new ArrayList<>();
        for (TriggerRuleEntity rule : repository.findAll()) {
            if (tag != null && !rule.getTags().contains(tag)) {
                continue;
            }
            if (enabled != null && rule.isEnabled() != enabled) {
                continue;
            }
            rules.add(rule);
        }
        rules.sort(Comparator.comparing(TriggerRuleEntity::getCreatedAt).thenComparing(r -> r.getId().toString()));
        Map<UUID, AlertStore.RuleStats> stats = alerts.stats(rules.stream().map(TriggerRuleEntity::getId).toList());
        return rules.stream()
                .map(r -> new RuleView(r, stats.getOrDefault(r.getId(), AlertStore.RuleStats.empty())))
                .toList();
    }

    /**
     * One rule.
     *
     * @throws NotFoundException when there is no such rule
     */
    public RuleView get(UUID id) {
        return view(load(id));
    }

    /** The stored rule, or 404. */
    public TriggerRuleEntity load(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Trigger rule", id));
    }

    /**
     * Creates a rule from a {@code TriggerRuleInput}.
     *
     * @throws ai.foresignal.api.error.ValidationException when the input is not valid
     */
    public RuleView create(JsonNode input) {
        ValidatedRule valid = validator.validate(input);
        Instant now = clock.instant();
        TriggerRuleEntity rule = new TriggerRuleEntity(UUID.randomUUID(), valid.name());
        valid.applyTo(rule);
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(saved.getId());
        return view(saved);
    }

    /**
     * Creates the rule with {@code id}, or replaces it when it exists (settings import, prompt 019).
     *
     * @throws ai.foresignal.api.error.ValidationException when the input is not valid
     */
    public RuleView upsert(UUID id, JsonNode input) {
        if (repository.existsById(id)) {
            return update(id, input);
        }
        ValidatedRule valid = validator.validate(input);
        Instant now = clock.instant();
        TriggerRuleEntity rule = new TriggerRuleEntity(id, valid.name());
        valid.applyTo(rule);
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(saved.getId());
        return view(saved);
    }

    /**
     * Replaces a rule whole with a {@code TriggerRuleInput}; snooze and storm counters are kept.
     *
     * @throws NotFoundException when there is no such rule
     */
    public RuleView update(UUID id, JsonNode input) {
        TriggerRuleEntity rule = load(id);
        ValidatedRule valid = validator.validate(input);
        valid.applyTo(rule);
        rule.setUpdatedAt(clock.instant());
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(id);
        return view(saved);
    }

    /**
     * Deletes a rule; its alerts are kept.
     *
     * @throws NotFoundException when there is no such rule
     */
    public void delete(UUID id) {
        TriggerRuleEntity rule = load(id);
        repository.delete(rule);
        repository.flush();
        changed(id);
    }

    /**
     * Snoozes a rule until {@code until}: its matches are recorded as suppressed alerts
     * ({@code rule_snoozed}) and not routed.
     *
     * @throws NotFoundException when there is no such rule
     */
    public RuleView snooze(UUID id, Instant until) {
        TriggerRuleEntity rule = load(id);
        rule.setSnoozedUntil(until);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(id);
        return view(saved);
    }

    /**
     * Snoozes a rule as a {@code SnoozeInput} body asks (at most 7 days).
     *
     * @throws ai.foresignal.api.error.ValidationException when the body is not a valid snooze
     */
    public RuleView snooze(UUID id, JsonNode body) {
        load(id);
        return snooze(id, Snoozes.expiry(body, clock.instant(), Snoozes.MAX_RULE_SNOOZE));
    }

    /** Clears a rule's snooze; idempotent. */
    public RuleView unsnooze(UUID id) {
        TriggerRuleEntity rule = load(id);
        if (rule.getSnoozedUntil() == null) {
            return view(rule);
        }
        rule.setSnoozedUntil(null);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(id);
        return view(saved);
    }

    private RuleView view(TriggerRuleEntity rule) {
        AlertStore.RuleStats stats = alerts.stats(List.of(rule.getId()))
                .getOrDefault(rule.getId(), AlertStore.RuleStats.empty());
        return new RuleView(rule, stats);
    }

    private void changed(UUID id) {
        events.publishEvent(new TriggerRulesChangedEvent(id));
    }
}
