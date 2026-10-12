package ai.foresignal.trigger;

import java.util.List;
import java.util.Locale;

/** Alert severity, which drives notification priority (FR-28). Ordered from least to most severe. */
public enum Severity {
    /** Informational. */
    INFO,
    /** The default. */
    WARNING,
    /** Needs attention now. */
    CRITICAL;

    /** The wire values, in order. */
    public static final List<String> WIRE_VALUES = List.of("info", "warning", "critical");

    /** The lowercase wire value. */
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a wire value.
     *
     * @throws IllegalArgumentException when it is not {@code info}, {@code warning} or {@code critical}
     */
    public static Severity fromWire(String value) {
        if (value == null) {
            throw new IllegalArgumentException("severity is required");
        }
        return switch (value) {
            case "info" -> INFO;
            case "warning" -> WARNING;
            case "critical" -> CRITICAL;
            default -> throw new IllegalArgumentException("must be one of info, warning, critical");
        };
    }

    /** The wire values at or above this severity. */
    public List<String> atOrAbove() {
        return WIRE_VALUES.subList(ordinal(), WIRE_VALUES.size());
    }
}
