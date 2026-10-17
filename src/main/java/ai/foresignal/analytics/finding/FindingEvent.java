package ai.foresignal.analytics.finding;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One stream event on the {@code analytics} SSE topic: {@code finding.created}, {@code finding.updated} or
 * {@code finding.dismissed} (prompt 119), or {@code precursor.validated}, {@code precursor.stale} or
 * {@code precursor.retired} (prompt 120); {@code cursor} is the {@code analytic_finding_log} id.
 *
 * @param cursor the topic cursor
 * @param event the event name
 * @param findingId the finding, or the precursor of a {@code precursor.*} event
 * @param finding the finding summary of the {@code FindingEvent} schema, or the precursor summary of the
 *     {@code PrecursorEvent} schema
 * @param at when it happened
 */
public record FindingEvent(long cursor, String event, String findingId, Map<String, Object> finding, Instant at) {

    /** A finding was created. */
    public static final String CREATED = "finding.created";

    /** A finding changed (superseded, linked). */
    public static final String UPDATED = "finding.updated";

    /** A finding was dismissed. */
    public static final String DISMISSED = "finding.dismissed";

    /** A precursor became validated (prompt 120). */
    public static final String PRECURSOR_VALIDATED = "precursor.validated";

    /** A precursor became stale (prompt 120). */
    public static final String PRECURSOR_STALE = "precursor.stale";

    /** A precursor was retired (prompt 120). */
    public static final String PRECURSOR_RETIRED = "precursor.retired";

    /** True for a {@code precursor.*} event. */
    public boolean precursorEvent() {
        return event != null && event.startsWith("precursor.");
    }

    /** The {@code FindingEvent} payload, or the {@code PrecursorEvent} payload of a {@code precursor.*} event. */
    public Map<String, Object> payload() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put(precursorEvent() ? "precursor" : "finding", finding);
        out.put("at", at == null ? null : at.toString());
        return out;
    }

    /** True when the finding is private. */
    public boolean privateFinding() {
        return finding != null && Boolean.TRUE.equals(finding.get("private"));
    }
}
