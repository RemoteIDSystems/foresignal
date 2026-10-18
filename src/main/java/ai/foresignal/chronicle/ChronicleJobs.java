package ai.foresignal.chronicle;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Runs the long chronicle jobs (prompt 131): backfills, archives and restores. Each runs on its own virtual thread,
 * so the request that starts it returns at once ({@code 202} for archive and restore); with
 * {@code foresignal.chronicles.async-jobs=false} (tests) it runs on the caller's thread and is finished when the request
 * returns.
 */
@Component
public class ChronicleJobs {

    private static final Logger log = LoggerFactory.getLogger(ChronicleJobs.class);

    private final ChronicleBackfill backfill;
    private final ChronicleArchiver archiver;
    private final ChronicleProperties properties;

    /** Creates the component. */
    public ChronicleJobs(ChronicleBackfill backfill, ChronicleArchiver archiver, ChronicleProperties properties) {
        this.backfill = backfill;
        this.archiver = archiver;
        this.properties = properties;
    }

    /** Backfills a chronicle from {@code from}. */
    public void backfill(String chronicleId, Instant from) {
        run("backfill-" + chronicleId, () -> backfill.run(chronicleId, from));
    }

    /** Archives a chronicle. */
    public void archive(String chronicleId) {
        run("archive-" + chronicleId, () -> archiver.archive(chronicleId));
    }

    /** Restores a chronicle. */
    public void restore(String chronicleId) {
        run("restore-" + chronicleId, () -> archiver.restore(chronicleId));
    }

    /** Deletes a chronicle's cold package (synchronously). */
    public void deletePackage(String chronicleId) {
        try {
            archiver.deletePackage(chronicleId);
        } catch (RuntimeException e) {
            log.warn("Could not delete the cold package of chronicle {}: {}", chronicleId, e.toString());
        }
    }

    private void run(String name, Runnable job) {
        Runnable guarded = () -> {
            try {
                job.run();
            } catch (RuntimeException e) {
                log.warn("Chronicle job {} failed: {}", name, e.toString());
            }
        };
        if (properties.asyncJobs()) {
            Thread.ofVirtual().name("chronicle-" + name).start(guarded);
        } else {
            guarded.run();
        }
    }
}
