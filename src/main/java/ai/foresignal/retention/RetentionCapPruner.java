package ai.foresignal.retention;

import ai.foresignal.settings.RetentionSettings;

/**
 * How the database-size cap removes the oldest history once it has chosen a shorter window (prompt R2-015). The
 * default ({@link #RERUN}) runs the retention steps again under the tightened settings, so whatever those steps do
 * before deleting (the prompt 111 hold copy and cold archive) happens here too. Prompt R2-016 (cold storage) provides
 * a bean of this type that archives first and then purges; {@link RetentionJob} uses that bean when one exists.
 */
@FunctionalInterface
public interface RetentionCapPruner {

    /** Runs the retention steps under the tightened settings. */
    RetentionCapPruner RERUN = (tightened, steps) -> steps.apply(tightened);

    /**
     * Prunes the data history down to {@code tightened}.
     *
     * @param tightened the run's settings with {@code maxDays} lowered to the chosen window
     * @param steps plans and runs retention passes
     * @return the run that pruned, or null when nothing was pruned
     */
    RetentionRun prune(RetentionSettings tightened, Passes steps);

    /**
     * Retention passes, in two halves so that a pruner can act on a pass's plan before the pass deletes anything
     * (prompt R2-016): {@link RetentionPlanner} memoizes a plan on its run, so a run planned with {@link #plan} and then
     * given to {@link #execute} deletes exactly what its plan showed.
     */
    interface Passes {

        /**
         * A pass not yet run, at the current time under {@code settings}.
         *
         * @param settings the settings of the pass
         * @return the run, whose plans are computed on first use and kept
         */
        RetentionRun plan(RetentionSettings settings);

        /**
         * Runs every retention step on {@code run}.
         *
         * @param run a run from {@link #plan}
         * @return the same run
         */
        RetentionRun execute(RetentionRun run);

        /**
         * Plans and runs a pass.
         *
         * @param settings the settings of the pass
         * @return the run
         */
        default RetentionRun apply(RetentionSettings settings) {
            return execute(plan(settings));
        }
    }
}
