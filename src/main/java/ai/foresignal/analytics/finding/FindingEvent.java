package ai.foresignal.analytics.finding;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One stream event on the {@code analytics} SSE topic: {@code finding.created}, {@code finding.updated} or
 * {@code finding.dismissed} (prompt 119), or {@code precursor.validated}, {@code precursor.stale} or
 * {@code precursor.retired} (prompt 120), or {@code outlook.published}, {@code index.changed} or
 * {@code index.weights_proposed} (prompt 126), or {@code impact.projected} or {@code impact.superseded} (prompt 127),
 * whose payload is the summary itself; {@code cursor} is the
 * {@code analytic_finding_log} id.
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

    /** A regional outlook was published (prompt 126). */
    public static final String OUTLOOK_PUBLISHED = "outlook.published";

    /** A regional index moved by at least 5 points (prompt 126). */
    public static final String INDEX_CHANGED = "index.changed";

    /** Fitted index weights were proposed (prompt 126). */
    public static final String INDEX_WEIGHTS_PROPOSED = "index.weights_proposed";

    /** A projected impact was recorded (prompt 127). */
    public static final String IMPACT_PROJECTED = "impact.projected";

    /** A projected impact was superseded by the next run of its hazard (prompt 127). */
    public static final String IMPACT_SUPERSEDED = "impact.superseded";

    /**
     * True for an {@code outlook.*} or {@code index.*} event (prompt 126) or an {@code impact.*} event (prompt 127), whose
     * payload is its summary itself.
     */
    public static boolean flat(String event) {
        return event != null && (event.startsWith("outlook.") || event.startsWith("index.")
                || event.startsWith("impact."));
    }

    /** True for a {@code precursor.*} event. */
    public boolean precursorEvent() {
        return event != null && event.startsWith("precursor.");
    }

    /** The {@code FindingEvent} payload, or the {@code PrecursorEvent} payload of a {@code precursor.*} event. */
    public Map<String, Object> payload() {
        if (flat(event)) {
            return finding == null ? new LinkedHashMap<>() : new LinkedHashMap<>(finding);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put(precursorEvent() ? "precursor" : "finding", finding);
        out.put("at", at == null ? null : at.toString());
        return out;
    }

    /** True when the finding is private (outlook, index and impact events never are). */
    public boolean privateFinding() {
        return !flat(event) && finding != null && Boolean.TRUE.equals(finding.get("private"));
    }
}
