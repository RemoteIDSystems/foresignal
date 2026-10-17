package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code loop.gif} renders through the queue and caches content-addressed sets (prompt 108, post-review fixes). */
class LoopGifRenderTest {

    /** Records every queued image request instead of drawing. */
    static final class RecordingRenderer extends RasterRenderService {
        final List<RenderQueue.Priority> priorities = new ArrayList<>();

        RecordingRenderer(TileCache cache) {
            super(null, null, StyleRegistry.shipped(), null, cache, null,
                    new ai.foresignal.media.GuardedImages(new ai.foresignal.media.ImageGuard(25L * 1024 * 1024,
                            new io.micrometer.core.instrument.simple.SimpleMeterRegistry())));
        }

        @Override
        public byte[] image(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style,
                ImageRequest request, String format, RenderQueue.Priority priority, String client) {
            priorities.add(priority);
            BufferedImage img = new BufferedImage(request.width(), request.height(), BufferedImage.TYPE_INT_ARGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ImageIO.write(img, "png", out);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return out.toByteArray();
        }
    }

    private static List<RasterRenderService.LoopFrame> frames() {
        ProductSpec spec = MapFixtures.reflectivity();
        RasterFrameRow frame = MapFixtures.frame();
        StyleRegistry.Resolved style = MapFixtures.reflectivityStyle();
        return List.of(new RasterRenderService.LoopFrame(spec, frame, style),
                new RasterRenderService.LoopFrame(spec, frame, style));
    }

    private static final RasterRenderService.ImageRequest BOX = new RasterRenderService.ImageRequest(-98, 29, -96,
            31, 64, 64, true, false);

    @Test
    @DisplayName("each frame is a queued image at prefetch priority, never drawDirect")
    void queued() {
        RecordingRenderer renderer = new RecordingRenderer(new TileCache(1 << 20));
        byte[] gif = renderer.loopGif(frames(), BOX, 500, false, "ip:1");
        assertThat(new String(gif, 0, 6, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("GIF89a");
        assertThat(renderer.priorities).containsExactly(RenderQueue.Priority.PREFETCH, RenderQueue.Priority.PREFETCH);
    }

    @Test
    @DisplayName("a content-addressed loop is cached; an alias loop is rendered every time")
    void cachedOnlyWhenContentAddressed() {
        RecordingRenderer renderer = new RecordingRenderer(new TileCache(1 << 20));
        byte[] first = renderer.loopGif(frames(), BOX, 500, true, "ip:1");
        byte[] second = renderer.loopGif(frames(), BOX, 500, true, "ip:1");
        assertThat(second).isSameAs(first);
        assertThat(renderer.priorities).hasSize(2);
        renderer.loopGif(frames(), BOX, 500, false, "ip:1");
        renderer.loopGif(frames(), BOX, 500, false, "ip:1");
        assertThat(renderer.priorities).hasSize(6);
    }
}
