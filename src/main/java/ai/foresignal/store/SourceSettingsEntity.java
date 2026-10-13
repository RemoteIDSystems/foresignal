package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Subscription state for one source: whether it is collected, how often, and at what budget.
 *
 * <p>This row is the only thing that causes fetching (Architecture §1), so the collector reads it
 * and nothing else to decide what to do.
 */
@Entity
@Table(name = "source_settings")
public class SourceSettingsEntity {

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "interval_seconds", nullable = false)
    private int intervalSeconds;

    /** Adapter-specific parameters, validated against the adapter's descriptor. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", nullable = false)
    private Map<String, Object> params = new LinkedHashMap<>();

    @Column(name = "budget_per_minute", nullable = false)
    private int budgetPerMinute;

    @Column(name = "budget_per_day", nullable = false)
    private int budgetPerDay;

    /** Per-source retention override as a duration string; null means the global default. */
    @Column(name = "retention")
    private String retention;

    /**
     * Operator override for the ingest policy, as the JSON of {@code IngestPolicy}; null means the
     * descriptor's default (Core-Data-Model §5).
     *
     * <p>Stored as raw JSON text in a {@code jsonb} column. Hibernate's JSON format mapper passes a
     * {@code String} through verbatim, so the document is stored as written rather than
     * re-encoded as a JSON string.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingest_policy")
    private String ingestPolicy;

    /**
     * Operator override of whether this source's records are embedded for semantic search
     * (prompt 027); null means the default, which is on when the descriptor declares
     * {@code embedFields}.
     */
    @Column(name = "embed")
    private Boolean embed;

    /**
     * Per-source override of the history downsample interval (prompt 044), TRACK sources only; null
     * means {@code retention.trackDownsampleInterval}. A PostgreSQL {@code interval}.
     */
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "downsample_interval")
    private Duration downsampleInterval;

    /**
     * The operator's egress override (prompt 069), {@code OPEN} or {@code LOCAL_ONLY}; null means the
     * descriptor's {@code egress}. Accepted only for an {@code egressOverridable} descriptor.
     */
    @Column(name = "egress", length = 16)
    private String egress;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Guards against two concurrent API writes clobbering each other. */
    @Version
    @Column(name = "lock_version", nullable = false)
    private long lockVersion;

    protected SourceSettingsEntity() {
        // for JPA
    }

    public SourceSettingsEntity(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params == null ? new LinkedHashMap<>() : params;
    }

    public int getBudgetPerMinute() {
        return budgetPerMinute;
    }

    public void setBudgetPerMinute(int budgetPerMinute) {
        this.budgetPerMinute = budgetPerMinute;
    }

    public int getBudgetPerDay() {
        return budgetPerDay;
    }

    public void setBudgetPerDay(int budgetPerDay) {
        this.budgetPerDay = budgetPerDay;
    }

    public String getRetention() {
        return retention;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    public String getIngestPolicy() {
        return ingestPolicy;
    }

    public void setIngestPolicy(String ingestPolicy) {
        this.ingestPolicy = ingestPolicy;
    }

    /** The {@code embed} override, or null for the default. */
    public Boolean getEmbed() {
        return embed;
    }

    /** Sets the {@code embed} override; null reverts to the default. */
    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    /** The egress override (prompt 069), or null for the descriptor's. */
    public String getEgress() {
        return egress;
    }

    /** Sets the egress override; null reverts to the descriptor's. */
    public void setEgress(String egress) {
        this.egress = egress;
    }

    /** The downsample interval override (prompt 044), or null for the global interval. */
    public Duration getDownsampleInterval() {
        return downsampleInterval;
    }

    /** Sets the downsample interval override; null reverts to the global interval. */
    public void setDownsampleInterval(Duration downsampleInterval) {
        this.downsampleInterval = downsampleInterval;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLockVersion() {
        return lockVersion;
    }
}
