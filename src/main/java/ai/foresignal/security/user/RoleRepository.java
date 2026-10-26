package ai.foresignal.security.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link RoleEntity}. */
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    /**
     * The role with a name.
     *
     * @param name the exact name
     * @return the role, or empty
     */
    Optional<RoleEntity> findByName(String name);

    /** Every role, built-in or custom, ordered by name. */
    List<RoleEntity> findAllByOrderByNameAsc();
}
