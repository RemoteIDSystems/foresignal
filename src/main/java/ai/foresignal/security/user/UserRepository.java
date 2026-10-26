package ai.foresignal.security.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link UserEntity}. */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * The user with a normalized email.
     *
     * @param email an email already passed through {@link UserEntity#normalizeEmail}
     * @return the user, or empty
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * True when a normalized email is taken.
     *
     * @param email an email already passed through {@link UserEntity#normalizeEmail}
     * @return true when a user has it
     */
    boolean existsByEmail(String email);

    /**
     * The user owning a principal.
     *
     * @param principalId the principal's ULID
     * @return the user, or empty when the principal belongs to no user
     */
    Optional<UserEntity> findByPrincipalId(String principalId);
}
