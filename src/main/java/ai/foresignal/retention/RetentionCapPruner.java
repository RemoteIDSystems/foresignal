package ai.foresignal.retention;

import ai.foresignal.settings.RetentionSettings;
import java.util.function.Function;

/**
 * How the database-size cap removes the oldest history once it has chosen a shorter window (prompt R2-015). The
 * default ({@link #RERUN}) runs the retention steps again under the tightened settings, so whatever those steps do
 * before deleting (the prompt 111 hold copy and cold archive) happens here too. Prompt R2-016 (cold storage) may
 * provide a bean of this type that archives first and then purges; {@link RetentionJob} uses that bean when one
 * exists.
 */
@FunctionalInterface
public interface RetentionCapPruner {

    /** Runs the retention steps under the tightened settings. */
    RetentionCapPruner RERUN = (tightened, steps) -> steps.apply(tightened);

    /**
     * Prunes the data history down to {@code tightened}.
     *
     * @param tightened the run's settings with {@code maxDays} lowered to the chosen window
     * @param steps runs every retention step under the given settings and returns that run
     * @return the run that pruned
     */
    RetentionRun prune(RetentionSettings tightened, Function<RetentionSettings, RetentionRun> steps);
}
