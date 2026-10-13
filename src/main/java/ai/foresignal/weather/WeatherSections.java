package ai.foresignal.weather;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the sections of a weather context or digest (prompt 056; Weather-Imagery-Design §7.1): each is
 * {@code {status, asOf?, statusText?, …}}. A non-ok section carries {@code statusText} and no metric values, so
 * unknown never reads as clear; {@code asOf} is the time of the last good data when one is known.
 */
public final class WeatherSections {

    /** {@code ok}. */
    public static final String OK = "ok";

    /** {@code stale}. */
    public static final String STALE = "stale";

    /** {@code no-coverage}. */
    public static final String NO_COVERAGE = "no-coverage";

    /** {@code unavailable}. */
    public static final String UNAVAILABLE = "unavailable";

    /** The default text of {@code stale}. */
    public static final String STALE_TEXT = "input is older than its maximum gap; this is not evidence of clear weather";

    /** The default text of {@code no-coverage}. */
    public static final String NO_COVERAGE_TEXT = "outside product coverage; this is not evidence of clear weather";

    /** The default text of {@code unavailable}. */
    public static final String UNAVAILABLE_TEXT =
            "input is unavailable (disabled, failing or no data yet); this is not evidence of clear weather";

    private WeatherSections() {}

    /** An ok section with {@code metrics} (null values are left out). */
    public static Map<String, Object> ok(Map<String, Object> metrics) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", OK);
        if (metrics != null) {
            metrics.forEach((k, v) -> {
                if (v != null) {
                    m.put(k, v);
                }
            });
        }
        return m;
    }

    /**
     * A non-ok section.
     *
     * @param status {@link #STALE}, {@link #NO_COVERAGE} or {@link #UNAVAILABLE}
     * @param text the explanation, or null for the status's default text
     * @param asOf the time of the last good data, or null when unknown
     */
    public static Map<String, Object> notOk(String status, String text, Instant asOf) {
        if (OK.equals(status)) {
            throw new IllegalArgumentException("notOk needs a non-ok status");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        if (asOf != null) {
            m.put("asOf", asOf.toString());
        }
        m.put("statusText", text == null || text.isBlank() ? defaultText(status) : text);
        return m;
    }

    /** {@link #notOk} with the default text and no {@code asOf}. */
    public static Map<String, Object> unavailable(String text) {
        return notOk(UNAVAILABLE, text, null);
    }

    /** The default explanation of a non-ok status. */
    public static String defaultText(String status) {
        return switch (status) {
            case STALE -> STALE_TEXT;
            case NO_COVERAGE -> NO_COVERAGE_TEXT;
            default -> UNAVAILABLE_TEXT;
        };
    }

    /** The status of a section map ({@code unavailable} when absent). */
    public static String status(Object section) {
        if (section instanceof Map<?, ?> m && m.get("status") != null) {
            return m.get("status").toString();
        }
        return UNAVAILABLE;
    }

    /** True when the section map is {@code ok}. */
    public static boolean isOk(Object section) {
        return OK.equals(status(section));
    }
}
