package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No code path decodes image pixels without passing {@code ImageGuard} first (prompt 043 acceptance):
 * {@link GuardedImages} is the only class in {@code src/main/java} that calls {@code ImageIO.read}.
 */
class GuardedImagesOnlyDecodeEntryTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Path ENTRY = Path.of("src/main/java/ai/foresignal/media/GuardedImages.java");
    private static final Pattern DIRECT_READ = Pattern.compile("ImageIO\\s*\\.\\s*read\\s*\\(");

    @Test
    @DisplayName("ImageIO.read appears only in GuardedImages")
    void onlyGuardedImagesDecodes() throws IOException {
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            offenders = files.filter(f -> f.toString().endsWith(".java"))
                    .filter(f -> !f.normalize().equals(ENTRY.normalize()))
                    .filter(f -> DIRECT_READ.matcher(read(f)).find())
                    .toList();
        }
        assertThat(offenders).isEmpty();
        assertThat(DIRECT_READ.matcher(read(ENTRY)).find()).as("the entry point itself decodes").isTrue();
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
