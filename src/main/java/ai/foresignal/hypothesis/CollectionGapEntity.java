package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A collection gap (prompt 125, table {@code collection_gap}): a pending indicator whose expected information gain
 * would be in its set's top three if it were fully observable, but whose detection probability is below 0.5 or whose
 * sources are disabled. The recommendations never change configuration; enabling a source stays an operator action.
 */
@Entity
@Table(name = "collection_gap", indexes = @Index(name = "collection_gap_set_idx", columnList = "set_id"),
        uniqueConstraints = @UniqueConstraint(name = "collection_gap_indicator_uq", columnNames = "indicator_id"))
public class CollectionGapEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @Column(name = "indicator_id", length = 26, nullable = false)
    private String indicatorId;

    @Column(name = "potential_eig", nullable = false)
    private double potentialEig;

    @Column(name = "actual_eig", nullable = false)
    private double actualEig;

    @Column(name = "p_detect")
    private Double pDetect;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> recommendations = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected CollectionGapEntity() {}

    /**
     * Creates a gap.
     *
     * @param id the ULID
     * @param setId the set
     * @param indicatorId the indicator
     * @param now the creation time
     */
    public CollectionGapEntity(String id, String setId, String indicatorId, Instant now) {
        this.id = id;
        this.setId = setId;
        this.indicatorId = indicatorId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** The indicator. */
    public String getIndicatorId() {
        return indicatorId;
    }

    /** The EIG with {@code p_detect = 1}. */
    public double getPotentialEig() {
        return potentialEig;
    }

    /** Sets the potential EIG. */
    public void setPotentialEig(double value) {
        this.potentialEig = value;
    }

    /** The EIG with the actual {@code p_detect}. */
    public double getActualEig() {
        return actualEig;
    }

    /** Sets the actual EIG. */
    public void setActualEig(double value) {
        this.actualEig = value;
    }

    /** The detection probability, or null. */
    public Double getPDetect() {
        return pDetect;
    }

    /** Sets the detection probability. */
    public void setPDetect(Double value) {
        this.pDetect = value;
    }

    /** The recommendations. */
    public List<Map<String, Object>> getRecommendations() {
        return recommendations;
    }

    /** Sets the recommendations. */
    public void setRecommendations(List<Map<String, Object>> value) {
        this.recommendations = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** When it was first found. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was last refreshed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it was last refreshed. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
