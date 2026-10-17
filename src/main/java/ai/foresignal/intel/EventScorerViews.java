package ai.foresignal.intel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The scorer's member views as manifests record them, and back (prompt 116), so a recorded score can be
 * re-computed exactly.
 */
public final class EventScorerViews {

    private EventScorerViews() {}

    /** A member view as a map. */
    public static Map<String, Object> toMap(EventScorer.MemberView view) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("signalFamily", view.signalFamily());
        out.put("weight", view.weight());
        out.put("sourceId", view.sourceId());
        out.put("sourceHealthy", view.sourceHealthy());
        out.put("reliability", view.reliability());
        out.put("evidenceGroup", view.evidenceGroup());
        out.put("originatorRoot", view.originatorRoot());
        return out;
    }

    /** Member views from their recorded maps. */
    public static List<EventScorer.MemberView> fromMaps(List<?> maps) {
        return maps.stream().filter(Map.class::isInstance).map(Map.class::cast).map(m -> new EventScorer.MemberView(
                m.get("signalFamily") == null ? null : String.valueOf(m.get("signalFamily")),
                m.get("weight") instanceof Number n ? n.doubleValue() : 0.0,
                m.get("sourceId") == null ? null : String.valueOf(m.get("sourceId")),
                Boolean.TRUE.equals(m.get("sourceHealthy")),
                m.get("reliability") instanceof Number r ? r.doubleValue() : null,
                m.get("evidenceGroup") == null ? null : String.valueOf(m.get("evidenceGroup")),
                m.get("originatorRoot") == null ? null : String.valueOf(m.get("originatorRoot")))).toList();
    }
}
