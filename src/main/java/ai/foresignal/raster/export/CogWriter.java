package ai.foresignal.raster.export;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Deflater;

/**
 * A minimal Cloud Optimized GeoTIFF writer (prompt 108; Weather-Imagery-Design §11): a classic TIFF
 * with tiled 512×512 blocks, deflate compression, internal factor-2 overviews until a level is below
 * 512 px, and every IFD (with every out-of-line tag value) written before the tile data (the COG
 * layout). Tiles carry physical float32 values; {@code ModelPixelScale}/{@code ModelTiepoint}/
 * {@code GeoKeyDirectory} georeference EPSG:4326 or EPSG:3857, {@code GDAL_NODATA} marks the no-data
 * value, and the full-resolution IFD carries {@code Copyright} (attribution and license) and
 * {@code GDAL_METADATA} when given.
 *
 * <p>Tag values follow TIFF 6.0 §2: a value whose bytes fit in four is stored inline in the entry
 * (left-justified), anything longer is stored elsewhere and the entry holds its offset.
 */
public final class CogWriter {

    /** The tile side, pixels. */
    public static final int TILE = 512;

    /** The Adobe Deflate compression code. */
    private static final int COMPRESSION_DEFLATE = 8;

    private static final int ASCII = 2;
    private static final int SHORT = 3;
    private static final int LONG = 4;
    private static final int DOUBLE = 12;

    private CogWriter() {}

    /**
     * Writes a tiled float32 GeoTIFF without a copyright tag.
     *
     * @see #write(float[], int, int, double[], int, double, String, String)
     */
    public static byte[] write(float[] values, int width, int height, double[] bounds, int epsg, double nodata,
            String gdalMetadata) {
        return write(values, width, height, bounds, epsg, nodata, gdalMetadata, null);
    }

    /**
     * Writes a tiled float32 GeoTIFF.
     *
     * @param values physical values, row-major north to south, {@code width × height}
     * @param width columns
     * @param height rows
     * @param bounds {@code [west, south, east, north]} in the CRS (degrees for 4326, meters for 3857)
     * @param epsg 4326 or 3857
     * @param nodata the no-data value (also written to {@code GDAL_NODATA})
     * @param gdalMetadata the {@code GDAL_METADATA} XML document, or null
     * @param copyright the {@code Copyright} text (attribution and license), or null; reduced to ASCII
     */
    public static byte[] write(float[] values, int width, int height, double[] bounds, int epsg, double nodata,
            String gdalMetadata, String copyright) {
        if (epsg != 4326 && epsg != 3857) {
            throw new IllegalArgumentException("Only EPSG:4326 and EPSG:3857 are supported, not " + epsg);
        }
        List<Level> levels = levels(values, width, height, nodata);
        for (Level level : levels) {
            level.tiles = encodeTiles(level.values, level.width, level.height, nodata);
        }
        double scaleX = (bounds[2] - bounds[0]) / width;
        double scaleY = (bounds[3] - bounds[1]) / height;
        int count = levels.size();

        // Pass 1: the layout only depends on the sizes of the tag values, not on the offsets they hold.
        long[] ifdOffset = new long[count];
        long[] externalOffset = new long[count];
        long cursor = 8;
        for (int i = 0; i < count; i++) {
            Level level = levels.get(i);
            int n = level.tilesAcross() * level.tilesDown();
            List<Tag> tags = tags(level, i, epsg, bounds, scaleX, scaleY, nodata, gdalMetadata, copyright, new long[n],
                    new long[n]);
            ifdOffset[i] = cursor;
            cursor += 2L + tags.size() * 12L + 4L;
            externalOffset[i] = cursor;
            cursor += externalBytes(tags);
        }
        long tileDataStart = cursor;
        long[] levelTileStart = new long[count];
        for (int i = 0; i < count; i++) {
            levelTileStart[i] = cursor;
            for (byte[] tile : levels.get(i).tiles) {
                cursor += tile.length;
            }
        }
        if (cursor > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("A classic TIFF cannot exceed 2 GiB here; shrink the export");
        }

        // Pass 2: write with the real tile offsets.
        ByteBuffer out = ByteBuffer.allocate((int) cursor).order(ByteOrder.LITTLE_ENDIAN);
        out.put((byte) 'I');
        out.put((byte) 'I');
        out.putShort((short) 42);
        out.putInt(8);
        for (int i = 0; i < count; i++) {
            Level level = levels.get(i);
            int n = level.tilesAcross() * level.tilesDown();
            long[] offsets = new long[n];
            long[] lengths = new long[n];
            long at = levelTileStart[i];
            for (int k = 0; k < n; k++) {
                offsets[k] = at;
                lengths[k] = level.tiles[k].length;
                at += lengths[k];
            }
            List<Tag> tags = tags(level, i, epsg, bounds, scaleX, scaleY, nodata, gdalMetadata, copyright, offsets,
                    lengths);
            writeIfd(out, tags, ifdOffset[i], externalOffset[i], i + 1 < count ? ifdOffset[i + 1] : 0);
        }
        out.position((int) tileDataStart);
        for (Level level : levels) {
            for (byte[] tile : level.tiles) {
                out.put(tile);
            }
        }
        return out.array();
    }

