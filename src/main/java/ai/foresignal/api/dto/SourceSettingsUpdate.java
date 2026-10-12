package ai.foresignal.api.dto;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * The {@code SourceSettingsUpdate} schema: a partial change to a source's settings.
 *
 * <p>A class with setters rather than a record, because the API distinguishes an omitted field
 * ("leave it alone") from an explicit JSON {@code null} ("clear it") for {@code retention} and
 * {@code ingestPolicy}. Jackson calls a setter for an explicit null and not at all for an absent
 * key, so each setter records that its key was present.
 *
 * <p>Durations stay strings here, so a malformed one is reported as a field error
 * ({@code errors[].field = "interval"}) by the mapper rather than as an unreadable body.
 */
public class SourceSettingsUpdate {

    private Boolean enabled;
    private String interval;
    private Map<String, Object> params;
    private @Valid BudgetDto budget;
    private String retention;
    private boolean retentionPresent;
    private @Valid IngestPolicyDto ingestPolicy;
    private boolean ingestPolicyPresent;
    private Boolean embed;
    private Boolean statsEnabled;
    private Boolean statsIncludePrivate;
    private Boolean privateSource;
    private String regionTagging;
    private String downsampleInterval;
    private boolean downsampleIntervalPresent;

    /** Whether the source should be collected; null leaves it unchanged. */
    public Boolean getEnabled() {
        return enabled;
    }

    /** Sets {@link #getEnabled()}. */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /** The poll interval as shorthand or ISO-8601; null leaves it unchanged. */
    public String getInterval() {
        return interval;
    }

    /** Sets {@link #getInterval()}. */
    public void setInterval(String interval) {
        this.interval = interval;
    }

    /** Adapter parameters, replacing the stored set; null leaves them unchanged. */
    public Map<String, Object> getParams() {
        return params;
    }

    /** Sets {@link #getParams()}. */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /** The request budget; null leaves it unchanged. */
    public BudgetDto getBudget() {
        return budget;
    }

    /** Sets {@link #getBudget()}. */
    public void setBudget(BudgetDto budget) {
        this.budget = budget;
    }

    /** The per-source retention override; see {@link #isRetentionPresent()}. */
    public String getRetention() {
        return retention;
    }

    /** Sets {@link #getRetention()} and records that the key was present. */
    public void setRetention(String retention) {
        this.retention = retention;
        this.retentionPresent = true;
    }

    /** True when the body named {@code retention}, even as null (null clears the override). */
    public boolean isRetentionPresent() {
        return retentionPresent;
    }

    /** The ingest-policy override; see {@link #isIngestPolicyPresent()}. */
    public IngestPolicyDto getIngestPolicy() {
        return ingestPolicy;
    }

    /** Sets {@link #getIngestPolicy()} and records that the key was present. */
    public void setIngestPolicy(IngestPolicyDto ingestPolicy) {
        this.ingestPolicy = ingestPolicy;
        this.ingestPolicyPresent = true;
    }

    /** True when the body named {@code ingestPolicy}, even as null (null clears the override). */
    public boolean isIngestPolicyPresent() {
        return ingestPolicyPresent;
    }

    /** Whether the source's records are embedded for semantic search (027); null leaves it unchanged. */
    public Boolean getEmbed() {
        return embed;
    }

    /** Sets {@link #getEmbed()}. */
    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    /** The history downsample interval override (prompt 044); see {@link #isDownsampleIntervalPresent()}. */
    public String getDownsampleInterval() {
        return downsampleInterval;
    }

    /** Sets {@link #getDownsampleInterval()} and records that the key was present. */
    public void setDownsampleInterval(String downsampleInterval) {
        this.downsampleInterval = downsampleInterval;
        this.downsampleIntervalPresent = true;
    }

    /** True when the body named {@code downsampleInterval}, even as null (null clears the override). */
    public boolean isDownsampleIntervalPresent() {
        return downsampleIntervalPresent;
    }

    /** {@code eager}, {@code lazy} or {@code off} (prompt 040); null leaves it unchanged. */
    public String getRegionTagging() {
        return regionTagging;
    }

    /** Sets {@link #getRegionTagging()}. */
    public void setRegionTagging(String regionTagging) {
        this.regionTagging = regionTagging;
    }

    /** Whether the source is included in activity rollups (038); null leaves it unchanged. */
    public Boolean getStatsEnabled() {
        return statsEnabled;
    }

    /** Sets {@link #getStatsEnabled()}. */
    public void setStatsEnabled(Boolean statsEnabled) {
        this.statsEnabled = statsEnabled;
    }

    /** Whether a private source is rolled up anyway (038 {@code stats.includePrivate}); null leaves it unchanged. */
    public Boolean getStatsIncludePrivate() {
        return statsIncludePrivate;
    }

    /** Sets {@link #getStatsIncludePrivate()}. */
    public void setStatsIncludePrivate(Boolean statsIncludePrivate) {
        this.statsIncludePrivate = statsIncludePrivate;
    }

    /**
     * The operator's {@code private} tightening (prompt 039): true makes the source private, false
     * removes the tightening (refused for a descriptor-private source); null leaves it unchanged.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("private")
    public Boolean getPrivateSource() {
        return privateSource;
    }

    /** Sets {@link #getPrivateSource()}. */
    @com.fasterxml.jackson.annotation.JsonProperty("private")
    public void setPrivateSource(Boolean privateSource) {
        this.privateSource = privateSource;
    }
}
