package ai.foresignal.raster.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The COG writer: IFD-before-data layout, GeoKeys, internal overviews, nodata and the inline rule (prompt 108). */
class CogWriterTest {

    private static final int SHORT = 3;
    private static final int LONG = 4;
    private static final int DOUBLE = 12;
    private static final int ASCII = 2;

    private static int sizeOf(int type) {
        return switch (type) {
            case SHORT -> 2;
            case LONG -> 4;
            case DOUBLE -> 8;
            case ASCII -> 1;
            default -> 4;
        };
    }

    /** One parsed IFD: values per tag, and the offset of each out-of-line value. */
    private record Ifd(long offset, Map<Integer, long[]> values, Map<Integer, String> ascii,
            Map<Integer, Long> external) {}

    private static List<Ifd> parse(byte[] tiff) {
        ByteBuffer buffer = ByteBuffer.wrap(tiff).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(buffer.get() == 'I' && buffer.get() == 'I').isTrue();
        assertThat(buffer.getShort()).isEqualTo((short) 42);
        long offset = Integer.toUnsignedLong(buffer.getInt());
        assertThat(offset).isEqualTo(8L);
        List<Ifd> ifds = new ArrayList<>();
        while (offset != 0) {
            buffer.position((int) offset);
            int count = buffer.getShort() & 0xFFFF;
            Map<Integer, long[]> tags = new LinkedHashMap<>();
            Map<Integer, String> ascii = new LinkedHashMap<>();
            Map<Integer, Long> external = new LinkedHashMap<>();
            int previous = -1;
            for (int k = 0; k < count; k++) {
                int tag = buffer.getShort() & 0xFFFF;
                assertThat(tag).as("tags ascend").isGreaterThan(previous);
                previous = tag;
                int type = buffer.getShort() & 0xFFFF;
                long n = Integer.toUnsignedLong(buffer.getInt());
                int bytes = sizeOf(type) * (int) n;
                int valuePos = buffer.position();
                long at = bytes > 4 ? Integer.toUnsignedLong(buffer.getInt()) : valuePos;
                if (bytes > 4) {
                    external.put(tag, at);
                }
                tags.put(tag, readValues(tiff, at, type, n));
                if (type == ASCII) {
                    ascii.put(tag, new String(tiff, (int) at, (int) n - 1, StandardCharsets.US_ASCII));
                    assertThat(tiff[(int) (at + n - 1)]).as("NUL-terminated").isZero();
                }
                buffer.position(valuePos + 4);
            }
            long next = Integer.toUnsignedLong(buffer.getInt());
            ifds.add(new Ifd(offset, tags, ascii, external));
            offset = next;
        }
        return ifds;
    }

    private static float[] fill(int width, int height) {
        float[] values = new float[width * height];
        for (int k = 0; k < values.length; k++) {
            values[k] = k % 7 == 0 ? -9999f : 1.5f;
        }
        return values;
    }

    @Test
    @DisplayName("all IFDs precede the tile data and carry the EPSG:4326 GeoKeys, overviews and GDAL_NODATA")
    void layout() {
        byte[] tiff = CogWriter.write(fill(1024, 1024), 1024, 1024, new double[] {-180, -85, 180, 85}, 4326, -9999,
                null);
        List<Ifd> ifds = parse(tiff);
        long firstTile = Long.MAX_VALUE;
        for (Ifd ifd : ifds) {
            for (long v : ifd.values().get(324)) {
                firstTile = Math.min(firstTile, v);
            }
        }
        for (Ifd ifd : ifds) {
            assertThat(ifd.offset()).isLessThan(firstTile);
            for (long ext : ifd.external().values()) {
                assertThat(ext).isLessThan(firstTile);
            }
        }
        // 1024 -> 512 -> 256: factor-2 overviews until a level is below 512 px
        assertThat(ifds).hasSize(3);
        Map<Integer, long[]> main = ifds.get(0).values();
        assertThat(main.get(256)[0]).isEqualTo(1024);
        assertThat(main.get(259)[0]).isEqualTo(8);
        assertThat(main.get(254)[0]).isZero();
        assertThat(ifds.get(1).values().get(254)[0]).isEqualTo(1);
        assertThat(ifds.get(1).values().get(256)[0]).isEqualTo(512);
        assertThat(ifds.get(2).values().get(256)[0]).isEqualTo(256);
        long[] geo = main.get(34735);
        assertThat(geo).startsWith(1, 1, 0, 3).contains(2048L, 4326L);
        assertThat(ifds.get(0).ascii().get(42113)).isEqualTo("-9999");
    }

    @Test
    @DisplayName("a value of four bytes or fewer is stored inline: GDAL_NODATA \"0\" and a single tile's offset")
    void inlineRule() {
        byte[] tiff = CogWriter.write(fill(300, 200), 300, 200, new double[] {0, 0, 3, 2}, 4326, 0, null);
        List<Ifd> ifds = parse(tiff);
        assertThat(ifds).hasSize(1);
        Ifd ifd = ifds.get(0);
        assertThat(ifd.external()).doesNotContainKeys(42113, 324, 325);
        assertThat(ifd.ascii().get(42113)).isEqualTo("0");
        long tileOffset = ifd.values().get(324)[0];
        long tileLength = ifd.values().get(325)[0];
        assertThat(tileOffset + tileLength).isEqualTo(tiff.length);
        // eight-byte and longer values are out of line
        assertThat(ifd.external()).containsKeys(33550, 33922, 34735);
    }

    @Test
    @DisplayName("EPSG:3857 writes the projected GeoKeys; the copyright carries attribution and license")
    void webMercatorAndCopyright() {
        byte[] tiff = CogWriter.write(fill(64, 64), 64, 64,
                new double[] {-1_000_000, -1_000_000, 1_000_000, 1_000_000}, 3857, -9999, null,
                "Data: NOAA MRMS; license: us-gov-public-domain © Météo");
        Ifd ifd = parse(tiff).get(0);
        assertThat(ifd.values().get(34735)).containsSequence(1024L, 0L, 1L, 1L).containsSequence(3072L, 0L, 1L,
                3857L);
        assertThat(ifd.ascii().get(33432)).isEqualTo("Data: NOAA MRMS; license: us-gov-public-domain (c) Meteo");
    }

    @Test
    @DisplayName("overview levels stop below 512 px on the longer side")
    void levels() {
        assertThat(CogWriter.levels(new float[600 * 100], 600, 100, -9999)).hasSize(2);
        assertThat(CogWriter.levels(new float[511 * 511], 511, 511, -9999)).hasSize(1);
        assertThat(CogWriter.levels(new float[512 * 10], 512, 10, -9999)).hasSize(2);
    }

    private static long[] readValues(byte[] tiff, long offset, int type, long count) {
        ByteBuffer slice = ByteBuffer.wrap(tiff, (int) offset, sizeOf(type) * (int) count)
                .order(ByteOrder.LITTLE_ENDIAN);
        long[] out = new long[(int) count];
        for (int k = 0; k < count; k++) {
            out[k] = switch (type) {
                case SHORT -> slice.getShort() & 0xFFFF;
                case LONG -> Integer.toUnsignedLong(slice.getInt());
                case DOUBLE -> (long) slice.getDouble();
                default -> slice.get() & 0xFF;
            };
        }
        return out;
    }
}
