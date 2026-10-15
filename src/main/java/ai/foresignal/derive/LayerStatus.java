package ai.foresignal.derive;

/**
 * The status of a derived record (prompt 099): whether the condition it represents is a real,
 * fully-covered finding, or whether the input is too thin to say (Weather-Imagery-Design §7.1
 * "unknown is not clear").
 */
public enum LayerStatus {

    /** Enough fresh, healthy input: the condition is asserted. */
    OK("ok"),
    /** Some input, but below the generator's minimum for an assertion. */
    INSUFFICIENT_DATA("insufficient-data"),
    /** No input at all from the required sources. */
    NO_COVERAGE("no-coverage"),
    /** A change-bus gap or a live drop touched the window. */
    DEGRADED_INPUT("degraded-input");

    private final String wire;

    LayerStatus(String wire) {
        this.wire = wire;
    }

    /** The wire value used in payloads. */
    public String wire() {
        return wire;
    }

    /** True when the condition may be asserted (never for the unknown statuses). */
    public boolean asserted() {
        return this == OK;
    }
}
