package ai.foresignal.ingest;

/**
 * A unit of work for the single ingest writer (Core-Data-Model §3, §6).
 *
 * <p>Sealed so the writer's dispatch is exhaustive: a batch of observations, a purge of heads
 * that retention has emptied, the erasure of one entity (prompt 039), or the tombstoning of the
 * records a complete-set reference dataset no longer lists (prompt 094).
 */
public sealed interface IngestCommand permits IngestBatch, HeadPurge, EntityPurge, TombstoneAbsent {}
