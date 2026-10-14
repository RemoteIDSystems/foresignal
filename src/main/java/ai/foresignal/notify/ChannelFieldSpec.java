package ai.foresignal.notify;

import java.util.List;

/**
 * One config or secret field of a channel type, as {@code GET /v1/channels/types} serves it.
 *
 * @param name the field name
 * @param type {@code string}, {@code url}, {@code integer}, {@code number} (prompt 084), {@code boolean},
 *     {@code string-list}, {@code map} or {@code enum}
 * @param required whether it must be present
 * @param description what it is
 * @param defaultValue the default, or null
 * @param allowed the allowed values of an {@code enum}, or empty
 */
public record ChannelFieldSpec(
        String name, String type, boolean required, String description, Object defaultValue, List<String> allowed) {

    /** A string field. */
    public static final String STRING = "string";
    /** An absolute http(s) URL. */
    public static final String URL = "url";
    /** An integer. */
    public static final String INTEGER = "integer";
    /** A number, integral or not (prompt 084: {@code dutyCyclePercent}). */
    public static final String NUMBER = "number";
    /** A boolean. */
    public static final String BOOLEAN = "boolean";
    /** A list of strings. */
    public static final String STRING_LIST = "string-list";
    /** A map of strings. */
    public static final String MAP = "map";
    /** One of {@link #allowed()}. */
    public static final String ENUM = "enum";

    /** Copies the allowed values. */
    public ChannelFieldSpec {
        allowed = allowed == null ? List.of() : List.copyOf(allowed);
    }

    /** A required field without a default. */
    public static ChannelFieldSpec required(String name, String type, String description) {
        return new ChannelFieldSpec(name, type, true, description, null, List.of());
    }

    /** An optional field. */
    public static ChannelFieldSpec optional(String name, String type, String description, Object defaultValue) {
        return new ChannelFieldSpec(name, type, false, description, defaultValue, List.of());
    }

    /** An enum field. */
    public static ChannelFieldSpec choice(
            String name, boolean required, String description, String defaultValue, List<String> allowed) {
        return new ChannelFieldSpec(name, ENUM, required, description, defaultValue, allowed);
    }
}
