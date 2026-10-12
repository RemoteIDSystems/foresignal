package ai.foresignal.security.audit;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.sql.Timestamp;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes {@code audit_event} rows older than the {@code audit} retention window (default 90 days,
 * prompt 023). Runs after the operational tables (order 750). Nothing consumes the audit log
 * durably, so no consumer clamp applies.
 */
@Component
public class AuditRetentionStep implements RetentionStep {

    @Override
    public String name() {
        return "audit";
    }

    @Override
    public int order() {
        return 750;
    }

    @Override
    public void run(RetentionRun run) {
        Timestamp cutoff = Timestamp.from(run.now().minus(run.settings().audit()));
        run.deleted("audit_event", run.jdbc().update("DELETE FROM audit_event WHERE at < :t",
                new MapSqlParameterSource("t", cutoff)));
    }
}
