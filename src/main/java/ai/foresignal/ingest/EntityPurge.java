package ai.foresignal.ingest;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Erases one entity (prompt 039, Core-Data-Model §6 amendment). Runs on the writer, so it is ordered
 * with every batch and never races one.
 *
 * <p>In one transaction the writer allocates one seq, writes a tombstone version
 * ({@link PersistReason#TOMBSTONE}, {@link QaFlags#PURGED}), deletes every older
 * {@code record_version} row of the entity on both tiers ({@code seq < tombstoneSeq}, a bounded
 * single-entity delete on the {@code (source_id, external_id, seq)} index), and sets the head and live
 * rows to the tombstone state. After the commit the tombstone's change is published with
 * {@code purged: true}. A later {@link HeadPurge} removes the head once retention has removed the
 * tombstone (invariant I7).
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param future completed with the outcome; {@link EntityPurgeOutcome#found()} is false when the
 *     entity had no head
 */
public record EntityPurge(String sourceId, String externalId, CompletableFuture<EntityPurgeOutcome> future)
        implements IngestCommand {

    /** Requires the ids; a null future becomes a new one. */
    public EntityPurge {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(externalId, "externalId");
        future = future == null ? new CompletableFuture<>() : future;
    }
}
