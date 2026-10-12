/**
 * The raster decoders (prompt 046, Weather-Imagery-Design §0.1 and §3): NetCDF-Java behind one small
 * SPI.
 *
 * <ul>
 *   <li>{@link ai.foresignal.raster.decode.RasterDecoder}: {@code supports} a
 *       {@link ai.foresignal.raster.decode.DecodeRequest} and {@code decode}s a file into a
 *       {@link ai.foresignal.raster.decode.DecodedGrid};
 *   <li>{@link ai.foresignal.raster.decode.Grib2Decoder}: GRIB2 messages selected by discipline,
 *       category, number and level, never by message index, through {@code Grib2RecordScanner} (no
 *       {@code .gbx9} or {@code .ncx4} index is ever written);
 *   <li>{@link ai.foresignal.raster.decode.NetcdfDecoder}: NetCDF4/HDF5 variables in enhanced mode,
 *       with the GOES-R {@code goes_imager_projection} grid;
 *   <li>{@link ai.foresignal.raster.decode.SafeNetcdf}: the only place NetCDF-Java opens a file;
 *       paths only, under {@code rasters/tmp} or {@code rasters/selftest}, never a {@code String}
 *       (NetCDF-Java would accept a URL: SSRF);
 *   <li>{@link ai.foresignal.raster.decode.NetcdfRuntime}: pins NetCDF-Java's disk caches under
 *       {@code ${FORESIGNAL_DATA_DIR}/cache/netcdf};
 *   <li>{@link ai.foresignal.raster.decode.DecoderSelfTest}: decodes bundled fixtures for GRIB2
 *       templates 5.3, 5.41 and 5.42 and one NetCDF4 file at startup, and disables the products whose
 *       decoder failed.
 * </ul>
 *
 * <p>A missing field or variable is a {@link ai.foresignal.source.spi.SilentFormatChangeException}:
 * the source's health degrades and no frame is written.
 */
package ai.foresignal.raster.decode;
