package ai.foresignal.forecast;

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
 * A resolvable forecast question (prompt 122, table {@code forecast_question}, Intelligence-Design §8.1): a title, a
 * type ({@code binary}, {@code multiple}, {@code ordered}, {@code numeric}), the outcome space, verbatim resolution
 * criteria and a machine resolver (or {@code {type: manual}}), the opening, closing and resolution times, the status
 * ({@code open}, {@code closed}, {@code resolved}, {@code annulled}), the resolution or annulment, the category and
 * tags, the objects it is about, its origin and the dedup key. Questions are ledger rows, not records.
 */
@Entity
@Table(name = "forecast_question", indexes = {
    @Index(name = "forecast_question_status_due_idx", columnList = "status, resolve_by"),
    @Index(name = "forecast_question_category_idx", columnList = "category, status"),
    @Index(name = "forecast_question_dedup_idx", columnList = "dedup_key")
})
public class ForecastQuestionEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "type", length = 16, nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outcomes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> outcomes = new LinkedHashMap<>();

    @Column(name = "resolution_criteria", length = 2000, nullable = false)
    private String resolutionCriteria;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolver", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> resolver = new LinkedHashMap<>();

    @Column(name = "resolver_type", length = 32, nullable = false)
    private String resolverType;

    @Column(name = "opens_at", nullable = false)
    private Instant opensAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    @Column(name = "resolve_by", nullable = false)
    private Instant resolveBy;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolution", columnDefinition = "jsonb")
    private Map<String, Object> resolution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annulment", columnDefinition = "jsonb")
    private Map<String, Object> annulment;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags = new String[0];

    @Column(name = "situation_id", length = 26)
    private String situationId;

    @Column(name = "hypothesis_set_id", length = 26)
    private String hypothesisSetId;

    @Column(name = "precursor_id", length = 26)
    private String precursorId;

    @Column(name = "finding_id", length = 26)
    private String findingId;

    @Column(name = "series_ref", length = 500)
    private String seriesRef;

    @Column(name = "outlook_id", length = 26)
    private String outlookId;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

    @Column(name = "dedup_key", length = 200)
    private String dedupKey;

    @Column(name = "private", nullable = false)
    private boolean privateQuestion;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "ensemble_probability")
    private Double ensembleProbability;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "baseline_notes", columnDefinition = "jsonb")
    private Map<String, Object> baselineNotes = new LinkedHashMap<>();

    @Column(name = "resolution_requested_at")
    private Instant resolutionRequestedAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ForecastQuestionEntity() {}

    /**
     * Creates a question.
     *
     * @param id the ULID
     * @param now the creation time
     */
    public ForecastQuestionEntity(String id, Instant now) {
        this.id = id;
        this.createdAt = now;
        this.updatedAt = now;
        this.status = ForecastStatus.OPEN;
        this.version = 1;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String value) {
        this.title = value;
    }

    /** The type: {@code binary}, {@code multiple}, {@code ordered} or {@code numeric}. */
    public String getType() {
        return type;
    }

    /** Sets the type. */
    public void setType(String value) {
        this.type = value;
    }

    /** The outcome space ({@code labels}, {@code unit}, {@code lower}, {@code upper}). */
    public Map<String, Object> getOutcomes() {
        return outcomes == null ? new LinkedHashMap<>() : outcomes;
    }

    /** Sets the outcome space. */
    public void setOutcomes(Map<String, Object> value) {
        this.outcomes = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The outcome labels of a multiple or ordered question, in order (empty otherwise). */
    public List<String> labels() {
        Object labels = getOutcomes().get("labels");
        List<String> out = new ArrayList<>();
        if (labels instanceof List<?> list) {
            for (Object o : list) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

    /** The resolution criteria, shown verbatim to every forecaster. */
    public String getResolutionCriteria() {
        return resolutionCriteria;
    }

    /** Sets the resolution criteria. */
    public void setResolutionCriteria(String value) {
        this.resolutionCriteria = value;
    }

    /** The resolver ({@code ForecastResolver}). */
    public Map<String, Object> getResolver() {
        return resolver == null ? new LinkedHashMap<>() : resolver;
    }

    /** Sets the resolver and its type. */
    public void setResolver(Map<String, Object> value) {
        this.resolver = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        Object t = this.resolver.get("type");
        this.resolverType = t == null ? "manual" : t.toString();
    }

    /** The resolver type. */
    public String getResolverType() {
        return resolverType;
    }

    /** When the question opens. */
    public Instant getOpensAt() {
        return opensAt;
    }

    /** Sets when the question opens. */
    public void setOpensAt(Instant value) {
        this.opensAt = value;
    }

    /** When forecasts stop being accepted. */
    public Instant getClosesAt() {
        return closesAt;
    }

    /** Sets the closing time. */
    public void setClosesAt(Instant value) {
        this.closesAt = value;
    }

    /** When the question resolves. */
    public Instant getResolveBy() {
        return resolveBy;
    }

    /** Sets the resolution time. */
    public void setResolveBy(Instant value) {
        this.resolveBy = value;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** The resolution ({@code ForecastResolution}), or null. */
    public Map<String, Object> getResolution() {
        return resolution;
    }

    /** Sets the resolution. */
    public void setResolution(Map<String, Object> value) {
        this.resolution = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The annulment ({@code QuestionAnnulment}), or null. */
    public Map<String, Object> getAnnulment() {
        return annulment;
    }

    /** Sets the annulment. */
    public void setAnnulment(Map<String, Object> value) {
        this.annulment = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The category. */
    public String getCategory() {
        return category;
    }

    /** Sets the category. */
    public void setCategory(String value) {
        this.category = value;
    }

    /** The tags. */
    public List<String> getTags() {
        return tags == null ? List.of() : Arrays.asList(tags);
    }

    /** Sets the tags. */
    public void setTags(List<String> value) {
        this.tags = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** The linked situation, or null. */
    public String getSituationId() {
        return situationId;
    }

    /** Sets the linked situation. */
    public void setSituationId(String value) {
        this.situationId = value;
    }

    /** The linked hypothesis set (125), or null. */
    public String getHypothesisSetId() {
        return hypothesisSetId;
    }

    /** Sets the linked hypothesis set. */
    public void setHypothesisSetId(String value) {
        this.hypothesisSetId = value;
    }

    /** The linked precursor, or null. */
    public String getPrecursorId() {
        return precursorId;
    }

    /** Sets the linked precursor. */
    public void setPrecursorId(String value) {
        this.precursorId = value;
    }

    /** The linked finding, or null. */
    public String getFindingId() {
        return findingId;
    }

    /** Sets the linked finding. */
    public void setFindingId(String value) {
        this.findingId = value;
    }

    /** The linked series reference, or null. */
    public String getSeriesRef() {
        return seriesRef;
    }

    /** Sets the linked series reference. */
    public void setSeriesRef(String value) {
        this.seriesRef = value;
    }

    /** The linked outlook (126), or null. */
    public String getOutlookId() {
        return outlookId;
    }

    /** Sets the linked outlook. */
    public void setOutlookId(String value) {
        this.outlookId = value;
    }

    /** The origin: {@code operator}, {@code agent}, {@code system} or {@code analyst}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /** The dedup key, or null. */
    public String getDedupKey() {
        return dedupKey;
    }

    /** Sets the dedup key. */
    public void setDedupKey(String value) {
        this.dedupKey = value;
    }

    /** True for a private question (built from private data, or marked private). */
    public boolean isPrivateQuestion() {
        return privateQuestion;
    }

    /** Sets the private flag. */
    public void setPrivateQuestion(boolean value) {
        this.privateQuestion = value;
    }

    /** Who opened it (a token id, a component), or null. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets who opened it. */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /** The latest ensemble probability (binary), or null. */
    public Double getEnsembleProbability() {
        return ensembleProbability;
    }

    /** Sets the latest ensemble probability. */
    public void setEnsembleProbability(Double value) {
        this.ensembleProbability = value;
    }

    /** Why baseline forecasters emitted nothing, by forecaster id. */
    public Map<String, Object> getBaselineNotes() {
        return baselineNotes == null ? new LinkedHashMap<>() : baselineNotes;
    }

    /** Sets the baseline notes. */
    public void setBaselineNotes(Map<String, Object> value) {
        this.baselineNotes = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** When the manual-resolution notification was raised, or null. */
    public Instant getResolutionRequestedAt() {
        return resolutionRequestedAt;
    }

    /** Sets when the manual-resolution notification was raised. */
    public void setResolutionRequestedAt(Instant value) {
        this.resolutionRequestedAt = value;
    }

    /** The 116 manifest of the question's opening, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the opening manifest. */
    public void setManifestId(String value) {
        this.manifestId = value;
    }

    /** The version, incremented on every change. */
    public int getVersion() {
        return version;
    }

    /** Increments the version and stamps {@code updatedAt}. */
    public void touch(Instant now) {
        this.version++;
        this.updatedAt = now;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** True while forecasts are accepted at {@code now}. */
    public boolean acceptsForecasts(Instant now) {
        return ForecastStatus.OPEN.equals(status) && now.isBefore(closesAt) && !now.isBefore(opensAt);
    }

    /** True once resolved or annulled. */
    public boolean finished() {
        return ForecastStatus.RESOLVED.equals(status) || ForecastStatus.ANNULLED.equals(status);
    }
}
