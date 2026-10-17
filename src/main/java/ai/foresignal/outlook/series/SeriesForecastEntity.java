package ai.foresignal.outlook.series;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of {@code series_forecast} (prompt 126): a quantile forecast of one series for one horizon and target
 * period, made nightly. {@code quantiles} holds the ledger levels p05…p95 (monotone); {@code tails} holds p01 and
 * p99 from the same predictive distribution (the {@code significant} exceedance band). {@code standardization} holds
 * the mean and standard deviation of the recent history, so {@code LOCAL_ONLY} series can be served in standardized
 * units. Append-only apart from the exceedance bookkeeping.
 */
@Entity
@Table(name = "series_forecast", indexes = {
    @Index(name = "series_forecast_ref_idx", columnList = "ref, made_at"),
    @Index(name = "series_forecast_target_idx", columnList = "ref, horizon, target_start"),
    @Index(name = "series_forecast_check_idx", columnList = "exceedance_checked, horizon")
})
public class SeriesForecastEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "ref", length = 600, nullable = false)
    private String ref;

    @Column(name = "made_at", nullable = false)
    private Instant madeAt;

    @Column(name = "asof_seq", nullable = false)
    private long asofSeq;

    @Column(name = "horizon", nullable = false)
    private int horizon;

    @Column(name = "target_start", nullable = false)
    private Instant targetStart;

    @Column(name = "target_end", nullable = false)
    private Instant targetEnd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quantiles", columnDefinition = "jsonb", nullable = false)
    private Map<String, Double> quantiles = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tails", columnDefinition = "jsonb")
    private Map<String, Double> tails = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "standardization", columnDefinition = "jsonb")
    private Map<String, Double> standardization = new LinkedHashMap<>();

    @Column(name = "method", length = 32, nullable = false)
    private String method;

    @Column(name = "version", length = 32, nullable = false)
    private String version;

    @Column(name = "unit", length = 64)
    private String unit;

    @Column(name = "local_only", nullable = false)
    private boolean localOnly;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "question_id", length = 26)
    private String questionId;

    @Column(name = "exceedance_checked", nullable = false)
    private boolean exceedanceChecked;

    @Column(name = "finding_id", length = 26)
    private String findingId;

    /** JPA. */
    protected SeriesForecastEntity() {}

    /** Creates a row. */
    public SeriesForecastEntity(String id, String ref, Instant madeAt, long asofSeq, int horizon, Instant targetStart,
            Instant targetEnd, String method, String version) {
        this.id = id;
        this.ref = ref;
        this.madeAt = madeAt;
        this.asofSeq = asofSeq;
        this.horizon = horizon;
        this.targetStart = targetStart;
        this.targetEnd = targetEnd;
        this.method = method;
        this.version = version;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** The series reference. */
    public String getRef() {
        return ref;
    }

    /** When the forecast was made. */
    public Instant getMadeAt() {
        return madeAt;
    }

    /** The seq the history was read up to. */
    public long getAsofSeq() {
        return asofSeq;
    }

    /** The horizon, days. */
    public int getHorizon() {
        return horizon;
    }

    /** The target period start. */
    public Instant getTargetStart() {
        return targetStart;
    }

    /** The target period end (exclusive). */
    public Instant getTargetEnd() {
        return targetEnd;
    }

    /** The ledger quantiles p05…p95. */
    public Map<String, Double> getQuantiles() {
        return quantiles;
    }

    /** Sets the ledger quantiles. */
    public void setQuantiles(Map<String, Double> quantiles) {
        this.quantiles = new LinkedHashMap<>(quantiles);
    }

    /** The tails p01 and p99. */
    public Map<String, Double> getTails() {
        return tails;
    }

    /** Sets the tails. */
    public void setTails(Map<String, Double> tails) {
        this.tails = tails == null ? new LinkedHashMap<>() : new LinkedHashMap<>(tails);
    }

    /** The {@code mean} and {@code sd} of the recent history. */
    public Map<String, Double> getStandardization() {
        return standardization;
    }

    /** Sets the standardization. */
    public void setStandardization(Map<String, Double> standardization) {
        this.standardization = standardization == null ? new LinkedHashMap<>()
                : new LinkedHashMap<>(standardization);
    }

    /** The method. */
    public String getMethod() {
        return method;
    }

    /** The forecaster version. */
    public String getVersion() {
        return version;
    }

    /** The unit, or null. */
    public String getUnit() {
        return unit;
    }

    /** Sets the unit. */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /** True when a source of the series is {@code LOCAL_ONLY}. */
    public boolean isLocalOnly() {
        return localOnly;
    }

    /** Sets whether a source is {@code LOCAL_ONLY}. */
    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The ledger question, or null. */
    public String getQuestionId() {
        return questionId;
    }

    /** Sets the ledger question. */
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    /** True once the realized value was compared with the band. */
    public boolean isExceedanceChecked() {
        return exceedanceChecked;
    }

    /** Marks the exceedance check done. */
    public void setExceedanceChecked(boolean exceedanceChecked) {
        this.exceedanceChecked = exceedanceChecked;
    }

    /** The {@code forecast-exceedance} finding, or null. */
    public String getFindingId() {
        return findingId;
    }

    /** Sets the finding. */
    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }
}
