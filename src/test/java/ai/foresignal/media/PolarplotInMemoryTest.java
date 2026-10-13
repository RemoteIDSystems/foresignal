package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The SatDump polar plot is kept in memory only (Weather-Imagery-Design §4.2, §6.6; checkpoint 057 focus
 * area): {@link LiveImageStore} holds a private copy per source, and nothing on the polar plot's path
 * writes a file or reaches the media store.
 */
class PolarplotInMemoryTest {

    private static final Path MAIN = Path.of("src", "main", "java", "ai", "foresignal");

    /** The classes the polar plot passes through, from fetch to response. */
    private static final List<Path> PATH_CLASSES = List.of(
            MAIN.resolve("media/LiveImageStore.java"),
            MAIN.resolve("source/spi/LiveImageHolder.java"),
            MAIN.resolve("api/GroundStationsController.java"));

    private static byte[] jpeg() throws IOException {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    @Test
    @DisplayName("the store holds a private copy per source, replaces it, and forgets it on clear")
    void heldInMemory() throws IOException {
        LiveImageStore store = new LiveImageStore(new ImageGuard(10_000_000L, null));
        byte[] plot = jpeg();
        byte[] original = plot.clone();
        Instant t = Instant.parse("2026-10-12T18:02:00Z");
        store.hold("satdump-home", plot, t);
        plot[plot.length - 3] ^= 0x5A;
        assertThat(store.latest("satdump-home")).hasValueSatisfying(held -> {
            assertThat(held.bytes()).isEqualTo(original);
            assertThat(held.fetchedAt()).isEqualTo(t);
        });
        assertThat(store.latest("satdump-other")).isEmpty();
        store.clear("satdump-home");
        assertThat(store.latest("satdump-home")).isEmpty();
    }

    @Test
    @DisplayName("nothing on the polar plot's path writes files or uses the media store")
    void noFileOrMediaOnThePath() throws IOException {
        for (Path file : PATH_CLASSES) {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            assertThat(source).as(file.toString()).doesNotContain("java.nio.file").doesNotContain("java.io.File")
                    .doesNotContain("MediaStore").doesNotContain("MediaSink");
        }
        String station = Files.readString(MAIN.resolve("source/adapters/satdump/SatDumpStation.java"),
                StandardCharsets.UTF_8);
        int start = station.indexOf("private void fetchPolarplot(");
        assertThat(start).as("SatDumpStation.fetchPolarplot exists").isPositive();
        String body = station.substring(start, station.indexOf("\n    }\n", start));
        assertThat(body).contains("liveImages.hold(").doesNotContain("media()").doesNotContain("Files.");
    }
}
