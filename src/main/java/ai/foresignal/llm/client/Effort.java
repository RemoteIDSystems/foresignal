package ai.foresignal.llm.client;

import java.util.Locale;

/**
 * How much thinking and output effort a model spends (Anthropic {@code output_config.effort}).
 *
 * <p>Foresignal always sends effort explicitly to Anthropic: Ask and briefings use {@link #MEDIUM},
 * enrichment {@link #LOW} (Architecture §19). Providers without an effort control ignore it.
 */
public enum Effort {
    /** Fewest tokens; simple, high-volume work such as enrichment. */
    LOW,
    /** The default for Ask and briefings. */
    MEDIUM,
    /** More thorough. */
    HIGH,
    /** Between high and max. */
    XHIGH,
    /** Most thorough, most expensive. */
    MAX;

    /** The wire value, for example {@code medium}. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a wire value.
     *
     * @throws IllegalArgumentException for an unknown value
     */
    public static Effort fromWire(String value) {
        if (value == null) {
            throw new IllegalArgumentException("effort is null");
        }
        for (Effort effort : values()) {
            if (effort.wireValue().equals(value.trim().toLowerCase(Locale.ROOT))) {
                return effort;
            }
        }
        throw new IllegalArgumentException("Unknown effort '" + value + "'. Expected low, medium, high, xhigh or max.");
    }
}
