/**
 * The {@code hrrr} forecast model source (prompt 051): NOAA HRRR CONUS from the
 * {@code noaa-hrrr-bdp-pds} bucket, read with byte ranges planned from each step's {@code .idx} by
 * {@link ai.foresignal.raster.model.ModelRunIngest}, grid-relative winds rotated to earth-relative.
 */
package ai.foresignal.source.adapters.hrrr;
