package ai.foresignal.security.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * How many {@code active} users other than {@code excluded} hold {@code users.manage} through any role (R2-005,
     * the last-admin guard). Read from the tables, never from the permission cache.
     *
     * @param excluded the user to leave out of the count
     * @return the count
     */
    @Query(value = """
            select count(distinct u.id) from "user" u
            join user_role ur on ur.user_id = u.id
            join role_permission rp on rp.role_id = ur.role_id
            where u.status = 'active' and rp.permission_key = 'users.manage' and u.id <> :excluded""",
            nativeQuery = true)
    long countActiveUserManagersExcept(@Param("excluded") UUID excluded);

    /**
     * How many {@code active} users hold a permission through any role (R2-006: a role edit may not leave the instance
     * without a holder of {@code users.manage} or {@code roles.manage}).
     *
     * @param permission the permission key
     * @return the count
     */
    @Query(value = """
            select count(distinct u.id) from "user" u
            join user_role ur on ur.user_id = u.id
            join role_permission rp on rp.role_id = ur.role_id
            where u.status = 'active' and rp.permission_key = :permission""",
            nativeQuery = true)
    long countActiveHolders(@Param("permission") String permission);

    /**
     * The oldest {@code active} users other than {@code excluded} holding {@code users.manage}, oldest first (R2-005,
     * the default target of a reassigning delete).
     *
     * @param excluded the user to leave out
     * @return their ids
     */
    @Query(value = """
            select u.id from "user" u
            where u.status = 'active' and u.id <> :excluded and exists (
                select 1 from user_role ur join role_permission rp on rp.role_id = ur.role_id
                where ur.user_id = u.id and rp.permission_key = 'users.manage')
            order by u.created_at, u.id""",
            nativeQuery = true)
    List<UUID> findActiveUserManagersExcept(@Param("excluded") UUID excluded);
}
