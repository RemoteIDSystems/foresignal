/**
 * Point and area sampling of stored raster frames (prompt 052; Weather-Imagery-Design §6.5).
 *
 * <p>{@link ai.foresignal.raster.sample.SampleService} resolves a time expression to a frame and samples
 * a point with explicit status semantics ({@code ok}, {@code no-coverage}, {@code no-data},
 * {@code stale}, {@code unavailable}: anything but ok is unknown, never clear), builds point series, and
 * is the read-only {@link ai.foresignal.raster.sample.RasterReader} of the DERIVED generators.
 * {@link ai.foresignal.raster.sample.AreaStatsService} computes area statistics with spherical cell areas
 * ({@link ai.foresignal.raster.sample.CellAreas}); {@link ai.foresignal.raster.sample.PlaceGridIndex}
 * precomputes each place's nearby cells sorted by distance; {@link
 * ai.foresignal.raster.sample.ForecastSeriesService} caches GFS and HRRR point series. Nothing here ever
 * triggers an upstream fetch.
 */
package ai.foresignal.raster.sample;
