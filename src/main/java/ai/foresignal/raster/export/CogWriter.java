package ai.foresignal.raster.export;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

/**
 * A minimal Cloud Optimized GeoTIFF writer (prompt 108; Weather-Imagery-Design §11): a classic
 * (big-)TIFF with tiled 512×512 blocks, deflate compression, internal factor-2 overviews down to
 * below 512 px, and every IFD written before the tile data (the COG layout). Tiles carry physical
 * float32 values; {@code ModelPixelScale}/{@code ModelTiepoint}/{@code GeoKeyDirectory} georeference
 * EPSG:4326 or EPSG:3857 and {@code GDAL_NODATA} marks the no-data value.
 */
public final class CogWriter {

    /** The tile side, pixels. */
    public static final int TILE = 512;

    /** The Adobe Deflate compression code. */
    private static final int COMPRESSION_DEFLATE = 8;

    private CogWriter() {}

    /**
     * Writes a tiled float32 GeoTIFF.
     *
     * @param values physical values, row-major north to south, {@code width × height}
     * @param width columns
     * @param height rows
     * @param bounds {@code [west, south, east, north]} in the CRS
     * @param epsg 4326 or 3857
     * @param nodata the no-data value (also written to {@code GDAL_NODATA})
     * @param gdalMetadata extra {@code GDAL_METADATA} XML (scale/offset), or null
     */
    public static byte[] write(float[] values, int width, int height, double[] bounds, int epsg, double nodata,
            String gdalMetadata) {
        List<Level> levels = new ArrayList<>();
        levels.add(new Level(values, width, height));
        int w = width;
        int h = height;
        float[] current = values;
        while (Math.min(w, h) >= 2 * TILE) {
            float[] next = downsample(current, w, h, nodata);
            w = Math.max(1, (w + 1) / 2);
            h = Math.max(1, (h + 1) / 2);
            levels.add(new Level(next, w, h));
            current = next;
        }
        for (Level level : levels) {
            level.tiles = encodeTiles(level.values, level.width, level.height, nodata);
        }

        // Layout: header (8) + IFD blocks + their external arrays, then tile data.
        double scaleX = (bounds[2] - bounds[0]) / width;
        double scaleY = (bounds[3] - bounds[1]) / height;
        int levelsCount = levels.size();
        long[] ifdOffset = new long[levelsCount];
        long[] externalOffset = new long[levelsCount];
        long[] externalSize = new long[levelsCount];
        long cursor = 8;
        for (int i = 0; i < levelsCount; i++) {
            ifdOffset[i] = cursor;
            int entries = tagCount(levels.get(i), epsg, gdalMetadata != null);
            long ifdSize = 2L + entries * 12L + 4L;
            externalOffset[i] = cursor + ifdSize;
            externalSize[i] = externalSize(levels.get(i), epsg, gdalMetadata);
            cursor += ifdSize + externalSize[i];
            if ((cursor & 1) == 1) {
                cursor++;
            }
        }
        long tileDataStart = cursor;
        long[] levelTileStart = new long[levelsCount];
        cursor = tileDataStart;
        for (int i = 0; i < levelsCount; i++) {
            levelTileStart[i] = cursor;
            for (byte[] tile : levels.get(i).tiles) {
                cursor += tile.length;
            }
        }

        ByteBuffer out = ByteBuffer.allocate((int) cursor).order(ByteOrder.LITTLE_ENDIAN);
        out.put((byte) 'I');
        out.put((byte) 'I');
        out.putShort((short) 42);
        out.putInt(8);
        for (int i = 0; i < levelsCount; i++) {
            writeIfd(out, levels.get(i), i, epsg, bounds, scaleX, scaleY, nodata, gdalMetadata, ifdOffset, externalOffset,
                    levelTileStart[i], i + 1 < levelsCount ? ifdOffset[i + 1] : 0);
        }
        for (int i = 0; i < levelsCount; i++) {
            out.position((int) (externalOffset[i] + externalSize[i]));
            if ((out.position() & 1) == 1) {
                out.position(out.position() + 1);
            }
        }
        out.position((int) tileDataStart);
        for (Level level : levels) {
            for (byte[] tile : level.tiles) {
                out.put(tile);
            }
        }
        return out.array();
    }

    private static final class Level {
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

    /** A tile: tag entry. */
    private record Tag(int id, int type, long count, long value) {}

    private static int tagCount(Level level, int epsg, boolean metadata) {
        return metadata ? 18 : 17;
    }

    private static long externalSize(Level level, int epsg, String gdalMetadata) {
        long size = 0;
        int tiles = level.tilesAcross() * level.tilesDown();
        size += (long) tiles * 4 * 2; // TileOffsets + TileByteCounts
        size += 3 * 8; // ModelPixelScale (DOUBLE x3)
        size += 6 * 8; // ModelTiepoint (DOUBLE x6)
        size += (4 + geoKeys(epsg).length) * 2L; // GeoKeyDirectory
        size += 64; // GDAL_NODATA ascii
        if (gdalMetadata != null) {
            size += gdalMetadata.length() + 1;
        }
        return size;
    }

