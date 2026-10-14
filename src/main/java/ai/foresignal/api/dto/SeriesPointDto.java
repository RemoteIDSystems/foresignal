package ai.foresignal.api.dto;

import java.time.Instant;
import java.util.Map;

/**
 * One data point of a series (OpenAPI {@code SeriesPoint}, prompt 093), the current version of a
 * {@code series-point} record.
 *
 * @param period the upstream period label
 * @param periodStart the period's start; the record's event time
 * @param periodEnd the period's end
 * @param value the measured value
 * @param unit the UCUM unit
 * @param status final, preliminary, estimated or forecast
 * @param extra flat scalar extras
 * @param version the record version; above 1 means the point was revised
 * @param observedAt the persisted observation time
 * @param knownAt the ingest time of the supplying version, or null
 * @param coverageOk false when the involved sources were failing, or null when not assessed
 */
public record SeriesPointDto(
        String period,
        Instant periodStart,
        Instant periodEnd,
        Double value,
        String unit,
        String status,
        Map<String, Object> extra,
        int version,
        Instant observedAt,
        Instant knownAt,
        Boolean coverageOk) {}
