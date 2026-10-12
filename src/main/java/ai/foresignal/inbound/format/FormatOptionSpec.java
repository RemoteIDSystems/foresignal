package ai.foresignal.inbound.format;

/**
 * One option a format accepts under {@code formats.{id}} (OpenAPI {@code IngestFormatOption}).
 *
 * @param name the option name
 * @param type {@code string}, {@code integer}, {@code number}, {@code boolean}, {@code object} or
 *     {@code array}
 * @param required true when the format cannot work without it
 * @param defaultValue the default, or null
 * @param description what it does
 */
public record FormatOptionSpec(String name, String type, boolean required, Object defaultValue, String description) {}
