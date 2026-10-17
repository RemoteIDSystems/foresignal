package ai.foresignal.trigger.window;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One evaluated anomaly bucket (prompt 100, table {@code anomaly_observation}). The detector name,
 * version and parameters, the baseline summary, the watermark and evidence refs make the result
 * reproducible; {@code p_value} assumes the stated count model and is never the probability that
 * something is happening.
 */
@Entity
@Table(name = "anomaly_observation")
public class AnomalyObservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "group_key", nullable = false, length = 256)
    private String groupKey;

    @Column(name = "bucket_start", nullable = false)
    private Instant bucketStart;

    @Column(name = "bucket_end", nullable = false)
    private Instant bucketEnd;

    @Column(name = "observed", nullable = false)
    private int observed;

    @Column(name = "expected")
    private Double expected;

    @Column(name = "exposure")
    private Double exposure;

    @Column(name = "p_value")
    private Double pValue;

    @Column(name = "score")
    private Double score;

    @Column(name = "direction", length = 8)
    private String direction;

    @Column(name = "classification", nullable = false, length = 32)
    private String classification;

    @Column(name = "low_confidence", nullable = false)
    private boolean lowConfidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "baseline")
    private Map<String, Object> baseline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detector")
    private Map<String, Object> detector;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence")
    private Map<String, Object> evidence;

    @Column(name = "watermark_seq")
    private Long watermarkSeq;

    /** The analysis manifest of this output (prompt 116), or null for outputs written before it. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** JPA. */
    protected AnomalyObservationEntity() {}

    /** Creates one observation. */
    public AnomalyObservationEntity(UUID ruleId, String groupKey, Instant bucketStart, Instant bucketEnd, int observed) {
        this.ruleId = ruleId;
        this.groupKey = groupKey;
        this.bucketStart = bucketStart;
        this.bucketEnd = bucketEnd;
        this.observed = observed;
    }

    public Long getId() {
        return id;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public Instant getBucketStart() {
        return bucketStart;
    }

    public Instant getBucketEnd() {
        return bucketEnd;
    }

    public int getObserved() {
        return observed;
    }

    public Double getExpected() {
        return expected;
    }

    public void setExpected(Double expected) {
        this.expected = expected;
    }

    public Double getExposure() {
        return exposure;
    }

    public void setExposure(Double exposure) {
        this.exposure = exposure;
    }

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public boolean isLowConfidence() {
        return lowConfidence;
    }

    public void setLowConfidence(boolean lowConfidence) {
        this.lowConfidence = lowConfidence;
    }

    public Map<String, Object> getBaseline() {
        return baseline;
    }

    public void setBaseline(Map<String, Object> baseline) {
        this.baseline = baseline;
    }

    public Map<String, Object> getDetector() {
        return detector;
    }

    public void setDetector(Map<String, Object> detector) {
        this.detector = detector;
    }

    public Map<String, Object> getEvidence() {
        return evidence;
    }

    public void setEvidence(Map<String, Object> evidence) {
        this.evidence = evidence;
    }

    public Long getWatermarkSeq() {
        return watermarkSeq;
    }

    public void setWatermarkSeq(Long watermarkSeq) {
        this.watermarkSeq = watermarkSeq;
    }

    /** The analysis manifest of this output (prompt 116), or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the analysis manifest (prompt 116). */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
}
