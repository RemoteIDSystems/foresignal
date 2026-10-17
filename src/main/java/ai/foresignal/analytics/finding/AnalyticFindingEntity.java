package ai.foresignal.analytics.finding;

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
import org.locationtech.jts.geom.Geometry;

/**
 * One row of {@code analytic_finding} (prompt 119, Intelligence-Design §5.1): every column of the design plus
 * {@code dedup_key}, {@code superseded_by} and {@code version}, and three bookkeeping columns: {@code signal_family}
 * (what the linker and co-occurrence group by), {@code local_only_sources} and {@code raw_stat_keys} (the
 * {@code stats} members that hold raw values of {@code LOCAL_ONLY} series, withheld from callers without local egress
 * and never put on a record). Never deleted by retention (RET-3); a dismissed or superseded finding stays.
 */
@Entity
@Table(name = "analytic_finding", indexes = {
    @Index(name = "analytic_finding_type_detected_idx", columnList = "finding_type, detected_at DESC"),
    @Index(name = "analytic_finding_dedup_idx", columnList = "dedup_key, status"),
    @Index(name = "analytic_finding_updated_idx", columnList = "updated_at")
})
public class AnalyticFindingEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "finding_type", length = 32, nullable = false)
    private String findingType;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "severity", length = 16, nullable = false)
    private String severity;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "open";

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "window_start")
    private Instant windowStart;

    @Column(name = "window_end")
    private Instant windowEnd;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "series_refs", columnDefinition = "text[]")
    private String[] seriesRefs = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "members", columnDefinition = "jsonb")
    private List<String> members = new ArrayList<>();

    @Column(name = "asof_seq", nullable = false)
    private long asOfSeq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats", columnDefinition = "jsonb")
    private Map<String, Object> stats = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_rate", columnDefinition = "jsonb")
    private Map<String, Object> baseRate = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats", columnDefinition = "jsonb")
    private List<Map<String, Object>> caveats = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detector", columnDefinition = "jsonb")
    private Map<String, Object> detector = new LinkedHashMap<>();

    @Column(name = "situation_id", length = 26)
    private String situationId;

    @Column(name = "precursor_id", length = 26)
    private String precursorId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "question_ids", columnDefinition = "text[]")
    private String[] questionIds = new String[0];

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    @Column(name = "private", nullable = false)
    private boolean privateFinding;

    @Column(name = "signal_family", length = 64)
    private String signalFamily;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "local_only_sources", columnDefinition = "text[]")
    private String[] localOnlySources = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "raw_stat_keys", columnDefinition = "text[]")
    private String[] rawStatKeys = new String[0];

    @Column(name = "dedup_key", length = 700)
    private String dedupKey;

    @Column(name = "superseded_by", length = 26)
    private String supersededBy;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dismissal", columnDefinition = "jsonb")
    private Map<String, Object> dismissal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected AnalyticFindingEntity() {}

    /** Creates an open finding. */
    public AnalyticFindingEntity(String id, String findingType, String title, String severity, Instant detectedAt,
            Instant now) {
        this.id = id;
        this.findingType = findingType;
        this.title = title;
        this.severity = severity;
        this.detectedAt = detectedAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The finding type ({@code change-point}, {@code pre-event-signal}, …). */
    public String getFindingType() {
        return findingType;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** {@code info}, {@code notable} or {@code significant}. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** {@code open}, {@code superseded} or {@code dismissed}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The geometry, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the geometry. */
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /** The 040 region codes. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : Arrays.asList(regionCodes);
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> regionCodes) {
        this.regionCodes = regionCodes == null ? new String[0] : regionCodes.toArray(String[]::new);
    }

    /** The window start, or null. */
    public Instant getWindowStart() {
        return windowStart;
    }

    /** Sets the window start. */
    public void setWindowStart(Instant windowStart) {
        this.windowStart = windowStart;
    }

    /** The window end, or null. */
    public Instant getWindowEnd() {
        return windowEnd;
    }

    /** Sets the window end. */
    public void setWindowEnd(Instant windowEnd) {
        this.windowEnd = windowEnd;
    }

    /** When the detector found it. */
    public Instant getDetectedAt() {
        return detectedAt;
    }

    /** The series references. */
    public List<String> getSeriesRefs() {
        return seriesRefs == null ? List.of() : Arrays.asList(seriesRefs);
    }

    /** Sets the series references. */
    public void setSeriesRefs(List<String> seriesRefs) {
        this.seriesRefs = seriesRefs == null ? new String[0] : seriesRefs.toArray(String[]::new);
    }

    /** The pinned member references. */
    public List<String> getMembers() {
        return members == null ? List.of() : members;
    }

    /** Sets the members. */
    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    /** The seq the detector read up to. */
    public long getAsOfSeq() {
        return asOfSeq;
    }

    /** Sets {@link #getAsOfSeq()}. */
    public void setAsOfSeq(long asOfSeq) {
        this.asOfSeq = asOfSeq;
    }

    /** The method numbers. */
    public Map<String, Object> getStats() {
        return stats == null ? Map.of() : stats;
    }

    /** Sets the statistics. */
    public void setStats(Map<String, Object> stats) {
        this.stats = stats == null ? new LinkedHashMap<>() : new LinkedHashMap<>(stats);
    }

    /** The base rate. */
    public Map<String, Object> getBaseRate() {
        return baseRate == null ? Map.of() : baseRate;
    }

    /** Sets the base rate. */
    public void setBaseRate(Map<String, Object> baseRate) {
        this.baseRate = baseRate == null ? new LinkedHashMap<>() : new LinkedHashMap<>(baseRate);
    }

    /** The caveats. */
    public List<Map<String, Object>> getCaveats() {
        return caveats == null ? List.of() : caveats;
    }

    /** Sets the caveats. */
    public void setCaveats(List<Map<String, Object>> caveats) {
        this.caveats = caveats == null ? new ArrayList<>() : new ArrayList<>(caveats);
    }

    /** The detector {@code {name, version, params}}. */
    public Map<String, Object> getDetector() {
        return detector == null ? Map.of() : detector;
    }

    /** Sets the detector. */
    public void setDetector(Map<String, Object> detector) {
        this.detector = detector == null ? new LinkedHashMap<>() : new LinkedHashMap<>(detector);
    }

    /** The linked situation, or null. */
    public String getSituationId() {
        return situationId;
    }

    /** Sets the linked situation. */
    public void setSituationId(String situationId) {
        this.situationId = situationId;
    }

    /** The precursor (120), or null. */
    public String getPrecursorId() {
        return precursorId;
    }

    /** The forecast questions (122). */
    public List<String> getQuestionIds() {
        return questionIds == null ? List.of() : Arrays.asList(questionIds);
    }

    /** The deterministic explanation. */
    public String getExplanation() {
        return explanation;
    }

    /** Sets the explanation. */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /** True when built from a private source. */
    public boolean isPrivateFinding() {
        return privateFinding;
    }

    /** Sets {@link #isPrivateFinding()}. */
    public void setPrivateFinding(boolean privateFinding) {
        this.privateFinding = privateFinding;
    }

    /** The signal family, or null. */
    public String getSignalFamily() {
        return signalFamily;
    }

    /** Sets the signal family. */
    public void setSignalFamily(String signalFamily) {
        this.signalFamily = signalFamily;
    }

    /** The {@code LOCAL_ONLY} sources whose raw values the statistics hold. */
    public List<String> getLocalOnlySources() {
        return localOnlySources == null ? List.of() : Arrays.asList(localOnlySources);
    }

    /** Sets the local-only sources. */
    public void setLocalOnlySources(List<String> sources) {
        this.localOnlySources = sources == null ? new String[0] : sources.toArray(String[]::new);
    }

    /** The {@code stats} members that hold raw values. */
    public List<String> getRawStatKeys() {
        return rawStatKeys == null ? List.of() : Arrays.asList(rawStatKeys);
    }

    /** Sets the raw-value keys. */
    public void setRawStatKeys(List<String> keys) {
        this.rawStatKeys = keys == null ? new String[0] : keys.toArray(String[]::new);
    }

    /** The dedup key ({@code type|scope}), or null. */
    public String getDedupKey() {
        return dedupKey;
    }

    /** Sets the dedup key. */
    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    /** The finding that superseded this one, or null. */
    public String getSupersededBy() {
        return supersededBy;
    }

    /** Sets {@link #getSupersededBy()}. */
    public void setSupersededBy(String supersededBy) {
        this.supersededBy = supersededBy;
    }

    /** The change counter. */
    public int getVersion() {
        return version;
    }

    /** Increments the change counter. */
    public void bumpVersion() {
        this.version++;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The dismissal {@code {reason, at, by}}, or null. */
    public Map<String, Object> getDismissal() {
        return dismissal;
    }

    /** Sets the dismissal. */
    public void setDismissal(Map<String, Object> dismissal) {
        this.dismissal = dismissal == null ? null : new LinkedHashMap<>(dismissal);
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets {@link #getUpdatedAt()}. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
