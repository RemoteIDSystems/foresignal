package ai.foresignal.quality;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import org.springframework.stereotype.Component;

/** Keeps {@code rule_quality_daily} for {@value RuleQualityDailyStore#RETENTION_DAYS} days (prompt 115). */
@Component
public class RuleQualityRetentionStep implements RetentionStep {

    private final RuleQualityDailyStore store;

    /** Creates the step. */
    public RuleQualityRetentionStep(RuleQualityDailyStore store) {
        this.store = store;
    }

    @Override
    public String name() {
        return "rule-quality-daily";
    }

    @Override
    public int order() {
        return 706;
    }

    @Override
    public void run(RetentionRun run) {
        run.deleted("rule_quality_daily", store.purge(run.now()));
    }
}
