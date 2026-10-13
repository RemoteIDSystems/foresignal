package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link BasemapStore} (prompt 055): the committed Natural Earth asset loads with its five layers and the
 * part counts {@code scripts/build-basemap.py} reported (Natural Earth v5.1.2), the per-layer cell index
 * culls to a map's box, the delta decoding is exact, and a corrupt file is refused.
 */
class BasemapStoreTest {

    final BasemapStore store = new BasemapStore();

    @Test
    @DisplayName("the shipped asset has the five layers with the generated part counts and bounding boxes")
    void loads() {
        Map<String, BasemapStore.Layer> layers = store.layers();
        assertThat(layers.keySet()).containsExactly("land", "lakes", "coastline", "admin0", "admin1");
        assertThat(store.layer(BasemapStore.LAND).parts()).hasSize(1421);
        assertThat(store.layer(BasemapStore.LAKES).parts()).hasSize(412);
        assertThat(store.layer(BasemapStore.COASTLINE).parts()).hasSize(1429);
        assertThat(store.layer(BasemapStore.ADMIN0).parts()).hasSize(393);
        assertThat(store.layer(BasemapStore.ADMIN1).parts()).hasSize(592);
        assertThat(store.layer(BasemapStore.LAND).polygons()).isTrue();
        assertThat(store.layer(BasemapStore.COASTLINE).polygons()).isFalse();
        assertThat(store.layers()).as("loaded once").isSameAs(layers);
    }

    @Test
    @DisplayName("every decoded point lies inside its part's bounding box (the deltas add up)")
    void decodedPointsInsideBoxes() {
        for (BasemapStore.Layer layer : store.layers().values()) {
            for (BasemapStore.Part part : layer.parts()) {
                for (int[] ring : part.rings()) {
                    for (int i = 0; i < ring.length; i += 2) {
                        assertThat(ring[i]).isBetween(part.minLon(), part.maxLon());
                        assertThat(ring[i + 1]).isBetween(part.minLat(), part.maxLat());
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("a query around Austin returns a few nearby parts, all intersecting; the open ocean has no land")
    void culling() {
        BasemapStore.Layer admin1 = store.layer(BasemapStore.ADMIN1);
        List<BasemapStore.Part> texas = admin1.query(-107, 25, -93, 37);
        assertThat(texas).isNotEmpty().hasSizeLessThan(admin1.parts().size() / 4);
        assertThat(texas).allMatch(p -> p.intersects(-107, 25, -93, 37));
        assertThat(store.layer(BasemapStore.LAND).query(-150, -40, -140, -35)).isEmpty();
        assertThat(store.layer(BasemapStore.COASTLINE).query(-96, 27, -94, 30)).as("the Texas coast").isNotEmpty();
    }

    @Test
    @DisplayName("a box across ±180° returns the parts of both sides once")
    void antimeridian() {
        BasemapStore.Layer land = store.layer(BasemapStore.LAND);
        List<BasemapStore.Part> across = land.query(170, 50, -165, 72);
        assertThat(across).doesNotHaveDuplicates();
        assertThat(across).anyMatch(p -> p.maxLon() > 170_000_000).anyMatch(p -> p.minLon() < -165_000_000);
    }

    @Test
    @DisplayName("a hand-built file decodes exactly: zigzag varint deltas, negative steps, two rings")
    void parsesHandBuiltFile() {
        byte[] file = file(new int[][] {{10_000_000, 20_000_000, 10_000_100, 19_999_900, 9_999_000, 20_000_500},
            {1, 2, 3, 4}});
        BasemapStore.Layer layer = BasemapStore.parse(file).get("test");
        assertThat(layer.polygons()).isTrue();
        assertThat(layer.parts()).hasSize(1);
        assertThat(layer.parts().get(0).rings().get(0))
                .containsExactly(10_000_000, 20_000_000, 10_000_100, 19_999_900, 9_999_000, 20_000_500);
        assertThat(layer.parts().get(0).rings().get(1)).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("a wrong magic, an unknown version or a truncated file is refused")
    void refusesCorruptFiles() {
        byte[] good = file(new int[][] {{0, 0, 1_000_000, 1_000_000}});
        byte[] magic = good.clone();
        magic[0] = 'X';
        assertThatThrownBy(() -> BasemapStore.parse(magic)).hasMessageContaining("magic");
        byte[] version = good.clone();
        version[4] = 9;
        assertThatThrownBy(() -> BasemapStore.parse(version)).hasMessageContaining("version");
        byte[] truncated = java.util.Arrays.copyOf(good, good.length - 3);
        assertThatThrownBy(() -> BasemapStore.parse(truncated)).isInstanceOf(IllegalArgumentException.class);
    }

    /** A one-layer, one-part polygon file in the format of {@code scripts/build-basemap.py}. */
    static byte[] file(int[][] rings) {
        ByteArrayOutputStream part = new ByteArrayOutputStream();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int[] ring : rings) {
            for (int i = 0; i < ring.length; i += 2) {
                minX = Math.min(minX, ring[i]);
                maxX = Math.max(maxX, ring[i]);
                minY = Math.min(minY, ring[i + 1]);
                maxY = Math.max(maxY, ring[i + 1]);
            }
        }
        ByteBuffer head = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        head.putInt(minX).putInt(minY).putInt(maxX).putInt(maxY).putShort((short) rings.length).putShort((short) 0);
        part.writeBytes(head.array());
        for (int[] ring : rings) {
            ByteBuffer r = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
            r.putInt(ring.length / 2).putInt(ring[0]).putInt(ring[1]);
            part.writeBytes(r.array());
            for (int i = 2; i < ring.length; i += 2) {
                varint(part, zigzag(ring[i] - ring[i - 2]));
                varint(part, zigzag(ring[i + 1] - ring[i - 1]));
            }
        }
        byte[] data = part.toByteArray();
        ByteBuffer out = ByteBuffer.allocate(12 + 48 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        out.put("FSBM".getBytes(StandardCharsets.US_ASCII)).putShort((short) 1).putShort((short) 1).putInt(0);
        byte[] name = new byte[16];
        System.arraycopy("test".getBytes(StandardCharsets.US_ASCII), 0, name, 0, 4);
        out.put(name).put((byte) 1).put(new byte[3]).putInt(1).putInt(60).putInt(data.length)
                .putInt(minX).putInt(minY).putInt(maxX).putInt(maxY);
        out.put(data);
        return out.array();
    }

    private static long zigzag(long n) {
        return (n << 1) ^ (n >> 63);
    }

    private static void varint(ByteArrayOutputStream out, long n) {
        while (true) {
            int b = (int) (n & 0x7F);
            n >>>= 7;
            if (n != 0) {
                out.write(b | 0x80);
            } else {
                out.write(b);
                return;
            }
        }
    }
}
