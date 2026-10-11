package ai.foresignal.ingest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Deletes heads (and their live rows) whose every version retention has removed
 * (Core-Data-Model §6.2 "HeadPurge", §13). Runs on the writer, so it never races a batch.
 *
 * @param specs one entry per source
 * @param future completed with the number of heads deleted per source
 */
public record HeadPurge(List<PurgeSpec> specs, CompletableFuture<PurgeOutcome> future)
        implements IngestCommand {

    public HeadPurge {
        specs = specs == null ? List.of() : List.copyOf(specs);
        future = future == null ? new CompletableFuture<>() : future;
    }
}
