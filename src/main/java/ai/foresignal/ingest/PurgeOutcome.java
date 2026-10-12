package ai.foresignal.ingest;

import java.util.Map;

/**
 * What a {@link HeadPurge} deleted.
 *
 * @param headsDeleted heads deleted per source id
 * @param watermarkSeq {@code ChangeBus.lastPublishedSeq()} read on the writer thread when the purge
 *     completed; retention records it as the source's {@code purgeSeq} when heads were deleted
 *     (prompt 019, Conventions "RetentionState.purgeSeq")
 */
public record PurgeOutcome(Map<String, Integer> headsDeleted, long watermarkSeq) {

    public PurgeOutcome {
        headsDeleted = headsDeleted == null ? Map.of() : Map.copyOf(headsDeleted);
    }

    /** An outcome without a watermark (tests). */
    public PurgeOutcome(Map<String, Integer> headsDeleted) {
        this(headsDeleted, 0L);
    }

    /** Heads deleted for {@code sourceId}; 0 when none. */
    public int deleted(String sourceId) {
        return headsDeleted.getOrDefault(sourceId, 0);
    }
}
