package ai.foresignal.export.pmtiles;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import tools.jackson.databind.json.JsonMapper;

/**
 * An in-house PMTiles v3 writer (prompt 108; Weather-Imagery-Design §11), extended by prompt 111 for
 * offline packs. It follows the v3 specification:
 *
 * <ul>
 *   <li>tile ids on the Hilbert curve: the tile count of every lower zoom plus the Hilbert position
 *       of {@code (x, y)} at {@code z} ({@link #tileId});
 *   <li>tile contents deduplicated by SHA-256: an entry's {@code run_length} grows only while the
 *       next tile id is exactly {@code tileId + run_length} with the same content; a repeat elsewhere
 *       gets its own entry that points at the earlier offset;
 *   <li>tile data written in tile-id order of first appearance, so every offset is either contiguous
 *       with the previous entry or a lesser (deduplicated) offset and the first entry is at 0: the
 *       archive is <em>clustered</em> ({@code clustered = 1});
 *   <li>directories encoded as a varint entry count, delta tile ids, run lengths, lengths and offsets
 *       ({@code 0} = contiguous with the previous entry, else {@code offset + 1}), gzip-compressed;
 *   <li>a root directory of at most 16,257 bytes (16,384 minus the header); when every entry does not
 *       fit, entries go to leaf directories, starting at 4,096 entries per leaf and doubling the leaf
 *       size until the root (one entry per leaf) fits;
 *   <li>the 127-byte little-endian header and gzip JSON metadata, serialized with a JSON mapper.
 * </ul>
 *
 * <p>{@link #write} builds a small archive in memory (raster exports, place packs); {@link Archive}
 * streams tiles through a spill file for offline packs of up to {@code packs.maxTiles} tiles.
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

    /** The root directory must fit, with the header, in the first 16,384 bytes. */
    public static final int ROOT_BUDGET = 16_384 - HEADER_BYTES;

    /** Entries per leaf directory on the first attempt. */
    static final int FIRST_LEAF_SIZE = 4096;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private PmtilesWriter() {}

    /** One tile. */
    public record Tile(int z, int x, int y, byte[] data) {}

    /** A directory entry: a tile run ({@code runLength ≥ 1}) or a leaf directory ({@code runLength = 0}). */
    public record Entry(long tileId, long offset, long length, long runLength) {}

    /**
     * Writes a PMTiles v3 archive in memory with uncompressed (image) tiles.
     *
     * @param tiles the tiles (any order)
     * @param metadata the JSON metadata
     * @param bounds {@code [w, s, e, n]}, degrees
     * @param tileType {@link #TYPE_PNG} or {@link #TYPE_JPEG}
     */
    public static byte[] write(List<Tile> tiles, Map<String, Object> metadata, double[] bounds, int minZoom, int maxZoom,
            int tileType) {
        return write(tiles, metadata, bounds, minZoom, maxZoom, tileType, COMPRESSION_NONE);
    }

    /**
     * Writes a PMTiles v3 archive in memory with an explicit tile compression.
     *
     * @param tileCompression {@link #COMPRESSION_NONE} (image tiles) or {@link #COMPRESSION_GZIP}
     *     (gzip-compressed MVT)
     */
    public static byte[] write(List<Tile> tiles, Map<String, Object> metadata, double[] bounds, int minZoom, int maxZoom,
            int tileType, int tileCompression) {
        Contents contents = new Contents();
        List<byte[]> blobs = new ArrayList<>();
        long[] ids = new long[tiles.size()];
        int[] refs = new int[tiles.size()];
        for (int k = 0; k < tiles.size(); k++) {
            Tile tile = tiles.get(k);
            ids[k] = tileId(tile.z(), tile.x(), tile.y());
            int before = contents.size();
            refs[k] = contents.intern(tile.data());
            if (contents.size() > before) {
                blobs.add(tile.data());
            }
        }
        Plan plan = plan(ids, refs, tiles.size(), contents);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeArchive(out, plan, metadata, bounds, minZoom, maxZoom, tileType, tileCompression,
                    (ref, sink) -> sink.write(blobs.get(ref)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    /** The Hilbert tile id of {@code (z, x, y)}: the tiles of every lower zoom plus the Hilbert position. */
    public static long tileId(int z, int x, int y) {
        long acc = ((1L << (2 * z)) - 1) / 3;
        return acc + hilbert(z, x, y);
    }

    /** The zoom of a tile id: the {@code z} whose id range {@code [(4^z - 1)/3, (4^(z+1) - 1)/3)} holds it. */
    public static int zoomOf(long tileId) {
        int z = 0;
        while (z < 31 && tileId >= ((1L << (2 * (z + 1))) - 1) / 3) {
            z++;
        }
        return z;
    }

    /** The Hilbert curve index (order {@code z}) of {@code (x, y)}. */
    public static long hilbert(int z, int x, int y) {
        long d = 0;
        long n = 1L << z;
        long rx;
        long ry;
        long px = x;
        long py = y;
        for (long s = n >> 1; s > 0; s >>= 1) {
            rx = (px & s) > 0 ? 1 : 0;
            ry = (py & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            if (ry == 0) {
                if (rx == 1) {
                    px = n - 1 - px;
                    py = n - 1 - py;
                }
                long t = px;
                px = py;
                py = t;
            }
        }
        return d;
    }

    // =====================================================================================
    // Streaming archive
    // =====================================================================================

    /**
     * A PMTiles archive built from tiles added one at a time (prompt 111). Each distinct tile content
     * is written once to a spill file next to the target; only tile ids, content references and
     * content hashes stay in memory. {@link #finish} sorts, plans the directories and writes the
     * archive, copying tile data from the spill file.
     */
    public static final class Archive implements AutoCloseable {

        private final Path target;
        private final Path spill;
        private final OutputStream spillOut;
        private final int tileType;
        private final int tileCompression;
        private final Contents contents = new Contents();
        private final List<long[]> spans = new ArrayList<>();
        private long spillBytes;
        private long[] ids = new long[1024];
        private int[] refs = new int[1024];
        private int count;
        private boolean finished;

        /**
         * Opens an archive that {@link #finish} writes to {@code target}.
         *
         * @param tileType {@link #TYPE_MVT}, {@link #TYPE_PNG} or {@link #TYPE_JPEG}
         * @param tileCompression {@link #COMPRESSION_GZIP} or {@link #COMPRESSION_NONE}
         */
        public Archive(Path target, int tileType, int tileCompression) throws IOException {
            this.target = target;
            this.tileType = tileType;
            this.tileCompression = tileCompression;
            this.spill = Files.createTempFile(target.toAbsolutePath().getParent(), "pmtiles-", ".spill");
            this.spillOut = new BufferedOutputStream(Files.newOutputStream(spill), 1 << 16);
        }

        /** Adds a tile; a tile id added twice keeps the last content. */
        public void add(int z, int x, int y, byte[] data) throws IOException {
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("A PMTiles tile must not be empty");
            }
            int before = contents.size();
            int ref = contents.intern(data);
            if (contents.size() > before) {
                spillOut.write(data);
                spans.add(new long[] {spillBytes, data.length});
                spillBytes += data.length;
            }
            if (count == ids.length) {
                ids = Arrays.copyOf(ids, count * 2);
                refs = Arrays.copyOf(refs, count * 2);
            }
            ids[count] = tileId(z, x, y);
            refs[count] = ref;
            count++;
        }

        /** Tiles added (addressed tiles before deduplication). */
        public int tiles() {
            return count;
        }

        /** Distinct tile contents added. */
        public int contents() {
            return contents.size();
        }

        /**
         * Writes the archive and deletes the spill file.
         *
         * @return the archive's size in bytes
         */
        public long finish(Map<String, Object> metadata, double[] bounds, int minZoom, int maxZoom) throws IOException {
            finished = true;
            spillOut.close();
            Plan plan = plan(ids, refs, count, contents);
            try (FileChannel source = FileChannel.open(spill, StandardOpenOption.READ);
                    OutputStream out = new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), 1 << 16)) {
                ByteBuffer buffer = ByteBuffer.allocate(1 << 16);
                writeArchive(out, plan, metadata, bounds, minZoom, maxZoom, tileType, tileCompression, (ref, sink) -> {
                    long[] span = spans.get(ref);
                    long position = span[0];
                    long remaining = span[1];
                    while (remaining > 0) {
                        buffer.clear();
                        buffer.limit((int) Math.min(buffer.capacity(), remaining));
                        int read = source.read(buffer, position);
                        if (read < 0) {
                            throw new IOException("The PMTiles spill file ended early");
                        }
                        sink.write(buffer.array(), 0, read);
                        position += read;
                        remaining -= read;
                    }
                });
            } finally {
                Files.deleteIfExists(spill);
            }
            return Files.size(target);
        }

        @Override
        public void close() throws IOException {
            if (!finished) {
                spillOut.close();
                Files.deleteIfExists(spill);
            }
        }
    }

    // =====================================================================================
    // Planning
    // =====================================================================================

    /** Distinct contents by SHA-256, with their lengths. */
    static final class Contents {
        private final Map<ByteBuffer, Integer> byHash = new HashMap<>();
        private final List<Long> lengths = new ArrayList<>();

        /** The reference of {@code data}'s content, adding it when new. */
        int intern(byte[] data) {
            ByteBuffer key = ByteBuffer.wrap(sha256(data));
            Integer ref = byHash.get(key);
            if (ref != null) {
                return ref;
            }
            int next = lengths.size();
            byHash.put(key, next);
            lengths.add((long) data.length);
            return next;
        }

        int size() {
            return lengths.size();
        }

        long length(int ref) {
            return lengths.get(ref);
        }
    }

    /**
     * The layout of an archive: the tile entries, the content references in data order and the
     * counts of the header.
     */
    record Plan(List<Entry> entries, int[] dataOrder, long dataLength, long addressed) {}

    /** Plans entries and data order for {@code count} tiles with ids {@code ids} and contents {@code refs}. */
    static Plan plan(long[] ids, int[] refs, int count, Contents contents) {
        long[][] tiles = new long[count][];
        for (int k = 0; k < count; k++) {
            tiles[k] = new long[] {ids[k], refs[k], k};
        }
        // Sort by tile id; for a repeated id the later addition wins.
        Arrays.sort(tiles, (a, b) -> a[0] != b[0] ? Long.compare(a[0], b[0]) : Long.compare(a[2], b[2]));
        List<long[]> unique = new ArrayList<>(count);
        for (long[] tile : tiles) {
            if (!unique.isEmpty() && unique.getLast()[0] == tile[0]) {
                unique.set(unique.size() - 1, tile);
            } else {
                unique.add(tile);
            }
        }
        long[] offsets = new long[contents.size()];
        Arrays.fill(offsets, -1);
        int[] order = new int[contents.size()];
        int written = 0;
        long dataLength = 0;
        List<Entry> entries = new ArrayList<>();
        for (long[] tile : unique) {
            int ref = (int) tile[1];
            if (offsets[ref] < 0) {
                offsets[ref] = dataLength;
                dataLength += contents.length(ref);
                order[written++] = ref;
            }
            long offset = offsets[ref];
            long length = contents.length(ref);
            if (!entries.isEmpty()) {
                Entry last = entries.getLast();
                if (last.offset() == offset && last.tileId() + last.runLength() == tile[0]) {
                    entries.set(entries.size() - 1,
                            new Entry(last.tileId(), last.offset(), last.length(), last.runLength() + 1));
                    continue;
                }
            }
            entries.add(new Entry(tile[0], offset, length, 1));
        }
        return new Plan(entries, Arrays.copyOf(order, written), dataLength, unique.size());
    }

    /** The root and leaf directories of {@code entries}. */
    record Directories(byte[] root, byte[] leaves, int leafCount) {}

    /**
     * Builds the directories: a single root when it fits {@link #ROOT_BUDGET}, else leaves of
     * {@link #FIRST_LEAF_SIZE} entries, doubling the leaf size until the root fits.
     */
    static Directories directories(List<Entry> entries) {
        byte[] root = gzip(serialize(entries));
        if (root.length <= ROOT_BUDGET) {
            return new Directories(root, new byte[0], 0);
        }
        for (long leafSize = FIRST_LEAF_SIZE; ; leafSize *= 2) {
            List<Entry> rootEntries = new ArrayList<>();
            ByteArrayOutputStream leaves = new ByteArrayOutputStream();
            for (int start = 0; start < entries.size(); start += (int) leafSize) {
                List<Entry> leaf = entries.subList(start, (int) Math.min(entries.size(), start + leafSize));
                byte[] compressed = gzip(serialize(leaf));
                rootEntries.add(new Entry(leaf.getFirst().tileId(), leaves.size(), compressed.length, 0));
                leaves.writeBytes(compressed);
            }
            byte[] candidate = gzip(serialize(rootEntries));
            if (candidate.length <= ROOT_BUDGET) {
                return new Directories(candidate, leaves.toByteArray(), rootEntries.size());
            }
            if (leafSize >= entries.size()) {
                throw new IllegalStateException("The PMTiles root directory cannot fit in " + ROOT_BUDGET + " bytes");
            }
        }
    }

    /** Copies one content to the archive. */
    @FunctionalInterface
    interface ContentCopier {
        void copy(int ref, OutputStream sink) throws IOException;
    }

    private static void writeArchive(OutputStream out, Plan plan, Map<String, Object> metadata, double[] bounds,
            int minZoom, int maxZoom, int tileType, int tileCompression, ContentCopier copier) throws IOException {
        Directories dirs = directories(plan.entries());
        byte[] json = gzip(metadataJson(metadata));
        long rootOffset = HEADER_BYTES;
        long metadataOffset = rootOffset + dirs.root().length;
        long leafOffset = metadataOffset + json.length;
        long tileOffset = leafOffset + dirs.leaves().length;
        // The header's zooms are those of the tiles present (pmtiles verify requires it); the requested
        // range applies only to an archive without tiles.
        int lowZoom = minZoom;
        int highZoom = maxZoom;
        if (!plan.entries().isEmpty()) {
            lowZoom = zoomOf(plan.entries().getFirst().tileId());
            Entry last = plan.entries().getLast();
            highZoom = zoomOf(last.tileId() + last.runLength() - 1);
        }
        ByteBuffer header = ByteBuffer.allocate(HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(header, rootOffset, dirs.root().length, metadataOffset, json.length, leafOffset,
                dirs.leaves().length, tileOffset, plan.dataLength(), plan.addressed(), plan.entries().size(),
                plan.dataOrder().length, lowZoom, highZoom, bounds, tileType, tileCompression);
        out.write(header.array());
        out.write(dirs.root());
        out.write(json);
        out.write(dirs.leaves());
        for (int ref : plan.dataOrder()) {
            copier.copy(ref, out);
        }
    }

    /** Serializes a directory (uncompressed). */
    static byte[] serialize(List<Entry> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(entries.size() * 4 + 4);
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
        for (int k = 0; k < entries.size(); k++) {
            Entry e = entries.get(k);
            if (k > 0 && e.offset() == entries.get(k - 1).offset() + entries.get(k - 1).length()) {
                writeVarint(out, 0);
            } else {
                writeVarint(out, e.offset() + 1);
            }
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

    /** The metadata as UTF-8 JSON (keys in insertion order; values escaped by the mapper). */
    static byte[] metadataJson(Map<String, Object> metadata) {
        return JSON.writeValueAsBytes(new LinkedHashMap<>(metadata == null ? Map.of() : metadata));
    }

    static byte[] gzip(byte[] raw) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(32, raw.length / 2));
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(raw);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not gzip a PMTiles section", e);
        }
        return bytes.toByteArray();
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void writeHeader(ByteBuffer out, long rootOffset, long rootLen, long metaOffset, long metaLen,
            long leafOffset, long leafLen, long tileOffset, long tileLen, long addressed, long entries, long contents,
            int minZoom, int maxZoom, double[] bounds, int tileType, int tileCompression) {
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
        out.put((byte) 1); // clustered: tile data in tile-id order of first appearance
        out.put((byte) COMPRESSION_GZIP);
        out.put((byte) tileCompression);
        out.put((byte) tileType);
        out.put((byte) minZoom);
        out.put((byte) maxZoom);
        out.putInt(e7(bounds[0]));
        out.putInt(e7(bounds[1]));
        out.putInt(e7(bounds[2]));
        out.putInt(e7(bounds[3]));
        out.put((byte) ((minZoom + maxZoom) / 2));
        out.putInt(e7((bounds[0] + bounds[2]) / 2));
        out.putInt(e7((bounds[1] + bounds[3]) / 2));
        if (out.position() != HEADER_BYTES) {
            throw new IllegalStateException("PMTiles header is " + out.position() + " bytes");
        }
    }

    private static int e7(double degrees) {
        return (int) Math.round(degrees * 1e7);
    }
}
