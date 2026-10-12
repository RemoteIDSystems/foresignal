/**
 * Retention: window resolution, the retention job and the retention state read clients depend on
 * (Core-Data-Model §13, prompt 019).
 *
 * <h2>Windows</h2>
 *
 * <p>{@link ai.foresignal.retention.RetentionResolver}: TRACK always uses {@code retention.trackWindow};
 * STANDARD resolves {@code retention.kinds.{kind}}, then the per-source override
 * ({@code source_settings.retention}), then the descriptor's {@code kindRetention[kind]}, then its
 * {@code defaultRetention}, then {@code retention.default}.
 *
 * <h2>The job</h2>
 *
 * <p>{@link ai.foresignal.retention.RetentionJob} runs ordered
 * {@link ai.foresignal.retention.RetentionStep}s: TRACK leaf drops, STANDARD chunked deletes, STANDARD
 * leaf drops, one head purge on the writer, {@code fetch_log} and {@code seq_clock} pruning and the
 * operational tables. Every cutoff is clamped by the registered
 * {@link ai.foresignal.retention.RetentionConsumer}s.
 *
 * <h2>Concurrency</h2>
 *
 * <ul>
 *   <li>Only the ingest writer inserts versions and writes heads and live rows (I1). Head purges are
 *       commands executed on the writer thread, so they never race a batch. Retention itself never
 *       writes {@code record_head} or {@code record_live}.
 *   <li>Retention deletes only versions whose seq is below a cutoff taken from the past
 *       ({@code cutoffSeq(now − window)} from {@code seq_clock}), so it never touches a version a
 *       batch in flight could be writing or referencing as {@code prev_seq} of a new one. Deletes run
 *       in small autocommit chunks.
 *   <li>Leaves are dropped only through {@code PartitionManager}, under its lock, with
 *       {@code DETACH PARTITION ... CONCURRENTLY}, and never a leaf the allocator has not passed.
 *   <li>An entity that is still observed keeps a version newer than a quarter of its window (the
 *       retention keyframe, I6), so its head's seq stays above the floor and it is never a purge
 *       candidate. A head is purged only when every version of it is gone ({@code h.seq < floorSeq},
 *       I7) and its live row is stale.
 *   <li>A purged entity that reappears starts again at version 1 with reason {@code FIRST}; no older
 *       versions survive to contradict it (I7).
 *   <li>FR-17 as amended: current state is kept while current; versions follow the window.
 * </ul>
 *
 * <h2>State</h2>
 *
 * <p>{@link ai.foresignal.retention.RetentionStateStore} publishes floors, {@code lowestRetainedSeq}
 * (prompt 015's {@code reset}) and {@code purgeSeq} (prompt 014's {@code 410 RESYNC_REQUIRED}),
 * persisted in {@code retention_state}.
 */
package ai.foresignal.retention;
