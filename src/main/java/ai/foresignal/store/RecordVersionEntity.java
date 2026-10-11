package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * One revision of one upstream record. Append-only.
 *
 * <p>{@code seq} is the spine of the whole system: because a single writer thread allocates it
 * (Architecture §6.1), commit order equals {@code seq} order, which makes it a reliable cursor for
 * SSE resumption, trigger catch-up and {@code /v1/changes}.
 *
 * <p>The entity is {@link Immutable}: Hibernate reads these rows but never writes them. The ingest
 * writer inserts them in JDBC batches instead (Architecture §6.2), so nothing here should be
 * treated as a persistence path for new rows.
 */
@Entity
@Immutable
@Table(
        name = "record_version",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "record_version_source_external_version_key",
                        columnNames = {"source_id", "external_id", "version"}),
        indexes = {
            @Index(name = "record_version_ingested_at_idx", columnList = "ingested_at"),
            @Index(name = "record_version_source_observed_idx", columnList = "source_id, observed_at")
        })
@SequenceGenerator(name = "record_seq", sequenceName = "record_seq", allocationSize = 1)
public class RecordVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_seq")
    @Column(name = "seq", nullable = false)
    private Long seq;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    /** 1-based, per {@code (source_id, external_id)}. */
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until")
    private Instant validUntil;

    /** Always SRID 4326; the writer forces it (Architecture §6.2). */
    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    /** SHA-256 of the canonical form; equal hashes mean an unchanged record. */
    @Column(name = "content_hash", columnDefinition = "char(64)", length = 64, nullable = false)
    private String contentHash;

    /** True when the upstream says this record is gone. */
    @Column(name = "tombstone", nullable = false)
    private boolean tombstone;

    protected RecordVersionEntity() {
        // for JPA
    }

    public Long getSeq() {
        return seq;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public int getVersion() {
        return version;
    }

    public String getKind() {
        return kind;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Geometry getGeom() {
        return geom;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getContentHash() {
        return contentHash;
    }

    public boolean isTombstone() {
        return tombstone;
    }
}
