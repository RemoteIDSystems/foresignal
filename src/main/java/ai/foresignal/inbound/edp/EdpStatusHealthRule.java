package ai.foresignal.inbound.edp;

import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.HealthStatus;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Turns EDP {@code status} items into source health (prompt 024): {@code degraded} and
 * {@code layout-changed} make the provider degraded, {@code blocked} and {@code geo-restricted}
 * make it failing, and a held snapshot deletion makes it degraded. {@code ok} clears a stream's
 * report. Applies only to external sources.
 */
@Component
public class EdpStatusHealthRule implements HealthRule {

    private final EdpStatusStore store;

    /** Creates the rule. */
    public EdpStatusHealthRule(EdpStatusStore store) {
        this.store = store;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!context.sourceId().startsWith(EdpProtocol.SOURCE_PREFIX)) {
            return Optional.empty();
        }
        Optional<Map.Entry<String, EdpStats.StatusReport>> worst = store.worst(context.sourceId());
        if (worst.isEmpty()) {
            return Optional.of(HealthVerdict.healthy(null));
        }
        EdpStats.StatusReport report = worst.get().getValue();
        String where = EdpStats.SOURCE_KEY.equals(worst.get().getKey())
                ? "the provider"
                : EdpStats.SNAPSHOT_KEY.equals(worst.get().getKey()) ? "a snapshot" : "stream " + worst.get().getKey();
        String reason = where + " reports " + report.health()
                + (report.message() == null || report.message().isBlank() ? "" : ": " + report.message());
        HealthStatus status = EdpStatusStore.severity(report.health()) >= 2 ? HealthStatus.FAILING : HealthStatus.DEGRADED;
        return Optional.of(new HealthVerdict(status, reason, null));
    }
}
