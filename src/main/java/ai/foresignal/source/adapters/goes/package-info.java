/**
 * GOES-19 sources from the {@code noaa-goes19} bucket.
 *
 * <ul>
 *   <li>{@code goes-east-abi} (prompt 046): ABI Level 2 Cloud and Moisture Imagery, CONUS sector, band 13
 *       (clean longwave infrared), every 5-minute scan, as raster frames.
 *   <li>{@code goes-glm} (prompt 048): GLM L2 flashes into the flash store (archive and places-AOI ring),
 *       and {@code lightning-cluster} records ({@link ai.foresignal.source.adapters.goes.LightningClusterer}).
 *   <li>{@code goes-fdc} (prompt 048): ABI Fire/Hot Spot Characterization as {@code fire-detection} and
 *       {@code fire-cluster} records ({@link ai.foresignal.source.adapters.goes.FireClusterer}).
 * </ul>
 *
 * <p>The lightning and fire sources tombstone only after scans that were actually processed, so a feed
 * outage freezes their records instead of clearing them.
 */
package ai.foresignal.source.adapters.goes;
