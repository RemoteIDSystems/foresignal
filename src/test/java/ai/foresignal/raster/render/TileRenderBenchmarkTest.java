package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMH-free microbenchmark of the prompt 045 render path (Weather-Imagery-Design §0.1 targets 4–8 ms
 * per tile): an MRMS-sized 7000 × 3500 u8 frame, cached index maps, gather plus palette plus PNG-8.
 * It logs timings only and asserts nothing about speed, so a slow build host never fails it.
 */
class TileRenderBenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(TileRenderBenchmarkTest.class);

    @Test
    @DisplayName("logs the per-tile render time over an MRMS-sized frame")
    void logsTimings() {
        int nx = 7000;
        int ny = 3500;
        GridDef grid = GridDef.of(GridDefinition.latLon(nx, ny, -130.0, 20.0, -60.0, 55.0));
        int[] codes = new int[nx * ny];
        Random random = new Random(45);
        for (int i = 0; i < codes.length; i++) {
            codes[i] = random.nextInt(10) < 7 ? 1 : 2 + random.nextInt(254);
        }
        ProductSpec spec = RenderFixtures.reflectivity();
        StyleRegistry styles = new StyleRegistry();
        CompiledStyle style = styles.compiled(styles.resolve("nws-reflectivity@1").style(), spec);
        var frame = RenderFixtures.u8(nx, ny, codes);
        IndexMapCache maps = new IndexMapCache(256L * 1024 * 1024);
        int z = 7;
        int[] range = WebMercator.tileRange(z, -100, 28, -94, 34);
        int tiles = 0;
        long mapNanos = 0;
        long renderNanos = 0;
        long bytes = 0;
        for (int x = range[0]; x <= range[2]; x++) {
            for (int y = range[1]; y <= range[3]; y++) {
                long t0 = System.nanoTime();
                IndexMap map = maps.tile("bench", grid, z, x, y, 1, false);
                long t1 = System.nanoTime();
                byte[] png = TileRenderer.draw(frame, style, map).png();
                long t2 = System.nanoTime();
                mapNanos += t1 - t0;
                renderNanos += t2 - t1;
                bytes += png.length;
                tiles++;
            }
        }
        long warmNanos = 0;
        for (int x = range[0]; x <= range[2]; x++) {
            for (int y = range[1]; y <= range[3]; y++) {
                long t0 = System.nanoTime();
                TileRenderer.draw(frame, style, maps.tile("bench", grid, z, x, y, 1, false)).png();
                warmNanos += System.nanoTime() - t0;
            }
        }
        log.info("Rendered {} z{} tiles: index map {} ms/tile (cold), render {} ms/tile (cold), {} ms/tile with cached"
                + " maps, {} bytes/tile", tiles, z, String.format("%.2f", mapNanos / 1e6 / tiles),
                String.format("%.2f", renderNanos / 1e6 / tiles), String.format("%.2f", warmNanos / 1e6 / tiles),
                bytes / tiles);
        assertThat(tiles).isPositive();
    }
}
