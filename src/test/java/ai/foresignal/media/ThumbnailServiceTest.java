package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.testsupport.media.ImageFixtures;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Thumbnail rendering (prompt 043) without a database: {@link ThumbnailRenderer}'s sizes, the JPEG
 * versus PNG-with-alpha choice, no upscaling, 16-bit grayscale input, and
 * {@link ThumbnailService}'s link-or-render decision. Storage, linking and single-flight are covered
 * by {@code MediaStoreIT} and {@code MediaApiIT}.
 */
class ThumbnailServiceTest {

    final GuardedImages images = new GuardedImages(new ImageGuard(25L * 1024 * 1024, null));

    private BufferedImage decode(byte[] bytes) {
        return images.decode(bytes, null);
    }

    /** Reads a rendition back (test-only; production decodes only through GuardedImages). */
    private BufferedImage readBack(ThumbnailRenderer.Rendition rendition) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(rendition.bytes()));
    }

    @Test
    @DisplayName("256 and 1024 px renditions keep the aspect ratio on the long edge")
    void sizes() throws Exception {
        BufferedImage source = decode(ImageFixtures.png(2000, 1000));
        ThumbnailRenderer.Rendition small = ThumbnailRenderer.render(source, 256);
        assertThat(small.width()).isEqualTo(256);
        assertThat(small.height()).isEqualTo(128);
        BufferedImage back = readBack(small);
        assertThat(back.getWidth()).isEqualTo(256);
        assertThat(back.getHeight()).isEqualTo(128);
        ThumbnailRenderer.Rendition large = ThumbnailRenderer.render(source, 1024);
        assertThat(large.width()).isEqualTo(1024);
        assertThat(large.height()).isEqualTo(512);
        BufferedImage portrait = decode(ImageFixtures.jpeg(300, 1500));
        ThumbnailRenderer.Rendition tall = ThumbnailRenderer.render(portrait, 1024);
        assertThat(tall.height()).isEqualTo(1024);
        assertThat(tall.width()).isEqualTo(205);
        assertThat(ThumbnailRenderer.targetSize(1, 5000, 256)).containsExactly(1, 256);
    }

    @Test
    @DisplayName("opaque sources become JPEG; sources with alpha become PNG and keep transparency")
    void jpegVersusPng() throws Exception {
        ThumbnailRenderer.Rendition opaque = ThumbnailRenderer.render(decode(ImageFixtures.png(600, 400)), 256);
        assertThat(opaque.contentType()).isEqualTo("image/jpeg");
        assertThat(ImageGuard.sniff(opaque.bytes(), 4)).isEqualTo(ImageFormat.JPEG);
        ThumbnailRenderer.Rendition alpha = ThumbnailRenderer.render(decode(ImageFixtures.pngWithAlpha(600, 400)), 256);
        assertThat(alpha.contentType()).isEqualTo("image/png");
        assertThat(ImageGuard.sniff(alpha.bytes(), 4)).isEqualTo(ImageFormat.PNG);
        assertThat(readBack(alpha).getColorModel().hasAlpha()).isTrue();
    }

    @Test
    @DisplayName("an image already smaller than the target is never upscaled")
    void noUpscaling() {
        ThumbnailRenderer.Rendition same = ThumbnailRenderer.render(decode(ImageFixtures.png(100, 60)), 1024);
        assertThat(same.width()).isEqualTo(100);
        assertThat(same.height()).isEqualTo(60);
        assertThat(ThumbnailRenderer.targetSize(256, 100, 256)).containsExactly(256, 100);
        assertThat(ThumbnailService.needsRendering(ImageFormat.PNG, 200, 100, 256)).isFalse();
        assertThat(ThumbnailService.needsRendering(ImageFormat.JPEG, 256, 256, 256)).isFalse();
        assertThat(ThumbnailService.needsRendering(ImageFormat.PNG, 257, 100, 256)).isTrue();
        assertThat(ThumbnailService.needsRendering(ImageFormat.TIFF, 20, 20, 256))
                .as("a small TIFF is re-encoded, since browsers cannot show TIFF").isTrue();
    }

    @Test
    @DisplayName("a 16-bit grayscale PNG renders, and brighter input gives a brighter thumbnail")
    void sixteenBitGray() throws Exception {
        BufferedImage dark = decode(ImageFixtures.png16Gray(800, 400, 8_000));
        BufferedImage bright = decode(ImageFixtures.png16Gray(800, 400, 60_000));
        ThumbnailRenderer.Rendition darkThumb = ThumbnailRenderer.render(dark, 256);
        ThumbnailRenderer.Rendition brightThumb = ThumbnailRenderer.render(bright, 256);
        assertThat(darkThumb.contentType()).isEqualTo("image/jpeg");
        assertThat(darkThumb.width()).isEqualTo(256);
        assertThat(darkThumb.height()).isEqualTo(128);
        int darkLevel = readBack(darkThumb).getRGB(128, 64) & 0xff;
        int brightLevel = readBack(brightThumb).getRGB(128, 64) & 0xff;
        assertThat(brightLevel).isGreaterThan(darkLevel);
    }

    @Test
    @DisplayName("progressive halving ends with one exact-size step")
    void halving() {
        BufferedImage source = new BufferedImage(4096, 1024, BufferedImage.TYPE_INT_RGB);
        BufferedImage scaled = ThumbnailRenderer.scale(source, 256, false);
        assertThat(scaled.getWidth()).isEqualTo(256);
        assertThat(scaled.getHeight()).isEqualTo(64);
        assertThat(scaled.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        BufferedImage alpha = ThumbnailRenderer.scale(new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB), 256,
                true);
        assertThat(alpha.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        assertThat(alpha.getWidth()).isEqualTo(256);
    }

    @Test
    @DisplayName("only the two offered widths are accepted")
    void widths() {
        assertThat(ThumbnailService.WIDTHS).containsExactly(256, 1024);
        ThumbnailService service = new ThumbnailService(null, null, images);
        assertThatThrownBy(() -> service.thumbnail("01JA8Q3V7S2K9W4T6Y1B5N0M3C", 512))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
