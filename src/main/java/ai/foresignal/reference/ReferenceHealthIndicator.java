package ai.foresignal.reference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health contributor {@code reference}, part of the {@code readiness} group (prompt 040). Always UP:
 * reference data never blocks readiness. Its details carry the load state of every dataset, so a failed
 * load shows up next to the other readiness details.
 */
@Component("reference")
public class ReferenceHealthIndicator implements HealthIndicator {

    private final ReferenceDataLoader loader;

    /** Creates the indicator. */
    public ReferenceHealthIndicator(ReferenceDataLoader loader) {
        this.loader = loader;
    }

    @Override
    public Health health() {
        Health.Builder health = Health.up();
        try {
            Map<String, Object> status = loader.status();
            Map<String, Object> states = new LinkedHashMap<>();
            if (status.get("datasets") instanceof List<?> datasets) {
                for (Object item : datasets) {
                    if (item instanceof Map<?, ?> dataset) {
                        Object error = dataset.get("error");
                        states.put(String.valueOf(dataset.get("name")),
                                error == null ? dataset.get("state") : dataset.get("state") + " (" + error + ")");
                    }
                }
            }
            health.withDetail("datasets", states).withDetail("loading", status.get("loading"));
        } catch (RuntimeException e) {
            health.withDetail("error", String.valueOf(e.getMessage()));
        }
        if (loader.lastError() != null) {
            health.withDetail("lastError", loader.lastError());
        }
        return health.build();
    }
}
