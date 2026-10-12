package ai.foresignal.trigger;

import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * When a rule's open alerts resolve on their own (prompt 017).
 *
 * @param onInverseEdge resolve when the entity stops satisfying the rule (the exit edge)
 * @param onTombstone resolve when upstream tombstones the entity
 * @param onExpiry resolve when the entity's live row expires or its head is gone
 * @param staleAfter resolve when the entity has not been observed for this long; null disables it
 */
public record AutoResolve(boolean onInverseEdge, boolean onTombstone, boolean onExpiry, Duration staleAfter) {

    /** Every trigger on, no staleness: the default of an {@code enter} rule. */
    public static final AutoResolve ALL = new AutoResolve(true, true, true, null);

    /** True when an inverse-edge or tombstone pass is needed. */
    public boolean needsEdgePass() {
        return onInverseEdge || onTombstone;
    }

    /** True when the periodic resolver has something to check. */
    public boolean needsPeriodicPass() {
        return onExpiry || staleAfter != null;
    }

    /** The stored form ({@code trigger_rule.auto_resolve}). */
    public Map<String, Object> toStorage() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("onInverseEdge", onInverseEdge);
        map.put("onTombstone", onTombstone);
        map.put("onExpiry", onExpiry);
        map.put("staleAfterSeconds", staleAfter == null ? null : staleAfter.toSeconds());
        return map;
    }

    /** The API form ({@code AutoResolve} schema). */
    public Map<String, Object> toApi() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("onInverseEdge", onInverseEdge);
        map.put("onTombstone", onTombstone);
        map.put("onExpiry", onExpiry);
        map.put("staleAfter", staleAfter == null ? null : Durations.format(staleAfter));
        return map;
    }

    /** Reads the stored form; null stays null (off). */
    public static AutoResolve fromStorage(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Object stale = map.get("staleAfterSeconds");
        return new AutoResolve(
                !Boolean.FALSE.equals(map.get("onInverseEdge")),
                !Boolean.FALSE.equals(map.get("onTombstone")),
                !Boolean.FALSE.equals(map.get("onExpiry")),
                stale instanceof Number n ? Duration.ofSeconds(n.longValue()) : null);
    }
}
