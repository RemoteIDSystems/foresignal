package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * The {@code IngestPolicy} schema: a union discriminated on {@code mode} ({@code ALL},
 * {@code SAMPLED}, {@code LATEST}), in the same JSON shape as {@code source_settings.ingest_policy}
 * (Core-Data-Model §5.1).
 *
 * <p>One flat record covers the three variants; fields a variant does not have are null and left
 * out of the JSON. Durations are strings in shorthand or ISO-8601, and {@code keyframe} may also be
 * {@code "none"} (only for a source without a TTL). Conversion to and from the domain
 * {@link ai.foresignal.source.spi.IngestPolicy} goes through {@link ai.foresignal.source.IngestPolicyCodec},
 * so the API and the stored settings cannot drift apart.
 *
 * @param mode {@code ALL}, {@code SAMPLED} or {@code LATEST}
 * @param minInterval minimum gap between SAMPLE versions (SAMPLED, LATEST)
 * @param keyframe keyframe interval, or {@code none}
 * @param minDeviationMeters dead-reckoning deviation that triggers a sample (SAMPLED)
 * @param thresholds whether descriptor thresholds also trigger samples (SAMPLED)
 * @param edgeMinInterval minimum gap between TRIGGER_EDGE versions (SAMPLED, LATEST)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IngestPolicyDto(
        String mode,
        String minInterval,
        String keyframe,
        @PositiveOrZero Double minDeviationMeters,
        Boolean thresholds,
        String edgeMinInterval) {}
