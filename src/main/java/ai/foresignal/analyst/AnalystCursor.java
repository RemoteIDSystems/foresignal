package ai.foresignal.analyst;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The analyst profile's durable cursor (prompt 102): how far each input type has been consumed.
 *
 * @param seq the highest record seq seen
 * @param alertId the highest alert id seen
 * @param eventUpdatedAt the newest intelligence-event update time seen
 * @param anomalyId the highest anomaly-observation id seen
 */
public record AnalystCursor(Long seq, Long alertId, Instant eventUpdatedAt, Long anomalyId) {

    /** The empty cursor. */
    public static AnalystCursor empty() {
        return new AnalystCursor(null, null, null, null);
    }

    /** From the stored jsonb form. */
    public static AnalystCursor fromMap(Map<String, Object> map) {
        if (map == null) {
            return empty();
        }
        return new AnalystCursor(longOf(map.get("seq")), longOf(map.get("alertId")),
                map.get("eventUpdatedAt") == null ? null : Instant.parse(String.valueOf(map.get("eventUpdatedAt"))),
                longOf(map.get("anomalyId")));
    }

    /** The stored jsonb form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("seq", seq);
        out.put("alertId", alertId);
        out.put("eventUpdatedAt", eventUpdatedAt == null ? null : eventUpdatedAt.toString());
        out.put("anomalyId", anomalyId);
        return out;
    }

    private static Long longOf(Object raw) {
        return raw instanceof Number number ? number.longValue() : null;
    }
}
