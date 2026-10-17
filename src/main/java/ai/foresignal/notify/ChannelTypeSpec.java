package ai.foresignal.notify;

import java.util.List;

/**
 * A channel type's catalog entry (OpenAPI {@code ChannelType}, without the capabilities, which come
 * from {@link ChannelType#capabilities()}).
 *
 * @param id the type id
 * @param name the display name
 * @param description what it delivers to
 * @param configFields the non-secret fields
 * @param secretFields the write-only secret fields
 * @param events the events the type can carry
 */
public record ChannelTypeSpec(
        String id,
        String name,
        String description,
        List<ChannelFieldSpec> configFields,
        List<ChannelFieldSpec> secretFields,
        List<String> events) {

    /** Every event a channel can subscribe to in prompt 018. */
    public static final List<String> ALL_EVENTS = List.of("alert", "briefing", "system", "analyst.note",
            "situation.activated", "situation.escalated");

    /** Copies the lists. */
    public ChannelTypeSpec {
        configFields = List.copyOf(configFields);
        secretFields = List.copyOf(secretFields);
        events = events == null ? ALL_EVENTS : List.copyOf(events);
    }

    /** The names of the secret fields. */
    public List<String> secretNames() {
        return secretFields.stream().map(ChannelFieldSpec::name).toList();
    }
}
