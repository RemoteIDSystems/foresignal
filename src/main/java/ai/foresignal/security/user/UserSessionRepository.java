package ai.foresignal.security.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence of {@link UserSessionEntity}. */
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {

    /**
     * The session with a bearer hash.
     *
     * @param tokenHash the SHA-256 hex of a presented bearer
     * @return the session, or empty
     */
    Optional<UserSessionEntity> findByTokenHash(String tokenHash);

    /**
     * A user's live sessions, oldest first by id after a cursor, for {@code listSessions}.
     *
     * @param userId the user
     * @param now the current time
     * @param after the last id of the previous page, or null for the first page
     * @param page the page size
     * @return the sessions
     */
    @Query("select s from UserSessionEntity s where s.userId = :userId and s.revokedAt is null"
            + " and s.expiresAt > :now and s.idleExpiresAt > :now and (:after is null or s.id > :after)"
            + " order by s.id")
    List<UserSessionEntity> findLive(@Param("userId") UUID userId, @Param("now") Instant now,
            @Param("after") UUID after, Pageable page);

    /**
     * Revokes every unrevoked session of a user, optionally keeping one.
     *
     * @param userId the user
     * @param keep a session to keep, or null to revoke all
     * @param now the revocation time
     * @return how many were revoked
     */
    @Modifying
    @Query("update UserSessionEntity s set s.revokedAt = :now where s.userId = :userId and s.revokedAt is null"
            + " and (:keep is null or s.id <> :keep)")
    int revokeAll(@Param("userId") UUID userId, @Param("keep") UUID keep, @Param("now") Instant now);
}
