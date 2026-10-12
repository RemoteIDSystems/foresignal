package ai.foresignal.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The id of a deleted media row (table {@code media_tombstone}, prompt 043), so
 * {@code GET /v1/media/{id}} answers {@code 410 MEDIA_EXPIRED} for a deleted image rather than
 * {@code 404 UNKNOWN_MEDIA}. Kept for {@code foresignal.media.tombstone-retention} (default 30 days).
 *
 * <p>Written and pruned with native SQL by {@link MediaQueries}; the entity gives Hibernate the DDL.
 */
@Entity
@Table(name = "media_tombstone")
public class MediaTombstoneEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;

    /** For JPA. */
    protected MediaTombstoneEntity() {}

    /** The deleted media id. */
    public String getId() {
        return id;
    }

    /** When it was deleted. */
    public Instant getDeletedAt() {
        return deletedAt;
    }
}
