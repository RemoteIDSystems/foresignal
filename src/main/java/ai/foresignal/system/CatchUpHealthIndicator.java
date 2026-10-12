package ai.foresignal.system;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health contributor {@code catchUp}, the only member of the {@code catchup} group (prompt 019):
 * OUT_OF_SERVICE while {@link CatchUpMonitor} reports catching up, else UP. Deliberately not part of
 * readiness, so a node working through a backlog keeps serving.
 */
@Component
public class CatchUpHealthIndicator implements HealthIndicator {

    private final CatchUpMonitor monitor;

    /** Creates the indicator. */
    public CatchUpHealthIndicator(CatchUpMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Health health() {
        return monitor.last().catchingUp() ? Health.outOfService().build() : Health.up().build();
    }
}
