package ai.foresignal.benchmark;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Runs benchmark runs as 103 jobs of type {@value BenchmarkService#TYPE} (prompt 123). */
@Component
public class BenchmarkJobRunner implements JobRunner {

    private final BenchmarkService service;

    /** Creates the runner. */
    public BenchmarkJobRunner(BenchmarkService service) {
        this.service = service;
    }

    @Override
    public String type() {
        return BenchmarkService.TYPE;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        return service.run(job, monitor);
    }
}
