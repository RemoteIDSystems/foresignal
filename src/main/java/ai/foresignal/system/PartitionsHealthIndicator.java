package ai.foresignal.system;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.PartitionManager;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health contributor {@code partitions}, part of the {@code readiness} group (prompt 019): UP while
 * every tier has at least one block of seq headroom attached ahead of the allocator, so the writer
 * cannot run out of partitions; DOWN otherwise; UNKNOWN before the manager's first read.
 */
@Component
public class PartitionsHealthIndicator implements HealthIndicator {

    private final PartitionManager partitions;

    /** Creates the indicator. */
    public PartitionsHealthIndicator(PartitionManager partitions) {
        this.partitions = partitions;
    }

    @Override
    public Health health() {
        boolean known = true;
        for (HistoryTier tier : HistoryTier.values()) {
            double headroom = partitions.headroom(tier);
            if (Double.isNaN(headroom)) {
                known = false;
                continue;
            }
            if (headroom < partitions.block(tier)) {
                return Health.down().withDetail("tier", tier.name()).build();
            }
        }
        return known ? Health.up().build() : Health.unknown().build();
    }

    /** True when every tier's headroom is known and at least one block. */
    public boolean healthy() {
        return "UP".equals(health().getStatus().getCode());
    }
}
