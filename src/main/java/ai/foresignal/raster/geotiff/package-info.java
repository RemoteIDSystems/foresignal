/**
 * The GeoTIFF reader of SatDump polar overlays (prompt 054, Weather-Imagery-Design §3 "GeoTIFF read" and
 * §4.5 item 2).
 *
 * <p>Foresignal only <em>reads</em> GeoTIFFs that SatDump wrote into a pass folder; it never reimplements
 * SatDump's projection. Stateless readers, like the decoders in {@code raster.decode}, so the SatDump
 * adapter may call them directly (Architecture §3):
 *
 * <ul>
 *   <li>{@link ai.foresignal.raster.geotiff.TiffDirectory}: the first image file directory of a classic
 *       TIFF, parsed from the header bytes; a BigTIFF (magic 43) is refused;
 *   <li>{@link ai.foresignal.raster.geotiff.GeoKeyParser}: ModelPixelScale (33550), ModelTiepoint (33922),
 *       ModelTransformation (34264, axis-aligned only) and the GeoKeyDirectory (34735); EPSG:4326 and
 *       EPSG:3857 only, with the PixelIsPoint half-pixel shift;
 *   <li>{@link ai.foresignal.raster.geotiff.GeoTiffReader}: the image guard's limits checked from the
 *       header, then the JDK {@code javax.imageio} TIFF plugin, giving packed RGBA pixels;
 *   <li>{@link ai.foresignal.raster.geotiff.FootprintBuilder}: the footprint from the alpha mask,
 *       simplified, fixed and split at the antimeridian ({@link ai.foresignal.raster.geotiff.Antimeridian});
 *   <li>{@link ai.foresignal.raster.geotiff.GdalWarp}: the optional {@code gdalwarp} subprocess
 *       ({@code WITH_GDAL}) for other CRS and BigTIFF.
 * </ul>
 */
package ai.foresignal.raster.geotiff;
