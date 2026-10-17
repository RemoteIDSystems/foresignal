package ai.foresignal.backtest;

import java.util.Map;

/**
 * A job type run by the 103 job framework but implemented elsewhere (prompt 116: {@code manifest-replay}).
 * {@link BacktestJobService} hands a claimed job of {@link #type()} to {@link #run}; the returned map becomes the
 * job's {@code result}.
 */
public interface JobRunner {

    /** The job type this runner handles. */
    String type();

    /**
     * Runs a claimed job.
     *
     * @param job the job (its {@code definition} holds the runner's inputs)
     * @param monitor cancellation and progress
     * @return the result
     */
    Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor);
}
