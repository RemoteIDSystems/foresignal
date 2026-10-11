package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * The current state of one upstream record.
 *
 * <p>Deliberately denormalized (Architecture §7.1): current-state, bbox and tile queries read only
 * this table and join {@code record_version} just for the payload. {@code seq} points at the
 * revision this head reflects.
 */
@Entity
@Table(
        name = "record_head",
        indexes = {
            @Index(name = "record_head_kind_idx", columnList = "kind"),
            @Index(name = "record_head_valid_until_idx", columnList = "valid_until")
        })
public class RecordHeadEntity {

    @EmbeddedId
    private RecordHeadId id;

    /** The {@code record_version.seq} this head reflects. */
    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @Column(name = "content_hash", columnDefinition = "char(64)", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "tombstone", nullable = false)
    private boolean tombstone;

    /** Flattened text from the payload, so queries can filter on words without reading jsonb. */
    @Column(name = "search_text", columnDefinition = "text")
    private String searchText;

    protected RecordHeadEntity() {
        // for JPA
    }

    public RecordHeadEntity(RecordHeadId id) {
        this.id = id;
    }

    public RecordHeadId getId() {
        return id;
    }

    public void setId(RecordHeadId id) {
        this.id = id;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
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

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public boolean isTombstone() {
        return tombstone;
    }

    public void setTombstone(boolean tombstone) {
        this.tombstone = tombstone;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
