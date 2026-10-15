package ai.foresignal.intel;

import ai.foresignal.raster.geotiff.GeoTiffException;
import ai.foresignal.raster.geotiff.GeoTiffReader;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.locationtech.jts.geom.Coordinate;

/**
 * Reads the population band of a GHS-POP GeoTIFF (prompt 101). Prompt 054's {@link GeoTiffReader}
 * decodes to RGBA8 only, so it cannot yield population counts; this reads the raw band 0 samples
 * through the JDK TIFF plugin, rejecting BigTIFF and enforcing EPSG:4326 through the 054 reader's
 * header inspection, and returns one {@code (lon, lat, value)} per pixel centre.
 */
public final class PopulationTiffReader {

    private PopulationTiffReader() {}

    /** One pixel's centre and population value. */
    public record Pixel(double lon, double lat, double value) {}

    /**
     * Reads every valid pixel centre.
     *
     * @throws GeoTiffException when the file is BigTIFF, not EPSG:4326/3857, rotated or oversized
     */
    public static List<Pixel> read(Path file) throws IOException, GeoTiffException {
        byte[] bytes = Files.readAllBytes(file);
        GeoTiffReader.Header header = GeoTiffReader.inspect(bytes);
        var georef = header.georeference();
        if (georef == null) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "no georeferencing");
        }
        Raster raster = rawRaster(file);
        int width = Math.min(header.width(), raster.getWidth());
        int height = Math.min(header.height(), raster.getHeight());
        List<Pixel> pixels = new ArrayList<>(Math.min(width * height, 1 << 22));
        Double nodata = header.nodata();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double value = raster.getSampleDouble(col, row, 0);
                if (Double.isNaN(value) || (nodata != null && value == nodata) || value < 0) {
                    continue;
                }
                double[] lonLat = georef.lonLat(col + 0.5, row + 0.5);
                pixels.add(new Pixel(lonLat[0], lonLat[1], value));
            }
        }
        return pixels;
    }

    private static Raster rawRaster(Path file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("No TIFF reader for " + file);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                return reader.readRaster(0, null);
            } finally {
                reader.dispose();
            }
        }
    }
}
