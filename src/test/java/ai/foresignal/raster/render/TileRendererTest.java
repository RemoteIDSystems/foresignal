package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The tile renderer of prompt 045: a golden synthetic frame compared pixel by pixel after decoding
 * (never by file bytes), {@code @2x} dimensions, the shared transparent tile and its fixed ETag,
 * {@code TILE_OUT_OF_RANGE}, the PNG {@code tRNS} chunk, and bilinear sampling.
 */
class TileRendererTest {

    private static final StyleRegistry STYLES = new StyleRegistry();

    /** Two cells side by side over the whole WebMercator world: west 50 dBZ, east no coverage. */
    private static final GridDef WORLD_2X1 = GridDef.of(new GridDefinition(GridDefinition.LATLON, 2, 1, -180,
            -RenderFixtures.LAT, 180, RenderFixtures.LAT, java.util.Map.of()));

    private static BufferedImage decode(byte[] png) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(png));
    }

    private static TileRenderer.Drawn drawWorld(int scale) {
        ProductSpec spec = RenderFixtures.reflectivity();
        CompiledStyle style = STYLES.compiled(STYLES.resolve("nws-reflectivity@1").style(), spec);
        int size = 256 * scale;
        IndexMap map = IndexMap.build(WORLD_2X1, size, size, IndexMap.tile(0, 0, 0, size), false);
        return TileRenderer.draw(RenderFixtures.u8(2, 1, 166, 0), style, map);
    }

    @Test
    @DisplayName("golden frame: the west half is the 50 dBZ color, the east half (no coverage) transparent")
    void goldenPixels() throws Exception {
        TileRenderer.Drawn drawn = drawWorld(1);
        assertThat(drawn.empty()).isFalse();
        BufferedImage image = decode(drawn.png());
        assertThat(image.getWidth()).isEqualTo(256);
        assertThat(image.getHeight()).isEqualTo(256);
        int red = 0xFFFD0000;
        for (int y = 0; y < 256; y += 17) {
            for (int x = 0; x < 256; x++) {
                int expected = x < 128 ? red : 0;
                int actual = image.getRGB(x, y);
                assertThat(actual >>> 24 == 0 ? 0 : actual).as("pixel %d,%d", x, y).isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("@2x renders 512 px for the same extent")
    void retina() throws Exception {
        BufferedImage image = decode(drawWorld(2).png());
        assertThat(image.getWidth()).isEqualTo(512);
        assertThat(image.getHeight()).isEqualTo(512);
        assertThat(image.getRGB(255, 256)).isEqualTo(0xFFFD0000);
        assertThat(image.getRGB(256, 256) >>> 24).isZero();
    }

    @Test
    @DisplayName("the PNG is palette-based with a tRNS chunk, and not gzipped")
    void paletteWithTrns() {
        byte[] png = drawWorld(1).png();
        String text = new String(png, StandardCharsets.ISO_8859_1);
        assertThat(text).contains("PLTE").contains("tRNS");
        assertThat(png[0] & 0xFF).isEqualTo(0x89);
        assertThat(png[25]).as("IHDR color type 3: indexed").isEqualTo((byte) 3);
    }

    @Test
    @DisplayName("an all-special frame and an off-grid tile are empty")
    void emptyRenders() {
        ProductSpec spec = RenderFixtures.reflectivity();
        CompiledStyle style = STYLES.compiled(STYLES.resolve("nws-reflectivity@1").style(), spec);
        IndexMap map = IndexMap.build(WORLD_2X1, 256, 256, IndexMap.tile(0, 0, 0, 256), false);
        assertThat(TileRenderer.draw(RenderFixtures.u8(2, 1, 0, 1), style, map).empty()).isTrue();
        GridDef texas = GridDef.of(GridDefinition.latLon(4, 3, -100, 28, -94, 34));
        IndexMap offGrid = IndexMap.build(texas, 256, 256, IndexMap.tile(4, 0, 0, 256), false);
        assertThat(offGrid.isEmpty()).isTrue();
        assertThat(TileRenderer.draw(RenderFixtures.u8(4, 3, new int[12]), style, offGrid).empty()).isTrue();
    }

    @Test
    @DisplayName("the shared transparent tile is one 256 px PNG with one fixed ETag")
    void transparentTile() throws Exception {
        assertThat(TransparentTile.png()).isEqualTo(TransparentTile.png());
        assertThat(TransparentTile.ETAG).isEqualTo("\"fs-transparent-1\"");
        BufferedImage image = decode(TransparentTile.png());
        assertThat(image.getWidth()).isEqualTo(256);
        assertThat(image.getRGB(10, 10) >>> 24).isZero();
        assertThat(TransparentTile.length()).isEqualTo(TransparentTile.png().length);
    }

    @Test
    @DisplayName("above native max zoom, below min zoom, or outside the tile range is TILE_OUT_OF_RANGE")
    void outOfRange() {
        ProductSpec spec = RenderFixtures.reflectivity();
        RasterRenderService.checkTile(spec, 8, 59, 105);
        assertThatThrownBy(() -> RasterRenderService.checkTile(spec, 9, 0, 0))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("TILE_OUT_OF_RANGE");
                    assertThat(e.getStatus().value()).isEqualTo(400);
                });
        assertThatThrownBy(() -> RasterRenderService.checkTile(spec, 2, 4, 0))
                .isInstanceOfSatisfying(ForesignalException.class, e -> assertThat(e.getCode())
                        .isEqualTo("TILE_OUT_OF_RANGE"));
        ProductSpec min3 = ProductSpec.builder("m").dtype(spec.dtype())
                .spatial(new ProductSpec.Spatial("CONUS", 8, 3)).build();
        assertThatThrownBy(() -> RasterRenderService.checkTile(min3, 2, 0, 0))
                .isInstanceOf(ForesignalException.class);
    }

    @Test
    @DisplayName("bilinear sampling blends physical values; nearest keeps the cell value")
    void bilinear() {
        GridDef grid = GridDef.of(new GridDefinition(GridDefinition.LATLON, 2, 1, -180, -RenderFixtures.LAT, 180,
                RenderFixtures.LAT, java.util.Map.of()));
        ProductSpec linear = RenderFixtures.temperature(true);
        ProductSpec nearest = RenderFixtures.temperature(false);
        StyleDefinition temperature = STYLES.resolve("temperature@1").style();
        IndexMap bilinearMap = IndexMap.build(grid, 256, 256, IndexMap.tile(0, 0, 0, 256), true);
        IndexMap nearestMap = IndexMap.build(grid, 256, 256, IndexMap.tile(0, 0, 0, 256), false);
        assertThat(bilinearMap.bilinear()).isTrue();
        // -40 °C west (233.15 K), +50 °C east (323.15 K).
        TileRenderer.Drawn smooth = TileRenderer.draw(RenderFixtures.i16(2, 1, -4000, 5000),
                STYLES.compiled(temperature, linear), bilinearMap);
        TileRenderer.Drawn blocky = TileRenderer.draw(RenderFixtures.i16(2, 1, -4000, 5000),
                STYLES.compiled(temperature, nearest), nearestMap);
        int mid = 128 * 256 + 127;
        int left = 128 * 256 + 70;
        assertThat(smooth.pixels()[mid] & 0xFF).isGreaterThan(smooth.pixels()[left] & 0xFF);
        assertThat(blocky.pixels()[mid] & 0xFF).isEqualTo(blocky.pixels()[left] & 0xFF);
    }

    @Test
    @DisplayName("JPEG output decodes to the same size")
    void jpeg() throws Exception {
        BufferedImage image = decode(drawWorld(1).jpeg());
        assertThat(image.getWidth()).isEqualTo(256);
        assertThat(image.getRGB(200, 128) & 0xFFFFFF).as("transparent becomes black").isLessThan(0x101010);
    }
}
