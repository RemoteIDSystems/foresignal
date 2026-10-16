package ai.foresignal.raster.export;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * The export directory (prompt 108; Weather-Imagery-Design §11):
 * {@code ${FORESIGNAL_DATA_DIR}/exports/}. Files live 48 hours and count against the raster disk
 * quota.
 */
@Component
public class ExportPaths {

    /** How long an export file lives. */
    public static final java.time.Duration TTL = java.time.Duration.ofHours(48);

    private final Path root;

    /** Creates the layout under {@code foresignal.data-dir}. */
    public ExportPaths(ForesignalProperties properties) {
        this.root = properties.dataDir().toAbsolutePath().normalize().resolve("exports");
    }

    /** The exports root, created. */
    public Path root() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the export directory " + root, e);
        }
        return root;
    }

    /** {@code exports/{jobId}/{name}}, created. */
    public Path file(String jobId, String name) {
        Path dir = root().resolve(jobId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the export job directory " + dir, e);
        }
        return dir.resolve(name);
    }

    /** The exports directory for a job. */
    public Path jobDir(String jobId) {
        return root().resolve(jobId);
    }
}
