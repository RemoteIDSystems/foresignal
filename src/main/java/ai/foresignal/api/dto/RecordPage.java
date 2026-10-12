package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * {@code RecordPage}: a page of current records or an asOf page.
 *
 * @param items the records ({@code Record} items, projected by {@code fields=})
 * @param nextCursor the next page's cursor; null on the last page
 * @param watermarkSeq {@code ChangeBus.lastPublishedSeq()} read before the query ({@code S(T)} for asOf)
 * @param asOfSeq the pinned snapshot seq ({@code asOfSeq} or {@code order=id}); null otherwise
 * @param asOf the echoed asOf; null otherwise
 * @param approximate asOf only: a SAMPLED or LATEST source is involved; omitted otherwise
 * @param horizon asOf only: the earliest supported T over the involved sources
 */
public record RecordPage(
        List<Map<String, Object>> items,
        String nextCursor,
        long watermarkSeq,
        Long asOfSeq,
        Instant asOf,
        @JsonInclude(JsonInclude.Include.NON_NULL) Boolean approximate,
        Instant horizon) {}
