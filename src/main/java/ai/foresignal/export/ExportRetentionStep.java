package ai.foresignal.export;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retention step 8.5 (prompt 111): expire finished exports and packs whose {@code expiresAt} passed,
 * delete their directories, and sweep orphan directories that have no job row. Snapshots expire only
 * when an {@code expiresAt} was set.
 */
@Component
public class ExportRetentionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(ExportRetentionStep.class);

    private final ExportJobRepository jobs;
    private final ExportStore store;

    /** Creates the step. */
    public ExportRetentionStep(ExportJobRepository jobs, ExportStore store) {
        this.jobs = jobs;
        this.store = store;
    }

    @Override
    public String name() {
        return "exports";
    }

    @Override
    public int order() {
        return 850;
    }

    @Override
    public void run(RetentionRun run) {
        Instant now = run.now();
        sweep(now);
        orphans();
    }

    /** Exposed for the scheduled step and tests. */
    @Transactional
    public void sweep(Instant now) {
        Set<String> known = new HashSet<>();
        for (ExportJobEntity job : jobs.findAll()) {
            known.add(job.getId());
            if (job.getExpiresAt() != null && job.getExpiresAt().isBefore(now)
                    && !ExportJobEntity.EXPIRED.equals(job.getStatus())
                    && !ExportJobEntity.RUNNING.equals(job.getStatus())
                    && !ExportJobEntity.QUEUED.equals(job.getStatus())) {
                deleteQuietly(dir(job));
                job.setStatus(ExportJobEntity.EXPIRED);
                jobs.save(job);
            }
        }
        // Directories under exports/ with no row are orphans (a crash between mkdir and save).
        sweepRoot(store.resolve("exports"), known);
    }

    private void orphans() {
        Set<String> known = new HashSet<>(jobs.findAll().stream().map(ExportJobEntity::getId).toList());
        sweepRoot(store.resolve("exports"), known);
        sweepRoot(store.resolve("snapshots"), known);
    }

    private void sweepRoot(Path root, Set<String> known) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var stream = Files.list(root)) {
            for (Path dir : stream.filter(Files::isDirectory).toList()) {
                if (!known.contains(dir.getFileName().toString())) {
                    deleteQuietly(dir);
                }
            }
        } catch (IOException e) {
            log.warn("Could not sweep the export directory {}", root, e);
        }
    }

    private Path dir(ExportJobEntity job) {
        if (ExportJobEntity.SNAPSHOT.equals(job.getKind())) {
            return store.resolve("snapshots/" + job.getId());
        }
        return store.resolve("exports/" + job.getId());
    }

    private static void deleteQuietly(Path dir) {
        try {
            if (Files.isDirectory(dir)) {
                try (var walk = Files.walk(dir)) {
                    for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(path);
                    }
                }
            }
        } catch (IOException e) {
            log.debug("Could not delete {}", dir, e);
        }
    }
}
