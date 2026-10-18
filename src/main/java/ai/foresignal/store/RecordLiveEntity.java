package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * The current, hot state of one upstream record (Core-Data-Model §8).
 *
 * <p>Every observation touches this row, persisted or not: the latest {@code observed_at}, the
 * live geometry, the full hash and the kinematic fields. {@code dirty} says the live state has
 * diverged from the persisted state in {@link RecordHeadEntity}. Exactly one live row exists per
 * head and the reverse (invariant I10). The current payload is
 * {@code jsonb_strip_nulls(head.payload || live.kinematic)}.
 *
 * <p>No index on {@code observed_at}, by design: touches must stay HOT updates. Do not add one.
 * The only secondary index is the GiST index on {@code geom}, and the table carries
 * {@code fillfactor=70} plus aggressive autovacuum settings; both are declared in
 * the Flyway baseline ({@code db/migration/V1__baseline.sql}), since JPA cannot express them.
 */
@Entity
@Table(name = "record_live")
public class RecordLiveEntity {

    @EmbeddedId
    private RecordHeadId id;

    /** The latest accepted observation time. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    /** When the latest observation was received; drives the touch throttle. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_received_at", nullable = false)
    private Instant lastReceivedAt;

    /** The effective expiry of the current state, or null when it does not expire. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until")
    private Instant validUntil;

    /** The live geometry, SRID 4326. */
    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    /** Hash of all non-volatile content of the latest observation (Core-Data-Model §4). */
    @Column(name = "full_hash", nullable = false)
    private long fullHash;

    /** True when the live state differs from the persisted head. */
    @Column(name = "dirty", nullable = false)
    private boolean dirty;

    /** Kinematic fields merged over the head payload on read. Never null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'{}'::jsonb")
    @Column(name = "kinematic", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> kinematic = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality")
    private Map<String, Object> quality;

    /** QA flag bits of the latest observation. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "qa_flags", nullable = false, columnDefinition = "smallint default 0")
    private Short qaFlags = 0;

    protected RecordLiveEntity() {
        // for JPA
    }

    /** Creates a live row for the given record; the caller fills every non-null column. */
    public RecordLiveEntity(RecordHeadId id) {
        this.id = id;
    }

    /** The record's identity. */
    public RecordHeadId getId() {
        return id;
    }

    /** The latest accepted observation time. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** Sets the latest accepted observation time. */
    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    /** When the latest observation was received. */
    public Instant getLastReceivedAt() {
        return lastReceivedAt;
    }

    /** Sets when the latest observation was received. */
    public void setLastReceivedAt(Instant lastReceivedAt) {
        this.lastReceivedAt = lastReceivedAt;
    }

    /** The effective expiry, or null. */
    public Instant getValidUntil() {
        return validUntil;
    }

    /** Sets the effective expiry. */
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    /** The live geometry, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the live geometry. */
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /** The full hash of the latest observation. */
    public long getFullHash() {
        return fullHash;
    }

    /** Sets the full hash. */
    public void setFullHash(long fullHash) {
        this.fullHash = fullHash;
    }

    /** Whether live and persisted state differ. */
    public boolean isDirty() {
        return dirty;
    }

    /** Sets the dirty flag. */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /** The kinematic fields; never null. */
    public Map<String, Object> getKinematic() {
        return kinematic;
    }

    /** Sets the kinematic fields; null is stored as an empty object. */
    public void setKinematic(Map<String, Object> kinematic) {
        this.kinematic = kinematic == null ? new LinkedHashMap<>() : kinematic;
    }

    /** The latest quality object, or null. */
    public Map<String, Object> getQuality() {
        return quality;
    }

    /** Sets the latest quality object. */
    public void setQuality(Map<String, Object> quality) {
        this.quality = quality;
    }

    /** The QA flag bits. */
    public Short getQaFlags() {
        return qaFlags;
    }

    /** Sets the QA flag bits; null is stored as 0. */
    public void setQaFlags(Short qaFlags) {
        this.qaFlags = qaFlags == null ? 0 : qaFlags;
    }
}
