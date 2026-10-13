/**
 * The derived weather sources (prompt 052; Weather-Imagery-Design §7.1–§7.4), built on the DERIVED
 * runtime of {@code ai.foresignal.derive}:
 * <ul>
 *   <li>{@code wx-places}: one {@code place-weather} record per place ({@code place:{id}}): radar, hail,
 *       precipitation, lightning, storm-cell ETA, smoke and GFS forecast sections;
 *   <li>{@code wx-areas}: {@code wx-area} records ({@code {field}:{threshold}:{aoi}}) with the area above a
 *       threshold inside the places AOI or a place;
 *   <li>{@code wx-cells}: tracked {@code radar-cell} records ({@code c-{ulid}}) with centroid motion.
 * </ul>
 *
 * <p><b>Unknown is not clear.</b> Every section has {@code status: ok|stale|no-coverage|unavailable}. When
 * its input is older than the product's maximum gap (MRMS 6 min, GLM 2 min), comes from a failing source
 * or lacks coverage, the section keeps its previous metric values, sets the status and adds {@code asOf},
 * the time of the last good data, fixed while not ok. Before the first good data a section carries its
 * status only; metric keys are omitted, never null. "None within 100 km" is the number {@code 999}.
 *
 * <p>These generators read rasters only through {@code DerivedContext.rasters()} (the read-only
 * {@code raster.sample.RasterReader}) and records through {@code DerivedContext.records()}; they never
 * write the store and never fetch.
 */
package ai.foresignal.source.adapters.wx;
