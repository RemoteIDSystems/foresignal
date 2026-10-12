package ai.foresignal.ingest;

/**
 * A unit of work for the single ingest writer (Core-Data-Model §3, §6).
 *
 * <p>Sealed so the writer's dispatch is exhaustive: a batch of observations, or a purge of heads
 * that retention has emptied, or the erasure of one entity (prompt 039).
 */
public sealed interface IngestCommand permits IngestBatch, HeadPurge, EntityPurge {}
