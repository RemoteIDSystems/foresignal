package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * {@code RecordVersionPage} (versions endpoint) and {@code HistoryPage} (history): versions, oldest
 * first or ordered by the selected time axis.
 *
 * @param items {@code RecordVersion} items
 * @param nextCursor the next page's cursor; null on the last page
 * @param attributions the aggregated attributions (prompt 109); null when none
 * @param licenceFiltered the number of versions removed by the caller's license policy (prompt 109)
 */
public record RecordVersionPage(
        List<Map<String, Object>> items,
        String nextCursor,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<Map<String, Object>> attributions,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer licenceFiltered) {

    /** The pre-109 page. */
    public RecordVersionPage(List<Map<String, Object>> items, String nextCursor) {
        this(items, nextCursor, null, null);
    }
}