    /** The full-resolution level and its factor-2 overviews, until a level is below {@link #TILE} px. */
    static List<Level> levels(float[] values, int width, int height, double nodata) {
        List<Level> levels = new ArrayList<>();
        levels.add(new Level(values, width, height));
        int w = width;
        int h = height;
        float[] current = values;
        while (Math.max(w, h) >= TILE && Math.max(w, h) > 1) {
            float[] next = downsample(current, w, h, nodata);
            w = Math.max(1, (w + 1) / 2);
            h = Math.max(1, (h + 1) / 2);
            levels.add(new Level(next, w, h));
            current = next;
        }
        return levels;
    }

    /** One resolution level. */
    static final class Level {
        final float[] values;
        final int width;
        final int height;
        byte[][] tiles;

        Level(float[] values, int width, int height) {
            this.values = values;
            this.width = width;
            this.height = height;
        }

        int tilesAcross() {
            return (width + TILE - 1) / TILE;
        }

        int tilesDown() {
            return (height + TILE - 1) / TILE;
        }
    }

    /**
     * One IFD entry with its value already encoded little-endian.
     *
     * @param id the tag
     * @param type the TIFF field type
     * @param count the value count
     * @param payload the encoded value bytes
     */
    record Tag(int id, int type, int count, byte[] payload) {

        /** True when the value fits in the entry's four value bytes. */
        boolean inline() {
            return payload.length <= 4;
        }
    }

    private static List<Tag> tags(Level level, int index, int epsg, double[] bounds, double scaleX, double scaleY,
            double nodata, String gdalMetadata, String copyright, long[] tileOffsets, long[] tileLengths) {
        List<Tag> tags = new ArrayList<>();
        tags.add(longs(254, index == 0 ? 0 : 1));
        tags.add(longs(256, level.width));
        tags.add(longs(257, level.height));
        tags.add(shorts(258, 32));
        tags.add(shorts(259, COMPRESSION_DEFLATE));
        tags.add(shorts(262, 1));
        tags.add(shorts(277, 1));
        tags.add(shorts(284, 1));
        tags.add(longs(322, TILE));
        tags.add(longs(323, TILE));
        tags.add(longs(324, tileOffsets));
        tags.add(longs(325, tileLengths));
        tags.add(shorts(339, 3));
        tags.add(ascii(42113, trimNumber(nodata)));
        if (index == 0) {
            tags.add(doubles(33550, scaleX, scaleY, 0.0));
            tags.add(doubles(33922, 0.0, 0.0, 0.0, bounds[0], bounds[3], 0.0));
            tags.add(shorts(34735, geoKeys(epsg)));
            if (copyright != null && !copyright.isBlank()) {
                tags.add(ascii(33432, copyright));
            }
            if (gdalMetadata != null && !gdalMetadata.isBlank()) {
                tags.add(ascii(42112, gdalMetadata));
            }
        }
        tags.sort(Comparator.comparingInt(Tag::id));
        return tags;
    }

    /** Bytes of the out-of-line values of {@code tags}, each padded to an even offset. */
    private static long externalBytes(List<Tag> tags) {
        long size = 0;
        for (Tag tag : tags) {
            if (!tag.inline()) {
                size += tag.payload().length + (tag.payload().length & 1);
            }
        }
        return size;
    }

    private static void writeIfd(ByteBuffer out, List<Tag> tags, long ifdOffset, long externalOffset, long nextIfd) {
        out.position((int) ifdOffset);
        out.putShort((short) tags.size());
        long ext = externalOffset;
        List<long[]> pending = new ArrayList<>();
        for (int k = 0; k < tags.size(); k++) {
            Tag tag = tags.get(k);
            out.putShort((short) tag.id());
            out.putShort((short) tag.type());
            out.putInt(tag.count());
            if (tag.inline()) {
                byte[] value = new byte[4];
                System.arraycopy(tag.payload(), 0, value, 0, tag.payload().length);
                out.put(value);
            } else {
                out.putInt((int) ext);
                pending.add(new long[] {k, ext});
                ext += tag.payload().length + (tag.payload().length & 1);
            }
        }
        out.putInt((int) nextIfd);
        for (long[] p : pending) {
            out.position((int) p[1]);
            out.put(tags.get((int) p[0]).payload());
        }
    }

