package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FsgHeader;
import ai.foresignal.raster.LatLonGrid;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** EDR {@code /area} native-grid subsets (prompt 108, post-review fixes). */
class EdrAreaTest {

    private static ProductSpec spec() {
        return ProductSpec.builder("t-u8")
                .title("T")
                .category("model")
                .units("K")
                .dtype(RasterDtype.U8)
                .scaleOffset(1.0, 0.0)
                .nodata(255.0)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(10), Duration.ofMinutes(30)))
                .spatial(new ProductSpec.Spatial("global", 4, 0))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
    }

    /** A 1-degree global grid whose code is {@code (row * 7 + col) % 250}, with one no-data cell. */
    private static FrameView global() {
        int nx = 360;
        int ny = 180;
        byte[] payload = new byte[nx * ny];
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                payload[j * nx + i] = (byte) ((j * 7 + i) % 250);
            }
        }
        // the cell centered at (10.5, 45.5): row 44, column 190
        payload[44 * nx + 190] = (byte) 255;
        LatLonGrid grid = new LatLonGrid(nx, ny, -179.5, 89.5, 1.0, 1.0, true);
        ElgCodec.Decoded decoded = new ElgCodec.Decoded(new FsgHeader(RasterDtype.U8, FsgHeader.CODEC_NONE, nx, ny, 1,
                false, 1.0, 0.0, 255.0), payload);
        return new FrameView(spec(), null, grid, decoded);
    }

    @Test
    @DisplayName("a regular grid answers its own cells in the box: a Grid domain with flat row-major values")
    void nativeCells() {
        EdrArea.Subset subset = EdrArea.of(global(), new double[] {10, 44, 13, 46}, 100_000);
        assertThat(subset.tooLarge()).isFalse();
        // centers 10.5, 11.5, 12.5 by 45.5, 44.5 (north to south)
        assertThat(subset.shape()).containsExactly(2, 3);
        assertThat(subset.axisNames()).containsExactly("y", "x");
        assertThat(subset.values()).hasSize(6);
        assertThat(subset.values().get(0)).isNull(); // the no-data cell (45.5, 10.5) is the null mask
        assertThat(subset.values().get(1)).isEqualTo((double) ((44 * 7 + 191) % 250));
        assertThat(subset.values().get(3)).isEqualTo((double) ((45 * 7 + 190) % 250));
        Map<?, ?> axes = (Map<?, ?>) subset.domain().get("axes");
        Map<?, ?> x = (Map<?, ?>) axes.get("x");
        assertThat(x.get("start")).isEqualTo(10.5);
        assertThat(x.get("stop")).isEqualTo(12.5);
        assertThat(x.get("num")).isEqualTo(3);
        assertThat(subset.domain().get("domainType")).isEqualTo("Grid");
    }

    @Test
    @DisplayName("a continental box at 1 degree fits the cap; the cap counts native cells, not a fixed lattice")
    void capOnNativeCells() {
        EdrArea.Subset ok = EdrArea.of(global(), new double[] {-130, 20, -60, 55}, 100_000);
        assertThat(ok.tooLarge()).isFalse();
        assertThat(ok.cells()).isEqualTo(70L * 35L);
        EdrArea.Subset big = EdrArea.of(global(), new double[] {-180, -90, 180, 90}, 1_000);
        assertThat(big.tooLarge()).isTrue();
        assertThat(big.cells()).isGreaterThan(1_000);
    }

    @Test
    @DisplayName("a box across the antimeridian keeps x ascending in unwrapped longitude")
    void antimeridian() {
        EdrArea.Subset subset = EdrArea.of(global(), new double[] {178, 0, -178, 1}, 100_000);
        Map<?, ?> axes = (Map<?, ?>) subset.domain().get("axes");
        Map<?, ?> x = (Map<?, ?>) axes.get("x");
        assertThat(x.get("start")).isEqualTo(178.5);
        assertThat(x.get("stop")).isEqualTo(181.5);
        assertThat(x.get("num")).isEqualTo(4);
        assertThat(subset.shape()).isEqualTo(List.of(1, 4));
    }

    @Test
    @DisplayName("values are a flat list, never nested")
    void flat() {
        EdrArea.Subset subset = EdrArea.of(global(), new double[] {0, 0, 2, 2}, 100_000);
        List<Object> raw = new java.util.ArrayList<>(subset.values());
        assertThat(raw).allMatch(v -> v == null || v instanceof Double);
        assertThat(subset.values().size())
                .isEqualTo(Arrays.stream(subset.shape().toArray(Integer[]::new)).reduce(1, (a, b) -> a * b));
    }
}
