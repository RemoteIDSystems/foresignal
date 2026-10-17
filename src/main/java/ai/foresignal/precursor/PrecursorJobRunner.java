package ai.foresignal.precursor;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Runs precursor mining and re-tests as 103 jobs of type {@value #TYPE} (prompt 120): the job's definition carries
 * {@code mode} ({@code mine} or {@code retest}) and {@code trigger}; the result is the finished {@code PrecursorBatch}.
 * The 103 framework supplies the worker pool, progress, cancellation and the terminal state.
 */
@Component
public class PrecursorJobRunner implements JobRunner {

    /** The job type. */
    public static final String TYPE = "precursor-mining";

    private final PrecursorMiner miner;

    /** Creates the runner. */
    public PrecursorJobRunner(PrecursorMiner miner) {
        this.miner = miner;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        Map<String, Object> definition = job.getDefinition() == null ? Map.of() : job.getDefinition();
        String mode = "retest".equals(String.valueOf(definition.get("mode"))) ? "retest" : "mine";
        Object trigger = definition.get("trigger");
        return PrecursorViews.batch(miner.run(mode, trigger == null ? "manual" : trigger.toString(), job.getId(),
                monitor));
    }
}
