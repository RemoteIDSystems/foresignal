package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One series catalogue entry (OpenAPI {@code SeriesInfo}, prompt 093), built from a {@code series-latest} record.
 *
 * @param sourceId the source the series belongs to
 * @param series the series id, matching the point payload's {@code series}
 * @param seriesName the human-readable name
 * @param dataset the upstream dataset or route
 * @param unit the UCUM unit
 * @param frequency the fixed frequency code
 * @param area the area the series covers
 * @param latestPeriod the newest period label
 * @param latestValue the newest value
 * @param status the newest point's status
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record SeriesInfoDto(
        String sourceId,
        String series,
        String seriesName,
        String dataset,
        String unit,
        String frequency,
        String area,
        String latestPeriod,
        Double latestValue,
        String status) {}
