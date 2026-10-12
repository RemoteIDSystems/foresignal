package ai.foresignal.trigger;

import java.time.Instant;
import java.util.UUID;

/**
 * Published once per storm window when a rule's storm cap starts dropping matches (prompt 017).
 * Prompt 018 routes it as a system event.
 *
 * @param ruleId the rule
 * @param ruleName the rule's name
 * @param at when the cap was hit
 * @param maxAlertsPerHour the effective cap
 * @param suppressedCount the rule's storm-suppressed total after this match
 * @param disabled true when {@code autoDisableOnStorm} disabled the rule
 */
public record RuleStormEvent(
        UUID ruleId, String ruleName, Instant at, int maxAlertsPerHour, long suppressedCount, boolean disabled) {}
