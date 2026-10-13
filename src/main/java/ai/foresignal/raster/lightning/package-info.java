/**
 * GOES GLM lightning storage (prompt 048, Weather-Imagery-Design §2.4 and §2.6): the
 * {@link ai.foresignal.raster.lightning.FlashStore} behind {@code ctx.rasters().flashes()}, with its
 * compact daily {@link ai.foresignal.raster.lightning.FlashArchive} (16 bytes per flash, 14 days) and
 * the in-memory 60-minute ring of flashes inside the places AOI.
 *
 * <p>Flashes are never records: the {@code goes-glm} adapter writes them here and derives
 * {@code lightning-cluster} records from the ring; 052's {@code wx-places} reads place metrics from it.
 */
package ai.foresignal.raster.lightning;
