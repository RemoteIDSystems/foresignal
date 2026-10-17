package ai.foresignal.forecast.eval;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Runs retrospective evaluations as 103 jobs of type {@value EvaluationService#TYPE} (prompt 122). */
@Component
public class EvaluationJobRunner implements JobRunner {

    private final EvaluationService service;

    /** Creates the runner. */
    public EvaluationJobRunner(EvaluationService service) {
        this.service = service;
    }

    @Override
    public String type() {
        return EvaluationService.TYPE;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        return service.run(job.getDefinition(), monitor);
    }
}
