package ai.foresignal.backtest;

/**
 * What a running backtest reports to its job and asks of it between chunks (prompt 103 §1): the
 * progress so far, and whether the job was canceled. A runner calls {@link #checkpoint} between
 * chunks; a canceled job throws {@link Canceled} there, which ends the run without overwriting
 * the {@code cancelled} status.
 */
public interface JobMonitor {

    /** A monitor that never cancels and discards progress (direct calls and unit tests). */
    JobMonitor NONE = new JobMonitor() {
        @Override
        public boolean canceled() {
            return false;
        }

        @Override
        public void progress(double fraction) {
            // discarded
        }
    };

    /** True when the job was canceled. */
    boolean canceled();

    /** Records the progress, 0–1. */
    void progress(double fraction);

    /**
     * Records the progress and stops the run when the job was canceled.
     *
     * @throws Canceled when the job was canceled
     */
    default void checkpoint(double fraction) {
        if (canceled()) {
            throw new Canceled();
        }
        progress(Math.max(0, Math.min(1, fraction)));
    }

    /** Thrown at a checkpoint of a canceled job. */
    final class Canceled extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public Canceled() {
            super("The job was canceled.", null, false, false);
        }
    }
}
