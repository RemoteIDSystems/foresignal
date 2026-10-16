package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One pinned piece of evidence of an intelligence event (prompt 101). A member is a specific
 * {@code (source, externalId, seq, version)} of a record, an alert, an anomaly or another event, with
 * the reason it was joined and its signal family. A version is pinned at most once per event (unique
 * {@code (event_id, member_type, source_id, external_id, seq)}).
 */
@Entity
@Table(name = "intelligence_event_member", uniqueConstraints = @UniqueConstraint(
        name = "intelligence_event_member_pin_uq",
        columnNames = {"event_id", "member_type", "source_id", "external_id", "seq"}))
public class IntelEventMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_id", length = 26, nullable = false)
    private String eventId;

    @Column(name = "role", length = 16, nullable = false)
    private String role = "supporting";

    @Column(name = "member_type", length = 16, nullable = false)
    private String memberType;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "version")
    private Integer version;

    @Column(name = "kind", length = 64)
    private String kind;

    @Column(name = "observed_at")
    private Instant observedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "join_reason")
    private Map<String, Object> joinReason;

    @Column(name = "signal_family", length = 64)
    private String signalFamily;

    @Column(name = "weight")
    private Double weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot")
    private Map<String, Object> snapshot;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "added_by", length = 128)
    private String addedBy;

    /** JPA. */
    protected IntelEventMemberEntity() {}

    /** Creates a member pinning one version. */
    public IntelEventMemberEntity(String eventId, String memberType, String sourceId, String externalId, long seq) {
        this.eventId = eventId;
        this.memberType = memberType;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    public Map<String, Object> getJoinReason() {
        return joinReason;
    }

    public void setJoinReason(Map<String, Object> joinReason) {
        this.joinReason = joinReason;
    }

    public String getSignalFamily() {
        return signalFamily;
    }

    public void setSignalFamily(String signalFamily) {
        this.signalFamily = signalFamily;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, Object> snapshot) {
        this.snapshot = snapshot;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
