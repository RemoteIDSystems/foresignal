package ai.foresignal.intel;

import java.util.Map;

/**
 * One intelligence-event stream event (prompt 101) on the {@code intel} SSE topic:
 * {@code intel.event.created|updated|resolved|merged}. {@code cursor} is the {@code
 * intelligence_event_log} id, strictly increasing.
 *
 * @param cursor the topic cursor
 * @param eventId the event id
 * @param change created | updated | resolved | merged
 * @param version the event's version (for upsert)
 * @param summary the compact event summary
 * @param mergedInto the surviving event id on a merge, or null
 */
public record IntelEvent(long cursor, String eventId, String change, int version, Map<String, Object> summary,
        String mergedInto) {

    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String RESOLVED = "resolved";
    public static final String MERGED = "merged";

    /** The SSE event name. */
    public String sseEvent() {
        return "intel.event." + change;
    }
}
