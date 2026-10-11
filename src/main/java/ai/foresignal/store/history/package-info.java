/**
 * Partitioned history storage: the bootstrap DDL for {@code record_version} and the
 * {@link ai.foresignal.store.history.PartitionManager} that keeps its seq-range leaves ahead of
 * allocation (Core-Data-Model §9).
 *
 * <p>Nothing here depends on JPA: these classes run before the {@code EntityManagerFactory} is
 * built, and their DDL is moved into the Flyway baseline by prompt 144.
 */
package ai.foresignal.store.history;
