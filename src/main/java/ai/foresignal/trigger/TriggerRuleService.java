package ai.foresignal.trigger;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.EscalationPolicyRepository;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Trigger rule management (prompt 017): create, replace, delete, list, snooze. Every change
 * publishes {@link TriggerRulesChangedEvent}, so the persist gate and the engine pick it up at once.
 *
 * <p>Prompt 033 adds {@code escalationPolicyId} (validated against {@code escalation_policy} on every
 * save; a settings import drops an unknown one with a warning, since policies are not exported) and
 * the rule mute ({@link #mute}, {@link #unmute}).
 *
 * <p>Takes and returns plain values, so prompt 025 can expose {@code snooze_rule} and
 * {@code test_trigger} as MCP tools without web types.
 */
@Service
public class TriggerRuleService {

    private static final Logger log = LoggerFactory.getLogger(TriggerRuleService.class);

    private final TriggerRuleRepository repository;
    private final RuleValidator validator;
    private final AlertStore alerts;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final EscalationPolicyRepository policies;

    /** Creates the service. */
    public TriggerRuleService(
            TriggerRuleRepository repository,
            RuleValidator validator,
            AlertStore alerts,
            ApplicationEventPublisher events,
            Clock clock,
            EscalationPolicyRepository policies) {
        this.policies = policies;
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
        UUID policy = escalationPolicy(input, false);
        Instant now = clock.instant();
        TriggerRuleEntity rule = new TriggerRuleEntity(UUID.randomUUID(), valid.name());
        valid.applyTo(rule);
        applyWindow(rule, input);
        rule.setEscalationPolicyId(policy);
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
            return replace(id, input, true);
        }
        ValidatedRule valid = validator.validate(input);
        UUID policy = escalationPolicy(input, true);
        Instant now = clock.instant();
        TriggerRuleEntity rule = new TriggerRuleEntity(id, valid.name());
        valid.applyTo(rule);
        applyWindow(rule, input);
        rule.setEscalationPolicyId(policy);
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
        return replace(id, input, false);
    }

    private RuleView replace(UUID id, JsonNode input, boolean importing) {
        TriggerRuleEntity rule = load(id);
        ValidatedRule valid = validator.validate(input);
        UUID policy = escalationPolicy(input, importing);
        valid.applyTo(rule);
        applyWindow(rule, input);
        rule.setEscalationPolicyId(policy);
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

    /**
     * Mutes a rule until {@code until} (prompt 033): its alerts are still recorded but not routed,
     * and escalations of its open alerts stop ({@link TriggerRuleMutedEvent}).
     *
     * @throws NotFoundException when there is no such rule
     */
    public RuleView mute(UUID id, Instant until) {
        TriggerRuleEntity rule = load(id);
        rule.setMutedUntil(until);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(id);
        events.publishEvent(new TriggerRuleMutedEvent(id, until));
        return view(saved);
    }

    /**
     * Mutes a rule as a {@code TriggerMuteInput} body asks (1 h to 7 d).
     *
     * @throws ValidationException when the body is not a valid mute
     */
    public RuleView mute(UUID id, JsonNode body) {
        load(id);
        return mute(id, Mutes.expiry(body, clock.instant()));
    }

    /** Clears a rule's mute; idempotent. */
    public RuleView unmute(UUID id) {
        TriggerRuleEntity rule = load(id);
        if (rule.getMutedUntil() == null) {
            return view(rule);
        }
        rule.setMutedUntil(null);
        TriggerRuleEntity saved = repository.saveAndFlush(rule);
        changed(id);
        return view(saved);
    }

    /**
     * The {@code escalationPolicyId} of a rule input: null when absent or null; otherwise it must name
     * an existing policy ({@code 400}), except on a settings import, which drops an unknown one.
     */
    UUID escalationPolicy(JsonNode input, boolean importing) {
        JsonNode node = input == null ? null : input.get("escalationPolicyId");
        if (node == null || node.isNull()) {
            return null;
        }
        UUID id;
        try {
            id = UUID.fromString(node.asString());
        } catch (RuntimeException e) {
            throw ValidationException.forField("escalationPolicyId", "must be an escalation policy id (UUID)");
        }
        if (!policies.existsById(id)) {
            if (importing) {
                log.warn("Imported trigger rule references unknown escalation policy {}; the reference was dropped", id);
                return null;
            }
            throw ValidationException.forField("escalationPolicyId", "names no escalation policy");
        }
        return id;
    }

    private RuleView view(TriggerRuleEntity rule) {
        AlertStore.RuleStats stats = alerts.stats(List.of(rule.getId()))
                .getOrDefault(rule.getId(), AlertStore.RuleStats.empty());
        return new RuleView(rule, stats);
    }

    private void changed(UUID id) {
        events.publishEvent(new TriggerRulesChangedEvent(id));
    }

    /**
     * Validates and stores the rule's {@code type} and {@code window} (prompt 100). A {@code record}
     * rule stores {@code type=record} and no window.
     *
     * @throws ai.foresignal.api.error.ValidationException {@code WINDOW_RULE_INVALID}
     */
    private void applyWindow(TriggerRuleEntity rule, JsonNode input) {
        ai.foresignal.trigger.window.RuleType type =
                ai.foresignal.trigger.window.WindowSpecValidator.type(input);
        if (type == ai.foresignal.trigger.window.RuleType.RECORD) {
            rule.setType("record");
            rule.setWindowSpec(null);
            return;
        }
        if (rule.getConditions().stream().anyMatch(c -> ApproachCondition.OP.equals(c.op()))) {
            // Prompt 114: approach conditions are per-version edges of record rules only.
            throw ValidationException.forField("conditions", "approach conditions apply only to record rules");
        }
        ai.foresignal.trigger.window.WindowSpec window =
                ai.foresignal.trigger.window.WindowSpecValidator.validate(type, input);
        rule.setType(type.wire());
        rule.setWindowSpec(window.toMap());
    }
}
