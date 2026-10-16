package ai.foresignal.export;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * The on-disk layout of exports, snapshots, packs and the cold archive under
 * {@code ${FORESIGNAL_DATA_DIR}} (prompt 111): {@code exports/{id}/} (exports and packs, beside the
 * 108 raster exports), {@code snapshots/{id}/} and {@code archive/}. A job writes into
 * {@code {dir}/tmp/} and renames its finished files into {@code {dir}/}.
 */
@Component
public class ExportStore {

    private final Path dataDir;

    /** Creates the store under {@code foresignal.data-dir}. */
    public ExportStore(ForesignalProperties properties) {
        this.dataDir = properties.dataDir().toAbsolutePath().normalize();
    }

    /** The data directory. */
    public Path dataDir() {
        return dataDir;
    }

    /** {@code exports/}, created. */
    public Path exportsRoot() {
        return create(dataDir.resolve("exports"));
    }

    /** {@code snapshots/}, created. */
    public Path snapshotsRoot() {
        return create(dataDir.resolve("snapshots"));
    }

    /** {@code archive/}, created. */
    public Path archiveRoot() {
        return create(dataDir.resolve("archive"));
    }

    /** The directory of {@code job} ({@code snapshots/{id}} for a snapshot, else {@code exports/{id}}), not created. */
    public Path dirOf(ExportJobEntity job) {
        return dirOf(job.getKind(), job.getId());
    }

    /** The directory of a job of {@code kind} with {@code id}, not created. */
    public Path dirOf(String kind, String id) {
        Path root = ExportJobEntity.SNAPSHOT.equals(kind) ? dataDir.resolve("snapshots") : dataDir.resolve("exports");
        return root.resolve(id);
    }

    /** {@code {dir}/tmp/} of {@code job}, created. */
    public Path tmpDir(ExportJobEntity job) {
        return create(dirOf(job).resolve("tmp"));
    }

    /** Resolves a path under the data directory, refusing one that escapes it. */
    public Path resolve(String relative) {
        Path resolved = dataDir.resolve(relative).normalize();
        if (!resolved.startsWith(dataDir)) {
            throw new IllegalArgumentException("The path escapes the data directory: " + relative);
        }
        return resolved;
    }

    /** The total size of the regular files under {@code root} (0 when it does not exist). */
    public static long bytesUnder(Path root) {
        if (!Files.isDirectory(root)) {
            return 0L;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        } catch (IOException | UncheckedIOException e) {
            return 0L;
        }
    }

    /** Deletes {@code dir} and everything under it; a missing directory is fine. */
    public static void deleteTree(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete " + dir, e);
        }
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
