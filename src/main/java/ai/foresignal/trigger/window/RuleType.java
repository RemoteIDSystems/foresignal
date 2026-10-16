package ai.foresignal.trigger.window;

import java.util.Locale;

/** A trigger rule's type (prompt 100): the 017 {@code record} engine, or a windowed type. */
public enum RuleType {

    /** The single-version edge engine of prompt 017. */
    RECORD("record"),
    /** N distinct entities in a window. */
    COUNT("count"),
    /** Nothing matching for a window while the sources are covered. */
    ABSENCE("absence"),
    /** Activity that is statistically unusual against a learned, coverage-aware baseline. */
    ANOMALY("anomaly");

    private final String wire;

    RuleType(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    public String wire() {
        return wire;
    }

    /** True for the three windowed types. */
    public boolean windowed() {
        return this != RECORD;
    }

    /**
     * True when a stored rule type belongs to the per-record engine (017) and its persist gate: only
     * {@code record} (or a null/blank type, which reads as record). A windowed type, or an unknown one,
     * is never compiled into the per-record engine, where a windowed rule's default {@code match} mode
     * would fire on every version.
     *
     * @param wire the stored {@code trigger_rule.type}
     * @return true for the record engine
     */
    public static boolean recordEngine(String wire) {
        try {
            return fromWire(wire) == RECORD;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Parses a wire value, defaulting to {@code record} for null/blank. */
    public static RuleType fromWire(String value) {
        if (value == null || value.isBlank()) {
            return RECORD;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (RuleType type : values()) {
            if (type.wire.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown rule type '" + value + "'");
    }
}
