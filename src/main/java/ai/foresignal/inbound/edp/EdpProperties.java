package ai.foresignal.inbound.edp;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * External Data Protocol settings, bound from {@code foresignal.edp.*} (prompt 024).
 *
 * @param maxMinor the highest {@code foresignal-edp/1.N} minor accepted (0 for EDP 1.0); a higher
 *     minor is refused at registration with {@code PROTOCOL_UNSUPPORTED}
 * @param snapshotWindow how long a snapshot may take from its first page to its final page
 *     (docs/External-Data-Protocol.md §3.2; 30 min)
 * @param maxAbsentRatio the largest share of a scope's entities a snapshot may remove before the
 *     deletion is held for the operator (0.5)
 * @param validationBudget the item validation time budget per batch (EDP §7; 2 s)
 * @param manifestFetchTimeout the overall timeout for fetching a manifest or an items page
 */
@ConfigurationProperties("foresignal.edp")
public record EdpProperties(
        @DefaultValue("0") int maxMinor,
        @DefaultValue("30m") Duration snapshotWindow,
        @DefaultValue("0.5") double maxAbsentRatio,
        @DefaultValue("2s") Duration validationBudget,
        @DefaultValue("30s") Duration manifestFetchTimeout) {

    /** Checks the ranges. */
    public EdpProperties {
        if (maxMinor < 0) {
            throw new IllegalArgumentException("foresignal.edp.max-minor must not be negative");
        }
        if (maxAbsentRatio < 0 || maxAbsentRatio > 1) {
            throw new IllegalArgumentException("foresignal.edp.max-absent-ratio must be between 0 and 1");
        }
    }

    /** The defaults, for tests and callers without a Spring context. */
    public static EdpProperties defaults() {
        return new EdpProperties(0, Duration.ofMinutes(30), 0.5, Duration.ofSeconds(2), Duration.ofSeconds(30));
    }
}
