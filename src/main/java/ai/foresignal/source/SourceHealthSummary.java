package ai.foresignal.source;

import java.time.Instant;

/**
 * The slice of health a source summary and detail carry (OpenAPI {@code SourceSummary}: {@code status},
 * {@code lastSuccessAt}, {@code budgetUsedToday}, {@code lastDataAt}).
 *
 * <p>The full picture is {@code GET /v1/sources/{id}/health}; this is what a list of sources needs to
 * show at a glance.
 *
 * @param status the derived status
 * @param statusReason why, when the status is not plainly healthy; null otherwise
 * @param lastSuccessAt the last successful fetch or stream flush, or null
 * @param lastDataAt the last fetch or flush that returned records, or null (a 304 never sets it)
 * @param budgetUsedToday requests spent today, UTC
 */
public record SourceHealthSummary(
        HealthStatus status,
        String statusReason,
        Instant lastSuccessAt,
        Instant lastDataAt,
        long budgetUsedToday) {

    /** Reason given when health could not be computed. */
    public static final String UNAVAILABLE_REASON = "health unavailable";

    /**
     * A summary for when health cannot be computed, for example in a slice test with no health
     * service or after a database error while reading it.
     *
     * <p>Still answers {@code disabled} for a disabled source, because that much is known from the
     * settings alone.
     */
    public static SourceHealthSummary unavailable(boolean enabled) {
        return enabled
                ? new SourceHealthSummary(HealthStatus.IDLE, UNAVAILABLE_REASON, null, null, 0)
                : new SourceHealthSummary(HealthStatus.DISABLED, "source disabled", null, null, 0);
    }
}
