package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link SatDumpProvenanceRetentionStep} (prompt 054, Weather-Imagery-Design §2.4): provenance copies
 * older than 30 days and remote uploads older than 2 days are removed, newer ones kept, and a link is
 * deleted as a link, never followed.
 */
class SatDumpProvenanceRetentionStepTest {

    static final Instant NOW = Instant.parse("2026-11-20T00:00:00Z");

    @TempDir Path data;

    Path file(Path path, Instant modified) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, "x");
        Files.setLastModifiedTime(path, FileTime.from(modified));
        Files.setLastModifiedTime(path.getParent(), FileTime.from(modified));
        return path;
    }

    @Test
    @DisplayName("30 days for provenance, 2 days for uploads; newer directories stay")
    void expiry() throws Exception {
        RasterPaths paths = new RasterPaths(data);
        Path satdump = paths.root().resolve("satdump");
        Path oldPass = satdump.resolve("home/home_meteor-m2-3_1/MSU-MR/a.tif");
        Path newPass = satdump.resolve("home/home_meteor-m2-3_2/dataset.json");
        Path oldUpload = satdump.resolve("home/incoming/01jold/pass/dataset.json");
        Path newUpload = satdump.resolve("home/incoming/01jnew/pass/dataset.json");
        file(oldPass, NOW.minus(Duration.ofDays(31)));
        Files.setLastModifiedTime(oldPass.getParent().getParent(), FileTime.from(NOW.minus(Duration.ofDays(31))));
        file(newPass, NOW.minus(Duration.ofDays(29)));
        file(oldUpload, NOW.minus(Duration.ofDays(3)));
        Files.setLastModifiedTime(oldUpload.getParent().getParent(), FileTime.from(NOW.minus(Duration.ofDays(3))));
        file(newUpload, NOW.minus(Duration.ofHours(12)));

        SatDumpProvenanceRetentionStep step = new SatDumpProvenanceRetentionStep(paths);
        assertThat(step.name()).isEqualTo("satdump-provenance");
        assertThat(step.order()).isEqualTo(SatDumpProvenanceRetentionStep.ORDER);
        assertThat(step.apply(NOW)).isEqualTo(2);

        assertThat(Files.exists(satdump.resolve("home/home_meteor-m2-3_1"))).isFalse();
        assertThat(Files.exists(newPass)).isTrue();
        assertThat(Files.exists(satdump.resolve("home/incoming/01jold"))).isFalse();
        assertThat(Files.exists(newUpload)).isTrue();
        assertThat(Files.exists(satdump.resolve("home/incoming"))).as("the incoming directory itself stays").isTrue();
        assertThat(step.apply(NOW)).isZero();
    }

    @Test
    @DisplayName("a newer file anywhere below keeps the whole pass directory; a link inside is removed, its target kept")
    void newestAndLinks() throws Exception {
        RasterPaths paths = new RasterPaths(data);
        Path pass = paths.root().resolve("satdump/home/home_x_1");
        file(pass.resolve("old.json"), NOW.minus(Duration.ofDays(40)));
        file(pass.resolve("sub/new.json"), NOW.minus(Duration.ofDays(1)));
        assertThat(SatDumpProvenanceRetentionStep.newest(pass)).isAfter(NOW.minus(Duration.ofDays(2)));
        SatDumpProvenanceRetentionStep step = new SatDumpProvenanceRetentionStep(paths);
        assertThat(step.apply(NOW)).isZero();

        Path outside = file(data.resolve("outside/keep.txt"), NOW.minus(Duration.ofDays(40)));
        Path stale = paths.root().resolve("satdump/home/home_x_2");
        file(stale.resolve("a.json"), NOW.minus(Duration.ofDays(40)));
        Files.createSymbolicLink(stale.resolve("link"), outside);
        Files.setLastModifiedTime(stale, FileTime.from(NOW.minus(Duration.ofDays(40))));
        assertThat(step.apply(NOW)).isEqualTo(1);
        assertThat(Files.exists(stale)).isFalse();
        assertThat(Files.exists(outside)).isTrue();
    }
}
