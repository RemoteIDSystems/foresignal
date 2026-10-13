package ai.foresignal.raster.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The static-map basemap (prompt 055; Weather-Imagery-Design §3 row "Static maps"): Natural Earth 1:50m
 * land, lakes, coastline, admin-0 boundary lines and admin-1 (state and province) lines, read from the
 * classpath resource {@value #RESOURCE} that {@code scripts/build-basemap.py} generated. Natural Earth is
 * public domain.
 *
 * <p>The file is loaded into memory once, on first use (a lazy holder; about 1 MB on disk and a few MB
 * decoded), and never changes afterwards, so reads are lock-free. Each layer gets a coarse index of
 * {@value #CELL_DEGREES}° cells listing the parts whose bounding box touches the cell, so a query for a
 * map's box returns only nearby parts. Nothing here touches the network: the asset ships in the jar.
 *
 * <p>Format (little-endian, see the script for the authoritative description): magic {@code FSBM},
 * {@code u16} version, {@code u16} layer count, {@code u32} reserved, a 48-byte table entry per layer
 * (name, geometry type, part count, offset, length, bounding box), then the parts: a bounding box, a ring
 * count and per ring the first point as {@code int32} microdegrees followed by zigzag varint deltas.
 */
@Component
public class BasemapStore {

    /** The classpath resource. */
    public static final String RESOURCE = "/basemap/ne50m.bin";

    /** The file magic: Foresignal basemap (translated from the prompt's pre-rename {@code ELBM}). */
    public static final String MAGIC = "FSBM";

    /** The supported format version. */
    public static final int VERSION = 1;

    /** The land polygons. */
    public static final String LAND = "land";

    /** The lake polygons. */
    public static final String LAKES = "lakes";

    /** The coastline lines. */
    public static final String COASTLINE = "coastline";

    /** The admin-0 (country) boundary lines on land. */
    public static final String ADMIN0 = "admin0";

    /** The admin-1 (state and province) lines. */
    public static final String ADMIN1 = "admin1";

    /** The side of an index cell, degrees. */
    static final int CELL_DEGREES = 10;

    private static final int COLUMNS = 360 / CELL_DEGREES;
    private static final int ROWS = 180 / CELL_DEGREES;
    private static final int ENTRY = 48;

    /** One geometry part: its bounding box and rings of interleaved {@code lon, lat} microdegrees. */
    public record Part(int minLon, int minLat, int maxLon, int maxLat, List<int[]> rings) {

        /** True when the part's box intersects the degree box (no antimeridian crossing). */
        public boolean intersects(double west, double south, double east, double north) {
            return maxLon >= west * 1e6 && minLon <= east * 1e6 && maxLat >= south * 1e6 && minLat <= north * 1e6;
        }
    }

    /**
     * One layer.
     *
     * @param name the layer name
     * @param polygons true for polygons (ring 0 is the outer ring, the rest holes), false for lines
     * @param parts every part
     * @param cells per index cell ({@code row × columns + column}), the indices of the parts touching it
     */
    public record Layer(String name, boolean polygons, List<Part> parts, List<int[]> cells) {

        /** The parts whose box intersects the degree box; a box with {@code west > east} crosses ±180°. */
        public List<Part> query(double west, double south, double east, double north) {
            if (west > east) {
                List<Part> out = new ArrayList<>(query(west, south, 180, north));
                for (Part p : query(-180, south, east, north)) {
                    if (!out.contains(p)) {
                        out.add(p);
                    }
                }
                return out;
            }
            BitSet seen = new BitSet(parts.size());
            List<Part> out = new ArrayList<>();
            int c0 = column(west);
            int c1 = column(east);
            int r0 = row(south);
            int r1 = row(north);
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    for (int index : cells.get(r * COLUMNS + c)) {
                        if (!seen.get(index)) {
                            seen.set(index);
                            Part part = parts.get(index);
                            if (part.intersects(west, south, east, north)) {
                                out.add(part);
                            }
                        }
                    }
                }
            }
            return out;
        }
    }

    private volatile Map<String, Layer> layers;

    /** The layers by name, loading the resource on first use. */
    public Map<String, Layer> layers() {
        Map<String, Layer> loaded = layers;
        if (loaded == null) {
            synchronized (this) {
                loaded = layers;
                if (loaded == null) {
                    loaded = load();
                    layers = loaded;
                }
            }
        }
        return loaded;
    }

    /**
     * One layer.
     *
     * @throws IllegalArgumentException for an unknown name
     */
    public Layer layer(String name) {
        Layer layer = layers().get(name);
        if (layer == null) {
            throw new IllegalArgumentException("The basemap has no layer '" + name + "'");
        }
        return layer;
    }

    private static Map<String, Layer> load() {
        try (InputStream in = BasemapStore.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The basemap resource " + RESOURCE + " is missing from the classpath");
            }
            return parse(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the basemap " + RESOURCE, e);
        }
    }

    /**
     * Parses a basemap file.
     *
     * @throws IllegalArgumentException for a wrong magic, an unsupported version or a truncated file
     */
    public static Map<String, Layer> parse(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (bytes.length < 12 || !MAGIC.equals(new String(bytes, 0, 4, StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("Not a Foresignal basemap (magic " + MAGIC + " expected)");
        }
        int version = Short.toUnsignedInt(buf.getShort(4));
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported basemap version " + version);
        }
        int count = Short.toUnsignedInt(buf.getShort(6));
        if (12 + (long) count * ENTRY > bytes.length) {
            throw new IllegalArgumentException("Truncated basemap layer table");
        }
        Map<String, Layer> out = new LinkedHashMap<>();
        for (int k = 0; k < count; k++) {
            int at = 12 + k * ENTRY;
            String name = new String(bytes, at, 16, StandardCharsets.US_ASCII).replace("\0", "");
            int type = Byte.toUnsignedInt(buf.get(at + 16));
            int parts = buf.getInt(at + 20);
            int offset = buf.getInt(at + 24);
            int length = buf.getInt(at + 28);
            if (offset < 0 || length < 0 || (long) offset + length > bytes.length) {
                throw new IllegalArgumentException("Basemap layer " + name + " lies outside the file");
            }
            out.put(name, readLayer(buf, name, type == 1, parts, offset, offset + length));
        }
        return Collections.unmodifiableMap(out);
    }

    private static Layer readLayer(ByteBuffer buf, String name, boolean polygons, int count, int start, int end) {
        List<Part> parts = new ArrayList<>(count);
        int[] pos = {start};
        for (int k = 0; k < count; k++) {
            int p = pos[0];
            if (p + 20 > end) {
                throw new IllegalArgumentException("Truncated basemap layer " + name);
            }
            int minLon = buf.getInt(p);
            int minLat = buf.getInt(p + 4);
            int maxLon = buf.getInt(p + 8);
            int maxLat = buf.getInt(p + 12);
            int ringCount = Short.toUnsignedInt(buf.getShort(p + 16));
            pos[0] = p + 20;
            List<int[]> rings = new ArrayList<>(ringCount);
            for (int r = 0; r < ringCount; r++) {
                int q = pos[0];
                if (q + 12 > end) {
                    throw new IllegalArgumentException("Truncated basemap ring in " + name);
                }
                int n = buf.getInt(q);
                if (n < 1 || n > (end - q)) {
                    throw new IllegalArgumentException("Bad basemap ring length in " + name);
                }
                int[] ring = new int[n * 2];
                ring[0] = buf.getInt(q + 4);
                ring[1] = buf.getInt(q + 8);
                pos[0] = q + 12;
                for (int i = 1; i < n; i++) {
                    ring[i * 2] = ring[i * 2 - 2] + unzig(varint(buf, pos, end));
                    ring[i * 2 + 1] = ring[i * 2 - 1] + unzig(varint(buf, pos, end));
                }
                rings.add(ring);
            }
            parts.add(new Part(minLon, minLat, maxLon, maxLat, List.copyOf(rings)));
        }
        return new Layer(name, polygons, List.copyOf(parts), index(parts));
    }

    private static List<int[]> index(List<Part> parts) {
        List<List<Integer>> cells = new ArrayList<>(COLUMNS * ROWS);
        for (int k = 0; k < COLUMNS * ROWS; k++) {
            cells.add(new ArrayList<>());
        }
        for (int i = 0; i < parts.size(); i++) {
            Part p = parts.get(i);
            int c0 = column(p.minLon() / 1e6);
            int c1 = column(p.maxLon() / 1e6);
            int r0 = row(p.minLat() / 1e6);
            int r1 = row(p.maxLat() / 1e6);
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    cells.get(r * COLUMNS + c).add(i);
                }
            }
        }
        List<int[]> out = new ArrayList<>(cells.size());
        for (List<Integer> cell : cells) {
            out.add(cell.stream().mapToInt(Integer::intValue).toArray());
        }
        return List.copyOf(out);
    }

    private static int column(double lon) {
        return Math.max(0, Math.min(COLUMNS - 1, (int) Math.floor((lon + 180) / CELL_DEGREES)));
    }

    private static int row(double lat) {
        return Math.max(0, Math.min(ROWS - 1, (int) Math.floor((lat + 90) / CELL_DEGREES)));
    }

    private static long varint(ByteBuffer buf, int[] pos, int end) {
        long out = 0;
        int shift = 0;
        while (true) {
            if (pos[0] >= end || shift > 63) {
                throw new IllegalArgumentException("Truncated basemap varint");
            }
            int b = Byte.toUnsignedInt(buf.get(pos[0]++));
            out |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return out;
            }
            shift += 7;
        }
    }

    private static int unzig(long n) {
        return (int) ((n >>> 1) ^ -(n & 1));
    }
}
