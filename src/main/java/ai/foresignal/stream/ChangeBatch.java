package ai.foresignal.stream;

import java.time.Instant;
import java.util.List;

/**
 * The versions one committed ingest batch persisted, in seq order (Core-Data-Model §10.1).
 *
 * <p>Published only after the batch's transaction commits, and only when it persisted at least
 * one version. Batches are published in commit order, which with one writer is seq order.
 *
 * @param sourceId the source
 * @param batchId the batch's {@code fetch_log.id}
 * @param ingestedAt the batch's transaction timestamp
 * @param fromSeq the lowest seq in the batch
 * @param toSeq the highest seq in the batch
 * @param changes one summary per version, ordered by seq
 */
public record ChangeBatch(
        String sourceId, long batchId, Instant ingestedAt, long fromSeq, long toSeq, List<ChangeSummary> changes) {

    public ChangeBatch {
        changes = changes == null ? List.of() : List.copyOf(changes);
    }
}
