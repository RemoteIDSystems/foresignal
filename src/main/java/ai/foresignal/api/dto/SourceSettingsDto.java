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
 * @param embed whether the source's records are embedded for semantic search (prompt 027; default
 *     true when the source declares {@code embedFields})
 * @param credentialsSet whether credentials are stored (values are never returned)
 * @param updatedAt when the settings last changed, or null when never saved
 * @param ingestPolicy the operator's ingest-policy override, or null for the descriptor default
 * @param effectiveIngestPolicy the override if set, else the descriptor default (read-only)
 * @param effectiveRetention the retention window in force; for TRACK, {@code retention.trackWindow}
 *     (read-only)
 * @param statsEnabled whether the source is included in activity rollups (prompt 038; default true)
 * @param statsIncludePrivate whether a private source is rolled up anyway (prompt 038; default false)
 * @param privateSource {@code private}: the effective privacy (prompt 039), true when the descriptor is
 *     private or the operator tightened it; a private source is never embedded ({@code embed} false)
 */
public record SourceSettingsDto(
        boolean enabled,
        Duration interval,
        Map<String, Object> params,
        BudgetDto budget,
        Duration retention,
        boolean embed,
        boolean credentialsSet,
        Instant updatedAt,
        IngestPolicyDto ingestPolicy,
        IngestPolicyDto effectiveIngestPolicy,
        Duration effectiveRetention,
        boolean statsEnabled,
        boolean statsIncludePrivate,
        @com.fasterxml.jackson.annotation.JsonProperty("private") boolean privateSource) {}
