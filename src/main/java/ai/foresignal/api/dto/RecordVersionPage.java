package ai.foresignal.api.dto;

import java.util.List;
import java.util.Map;

/**
 * {@code RecordVersionPage} (versions endpoint) and {@code HistoryPage} (history): versions, oldest
 * first or ordered by the selected time axis.
 *
 * @param items {@code RecordVersion} items
 * @param nextCursor the next page's cursor; null on the last page
 */
public record RecordVersionPage(List<Map<String, Object>> items, String nextCursor) {}
