package ai.foresignal.raster.geotiff;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/**
 * A categorical GeoTIFF's raw band-0 values (prompt 106, {@code wpc-heatrisk}): the palette index of
 * every pixel, which {@link GeoTiffReader#read} would otherwise render to RGBA. The georeference comes
 * from {@link GeoTiffReader#inspect}; the samples come from the JDK TIFF plugin.
 */
public final class GeoTiffValues {

    private GeoTiffValues() {}

    /**
     * The values.
     *
     * @param samples band-0 samples, row-major as stored
     * @param width columns
     * @param height rows
     * @param georeference the georeference
     * @param nodata the GDAL nodata value, or null
     */
    public record Values(int[] samples, int width, int height, GeoReference georeference, Double nodata) {}

    /** Reads the band-0 values, refusing anything {@link GeoTiffReader#inspect} refuses. */
    public static Values read(byte[] bytes) throws GeoTiffException {
        GeoTiffReader.Header header = GeoTiffReader.inspect(bytes);
        // the TIFF reader runs only after inspect has applied the image guard's pixel and side limits
        BufferedImage image = GeoTiffReader.decode(bytes);
        Raster raster = image.getRaster();
        int width = image.getWidth();
        int height = image.getHeight();
        int[] samples = new int[width * height];
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            raster.getSamples(0, y, width, 1, 0, row);
            System.arraycopy(row, 0, samples, y * width, width);
        }
        return new Values(samples, width, height, header.georeference(), header.nodata());
    }
}
