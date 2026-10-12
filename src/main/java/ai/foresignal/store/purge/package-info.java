/**
 * Per-entity export and erasure (prompt 039): {@link ai.foresignal.store.purge.EntityPurgeService}
 * submits the writer's {@code EntityPurge} command and fans the erasure out to derived stores
 * ({@link ai.foresignal.store.purge.PurgeFanout}); {@link ai.foresignal.store.purge.EntityExportService}
 * streams everything retained about one entity as GeoJSON, ndjson or GPX with a hashed manifest.
 */
package ai.foresignal.store.purge;
