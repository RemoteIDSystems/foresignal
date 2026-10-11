package ai.foresignal.api.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * The {@code SourceSettings} schema: how the operator has configured a source.
 *
 * @param enabled whether the source is collected
 * @param interval the poll interval in force
 * @param params the parameters in force, defaults applied
 * @param budget the request budget in force
 * @param retention the operator's per-source retention override, or null
 * @param credentialsSet whether credentials are stored (values are never returned)
 * @param updatedAt when the settings last changed, or null when never saved
 * @param ingestPolicy the operator's ingest-policy override, or null for the descriptor default
 * @param effectiveIngestPolicy the override if set, else the descriptor default (read-only)
 * @param effectiveRetention the retention window in force; for TRACK, {@code retention.trackWindow}
 *     (read-only)
 */
public record SourceSettingsDto(
        boolean enabled,
        Duration interval,
        Map<String, Object> params,
        BudgetDto budget,
        Duration retention,
        boolean credentialsSet,
        Instant updatedAt,
        IngestPolicyDto ingestPolicy,
        IngestPolicyDto effectiveIngestPolicy,
        Duration effectiveRetention) {}
