package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import java.time.Instant;

/**
 * The result of {@link HealthEvaluator#evaluate}.
 *
 * @param status the derived status
 * @param statusReason why, or null for a plainly healthy source
 * @param newestFrameAt the newest frame time a {@link HealthRule} reported, or null
 */
public record HealthAssessment(HealthStatus status, String statusReason, Instant newestFrameAt) {

    /** An assessment with no frame time. */
    public static HealthAssessment of(HealthStatus status, String reason) {
        return new HealthAssessment(status, reason, null);
    }
}
