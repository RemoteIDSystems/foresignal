package ai.foresignal.baserate.reference;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Runs reference-history imports as 103 jobs of type {@value #TYPE} (prompt 121): the job's definition carries the
 * {@code importId}; the result is the finished {@code ReferenceImport}. The 103 framework supplies the worker pool,
 * progress, cancellation and the terminal state.
 */
@Component
public class ReferenceImportJobRunner implements JobRunner {

    /** The job type. */
    public static final String TYPE = "reference-import";

    private final ReferenceImportService service;

    /** Creates the runner. */
    public ReferenceImportJobRunner(ReferenceImportService service) {
        this.service = service;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        Map<String, Object> definition = job.getDefinition() == null ? Map.of() : job.getDefinition();
        Object id = definition.get("importId");
        return service.run(id == null ? job.getTargetId() : id.toString(), monitor);
    }
}
