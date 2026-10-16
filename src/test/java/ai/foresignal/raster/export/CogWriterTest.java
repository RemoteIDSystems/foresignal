package ai.foresignal.raster.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The COG writer: IFD-before-data layout, GeoKeys, internal overviews and nodata (prompt 108). */
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

    @Test
    @DisplayName("all IFDs precede the tile data and carry the EPSG:4326 GeoKeys, an overview and GDAL_NODATA")
    void layout() {
        int width = 1024;
        int height = 1024;
        float[] values = new float[width * height];
        for (int k = 0; k < values.length; k++) {
            values[k] = k % 7 == 0 ? -9999f : 1.5f;
        }
        byte[] tiff = CogWriter.write(values, width, height, new double[] {-180, -85, 180, 85}, 4326, -9999, null);

        ByteBuffer buffer = ByteBuffer.wrap(tiff).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(buffer.get() == 'I' && buffer.get() == 'I').isTrue();
        assertThat(buffer.getShort()).isEqualTo((short) 42);
        long firstIfd = Integer.toUnsignedLong(buffer.getInt());
        assertThat(firstIfd).isEqualTo(8L);

        List<Map<Integer, long[]>> ifds = new ArrayList<>();
        long offset = firstIfd;
        while (offset != 0) {
            buffer.position((int) offset);
            int count = buffer.getShort() & 0xFFFF;
            Map<Integer, long[]> tags = new LinkedHashMap<>();
            Map<Integer, Long> external = new LinkedHashMap<>();
            for (int k = 0; k < count; k++) {
                int tag = buffer.getShort() & 0xFFFF;
                int type = buffer.getShort() & 0xFFFF;
                long n = Integer.toUnsignedLong(buffer.getInt());
                int bytes = sizeOf(type) * (int) n;
                int valuePos = buffer.position();
                if (bytes > 4) {
                    long ext = Integer.toUnsignedLong(buffer.getInt());
                    tags.put(tag, readValues(tiff, ext, type, n));
                    external.put(tag, ext);
                } else {
                    tags.put(tag, readInline(buffer, type, n));
                    buffer.position(valuePos + 4);
                }
            }
            long next = Integer.toUnsignedLong(buffer.getInt());
            ifds.add(tags);
            // Every IFD, and every external array, must sit before the tile data.
            long tileStart = tags.get(324)[0];
            assertThat(offset).isLessThan(tileStart);
            for (long ext : external.values()) {
                assertThat(ext).isLessThan(tileStart);
            }
            offset = next;
        }

        assertThat(ifds).hasSize(2);
        Map<Integer, long[]> main = ifds.get(0);
        assertThat(main.get(256)[0]).isEqualTo(1024); // ImageWidth
        assertThat(main.get(259)[0]).isEqualTo(8); // deflate
        assertThat(main.get(254)[0]).isZero(); // full-resolution
        Map<Integer, long[]> overview = ifds.get(1);
        assertThat(overview.get(254)[0]).isEqualTo(1); // reduced-resolution
        assertThat(overview.get(256)[0]).isEqualTo(512);
        long[] geo = main.get(34735);
        assertThat(geo).contains(4326L);
        assertThat(main.get(42113)).isNotEmpty(); // GDAL_NODATA
    }

    private static long[] readInline(ByteBuffer buffer, int type, long count) {
        int bytes = sizeOf(type) * (int) count;
        ByteBuffer slice = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
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
