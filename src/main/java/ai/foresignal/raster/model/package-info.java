/**
 * Forecast models and wind (prompt 051; Weather-Imagery-Design §1.4, §2.4, §6.4).
 *
 * <ul>
 *   <li>Ported from God's Eye View (MIT, notices in each file and in {@code THIRD_PARTY_NOTICES.md}):
 *       {@link ai.foresignal.raster.model.GribIdx} ({@code .idx} inventories),
 *       {@link ai.foresignal.raster.model.IdxRangePlanner} (byte ranges),
 *       {@link ai.foresignal.raster.model.ModelCycleSelector} (cycles and object keys) and
 *       {@link ai.foresignal.raster.model.WindResampler} (the God's Eye View field grid);
 *   <li>{@link ai.foresignal.raster.model.FieldSelector}: fields by variable, level and run-total
 *       accumulation, never by message number;
 *   <li>{@link ai.foresignal.raster.model.WindRotation}: grid-relative to earth-relative winds on a
 *       Lambert conformal grid (HRRR);
 *   <li>{@link ai.foresignal.raster.model.ModelRunIngest}: the engine of the {@code gfs} and
 *       {@code hrrr} sources (runs, steps, budgeted range reads, run completion);
 *   <li>{@link ai.foresignal.raster.model.FieldStore}: the precompressed {@code .f32.gz} wind fields;
 *   <li>{@link ai.foresignal.raster.model.FieldService}, {@link ai.foresignal.raster.model.VectorTileService}
 *       and {@link ai.foresignal.raster.model.TimelineService}: the fields, barb vector tile and
 *       timeline endpoints.
 * </ul>
 */
package ai.foresignal.raster.model;
