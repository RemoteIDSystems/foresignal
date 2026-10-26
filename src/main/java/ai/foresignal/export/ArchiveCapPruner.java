package ai.foresignal.export;

import ai.foresignal.retention.RetentionCapPruner;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.settings.RetentionSettings;
import org.springframework.stereotype.Component;

/**
 * Fills prompt R2-015's size-cap seam (prompt R2-016): when a cold-storage tier is configured, the retention size cap
 * archives the oldest history before it purges it ({@link ArchiveRunner#archiveThenPurge}) instead of deleting it
 * outright. With the archive off it does what {@link RetentionCapPruner#RERUN} does.
 */
@Component
public class ArchiveCapPruner implements RetentionCapPruner {

    private final ArchiveRunner runner;

    /** Creates the pruner. */
    public ArchiveCapPruner(ArchiveRunner runner) {
        this.runner = runner;
    }

    @Override
    public RetentionRun prune(RetentionSettings tightened, Passes steps) {
        if (!runner.configured()) {
            return RERUN.prune(tightened, steps);
        }
        return runner.archiveThenPurge(tightened, steps);
    }
}
