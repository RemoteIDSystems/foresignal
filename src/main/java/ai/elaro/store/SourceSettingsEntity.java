package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
