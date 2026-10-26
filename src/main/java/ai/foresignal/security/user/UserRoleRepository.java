package ai.foresignal.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * How many users hold each role, in one query (R2-006, {@code Role.userCount}). Roles nobody holds are absent.
     *
     * @return rows of (role id, user count)
     */
    @Query("select ur.roleId, count(ur) from UserRoleEntity ur group by ur.roleId")
    List<Object[]> countUsersByRole();
}
