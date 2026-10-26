package ai.foresignal.security.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link UserTokenEntity}. */
public interface UserTokenRepository extends JpaRepository<UserTokenEntity, UUID> {

    /**
     * The token with a hash.
     *
     * @param tokenHash the SHA-256 hex of a presented token
     * @return the token, or empty
     */
    Optional<UserTokenEntity> findByTokenHash(String tokenHash);

    /**
     * A user's tokens of one kind.
     *
     * @param userId the user
     * @param kind the stored kind ({@link UserTokenKind#wireValue()})
     * @return the tokens
     */
    List<UserTokenEntity> findByUserIdAndKind(UUID userId, String kind);
}
