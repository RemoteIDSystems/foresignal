package ai.foresignal.export.pmtiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The PMTiles v3 writer (prompts 108 and 111): Hilbert tile ids against the spec's vectors, the
 * 127-byte header, the varint directory encoding, deduplication (run lengths only over consecutive
 * ids, shared offsets otherwise), leaf directories past the 16 KiB root, the clustered data order,
 * escaped JSON metadata, and the streaming archive, all read back with an in-test reader.
 */
class PmtilesWriterTest {

    @TempDir Path dir;

    private static byte[] tileData(int size, int seed) {
        byte[] bytes = new byte[size];
        for (int k = 0; k < size; k++) {
            bytes[k] = (byte) (seed + k);
        }
        return bytes;
    }

    // =====================================================================================
    // An in-test reader of the format
    // =====================================================================================

    /** A minimal PMTiles v3 reader over a whole archive. */
    static final class Reader {
        final byte[] archive;
        final ByteBuffer header;

        Reader(byte[] archive) {
            this.archive = archive;
            this.header = ByteBuffer.wrap(archive).order(ByteOrder.LITTLE_ENDIAN);
        }

        long rootOffset() {
            return header.getLong(8);
        }

        long rootLength() {
            return header.getLong(16);
        }

        long metadataOffset() {
            return header.getLong(24);
        }

        long metadataLength() {
            return header.getLong(32);
        }

        long leafOffset() {
            return header.getLong(40);
        }

        long leafLength() {
            return header.getLong(48);
        }

        long tileOffset() {
            return header.getLong(56);
        }

        long tileLength() {
            return header.getLong(64);
        }

        List<PmtilesWriter.Entry> directory(long offset, long length) throws Exception {
            byte[] gz = new byte[(int) length];
            System.arraycopy(archive, (int) offset, gz, 0, (int) length);
            return decode(gunzip(gz));
        }

        List<PmtilesWriter.Entry> root() throws Exception {
            return directory(rootOffset(), rootLength());
        }

        String metadata() throws Exception {
            byte[] gz = new byte[(int) metadataLength()];
            System.arraycopy(archive, (int) metadataOffset(), gz, 0, gz.length);
            return new String(gunzip(gz), StandardCharsets.UTF_8);
        }

        /** Every tile entry, through the leaf directories. */
        List<PmtilesWriter.Entry> tileEntries() throws Exception {
            List<PmtilesWriter.Entry> out = new ArrayList<>();
            for (PmtilesWriter.Entry entry : root()) {
                if (entry.runLength() == 0) {
                    out.addAll(directory(leafOffset() + entry.offset(), entry.length()));
                } else {
                    out.add(entry);
                }
            }
            return out;
        }

        /** The bytes of tile {@code (z, x, y)}, or null. */
        byte[] tile(int z, int x, int y) throws Exception {
            long id = PmtilesWriter.tileId(z, x, y);
            for (PmtilesWriter.Entry entry : tileEntries()) {
                if (id >= entry.tileId() && id < entry.tileId() + entry.runLength()) {
                    byte[] out = new byte[(int) entry.length()];
                    System.arraycopy(archive, (int) (tileOffset() + entry.offset()), out, 0, out.length);
                    return out;
                }
            }
            return null;
        }
    }

