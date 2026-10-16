package ai.foresignal.export.pmtiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * An in-house PMTiles v3 writer (prompt 108; Weather-Imagery-Design §11), reused by 111. Implements
 * the format from the v3 spec: the 127-byte header, Hilbert tile ids, a clustered tile data section,
 * gzip-compressed (varint) root and leaf directories and gzip JSON metadata. Internal compression is
 * gzip ({@value #COMPRESSION_GZIP}); tiles are stored as-is (PNG/JPEG are already compressed).
 */
public final class PmtilesWriter {

    /** The internal compression: gzip. */
    public static final int COMPRESSION_GZIP = 2;

    /** The tile compression: none (the tiles are already compressed image formats). */
    public static final int COMPRESSION_NONE = 1;

    /** The MVT tile type. */
    public static final int TYPE_MVT = 1;

    /** The PNG tile type. */
    public static final int TYPE_PNG = 2;

    /** The JPEG tile type. */
    public static final int TYPE_JPEG = 3;

    /** The header is 127 bytes. */
    public static final int HEADER_BYTES = 127;

    /** The root directory (with the header) must fit in 16,384 bytes. */
    public static final int ROOT_BUDGET = 16_384 - HEADER_BYTES;

    private static final Comparator<Tile> BY_ID = Comparator.comparingLong(t -> tileId(t.z(), t.x(), t.y()));

    private PmtilesWriter() {}

    /** One tile. */
    public record Tile(int z, int x, int y, byte[] data) {}

    /**
     * Writes a PMTiles v3 archive.
     *
     * @param tiles the tiles (any order)
     * @param metadata extra JSON metadata (name, attribution, encode...)
     * @param bounds {@code [w, s, e, n]}, degrees
     * @param minZoom the minimum zoom
     * @param maxZoom the maximum zoom
     * @param tileType {@link #TYPE_PNG} or {@link #TYPE_JPEG}
     */
    public static byte[] write(List<Tile> tiles, Map<String, Object> metadata, double[] bounds, int minZoom, int maxZoom,
            int tileType) {
        return write(tiles, metadata, bounds, minZoom, maxZoom, tileType, COMPRESSION_NONE);
    }

    /**
     * Writes a PMTiles v3 archive with an explicit tile-section compression.
     *
     * @param tileCompression {@link #COMPRESSION_NONE} (pre-compressed image tiles) or
     *     {@link #COMPRESSION_GZIP} (gzip-compressed MVT)
     */
    public static byte[] write(List<Tile> tiles, Map<String, Object> metadata, double[] bounds, int minZoom, int maxZoom,
            int tileType, int tileCompression) {
        List<Tile> sorted = new ArrayList<>(tiles);
        sorted.sort(BY_ID);

        // Clustered tile data with run-length dedup: consecutive identical tiles share one content
        // offset and increment the run length.
        List<Entry> entries = new ArrayList<>();
        List<byte[]> unique = new ArrayList<>();
        long dataLength = 0;
        for (Tile tile : sorted) {
            long id = tileId(tile.z(), tile.x(), tile.y());
            if (!unique.isEmpty() && java.util.Arrays.equals(unique.getLast(), tile.data())) {
                Entry last = entries.getLast();
                entries.set(entries.size() - 1,
                        new Entry(last.tileId(), last.offset(), last.length(), last.runLength() + 1));
                continue;
            }
            entries.add(new Entry(id, dataLength, tile.data().length, 1));
            unique.add(tile.data());
            dataLength += tile.data().length;
        }

        Directory dirs = directory(entries, unique);
        byte[] root = gzip(serialize(dirs.root()));
        byte[] leaves = dirs.leaves();
        byte[] json = gzip(metadataJson(metadata).getBytes(StandardCharsets.UTF_8));
        if (root.length > ROOT_BUDGET) {
            throw new IllegalStateException("The PMTiles root directory is " + root.length + " bytes; at most "
                    + ROOT_BUDGET);
        }

        long rootOffset = HEADER_BYTES;
        long metadataOffset = rootOffset + root.length;
        long leafOffset = metadataOffset + json.length;
        long tileOffset = leafOffset + leaves.length;
        long total = tileOffset + dataLength;

        ByteBuffer out = ByteBuffer.allocate((int) total).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(out, rootOffset, root.length, metadataOffset, json.length, leafOffset, leaves.length, tileOffset,
                dataLength, sorted.size(), entries.size(), unique.size(), (byte) minZoom, (byte) maxZoom, bounds,
                tileType, tileCompression);
        out.put(root);
        out.put(json);
        out.put(leaves);
        for (byte[] data : unique) {
            out.put(data);
        }
        return out.array();
    }

    /** The Hilbert tile id of {@code (z, x, y)}. */
    public static long tileId(int z, int x, int y) {
        long acc = ((1L << (2 * z)) - 1) / 3;
        return acc + hilbert(z, x, y);
    }

    /** The Hilbert curve index (order {@code z}) of {@code (x, y)}. */
    public static long hilbert(int z, int x, int y) {
        long d = 0;
        int n = 1 << z;
        for (int s = n >> 1; s > 0; s >>= 1) {
            int rx = (x & s) > 0 ? 1 : 0;
            int ry = (y & s) > 0 ? 1 : 0;
            d += (long) s * s * ((3 * rx) ^ ry);
            if (ry == 0) {
                if (rx == 1) {
                    x = n - 1 - x;
                    y = n - 1 - y;
                }
                int t = x;
                x = y;
                y = t;
            }
        }
        return d;
    }

    /** A directory entry. */
    record Entry(long tileId, long offset, int length, int runLength) {}

    private record Directory(List<Entry> root, byte[] leaves) {}

    /** Builds the root and leaf directories, splitting until the root fits the budget. */
    private static Directory directory(List<Entry> entries, List<byte[]> unique) {
        int leafMax = 4096;
        while (true) {
            if (entries.size() <= leafMax) {
                byte[] root = serialize(entries);
                if (gzip(root).length <= ROOT_BUDGET) {
                    // Force gzip for the size check; the caller re-gzips. Single root: no leaves.
                    return new Directory(entries, new byte[0]);
                }
            }
            Directory split = split(entries, leafMax);
            if (split != null) {
                return split;
            }
            leafMax = Math.max(1, leafMax / 2);
        }
    }

    private static Directory split(List<Entry> entries, int leafMax) {
        List<Entry> root = new ArrayList<>();
        ByteArrayOutputStream leaves = new ByteArrayOutputStream();
        for (int start = 0; start < entries.size(); start += leafMax) {
            List<Entry> leaf = entries.subList(start, Math.min(entries.size(), start + leafMax));
            byte[] gz = gzip(serialize(leaf));
            root.add(new Entry(leaf.get(0).tileId(), leaves.size(), gz.length, 0));
            leaves.writeBytes(gz);
        }
        if (gzip(serialize(root)).length > ROOT_BUDGET) {
            return null;
        }
        return new Directory(root, leaves.toByteArray());
    }

    /** Serializes a directory (uncompressed). */
    static byte[] serialize(List<Entry> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(entries.size() * 4);
        writeVarint(out, entries.size());
        long last = 0;
        for (Entry e : entries) {
            writeVarint(out, e.tileId() - last);
            last = e.tileId();
        }
        for (Entry e : entries) {
            writeVarint(out, e.runLength());
        }
        for (Entry e : entries) {
            writeVarint(out, e.length());
        }
        long next = 0;
        for (int k = 0; k < entries.size(); k++) {
            Entry e = entries.get(k);
            if (k > 0 && e.offset() == next) {
                writeVarint(out, 0);
            } else {
                writeVarint(out, e.offset() + 1);
            }
            next = e.offset() + e.length();
        }
        return out.toByteArray();
    }

    /** A protobuf little-endian variable-width integer. */
    static void writeVarint(ByteArrayOutputStream out, long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                out.write((int) value);
                return;
            }
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
    }

    private static String metadataJson(Map<String, Object> metadata) {
        Map<String, Object> m = new java.util.LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        StringBuilder json = new StringBuilder();
        json.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) {
                json.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                json.append(v);
            } else {
                json.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }
        json.append('}');
        return json.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static byte[] gzip(byte[] raw) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(32, raw.length / 2));
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(raw);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not gzip a PMTiles section", e);
        }
        return bytes.toByteArray();
    }

    private static void writeHeader(ByteBuffer out, long rootOffset, long rootLen, long metaOffset, long metaLen,
            long leafOffset, long leafLen, long tileOffset, long tileLen, long addressed, long entries, long contents,
            byte minZoom, byte maxZoom, double[] bounds, int tileType, int tileCompression) {
        out.put("PMTiles".getBytes(StandardCharsets.US_ASCII));
        out.put((byte) 3);
        out.putLong(rootOffset);
        out.putLong(rootLen);
        out.putLong(metaOffset);
        out.putLong(metaLen);
        out.putLong(leafOffset);
        out.putLong(leafLen);
        out.putLong(tileOffset);
        out.putLong(tileLen);
        out.putLong(addressed);
        out.putLong(entries);
        out.putLong(contents);
        out.put((byte) 1); // clustered
        out.put((byte) COMPRESSION_GZIP);
        out.put((byte) tileCompression);
        out.put((byte) tileType);
        out.put(minZoom);
        out.put(maxZoom);
        out.putInt((int) Math.round(bounds[0] * 1e7));
        out.putInt((int) Math.round(bounds[1] * 1e7));
        out.putInt((int) Math.round(bounds[2] * 1e7));
        out.putInt((int) Math.round(bounds[3] * 1e7));
        out.put((byte) minZoom);
        out.putInt((int) Math.round((bounds[0] + bounds[2]) / 2 * 1e7));
        out.putInt((int) Math.round((bounds[1] + bounds[3]) / 2 * 1e7));
        if (out.position() != HEADER_BYTES) {
            throw new IllegalStateException("PMTiles header is " + out.position() + " bytes");
        }
    }
}
