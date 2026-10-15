package ai.foresignal.backtest;

import java.util.List;
import java.util.Map;

/**
 * One caveat of a backtest (prompt 103): what the result cannot say. Every result carries
 * {@code NOT_GROUND_TRUTH} plus each applicable caveat.
 *
 * @param code a stable code
 * @param text a human explanation
 * @param data structured detail, or an empty map
 */
public record BacktestCaveat(String code, String text, Map<String, Object> data) {

    public static final String RANGE_CLAMPED_BY_RETENTION = "RANGE_CLAMPED_BY_RETENTION";
    public static final String DOWNSAMPLED_RANGE = "DOWNSAMPLED_RANGE";
    public static final String SAMPLED_SOURCE = "SAMPLED_SOURCE";
    public static final String COVERAGE_GAPS = "COVERAGE_GAPS";
    public static final String DEFINITION_DIFFERS = "DEFINITION_DIFFERS";
    public static final String ENTITY_RESOLUTION_CHANGED = "ENTITY_RESOLUTION_CHANGED";
    public static final String WINDOW_APPROXIMATE = "WINDOW_APPROXIMATE";
    public static final String SMALL_SAMPLE = "SMALL_SAMPLE";
    public static final String NOT_GROUND_TRUTH = "NOT_GROUND_TRUTH";

    /** Copies the data. */
    public BacktestCaveat {
        data = data == null ? Map.of() : Map.copyOf(data);
    }

    /** A caveat without data. */
    public static BacktestCaveat of(String code, String text) {
        return new BacktestCaveat(code, text, Map.of());
    }

    /** The stored jsonb form. */
    public Map<String, Object> toMap() {
        java.util.LinkedHashMap<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        if (!data.isEmpty()) {
            out.put("data", data);
        }
        return out;
    }

    /** The stored forms of a list. */
    public static List<Map<String, Object>> toMaps(List<BacktestCaveat> caveats) {
        return caveats.stream().map(BacktestCaveat::toMap).toList();
    }
}
