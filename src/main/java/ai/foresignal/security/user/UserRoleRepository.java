package ai.foresignal.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link UserRoleEntity}. */
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.Key> {

    /**
     * The roles a user holds.
     *
     * @param userId the user
     * @return its links
     */
    List<UserRoleEntity> findByUserId(UUID userId);

    /**
     * The links of several users at once.
     *
     * @param userIds the users
     * @return their links
     */
    List<UserRoleEntity> findByUserIdIn(Collection<UUID> userIds);

    /**
     * How many users hold a role.
     *
     * @param roleId the role
     * @return the user count
     */
    long countByRoleId(UUID roleId);

    /**
     * Removes every role from a user.
     *
     * @param userId the user
     */
    void deleteByUserId(UUID userId);
}
