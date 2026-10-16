package ai.foresignal.export.pmtiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The PMTiles v3 writer: the 127-byte header and the root directory (prompt 108). */
class PmtilesWriterTest {

    private static byte[] tileData(int size, int seed) {
        byte[] bytes = new byte[size];
        for (int k = 0; k < size; k++) {
            bytes[k] = (byte) (seed + k);
        }
        return bytes;
    }

    @Test
    @DisplayName("the header is a 127-byte PMTiles v3 archive over the tile data section")
    void header() throws Exception {
        List<PmtilesWriter.Tile> tiles = List.of(
                new PmtilesWriter.Tile(2, 0, 0, tileData(10, 1)),
                new PmtilesWriter.Tile(2, 1, 0, tileData(20, 2)),
                new PmtilesWriter.Tile(2, 1, 1, tileData(30, 3)));
        byte[] archive = PmtilesWriter.write(tiles, Map.of("name", "test"), new double[] {-180, -85, 180, 85}, 2, 2,
                PmtilesWriter.TYPE_PNG);
        assertThat(new String(archive, 0, 7, StandardCharsets.US_ASCII)).isEqualTo("PMTiles");
        ByteBuffer header = ByteBuffer.wrap(archive).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(archive[7]).isEqualTo((byte) 3);
        assertThat(header.getLong(8)).isEqualTo(127L);
        assertThat(header.getLong(72)).isEqualTo(3L); // addressed tiles
        assertThat(header.getLong(80)).isEqualTo(3L); // tile entries
        assertThat(archive[96]).isEqualTo((byte) 1); // clustered
        assertThat(archive[97]).isEqualTo((byte) PmtilesWriter.COMPRESSION_GZIP);
        assertThat(archive[98]).isEqualTo((byte) PmtilesWriter.COMPRESSION_NONE);
        assertThat(archive[99]).isEqualTo((byte) PmtilesWriter.TYPE_PNG);
        assertThat(archive[100]).isEqualTo((byte) 2);
        assertThat(archive[101]).isEqualTo((byte) 2);
        assertThat(header.getLong(64)).isEqualTo(60L); // tile data length
        assertThat(header.getLong(48)).isZero(); // no leaf directories
    }

    @Test
    @DisplayName("the root directory varint-encodes the sorted Hilbert tile ids")
    void directory() throws Exception {
        List<PmtilesWriter.Tile> tiles = List.of(
                new PmtilesWriter.Tile(2, 1, 1, tileData(5, 9)),
                new PmtilesWriter.Tile(2, 0, 0, tileData(5, 1)));
        byte[] archive = PmtilesWriter.write(tiles, Map.of(), new double[] {-180, -85, 180, 85}, 2, 2,
                PmtilesWriter.TYPE_PNG);
        ByteBuffer header = ByteBuffer.wrap(archive).order(ByteOrder.LITTLE_ENDIAN);
        long rootOffset = header.getLong(8);
        int rootLength = (int) header.getLong(16);
        byte[] root = new byte[rootLength];
        System.arraycopy(archive, (int) rootOffset, root, 0, rootLength);
        byte[] decoded = gunzip(root);
        List<Long> ids = decodeIds(decoded, 2);
        assertThat(ids).containsExactly(
                PmtilesWriter.tileId(2, 0, 0),
                PmtilesWriter.tileId(2, 1, 1));
    }

    /** Reads the count and the delta-encoded tile ids of a directory. */
    private static List<Long> decodeIds(byte[] directory, int expected) {
        Cursor cursor = new Cursor(directory);
        long count = cursor.varint();
        assertThat(count).isEqualTo(expected);
        List<Long> ids = new ArrayList<>();
        long last = 0;
        for (int k = 0; k < count; k++) {
            last += cursor.varint();
            ids.add(last);
        }
        return ids;
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
}
