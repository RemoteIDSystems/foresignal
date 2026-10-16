package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/** The animated GIF loop: frame count, size and delay (prompt 108). */
class GifEncoderTest {

    private static BufferedImage frame(int color) {
        BufferedImage image = new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 32; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    @Test
    @DisplayName("two frames become a two-image GIF89a with the requested delay")
    void frames() throws Exception {
        byte[] gif = GifEncoder.encode(List.of(frame(0x112233), frame(0x445566)), 700);
        assertThat(new String(gif, 0, 6, StandardCharsets.US_ASCII)).isEqualTo("GIF89a");
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(gif))) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            ImageReader reader = readers.next();
            reader.setInput(stream);
            assertThat(reader.getNumImages(true)).isEqualTo(2);
            assertThat(reader.read(0).getWidth()).isEqualTo(32);
            assertThat(reader.read(1).getHeight()).isEqualTo(24);
            assertThat(delay(reader, 0)).isEqualTo(70); // hundredths of a second
            reader.dispose();
        }
    }

    private static int delay(ImageReader reader, int index) throws Exception {
        IIOMetadata metadata = reader.getImageMetadata(index);
        Node tree = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node gce = find(tree, "GraphicControlExtension");
        return Integer.parseInt(((org.w3c.dom.Element) gce).getAttribute("delayTime"));
    }

    private static Node find(Node node, String name) {
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeName().equalsIgnoreCase(name)) {
                return n;
            }
            Node deeper = find(n, name);
            if (deeper != null) {
                return deeper;
            }
        }
        return null;
    }
}
