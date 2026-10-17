package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * A first-class intelligence finding (prompt 101): a typed, located, time-bounded event built from
 * pinned record versions by deterministic correlation recipes, or created by an operator or agent.
 *
 * <p>{@code version} is an optimistic lock; {@code merged_into} points at the surviving event after a
 * same-event merge; {@code private} follows prompt 039 when any member source is private.
 */
@Entity
@Table(name = "intelligence_event")
public class IntelligenceEventEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "type", length = 64, nullable = false)
    private String type;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "severity", length = 16, nullable = false)
    private String severity = "warning";

    @Column(name = "status", length = 16, nullable = false)
    private String status = "emerging";

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "last_evidence_at")
    private Instant lastEvidenceAt;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @Column(name = "centroid", columnDefinition = "geometry(Point,4326)")
    private Point centroid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region_tags", nullable = false, columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> regionTags = new ArrayList<>();

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "confidence_label", length = 16)
    private String confidenceLabel;

    @Column(name = "confidence_method", length = 64)
    private String confidenceMethod;

    @Column(name = "calibrated", nullable = false, columnDefinition = "boolean default false")
    private boolean calibrated;

    @Column(name = "evidence_quality", length = 16)
    private String evidenceQuality;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detector")
    private Map<String, Object> detector;

    /** The analysis manifest of this output (prompt 116), or null for outputs written before it. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "recipe_id", length = 64)
    private String recipeId;

    @Column(name = "recipe_version")
    private Integer recipeVersion;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = "recipe";

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alternatives")
    private List<Map<String, Object>> alternatives = new ArrayList<>();

    @Column(name = "narrative", columnDefinition = "text")
    private String narrative;

    /** Operator or agent notes (markdown); never LLM output, never labeled speculation. */
    @Column(name = "operator_notes", columnDefinition = "text")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exposure")
    private Map<String, Object> exposure;

    /** Context added by annotators (prompt 105), for example {@code {weather: …}}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context")
    private Map<String, Object> context;

    @Column(name = "dedup_key", length = 256)
    private String dedupKey;

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @Column(name = "private", nullable = false, columnDefinition = "boolean default false")
    private boolean privateEvent;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected IntelligenceEventEntity() {}

    /** Creates an event with its identity and start. */
    public IntelligenceEventEntity(String id, String type, String title, Instant startTime) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Instant getLastEvidenceAt() {
        return lastEvidenceAt;
    }

    public void setLastEvidenceAt(Instant lastEvidenceAt) {
        this.lastEvidenceAt = lastEvidenceAt;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public List<String> getRegionTags() {
        return regionTags;
    }

    public void setRegionTags(List<String> regionTags) {
        this.regionTags = regionTags;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getConfidenceLabel() {
        return confidenceLabel;
    }

    public void setConfidenceLabel(String confidenceLabel) {
        this.confidenceLabel = confidenceLabel;
    }

    public String getConfidenceMethod() {
        return confidenceMethod;
    }

    public void setConfidenceMethod(String confidenceMethod) {
        this.confidenceMethod = confidenceMethod;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    public String getEvidenceQuality() {
        return evidenceQuality;
    }

    public void setEvidenceQuality(String evidenceQuality) {
        this.evidenceQuality = evidenceQuality;
    }

    public Map<String, Object> getDetector() {
        return detector;
    }

    public void setDetector(Map<String, Object> detector) {
        this.detector = detector;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public Integer getRecipeVersion() {
        return recipeVersion;
    }

    public void setRecipeVersion(Integer recipeVersion) {
        this.recipeVersion = recipeVersion;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Map<String, Object>> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<Map<String, Object>> alternatives) {
        this.alternatives = alternatives == null ? new ArrayList<>() : alternatives;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    /** Operator or agent notes (markdown), or null. */
    public String getNotes() {
        return notes;
    }

    /** Sets the operator or agent notes. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Map<String, Object> getExposure() {
        return exposure;
    }

    public void setExposure(Map<String, Object> exposure) {
        this.exposure = exposure;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    public String getMergedInto() {
        return mergedInto;
    }

    public void setMergedInto(String mergedInto) {
        this.mergedInto = mergedInto;
    }

    public boolean isPrivateEvent() {
        return privateEvent;
    }

    public void setPrivateEvent(boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
