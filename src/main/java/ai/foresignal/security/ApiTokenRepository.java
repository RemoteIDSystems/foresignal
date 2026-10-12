package ai.foresignal.security;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence of {@link ApiTokenEntity}. */
public interface ApiTokenRepository extends JpaRepository<ApiTokenEntity, UUID> {

    /** The token whose SHA-256 is {@code tokenHash}. */
    Optional<ApiTokenEntity> findByTokenHash(String tokenHash);

    /** The bootstrap admin token, by its {@code created_via}. */
    Optional<ApiTokenEntity> findFirstByCreatedViaOrderByCreatedAtAsc(String createdVia);

    /** The token named {@code name}, oldest first. */
    Optional<ApiTokenEntity> findFirstByNameOrderByCreatedAtAsc(String name);

    /** Every token, newest first. */
    List<ApiTokenEntity> findAllByOrderByCreatedAtDesc();

    /** Stamps {@code last_used_at}; never moves it backwards. */
    @Modifying
    @Query("update ApiTokenEntity t set t.lastUsedAt = :at where t.id = :id and (t.lastUsedAt is null or t.lastUsedAt < :at)")
    int touch(@Param("id") UUID id, @Param("at") Instant at);
}
