package ai.foresignal.api.dto;

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
 */
public record ChangePage(
        List<Map<String, Object>> items, long lastSeq, boolean hasMore, long watermarkSeq, long purgeHorizon) {}
