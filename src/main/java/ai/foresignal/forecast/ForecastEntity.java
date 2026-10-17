package ai.foresignal.forecast;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One immutable forecast (prompt 122, table {@code forecast}, LED-1): written once and never updated; a newer forecast
 * of the same forecaster on the same question supersedes it ({@code supersedes_id}). {@code made_at} and
 * {@code asof_seq} are set by the server. A binary forecast carries {@code probability}, a multiple or ordered one a
 * {@code distribution} (label → probability, summing to 1 ± 1e-6), a numeric one monotone {@code quantiles}.
 */
@Entity
@Table(name = "forecast", indexes = {
    @Index(name = "forecast_question_forecaster_idx", columnList = "question_id, forecaster_id, made_at DESC"),
    @Index(name = "forecast_forecaster_idx", columnList = "forecaster_id, made_at")
})
public class ForecastEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false, updatable = false)
    private String id;

    @Column(name = "question_id", length = 26, nullable = false, updatable = false)
    private String questionId;

    @Column(name = "forecaster_id", length = 128, nullable = false, updatable = false)
    private String forecasterId;

    @Column(name = "forecaster_type", length = 32, nullable = false, updatable = false)
    private String forecasterType;

    @Column(name = "made_at", nullable = false, updatable = false)
    private Instant madeAt;

    @Column(name = "asof_seq", nullable = false, updatable = false)
    private long asOfSeq;

    @Column(name = "probability", updatable = false)
    private Double probability;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribution", columnDefinition = "jsonb", updatable = false)
    private Map<String, Double> distribution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quantiles", columnDefinition = "jsonb", updatable = false)
    private Map<String, Double> quantiles;

    @Column(name = "rationale", columnDefinition = "text", updatable = false)
    private String rationale;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "claims", columnDefinition = "jsonb", updatable = false)
    private List<Map<String, Object>> claims = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations", columnDefinition = "jsonb", updatable = false)
    private List<Map<String, Object>> citations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs", columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> inputs = new LinkedHashMap<>();

    @Column(name = "supersedes_id", length = 26, updatable = false)
    private String supersedesId;

    @Column(name = "recalibrated", nullable = false, updatable = false)
    private boolean recalibrated;

    @Column(name = "knowledge_cutoff", updatable = false)
    private LocalDate knowledgeCutoff;

    @Column(name = "manifest_id", length = 26, updatable = false)
    private String manifestId;

    /** For JPA. */
    protected ForecastEntity() {}

    /**
     * Creates a forecast.
     *
     * @param id the ULID
     * @param questionId the question
     * @param forecasterId the forecaster
     * @param forecasterType its type
     * @param madeAt the server time
     * @param asOfSeq the server's knowledge cutoff in seq
     */
    public ForecastEntity(String id, String questionId, String forecasterId, String forecasterType, Instant madeAt,
            long asOfSeq) {
        this.id = id;
        this.questionId = questionId;
        this.forecasterId = forecasterId;
        this.forecasterType = forecasterType;
        this.madeAt = madeAt;
        this.asOfSeq = asOfSeq;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The question. */
    public String getQuestionId() {
        return questionId;
    }

    /** The forecaster. */
    public String getForecasterId() {
        return forecasterId;
    }

    /** The forecaster's type when the forecast was made. */
    public String getForecasterType() {
        return forecasterType;
    }

    /** The server time it was made. */
    public Instant getMadeAt() {
        return madeAt;
    }

    /** The knowledge cutoff in seq. */
    public long getAsOfSeq() {
        return asOfSeq;
    }

    /** The binary probability, or null. */
    public Double getProbability() {
        return probability;
    }

    /** Sets the binary probability (before the first save only). */
    public void setProbability(Double value) {
        this.probability = value;
    }

    /** The distribution, or null. */
    public Map<String, Double> getDistribution() {
        return distribution;
    }

    /** Sets the distribution (before the first save only). */
    public void setDistribution(Map<String, Double> value) {
        this.distribution = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The quantiles, or null. */
    public Map<String, Double> getQuantiles() {
        return quantiles;
    }

    /** Sets the quantiles (before the first save only). */
    public void setQuantiles(Map<String, Double> value) {
        this.quantiles = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The rationale (Markdown), or null. */
    public String getRationale() {
        return rationale;
    }

    /** Sets the rationale (before the first save only). */
    public void setRationale(String value) {
        this.rationale = value;
    }

    /** Typed claims (LLM forecasters). */
    public List<Map<String, Object>> getClaims() {
        return claims == null ? List.of() : claims;
    }

    /** Sets the claims (before the first save only). */
    public void setClaims(List<Map<String, Object>> value) {
        this.claims = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The citations. */
    public List<Map<String, Object>> getCitations() {
        return citations == null ? List.of() : citations;
    }

    /** Sets the citations (before the first save only). */
    public void setCitations(List<Map<String, Object>> value) {
        this.citations = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The inputs. */
    public Map<String, Object> getInputs() {
        return inputs == null ? new LinkedHashMap<>() : inputs;
    }

    /** Sets the inputs (before the first save only). */
    public void setInputs(Map<String, Object> value) {
        this.inputs = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The forecast this one supersedes, or null. */
    public String getSupersedesId() {
        return supersedesId;
    }

    /** Sets the superseded forecast (before the first save only). */
    public void setSupersedesId(String value) {
        this.supersedesId = value;
    }

    /** True for an ensemble forecast built from Platt-recalibrated inputs. */
    public boolean isRecalibrated() {
        return recalibrated;
    }

    /** Sets the recalibrated flag (before the first save only). */
    public void setRecalibrated(boolean value) {
        this.recalibrated = value;
    }

    /** The model's knowledge cutoff (LLM forecasters), or null. */
    public LocalDate getKnowledgeCutoff() {
        return knowledgeCutoff;
    }

    /** Sets the knowledge cutoff (before the first save only). */
    public void setKnowledgeCutoff(LocalDate value) {
        this.knowledgeCutoff = value;
    }

    /** The 116 manifest. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest (before the first save only). */
    public void setManifestId(String value) {
        this.manifestId = value;
    }
}
