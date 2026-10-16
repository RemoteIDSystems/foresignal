package ai.foresignal.share;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** The share links (prompt 112). */
public interface ShareLinkRepository extends JpaRepository<ShareLinkEntity, String> {

    /** The link with this key hash. */
    Optional<ShareLinkEntity> findByKeyHash(String keyHash);

    /** A page of links, newest first; {@code createdBy} null lists every creator. */
    @Query("SELECT s FROM ShareLinkEntity s WHERE (:createdBy IS NULL OR s.createdBy = :createdBy)"
            + " AND (:cursor IS NULL OR s.id < :cursor) ORDER BY s.id DESC")
    List<ShareLinkEntity> page(@Param("createdBy") String createdBy, @Param("cursor") String cursor, Limit limit);

    /**
     * Counts one use when the link is still usable at {@code now}: not revoked, not expired and below its maximum.
     *
     * @return 1 when the use was counted, 0 when the link is no longer usable
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ShareLinkEntity s SET s.uses = s.uses + 1, s.lastUsedAt = :now WHERE s.id = :id"
            + " AND s.revokedAt IS NULL AND s.expiresAt > :now AND (s.maxUses IS NULL OR s.uses < s.maxUses)")
    int use(@Param("id") String id, @Param("now") Instant now);
}
