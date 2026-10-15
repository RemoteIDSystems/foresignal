package ai.foresignal.backtest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A precursor test (prompt 103): does X tend to precede Y within T? */
@Entity
@Table(name = "precursor_test")
public class PrecursorTestEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "statement", columnDefinition = "text")
    private String statement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "antecedent", nullable = false)
    private Map<String, Object> antecedent = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consequent", nullable = false)
    private Map<String, Object> consequent = new LinkedHashMap<>();

    @Column(name = "min_lag_seconds", nullable = false)
    private long minLagSeconds;

    @Column(name = "max_lag_seconds", nullable = false)
    private long maxLagSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spatial")
    private Map<String, Object> spatial;

    @Column(name = "dedup_window_seconds")
    private Long dedupWindowSeconds;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** JPA. */
    protected PrecursorTestEntity() {}

    /** Creates a precursor test. */
    public PrecursorTestEntity(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Map<String, Object> getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(Map<String, Object> antecedent) {
        this.antecedent = antecedent == null ? new LinkedHashMap<>() : antecedent;
    }

    public Map<String, Object> getConsequent() {
        return consequent;
    }

    public void setConsequent(Map<String, Object> consequent) {
        this.consequent = consequent == null ? new LinkedHashMap<>() : consequent;
    }

    public long getMinLagSeconds() {
        return minLagSeconds;
    }

    public void setMinLagSeconds(long minLagSeconds) {
        this.minLagSeconds = minLagSeconds;
    }

    public long getMaxLagSeconds() {
        return maxLagSeconds;
    }

    public void setMaxLagSeconds(long maxLagSeconds) {
        this.maxLagSeconds = maxLagSeconds;
    }

    public Map<String, Object> getSpatial() {
        return spatial;
    }

    public void setSpatial(Map<String, Object> spatial) {
        this.spatial = spatial;
    }

    public Long getDedupWindowSeconds() {
        return dedupWindowSeconds;
    }

    public void setDedupWindowSeconds(Long dedupWindowSeconds) {
        this.dedupWindowSeconds = dedupWindowSeconds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
