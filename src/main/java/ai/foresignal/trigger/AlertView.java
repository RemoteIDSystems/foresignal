package ai.foresignal.trigger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One alert as stored, with its rule's name (prompt 017). The API maps it to the {@code Alert}
 * schema; MCP tools (025) and notification channels (018) use it directly.
 *
 * @param id the alert id
 * @param ruleId the rule
 * @param ruleName the rule's current name, or null when the rule was deleted
 * @param seq the matching version
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the kind, or null for alerts older than 017
 * @param firedAt when it fired
 * @param observedAt the matching version's {@code observed_at}
 * @param message the rendered message
 * @param severity the severity wire value
 * @param state the state wire value
 * @param rev the revision
 * @param acknowledgedAt when acknowledged, or null
 * @param acknowledgedBy who acknowledged, or null
 * @param resolvedAt when resolved, or null
 * @param resolvedBy who or what resolved, or null
 * @param resolvedSeq the resolving version, or null
 * @param feedback the feedback wire value, or null
 * @param feedbackAt when feedback was given, or null
 * @param note the note, or null
 * @param snoozedUntil the alert snooze expiry, or null
 * @param suppressed true when recorded under a snooze
 * @param suppressedReason {@code alert_snoozed} or {@code rule_snoozed}, or null
 * @param tags the rule's tags at fire time
 * @param recordSnapshot the record snapshot as JSON text, or null
 * @param summary the enrichment summary (prompt 030), or null
 * @param enrichedAt when the summary was stored, or null
 * @param summaryCitations the summary's verified citations as a JSON array text, or null
 * @param sensitive true for an alert of a rule referencing a watchlist (prompt 036): hidden from
 *     anonymous callers and feed keys
 * @param context the computed alert context ({@code AlertContext}) as JSON text, or null (prompt 036:
 *     {@code near}, {@code anchor})
 * @param region the record's {@code RegionTags} at fire time as JSON text, or null (prompt 040)
 */
public record AlertView(
        long id,
        UUID ruleId,
        String ruleName,
        long seq,
        String sourceId,
        String externalId,
        String kind,
        Instant firedAt,
        Instant observedAt,
        String message,
        String severity,
        String state,
        long rev,
        Instant acknowledgedAt,
        String acknowledgedBy,
        Instant resolvedAt,
        String resolvedBy,
        Long resolvedSeq,
        String feedback,
        Instant feedbackAt,
        String note,
        Instant snoozedUntil,
        boolean suppressed,
        String suppressedReason,
        List<String> tags,
        String recordSnapshot,
        String summary,
        Instant enrichedAt,
        String summaryCitations,
        boolean sensitive,
        String context,
        String region) {

    /** An alert without region tags (the shape before prompt 040). */
    public AlertView(long id, UUID ruleId, String ruleName, long seq, String sourceId, String externalId, String kind,
            Instant firedAt, Instant observedAt, String message, String severity, String state, long rev,
            Instant acknowledgedAt, String acknowledgedBy, Instant resolvedAt, String resolvedBy, Long resolvedSeq,
            String feedback, Instant feedbackAt, String note, Instant snoozedUntil, boolean suppressed,
            String suppressedReason, List<String> tags, String recordSnapshot, String summary, Instant enrichedAt,
            String summaryCitations, boolean sensitive, String context) {
        this(id, ruleId, ruleName, seq, sourceId, externalId, kind, firedAt, observedAt, message, severity, state, rev,
                acknowledgedAt, acknowledgedBy, resolvedAt, resolvedBy, resolvedSeq, feedback, feedbackAt, note,
                snoozedUntil, suppressed, suppressedReason, tags, recordSnapshot, summary, enrichedAt, summaryCitations,
                sensitive, context, null);
    }

    /** Copies the tags. */
    public AlertView {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    /** An alert without the prompt 036 members: not sensitive, no context. */
    public AlertView(long id, UUID ruleId, String ruleName, long seq, String sourceId, String externalId, String kind,
            Instant firedAt, Instant observedAt, String message, String severity, String state, long rev,
            Instant acknowledgedAt, String acknowledgedBy, Instant resolvedAt, String resolvedBy, Long resolvedSeq,
            String feedback, Instant feedbackAt, String note, Instant snoozedUntil, boolean suppressed,
            String suppressedReason, List<String> tags, String recordSnapshot, String summary, Instant enrichedAt,
            String summaryCitations) {
        this(id, ruleId, ruleName, seq, sourceId, externalId, kind, firedAt, observedAt, message, severity, state, rev,
                acknowledgedAt, acknowledgedBy, resolvedAt, resolvedBy, resolvedSeq, feedback, feedbackAt, note,
                snoozedUntil, suppressed, suppressedReason, tags, recordSnapshot, summary, enrichedAt, summaryCitations,
                false, null);
    }

    /** An alert that has not been enriched (no summary, no citations). */
    public AlertView(long id, UUID ruleId, String ruleName, long seq, String sourceId, String externalId, String kind,
            Instant firedAt, Instant observedAt, String message, String severity, String state, long rev,
            Instant acknowledgedAt, String acknowledgedBy, Instant resolvedAt, String resolvedBy, Long resolvedSeq,
            String feedback, Instant feedbackAt, String note, Instant snoozedUntil, boolean suppressed,
            String suppressedReason, List<String> tags, String recordSnapshot) {
        this(id, ruleId, ruleName, seq, sourceId, externalId, kind, firedAt, observedAt, message, severity, state, rev,
                acknowledgedAt, acknowledgedBy, resolvedAt, resolvedBy, resolvedSeq, feedback, feedbackAt, note,
                snoozedUntil, suppressed, suppressedReason, tags, recordSnapshot, null, null, null);
    }

    /** True when an enrichment summary is stored. */
    public boolean enriched() {
        return summary != null;
    }

    /** True when the alert is not resolved. */
    public boolean open() {
        return !AlertState.RESOLVED.wire().equals(state);
    }

    /** The severity as an enum. */
    public Severity severityLevel() {
        return Severity.fromWire(severity);
    }
}
