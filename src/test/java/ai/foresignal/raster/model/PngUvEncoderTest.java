package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The png-uv field encoding (prompt 106): R = u, G = v, B = 0, with the scale bounds. */
class PngUvEncoderTest {

    @Test
    @DisplayName("encodes R=u and G=v scaled over the bounds")
    void encodes() throws Exception {
        float[] u = {0, 10, 5, -10};
        float[] v = {0, 0, 10, -10};
        byte[] raw = FieldStore.planar(u, v);
        PngUvEncoder.Result result = PngUvEncoder.encode(raw, 2, 2);
        assertThat(result.width()).isEqualTo(2);
        assertThat(result.height()).isEqualTo(2);
        assertThat(result.uMin()).isEqualTo(-10);
        assertThat(result.uMax()).isEqualTo(10);
        assertThat(result.vMin()).isEqualTo(-10);
        assertThat(result.vMax()).isEqualTo(10);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result.png()));
        assertThat(image.getWidth()).isEqualTo(2);
        // u = -10 (pixel 1,1) -> R 0; u = 10 (pixel 1,0) -> R 255.
        assertThat(red(image, 1, 1)).isEqualTo(0);
        assertThat(red(image, 1, 0)).isEqualTo(255);
        // v = -10 (pixel 1,1) -> G 0; v = 10 (pixel 0,1) -> G 255.
        assertThat(green(image, 0, 1)).isEqualTo(255);
        assertThat(green(image, 1, 1)).isEqualTo(0);
        // B is always 0.
        assertThat(image.getRGB(0, 0) & 0xFF).isZero();
    }

    @Test
    @DisplayName("a flat field gets a sane 0..1 range and a middle channel")
    void flatField() {
        float[] u = {3, 3, 3, 3};
        float[] v = {1, 1, 1, 1};
        PngUvEncoder.Result result = PngUvEncoder.encode(FieldStore.planar(u, v), 2, 2);
        // a unit-wide range centered on the flat value, so the value sits in the middle channel
        assertThat(result.uMax() - result.uMin()).isEqualTo(1.0);
        assertThat(result.uMin()).isEqualTo(2.5);
        assertThat(PngUvEncoder.channel(3, result.uMin(), result.uMax())).isEqualTo(128);
        assertThat(result.vMin()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("the channel scales and clamps")
    void channel() {
        assertThat(PngUvEncoder.channel(Double.NaN, 0, 10)).isZero();
        assertThat(PngUvEncoder.channel(-5, 0, 10)).isZero();
        assertThat(PngUvEncoder.channel(15, 0, 10)).isEqualTo(255);
        assertThat(PngUvEncoder.channel(5, 0, 10)).isEqualTo(128);
    }

    private static int red(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) >> 16) & 0xFF;
    }

    private static int green(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) >> 8) & 0xFF;
    }
}
