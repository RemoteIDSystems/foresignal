package ai.foresignal.precursor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An operator-registered candidate pair (prompt 120, table {@code precursor_candidate}): event selectors or series
 * references, a spatial rule and lag bounds. Every mining run includes it, outside the cap.
 */
@Entity
@Table(name = "precursor_candidate")
public class PrecursorCandidateEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "kind", length = 8, nullable = false)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "antecedent", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> antecedent = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consequent", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> consequent = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spatial", columnDefinition = "jsonb")
    private Map<String, Object> spatial = new LinkedHashMap<>();

    @Column(name = "lag_min_seconds", nullable = false)
    private long lagMinSeconds;

    @Column(name = "lag_max_seconds", nullable = false)
    private long lagMaxSeconds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** JPA. */
    protected PrecursorCandidateEntity() {}

    /** Creates a row. */
    public PrecursorCandidateEntity(String id, String kind, String createdBy, Instant createdAt) {
        this.id = id;
        this.kind = kind;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** An optional name. */
    public String getName() {
        return name;
    }

    /** Sets: an optional name. */
    public void setName(String value) {
        this.name = value;
    }

    /** {@code event} or {@code series}. */
    public String getKind() {
        return kind;
    }

    /** Sets: {@code event} or {@code series}. */
    public void setKind(String value) {
        this.kind = value;
    }

    /** The antecedent side. */
    public Map<String, Object> getAntecedent() {
        return antecedent;
    }

    /** Sets: the antecedent side. */
    public void setAntecedent(Map<String, Object> value) {
        this.antecedent = value;
    }

    /** The consequent side. */
    public Map<String, Object> getConsequent() {
        return consequent;
    }

    /** Sets: the consequent side. */
    public void setConsequent(Map<String, Object> value) {
        this.consequent = value;
    }

    /** The spatial rule. */
    public Map<String, Object> getSpatial() {
        return spatial;
    }

    /** Sets: the spatial rule. */
    public void setSpatial(Map<String, Object> value) {
        this.spatial = value;
    }

    /** The shortest lag, seconds. */
    public long getLagMinSeconds() {
        return lagMinSeconds;
    }

    /** Sets: the shortest lag, seconds. */
    public void setLagMinSeconds(long value) {
        this.lagMinSeconds = value;
    }

    /** The longest lag, seconds. */
    public long getLagMaxSeconds() {
        return lagMaxSeconds;
    }

    /** Sets: the longest lag, seconds. */
    public void setLagMaxSeconds(long value) {
        this.lagMaxSeconds = value;
    }

    /** The 040 regions. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : Arrays.asList(regionCodes);
    }

    /** Sets: the 040 regions. */
    public void setRegionCodes(List<String> value) {
        this.regionCodes = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** Who registered it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets: who registered it. */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /** When it was registered. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets: when it was registered. */
    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }


}
