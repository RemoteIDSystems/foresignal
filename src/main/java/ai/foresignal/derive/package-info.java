/**
 * The DERIVED-source runtime (prompt 052; Weather-Imagery-Design §2.1 ⑥ and §7.1; prompt 099 reuses
 * it).
 *
 * <p>A derived source is a {@link ai.foresignal.derive.DerivedGenerator}: an ordinary
 * {@link ai.foresignal.source.spi.SourceAdapter} with {@code Mode.DERIVED} that computes records from
 * data Foresignal already holds. The {@link ai.foresignal.derive.DerivedRunner} feeds it change batches,
 * raster frame events and periodic ticks on one virtual thread per enabled source, and hands what it
 * emits to the single ingest writer, one batch and one {@code fetch_log} row per invocation, recorded
 * in health like a fetch.
 *
 * <p>The rules every generator keeps:
 * <ul>
 *   <li>emit only when the quantized payload changed (the writer's content hash deduplicates, and the
 *       runtime also skips an emit identical to the previous one);
 *   <li>{@code observedAt} is the time of the input in which the change was seen, never earlier than
 *       the entity's previous emit;
 *   <li>tombstone only when an object disappears or its subject is deleted, <strong>never because input
 *       is stale</strong>, and never write heartbeat versions (derived records have no TTL);
 *   <li>rebuild state in {@link ai.foresignal.derive.DerivedGenerator#reconcile} at startup, on enable
 *       and after a change-bus gap.
 * </ul>
 *
 * <p>Nothing here knows about weather; the weather generators live in
 * {@code ai.foresignal.source.adapters.wx}.
 */
package ai.foresignal.derive;
