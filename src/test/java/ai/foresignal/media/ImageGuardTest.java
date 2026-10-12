package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.testsupport.media.ImageFixtures;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link ImageGuard} and {@link GuardedImages} (prompt 043): valid PNG, JPEG and TIFF; crafted
 * headers declaring a side over 12,000 px or more than 150 MP refused from the header alone, fast and
 * without an {@link OutOfMemoryError}; BigTIFF with its exact reason; magic and content-type
 * mismatches; truncated headers; oversized files; multi-image TIFFs; the rejection counter.
 */
class ImageGuardTest {

    static final long CAP = 25L * 1024 * 1024;

    final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    final ImageGuard guard = new ImageGuard(CAP, meters);

    @TempDir Path dir;

    private Path write(String name, byte[] bytes) throws Exception {
        Path file = dir.resolve(name);
        Files.write(file, bytes);
        return file;
    }

    private static String tag(Runnable action) {
        try {
            action.run();
        } catch (ImageRejectedException e) {
            return e.tag();
        }
        throw new AssertionError("the image was not rejected");
    }

    /** Runs {@code action} with a two-second ceiling; any Error (an OOM included) fails the test. */
    private static String fastTag(Runnable action) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), () -> tag(action));
    }

    @Test
    @DisplayName("valid PNG, JPEG and TIFF files pass with their header facts")
    void validImages() throws Exception {
        ImageInfo png = guard.inspect(write("a.png", ImageFixtures.png(40, 30)), "image/png");
        assertThat(png).isEqualTo(new ImageInfo(ImageFormat.PNG, 40, 30, 8, false));
        ImageInfo alpha = guard.inspect(ImageFixtures.pngWithAlpha(20, 10), "image/png", false);
        assertThat(alpha.hasAlpha()).isTrue();
        ImageInfo jpeg = guard.inspect(write("a.jpg", ImageFixtures.jpeg(64, 48)), "image/jpeg");
        assertThat(jpeg.format()).isEqualTo(ImageFormat.JPEG);
        assertThat(jpeg.width()).isEqualTo(64);
        assertThat(jpeg.height()).isEqualTo(48);
        ImageInfo tiff = guard.inspect(write("a.tif", ImageFixtures.tiff(32, 16)), "image/tiff");
        assertThat(tiff.format()).isEqualTo(ImageFormat.TIFF);
        assertThat(tiff.width()).isEqualTo(32);
        assertThat(tiff.height()).isEqualTo(16);
        ImageInfo gray16 = guard.inspect(ImageFixtures.png16Gray(8, 8, 40_000), "image/png", false);
        assertThat(gray16.bitDepth()).isEqualTo(16);
        assertThat(guard.inspect(new ByteArrayInputStream(ImageFixtures.png(5, 5)), "IMAGE/PNG; x=y").width())
                .as("type parameters and case are ignored").isEqualTo(5);
        assertThat(guard.inspect(ImageFixtures.png(5, 5), null, false).format())
                .as("no declared type accepts what is sniffed").isEqualTo(ImageFormat.PNG);
    }

    @Test
    @DisplayName("a 12,001 px side is refused from the header alone, fast, for PNG, TIFF and JPEG")
    void sideTooLong() throws Exception {
        Path png = write("wide.png", ImageFixtures.craftedPngHeader(12_001, 10));
        assertThat(Files.size(png)).as("no pixel data behind the header").isLessThan(100);
        assertThat(fastTag(() -> guard.inspect(png, "image/png"))).isEqualTo(ImageRejectedException.SIDE_TOO_LONG);
        Path tiff = write("tall.tif", ImageFixtures.craftedTiffHeader(10, 12_001, 1));
        assertThat(fastTag(() -> guard.inspect(tiff, "image/tiff"))).isEqualTo(ImageRejectedException.SIDE_TOO_LONG);
        Path jpeg = write("wide.jpg", ImageFixtures.craftedJpegHeader(12_001, 16));
        assertThat(fastTag(() -> guard.inspect(jpeg, "image/jpeg"))).isEqualTo(ImageRejectedException.SIDE_TOO_LONG);
    }

    @Test
    @DisplayName("151 megapixels are refused from the header alone, fast, without decoding")
    void tooManyPixels() throws Exception {
        Path png = write("huge.png", ImageFixtures.craftedPngHeader(15_100, 10_000));
        assertThat(fastTag(() -> guard.inspect(png, "image/png"))).isEqualTo(ImageRejectedException.TOO_MANY_PIXELS);
        Path tiff = write("huge.tif", ImageFixtures.craftedTiffHeader(15_100, 10_000, 1));
        assertThat(fastTag(() -> guard.inspect(tiff, "image/tiff")))
                .isEqualTo(ImageRejectedException.TOO_MANY_PIXELS);
        GuardedImages images = new GuardedImages(guard);
        assertThat(fastTag(() -> images.decode(png))).as("GuardedImages never reaches ImageIO.read")
                .isEqualTo(ImageRejectedException.TOO_MANY_PIXELS);
    }

    @Test
    @DisplayName("exactly 12,000 x 12,000 passes the header check (the limits are inclusive)")
    void limitsAreInclusive() throws Exception {
        ImageInfo info = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> guard.inspect(ImageFixtures.craftedPngHeader(12_000, 12_000), "image/png", false));
        assertThat(info.width()).isEqualTo(12_000);
        assertThat(info.height()).isEqualTo(12_000);
    }

    @Test
    @DisplayName("BigTIFF is refused with the exact reason, little- and big-endian")
    void bigTiff() throws Exception {
        for (boolean little : new boolean[] {true, false}) {
            Path file = write("big" + little + ".tif", ImageFixtures.bigTiffHeader(little));
            assertThatThrownBy(() -> guard.inspect(file, "image/tiff"))
                    .isInstanceOfSatisfying(ImageRejectedException.class, e -> {
                        assertThat(e.reason()).isEqualTo("BigTIFF not supported");
                        assertThat(e.tag()).isEqualTo(ImageRejectedException.BIGTIFF);
                    });
        }
        assertThat(meters.counter(ImageGuard.REJECTED_METRIC, "reason", ImageRejectedException.BIGTIFF).count())
                .isEqualTo(2.0);
    }

    @Test
    @DisplayName("the declared content type must agree with the magic bytes; unknown formats are refused")
    void mismatch() {
        assertThat(tag(() -> guard.inspect(ImageFixtures.png(4, 4), "image/jpeg", false)))
                .isEqualTo(ImageRejectedException.TYPE_MISMATCH);
        assertThat(tag(() -> guard.inspect(ImageFixtures.jpeg(4, 4), "image/png", false)))
                .isEqualTo(ImageRejectedException.TYPE_MISMATCH);
        assertThat(tag(() -> guard.inspect(ImageFixtures.tiff(4, 4), "image/gif", false)))
                .isEqualTo(ImageRejectedException.TYPE_MISMATCH);
        assertThat(tag(() -> guard.inspect(ImageFixtures.gif(), "image/gif", false)))
                .isEqualTo(ImageRejectedException.UNKNOWN_FORMAT);
        assertThat(meters.counter(ImageGuard.REJECTED_METRIC, "reason", ImageRejectedException.TYPE_MISMATCH).count())
                .isEqualTo(3.0);
    }

    @Test
    @DisplayName("truncated headers are refused")
    void truncated() throws Exception {
        byte[] png = ImageFixtures.png(10, 10);
        assertThat(tag(() -> guard.inspect(ImageFixtures.truncated(png, 2), null, false)))
                .isEqualTo(ImageRejectedException.TRUNCATED);
        assertThat(tag(() -> guard.inspect(ImageFixtures.truncated(png, 14), "image/png", false)))
                .as("signature and part of IHDR").isEqualTo(ImageRejectedException.TRUNCATED);
        Path tiff = write("cut.tif", ImageFixtures.truncated(ImageFixtures.craftedTiffHeader(10, 10, 1), 12));
        assertThat(tag(() -> guard.inspect(tiff, "image/tiff"))).isEqualTo(ImageRejectedException.TRUNCATED);
        assertThat(tag(() -> guard.inspect(ImageFixtures.truncated(ImageFixtures.jpeg(8, 8), 4), "image/jpeg", false)))
                .isEqualTo(ImageRejectedException.TRUNCATED);
    }

    @Test
    @DisplayName("files over max-file-bytes are refused before anything is read, for paths, streams and bytes")
    void oversizedFile() throws Exception {
        ImageGuard small = new ImageGuard(1_000, meters);
        byte[] noisy = ImageFixtures.noisyPng(64, 64);
        assertThat(noisy.length).isGreaterThan(1_000);
        Path file = write("noisy.png", noisy);
        assertThat(tag(() -> small.inspect(file, "image/png"))).isEqualTo(ImageRejectedException.FILE_TOO_LARGE);
        assertThat(tag(() -> small.inspect(new ByteArrayInputStream(noisy), "image/png")))
                .isEqualTo(ImageRejectedException.FILE_TOO_LARGE);
        assertThat(tag(() -> small.inspect(noisy, "image/png", false))).isEqualTo(ImageRejectedException.FILE_TOO_LARGE);
        assertThat(small.maxFileBytes()).isEqualTo(1_000);
    }

    @Test
    @DisplayName("a TIFF with a second image directory is refused unless the caller opts in")
    void multiImageTiff() throws Exception {
        Path file = write("two.tif", ImageFixtures.craftedTiffHeader(20, 20, 2));
        assertThat(tag(() -> guard.inspect(file, "image/tiff"))).isEqualTo(ImageRejectedException.MULTI_IMAGE);
        assertThat(guard.inspect(file, "image/tiff", true).width()).isEqualTo(20);
        assertThat(guard.inspect(write("one.tif", ImageFixtures.craftedTiffHeader(20, 20, 1)), "image/tiff").height())
                .isEqualTo(20);
    }

    @Test
    @DisplayName("GuardedImages decodes an accepted image, 16-bit grayscale included")
    void guardedDecode() throws Exception {
        GuardedImages images = new GuardedImages(guard);
        BufferedImage png = images.decode(write("ok.png", ImageFixtures.png(12, 9)));
        assertThat(png.getWidth()).isEqualTo(12);
        BufferedImage gray = images.decode(ImageFixtures.png16Gray(6, 4, 1234), "image/png");
        assertThat(gray.getType()).isEqualTo(BufferedImage.TYPE_USHORT_GRAY);
        assertThat(gray.getRaster().getSample(2, 2, 0)).isEqualTo(1234);
        assertThat(tag(() -> images.decode(ImageFixtures.png(4, 4), "image/tiff")))
                .isEqualTo(ImageRejectedException.TYPE_MISMATCH);
        assertThat(images.guard()).isSameAs(guard);
    }

    @Test
    @DisplayName("sniffing reads only the magic bytes")
    void sniff() {
        assertThat(ImageGuard.sniff(new byte[] {(byte) 0x89, 'P', 'N', 'G'}, 4)).isEqualTo(ImageFormat.PNG);
        assertThat(ImageGuard.sniff(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}, 4)).isEqualTo(ImageFormat.JPEG);
        assertThat(ImageGuard.sniff(new byte[] {'I', 'I', 42, 0}, 4)).isEqualTo(ImageFormat.TIFF);
        assertThat(ImageGuard.sniff(new byte[] {'M', 'M', 0, 42}, 4)).isEqualTo(ImageFormat.TIFF);
        assertThat(ImageFormat.ofContentType("image/tif")).isEmpty();
        assertThat(ImageFormat.TIFF.browserSafe()).isFalse();
    }
}