    private static Tag shorts(int id, int... values) {
        ByteBuffer b = ByteBuffer.allocate(values.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int v : values) {
            b.putShort((short) v);
        }
        return new Tag(id, SHORT, values.length, b.array());
    }

    private static Tag longs(int id, long... values) {
        ByteBuffer b = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (long v : values) {
            b.putInt((int) v);
        }
        return new Tag(id, LONG, values.length, b.array());
    }

    private static Tag doubles(int id, double... values) {
        ByteBuffer b = ByteBuffer.allocate(values.length * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (double v : values) {
            b.putDouble(v);
        }
        return new Tag(id, DOUBLE, values.length, b.array());
    }

    /** A NUL-terminated ASCII value; characters outside 7-bit ASCII lose their accents or become {@code ?}. */
    private static Tag ascii(int id, String text) {
        byte[] bytes = (toAscii(text) + "\0").getBytes(StandardCharsets.US_ASCII);
        return new Tag(id, ASCII, bytes.length, bytes);
    }

    /** {@code text} as 7-bit ASCII (TIFF {@code ASCII} fields): accents stripped, others replaced. */
    static String toAscii(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder out = new StringBuilder(decomposed.length());
        for (int k = 0; k < decomposed.length(); k++) {
            char c = decomposed.charAt(k);
            if (c == '©') {
                out.append("(c)");
            } else if (c >= 0x20 && c < 0x7f || c == '\n' || c == '\t') {
                out.append(c);
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }

    /** The GeoKeyDirectory: header {1, 1, 0, keys} then one {id, location 0, count 1, value} per key. */
    static int[] geoKeys(int epsg) {
        if (epsg == 3857) {
            return new int[] {1, 1, 0, 3,
                1024, 0, 1, 1, // GTModelTypeGeoKey = projected
                1025, 0, 1, 1, // GTRasterTypeGeoKey = PixelIsArea
                3072, 0, 1, 3857}; // ProjectedCSTypeGeoKey
        }
        return new int[] {1, 1, 0, 3,
            1024, 0, 1, 2, // GTModelTypeGeoKey = geographic
            1025, 0, 1, 1, // GTRasterTypeGeoKey = PixelIsArea
            2048, 0, 1, 4326}; // GeographicTypeGeoKey
    }

    /** Encodes the deflate tiles of one level. */
    private static byte[][] encodeTiles(float[] values, int width, int height, double nodata) {
        int across = (width + TILE - 1) / TILE;
        int down = (height + TILE - 1) / TILE;
        byte[][] out = new byte[across * down][];
        for (int ty = 0; ty < down; ty++) {
            for (int tx = 0; tx < across; tx++) {
                float[] block = new float[TILE * TILE];
                for (int y = 0; y < TILE; y++) {
                    int sy = ty * TILE + y;
                    for (int x = 0; x < TILE; x++) {
                        int sx = tx * TILE + x;
                        float v = (float) nodata;
                        if (sy < height && sx < width) {
                            v = values[sy * width + sx];
                        }
                        block[y * TILE + x] = v;
                    }
                }
                out[ty * across + tx] = deflate(block);
            }
        }
        return out;
    }

    /** Factor-2 average overview; a cell with no finite sample stays no-data. */
    static float[] downsample(float[] values, int width, int height, double nodata) {
        int nw = Math.max(1, (width + 1) / 2);
        int nh = Math.max(1, (height + 1) / 2);
        float[] out = new float[nw * nh];
        for (int y = 0; y < nh; y++) {
            for (int x = 0; x < nw; x++) {
                double sum = 0;
                int count = 0;
                for (int dy = 0; dy < 2; dy++) {
                    for (int dx = 0; dx < 2; dx++) {
                        int sx = x * 2 + dx;
                        int sy = y * 2 + dy;
                        if (sx < width && sy < height) {
                            float v = values[sy * width + sx];
                            if (Float.isFinite(v) && v != nodata) {
                                sum += v;
                                count++;
                            }
                        }
                    }
                }
                out[y * nw + x] = count == 0 ? (float) nodata : (float) (sum / count);
            }
        }
        return out;
    }

    private static byte[] deflate(float[] values) {
        ByteBuffer raw = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float v : values) {
            raw.putFloat(v);
        }
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(raw.array());
            deflater.finish();
            ByteArrayOutputStream out = new ByteArrayOutputStream(raw.array().length / 2);
            byte[] buffer = new byte[8192];
            while (!deflater.finished()) {
                int n = deflater.deflate(buffer);
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    private static String trimNumber(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v)) {
            return Long.toString((long) v);
        }
        return Double.toString(v);
    }
}
