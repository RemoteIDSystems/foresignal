/**
 * Partitioned history storage: the shape of {@code record_version} ({@link ai.foresignal.store.history.HistoryDdl}) and the
 * {@link ai.foresignal.store.history.PartitionManager} that keeps its seq-range leaves ahead of
 * allocation (Core-Data-Model §9).
 *
 * <p>Nothing here depends on JPA: the Flyway baseline creates the partitioned parent, and these classes attach its
 * leaves after Flyway and before the {@code EntityManagerFactory} is built (Architecture §25).
 *
 * <p>Prompt 037 adds the shared asOf planning on the transaction axis
 * ({@link ai.foresignal.store.history.AsOfResolver}, {@link ai.foresignal.store.history.AsOfPlan})
 * and the downsampling hook {@link ai.foresignal.store.history.HistoryFidelity}; those are ordinary
 * read-side beans and run after startup.
 */
package ai.foresignal.store.history;
