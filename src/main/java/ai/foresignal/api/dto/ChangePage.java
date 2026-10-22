package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * {@code ChangePage}: changes in commit order.
 *
 * @param items {@code Change} items
 * @param lastSeq pass as {@code after} next time
 * @param hasMore true when more changes are waiting
 * @param watermarkSeq {@code ChangeBus.lastPublishedSeq()} at query time
 * @param purgeHorizon the largest {@code RetentionState.purgeSeq} over the filtered sources
 * @param attributions the aggregated attributions (prompt 109); null when none
 * @param licenceFiltered the number of changes removed by the caller's license policy (prompt 109)
 */
public record ChangePage(
        List<Map<String, Object>> items, long lastSeq, boolean hasMore, long watermarkSeq, long purgeHorizon,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<Map<String, Object>> attributions,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer licenceFiltered) {

    /** The pre-109 page. */
    public ChangePage(List<Map<String, Object>> items, long lastSeq, boolean hasMore, long watermarkSeq,
            long purgeHorizon) {
        this(items, lastSeq, hasMore, watermarkSeq, purgeHorizon, null, null);
    }
}
