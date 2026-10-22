package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * {@code FeatureCollection} (RFC 7946) with the paging and snapshot foreign members.
 *
 * @param type always {@code FeatureCollection}
 * @param features the features
 * @param nextCursor the next page's cursor; null on the last page or when streaming does not apply
 * @param watermarkSeq the snapshot watermark; null for history
 * @param asOfSeq the pinned snapshot seq; omitted when null
 * @param asOf the echoed asOf; omitted when null
 * @param approximate asOf only; omitted otherwise
 * @param horizon asOf only; omitted otherwise
 * @param attributions the aggregated attributions (prompt 109, a foreign member); null when none
 * @param licenceFiltered the number of features removed by the caller's license policy (prompt 109)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeatureCollectionPage(
        String type,
        @JsonInclude(JsonInclude.Include.ALWAYS) List<Map<String, Object>> features,
        @JsonInclude(JsonInclude.Include.ALWAYS) String nextCursor,
        Long watermarkSeq,
        Long asOfSeq,
        Instant asOf,
        Boolean approximate,
        Instant horizon,
        List<Map<String, Object>> attributions,
        Integer licenceFiltered) {

    /** A collection of {@code features}. */
    public static FeatureCollectionPage of(
            List<Map<String, Object>> features,
            String nextCursor,
            Long watermarkSeq,
            Long asOfSeq,
            Instant asOf,
            Boolean approximate,
            Instant horizon) {
        return new FeatureCollectionPage(
                "FeatureCollection", features, nextCursor, watermarkSeq, asOfSeq, asOf, approximate, horizon, null,
                null);
    }

    /** A collection carrying the aggregated attributions and the license-filtered count (prompt 109). */
    public static FeatureCollectionPage of(
            List<Map<String, Object>> features,
            String nextCursor,
            Long watermarkSeq,
            Long asOfSeq,
            Instant asOf,
            Boolean approximate,
            Instant horizon,
            List<Map<String, Object>> attributions,
            Integer licenceFiltered) {
        return new FeatureCollectionPage(
                "FeatureCollection", features, nextCursor, watermarkSeq, asOfSeq, asOf, approximate, horizon,
                attributions, licenceFiltered);
    }
}
