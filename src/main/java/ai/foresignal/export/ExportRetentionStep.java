package ai.foresignal.export;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Retention of export jobs (prompt 111): a finished export, pack or snapshot whose {@code expiresAt}
 * has passed loses its directory and becomes {@code EXPIRED} (a snapshot without {@code expiresAt}
 * never does), and directories under {@code exports/} and {@code snapshots/} that no job owns are
 * swept. A directory that belongs to a 108 raster export ({@code raster_export_job}) is not an
 * orphan, and a directory younger than {@value #ORPHAN_MIN_AGE_HOURS} hour is left alone, so a job
 * being created is never swept.
 */
@Component
public class ExportRetentionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(ExportRetentionStep.class);

    /** The youngest directory the orphan sweep removes, in hours. */
    static final int ORPHAN_MIN_AGE_HOURS = 1;

    private final BulkExportJobRepository jobs;
    private final ai.foresignal.raster.export.ExportJobRepository rasterJobs;
    private final ExportStore store;

    /** Creates the step. */
    public ExportRetentionStep(BulkExportJobRepository jobs, ai.foresignal.raster.export.ExportJobRepository rasterJobs,
            ExportStore store) {
        this.jobs = jobs;
        this.rasterJobs = rasterJobs;
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
        int expired = expire(run.now());
        int swept = sweep(store.exportsRoot(), run.now()) + sweep(store.snapshotsRoot(), run.now());
        if (expired + swept > 0) {
            log.info("Export retention expired {} jobs and swept {} orphan directories", expired, swept);
        }
        run.deleted("export_job.expired", expired);
    }

    /** Expires every finished job whose {@code expiresAt} is at or before {@code now}; returns the count. */
    int expire(Instant now) {
        int count = 0;
        for (ExportJobEntity job : jobs.findByStatusIn(List.of(ExportJobEntity.DONE, ExportJobEntity.FAILED,
                ExportJobEntity.CANCELLED))) {
            if (job.getExpiresAt() == null || job.getExpiresAt().isAfter(now)) {
                continue;
            }
            ExportJobService.deleteQuietly(store.dirOf(job));
            job.setStatus(ExportJobEntity.EXPIRED);
            jobs.save(job);
            count++;
        }
        return count;
    }

    /** Deletes the directories under {@code root} that no job owns and that are older than an hour. */
    int sweep(Path root, Instant now) {
        int count = 0;
        Instant cutoff = now.minus(Duration.ofHours(ORPHAN_MIN_AGE_HOURS));
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                String id = dir.getFileName().toString();
                if (jobs.existsById(id) || rasterJobs.existsById(id)) {
                    continue;
                }
                if (Files.getLastModifiedTime(dir).toInstant().isAfter(cutoff)) {
                    continue;
                }
                ExportJobService.deleteQuietly(dir);
                count++;
            }
        } catch (IOException e) {
            log.warn("Could not sweep the export directory {}", root, e);
        }
        return count;
    }
}
