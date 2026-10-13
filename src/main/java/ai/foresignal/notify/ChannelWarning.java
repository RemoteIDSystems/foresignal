package ai.foresignal.notify;

/**
 * A non-fatal configuration warning returned with a channel (OpenAPI {@code Channel.warnings}), for example
 * {@code MQTT_INSECURE_PUBLIC_BROKER} (prompt 060). It never refuses the channel.
 *
 * @param code the machine-readable code
 * @param message what to do about it
 */
public record ChannelWarning(String code, String message) {}
