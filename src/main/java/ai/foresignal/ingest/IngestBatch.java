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
 */
public record IngestBatch(
        String sourceId, FetchLogEntry log, List<IngestRecord> records, CompletableFuture<IngestOutcome> future)
        implements IngestCommand {

    public IngestBatch {
        Objects.requireNonNull(sourceId, "sourceId");
        records = records == null ? List.of() : List.copyOf(records);
        future = future == null ? new CompletableFuture<>() : future;
    }
}
