package ai.foresignal.trigger;

import java.time.Instant;
import java.util.UUID;

/**
 * A rule was snoozed (prompt 115: the implicit {@code SNOOZED} signal of its open alerts).
 *
 * @param ruleId the rule
 * @param until the snooze expiry
 * @param at when it was snoozed
 */
public record TriggerRuleSnoozedEvent(UUID ruleId, Instant until, Instant at) {}
