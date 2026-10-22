package ai.foresignal.intel;

import ai.foresignal.raster.geotiff.GeoTiffException;
import ai.foresignal.raster.geotiff.GeoTiffReader;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Reads the population band of a GHS-POP GeoTIFF (prompt 101). Prompt 054's {@link GeoTiffReader}
 * decodes to RGBA8 only, so it cannot yield population counts; this reads the raw band 0 samples
 * through the JDK TIFF plugin, rejecting BigTIFF and enforcing EPSG:4326 through the 054 reader's
 * header inspection, and hands one {@code (lon, lat, value)} per pixel center to a {@link PixelSink}.
 *
 * <p>Pixels are <strong>streamed</strong>: the raster is decoded in strips of {@value #STRIP_ROWS}
 * rows, so memory is bounded by one strip rather than the whole tile.
 */
public final class PopulationTiffReader {

    /** Rows decoded per strip. */
    static final int STRIP_ROWS = 256;

    private PopulationTiffReader() {}

    /** One pixel's center and population value. */
    public record Pixel(double lon, double lat, double value) {}

    /** Receives each valid pixel as it is decoded. */
    @FunctionalInterface
    public interface PixelSink {

        /** One valid pixel center and its value. */
        void accept(double lon, double lat, double value);
    }

    /**
     * Streams every valid pixel center to {@code sink}.
     *
     * @return how many pixels were passed on
     * @throws GeoTiffException when the file is BigTIFF, not EPSG:4326/3857, rotated or oversized
     */
    public static long read(Path file, PixelSink sink) throws IOException, GeoTiffException {
        GeoTiffReader.Header header = GeoTiffReader.inspect(Files.readAllBytes(file));
        var georef = header.georeference();
        if (georef == null) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "no georeferencing");
        }
        Double nodata = header.nodata();
        long passed = 0;
        try (ImageInputStream input = ImageIO.createImageInputStream(file.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("No TIFF reader for " + file);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                int width = Math.min(header.width(), reader.getWidth(0));
                int height = Math.min(header.height(), reader.getHeight(0));
                for (int top = 0; top < height; top += STRIP_ROWS) {
                    int rows = Math.min(STRIP_ROWS, height - top);
                    ImageReadParam param = reader.getDefaultReadParam();
                    param.setSourceRegion(new Rectangle(0, top, width, rows));
                    Raster strip = reader.readRaster(0, param);
                    int stripWidth = Math.min(width, strip.getWidth());
                    int stripRows = Math.min(rows, strip.getHeight());
                    for (int r = 0; r < stripRows; r++) {
                        for (int col = 0; col < stripWidth; col++) {
                            double value = strip.getSampleDouble(strip.getMinX() + col, strip.getMinY() + r, 0);
                            if (Double.isNaN(value) || (nodata != null && value == nodata) || value < 0) {
                                continue;
                            }
                            double[] lonLat = georef.lonLat(col + 0.5, top + r + 0.5);
                            sink.accept(lonLat[0], lonLat[1], value);
                            passed++;
                        }
                    }
                }
            } finally {
                reader.dispose();
            }
        }
        return passed;
    }

    /**
     * Every valid pixel center as a list (small files and tests only; imports use
     * {@link #read(Path, PixelSink)}).
     */
    public static List<Pixel> read(Path file) throws IOException, GeoTiffException {
        List<Pixel> pixels = new ArrayList<>();
        read(file, (lon, lat, value) -> pixels.add(new Pixel(lon, lat, value)));
        return pixels;
    }
}
