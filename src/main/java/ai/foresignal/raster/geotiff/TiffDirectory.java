package ai.foresignal.raster.geotiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The first image file directory of a classic TIFF (TIFF 6.0), read from the header bytes without
 * decoding a pixel (prompt 054).
 *
 * <p>Only what the GeoTIFF reader needs is kept: every entry's type, count and raw value bytes, read
 * as integers, doubles or ASCII on demand. Offsets and counts are bounds-checked against the file, so
 * a hostile header cannot make the parser read outside the bytes or allocate more than
 * {@value #MAX_VALUES} values per tag. A BigTIFF (magic 43) is refused with
 * {@link GeoTiffException.Kind#BIGTIFF}.
 */
public final class TiffDirectory {

    /** {@code ImageWidth}. */
    public static final int IMAGE_WIDTH = 256;

    /** {@code ImageLength}. */
    public static final int IMAGE_LENGTH = 257;

    /** {@code BitsPerSample}. */
    public static final int BITS_PER_SAMPLE = 258;

    /** {@code PhotometricInterpretation}. */
    public static final int PHOTOMETRIC = 262;

    /** {@code SamplesPerPixel}. */
    public static final int SAMPLES_PER_PIXEL = 277;

    /** {@code ExtraSamples}. */
    public static final int EXTRA_SAMPLES = 338;

    /** {@code SampleFormat}. */
    public static final int SAMPLE_FORMAT = 339;

    /** GeoTIFF {@code ModelPixelScaleTag}. */
    public static final int MODEL_PIXEL_SCALE = 33550;

    /** GeoTIFF {@code ModelTiepointTag}. */
    public static final int MODEL_TIEPOINT = 33922;

    /** GeoTIFF {@code ModelTransformationTag}. */
    public static final int MODEL_TRANSFORMATION = 34264;

    /** GeoTIFF {@code GeoKeyDirectoryTag}. */
    public static final int GEO_KEY_DIRECTORY = 34735;

    /** GeoTIFF {@code GeoDoubleParamsTag}. */
    public static final int GEO_DOUBLE_PARAMS = 34736;

    /** GeoTIFF {@code GeoAsciiParamsTag}. */
    public static final int GEO_ASCII_PARAMS = 34737;

    /** GDAL's {@code GDAL_NODATA} (ASCII). */
    public static final int GDAL_NODATA = 42113;

    /** The most values read from one tag. */
    public static final int MAX_VALUES = 1_000_000;

    /** The most entries in the directory. */
    public static final int MAX_ENTRIES = 4096;

    private static final int[] TYPE_SIZES = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

    /**
     * One directory entry.
     *
     * @param type the TIFF field type (1–12)
     * @param count the number of values
     * @param value the raw value bytes, in the file's byte order
     */
    record Entry(int type, int count, byte[] value) {}

    private final ByteOrder order;
    private final Map<Integer, Entry> entries;

    private TiffDirectory(ByteOrder order, Map<Integer, Entry> entries) {
        this.order = order;
        this.entries = entries;
    }

    /**
     * Parses the first image file directory.
     *
     * @param bytes the file, or at least its header and first directory
     * @throws GeoTiffException {@code BIGTIFF} for magic 43, {@code MALFORMED} for anything that is not a
     *     readable classic TIFF
     */
    public static TiffDirectory parse(byte[] bytes) throws GeoTiffException {
        if (bytes == null || bytes.length < 8) {
            throw malformed("The file is too short to be a TIFF.");
        }
        ByteOrder order;
        if (bytes[0] == 'I' && bytes[1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (bytes[0] == 'M' && bytes[1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            throw malformed("The file is not a TIFF.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(order);
        int magic = Short.toUnsignedInt(buffer.getShort(2));
        if (magic == 43) {
            throw new GeoTiffException(GeoTiffException.Kind.BIGTIFF, GeoTiffException.BIGTIFF_REASON);
        }
        if (magic != 42) {
            throw malformed("The file is not a TIFF (magic " + magic + ").");
        }
        long ifd = Integer.toUnsignedLong(buffer.getInt(4));
        if (ifd < 8 || ifd + 2 > bytes.length) {
            throw malformed("The TIFF's first directory lies outside the file.");
        }
        int count = Short.toUnsignedInt(buffer.getShort((int) ifd));
        if (count == 0 || count > MAX_ENTRIES || ifd + 2 + 12L * count > bytes.length) {
            throw malformed("The TIFF's first directory is truncated or implausible (" + count + " entries).");
        }
        Map<Integer, Entry> entries = new LinkedHashMap<>();
        for (int k = 0; k < count; k++) {
            int at = (int) ifd + 2 + 12 * k;
            int tag = Short.toUnsignedInt(buffer.getShort(at));
            int type = Short.toUnsignedInt(buffer.getShort(at + 2));
            long n = Integer.toUnsignedLong(buffer.getInt(at + 4));
            if (type < 1 || type >= TYPE_SIZES.length) {
                continue; // unknown types are skipped, as TIFF 6.0 requires
            }
            if (n > MAX_VALUES) {
                throw malformed("TIFF tag " + tag + " declares " + n + " values.");
            }
            int size = (int) n * TYPE_SIZES[type];
            byte[] value = new byte[size];
            if (size <= 4) {
                System.arraycopy(bytes, at + 8, value, 0, size);
            } else {
                long offset = Integer.toUnsignedLong(buffer.getInt(at + 8));
                if (offset + size > bytes.length) {
                    throw malformed("TIFF tag " + tag + " points outside the file.");
                }
                System.arraycopy(bytes, (int) offset, value, 0, size);
            }
            entries.put(tag, new Entry(type, (int) n, value));
        }
        return new TiffDirectory(order, Collections.unmodifiableMap(entries));
    }

    /** True when the directory has {@code tag}. */
    public boolean has(int tag) {
        return entries.containsKey(tag);
    }

    /** The byte order of the file. */
    public ByteOrder order() {
        return order;
    }

    /**
     * The values of an integer tag (BYTE, SHORT, LONG and their signed forms), or null when absent or
     * not integer-typed.
     */
    public long[] longs(int tag) {
        Entry e = entries.get(tag);
        if (e == null) {
            return null;
        }
        ByteBuffer b = ByteBuffer.wrap(e.value()).order(order);
        long[] out = new long[e.count()];
        for (int i = 0; i < out.length; i++) {
            out[i] = switch (e.type()) {
                case 1, 7 -> Byte.toUnsignedInt(b.get(i));
                case 6 -> b.get(i);
                case 3 -> Short.toUnsignedInt(b.getShort(2 * i));
                case 8 -> b.getShort(2 * i);
                case 4 -> Integer.toUnsignedLong(b.getInt(4 * i));
                case 9 -> b.getInt(4 * i);
                default -> Long.MIN_VALUE;
            };
            if (out[i] == Long.MIN_VALUE) {
                return null;
            }
        }
        return out;
    }

    /** The first value of an integer tag, or {@code fallback}. */
    public long first(int tag, long fallback) {
        long[] values = longs(tag);
        return values == null || values.length == 0 ? fallback : values[0];
    }

    /** The values of a numeric tag as doubles (any numeric type), or null when absent. */
    public double[] doubles(int tag) {
        Entry e = entries.get(tag);
        if (e == null) {
            return null;
        }
        ByteBuffer b = ByteBuffer.wrap(e.value()).order(order);
        double[] out = new double[e.count()];
        for (int i = 0; i < out.length; i++) {
            out[i] = switch (e.type()) {
                case 11 -> b.getFloat(4 * i);
                case 12 -> b.getDouble(8 * i);
                case 5 -> Integer.toUnsignedLong(b.getInt(8 * i)) / (double) Math.max(1, Integer.toUnsignedLong(b.getInt(8 * i + 4)));
                case 10 -> b.getInt(8 * i) / (double) (b.getInt(8 * i + 4) == 0 ? 1 : b.getInt(8 * i + 4));
                default -> Double.NaN;
            };
            if (Double.isNaN(out[i]) && e.type() != 11 && e.type() != 12) {
                long[] ints = longs(tag);
                if (ints == null) {
                    return null;
                }
                double[] converted = new double[ints.length];
                for (int k = 0; k < ints.length; k++) {
                    converted[k] = ints[k];
                }
                return converted;
            }
        }
        return out;
    }

    /** The text of an ASCII tag without its trailing NULs, or null when absent. */
    public String ascii(int tag) {
        Entry e = entries.get(tag);
        if (e == null || e.type() != 2) {
            return null;
        }
        String text = new String(e.value(), StandardCharsets.US_ASCII);
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == 0) {
            end--;
        }
        return text.substring(0, end);
    }

    private static GeoTiffException malformed(String reason) {
        return new GeoTiffException(GeoTiffException.Kind.MALFORMED, reason);
    }
}
