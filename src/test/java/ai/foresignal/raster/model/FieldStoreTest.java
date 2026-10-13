package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterPaths;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.testsupport.raster.ModelFixtures;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The precompressed God's Eye View fields (prompt 051), without Spring. */
class FieldStoreTest {

    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");

    @TempDir Path dir;

    RasterPaths paths;
    FieldStore store;

    @BeforeEach
    void setUp() {
        paths = new RasterPaths(dir);
        paths.createLayout();
        store = new FieldStore(paths);
    }

    static float[] fill(int n, float v) {
        float[] a = new float[n];
        java.util.Arrays.fill(a, v);
        return a;
    }

    @Test
    @DisplayName("writes 1° and 0.25° files: planar little-endian u then v, gzip, sha256 of the raw bytes")
    void writesBoth() {
        float[] u = new float[360 * 181];
        float[] v = new float[360 * 181];
        for (int i = 0; i < u.length; i++) {
            u[i] = i % 360;
            v[i] = 90 - i / 360;
        }
        Map<String, Object> stats = store.write("gfs", RUN, 6, ModelFixtures.GLOBAL_1DEG, u, v);
        Map<String, FieldStore.StoredField> files = FieldStore.fromStats(Map.of(FieldStore.STATS_KEY, stats));
        FieldStore.StoredField one = files.get(FieldStore.RES_1);
        assertThat(one.path()).isEqualTo("fields/gfs/2026101212Z/f006.1p00.f32.gz");
        assertThat(one.byteLength()).isEqualTo(521_280);
        assertThat(one.nx()).isEqualTo(360);
        assertThat(one.ny()).isEqualTo(181);
        byte[] raw = store.readRaw(one.path());
        assertThat(raw).hasSize(521_280);
        assertThat(ai.foresignal.raster.FrameStore.sha256Hex(raw)).isEqualTo(one.sha256());
        ByteBuffer b = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(b.getFloat(4 * 37)).as("the 1° file is the native grid unchanged").isEqualTo(37f);
        assertThat(b.getFloat(4 * (360 * 181 + 360 * 10))).isEqualTo(80f);
        assertThat(store.readGzip(one.path()).length).isEqualTo((int) one.gzipBytes());
        FieldStore.StoredField quarter = files.get(FieldStore.RES_QUARTER);
        assertThat(quarter.byteLength()).isEqualTo(8_305_920);
        assertThat(quarter.nx()).isEqualTo(1440);
        assertThat(quarter.dx()).isEqualTo(0.25);
        assertThat(store.exists(quarter.path())).isTrue();
        assertThat(store.exists("fields/gfs/none.f32.gz")).isFalse();
    }

    @Test
    @DisplayName("applies only to forecast frames of global field products with two physical bands")
    void applies() {
        ProductSpec field = ProductSpec.builder("gfs").kind(ProductSpec.KIND_FIELD).dtype(RasterDtype.I16).build();
        ProductSpec raster = ProductSpec.builder("gfs-gust").dtype(RasterDtype.I16).build();
        DecodedBands two = DecodedBands.physical(new float[1], new float[1]);
        DecodedBands one = DecodedBands.physical(new float[1]);
        GridDefinition regional = GridDefinition.latLon(4, 3, -100, 28, -94, 34);
        assertThat(FieldStore.applies(field, ModelFixtures.GLOBAL_1DEG, two, true)).isTrue();
        assertThat(FieldStore.applies(field, ModelFixtures.GLOBAL_1DEG, two, false)).isFalse();
        assertThat(FieldStore.applies(raster, ModelFixtures.GLOBAL_1DEG, two, true)).isFalse();
        assertThat(FieldStore.applies(field, ModelFixtures.GLOBAL_1DEG, one, true)).isFalse();
        assertThat(FieldStore.applies(field, regional, two, true)).isFalse();
        assertThat(FieldStore.applies(field, ModelFixtures.GLOBAL_1DEG, null, true)).isFalse();
        assertThatThrownBy(() -> store.write("gfs", RUN, 0, regional, new float[12], new float[12]))
                .isInstanceOf(IllegalArgumentException.class);
        float[] nan = fill(360 * 181, 1);
        nan[3] = Float.NaN;
        assertThatThrownBy(() -> store.write("gfs", RUN, 0, ModelFixtures.GLOBAL_1DEG, nan, fill(360 * 181, 1)))
                .hasMessage("Invalid wind grid values");
    }

    @Test
    @DisplayName("the sweep removes run directories with no frames left, after the grace period")
    void sweep() throws Exception {
        store.write("gfs", RUN, 0, ModelFixtures.GLOBAL_1DEG, fill(360 * 181, 1), fill(360 * 181, 2));
        store.write("gfs", RUN.plusSeconds(6 * 3600), 0, ModelFixtures.GLOBAL_1DEG, fill(360 * 181, 1),
                fill(360 * 181, 2));
        Path old = paths.fields().resolve("gfs/2026101212Z");
        Path kept = paths.fields().resolve("gfs/2026101218Z");
        Instant now = Instant.parse("2030-01-01T00:00:00Z");
        assertThat(store.sweep(p -> Set.of("2026101218Z"), Instant.EPOCH)).as("inside the grace period").isZero();
        Files.setLastModifiedTime(old, FileTime.from(Instant.parse("2026-10-12T12:00:00Z")));
        Files.setLastModifiedTime(kept, FileTime.from(Instant.parse("2026-10-12T12:00:00Z")));
        assertThat(store.sweep(p -> Set.of("2026101218Z"), now)).isEqualTo(2);
        assertThat(old).doesNotExist();
        assertThat(kept).isDirectory();
        assertThat(new FieldStore(new RasterPaths(dir.resolve("empty"))).sweep(p -> Set.of(), now)).isZero();
    }

    @Test
    @DisplayName("gzip and gunzip round-trip; malformed stats entries are ignored")
    void helpers() {
        byte[] raw = FieldStore.planar(new float[] {1, 2}, new float[] {3, 4});
        assertThat(raw).hasSize(16);
        assertThat(FieldStore.gunzip(FieldStore.gzip(raw))).isEqualTo(raw);
        assertThat(FieldStore.fromStats(null)).isEmpty();
        assertThat(FieldStore.fromStats(Map.of(FieldStore.STATS_KEY, Map.of("1", "oops")))).isEmpty();
        assertThat(FieldStore.relativePath("gfs", RUN, 120, FieldStore.RES_QUARTER))
                .isEqualTo("fields/gfs/2026101212Z/f120.0p25.f32.gz");
    }
}
