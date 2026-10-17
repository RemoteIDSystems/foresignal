package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An indicator of a hypothesis set (prompt 125, table {@code indicator}): an observable signal ({@code rule},
 * {@code precursor}, {@code series}, {@code intelEvent}, {@code situation}, {@code question} or {@code manual}) with its
 * 119 signal family, credibility and relevance, an optional {@code expected_by}, likelihood ratios per hypothesis and
 * optional absence overrides, its state, the 017 watch rule that watches for it (and the optional first-stage rule of
 * a two-stage threshold), the evidence that observed it and its origin.
 */
@Entity
@Table(name = "indicator", indexes = {
    @Index(name = "indicator_set_idx", columnList = "set_id"),
    @Index(name = "indicator_state_idx", columnList = "state, expected_by"),
    @Index(name = "indicator_rule_idx", columnList = "watch_rule_id")
})
public class IndicatorEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signal", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> signal = new LinkedHashMap<>();

    @Column(name = "signal_family", length = 64, nullable = false)
    private String signalFamily;

    @Column(name = "credibility", length = 1, nullable = false)
    private String credibility;

    @Column(name = "relevance", length = 1, nullable = false)
    private String relevance;

    @Column(name = "expected_by")
    private Instant expectedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lr", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> lr = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "absence_lr", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> absenceLr = new LinkedHashMap<>();

    @Column(name = "state", length = 32, nullable = false)
    private String state = HypothesisVocabulary.PENDING;

    @Column(name = "watch_rule_id")
    private UUID watchRuleId;

    @Column(name = "emerging_rule_id")
    private UUID emergingRuleId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "observed_evidence", columnDefinition = "jsonb")
    private Map<String, Object> observedEvidence;

    @Column(name = "p_detect")
    private Double pDetect;

    @Column(name = "last_state_change_at")
    private Instant lastStateChangeAt;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected IndicatorEntity() {}

    /**
     * Creates an indicator.
     *
     * @param id the ULID
     * @param setId the set
     * @param now the creation time
     */
    public IndicatorEntity(String id, String setId, Instant now) {
        this.id = id;
        this.setId = setId;
        this.createdAt = now;
        this.updatedAt = now;
        this.lastStateChangeAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** The description. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The signal ({@code IndicatorSignal}). */
    public Map<String, Object> getSignal() {
        return signal;
    }

    /** Sets the signal. */
    public void setSignal(Map<String, Object> value) {
        this.signal = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The signal type. */
    public String signalType() {
        Object type = signal == null ? null : signal.get("type");
        return type == null ? null : type.toString();
    }

    /** The 119 signal family. */
    public String getSignalFamily() {
        return signalFamily;
    }

    /** Sets the signal family. */
    public void setSignalFamily(String value) {
        this.signalFamily = value;
    }

    /** The credibility grade (H, M, L). */
    public String getCredibility() {
        return credibility;
    }

    /** Sets the credibility. */
    public void setCredibility(String value) {
        this.credibility = value;
    }

    /** The relevance grade (H, M, L). */
    public String getRelevance() {
        return relevance;
    }

    /** Sets the relevance. */
    public void setRelevance(String value) {
        this.relevance = value;
    }

    /** When the indicator is expected by, or null. */
    public Instant getExpectedBy() {
        return expectedBy;
    }

    /** Sets when the indicator is expected by. */
    public void setExpectedBy(Instant value) {
        this.expectedBy = value;
    }

    /** The likelihood per hypothesis id ({@code IndicatorLikelihood} maps). */
    public Map<String, Object> getLr() {
        return lr;
    }

    /** Sets the likelihoods. */
    public void setLr(Map<String, Object> value) {
        this.lr = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The absence LR overrides per hypothesis id. */
    public Map<String, Object> getAbsenceLr() {
        return absenceLr;
    }

    /** Sets the absence LR overrides. */
    public void setAbsenceLr(Map<String, Object> value) {
        this.absenceLr = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The state. */
    public String getState() {
        return state;
    }

    /** Sets the state and the time of the change. */
    public void setState(String value, Instant at) {
        if (!java.util.Objects.equals(this.state, value)) {
            this.lastStateChangeAt = at;
        }
        this.state = value;
    }

    /** True while the indicator still waits for its signal. */
    public boolean waiting() {
        return HypothesisVocabulary.PENDING.equals(state) || HypothesisVocabulary.EMERGING.equals(state);
    }

    /** The watch rule, or null. */
    public UUID getWatchRuleId() {
        return watchRuleId;
    }

    /** Sets the watch rule. */
    public void setWatchRuleId(UUID value) {
        this.watchRuleId = value;
    }

    /** The first-stage rule of a two-stage threshold, or null. */
    public UUID getEmergingRuleId() {
        return emergingRuleId;
    }

    /** Sets the first-stage rule. */
    public void setEmergingRuleId(UUID value) {
        this.emergingRuleId = value;
    }

    /** The evidence that observed it, or null. */
    public Map<String, Object> getObservedEvidence() {
        return observedEvidence;
    }

    /** Sets the observed evidence. */
    public void setObservedEvidence(Map<String, Object> value) {
        this.observedEvidence = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The 116 detection probability last computed, or null. */
    public Double getPDetect() {
        return pDetect;
    }

    /** Sets the detection probability. */
    public void setPDetect(Double value) {
        this.pDetect = value;
    }

    /** When the state last changed. */
    public Instant getLastStateChangeAt() {
        return lastStateChangeAt;
    }

    /** {@code operator}, {@code agent} or {@code analyst}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /** The version, incremented on every change. */
    public int getVersion() {
        return version;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Marks a change at {@code now} and increments the version. */
    public void touch(Instant now) {
        this.updatedAt = now;
        this.version++;
    }
}
