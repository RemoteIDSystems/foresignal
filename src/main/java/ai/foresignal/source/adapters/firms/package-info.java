/**
 * The NASA FIRMS active-fire source {@code nasa-firms} (prompt 065): VIIRS (NOAA-20, NOAA-21, Suomi-NPP), MODIS and
 * optionally Landsat near-real-time detections from the FIRMS area API, as {@code fire-detection} point records,
 * the kind {@code goes-fdc} and {@code hms-fire} also write. Fetched incrementally with a per-product high-water
 * mark, one product after another, and never without the operator's MAP_KEY.
 */
package ai.foresignal.source.adapters.firms;
