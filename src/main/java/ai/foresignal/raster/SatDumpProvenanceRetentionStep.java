package ai.foresignal.raster;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Expires the SatDump copies under {@code ${FORESIGNAL_DATA_DIR}/rasters/satdump/} (prompt 054,
 * Weather-Imagery-Design §2.4: "SatDump polar 30 d"), as part of the hourly retention run.
 *
 * <ul>
 *   <li><b>Provenance</b>, {@code {station}/{passDir}/}: a pass directory whose newest file is older
 *       than {@value #PROVENANCE_DAYS} days is deleted with everything in it.
 *   <li><b>Remote uploads</b>, {@code {station}/incoming/{uploadId}/}: an upload older than
 *       {@value #UPLOAD_DAYS} days has long been processed (the inbox loop takes it on its next scan)
 *       and copied as provenance, so it is deleted.
 * </ul>
 *
 * <p>Nothing under {@code rasters/satdump/} is a frame: frames follow their product's ladder in
 * {@link RasterRetentionStep}. Links are never followed and every deleted path is checked to lie
 * inside {@code rasters/satdump/}.
 */
@Component
public class SatDumpProvenanceRetentionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(SatDumpProvenanceRetentionStep.class);

    /** Days a pass's provenance copy is kept. */
    public static final int PROVENANCE_DAYS = 30;

    /** Days a processed remote upload is kept. */
    public static final int UPLOAD_DAYS = 2;

    /** The directory of remote uploads under a station. */
    public static final String INCOMING = "incoming";

    /** Runs after the frame step. */
    public static final int ORDER = 805;

    private final RasterPaths paths;

    /** Creates the step. */
    public SatDumpProvenanceRetentionStep(RasterPaths paths) {
        this.paths = paths;
    }

    @Override
    public String name() {
        return "satdump-provenance";
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void run(RetentionRun run) {
        int removed = apply(run.now());
        if (removed > 0) {
            log.info("SatDump retention: {} provenance and upload directories removed", removed);
        }
    }

    /**
     * Applies both rules.
     *
     * @return the number of directories removed
     */
    public int apply(Instant now) {
        Path base = paths.root().resolve("satdump").toAbsolutePath().normalize();
        if (!Files.isDirectory(base, LinkOption.NOFOLLOW_LINKS)) {
            return 0;
        }
        int removed = 0;
        for (Path station : children(base)) {
            for (Path dir : children(station)) {
                if (dir.getFileName().toString().equals(INCOMING)) {
                    for (Path upload : children(dir)) {
                        removed += expire(base, upload, now.minus(Duration.ofDays(UPLOAD_DAYS)));
                    }
                } else {
                    removed += expire(base, dir, now.minus(Duration.ofDays(PROVENANCE_DAYS)));
                }
            }
        }
        return removed;
    }

    /** Deletes {@code dir} when its newest file is older than {@code cutoff}; 1 when deleted. */
    private static int expire(Path base, Path dir, Instant cutoff) {
        try {
            Instant newest = newest(dir);
            if (newest.isBefore(cutoff)) {
                delete(base, dir);
                return 1;
            }
        } catch (IOException e) {
            log.warn("SatDump retention: could not expire {}: {}", dir, e.getMessage());
        }
        return 0;
    }

    /**
     * The newest modification time of the regular files below {@code dir}, or the directory's own when it
     * is newer (or empty); links are ignored.
     */
    static Instant newest(Path dir) throws IOException {
        Instant[] newest = {Files.getLastModifiedTime(dir, LinkOption.NOFOLLOW_LINKS).toInstant()};
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isSymbolicLink()) {
                    return FileVisitResult.CONTINUE; // a planted link never keeps a directory alive
                }
                Instant t = attrs.lastModifiedTime().toInstant();
                if (t.isAfter(newest[0])) {
                    newest[0] = t;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return newest[0];
    }

    private static void delete(Path base, Path dir) throws IOException {
        Path target = dir.toAbsolutePath().normalize();
        if (!target.startsWith(base) || target.equals(base)) {
            throw new IOException(dir + " is outside rasters/satdump/");
        }
        Files.walkFileTree(target, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static List<Path> children(Path dir) {
        try (Stream<Path> list = Files.list(dir)) {
            return list.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)).sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }
}
