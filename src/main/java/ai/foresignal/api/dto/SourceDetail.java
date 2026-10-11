package ai.foresignal.api.dto;

/**
 * The {@code SourceDetail} schema: descriptor, settings and health of one source.
 *
 * @param descriptor what the source is
 * @param settings how it is configured
 * @param health how it is doing
 */
public record SourceDetail(
        SourceDescriptorDto descriptor, SourceSettingsDto settings, SourceHealthDto health) {}
