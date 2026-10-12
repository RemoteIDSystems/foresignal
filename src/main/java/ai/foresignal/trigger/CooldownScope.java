package ai.foresignal.trigger;

import java.util.Locale;

/**
 * What a rule's cooldown is counted per (Weather-Imagery-Design §7.3): {@link #RECORD} per entity
 * (the default), {@link #RULE} across all of the rule's entities.
 */
public enum CooldownScope {
    /** Per (rule, source, externalId). */
    RECORD,
    /** Per rule. */
    RULE;

    /** The lowercase wire value. */
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a wire value.
     *
     * @throws IllegalArgumentException for anything but {@code record} or {@code rule}
     */
    public static CooldownScope fromWire(String value) {
        return switch (value) {
            case "record" -> RECORD;
            case "rule" -> RULE;
            default -> throw new IllegalArgumentException("must be record or rule");
        };
    }
}
