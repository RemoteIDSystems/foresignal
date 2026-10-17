package ai.foresignal.situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * A persistent storyline (prompt 118): related intelligence events, findings, alerts, anomalies, records and
 * investigations grouped over days to months, with a lifecycle, a deterministic summary and an optional LLM brief.
 *
 * <p>{@code version} is an optimistic lock; {@code merged_into} points at the surviving situation after a merge;
 * {@code private} is true when any member comes from a private source (039). Members are evidence; status,
 * severity and summary are inference by {@link SituationDeriver}; the brief is labeled speculation and never
 * changes either. The GiST index on {@code geom} and the GIN index on {@code region_codes} are created by
 * {@code SpatialIndexInitializer}.
 */
@Entity
@Table(name = "situation", indexes = @Index(name = "situation_status_activity_idx",
        columnList = "status, last_activity_at DESC"))
public class SituationEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "theme", length = 32, nullable = false)
    private String theme = SituationThemes.MIXED;

    @Column(name = "status", length = 16, nullable = false)
    private String status = SituationLifecycle.EMERGING;

    @Column(name = "severity", length = 16, nullable = false)
    private String severity = "info";

    @Column(name = "severity_override", length = 16)
    private String severityOverride;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @Column(name = "centroid", columnDefinition = "geometry(Point,4326)")
    private Point centroid;

    @Column(name = "area_km2")
    private Double areaKm2;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "topic_ids", columnDefinition = "text[]")
    private String[] topicIds = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "entity_ids", columnDefinition = "text[]")
    private String[] entityIds = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "investigation_ids", columnDefinition = "text[]")
    private String[] investigationIds = new String[0];

    @Column(name = "summary", columnDefinition = "text", nullable = false)
    private String summary = "";

    @Column(name = "brief", columnDefinition = "text")
    private String brief;

    @Column(name = "brief_generated_at")
    private Instant briefGeneratedAt;

    @Column(name = "brief_model", length = 200)
    private String briefModel;

    /** The analysis manifest of the current brief (prompt 116), or null. */
    @Column(name = "brief_manifest_id", length = 26)
    private String briefManifestId;

    /** The number of core members when the brief was last generated, for the "3 new core members" trigger. */
    @Column(name = "brief_core_mark", nullable = false, columnDefinition = "integer default 0")
    private int briefCoreMark;

    /** True while a brief regeneration is wanted but debounced or queued. */
    @Column(name = "brief_pending", nullable = false, columnDefinition = "boolean default false")
    private boolean briefPending;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = "auto";

    @Column(name = "linker_version", length = 32)
    private String linkerVersion;

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @Column(name = "private", nullable = false, columnDefinition = "boolean default false")
    private boolean privateSituation;

    /** The member count last derived. */
    @Column(name = "member_count", nullable = false, columnDefinition = "integer default 0")
    private int memberCount;

    /** The signal families of the core members, last derived. */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "signal_families", columnDefinition = "text[]")
    private String[] signalFamilies = new String[0];

    /** The corroboration share last derived (prompt 116 roll-up), or null when no member has a canonical event. */
    @Column(name = "corroboration_share")
    private Double corroborationShare;

    /** The analysis manifest of the last derivation (prompt 116), or null. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected SituationEntity() {}

    /**
     * Creates a situation.
     *
     * @param id the ULID
     * @param title the title
     * @param theme the theme ({@link SituationThemes})
     * @param startedAt when its first member was observed
     */
    public SituationEntity(String id, String title, String theme, Instant startedAt) {
        this.id = id;
        this.title = title;
        this.theme = theme;
        this.startedAt = startedAt;
        this.lastActivityAt = startedAt;
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
    public void setTitle(String title) {
        this.title = title;
    }

    /** The theme. */
    public String getTheme() {
        return theme;
    }

    /** Sets the theme. */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /** The status: emerging, active, dormant or closed. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The severity in force (the override when set, else the derived one). */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity in force. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** The operator's severity override, or null. */
    public String getSeverityOverride() {
        return severityOverride;
    }

    /** Sets the severity override (null clears it). */
    public void setSeverityOverride(String severityOverride) {
        this.severityOverride = severityOverride;
    }

    /** The 040 region codes, sorted. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : List.of(regionCodes);
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> codes) {
        this.regionCodes = codes == null ? new String[0] : codes.toArray(String[]::new);
    }

    /** The buffered convex hull of the member geometries, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the geometry. */
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /** The centroid of the geometry, or null. */
    public Point getCentroid() {
        return centroid;
    }

    /** Sets the centroid. */
    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    /** The area of the geometry, km², or null. */
    public Double getAreaKm2() {
        return areaKm2;
    }

    /** Sets the area. */
    public void setAreaKm2(Double areaKm2) {
        this.areaKm2 = areaKm2;
    }

    /** When the first member was observed. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets the start. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When the latest core member was observed. */
    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    /** Sets the last activity. */
    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    /** When the situation was closed, or null. */
    public Instant getClosedAt() {
        return closedAt;
    }

    /** Sets the closure time. */
    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    /** The linked 117 attention topics. */
    public List<String> getTopicIds() {
        return topicIds == null ? List.of() : List.of(topicIds);
    }

    /** Sets the topics. */
    public void setTopicIds(List<String> ids) {
        this.topicIds = ids == null ? new String[0] : ids.toArray(String[]::new);
    }

    /** The linked 098 entities. */
    public List<String> getEntityIds() {
        return entityIds == null ? List.of() : List.of(entityIds);
    }

    /** Sets the entities. */
    public void setEntityIds(List<String> ids) {
        this.entityIds = ids == null ? new String[0] : ids.toArray(String[]::new);
    }

    /** The linked 101 investigations. */
    public List<String> getInvestigationIds() {
        return investigationIds == null ? List.of() : new ArrayList<>(Arrays.asList(investigationIds));
    }

    /** Sets the investigations. */
    public void setInvestigationIds(List<String> ids) {
        this.investigationIds = ids == null ? new String[0] : ids.toArray(String[]::new);
    }

    /** The deterministic summary (inference). */
    public String getSummary() {
        return summary;
    }

    /** Sets the summary. */
    public void setSummary(String summary) {
        this.summary = summary == null ? "" : summary;
    }

    /** The optional LLM brief (speculation), or null. */
    public String getBrief() {
        return brief;
    }

    /** Sets the brief. */
    public void setBrief(String brief) {
        this.brief = brief;
    }

    /** When the brief was generated, or null. */
    public Instant getBriefGeneratedAt() {
        return briefGeneratedAt;
    }

    /** Sets the brief time. */
    public void setBriefGeneratedAt(Instant briefGeneratedAt) {
        this.briefGeneratedAt = briefGeneratedAt;
    }

    /** The model that wrote the brief, or null. */
    public String getBriefModel() {
        return briefModel;
    }

    /** Sets the brief model. */
    public void setBriefModel(String briefModel) {
        this.briefModel = briefModel;
    }

    /** The manifest of the brief, or null. */
    public String getBriefManifestId() {
        return briefManifestId;
    }

    /** Sets the brief manifest. */
    public void setBriefManifestId(String briefManifestId) {
        this.briefManifestId = briefManifestId;
    }

    /** The core member count at the last brief. */
    public int getBriefCoreMark() {
        return briefCoreMark;
    }

    /** Sets the core member count at the last brief. */
    public void setBriefCoreMark(int briefCoreMark) {
        this.briefCoreMark = briefCoreMark;
    }

    /** True while a brief is wanted but not yet generated. */
    public boolean isBriefPending() {
        return briefPending;
    }

    /** Marks a brief as wanted or done. */
    public void setBriefPending(boolean briefPending) {
        this.briefPending = briefPending;
    }

    /** auto, operator, agent or analyst. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /** The linker version that created it, or null for manual situations. */
    public String getLinkerVersion() {
        return linkerVersion;
    }

    /** Sets the linker version. */
    public void setLinkerVersion(String linkerVersion) {
        this.linkerVersion = linkerVersion;
    }

    /** The surviving situation after a merge, or null. */
    public String getMergedInto() {
        return mergedInto;
    }

    /** Sets the merge target. */
    public void setMergedInto(String mergedInto) {
        this.mergedInto = mergedInto;
    }

    /** True when any member is private. */
    public boolean isPrivateSituation() {
        return privateSituation;
    }

    /** Sets the private flag. */
    public void setPrivateSituation(boolean privateSituation) {
        this.privateSituation = privateSituation;
    }

    /** The member count. */
    public int getMemberCount() {
        return memberCount;
    }

    /** Sets the member count. */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    /** The signal families of the core members. */
    public List<String> getSignalFamilies() {
        return signalFamilies == null ? List.of() : List.of(signalFamilies);
    }

    /** Sets the signal families. */
    public void setSignalFamilies(java.util.Collection<String> families) {
        this.signalFamilies = families == null ? new String[0] : families.toArray(String[]::new);
    }

    /** The corroboration share, or null. */
    public Double getCorroborationShare() {
        return corroborationShare;
    }

    /** Sets the corroboration share. */
    public void setCorroborationShare(Double corroborationShare) {
        this.corroborationShare = corroborationShare;
    }

    /** The manifest of the last derivation, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The optimistic-locking version. */
    public int getVersion() {
        return version;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the creation time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** True when the situation is closed (final). */
    public boolean isClosed() {
        return SituationLifecycle.CLOSED.equals(status);
    }
}