    static List<PmtilesWriter.Entry> decode(byte[] directory) {
        Cursor cursor = new Cursor(directory);
        int count = (int) cursor.varint();
        long[] ids = new long[count];
        long last = 0;
        for (int k = 0; k < count; k++) {
            last += cursor.varint();
            ids[k] = last;
        }
        long[] runs = new long[count];
        for (int k = 0; k < count; k++) {
            runs[k] = cursor.varint();
        }
        long[] lengths = new long[count];
        for (int k = 0; k < count; k++) {
            lengths[k] = cursor.varint();
        }
        List<PmtilesWriter.Entry> out = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            long raw = cursor.varint();
            long offset = raw == 0 && k > 0 ? out.get(k - 1).offset() + out.get(k - 1).length() : raw - 1;
            out.add(new PmtilesWriter.Entry(ids[k], offset, lengths[k], runs[k]));
        }
        return out;
    }

    private static byte[] gunzip(byte[] gz) throws Exception {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
            return in.readAllBytes();
        }
    }

    private static final class Cursor {
        private final byte[] data;
        private int index;

        Cursor(byte[] data) {
            this.data = data;
        }

        long varint() {
            long value = 0;
            int shift = 0;
            while (true) {
                int b = data[index++] & 0xFF;
                value |= (long) (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return value;
                }
                shift += 7;
            }
        }
    }

    // =====================================================================================
    // Tests
    // =====================================================================================

    @Test
    @DisplayName("tile ids follow the spec's Hilbert vectors and zoomOf inverts the zoom")
    void hilbertVectors() {
        assertThat(PmtilesWriter.tileId(0, 0, 0)).isZero();
        assertThat(PmtilesWriter.tileId(1, 0, 0)).isEqualTo(1);
        assertThat(PmtilesWriter.tileId(1, 0, 1)).isEqualTo(2);
        assertThat(PmtilesWriter.tileId(1, 1, 1)).isEqualTo(3);
        assertThat(PmtilesWriter.tileId(1, 1, 0)).isEqualTo(4);
        assertThat(PmtilesWriter.tileId(2, 0, 0)).isEqualTo(5);
        assertThat(PmtilesWriter.tileId(12, 3423, 1763)).isEqualTo(19_078_479L);
        assertThat(PmtilesWriter.zoomOf(0)).isZero();
        assertThat(PmtilesWriter.zoomOf(4)).isEqualTo(1);
        assertThat(PmtilesWriter.zoomOf(5)).isEqualTo(2);
        assertThat(PmtilesWriter.zoomOf(19_078_479L)).isEqualTo(12);
    }

    @Test
    @DisplayName("the header is a 127-byte little-endian v3 header over the sections")
    void header() throws Exception {
        List<PmtilesWriter.Tile> tiles = List.of(
                new PmtilesWriter.Tile(2, 0, 0, tileData(10, 1)),
                new PmtilesWriter.Tile(2, 1, 0, tileData(20, 2)),
                new PmtilesWriter.Tile(2, 1, 1, tileData(30, 3)));
        byte[] archive = PmtilesWriter.write(tiles, Map.of("name", "test"), new double[] {-10, -5, 10, 5}, 2, 2,
                PmtilesWriter.TYPE_PNG);
        Reader reader = new Reader(archive);
        assertThat(new String(archive, 0, 7, StandardCharsets.US_ASCII)).isEqualTo("PMTiles");
        assertThat(archive[7]).isEqualTo((byte) 3);
        assertThat(reader.rootOffset()).isEqualTo(127L);
        assertThat(reader.metadataOffset()).isEqualTo(127L + reader.rootLength());
        assertThat(reader.leafOffset()).isEqualTo(reader.metadataOffset() + reader.metadataLength());
        assertThat(reader.tileOffset()).isEqualTo(reader.leafOffset() + reader.leafLength());
        assertThat(archive.length).isEqualTo(reader.tileOffset() + reader.tileLength());
        assertThat(reader.header.getLong(72)).isEqualTo(3L); // addressed tiles
        assertThat(reader.header.getLong(80)).isEqualTo(3L); // tile entries
        assertThat(reader.header.getLong(88)).isEqualTo(3L); // tile contents
        assertThat(archive[96]).isEqualTo((byte) 1); // clustered
        assertThat(archive[97]).isEqualTo((byte) PmtilesWriter.COMPRESSION_GZIP);
        assertThat(archive[98]).isEqualTo((byte) PmtilesWriter.COMPRESSION_NONE);
        assertThat(archive[99]).isEqualTo((byte) PmtilesWriter.TYPE_PNG);
        assertThat(archive[100]).isEqualTo((byte) 2);
        assertThat(archive[101]).isEqualTo((byte) 2);
        assertThat(reader.header.getInt(102)).isEqualTo(-100_000_000);
        assertThat(reader.header.getInt(106)).isEqualTo(-50_000_000);
        assertThat(reader.header.getInt(110)).isEqualTo(100_000_000);
        assertThat(reader.header.getInt(114)).isEqualTo(50_000_000);
        assertThat(archive[118]).isEqualTo((byte) 2); // center zoom
        assertThat(reader.header.getInt(119)).isZero();
        assertThat(reader.tileLength()).isEqualTo(60L);
        assertThat(reader.leafLength()).isZero();
        assertThat(reader.tile(2, 1, 0)).isEqualTo(tileData(20, 2));
        assertThat(reader.tile(2, 3, 3)).isNull();
    }

    @Test
    @DisplayName("the directory varint-encodes sorted ids, run lengths, lengths and contiguous offsets")
    void directoryRoundTrip() throws Exception {
        List<PmtilesWriter.Entry> entries = List.of(new PmtilesWriter.Entry(5, 0, 10, 1),
                new PmtilesWriter.Entry(42, 10, 300, 2), new PmtilesWriter.Entry(69, 0, 10, 1),
                new PmtilesWriter.Entry(1L << 40, 310, 5, 1));
        byte[] encoded = PmtilesWriter.serialize(entries);
        assertThat(decode(encoded)).isEqualTo(entries);
        ByteArrayOutputStream varint = new ByteArrayOutputStream();
        PmtilesWriter.writeVarint(varint, 300);
        assertThat(varint.toByteArray()).containsExactly(0xAC, 0x02);
        byte[] archive = PmtilesWriter.write(List.of(new PmtilesWriter.Tile(2, 1, 1, tileData(5, 9)),
                new PmtilesWriter.Tile(2, 0, 0, tileData(5, 1))), Map.of(), new double[] {-180, -85, 180, 85}, 2, 2,
                PmtilesWriter.TYPE_PNG);
        assertThat(new Reader(archive).root()).extracting(PmtilesWriter.Entry::tileId)
                .containsExactly(PmtilesWriter.tileId(2, 0, 0), PmtilesWriter.tileId(2, 1, 1));
    }

    @Test
    @DisplayName("identical consecutive ids collapse into a run; a repeat elsewhere shares the earlier offset")
    void deduplication() throws Exception {
        byte[] same = tileData(8, 7);
        // ids 5, 6, 7 (z2) consecutive with one content; z2 id 9 differs; z2 id 11 repeats the first content.
        List<PmtilesWriter.Tile> tiles = new ArrayList<>();
        Map<Long, int[]> byId = new LinkedHashMap<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                byId.put(PmtilesWriter.tileId(2, x, y), new int[] {x, y});
            }
        }
        for (long id : new long[] {5, 6, 7, 11}) {
            int[] xy = byId.get(id);
            tiles.add(new PmtilesWriter.Tile(2, xy[0], xy[1], same));
        }
        int[] other = byId.get(9L);
        tiles.add(new PmtilesWriter.Tile(2, other[0], other[1], tileData(4, 99)));
        byte[] archive = PmtilesWriter.write(tiles, Map.of(), new double[] {-180, -85, 180, 85}, 2, 2,
                PmtilesWriter.TYPE_PNG);
        Reader reader = new Reader(archive);
        List<PmtilesWriter.Entry> entries = reader.root();
        assertThat(entries).containsExactly(new PmtilesWriter.Entry(5, 0, 8, 3), new PmtilesWriter.Entry(9, 8, 4, 1),
                new PmtilesWriter.Entry(11, 0, 8, 1));
        assertThat(reader.header.getLong(72)).isEqualTo(5L);
        assertThat(reader.header.getLong(80)).isEqualTo(3L);
        assertThat(reader.header.getLong(88)).isEqualTo(2L);
        assertThat(reader.tileLength()).isEqualTo(12L);
        int[] xy = byId.get(11L);
        assertThat(reader.tile(2, xy[0], xy[1])).isEqualTo(same);
        // A run never bridges a gap: ids 5 and 7 with the same content stay two entries.
        List<PmtilesWriter.Tile> gap = List.of(new PmtilesWriter.Tile(2, byId.get(5L)[0], byId.get(5L)[1], same),
                new PmtilesWriter.Tile(2, byId.get(7L)[0], byId.get(7L)[1], same));
        List<PmtilesWriter.Entry> gapEntries = new Reader(PmtilesWriter.write(gap, Map.of(),
                new double[] {-180, -85, 180, 85}, 2, 2, PmtilesWriter.TYPE_PNG)).root();
        assertThat(gapEntries).containsExactly(new PmtilesWriter.Entry(5, 0, 8, 1), new PmtilesWriter.Entry(7, 0, 8, 1));
    }

    @Test
    @DisplayName("past the 16 KiB root the entries move to leaf directories and every tile stays addressable")
    void leafDirectories() throws Exception {
        Random random = new Random(42);
        List<PmtilesWriter.Tile> tiles = new ArrayList<>();
        int n = 1 << 8;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                byte[] data = new byte[8 + random.nextInt(40)];
                random.nextBytes(data);
                tiles.add(new PmtilesWriter.Tile(8, x, y, data));
            }
        }
        byte[] archive = PmtilesWriter.write(tiles, Map.of(), new double[] {-180, -85, 180, 85}, 8, 8,
                PmtilesWriter.TYPE_PNG);
        Reader reader = new Reader(archive);
        assertThat(127 + reader.rootLength()).isLessThanOrEqualTo(16_384L);
        assertThat(reader.leafLength()).isPositive();
        assertThat(reader.root()).allMatch(entry -> entry.runLength() == 0);
        List<PmtilesWriter.Entry> entries = reader.tileEntries();
        assertThat(entries).hasSize(n * n);
        for (int k = 1; k < entries.size(); k++) {
            assertThat(entries.get(k).tileId()).isGreaterThan(entries.get(k - 1).tileId());
            assertThat(entries.get(k).offset()).as("clustered").isEqualTo(entries.get(k - 1).offset()
                    + entries.get(k - 1).length());
        }
        PmtilesWriter.Tile probe = tiles.get(777);
        assertThat(reader.tile(probe.z(), probe.x(), probe.y())).isEqualTo(probe.data());
    }

    @Test
    @DisplayName("metadata is JSON through the mapper, so quotes and newlines are escaped")
    void metadataEscaping() throws Exception {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", "a \"quoted\"\nname");
        metadata.put("vector_layers", List.of(Map.of("id", "foresignal")));
        byte[] archive = PmtilesWriter.write(List.of(new PmtilesWriter.Tile(0, 0, 0, tileData(3, 1))), metadata,
                new double[] {-180, -85, 180, 85}, 0, 0, PmtilesWriter.TYPE_MVT, PmtilesWriter.COMPRESSION_GZIP);
        String json = new Reader(archive).metadata();
        assertThat(json).isEqualTo("{\"name\":\"a \\\"quoted\\\"\\nname\",\"vector_layers\":[{\"id\":\"foresignal\"}]}");
        assertThat(archive[98]).isEqualTo((byte) PmtilesWriter.COMPRESSION_GZIP);
        assertThat(archive[99]).isEqualTo((byte) PmtilesWriter.TYPE_MVT);
    }

    @Test
    @DisplayName("the streaming archive writes the same bytes as the in-memory writer, header zooms from the tiles")
    void streamingArchive() throws Exception {
        List<PmtilesWriter.Tile> tiles = List.of(new PmtilesWriter.Tile(3, 1, 1, tileData(6, 1)),
                new PmtilesWriter.Tile(3, 1, 2, tileData(6, 1)), new PmtilesWriter.Tile(4, 0, 0, tileData(9, 4)));
        Path target = dir.resolve("pack.pmtiles");
        try (PmtilesWriter.Archive archive = new PmtilesWriter.Archive(target, PmtilesWriter.TYPE_PNG,
                PmtilesWriter.COMPRESSION_NONE)) {
            for (PmtilesWriter.Tile tile : tiles) {
                archive.add(tile.z(), tile.x(), tile.y(), tile.data());
            }
            assertThat(archive.tiles()).isEqualTo(3);
            assertThat(archive.contents()).isEqualTo(2);
            assertThat(archive.finish(Map.of("name", "p"), new double[] {-10, -10, 10, 10}, 0, 8))
                    .isEqualTo(Files.size(target));
        }
        byte[] streamed = Files.readAllBytes(target);
        byte[] memory = PmtilesWriter.write(tiles, Map.of("name", "p"), new double[] {-10, -10, 10, 10}, 0, 8,
                PmtilesWriter.TYPE_PNG);
        assertThat(streamed).isEqualTo(memory);
        assertThat(streamed[100]).as("min zoom of the tiles present").isEqualTo((byte) 3);
        assertThat(streamed[101]).as("max zoom of the tiles present").isEqualTo((byte) 4);
        try (var listing = Files.list(dir)) {
            assertThat(listing.map(p -> p.getFileName().toString())).containsExactly("pack.pmtiles");
        }
        Path abandoned = dir.resolve("abandoned.pmtiles");
        try (PmtilesWriter.Archive archive = new PmtilesWriter.Archive(abandoned, PmtilesWriter.TYPE_PNG,
                PmtilesWriter.COMPRESSION_NONE)) {
            archive.add(0, 0, 0, tileData(2, 2));
        }
        try (var listing = Files.list(dir)) {
            assertThat(listing.map(p -> p.getFileName().toString())).containsExactly("pack.pmtiles");
        }
    }
}
