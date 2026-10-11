package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import java.time.Instant;

/**
 * What a {@link HealthRule} concluded about a source.
 *
 * @param status {@link HealthStatus#HEALTHY}, {@link HealthStatus#DEGRADED} or
 *     {@link HealthStatus#FAILING}; a rule cannot make a source disabled or idle
 * @param reason why, for a non-healthy status
 * @param newestFrameAt for product sources, the newest frame time, reported in the health view
 *     whatever the status
 */
public record HealthVerdict(HealthStatus status, String reason, Instant newestFrameAt) {

    public HealthVerdict {
        if (status == null || !status.isRunning()) {
            throw new IllegalArgumentException("A health rule may only report healthy, degraded or failing");
        }
    }

    /** A healthy verdict, optionally carrying the newest frame time. */
    public static HealthVerdict healthy(Instant newestFrameAt) {
        return new HealthVerdict(HealthStatus.HEALTHY, null, newestFrameAt);
    }

    /** A degraded verdict. */
    public static HealthVerdict degraded(String reason, Instant newestFrameAt) {
        return new HealthVerdict(HealthStatus.DEGRADED, reason, newestFrameAt);
    }
}
