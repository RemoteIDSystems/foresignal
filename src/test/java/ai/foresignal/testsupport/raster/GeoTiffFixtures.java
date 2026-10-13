package ai.foresignal.testsupport.raster;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds small classic TIFF and GeoTIFF files in memory for the prompt 054 tests: one uncompressed strip,
 * 8- or 16-bit samples, any GeoTIFF tags. Nothing here comes from SatDump; the default tags are those
 * SatDump 1.2.2 writes for an equirectangular composite projection (one tie point, a pixel scale,
 * {@code GTModelTypeGeoKey = 2}, {@code GeographicTypeGeoKey = 4326}).
 */
public final class GeoTiffFixtures {

    /** Samples of one pixel. */
    @FunctionalInterface
    public interface Pixels {

        /** The samples of pixel {@code (x, y)}, one per band. */
        int[] at(int x, int y);
    }

    private record Tag(int tag, int type, Object values) {}

    private int width = 8;
    private int height = 6;
    private int bits = 8;
    private int samples = 3;
    private boolean bigEndian;
    private boolean omitPixels;
    private Pixels pixels = (x, y) -> new int[] {10, 20, 30};
    private final List<Tag> tags = new ArrayList<>();

    private GeoTiffFixtures() {}

    /** A new 8 × 6 RGB builder without GeoTIFF tags. */
    public static GeoTiffFixtures tiff() {
        return new GeoTiffFixtures();
    }

    /**
     * A new builder with SatDump 1.2.2's EPSG:4326 tags: the tie point at raster (0, 0) maps to
     * {@code (west, north)} and each pixel is {@code scale} degrees.
     */
    public static GeoTiffFixtures wgs84(double west, double north, double scale) {
        return tiff().pixelScale(scale, scale).tiepoint(0, 0, west, north)
                .geoKeys(1024, 2, 2048, 4326, 2054, 9102);
    }

    /** The size. */
    public GeoTiffFixtures size(int w, int h) {
        this.width = w;
        this.height = h;
        return this;
    }

    /** Bits per sample (8 or 16) and samples per pixel (1–4). */
    public GeoTiffFixtures layout(int bitsPerSample, int samplesPerPixel) {
        this.bits = bitsPerSample;
        this.samples = samplesPerPixel;
        return this;
    }

    /** Writes big-endian ({@code MM}). */
    public GeoTiffFixtures bigEndian() {
        this.bigEndian = true;
        return this;
    }

    /** Writes no pixel data: the strip offset points past the end of the file (header-only tests). */
    public GeoTiffFixtures withoutPixels() {
        this.omitPixels = true;
        return this;
    }

    /** The pixel function. */
    public GeoTiffFixtures pixels(Pixels p) {
        this.pixels = p;
        return this;
    }

    /** {@code ModelPixelScaleTag}. */
    public GeoTiffFixtures pixelScale(double sx, double sy) {
        return doubles(33550, sx, sy, 0);
    }

    /** {@code ModelTiepointTag} for one tie point. */
    public GeoTiffFixtures tiepoint(double i, double j, double x, double y) {
        return doubles(33922, i, j, 0, x, y, 0);
    }

    /** {@code ModelTransformationTag}. */
    public GeoTiffFixtures transformation(double... sixteen) {
        return doubles(34264, sixteen);
    }

    /**
     * The GeoKeyDirectory with SHORT-valued keys given as {@code id, value} pairs (sorted here).
     */
    public GeoTiffFixtures geoKeys(int... idValue) {
        List<int[]> keys = new ArrayList<>();
        for (int k = 0; k + 1 < idValue.length; k += 2) {
            keys.add(new int[] {idValue[k], idValue[k + 1]});
        }
        keys.sort(Comparator.comparingInt(a -> a[0]));
        int[] dir = new int[4 + 4 * keys.size()];
        dir[0] = 1;
        dir[1] = 1;
        dir[2] = 0;
        dir[3] = keys.size();
        for (int k = 0; k < keys.size(); k++) {
            dir[4 + 4 * k] = keys.get(k)[0];
            dir[5 + 4 * k] = 0;
            dir[6 + 4 * k] = 1;
            dir[7 + 4 * k] = keys.get(k)[1];
        }
        tags.removeIf(t -> t.tag() == 34735);
        tags.add(new Tag(34735, 3, dir));
        return this;
    }

    /** {@code GDAL_NODATA}. */
    public GeoTiffFixtures nodata(String value) {
        tags.add(new Tag(42113, 2, value));
        return this;
    }

    /** Any DOUBLE tag. */
    public GeoTiffFixtures doubles(int tag, double... values) {
        tags.removeIf(t -> t.tag() == tag);
        tags.add(new Tag(tag, 12, values));
        return this;
    }

    /** Any SHORT tag. */
    public GeoTiffFixtures shorts(int tag, int... values) {
        tags.removeIf(t -> t.tag() == tag);
        tags.add(new Tag(tag, 3, values));
        return this;
    }

