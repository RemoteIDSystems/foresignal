package ai.foresignal.briefing;

import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The alert that fires a breaking-news edition (prompt NP-006), stored on the run as {@code BriefingRun.breaking}.
 *
 * @param alertId the alert
 * @param ruleId its trigger rule
 * @param ruleName the rule's name
 * @param severity the alert's severity wire value
 * @param firedAt when it fired
 * @param message the alert's message, or null
 */
public record BreakingTrigger(long alertId, UUID ruleId, String ruleName, String severity, Instant firedAt,
        String message) {

    /** The trigger of {@code alert}. */
    public static BreakingTrigger of(AlertView alert) {
        return new BreakingTrigger(alert.id(), alert.ruleId(), alert.ruleName(), alert.severity(), alert.firedAt(),
                alert.message());
    }

    /** The stored and wire form, {@code {alertId, ruleId, ruleName, severity, firedAt, message}}. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alertId", alertId);
        out.put("ruleId", ruleId == null ? null : ruleId.toString());
        out.put("ruleName", ruleName);
        out.put("severity", severity);
        out.put("firedAt", firedAt == null ? null : firedAt.toString());
        out.put("message", message);
        return out;
    }

    /** The lead line of a breaking edition: {@code **Breaking:** <rule>: <message>}. */
    public String lead() {
        String what = message == null || message.isBlank() ? "" : ": " + message.strip();
        return "**Breaking:** " + (ruleName == null ? "alert " + alertId : ruleName) + what;
    }
}
