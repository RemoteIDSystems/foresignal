package ai.foresignal.trigger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The trigger condition {@code approach} (prompt 114, OpenAPI {@code ApproachCondition}): a version matches
 * when its record's closest point of approach to the target is within {@code withinMeters} no later than
 * {@code withinMinutes} from the version's time, under constant velocity.
 *
 * <p>Stored in the rule's {@code conditions} array as {@code {path: null, op: "approach", value: {target,
 * withinMeters, withinMinutes}}} ({@link #OP}). It is evaluated in Java, never in SQL: by the trigger engine on
 * each candidate version of the rule's sources and by the persist gate as a non-SQL predicate
 * ({@code ai.foresignal.kinematics.ApproachEvaluator}). Edge resolution is the poll or flush resolution of the
 * watched sources.
 *
 * @param placeId the target place, or null
 * @param point the target point {@code {lon, lat}}, or null
 * @param record the target record {@code {sourceId}/{externalId}}, or null
 * @param withinMeters the largest closest-approach distance that matches
 * @param withinMinutes the latest closest-approach time that matches, minutes from the version's time
 */
public record ApproachCondition(String placeId, double[] point, String record, double withinMeters,
        double withinMinutes) {

    /** The operator name a stored approach condition carries. */
    public static final String OP = "approach";

    /** The condition {@code type} on the wire. */
    public static final String TYPE = "approach";

    /** Requires exactly one target. */
    public ApproachCondition {
        int targets = (placeId != null ? 1 : 0) + (point != null ? 1 : 0) + (record != null ? 1 : 0);
        if (targets != 1) {
            throw new IllegalArgumentException("an approach target needs exactly one of placeId, point or record");
        }
        point = point == null ? null : point.clone();
    }

    /** The horizon in seconds. */
    public double horizonSeconds() {
        return withinMinutes * 60.0;
    }

    /** The stored {@code value}: {@code {target, withinMeters, withinMinutes}}. */
    public Map<String, Object> toValue() {
        Map<String, Object> target = new LinkedHashMap<>();
        if (placeId != null) {
            target.put("placeId", placeId);
        } else if (point != null) {
            target.put("point", Map.of("type", "Point", "coordinates", List.of(point[0], point[1])));
        } else {
            target.put("record", record);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("target", target);
        out.put("withinMeters", withinMeters);
        out.put("withinMinutes", withinMinutes);
        return out;
    }

    /** The wire form, the stored value with {@code type: approach}. */
    public Map<String, Object> toWire() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", TYPE);
        out.putAll(toValue());
        return out;
    }

    /**
     * Reads a stored value.
     *
     * @throws IllegalArgumentException when it is not an approach value
     */
    @SuppressWarnings("unchecked")
    public static ApproachCondition fromValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("an approach condition needs an object value");
        }
        Object targetValue = map.get("target");
        if (!(targetValue instanceof Map<?, ?> target)) {
            throw new IllegalArgumentException("an approach condition needs a target");
        }
        String placeId = target.get("placeId") instanceof String s ? s : null;
        String record = target.get("record") instanceof String s ? s : null;
        double[] point = null;
        if (target.get("point") instanceof Map<?, ?> p && p.get("coordinates") instanceof List<?> c && c.size() >= 2
                && c.get(0) instanceof Number lon && c.get(1) instanceof Number lat) {
            point = new double[] {lon.doubleValue(), lat.doubleValue()};
        }
        return new ApproachCondition(placeId, point, record, number(map.get("withinMeters")),
                number(map.get("withinMinutes")));
    }

    private static double number(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        throw new IllegalArgumentException("withinMeters and withinMinutes must be numbers");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ApproachCondition other && Objects.equals(placeId, other.placeId)
                && java.util.Arrays.equals(point, other.point) && Objects.equals(record, other.record)
                && withinMeters == other.withinMeters && withinMinutes == other.withinMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, java.util.Arrays.hashCode(point), record, withinMeters, withinMinutes);
    }

    @Override
    public String toString() {
        return "approach" + toValue();
    }
}
