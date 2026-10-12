package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.TileWarmer;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Warming (prompt 045): committing a complete frame renders, without any request, the tiles over the
 * places-AOI envelope (here the default Texas box: the test has no places) up to the product's
 * {@code maxZoomForAnimation} (7) at 1× and 2× into the tile cache, and nothing outside the AOI; a
 * partial frame warms nothing.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class WarmingIT extends RasterApiSupport {

    /** A 40 × 20 grid over the south-western US, larger than the AOI. */
    static final GridDefinition WIDE = GridDefinition.latLon(40, 20, -120.0, 20.0, -80.0, 40.0);

    @Autowired TileCache tiles;
    @Autowired TileWarmer warmer;
    @Autowired JdbcTemplate jdbc;

    private static int[] tileAt(int z, double lon, double lat) {
        double size = 2 * WebMercator.HALF / WebMercator.tiles(z);
        return new int[] {(int) Math.floor((WebMercator.x(lon) + WebMercator.HALF) / size),
            (int) Math.floor((WebMercator.HALF - WebMercator.y(lat)) / size)};
    }

    private static FrameInput wide(java.time.Instant t, boolean partial) {
        int[] codes = new int[40 * 20];
        Arrays.fill(codes, 126);
        return FrameInput.builder(OBS).validTime(t).grid(WIDE).bands(DecodedBands.codes(codes)).partial(partial)
                .build();
    }

    private TileCache.Key key(RasterFrameRow frame, int z, int[] xy, int scale) {
        return TileCache.Key.tile(OBS, frame.frameKey(), "nws-reflectivity@1", z, xy[0], xy[1], scale);
    }

    @Test
    @DisplayName("a complete frame warms the AOI tiles at z ≤ 7 at 1× and 2×, and none outside the AOI")
    void warmsAoiOnly() throws Exception {
        pipeline.commit(SOURCE, wide(T, false));
        RasterFrameRow frame = frames.atValidTime(OBS, T).get(0);
        int[] austin7 = tileAt(7, -97.74, 30.27);
        int[] austin0 = tileAt(0, -97.74, 30.27);
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(tiles.get(key(frame, 7, austin7, 1))).isNotNull();
            assertThat(tiles.get(key(frame, 7, austin7, 2))).isNotNull();
            assertThat(tiles.get(key(frame, 0, austin0, 1))).isNotNull();
        });
        assertThat(tiles.get(key(frame, 7, austin7, 1)).length).as("a rendered, non-empty tile").isPositive();
        int[] losAngeles = tileAt(7, -118.2, 34.05);
        assertThat(tiles.get(key(frame, 7, losAngeles, 1))).as("inside the grid, outside the AOI").isNull();
        assertThat(tiles.get(key(frame, 8, tileAt(8, -97.74, 30.27), 1))).as("above maxZoomForAnimation").isNull();
        assertThat(fetchCount()).as("warming never fetches upstream").isZero();
    }

    @Test
    @DisplayName("a partial frame warms nothing; the warmer reports how many tiles it queued")
    void partialFramesAreNotWarmed() throws Exception {
        pipeline.commit(SOURCE, wide(T, true));
        RasterFrameRow partial = frames.atValidTime(OBS, T).get(0);
        assertThat(warmer.warm(OBS, partial.frameId())).isZero();
        pipeline.commit(SOURCE, wide(T.plusSeconds(120), false));
        RasterFrameRow complete = frames.atValidTime(OBS, T.plusSeconds(120)).get(0);
        assertThat(warmer.warm(OBS, complete.frameId())).isBetween(1, TileWarmer.MAX_TILES);
        assertThat(warmer.warm("no-such-product", "x")).isZero();
    }

    private int fetchCount() {
        return jdbc.queryForObject("select count(*) from fetch_log", Integer.class);
    }
}
