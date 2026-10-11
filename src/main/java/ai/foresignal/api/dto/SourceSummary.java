package ai.foresignal.api.dto;

import ai.foresignal.source.HealthStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The {@code SourceSummary} schema: one row of {@code GET /v1/sources}.
 *
 * @param id source id
 * @param name display name
 * @param kinds record kinds, sorted
 * @param mode {@code polling}, {@code streaming}, {@code push} or {@code derived}
 * @param custom true for a runtime-defined custom push source
 * @param enabled whether the source is collected
 * @param status current health status
 * @param credentialsRequired true if any credential field is required
 * @param credentialsSet true when credentials are stored
 * @param interval the poll interval in force
 * @param budget the request budget in force
 * @param budgetUsedToday requests used today, UTC
 * @param lastSuccessAt the last successful fetch, or null
 * @param historyTier {@code STANDARD} or {@code TRACK}
 * @param licence license identifier (wire name {@code licence})
 * @param termsUrl the upstream's terms
 * @param lastDataAt the last fetch or flush that returned records (never a 304), or null
 */
public record SourceSummary(
        String id,
        String name,
        List<String> kinds,
        String mode,
        boolean custom,
        boolean enabled,
        HealthStatus status,
        boolean credentialsRequired,
        boolean credentialsSet,
        Duration interval,
        BudgetDto budget,
        long budgetUsedToday,
        Instant lastSuccessAt,
        String historyTier,
        String licence,
        String termsUrl,
        Instant lastDataAt) {}
