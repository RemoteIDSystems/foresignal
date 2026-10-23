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
 * @param regionTagging {@code eager}, {@code lazy} or {@code off} (prompt 040; default eager for STANDARD,
 *     lazy for TRACK)
 * @param downsampleInterval the operator's history downsample interval override (prompt 044, TRACK
 *     sources only), or null for {@code retention.trackDownsampleInterval}
 * @param egress the operator's egress override (prompt 069; {@code OPEN} or {@code LOCAL_ONLY}), or null for
 *     the descriptor's {@code egress}
 * @param reliability the operator's Admiralty reliability override (prompt 109; {@code A}–{@code F}), or null for
 *     the descriptor's grade
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
        @com.fasterxml.jackson.annotation.JsonProperty("private") boolean privateSource,
        String regionTagging,
        Duration downsampleInterval,
        String egress,
        String reliability) {

    /** Settings without a reliability override (the shape before prompt 109). */
    public SourceSettingsDto(boolean enabled, Duration interval, Map<String, Object> params, BudgetDto budget,
            Duration retention, boolean embed, boolean credentialsSet, Instant updatedAt, IngestPolicyDto ingestPolicy,
            IngestPolicyDto effectiveIngestPolicy, Duration effectiveRetention, boolean statsEnabled,
            boolean statsIncludePrivate, boolean privateSource, String regionTagging, Duration downsampleInterval,
            String egress) {
        this(enabled, interval, params, budget, retention, embed, credentialsSet, updatedAt, ingestPolicy,
                effectiveIngestPolicy, effectiveRetention, statsEnabled, statsIncludePrivate, privateSource,
                regionTagging, downsampleInterval, egress, null);
    }

    /** Settings without an egress override (the shape before prompt 069). */
    public SourceSettingsDto(boolean enabled, Duration interval, Map<String, Object> params, BudgetDto budget,
            Duration retention, boolean embed, boolean credentialsSet, Instant updatedAt, IngestPolicyDto ingestPolicy,
            IngestPolicyDto effectiveIngestPolicy, Duration effectiveRetention, boolean statsEnabled,
            boolean statsIncludePrivate, boolean privateSource, String regionTagging, Duration downsampleInterval) {
        this(enabled, interval, params, budget, retention, embed, credentialsSet, updatedAt, ingestPolicy,
                effectiveIngestPolicy, effectiveRetention, statsEnabled, statsIncludePrivate, privateSource,
                regionTagging, downsampleInterval, (String) null);
    }

    /** Settings without a downsample interval override (the shape before prompt 044). */
    public SourceSettingsDto(boolean enabled, Duration interval, Map<String, Object> params, BudgetDto budget,
            Duration retention, boolean embed, boolean credentialsSet, Instant updatedAt, IngestPolicyDto ingestPolicy,
            IngestPolicyDto effectiveIngestPolicy, Duration effectiveRetention, boolean statsEnabled,
            boolean statsIncludePrivate, boolean privateSource, String regionTagging) {
        this(enabled, interval, params, budget, retention, embed, credentialsSet, updatedAt, ingestPolicy,
                effectiveIngestPolicy, effectiveRetention, statsEnabled, statsIncludePrivate, privateSource,
                regionTagging, (Duration) null);
    }

    /** Settings without a region tagging mode (the shape before prompt 040). */
    public SourceSettingsDto(boolean enabled, Duration interval, Map<String, Object> params, BudgetDto budget,
            Duration retention, boolean embed, boolean credentialsSet, Instant updatedAt, IngestPolicyDto ingestPolicy,
            IngestPolicyDto effectiveIngestPolicy, Duration effectiveRetention, boolean statsEnabled,
            boolean statsIncludePrivate, boolean privateSource) {
        this(enabled, interval, params, budget, retention, embed, credentialsSet, updatedAt, ingestPolicy,
                effectiveIngestPolicy, effectiveRetention, statsEnabled, statsIncludePrivate, privateSource,
                (String) null);
    }
}