    private static short[] geoKeys(int epsg) {
        // header (4 shorts) + keys (4 shorts each).
        List<Short> keys = new ArrayList<>();
        if (epsg == 3857) {
            keys.add((short) 1024);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 1); // GTModelTypeGeoKey = projected
            keys.add((short) 1025);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 1); // RasterPixelIsArea
            keys.add((short) 3072);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 3857); // ProjectedCSTypeGeoKey
        } else {
            keys.add((short) 1024);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 2); // geographic
            keys.add((short) 1025);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 1);
            keys.add((short) 2048);
            keys.add((short) 0);
            keys.add((short) 1);
            keys.add((short) 4326); // GeographicTypeGeoKey
        }
        short[] out = new short[keys.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = keys.get(i);
        }
        return out;
    }

    private static void writeIfd(ByteBuffer out, Level level, int index, int epsg, double[] bounds, double scaleX,
            double scaleY, double nodata, String gdalMetadata, long[] ifdOffset, long[] externalOffset, long tileStart,
            long nextIfd) {
        int tiles = level.tilesAcross() * level.tilesDown();
        long[] tileOffsets = new long[tiles];
        long[] tileLengths = new long[tiles];
        long cursor = tileStart;
        int k = 0;
        for (int ty = 0; ty < level.tilesDown(); ty++) {
            for (int tx = 0; tx < level.tilesAcross(); tx++) {
                byte[] tile = level.tiles[ty * level.tilesAcross() + tx];
                tileOffsets[k] = cursor;
                tileLengths[k] = tile.length;
                cursor += tile.length;
                k++;
            }
        }
        double[] pixelScale = {scaleX, scaleY, 0.0};
        double[] tiepoint = {0.0, 0.0, 0.0, bounds[0], bounds[3], 0.0};
        short[] geo = geoKeys(epsg);
        byte[] nodataAscii = (trimNumber(nodata) + "\0").getBytes(StandardCharsets.US_ASCII);
        byte[] gdal = gdalMetadata == null ? null : (gdalMetadata + "\0").getBytes(StandardCharsets.US_ASCII);

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag(254, 4, 1, index == 0 ? 0 : 1));
        tags.add(new Tag(256, 4, 1, level.width));
        tags.add(new Tag(257, 4, 1, level.height));
        tags.add(new Tag(258, 3, 1, 32));
        tags.add(new Tag(259, 3, 1, COMPRESSION_DEFLATE));
        tags.add(new Tag(262, 3, 1, 1));
        tags.add(new Tag(277, 3, 1, 1));
        tags.add(new Tag(284, 3, 1, 1));
        tags.add(new Tag(322, 4, 1, TILE));
        tags.add(new Tag(323, 4, 1, TILE));
        tags.add(new Tag(324, 4, tiles, 0));
        tags.add(new Tag(325, 4, tiles, 0));
        tags.add(new Tag(339, 3, 1, 3));
        tags.add(new Tag(33550, 12, 3, 0));
        tags.add(new Tag(33922, 12, 6, 0));
        tags.add(new Tag(34735, 3, geo.length, 0));
        tags.add(new Tag(42113, 2, nodataAscii.length, 0));
        if (gdal != null) {
            tags.add(new Tag(42112, 2, gdal.length, 0));
        }
        tags.sort(java.util.Comparator.comparingInt(Tag::id));

        long ext = externalOffset[index];
        long extStart = ext;
        long tileOffsetsAt = 0;
        long tileCountsAt = 0;
        long pixelScaleAt = 0;
        long tiepointAt = 0;
        long geoAt = 0;
        long nodataAt = 0;
        long gdalAt = 0;
        // Assign external offsets in a stable order.
        tileOffsetsAt = extStart;
        ext += (long) tiles * 4;
        tileCountsAt = ext;
        ext += (long) tiles * 4;
        pixelScaleAt = ext;
        ext += 24;
        tiepointAt = ext;
        ext += 48;
        geoAt = ext;
        ext += (long) geo.length * 2;
        nodataAt = ext;
        ext += nodataAscii.length;
        if (gdal != null) {
            gdalAt = ext;
            ext += gdal.length;
        }

        out.position((int) ifdOffset[index]);
        out.putShort((short) tags.size());
        for (Tag tag : tags) {
            long value = tag.value();
            switch (tag.id()) {
                case 324 -> value = tiles * 4 > 4 ? tileOffsetsAt : tileOffsets[0];
                case 325 -> value = tiles * 4 > 4 ? tileCountsAt : tileLengths[0];
                case 33550 -> value = pixelScaleAt;
                case 33922 -> value = tiepointAt;
                case 34735 -> value = geoAt;
                case 42113 -> value = nodataAt;
                case 42112 -> value = gdalAt;
                default -> {
                }
            }
            out.putShort((short) tag.id());
            out.putShort((short) tag.type());
            out.putInt((int) tag.count());
            out.putInt((int) value);
        }
        out.putInt((int) nextIfd);

        // External arrays.
        out.position((int) tileOffsetsAt);
        for (long v : tileOffsets) {
            out.putInt((int) v);
        }
        out.position((int) tileCountsAt);
        for (long v : tileLengths) {
            out.putInt((int) v);
        }
        out.position((int) pixelScaleAt);
        for (double v : pixelScale) {
            out.putDouble(v);
        }
        out.position((int) tiepointAt);
        for (double v : tiepoint) {
            out.putDouble(v);
        }
        out.position((int) geoAt);
        for (short v : geo) {
            out.putShort(v);
        }
        out.position((int) nodataAt);
        out.put(nodataAscii);
        if (gdal != null) {
            out.position((int) gdalAt);
            out.put(gdal);
        }
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
