package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One series catalog entry (OpenAPI {@code SeriesInfo} of prompt 093, extended to {@code SeriesCatalogEntry} by
 * prompt 117), built from a {@code series-latest} record.
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
 * @param family the series family (117): price, volume, open-interest, position, rate, attention, tone, count,
 *     index or structural
 * @param transform the transform hint (117): log-return, log-level, difference or level
 * @param releaseLagDays the typical delay between period and publication in days (117)
 * @param egress the source's egress in force, {@code OPEN} or {@code LOCAL_ONLY} (117)
 * @param regionCode the 040 region code, or null (117)
 * @param topicId the attention topic, or null (117)
 * @param attribution the credit line the provider asks for, or null (117: the FRED notice and the series' source)
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
        String status,
        String family,
        String transform,
        Double releaseLagDays,
        String egress,
        String regionCode,
        String topicId,
        String attribution) {

    /** An entry without the prompt 117 metadata, for tests and callers that build a bare catalog entry. */
    public SeriesInfoDto(String sourceId, String series, String seriesName, String dataset, String unit,
            String frequency, String area, String latestPeriod, Double latestValue, String status) {
        this(sourceId, series, seriesName, dataset, unit, frequency, area, latestPeriod, latestValue, status, null,
                null, null, null, null, null, null);
    }
}
