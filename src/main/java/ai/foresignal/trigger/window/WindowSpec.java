package ai.foresignal.trigger.window;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A windowed rule's specification (prompt 100), the union of the {@code CountWindow},
 * {@code AbsenceWindow} and {@code AnomalyWindow} schemas. Which fields apply is decided by the
 * rule's {@link RuleType}.
 *
 * @param window the window length, 1 minute to 7 days
 * @param step the evaluation period, at least 30 seconds
 * @param distinctBy {@code entity} (default), {@code externalId} or a payload path
 * @param groupBy null, {@code source}, {@code place}, {@code h3:&lt;res 3..8&gt;}, {@code admin1},
 *     {@code country} or a payload path
 * @param maxGroups the most groups evaluated per rule
 * @param op the count comparison ({@code >=} or {@code <=})
 * @param threshold the count threshold
 * @param bucket the anomaly bucket length ({@code 15m} or {@code 1h})
 * @param slotMode {@code hour-of-week} or {@code hour-of-day}
 * @param direction {@code up}, {@code down} or {@code both}
 * @param sensitivity {@code low}, {@code medium} or {@code high} (α 1e-4 / 1e-3 / 1e-2)
 * @param minAbsoluteDelta the smallest absolute deviation that may fire
 * @param minWeeks the warm-up weeks (8 to 12)
 * @param coverageMin the minimum coverage share for a bucket to count
 */
public record WindowSpec(
        Duration window,
        Duration step,
        String distinctBy,
        String groupBy,
        int maxGroups,
        String op,
        Integer threshold,
        Duration bucket,
        String slotMode,
        String direction,
        String sensitivity,
        double minAbsoluteDelta,
        int minWeeks,
        double coverageMin) {

    /** The default group cap. */
    public static final int DEFAULT_MAX_GROUPS = 500;

    /** The smallest step. */
    public static final Duration MIN_STEP = Duration.ofSeconds(30);

    /** The largest window. */
    public static final Duration MAX_WINDOW = Duration.ofDays(7);

    /** Fills the defaults and checks nothing (the validator checks). */
    public WindowSpec {
        if (step == null) {
            step = defaultStep(window);
        }
        distinctBy = distinctBy == null || distinctBy.isBlank() ? "entity" : distinctBy;
        maxGroups = maxGroups <= 0 ? DEFAULT_MAX_GROUPS : maxGroups;
        bucket = bucket == null ? Duration.ofHours(1) : bucket;
        slotMode = slotMode == null || slotMode.isBlank() ? "hour-of-week" : slotMode;
        direction = direction == null || direction.isBlank() ? "up" : direction;
        sensitivity = sensitivity == null || sensitivity.isBlank() ? "medium" : sensitivity;
        minWeeks = minWeeks <= 0 ? 8 : minWeeks;
        coverageMin = coverageMin <= 0 ? 0.8 : coverageMin;
    }

    /** The default step: {@code min(window/4, 5m)}, at least 30 s. */
    public static Duration defaultStep(Duration window) {
        if (window == null) {
            return Duration.ofMinutes(1);
        }
        Duration quarter = window.dividedBy(4);
        Duration step = quarter.compareTo(Duration.ofMinutes(5)) < 0 ? quarter : Duration.ofMinutes(5);
        return step.compareTo(MIN_STEP) < 0 ? MIN_STEP : step;
    }

    /** The α the sensitivity maps to. */
    public double alpha() {
        return switch (sensitivity) {
            case "low" -> 1e-4;
            case "high" -> 1e-2;
            default -> 1e-3;
        };
    }

    /** The stored jsonb form. */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("window", window.toString());
        map.put("step", step.toString());
        map.put("distinctBy", distinctBy);
        if (groupBy != null) {
            map.put("groupBy", groupBy);
        }
        map.put("maxGroups", maxGroups);
        if (op != null) {
            map.put("op", op);
        }
        if (threshold != null) {
            map.put("threshold", threshold);
        }
        map.put("bucket", bucket.toString());
        map.put("slotMode", slotMode);
        map.put("direction", direction);
        map.put("sensitivity", sensitivity);
        map.put("minAbsoluteDelta", minAbsoluteDelta);
        map.put("minWeeks", minWeeks);
        map.put("coverageMin", coverageMin);
        return map;
    }

    /** Parses the stored jsonb form, already validated. */
    public static WindowSpec fromStored(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return new WindowSpec(
                duration(map.get("window")),
                duration(map.get("step")),
                string(map.get("distinctBy")),
                string(map.get("groupBy")),
                integer(map.get("maxGroups"), DEFAULT_MAX_GROUPS),
                string(map.get("op")),
                map.get("threshold") == null ? null : integer(map.get("threshold"), 0),
                duration(map.get("bucket")),
                string(map.get("slotMode")),
                string(map.get("direction")),
                string(map.get("sensitivity")),
                number(map.get("minAbsoluteDelta"), 0),
                integer(map.get("minWeeks"), 0),
                number(map.get("coverageMin"), 0));
    }

    private static Duration duration(Object value) {
        return value == null ? null : Duration.parse(String.valueOf(value));
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int integer(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double number(Object value, double fallback) {
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
