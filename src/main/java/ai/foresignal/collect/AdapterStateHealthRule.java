package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AdapterStateStore;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fails a source whose adapter recorded a fatal problem with {@link AdapterStateStore#fail} (prompt
 * 049), and degrades one whose adapter recorded a non-fatal problem (prompt 047) with
 * {@link AdapterStateStore#degrade}, for example {@code spc-outlooks} when one outlook layer answers
 * 404 while the others succeed: the fetch counts as a success, nothing is tombstoned, and health
 * says {@code degraded} with the adapter's reason until the adapter clears it.
 *
 * <p>The reason lives in {@code source_health.adapter_state} under
 * {@value AdapterStateStore#DEGRADED_REASON_KEY}, so it survives a restart.
 */
@Component
public class AdapterStateHealthRule implements HealthRule {

    private static final Logger log = LoggerFactory.getLogger(AdapterStateHealthRule.class);

    static final String SQL = "select adapter_state ->> ? from source_health where source_id = ?";

    private final JdbcTemplate jdbc;

    /** Creates the rule. */
    public AdapterStateHealthRule(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        String failing = reason(context.sourceId(), AdapterStateStore.FAILING_REASON_KEY);
        if (failing != null && !failing.isBlank()) {
            return Optional.of(new HealthVerdict(HealthStatus.FAILING, failing, null));
        }
        String reason = reason(context.sourceId());
        return reason == null || reason.isBlank() ? Optional.empty() : Optional.of(HealthVerdict.degraded(reason, null));
    }

    /** The recorded degraded reason for {@code sourceId}, or null. */
    String reason(String sourceId) {
        return reason(sourceId, AdapterStateStore.DEGRADED_REASON_KEY);
    }

    /** The reason recorded under {@code key} for {@code sourceId}, or null (prompt 049 adds the failing key). */
    String reason(String sourceId, String key) {
        try {
            return jdbc.query(SQL, rs -> rs.next() ? rs.getString(1) : null, key, sourceId);
        } catch (RuntimeException e) {
            log.debug("Could not read the adapter state of {}", sourceId, e);
            return null;
        }
    }
}
