package ai.foresignal.inbound;

import ai.foresignal.api.mapper.FetchOutcomeMapper;
import ai.foresignal.ingest.IngestOutcome;
import java.util.List;
import java.util.Map;

/**
 * The v1.2 {@code IngestOutcome} of one push (OpenAPI {@code IngestOutcome}, prompt 024).
 *
 * <p>Counts cover the whole request: {@code seen} is every item the request carried, and
 * {@code rejected} and {@code rejections} include the items the format refused as well as the
 * records the normalizer refused, each with its index in the request.
 *
 * @param seen items in the request
 * @param changed deprecated alias of {@code persisted}
 * @param persisted versions written
 * @param live live-only updates
 * @param touched touches
 * @param touchesThrottled touches suppressed by the throttle
 * @param late observations older than the live state
 * @param skipped tombstones for absent or already tombstoned entities
 * @param rejected refused items
 * @param fromSeq the first seq allocated, or null
 * @param toSeq the last seq allocated, or null
 * @param byReason versions per persist reason, all nine present
 * @param versionBytes estimated stored size of the versions written
 * @param rejections each refused item
 */
public record IngestOutcomeView(
        int seen,
        int changed,
        int persisted,
        int live,
        int touched,
        int touchesThrottled,
        int late,
        int skipped,
        int rejected,
        Long fromSeq,
        Long toSeq,
        Map<String, Integer> byReason,
        long versionBytes,
        List<RejectionView> rejections) {

    /**
     * One refused item.
     *
     * @param index its position in the request
     * @param externalId its external id, or null
     * @param reason the rejection code
     */
    public record RejectionView(int index, String externalId, String reason) {}

    /** The view of a writer outcome, with the request-level counts and merged rejections. */
    public static IngestOutcomeView of(IngestOutcome outcome, int seen, List<RejectionView> rejections) {
        IngestOutcome o = outcome == null ? IngestOutcome.empty() : outcome;
        return new IngestOutcomeView(
                seen,
                o.persisted(),
                o.persisted(),
                o.live(),
                o.touched(),
                o.touchesThrottled(),
                o.late(),
                o.skipped(),
                rejections.size(),
                o.fromSeq() == 0 ? null : o.fromSeq(),
                o.toSeq() == 0 ? null : o.toSeq(),
                FetchOutcomeMapper.byReason(o.byReason()),
                o.versionBytes(),
                List.copyOf(rejections));
    }
}
