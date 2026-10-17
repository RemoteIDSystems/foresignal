package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Data tiles: value round trip, no-data alpha and the {@code custom}/{@code terrarium} metadata (prompt 108). */
class DataTileEncoderTest {

    private static BufferedImage decode(byte[] png) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(png));
    }

    @Test
    @DisplayName("custom: the raw code sits in R (high byte) and G (low byte), no data is alpha 0")
    void custom() throws Exception {
        int[] codes = {0, 1, 256, 65535, DataTileEncoder.NO_DATA};
        DataTileEncoder.Result result = DataTileEncoder.encode(codes, 5, 1, 1.0, 0.0, false,
                DataTileEncoder.CUSTOM);
        BufferedImage image = decode(result.png());
        assertThat(image.getRGB(0, 0)).isEqualTo(0xFF000000);
        assertThat(image.getRGB(1, 0)).isEqualTo(0xFF000100);
        assertThat(image.getRGB(2, 0)).isEqualTo(0xFF010000);
        assertThat(image.getRGB(3, 0)).isEqualTo(0xFFFFFF00);
        assertThat(image.getRGB(4, 0) >>> 24).isZero();
        assertThat(result.encoding()).containsEntry("encoding", "custom");
        assertThat(result.encoding()).containsEntry("redFactor", 256.0);
        assertThat(result.encoding()).containsEntry("greenFactor", 1.0);
        assertThat(result.encoding()).containsEntry("blueFactor", 0.0);
        assertThat(result.encoding()).containsEntry("baseShift", 0.0);
    }

    @Test
    @DisplayName("custom signed: an i16 code is shifted into the unsigned field, baseShift carries −32768·scale")
    void signed() throws Exception {
        int[] codes = {-1, -32768, 32767};
        DataTileEncoder.Result result = DataTileEncoder.encode(codes, 3, 1, 1.0, 0.0, true, DataTileEncoder.CUSTOM);
        BufferedImage image = decode(result.png());
        assertThat(image.getRGB(0, 0)).isEqualTo(0xFF7FFF00);
        assertThat(image.getRGB(1, 0)).isEqualTo(0xFF000000);
        assertThat(image.getRGB(2, 0)).isEqualTo(0xFFFFFF00);
        assertThat(result.encoding()).containsEntry("baseShift", -32768.0);
    }

    @Test
    @DisplayName("terrarium: v = (R·256 + G + B/256) − 32768 round trips within 1/256")
    void terrarium() throws Exception {
        int[] codes = {0, 10000, -20000};
        DataTileEncoder.Result result = DataTileEncoder.encode(codes, 3, 1, 1.0, 0.0, false,
                DataTileEncoder.TERRARIUM);
        BufferedImage image = decode(result.png());
        for (int k = 0; k < codes.length; k++) {
            int argb = image.getRGB(k, 0);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            double decoded = (r * 256.0 + g + b / 256.0) - 32768.0;
            assertThat(decoded).as("code %d", codes[k]).isCloseTo(codes[k],
                    org.assertj.core.data.Offset.offset(1.0 / 256 + 1e-9));
        }
        assertThat(result.encoding()).containsEntry("encoding", "terrarium");
    }

    @Test
    @DisplayName("terrarium: a fraction that rounds up to a whole step carries into green instead of wrapping blue")
    void terrariumCarry() {
        // 100.999 + 32768 = 32868.999: 0.999 * 256 = 255.74 rounds to 256, so the value is 32869 exactly
        int rgb = DataTileEncoder.terrarium(100.999 + 32768.0);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        assertThat(b).isZero();
        assertThat(r * 256.0 + g + b / 256.0 - 32768.0).isEqualTo(101.0);
        // and a carry out of green moves into red
        int carried = DataTileEncoder.terrarium(255.999);
        assertThat((carried >> 16) & 0xFF).isEqualTo(1);
        assertThat((carried >> 8) & 0xFF).isZero();
        assertThat(DataTileEncoder.terrarium(70000.0)).isEqualTo(0xFFFFFF);
    }
}
