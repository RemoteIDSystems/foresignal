package ai.foresignal.analytics.finding;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One finding stream event (prompt 119) on the {@code analytics} SSE topic: {@code finding.created},
 * {@code finding.updated} or {@code finding.dismissed}; {@code cursor} is the {@code analytic_finding_log} id.
 *
 * @param cursor the topic cursor
 * @param event the event name
 * @param findingId the finding
 * @param finding the finding summary of the {@code FindingEvent} schema
 * @param at when it happened
 */
public record FindingEvent(long cursor, String event, String findingId, Map<String, Object> finding, Instant at) {

    /** A finding was created. */
    public static final String CREATED = "finding.created";

    /** A finding changed (superseded, linked). */
    public static final String UPDATED = "finding.updated";

    /** A finding was dismissed. */
    public static final String DISMISSED = "finding.dismissed";

    /** The {@code FindingEvent} payload. */
    public Map<String, Object> payload() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put("finding", finding);
        out.put("at", at == null ? null : at.toString());
        return out;
    }

    /** True when the finding is private. */
    public boolean privateFinding() {
        return finding != null && Boolean.TRUE.equals(finding.get("private"));
    }
}
