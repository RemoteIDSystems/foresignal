package ai.foresignal.security.mfa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The recovery codes (prompt R2-024). */
public interface RecoveryCodeRepository extends JpaRepository<RecoveryCodeEntity, UUID> {

    /**
     * An unused code of a user by its hash.
     *
     * @param userId the user
     * @param codeHash the SHA-256 hex of the normalized code
     * @return the code, or empty
     */
    Optional<RecoveryCodeEntity> findByUserIdAndCodeHashAndUsedAtIsNull(UUID userId, String codeHash);

    /**
     * Every code of a user, used or not.
     *
     * @param userId the user
     * @return the codes
     */
    List<RecoveryCodeEntity> findByUserId(UUID userId);

    /**
     * Deletes every code of a user.
     *
     * @param userId the user
     * @return the rows deleted
     */
    @Modifying
    @Query("delete from RecoveryCodeEntity r where r.userId = :userId")
    int deleteByUserId(@Param("userId") UUID userId);
}
