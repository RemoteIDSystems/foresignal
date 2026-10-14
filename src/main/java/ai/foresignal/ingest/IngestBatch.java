package ai.foresignal.ingest;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * One fetch, flush or push worth of records for one source.
 *
 * @param sourceId the source
 * @param log the batch's provenance, written as its {@code fetch_log} row
 * @param records the records in arrival order
 * @param future completed by the writer with the outcome, or exceptionally
 * @param forceTouch true for a complete-set reference run (prompt 094): an unchanged record is written as a
 *     touch even inside {@code touchMin}, so its {@code record_live.last_received_at} advances and the
 *     complete-set reconciliation never mistakes it for a record that disappeared
 */
public record IngestBatch(
        String sourceId, FetchLogEntry log, List<IngestRecord> records, CompletableFuture<IngestOutcome> future,
        boolean forceTouch)
        implements IngestCommand {

    public IngestBatch {
        Objects.requireNonNull(sourceId, "sourceId");
        records = records == null ? List.of() : List.copyOf(records);
        future = future == null ? new CompletableFuture<>() : future;
    }

    /** An ordinary batch that does not force touches. */
    public IngestBatch(
            String sourceId, FetchLogEntry log, List<IngestRecord> records, CompletableFuture<IngestOutcome> future) {
        this(sourceId, log, records, future, false);
    }
}
