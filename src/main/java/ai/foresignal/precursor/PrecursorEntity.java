package ai.foresignal.precursor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A pattern of the precursor library (prompt 120, table {@code precursor}): an event or series pair with its spatial
 * rule and lag bounds, its discovery and validation statistics (raw and adjusted p, verdict), the FDR batch it was
 * tested in, and its lifecycle ({@code candidate}, {@code validated}, {@code stale}, {@code retired}). The headline
 * statistics ({@code hit_rate}, {@code base_rate}, {@code lift}, {@code n_antecedent}) are those of the latest
 * out-of-sample window: validation, then each re-test. {@code candidate_key} identifies the pattern across batches.
 */
@Entity
@Table(name = "precursor", indexes = {
    @Index(name = "precursor_key_idx", columnList = "candidate_key", unique = true),
    @Index(name = "precursor_status_idx", columnList = "status, id")
})
public class PrecursorEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "candidate_key", length = 64, nullable = false)
    private String candidateKey;

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

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "families", columnDefinition = "text[]")
    private String[] families = new String[0];

    @Column(name = "lag_min_seconds", nullable = false)
    private long lagMinSeconds;

    @Column(name = "lag_max_seconds", nullable = false)
    private long lagMaxSeconds;

    @Column(name = "best_lag_seconds")
    private Long bestLagSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lag_distribution", columnDefinition = "jsonb")
    private Map<String, Object> lagDistribution;

    @Column(name = "hit_rate")
    private Double hitRate;

    @Column(name = "hit_rate_lower")
    private Double hitRateLower;

    @Column(name = "hit_rate_upper")
    private Double hitRateUpper;

    @Column(name = "base_rate")
    private Double baseRate;

    @Column(name = "lift")
    private Double lift;

    @Column(name = "correlation")
    private Double correlation;

    @Column(name = "correlation_sign", length = 8)
    private String correlationSign;

    @Column(name = "n_antecedent", nullable = false)
    private long nAntecedent;

    @Column(name = "hits", nullable = false)
    private long hits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "discovery", columnDefinition = "jsonb")
    private Map<String, Object> discovery = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation", columnDefinition = "jsonb")
    private Map<String, Object> validation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_retest", columnDefinition = "jsonb")
    private Map<String, Object> lastRetest;

    @Column(name = "batch_id", length = 26)
    private String batchId;

    @Column(name = "batch_size", nullable = false)
    private int batchSize;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "candidate";

    @Column(name = "status_reason", columnDefinition = "text")
    private String statusReason;

    @Column(name = "stale_count", nullable = false)
    private int staleCount;

    @Column(name = "operator_validated", nullable = false)
    private boolean operatorValidated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detector", columnDefinition = "jsonb")
    private Map<String, Object> detector = new LinkedHashMap<>();

    @Column(name = "detector_version", length = 64)
    private String detectorVersion;

    @Column(name = "first_validated_at")
    private Instant firstValidatedAt;

    @Column(name = "last_tested_at")
    private Instant lastTestedAt;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = "auto";

    @Column(name = "candidate_id", length = 26)
    private String candidateId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats", columnDefinition = "jsonb")
    private List<Map<String, Object>> caveats = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "history", columnDefinition = "jsonb")
    private List<Map<String, Object>> history = new ArrayList<>();

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected PrecursorEntity() {}

    /** Creates a row. */
    public PrecursorEntity(String id, String candidateKey, String kind, Instant createdAt) {
        this.id = id;
        this.candidateKey = candidateKey;
        this.kind = kind;
        this.createdAt = createdAt;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** The definition key (SHA-256 of the pattern). */
    public String getCandidateKey() {
        return candidateKey;
    }

    /** Sets: the definition key (SHA-256 of the pattern). */
    public void setCandidateKey(String value) {
        this.candidateKey = value;
    }

    /** {@code event} or {@code series}. */
    public String getKind() {
        return kind;
    }

    /** Sets: {@code event} or {@code series}. */
    public void setKind(String value) {
        this.kind = value;
    }

    /** The antecedent side ({@code PrecursorSide}). */
    public Map<String, Object> getAntecedent() {
        return antecedent;
    }

    /** Sets: the antecedent side ({@code PrecursorSide}). */
    public void setAntecedent(Map<String, Object> value) {
        this.antecedent = value;
    }

    /** The consequent side ({@code PrecursorSide}). */
    public Map<String, Object> getConsequent() {
        return consequent;
    }

    /** Sets: the consequent side ({@code PrecursorSide}). */
    public void setConsequent(Map<String, Object> value) {
        this.consequent = value;
    }

    /** The spatial rule ({@code PrecursorSpatial}). */
    public Map<String, Object> getSpatial() {
        return spatial;
    }

    /** Sets: the spatial rule ({@code PrecursorSpatial}). */
    public void setSpatial(Map<String, Object> value) {
        this.spatial = value;
    }

    /** The 040 regions. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : Arrays.asList(regionCodes);
    }

    /** Sets: the 040 regions. */
    public void setRegionCodes(List<String> value) {
        this.regionCodes = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** The antecedent and consequent families (for filtering). */
    public List<String> getFamilies() {
        return families == null ? List.of() : Arrays.asList(families);
    }

    /** Sets: the antecedent and consequent families (for filtering). */
    public void setFamilies(List<String> value) {
        this.families = value == null ? new String[0] : value.toArray(String[]::new);
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

    /** The best lag, seconds, or null. */
    public Long getBestLagSeconds() {
        return bestLagSeconds;
    }

    /** Sets: the best lag, seconds, or null. */
    public void setBestLagSeconds(Long value) {
        this.bestLagSeconds = value;
    }

    /** The lag quartiles of hits ({@code p25, median, p75}), or null. */
    public Map<String, Object> getLagDistribution() {
        return lagDistribution;
    }

    /** Sets: the lag quartiles of hits ({@code p25, median, p75}), or null. */
    public void setLagDistribution(Map<String, Object> value) {
        this.lagDistribution = value;
    }

    /** The hit rate (events), or null. */
    public Double getHitRate() {
        return hitRate;
    }

    /** Sets: the hit rate (events), or null. */
    public void setHitRate(Double value) {
        this.hitRate = value;
    }

    /** The lower bound of the hit rate's Wilson 95% interval, or null. */
    public Double getHitRateLower() {
        return hitRateLower;
    }

    /** Sets: the lower bound of the hit rate's Wilson 95% interval, or null. */
    public void setHitRateLower(Double value) {
        this.hitRateLower = value;
    }

    /** The upper bound of the hit rate's Wilson 95% interval, or null. */
    public Double getHitRateUpper() {
        return hitRateUpper;
    }

    /** Sets: the upper bound of the hit rate's Wilson 95% interval, or null. */
    public void setHitRateUpper(Double value) {
        this.hitRateUpper = value;
    }

    /** The base rate, or null. */
    public Double getBaseRate() {
        return baseRate;
    }

    /** Sets: the base rate, or null. */
    public void setBaseRate(Double value) {
        this.baseRate = value;
    }

    /** The lift, or null. */
    public Double getLift() {
        return lift;
    }

    /** Sets: the lift, or null. */
    public void setLift(Double value) {
        this.lift = value;
    }

    /** The cross-correlation at the best lag (series), or null. */
    public Double getCorrelation() {
        return correlation;
    }

    /** Sets: the cross-correlation at the best lag (series), or null. */
    public void setCorrelation(Double value) {
        this.correlation = value;
    }

    /** {@code positive}, {@code negative} or null. */
    public String getCorrelationSign() {
        return correlationSign;
    }

    /** Sets: {@code positive}, {@code negative} or null. */
    public void setCorrelationSign(String value) {
        this.correlationSign = value;
    }

    /** The antecedent occurrences behind the headline statistics. */
    public long getNAntecedent() {
        return nAntecedent;
    }

    /** Sets: the antecedent occurrences behind the headline statistics. */
    public void setNAntecedent(long value) {
        this.nAntecedent = value;
    }

    /** The hits behind the headline statistics. */
    public long getHits() {
        return hits;
    }

    /** Sets: the hits behind the headline statistics. */
    public void setHits(long value) {
        this.hits = value;
    }

    /** The discovery-window statistics ({@code PrecursorWindowStats}). */
    public Map<String, Object> getDiscovery() {
        return discovery;
    }

    /** Sets: the discovery-window statistics ({@code PrecursorWindowStats}). */
    public void setDiscovery(Map<String, Object> value) {
        this.discovery = value;
    }

    /** The validation-window statistics, or null. */
    public Map<String, Object> getValidation() {
        return validation;
    }

    /** Sets: the validation-window statistics, or null. */
    public void setValidation(Map<String, Object> value) {
        this.validation = value;
    }

    /** The statistics of the latest re-test window ({@code PrecursorWindowStats}), or null. */
    public Map<String, Object> getLastRetest() {
        return lastRetest;
    }

    /** Sets the statistics of the latest re-test window. */
    public void setLastRetest(Map<String, Object> value) {
        this.lastRetest = value;
    }

    /** The batch that tested it last. */
    public String getBatchId() {
        return batchId;
    }

    /** Sets: the batch that tested it last. */
    public void setBatchId(String value) {
        this.batchId = value;
    }

    /** The tests in that batch's FDR family. */
    public int getBatchSize() {
        return batchSize;
    }

    /** Sets: the tests in that batch's FDR family. */
    public void setBatchSize(int value) {
        this.batchSize = value;
    }

    /** The lifecycle status. */
    public String getStatus() {
        return status;
    }

    /** Sets: the lifecycle status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** Why it has its status, or null. */
    public String getStatusReason() {
        return statusReason;
    }

    /** Sets: why it has its status, or null. */
    public void setStatusReason(String value) {
        this.statusReason = value;
    }

    /** Consecutive stale re-tests. */
    public int getStaleCount() {
        return staleCount;
    }

    /** Sets: consecutive stale re-tests. */
    public void setStaleCount(int value) {
        this.staleCount = value;
    }

    /** True when an operator pinned it as validated. */
    public boolean isOperatorValidated() {
        return operatorValidated;
    }

    /** Sets: true when an operator pinned it as validated. */
    public void setOperatorValidated(boolean value) {
        this.operatorValidated = value;
    }

    /** The detector {@code {name, version, params}}. */
    public Map<String, Object> getDetector() {
        return detector;
    }

    /** Sets: the detector {@code {name, version, params}}. */
    public void setDetector(Map<String, Object> value) {
        this.detector = value;
    }

    /** The detector name and version. */
    public String getDetectorVersion() {
        return detectorVersion;
    }

    /** Sets: the detector name and version. */
    public void setDetectorVersion(String value) {
        this.detectorVersion = value;
    }

    /** When it was first validated, or null. */
    public Instant getFirstValidatedAt() {
        return firstValidatedAt;
    }

    /** Sets: when it was first validated, or null. */
    public void setFirstValidatedAt(Instant value) {
        this.firstValidatedAt = value;
    }

    /** When it was last tested, or null. */
    public Instant getLastTestedAt() {
        return lastTestedAt;
    }

    /** Sets: when it was last tested, or null. */
    public void setLastTestedAt(Instant value) {
        this.lastTestedAt = value;
    }

    /** {@code auto} or {@code operator}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets: {@code auto} or {@code operator}. */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /** The operator candidate it came from, or null. */
    public String getCandidateId() {
        return candidateId;
    }

    /** Sets: the operator candidate it came from, or null. */
    public void setCandidateId(String value) {
        this.candidateId = value;
    }

    /** The deterministic description. */
    public String getDescription() {
        return description;
    }

    /** Sets: the deterministic description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The caveats. */
    public List<Map<String, Object>> getCaveats() {
        return caveats;
    }

    /** Sets: the caveats. */
    public void setCaveats(List<Map<String, Object>> value) {
        this.caveats = value;
    }

    /** The status changes ({@code at, by, from, to, reason}). */
    public List<Map<String, Object>> getHistory() {
        return history;
    }

    /** Sets: the status changes ({@code at, by, from, to, reason}). */
    public void setHistory(List<Map<String, Object>> value) {
        this.history = value;
    }

    /** The 116 manifest. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets: the 116 manifest. */
    public void setManifestId(String value) {
        this.manifestId = value;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets: when it was created. */
    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets: when it last changed. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }


}
