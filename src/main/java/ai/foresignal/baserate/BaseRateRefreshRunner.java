package ai.foresignal.baserate;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Runs base-rate refreshes as 103 jobs of type {@value #TYPE} (prompt 121): region profiles, then every automatic
 * rate. The result is a {@code BaseRateRefreshResult}.
 */
@Component
public class BaseRateRefreshRunner implements JobRunner {

    /** The job type. */
    public static final String TYPE = "base-rate-refresh";

    private final BaseRateService service;

    /** Creates the runner. */
    public BaseRateRefreshRunner(BaseRateService service) {
        this.service = service;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        return service.refreshAll(monitor);
    }
}
