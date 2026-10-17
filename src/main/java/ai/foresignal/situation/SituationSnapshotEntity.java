package ai.foresignal.situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The deterministic daily snapshot of one situation (prompt 118), written once per non-closed situation for the
 * previous UTC day by {@link SituationSnapshots}, so "what changed since yesterday or last week" is a diff of
 * stored rows and never needs an LLM. Primary key {@code (situation_id, day)}.
 */
@Entity
@Table(name = "situation_snapshot")
@IdClass(SituationSnapshotEntity.Key.class)
public class SituationSnapshotEntity {

    /**
     * The composite primary key.
     *
     * @param situationId the situation
     * @param day the UTC day
     */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private String situationId;
        private LocalDate day;

        /** JPA. */
        public Key() {}

        /** Creates a key. */
        public Key(String situationId, LocalDate day) {
            this.situationId = situationId;
            this.day = day;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && Objects.equals(situationId, key.situationId)
                    && Objects.equals(day, key.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(situationId, day);
        }
    }

    @Id
    @Column(name = "situation_id", length = 26, nullable = false)
    private String situationId;

    @Id
    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "severity", length = 16, nullable = false)
    private String severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "member_counts", nullable = false)
    private Map<String, Object> memberCounts;

    @Column(name = "new_members", nullable = false)
    private int newMembers;

    @Column(name = "area_km2")
    private Double areaKm2;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_values", nullable = false)
    private Map<String, Object> keyValues = Map.of();

    @Column(name = "asof_seq", nullable = false)
    private long asofSeq;

    /** The analysis manifest of the snapshot (prompt 116), or null. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** JPA. */
    protected SituationSnapshotEntity() {}

    /** Creates a snapshot of {@code situationId} for {@code day}. */
    public SituationSnapshotEntity(String situationId, LocalDate day) {
        this.situationId = situationId;
        this.day = day;
    }

    /** The situation. */
    public String getSituationId() {
        return situationId;
    }

    /** The UTC day. */
    public LocalDate getDay() {
        return day;
    }

    /** The status at the end of the day. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The severity at the end of the day. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** Member counts: {@code {byType: {...}, byFamily: {...}}}. */
    public Map<String, Object> getMemberCounts() {
        return memberCounts;
    }

    /** Sets the member counts. */
    public void setMemberCounts(Map<String, Object> memberCounts) {
        this.memberCounts = memberCounts;
    }

    /** Members added during the day. */
    public int getNewMembers() {
        return newMembers;
    }

    /** Sets the members added during the day. */
    public void setNewMembers(int newMembers) {
        this.newMembers = newMembers;
    }

    /** The area of the situation geometry, km², or null. */
    public Double getAreaKm2() {
        return areaKm2;
    }

    /** Sets the area. */
    public void setAreaKm2(Double areaKm2) {
        this.areaKm2 = areaKm2;
    }

    /** The region codes. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : List.of(regionCodes);
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> codes) {
        this.regionCodes = codes == null ? new String[0] : codes.toArray(String[]::new);
    }

    /** Latest values of linked analysis series (119 onward); empty before. */
    public Map<String, Object> getKeyValues() {
        return keyValues;
    }

    /** Sets the key values. */
    public void setKeyValues(Map<String, Object> keyValues) {
        this.keyValues = keyValues == null ? Map.of() : keyValues;
    }

    /** The published seq the snapshot was taken at. */
    public long getAsofSeq() {
        return asofSeq;
    }

    /** Sets the as-of seq. */
    public void setAsofSeq(long asofSeq) {
        this.asofSeq = asofSeq;
    }

    /** The manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** When the snapshot was written. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the write time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
