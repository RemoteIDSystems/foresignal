package ai.foresignal.trigger;

import java.time.Instant;
import java.util.UUID;

/**
 * A rule was muted (prompt 033): published after the mute is stored, so escalations of the rule's
 * open alerts stop.
 *
 * @param ruleId the rule
 * @param mutedUntil the mute expiry
 */
public record TriggerRuleMutedEvent(UUID ruleId, Instant mutedUntil) {}
