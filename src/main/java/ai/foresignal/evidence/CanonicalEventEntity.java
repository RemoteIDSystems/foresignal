package ai.foresignal.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * A real-world event, distinct from the observations that describe it (prompt 116, table
 * {@code canonical_event}). Written only by the {@code EvidenceResolver}; never deleted by retention (an
 * intelligence table, RET-3).
 */
@Entity
@Table(name = "canonical_event", indexes = {
        @Index(name = "canonical_event_family_time_idx", columnList = "family, event_time"),
        @Index(name = "canonical_event_status_idx", columnList = "status, updated_at")})
public class CanonicalEventEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "band_value")
    private Double bandValue;

    @Column(name = "first_observed_at", nullable = false)
    private Instant firstObservedAt;

    @Column(name = "first_observation_ref", length = 600, nullable = false)
    private String firstObservationRef;

    @Column(name = "observation_count", nullable = false)
    private int observationCount;

    @Column(name = "evidence_group_count", nullable = false)
    private int evidenceGroupCount;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "quality_flags", columnDefinition = "text[]")
    private String[] qualityFlags = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "source_ids", columnDefinition = "text[]")
    private String[] sourceIds = new String[0];

    @Column(name = "status", length = 16, nullable = false)
    private String status = "open";

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @Column(name = "resolver_version", length = 32, nullable = false)
    private String resolverVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected CanonicalEventEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }

    /** The family. */
    public String getFamily() {
        return family;
    }

    /** The event time. */
    public Instant getEventTime() {
        return eventTime;
    }

    /** The location, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** The 040 region codes. */
    public String[] getRegionCodes() {
        return regionCodes == null ? new String[0] : regionCodes;
    }

    /** The value threshold bands are taken from (the magnitude), or null. */
    public Double getBandValue() {
        return bandValue;
    }

    /** When the first observation was made. */
    public Instant getFirstObservedAt() {
        return firstObservedAt;
    }

    /** The first observation's record ref. */
    public String getFirstObservationRef() {
        return firstObservationRef;
    }

    /** The linked observations. */
    public int getObservationCount() {
        return observationCount;
    }

    /** The evidence groups. */
    public int getEvidenceGroupCount() {
        return evidenceGroupCount;
    }

    /** The quality flags. */
    public String[] getQualityFlags() {
        return qualityFlags == null ? new String[0] : qualityFlags;
    }

    /** The distinct source ids of the linked observations. */
    public String[] getSourceIds() {
        return sourceIds == null ? new String[0] : sourceIds;
    }

    /** {@code open}, {@code merged} or {@code closed}. */
    public String getStatus() {
        return status;
    }

    /** The surviving event after a merge, or null. */
    public String getMergedInto() {
        return mergedInto;
    }

    /** The resolver version that last wrote the event. */
    public String getResolverVersion() {
        return resolverVersion;
    }

    /** When the event was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the event last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
