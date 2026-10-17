package ai.foresignal.situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * One pinned member of a situation (prompt 118): an intelligence event, finding, alert, anomaly, record or
 * investigation, with the reason it joined ({@code join_reason}: the linker's score and components, or the
 * operator or agent action) and its 116 corroboration status.
 *
 * <p>{@code ref} is the member's id, or {@code sourceId/externalId} for records and anomalies; {@code seq} and
 * {@code version} pin the record version the member was linked from (null for alerts and investigations).
 * Unique per {@code (situation_id, member_type, ref, seq)}. {@code geom}, {@code region_codes},
 * {@code entity_ids}, {@code title} and {@code severity} are what the member carried when it was linked, so the
 * situation can be re-derived without reading the member again.
 */
@Entity
@Table(name = "situation_member",
        uniqueConstraints = @UniqueConstraint(name = "situation_member_pin_key",
                columnNames = {"situation_id", "member_type", "ref", "seq"}),
        indexes = {@Index(name = "situation_member_situation_idx", columnList = "situation_id, observed_at"),
                @Index(name = "situation_member_ref_idx", columnList = "member_type, ref")})
public class SituationMemberEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "situation_id", length = 26, nullable = false)
    private String situationId;

    @Column(name = "member_type", length = 16, nullable = false)
    private String memberType;

    @Column(name = "ref", length = 600, nullable = false)
    private String ref;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "version")
    private Integer version;

    @Column(name = "kind", length = 64)
    private String kind;

    @Column(name = "signal_family", length = 64)
    private String signalFamily;

    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "severity", length = 16)
    private String severity;

    @Column(name = "theme", length = 32)
    private String theme;

    @Column(name = "observed_at")
    private Instant observedAt;

    @Column(name = "role", length = 16, nullable = false)
    private String role = "core";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "join_reason")
    private Map<String, Object> joinReason;

    @Column(name = "corroboration", length = 16, nullable = false)
    private String corroboration = "unknown";

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "entity_ids", columnDefinition = "text[]")
    private String[] entityIds = new String[0];

    /** The records ({@code sourceId/externalId}) whose evidence groups the member rests on (116 roll-up). */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "evidence_refs", columnDefinition = "text[]")
    private String[] evidenceRefs = new String[0];

    /** The source of the referenced object, when it has one (private-source rule, 039). */
    @Column(name = "source_id", length = 128)
    private String sourceId;

    /** True when the member comes from a private source or is a private object (039, EGR-1). */
    @Column(name = "private_member", nullable = false, columnDefinition = "boolean default false")
    private boolean privateMember;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "added_by", length = 200, nullable = false)
    private String addedBy;

    /** JPA. */
    protected SituationMemberEntity() {}

    /**
     * Creates a member.
     *
     * @param id the ULID
     * @param situationId the situation
     * @param memberType the member type ({@link SituationMemberTypes})
     * @param ref the member id, or {@code sourceId/externalId}
     * @param seq the pinned seq, or null
     */
    public SituationMemberEntity(String id, String situationId, String memberType, String ref, Long seq) {
        this.id = id;
        this.situationId = situationId;
        this.memberType = memberType;
        this.ref = ref;
        this.seq = seq;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The situation. */
    public String getSituationId() {
        return situationId;
    }

    /** Moves the member to another situation. */
    public void setSituationId(String situationId) {
        this.situationId = situationId;
    }

    /** The member type. */
    public String getMemberType() {
        return memberType;
    }

    /** The member id, or {@code sourceId/externalId}. */
    public String getRef() {
        return ref;
    }

    /** The pinned seq, or null. */
    public Long getSeq() {
        return seq;
    }

    /** The pinned version, or null. */
    public Integer getVersion() {
        return version;
    }

    /** Sets the pinned version. */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /** The record kind or member kind. */
    public String getKind() {
        return kind;
    }

    /** Sets the kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** The signal family. */
    public String getSignalFamily() {
        return signalFamily;
    }

    /** Sets the signal family. */
    public void setSignalFamily(String signalFamily) {
        this.signalFamily = signalFamily;
    }

    /** The display title of the referenced object. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** The severity the referenced object reported (info, warning or critical). */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** The theme the theme table gave the member, or null. */
    public String getTheme() {
        return theme;
    }

    /** Sets the theme. */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /** When the member was observed. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** Sets the observation time. */
    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    /** core or context. */
    public String getRole() {
        return role;
    }

    /** Sets the role. */
    public void setRole(String role) {
        this.role = role;
    }

    /** True for a core member (drives status and severity). */
    public boolean isCore() {
        return "core".equals(role);
    }

    /** Why the member joined. */
    public Map<String, Object> getJoinReason() {
        return joinReason;
    }

    /** Sets the join reason. */
    public void setJoinReason(Map<String, Object> joinReason) {
        this.joinReason = joinReason;
    }

    /** corroborated, uncorroborated, unknown or n/a. */
    public String getCorroboration() {
        return corroboration;
    }

    /** Sets the corroboration status. */
    public void setCorroboration(String corroboration) {
        this.corroboration = corroboration;
    }

    /** The member geometry, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the geometry. */
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /** The member's region codes. */
    public java.util.List<String> getRegionCodes() {
        return regionCodes == null ? java.util.List.of() : java.util.List.of(regionCodes);
    }

    /** Sets the region codes. */
    public void setRegionCodes(java.util.Collection<String> codes) {
        this.regionCodes = codes == null ? new String[0] : codes.toArray(String[]::new);
    }

    /** The member's entities. */
    public java.util.List<String> getEntityIds() {
        return entityIds == null ? java.util.List.of() : java.util.List.of(entityIds);
    }

    /** Sets the entities. */
    public void setEntityIds(java.util.Collection<String> ids) {
        this.entityIds = ids == null ? new String[0] : ids.toArray(String[]::new);
    }

    /** The records the member rests on, as {@code sourceId/externalId}. */
    public java.util.List<String> getEvidenceRefs() {
        return evidenceRefs == null ? java.util.List.of() : java.util.List.of(evidenceRefs);
    }

    /** Sets the records the member rests on. */
    public void setEvidenceRefs(java.util.Collection<String> refs) {
        this.evidenceRefs = refs == null ? new String[0] : refs.toArray(String[]::new);
    }

    /** The source of the referenced object, or null. */
    public String getSourceId() {
        return sourceId;
    }

    /** Sets the source. */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /** True when the member is private. */
    public boolean isPrivateMember() {
        return privateMember;
    }

    /** Sets the private flag. */
    public void setPrivateMember(boolean privateMember) {
        this.privateMember = privateMember;
    }

    /** When the member was added. */
    public Instant getAddedAt() {
        return addedAt;
    }

    /** Sets the addition time. */
    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    /** Who added it: a token id, {@code linker:<version>} or {@code analyst:<profile>}. */
    public String getAddedBy() {
        return addedBy;
    }

    /** Sets the actor. */
    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    /** The key a member is unique by within a situation: type, ref and seq. */
    public String pinKey() {
        return memberType + "|" + ref + "|" + seq;
    }

    /** The key a member is linked by across situations: type and ref. */
    public String linkKey() {
        return memberType + "|" + ref;
    }
}
