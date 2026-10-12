package ai.foresignal.api.support;

import java.util.List;

/**
 * The time axis that {@code since}/{@code until} filter on (Core-Data-Model §12).
 *
 * <ul>
 *   <li>{@link #EFFECTIVE}: {@code COALESCE(event_time, observed_at)}, the default for records;
 *   <li>{@link #OBSERVED}: {@code observed_at}, the default for history;
 *   <li>{@link #EVENT}: {@code event_time} (records without one never match);
 *   <li>{@link #INGESTED}: transaction time, translated to seq bounds through {@code seq_clock}.
 * </ul>
 */
public enum TimeField {
    EFFECTIVE,
    OBSERVED,
    EVENT,
    INGESTED;

    /** The wire values, in declaration order. */
    public static final List<String> WIRE_VALUES = List.of("effective", "observed", "event", "ingested");

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Parses a wire value; {@code defaultValue} when absent. */
    public static TimeField parse(String raw, TimeField defaultValue) {
        String value = QueryParams.oneOf("timeField", raw, WIRE_VALUES, defaultValue.wireValue());
        return valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
}
