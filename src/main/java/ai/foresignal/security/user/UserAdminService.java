package ai.foresignal.security.user;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management (prompt R2-005): invite, list, read, update, set roles, suspend, unsuspend, resend an invite, send
 * a password reset and delete. Every operation is behind {@code users.manage} (R2-004); writes are audited by the
 * token filter, and a deletion also records what became of the user's configuration.
 *
 * <p><strong>Last-admin guard.</strong> An "admin" here is an {@code active} user holding {@code users.manage}
 * through any role. Suspending, deleting or re-roling such a user is refused with {@code 409 LAST_ADMIN} when no
 * other one would remain, so the management surface can never lock the instance out of itself (the bootstrap admin
 * token stays the break-glass credential either way). The check and the change run under one transaction-scoped
 * advisory lock, so two admins removing each other at once cannot both pass.
 *
 * <p><strong>Links without email.</strong> An invite or reset link that was not emailed (no sender configured, or
 * {@code sendInvite: false}) is returned to the admin once, as {@link IssuedLink} with its plaintext token; an emailed
 * one is reported without it. The token itself is stored only as a hash (R2-002).
 */
@Service
public class UserAdminService {

    /** The permission whose holders the last-admin guard counts. */
    static final String MANAGER_PERMISSION = Permissions.USERS_MANAGE.key();

    /** The transaction-scoped advisory lock key serializing the last-admin guard ({@code "USERADMN"}). */
    static final long LOCK_KEY = 0x5553455241444D4EL;

    /** Longest full name ({@code user.full_name}). */
    static final int FULL_NAME_LENGTH = 200;

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final UserLifecycleService lifecycle;
    private final UserTokenService userTokens;
    private final UserViews views;
    private final UserDataRestrictionService dataRestrictions;
    private final PrincipalService principals;
    private final AccountMailer mailer;
    private final ObjectProvider<UserOwnedConfigReassigner> reassigners;
    private final ApplicationEventPublisher events;
    private final AuditService audit;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param users the user store
     * @param roles the role store
     * @param userRoles user-to-role links
     * @param rolePermissions role-to-permission links
     * @param lifecycle the status machine
     * @param userTokens activation and reset tokens
     * @param views the {@code User} view
     * @param dataRestrictions the users' data restrictions (R2-013)
     * @param principals principal creation
     * @param mailer the account email sender
     * @param reassigners every kind of user-owned configuration (none until R2-008 registers the first)
     * @param events the event publisher
     * @param audit the audit log
     * @param entityManager the entity manager, for the filtered list
     * @param jdbc JDBC, for the advisory lock
     * @param clock the clock
     */
    public UserAdminService(UserRepository users, RoleRepository roles, UserRoleRepository userRoles,
            RolePermissionRepository rolePermissions, UserLifecycleService lifecycle, UserTokenService userTokens,
            UserViews views, UserDataRestrictionService dataRestrictions, PrincipalService principals,
            AccountMailer mailer,
            ObjectProvider<UserOwnedConfigReassigner> reassigners, ApplicationEventPublisher events, AuditService audit,
            EntityManager entityManager, JdbcTemplate jdbc, Clock clock) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.lifecycle = lifecycle;
        this.userTokens = userTokens;
        this.views = views;
        this.dataRestrictions = dataRestrictions;
        this.principals = principals;
        this.mailer = mailer;
        this.reassigners = reassigners;
        this.events = events;
        this.audit = audit;
        this.entityManager = entityManager;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A one-time link issued for a user: what {@code createUser}, {@code resendUserInvite} and
     * {@code adminResetUserPassword} report.
     *
     * @param kind the token kind
     * @param emailed true when the email left; the token is then withheld
     * @param token the plaintext token when it was not emailed, else null
     * @param tokenPrefix the token's first eight characters
     * @param expiresAt when the link stops working
     */
    public record IssuedLink(UserTokenKind kind, boolean emailed, String token, String tokenPrefix,
            Instant expiresAt) {

        /** Never prints the token. */
        @Override
        public String toString() {
            return "IssuedLink[" + kind + ", emailed=" + emailed + ", " + tokenPrefix + "…]";
        }

        /**
         * The {@code UserLinkIssued} view.
         *
         * @return the view
         */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("kind", kind.wireValue());
            out.put("emailed", emailed);
            out.put("token", token);
            out.put("tokenPrefix", tokenPrefix);
            out.put("expiresAt", expiresAt);
            return out;
        }

