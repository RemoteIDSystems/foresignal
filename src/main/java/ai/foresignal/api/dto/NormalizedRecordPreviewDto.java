package ai.foresignal.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The {@code NormalizedRecordPreview} schema: one parsed record from a probe, never ingested.
 *
 * @param externalId the upstream id
 * @param kind the record kind
 * @param observedAt observation time
 * @param geometry GeoJSON geometry, or null
 * @param validUntil end of validity, or null
 * @param tombstone whether the record deletes the entity
 * @param payload the normalized payload
 * @param eventTime when the event happened, or null
 * @param validFrom start of validity, or null
 * @param quality observation quality, or null
 * @param observers receivers that observed it
 */
public record NormalizedRecordPreviewDto(
        String externalId,
        String kind,
        Instant observedAt,
        Map<String, Object> geometry,
        Instant validUntil,
        boolean tombstone,
        Map<String, Object> payload,
        Instant eventTime,
        Instant validFrom,
        QualityDto quality,
        List<ObserverDto> observers) {}
