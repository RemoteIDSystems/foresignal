package ai.foresignal.api.dto;

import ai.foresignal.source.HealthStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * The {@code SourceHealth} schema. Every value comes from {@code HealthService}; nothing is
 * recomputed in the API layer.
 *
 * @param sourceId the source
 * @param status current status
 * @param statusReason why it has that status
 * @param lastAttemptAt last fetch attempt
 * @param lastSuccessAt last successful fetch
 * @param lastErrorAt last failure
 * @param lastError last redacted error message
 * @param lastHttpStatus last upstream HTTP status
 * @param consecutiveFailures failures in a row
 * @param lastRecordsSeen records seen by the last fetch
 * @param lastRecordsChanged versions persisted by the last fetch
 * @param errorRateLastHour share of requests that failed in the last hour
 * @param backoffUntil end of the current backoff, or null
 * @param budget the request budget, or null when the source has none
 * @param budgetUsedToday requests used today, UTC
 * @param budgetRemainingToday requests left today, or null without a budget
 * @param refusedLastHour requests the budget refused in the last hour
 * @param requestsLastHour requests sent in the last hour
 * @param bytesLastHour response bytes in the last hour
 * @param coverage the last reported coverage, or null
 * @param storage storage cost (Core-Data-Model §15)
 * @param lastDataAt last OK fetch or flush with records
 * @param budgetHoursRemaining hours until today's budget runs out at the last hour's rate; null
 *     when that rate is 0
 * @param newestFrameAt product sources: newest frame time, or null
 * @param frameAgeSeconds product sources: age of the newest frame, or null
 * @param details adapter-specific details (063), omitted when there are none
 * @param spentUsdThisMonth paid adapters: US dollars spent this calendar month (135), omitted for free sources
 * @param lastDoctorReport the newest adapter-doctor report's summary (140), or null when the doctor never ran
 */
public record SourceHealthDto(
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
        BudgetDto budget,
        long budgetUsedToday,
        Long budgetRemainingToday,
        long refusedLastHour,
        long requestsLastHour,
        long bytesLastHour,
        CoverageDto coverage,
        StorageDto storage,
        Instant lastDataAt,
        Double budgetHoursRemaining,
        Instant newestFrameAt,
        Long frameAgeSeconds,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, Object> details,
        @JsonInclude(JsonInclude.Include.NON_NULL) Double spentUsdThisMonth,
        ai.foresignal.doctor.DoctorReportSummary lastDoctorReport) {

    /** A health view without a doctor report (the members before prompt 140). */
    public SourceHealthDto(String sourceId, HealthStatus status, String statusReason, Instant lastAttemptAt,
            Instant lastSuccessAt, Instant lastErrorAt, String lastError, Integer lastHttpStatus, int consecutiveFailures,
            int lastRecordsSeen, int lastRecordsChanged, double errorRateLastHour, Instant backoffUntil, BudgetDto budget,
            long budgetUsedToday, Long budgetRemainingToday, long refusedLastHour, long requestsLastHour,
            long bytesLastHour, CoverageDto coverage, StorageDto storage, Instant lastDataAt,
            Double budgetHoursRemaining, Instant newestFrameAt, Long frameAgeSeconds, Map<String, Object> details,
            Double spentUsdThisMonth) {
        this(sourceId, status, statusReason, lastAttemptAt, lastSuccessAt, lastErrorAt, lastError, lastHttpStatus,
                consecutiveFailures, lastRecordsSeen, lastRecordsChanged, errorRateLastHour, backoffUntil, budget,
                budgetUsedToday, budgetRemainingToday, refusedLastHour, requestsLastHour, bytesLastHour, coverage,
                storage, lastDataAt, budgetHoursRemaining, newestFrameAt, frameAgeSeconds, details, spentUsdThisMonth,
                null);
    }

    /** A copy with {@code report} as {@code lastDoctorReport} (140). */
    public SourceHealthDto withLastDoctorReport(ai.foresignal.doctor.DoctorReportSummary report) {
        return new SourceHealthDto(sourceId, status, statusReason, lastAttemptAt, lastSuccessAt, lastErrorAt, lastError,
                lastHttpStatus, consecutiveFailures, lastRecordsSeen, lastRecordsChanged, errorRateLastHour,
                backoffUntil, budget, budgetUsedToday, budgetRemainingToday, refusedLastHour, requestsLastHour,
                bytesLastHour, coverage, storage, lastDataAt, budgetHoursRemaining, newestFrameAt, frameAgeSeconds,
                details, spentUsdThisMonth, report);
    }
}
