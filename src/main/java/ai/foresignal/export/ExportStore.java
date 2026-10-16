package ai.foresignal.export;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * The on-disk layout of exports, snapshots and packs under {@code ${FORESIGNAL_DATA_DIR}}
 * (prompt 111): {@code exports/{id}/}, {@code snapshots/{id}/} and the cold {@code archive/}.
 */
@Component
public class ExportStore {

    /** The default export lifetime. */
    public static final java.time.Duration DEFAULT_TTL = java.time.Duration.ofDays(7);

    private final Path dataDir;

    /** Creates the store. */
    public ExportStore(ForesignalProperties properties) {
        this.dataDir = properties.dataDir().toAbsolutePath().normalize();
    }

    /** {@code exports/{id}/}, created. */
    public Path jobDir(String id) {
        return create(dataDir.resolve("exports").resolve(id));
    }

    /** {@code exports/{id}/tmp/}, created. */
    public Path tmpDir(String id) {
        return create(jobDir(id).resolve("tmp"));
    }

    /** {@code snapshots/{id}/}, created. */
    public Path snapshotDir(String id) {
        return create(dataDir.resolve("snapshots").resolve(id));
    }

    /** The cold archive root. */
    public Path archiveRoot() {
        return create(dataDir.resolve("archive"));
    }

    /** The exports root. */
    public Path exportsRoot() {
        return create(dataDir.resolve("exports"));
    }

    /** Resolves a path under the data directory, rejecting an escape. */
    public Path resolve(String relative) {
        Path resolved = dataDir.resolve(relative).normalize();
        if (!resolved.startsWith(dataDir)) {
            throw new IllegalArgumentException("path escapes the data directory: " + relative);
        }
        return resolved;
    }

    private static Path create(Path dir) {
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create " + dir, e);
        }
    }
}
