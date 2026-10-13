package ai.foresignal.raster.geotiff;

import ai.foresignal.media.ImageGuard;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * Reads a SatDump polar GeoTIFF into packed RGBA pixels and its {@link GeoReference} (prompt 054,
 * Weather-Imagery-Design §3 "GeoTIFF read" and §4.5 item 2).
 *
 * <p>Order, so nothing large is decoded before it is judged:
 *
 * <ol>
 *   <li>the header ({@link TiffDirectory}): a BigTIFF is refused with
 *       {@value GeoTiffException#BIGTIFF_REASON};
 *   <li>the image guard's limits from the header's dimensions only: more than
 *       {@link ImageGuard#MAX_PIXELS} pixels or a side over {@link ImageGuard#MAX_SIDE} px is refused;
 *   <li>the layout: 8- or 16-bit unsigned samples, 1 (gray), 2 (gray and alpha), 3 (RGB) or 4 (RGBA)
 *       per pixel;
 *   <li>the georeferencing ({@link GeoKeyParser}): EPSG:4326 or EPSG:3857 only;
 *   <li>only then the pixels, with the JDK {@code javax.imageio} TIFF plugin.
 * </ol>
 *
 * <p>Pixels come out as {@code 0xRRGGBBAA} (the {@code rgba8} dtype), rows north to south as the file
 * stores them. 16-bit samples are scaled to 8 bits. Without an alpha channel, a pixel is transparent
 * when every band equals the GDAL nodata value ({@code GDAL_NODATA}, tag 42113) or, without one, when
 * every band is zero (SatDump's fill outside the swath).
 */
public final class GeoTiffReader {

    /**
     * The header facts, read without decoding pixels.
     *
     * @param width columns
     * @param height rows
     * @param bitsPerSample 8 or 16
     * @param samplesPerPixel 1 to 4
     * @param georeference where the pixels lie
     * @param nodata the GDAL nodata value, or null
     */
    public record Header(int width, int height, int bitsPerSample, int samplesPerPixel, GeoReference georeference,
            Double nodata) {}

    /**
     * A decoded GeoTIFF.
     *
     * @param width columns
     * @param height rows
     * @param rgba one packed {@code 0xRRGGBBAA} pixel per cell, row-major, rows as stored
     * @param georeference where the pixels lie
     * @param header the header facts
     */
    public record Image(int width, int height, int[] rgba, GeoReference georeference, Header header) {

        /** The number of pixels with a non-zero alpha. */
        public long validPixels() {
            long n = 0;
            for (int p : rgba) {
                if ((p & 0xFF) != 0) {
                    n++;
                }
            }
            return n;
        }
    }

    private GeoTiffReader() {}

    /**
     * Reads the header and judges the file without decoding a pixel.
     *
     * @throws GeoTiffException when the file is refused
     */
    public static Header inspect(byte[] bytes) throws GeoTiffException {
        TiffDirectory dir = TiffDirectory.parse(bytes);
        long width = dir.first(TiffDirectory.IMAGE_WIDTH, -1);
        long height = dir.first(TiffDirectory.IMAGE_LENGTH, -1);
        guard(width, height);
        int samples = (int) dir.first(TiffDirectory.SAMPLES_PER_PIXEL, 1);
        long[] bits = dir.longs(TiffDirectory.BITS_PER_SAMPLE);
        int bitDepth = bits == null || bits.length == 0 ? 1 : (int) bits[0];
        if (bits != null) {
            for (long b : bits) {
                if (b != bitDepth) {
                    throw layout("GeoTIFF bands have different bit depths");
                }
            }
        }
        if (bitDepth != 8 && bitDepth != 16) {
            throw layout("GeoTIFF has " + bitDepth + "-bit samples; only 8 and 16 bits are read");
        }
        if (samples < 1 || samples > 4) {
            throw layout("GeoTIFF has " + samples + " samples per pixel; only 1 to 4 are read");
        }
        long format = dir.first(TiffDirectory.SAMPLE_FORMAT, 1);
        if (format != 1) {
            throw layout("GeoTIFF samples are not unsigned integers (SampleFormat " + format + ")");
        }
        GeoReference georeference = GeoKeyParser.parse(dir);
        return new Header((int) width, (int) height, bitDepth, samples, georeference, nodata(dir));
    }

    /**
     * Reads and decodes the file.
     *
     * @throws GeoTiffException when the file is refused or cannot be decoded
     */
    public static Image read(byte[] bytes) throws GeoTiffException {
        Header header = inspect(bytes);
        BufferedImage image = decode(bytes);
        if (image.getWidth() != header.width() || image.getHeight() != header.height()) {
            throw new GeoTiffException(GeoTiffException.Kind.MALFORMED, "GeoTIFF decoded to " + image.getWidth() + " x "
                    + image.getHeight() + " px, not the header's " + header.width() + " x " + header.height());
        }
        return new Image(header.width(), header.height(), rgba(image.getRaster(), header), header.georeference(), header);
    }

    /** Refuses what the image guard refuses, from the dimensions alone. */
    static void guard(long width, long height) throws GeoTiffException {
        if (width <= 0 || height <= 0) {
            throw new GeoTiffException(GeoTiffException.Kind.MALFORMED, "GeoTIFF header declares " + width + " x "
                    + height + " px");
        }
        long pixels = width * height;
        if (pixels > ImageGuard.MAX_PIXELS) {
            throw new GeoTiffException(GeoTiffException.Kind.OVERSIZE, String.format(Locale.ROOT,
                    "GeoTIFF %d x %d px is %.1f megapixels, over the %d megapixel limit", width, height, pixels / 1e6,
                    ImageGuard.MAX_PIXELS / 1_000_000));
        }
        if (width > ImageGuard.MAX_SIDE || height > ImageGuard.MAX_SIDE) {
            throw new GeoTiffException(GeoTiffException.Kind.OVERSIZE, "GeoTIFF " + width + " x " + height
                    + " px has a side over the " + ImageGuard.MAX_SIDE + " px limit");
        }
    }

    private static BufferedImage decode(byte[] bytes) throws GeoTiffException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("tiff");
        if (!readers.hasNext()) {
            throw new GeoTiffException(GeoTiffException.Kind.MALFORMED, "No TIFF reader is available");
        }
        ImageReader reader = readers.next();
        try (ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            reader.setInput(in, false, true);
            BufferedImage image = reader.read(0);
            if (image == null) {
                throw new GeoTiffException(GeoTiffException.Kind.MALFORMED, "GeoTIFF decoded to nothing");
            }
            return image;
        } catch (IOException | RuntimeException e) {
            throw new GeoTiffException(GeoTiffException.Kind.MALFORMED, "GeoTIFF pixels could not be decoded: "
                    + e.getMessage(), 0, e);
        } finally {
            reader.dispose();
        }
    }

    /** Packs the raster's samples as {@code 0xRRGGBBAA}. */
    static int[] rgba(Raster raster, Header header) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        int bands = Math.min(raster.getNumBands(), header.samplesPerPixel());
        int shift = header.bitsPerSample() == 16 ? 8 : 0;
        boolean hasAlpha = bands == 2 || bands == 4;
        int colorBands = hasAlpha ? bands - 1 : bands;
        Double nodata = header.nodata();
        int[] out = new int[w * h];
        int[][] rows = new int[bands][];
        for (int y = 0; y < h; y++) {
            for (int b = 0; b < bands; b++) {
                rows[b] = raster.getSamples(0, y, w, 1, b, rows[b]);
            }
            for (int x = 0; x < w; x++) {
                int r;
                int g;
                int bl;
                if (colorBands >= 3) {
                    r = rows[0][x] >>> shift;
                    g = rows[1][x] >>> shift;
                    bl = rows[2][x] >>> shift;
                } else {
                    r = rows[0][x] >>> shift;
                    g = r;
                    bl = r;
                }
                int a;
                if (hasAlpha) {
                    a = rows[bands - 1][x] >>> shift;
                } else if (nodata != null) {
                    boolean all = true;
                    for (int b = 0; b < colorBands && all; b++) {
                        all = rows[b][x] == nodata;
                    }
                    a = all ? 0 : 255;
                } else {
                    boolean zero = true;
                    for (int b = 0; b < colorBands && zero; b++) {
                        zero = rows[b][x] == 0;
                    }
                    a = zero ? 0 : 255;
                }
                out[y * w + x] = (r & 0xFF) << 24 | (g & 0xFF) << 16 | (bl & 0xFF) << 8 | (a & 0xFF);
            }
        }
        return out;
    }

    private static Double nodata(TiffDirectory dir) {
        String text = dir.ascii(TiffDirectory.GDAL_NODATA);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double v = Double.parseDouble(text.strip());
            return Double.isFinite(v) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static GeoTiffException layout(String reason) {
        return new GeoTiffException(GeoTiffException.Kind.UNSUPPORTED_LAYOUT, reason);
    }
}
