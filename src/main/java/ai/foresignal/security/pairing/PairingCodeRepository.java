package ai.foresignal.security.pairing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence of {@link PairingCodeEntity}. */
public interface PairingCodeRepository extends JpaRepository<PairingCodeEntity, UUID> {

    /** The code whose hash is {@code codeHash}. */
    Optional<PairingCodeEntity> findByCodeHash(String codeHash);

    /** Unclaimed codes that have not expired at {@code now}, newest first. */
    @Query("select p from PairingCodeEntity p where p.claimedAt is null and p.expiresAt > :now order by p.createdAt desc")
    List<PairingCodeEntity> findOpen(@Param("now") Instant now);

    /**
     * Claims code {@code id} if it is still unclaimed and unexpired; the single-use guarantee.
     *
     * @return 1 when this call claimed it, 0 otherwise
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PairingCodeEntity p set p.claimedAt = :now where p.id = :id and p.claimedAt is null and p.expiresAt > :now")
    int claim(@Param("id") UUID id, @Param("now") Instant now);
}