        static IssuedLink of(IssuedUserToken issued, boolean emailed) {
            UserTokenEntity row = issued.token();
            return new IssuedLink(row.getKind(), emailed, emailed ? null : issued.plaintext(), row.getTokenPrefix(),
                    row.getExpiresAt());
        }
    }

    /**
     * A created user and its activation link.
     *
     * @param user the {@code User} view
     * @param activation the link
     */
    public record Created(Map<String, Object> user, IssuedLink activation) {}

    /**
     * What {@code deleteUser} did.
     *
     * @param deletedUserId the user
     * @param reassigned true when the configuration was reassigned, false when deleted
     * @param reassignedTo the admin who received it, or null (deleted, or no admin account exists)
     * @param counts rows changed per kind of configuration
     */
    public record Deletion(UUID deletedUserId, boolean reassigned, UUID reassignedTo, Map<String, Integer> counts) {

        /**
         * The {@code UserDeleteResult} view.
         *
         * @return the view
         */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("deletedUserId", deletedUserId.toString());
            out.put("ownedConfigDisposition", reassigned ? "reassigned" : "deleted");
            out.put("reassignedTo", reassignedTo == null ? null : reassignedTo.toString());
            return out;
        }
    }

    /**
     * A page of users ordered by email ({@code listUsers}).
     *
     * @param status a status to keep, or null
     * @param q text matched against the email and full name (case-insensitive), or null
     * @param cursor the {@code nextCursor} of the previous page, or null
     * @param limit the page size
     * @return the {@code UserPage} view
     * @throws ValidationException {@code 400} for an unknown status or a foreign cursor
     */
    @Transactional(readOnly = true)
    public Map<String, Object> list(String status, String q, String cursor, int limit) {
        UserStatus wanted = null;
        if (status != null && !status.isBlank()) {
            try {
                wanted = UserStatus.fromWire(status);
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("status", "must be invited, active, suspended or disabled");
            }
        }
        String after = decodeCursor(cursor);
        StringBuilder jpql = new StringBuilder("select u from UserEntity u where 1 = 1");
        if (wanted != null) {
            jpql.append(" and u.status = :status");
        }
        String like = q == null || q.isBlank() ? null : "%" + escapeLike(q.strip().toLowerCase(Locale.ROOT)) + "%";
        if (like != null) {
            jpql.append(" and (lower(u.email) like :q escape '\\' or lower(u.fullName) like :q escape '\\')");
        }
        if (after != null) {
            jpql.append(" and u.email > :after");
        }
        jpql.append(" order by u.email");
        TypedQuery<UserEntity> query = entityManager.createQuery(jpql.toString(), UserEntity.class);
        if (wanted != null) {
            query.setParameter("status", wanted.wireValue());
        }
        if (like != null) {
            query.setParameter("q", like);
        }
        if (after != null) {
            query.setParameter("after", after);
        }
        List<UserEntity> page = query.setMaxResults(limit + 1).getResultList();
        boolean more = page.size() > limit;
        List<UserEntity> items = more ? page.subList(0, limit) : page;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", views.users(items));
        out.put("nextCursor", more ? encodeCursor(items.get(items.size() - 1).getEmail()) : null);
        return out;
    }

    /**
     * One user ({@code getUser}).
     *
     * @param id the user
     * @return the {@code User} view
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) {
        return views.user(find(id));
    }

    /**
     * Invites a user ({@code createUser}): {@code invited}, no password, the given roles, an activation token, and the
     * welcome email unless {@code sendInvite} is false.
     *
     * @param email the email; normalized
     * @param fullName the full name
     * @param roleIds the roles to assign; each must exist
     * @param sendInvite false to skip the email and hand the link to the admin
     * @param actor the acting admin's user id, or null for an API token
     * @return the user and its activation link
     * @throws ValidationException {@code 400} for a bad email, name or role
     * @throws ConflictException {@code 409 EMAIL_TAKEN}
     */
    @Transactional
    public Created create(String email, String fullName, List<UUID> roleIds, boolean sendInvite, UUID actor) {
        String normalized = validEmail(email, "email");
        String name = validName(fullName);
        List<RoleEntity> assigned = existingRoles(roleIds);
        if (users.existsByEmail(normalized)) {
            throw emailTaken();
        }
        Instant now = clock.instant();
        UserEntity user = new UserEntity(UUID.randomUUID(), normalized, name,
                principals.createFor(normalized, PrincipalKind.PERSON).getId(), actor, now);
        users.saveAndFlush(user);
        for (RoleEntity role : assigned) {
            userRoles.save(new UserRoleEntity(user.getId(), role.getId()));
        }
        userRoles.flush();
        IssuedUserToken issued = userTokens.issueActivation(user, actor, null);
        boolean emailed = sendInvite && mailer.sendWelcome(user, names(assigned), issued);
        return new Created(UserViews.view(user, assigned), IssuedLink.of(issued, emailed));
    }

    /**
     * Updates a user's full name and email ({@code updateUser}); a null field is left as it is. Never touches the
     * password, roles or status.
     *
     * @param id the user
     * @param fullName the new full name, or null
     * @param email the new email, or null; normalized and checked for uniqueness
     * @return the {@code User} view
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     * @throws ConflictException {@code 409 EMAIL_TAKEN}
     */
    @Transactional
    public Map<String, Object> update(UUID id, String fullName, String email) {
        return update(id, fullName, email, false, null);
    }

    /**
     * {@link #update(UUID, String, String)}, also replacing the user's data restriction (prompt R2-013) when
     * {@code setRestriction} is true, in the same transaction.
     *
     * @param id the user
     * @param fullName the new full name, or null
     * @param email the new email, or null
     * @param setRestriction true when the request carried {@code dataRestriction}
     * @param restriction the validated restriction, or null to lift it
     * @return the {@code User} view
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     * @throws ConflictException {@code 409 EMAIL_TAKEN}
     */
    @Transactional
    public Map<String, Object> update(UUID id, String fullName, String email, boolean setRestriction,
            TokenRestrictions restriction) {
        UserEntity user = find(id);
        Instant now = clock.instant();
        if (fullName != null) {
            user.setFullName(validName(fullName), now);
        }
        if (email != null) {
            String normalized = validEmail(email, "email");
            if (!normalized.equals(user.getEmail())) {
                if (users.existsByEmail(normalized)) {
                    throw emailTaken();
                }
                user.setEmail(normalized, now);
            }
        }
        users.saveAndFlush(user);
        if (setRestriction) {
            dataRestrictions.set(id, restriction);
        }
        return views.user(user);
    }

    /**
     * Replaces a user's roles ({@code setUserRoles}), drops the user's cached permissions after commit, and sends the
     * role-changed email when the set actually changed.
     *
     * @param id the user
     * @param roleIds the complete new set; each must exist
     * @return the {@code User} view
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     * @throws ConflictException {@code 409 LAST_ADMIN} when the user is the last admin and the new set lacks
     *     {@code users.manage}
     */
    @Transactional
    public Map<String, Object> setRoles(UUID id, List<UUID> roleIds) {
        List<RoleEntity> assigned = existingRoles(roleIds);
        lock();
        UserEntity user = find(id);
        Set<UUID> before = new LinkedHashSet<>(userRoles.findByUserId(id).stream().map(UserRoleEntity::getRoleId)
                .toList());
        Set<UUID> after = new LinkedHashSet<>(assigned.stream().map(RoleEntity::getId).toList());
        guardLastAdmin(user, grantsManager(after));
        if (!before.equals(after)) {
            userRoles.deleteByUserId(id);
            userRoles.flush();
            for (UUID role : after) {
                userRoles.save(new UserRoleEntity(id, role));
            }
            userRoles.flush();
            user.touch(clock.instant());
            users.save(user);
            events.publishEvent(new UserRolesChangedEvent(id));
            mailer.sendRoleChanged(user, names(assigned));
        }
        return UserViews.view(user, assigned);
    }

    /**
     * Suspends an active user ({@code suspendUser}): every session ends; the account and its configuration stay.
     *
     * @param id the user
     * @return the {@code User} view
     * @throws ConflictException {@code 409 LAST_ADMIN}, or {@code 409 USER_STATUS_CONFLICT} unless active
     */
    @Transactional
    public Map<String, Object> suspend(UUID id) {
        lock();
        UserEntity user = find(id);
        guardLastAdmin(user, false);
        lifecycle.suspend(user);
        mailer.sendSuspended(user);
        return views.user(user);
    }

    /**
     * Restores a suspended user ({@code unsuspendUser}).
     *
     * @param id the user
     * @return the {@code User} view
     * @throws ConflictException {@code 409 USER_STATUS_CONFLICT} unless suspended
     */
    @Transactional
    public Map<String, Object> unsuspend(UUID id) {
        UserEntity user = find(id);
        lifecycle.unsuspend(user);
        return views.user(user);
    }

    /**
     * Issues a fresh activation token for an invited user, invalidating the earlier one, and sends the welcome email
     * again ({@code resendUserInvite}).
     *
     * @param id the user
     * @param actor the acting admin's user id, or null
     * @return the link
     * @throws ConflictException {@code 409 ALREADY_ACTIVE} unless the user is invited
     */
    @Transactional
    public IssuedLink resendInvite(UUID id, UUID actor) {
        UserEntity user = find(id);
        if (user.getStatus() != UserStatus.INVITED) {
            throw new ConflictException(ErrorCodes.ALREADY_ACTIVE,
                    "The account is " + user.getStatus().wireValue() + ", not invited; there is no invite to resend.");
        }
        IssuedUserToken issued = userTokens.issueActivation(user, actor, null);
        List<String> roleNames = names(rolesOf(id));
        return IssuedLink.of(issued, mailer.sendWelcome(user, roleNames, issued));
    }

    /**
     * Sends a user a password reset ({@code adminResetUserPassword}): a reset token replaces any earlier one, every
     * session of the user ends, and the email goes out. The password itself is neither shown nor set.
     *
     * @param id the user
     * @return the link
     */
    @Transactional
    public IssuedLink resetPassword(UUID id) {
        UserEntity user = find(id);
        IssuedUserToken issued = userTokens.issueReset(user, null);
        events.publishEvent(new UserSessionsRevokedEvent(id, UserSessionsRevokedEvent.Reason.PASSWORD_RESET_ISSUED));
        return IssuedLink.of(issued, mailer.sendPasswordReset(user, issued));
    }

    /**
     * Deletes a user ({@code deleteUser}). By default the user's configuration is first reassigned, through every
     * {@link UserOwnedConfigReassigner}, to: the acting admin when the caller is a signed-in admin other than the
     * user; otherwise the oldest other active admin; otherwise nobody (instance-owned). With {@code reassign} false it
     * is deleted instead. The user's role links, sessions and activation/reset tokens go with the row.
     *
     * @param id the user
     * @param reassign true to reassign (the default), false to delete the configuration
     * @param actor the acting admin's user id, or null for an API token
     * @param remoteIp the client address, for the audit
     * @return what happened
     * @throws ConflictException {@code 409 LAST_ADMIN}
     */
    @Transactional
    public Deletion delete(UUID id, boolean reassign, UUID actor, String remoteIp) {
        lock();
        UserEntity user = find(id);
        guardLastAdmin(user, false);
        UUID target = reassign ? reassignTarget(id, actor) : null;
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (UserOwnedConfigReassigner reassigner : reassigners.orderedStream().toList()) {
            counts.put(reassigner.name(), reassign ? reassigner.reassign(id, target) : reassigner.delete(id));
        }
        users.delete(user);
        users.flush();
        events.publishEvent(new UserRolesChangedEvent(id));
        Deletion deletion = new Deletion(id, reassign, target, counts);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("event", "user_deleted");
        detail.put("email", user.getEmail());
        detail.put("ownedConfigDisposition", reassign ? "reassigned" : "deleted");
        detail.put("reassignedTo", target == null ? null : target.toString());
        detail.put("counts", counts);
        AuditRecord entry = new AuditRecord(clock.instant(), AuditRecord.SYSTEM, null, null, null, null, "DELETE", null,
                "deleteUser", 200, "users", id.toString(), remoteIp, detail);
        // R2-013: a signed-in admin's deletion is attributed to that admin; an API token's stays a system entry.
        UserEntity acting = actor == null ? null : users.findById(actor).orElse(null);
        audit.record(acting == null ? entry : entry.asUser(acting.getId(), acting.getEmail()));
        return deletion;
    }

    /**
     * True when the user is an admin for the last-admin guard: {@code active} and holding {@code users.manage}.
     *
     * @param user the user
     * @return true when an admin
     */
    boolean isActiveManager(UserEntity user) {
        return user.getStatus() == UserStatus.ACTIVE
                && grantsManager(userRoles.findByUserId(user.getId()).stream().map(UserRoleEntity::getRoleId).toList());
    }

    private void guardLastAdmin(UserEntity user, boolean remainsManager) {
        if (remainsManager || !isActiveManager(user)) {
            return;
        }
        if (users.countActiveUserManagersExcept(user.getId()) == 0) {
            throw new ConflictException(ErrorCodes.LAST_ADMIN, "This is the last active user who can manage users;"
                    + " give another user that permission first.");
        }
    }

    private UUID reassignTarget(UUID deleted, UUID actor) {
        if (actor != null && !actor.equals(deleted)) {
            UserEntity acting = users.findById(actor).orElse(null);
            if (acting != null && isActiveManager(acting)) {
                return actor;
            }
        }
        return users.findActiveUserManagersExcept(deleted).stream().findFirst().orElse(null);
    }

    private boolean grantsManager(Collection<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return false;
        }
        return rolePermissions.findByRoleIdIn(roleIds).stream()
                .anyMatch(rp -> MANAGER_PERMISSION.equals(rp.getPermissionKey()));
    }

    private void lock() {
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class, LOCK_KEY);
    }

    private UserEntity find(UUID id) {
        return users.findById(id).orElseThrow(() -> unknownUser());
    }

    private List<RoleEntity> rolesOf(UUID userId) {
        return roles.findAllById(userRoles.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList());
    }

    private List<RoleEntity> existingRoles(List<UUID> roleIds) {
        if (roleIds == null) {
            throw ValidationException.forField("roles", "is required");
        }
        Set<UUID> wanted = new LinkedHashSet<>(roleIds);
        List<RoleEntity> found = roles.findAllById(wanted);
        if (found.size() != wanted.size()) {
            Set<UUID> missing = new LinkedHashSet<>(wanted);
            found.forEach(r -> missing.remove(r.getId()));
            throw ValidationException.forField("roles", "names roles that do not exist: " + missing);
        }
        List<RoleEntity> sorted = new ArrayList<>(found);
        sorted.sort(Comparator.comparing(RoleEntity::getName));
        return sorted;
    }

    private static List<String> names(List<RoleEntity> list) {
        return list.stream().map(RoleEntity::getName).sorted().toList();
    }

    /**
     * Normalizes and checks an email address (RFC 822 syntax through {@link InternetAddress}, at most
     * {@link UserEntity#EMAIL_LENGTH} characters).
     *
     * @param email the address as given
     * @param field the request field, for the error
     * @return the normalized address
     * @throws ValidationException {@code 400} when missing or malformed
     */
    static String validEmail(String email, String field) {
        if (email == null || email.isBlank()) {
            throw ValidationException.forField(field, "is required");
        }
        String normalized = UserEntity.normalizeEmail(email);
        if (normalized.length() > UserEntity.EMAIL_LENGTH || normalized.indexOf('@') <= 0) {
            throw ValidationException.forField(field, "is not an email address");
        }
        try {
            InternetAddress parsed = new InternetAddress(normalized, true);
            if (!normalized.equals(parsed.getAddress())) {
                throw ValidationException.forField(field, "is not an email address");
            }
        } catch (AddressException e) {
            throw ValidationException.forField(field, "is not an email address");
        }
        return normalized;
    }

    private static String validName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw ValidationException.forField("fullName", "is required");
        }
        String name = fullName.strip();
        if (name.length() > FULL_NAME_LENGTH) {
            throw ValidationException.forField("fullName", "is longer than " + FULL_NAME_LENGTH + " characters");
        }
        return name;
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static String encodeCursor(String email) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(email.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor from this list");
        }
    }

    private static ConflictException emailTaken() {
        return new ConflictException(ErrorCodes.EMAIL_TAKEN, "Another user already has that email.");
    }

    /**
     * The {@code 404 UNKNOWN_USER} answer.
     *
     * @return the exception
     */
    public static NotFoundException unknownUser() {
        return new NotFoundException(ErrorCodes.UNKNOWN_USER, "No such user.");
    }
}