    /** Removes a tag. */
    public GeoTiffFixtures without(int tag) {
        tags.removeIf(t -> t.tag() == tag);
        return this;
    }

    /** A BigTIFF header ({@code II+\0}, offset size 8). */
    public static byte[] bigTiffHeader() {
        ByteBuffer b = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte) 'I').put((byte) 'I').putShort((short) 43).putShort((short) 8).putShort((short) 0).putLong(16);
        return b.array();
    }

    /** The file. */
    public byte[] build() {
        ByteOrder order = bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        int bytesPerSample = bits / 8;
        long dataLength = (long) width * height * samples * bytesPerSample;
        byte[] data;
        if (omitPixels) {
            data = new byte[0];
        } else {
            ByteBuffer d = ByteBuffer.allocate((int) dataLength).order(order);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int[] s = pixels.at(x, y);
                    for (int b = 0; b < samples; b++) {
                        if (bytesPerSample == 1) {
                            d.put((byte) s[b]);
                        } else {
                            d.putShort((short) s[b]);
                        }
                    }
                }
            }
            data = d.array();
        }
        List<Tag> all = new ArrayList<>();
        all.add(new Tag(256, 4, new int[] {width}));
        all.add(new Tag(257, 4, new int[] {height}));
        int[] bps = new int[samples];
        java.util.Arrays.fill(bps, bits);
        all.add(new Tag(258, 3, bps));
        all.add(new Tag(259, 3, new int[] {1}));
        all.add(new Tag(262, 3, new int[] {samples >= 3 ? 2 : 1}));
        all.add(new Tag(273, 4, new int[] {omitPixels ? 1_000_000 : 8}));
        all.add(new Tag(277, 3, new int[] {samples}));
        all.add(new Tag(278, 4, new int[] {height}));
        all.add(new Tag(279, 4, new int[] {(int) Math.min(Integer.MAX_VALUE, dataLength)}));
        all.add(new Tag(284, 3, new int[] {1}));
        if (samples == 2 || samples == 4) {
            all.add(new Tag(338, 3, new int[] {2}));
        }
        all.addAll(tags);
        all.sort(Comparator.comparingInt(Tag::tag));

        int ifdOffset = 8 + data.length;
        ifdOffset += ifdOffset % 2;
        int extraOffset = ifdOffset + 2 + 12 * all.size() + 4;
        ByteArrayOutputStream extra = new ByteArrayOutputStream();
        ByteBuffer ifd = ByteBuffer.allocate(2 + 12 * all.size() + 4).order(order);
        ifd.putShort((short) all.size());
        for (Tag t : all) {
            byte[] payload = payload(t, order);
            int count = switch (t.type()) {
                case 2 -> payload.length;
                case 3 -> ((int[]) t.values()).length;
                case 4 -> ((int[]) t.values()).length;
                default -> ((double[]) t.values()).length;
            };
            ifd.putShort((short) t.tag()).putShort((short) t.type()).putInt(count);
            if (payload.length <= 4) {
                byte[] padded = java.util.Arrays.copyOf(payload, 4);
                ifd.put(padded);
            } else {
                ifd.putInt(extraOffset + extra.size());
                extra.writeBytes(payload);
                if (extra.size() % 2 == 1) {
                    extra.write(0);
                }
            }
        }
        ifd.putInt(0);
        ByteBuffer header = ByteBuffer.allocate(8).order(order);
        header.put(bigEndian ? (byte) 'M' : (byte) 'I').put(bigEndian ? (byte) 'M' : (byte) 'I').putShort((short) 42)
                .putInt(ifdOffset);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(header.array());
        out.writeBytes(data);
        while (out.size() < ifdOffset) {
            out.write(0);
        }
        out.writeBytes(ifd.array());
        out.writeBytes(extra.toByteArray());
        return out.toByteArray();
    }

    private static byte[] payload(Tag t, ByteOrder order) {
        return switch (t.type()) {
            case 2 -> (((String) t.values()) + "\0").getBytes(StandardCharsets.US_ASCII);
            case 3 -> {
                int[] v = (int[]) t.values();
                ByteBuffer b = ByteBuffer.allocate(2 * v.length).order(order);
                for (int x : v) {
                    b.putShort((short) x);
                }
                yield b.array();
            }
            case 4 -> {
                int[] v = (int[]) t.values();
                ByteBuffer b = ByteBuffer.allocate(4 * v.length).order(order);
                for (int x : v) {
                    b.putInt(x);
                }
                yield b.array();
            }
            default -> {
                double[] v = (double[]) t.values();
                ByteBuffer b = ByteBuffer.allocate(8 * v.length).order(order);
                for (double x : v) {
                    b.putDouble(x);
                }
                yield b.array();
            }
        };
    }
}
