package ai.foresignal.weather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The template variables of an alert's {@code context.weather} (prompt 056): {@code {{context.weather.summary}}}
 * and every scalar field of the sections by its dot path, for example {@code {{context.weather.radar.maxDbz}}},
 * {@code {{context.weather.radar.nearestCell.motion.speedKmh}}}, {@code {{context.weather.lightning.status}}},
 * {@code {{context.weather.wind.gustMs}}} or {@code {{context.weather.warnings.items.0.event}}}.
 *
 * <p>Numbers render canonically (no trailing {@code .0}), booleans as {@code true}/{@code false}; objects and
 * arrays are not variables themselves, only their scalar leaves. A missing context, section or field renders
 * empty (the template engine's rule for unknown placeholders).
 */
public final class WeatherTemplateVars {

    /** The prefix of every variable. */
    public static final String PREFIX = "context.weather.";

    /** The summary variable. */
    public static final String SUMMARY = PREFIX + "summary";

    /** Deeper paths are not flattened. */
    static final int MAX_DEPTH = 6;

    private WeatherTemplateVars() {}

    /**
     * The variables of a weather context.
     *
     * @param weather the {@code context.weather} map, or null
     * @return the variables by name (empty for null)
     */
    public static Map<String, String> of(Map<?, ?> weather) {
        Map<String, String> out = new HashMap<>();
        if (weather != null) {
            flatten(PREFIX, weather, out, 0);
        }
        return out;
    }

    /**
     * The variables of an alert's whole context ({@code AlertContext}): those of its {@code weather} member.
     *
     * @param context the context map, or null
     */
    public static Map<String, String> fromAlertContext(Object context) {
        if (context instanceof Map<?, ?> map && map.get("weather") instanceof Map<?, ?> weather) {
            return of(weather);
        }
        return new HashMap<>();
    }

    private static void flatten(String prefix, Object value, Map<String, String> out, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null) {
                    flatten(prefix + e.getKey() + ".", e.getValue(), out, depth + 1);
                }
            }
        } else if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flatten(prefix + i + ".", list.get(i), out, depth + 1);
            }
        } else if (value != null) {
            out.put(prefix.substring(0, prefix.length() - 1), scalar(value));
        }
    }

    /** A scalar as template text. */
    static String scalar(Object value) {
        if (value instanceof Double d && d == Math.rint(d) && !d.isInfinite()) {
            return Long.toString(d.longValue());
        }
        if (value instanceof Float f && f == Math.rint(f) && !f.isInfinite()) {
            return Long.toString(f.longValue());
        }
        if (value instanceof java.math.BigDecimal b) {
            return b.stripTrailingZeros().toPlainString();
        }
        return value.toString();
    }
}
