package ai.foresignal.trigger;

import java.util.UUID;

/**
 * Published after trigger rules change (created, replaced, deleted, snoozed, disabled by a storm or
 * by {@code activeUntil}). The persist gate and the engine rebuild their rule snapshots on it.
 *
 * @param ruleId the rule that changed, or null when several may have
 */
public record TriggerRulesChangedEvent(UUID ruleId) {}
