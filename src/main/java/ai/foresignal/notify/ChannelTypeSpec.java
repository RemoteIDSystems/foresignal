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

    /**
     * The personal events (prompt 130) an instance channel can also subscribe to: each notice to a principal is
     * copied to every enabled instance channel subscribed to it, with the TLP label at its head. They never reach
     * the device inbox or the instance SSE topics.
     */
    public static final List<String> PERSONAL_COPY_EVENTS = List.of("mention", "share", "report.received",
            "package.received", "chronicle.proposed", "chronicle.cap_warning", "chronicle.gap",
            "chronicle.access.requested", "saved-search.matches");

    /** Every event a channel can subscribe to (prompt 018, extended by later prompts up to 137), as the contract's
     * {@code Channel.events} lists them. */
    public static final List<String> ALL_EVENTS = List.of("alert", "briefing", "system", "analyst.note",
            "situation.activated", "situation.escalated", "finding.significant", "forecast.resolved",
            "forecast.resolution_needed", "hypothesis.band_changed", "indicator.observed", "assumption.broken",
            "assessment.published", "social.divergence", "social.attention_gap", "mention", "share",
            "report.received", "package.received", "chronicle.proposed", "chronicle.cap_warning", "chronicle.gap",
            "chronicle.access.requested", "saved-search.matches");

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
