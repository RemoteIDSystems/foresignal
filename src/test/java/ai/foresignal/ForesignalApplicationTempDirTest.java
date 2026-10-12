package ai.foresignal;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The temp directories are created before anything uses them (prompt 046, {@code /data/tmp}). */
class ForesignalApplicationTempDirTest {

    @TempDir Path dir;

    @Test
    @DisplayName("jna.tmpdir is created when missing; java.io.tmpdir is left as it is")
    void createsTheTempDirectories() {
        Path jna = dir.resolve("data").resolve("tmp");
        String previous = System.getProperty("jna.tmpdir");
        System.setProperty("jna.tmpdir", jna.toString());
        try {
            ForesignalApplication.createTempDirectory();
            assertThat(Files.isDirectory(jna)).isTrue();
            assertThat(Files.isDirectory(Path.of(System.getProperty("java.io.tmpdir")))).isTrue();
        } finally {
            if (previous == null) {
                System.clearProperty("jna.tmpdir");
            } else {
                System.setProperty("jna.tmpdir", previous);
            }
        }
    }
}
