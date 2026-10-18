package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.CoverageSample;
import java.time.Instant;
import java.util.Map;

/**
 * One source's health, shaped like the OpenAPI {@code SourceHealth} schema (FR-10).
 *
 * <p>{@code lastDoctorReport} is added by the prompt that owns it (140); {@code details} arrived with 063 and
 * {@code spentUsdThisMonth} with 135.
 *
 * @param sourceId the source
 * @param status the derived status
 * @param statusReason why, or null for a plainly healthy source
 * @param lastAttemptAt the last attempt, or null
 * @param lastSuccessAt the last success, or null
 * @param lastErrorAt the last failure, or null
 * @param lastError the last failure's redacted message, or null
 * @param lastHttpStatus the last failure's HTTP status, or null
 * @param consecutiveFailures failures since the last success
 * @param lastRecordsSeen records seen by the last success
 * @param lastRecordsChanged records persisted by the last success
 * @param errorRateLastHour errors / requests over the last hour, 0 with no requests
 * @param backoffUntil when the source may be fetched again, or null
 * @param budget the effective budget, or null for a source without one
 * @param budgetUsedToday requests spent today, UTC
 * @param budgetRemainingToday requests left today, or null without a budget
 * @param refusedLastHour requests the budget refused in the last hour
 * @param requestsLastHour requests sent in the last hour
 * @param bytesLastHour response bytes in the last hour
 * @param coverage the latest coverage report, or null
 * @param storage the storage cost
 * @param lastDataAt the last success that returned records, or null
 * @param budgetHoursRemaining hours until today's budget runs out at the last hour's rate, or null
 * @param newestFrameAt product sources: the newest frame, or null
 * @param frameAgeSeconds product sources: the newest frame's age, or null
 * @param details adapter-specific details from the {@link HealthDetailsContributor}s, or null when there
 *     are none (063: aisstream {@code compressionEnabled})
 * @param spentUsdThisMonth paid adapters: US dollars spent this calendar month (135), or null
 */
public record SourceHealthView(
        String sourceId,
        HealthStatus status,
        String statusReason,
        Instant lastAttemptAt,
        Instant lastSuccessAt,
        Instant lastErrorAt,
        String lastError,
        Integer lastHttpStatus,
        int consecutiveFailures,
        int lastRecordsSeen,
        int lastRecordsChanged,
        double errorRateLastHour,
        Instant backoffUntil,
        Budget budget,
        long budgetUsedToday,
        Long budgetRemainingToday,
        long refusedLastHour,
        long requestsLastHour,
        long bytesLastHour,
        CoverageSample coverage,
        SourceStorage storage,
        Instant lastDataAt,
        Double budgetHoursRemaining,
        Instant newestFrameAt,
        Long frameAgeSeconds,
        Map<String, Object> details,
        Double spentUsdThisMonth) {

    /** A view of a source that spends no money (the members before prompt 135). */
    public SourceHealthView(
            String sourceId,
            HealthStatus status,
            String statusReason,
            Instant lastAttemptAt,
            Instant lastSuccessAt,
            Instant lastErrorAt,
            String lastError,
            Integer lastHttpStatus,
            int consecutiveFailures,
            int lastRecordsSeen,
            int lastRecordsChanged,
            double errorRateLastHour,
            Instant backoffUntil,
            Budget budget,
            long budgetUsedToday,
            Long budgetRemainingToday,
            long refusedLastHour,
            long requestsLastHour,
            long bytesLastHour,
            CoverageSample coverage,
            SourceStorage storage,
            Instant lastDataAt,
            Double budgetHoursRemaining,
            Instant newestFrameAt,
            Long frameAgeSeconds,
            Map<String, Object> details) {
        this(sourceId, status, statusReason, lastAttemptAt, lastSuccessAt, lastErrorAt, lastError, lastHttpStatus,
                consecutiveFailures, lastRecordsSeen, lastRecordsChanged, errorRateLastHour, backoffUntil, budget,
                budgetUsedToday, budgetRemainingToday, refusedLastHour, requestsLastHour, bytesLastHour, coverage,
                storage, lastDataAt, budgetHoursRemaining, newestFrameAt, frameAgeSeconds, details, null);
    }
}
