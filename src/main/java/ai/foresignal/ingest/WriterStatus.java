package ai.foresignal.ingest;

import java.time.Instant;
import java.util.List;

/**
 * A snapshot of the ingest writer, for {@code /v1/system} (prompt 019).
 *
 * @param state what the writer is doing
 * @param warnings recent warnings (clock jumps, lock loss, partition headroom), newest last
 * @param lastSeq the highest committed seq
 * @param lastIngestedAt the latest committed {@code ingested_at}, or null before any
 * @param queueDepth commands waiting
 */
public record WriterStatus(
        WriterState state, List<String> warnings, long lastSeq, Instant lastIngestedAt, int queueDepth) {

    public WriterStatus {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
