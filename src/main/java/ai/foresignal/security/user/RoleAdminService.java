package ai.foresignal.security.user;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role management (prompt R2-006): list, read, create, update and delete roles, and the permission catalog. Every
 * operation is behind {@code roles.manage} (R2-004).
 *
 * <p><strong>Built-in roles are immutable.</strong> Admin, Operator, Editor, Viewer and Auditor cannot be updated (name,
 * description or permissions) or deleted: {@code 409 BUILTIN_ROLE_PROTECTED}. {@link BuiltInRoleSeeder} re-creates any
 * that goes missing at startup. <strong>A custom role is a named subset of the fixed catalog</strong>: every key must
 * be in {@link Permissions} ({@code 400 VALIDATION_FAILED} naming the unknown ones), and its name may not equal another
 * role's, ignoring case ({@code 409 ROLE_NAME_TAKEN}).
 *
 * <p>A role users still hold cannot be deleted ({@code 409 ROLE_IN_USE}, with {@code userCount}): reassign the users
 * first. A permission change publishes {@link RolePermissionsChangedEvent}, which drops every cached permission set
 * after commit (R2-004), and is refused with {@code 409 LAST_ADMIN} when it would leave no {@code active} user holding
 * {@code users.manage} or {@code roles.manage}; the check runs under the same advisory lock as user management.
 */
@Service
public class RoleAdminService {

    /** Longest role name ({@code role.name}). */
    static final int NAME_LENGTH = 100;

    /** Longest role description ({@code role.description}). */
    static final int DESCRIPTION_LENGTH = 500;

    /** The permissions the instance may never lose every holder of. */
    static final List<String> GUARDED = List.of(Permissions.USERS_MANAGE.key(), Permissions.ROLES_MANAGE.key());

    private final RoleRepository roles;
    private final RolePermissionRepository rolePermissions;
    private final UserRoleRepository userRoles;
    private final UserRepository users;
    private final ApplicationEventPublisher events;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param roles the role store
     * @param rolePermissions role-to-permission links
     * @param userRoles user-to-role links
     * @param users the user store, for the holder guard
     * @param events the event publisher
     * @param jdbc JDBC, for the advisory lock
     * @param clock the clock
     */
    public RoleAdminService(RoleRepository roles, RolePermissionRepository rolePermissions,
            UserRoleRepository userRoles, UserRepository users, ApplicationEventPublisher events, JdbcTemplate jdbc,
            Clock clock) {
        this.roles = roles;
        this.rolePermissions = rolePermissions;
        this.userRoles = userRoles;
        this.users = users;
        this.events = events;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A page of roles ordered by name, each with its permissions and user count ({@code listRoles}). Two queries for
     * the page's permissions and one for every role's user count, whatever the page size.
     *
     * @param cursor the {@code nextCursor} of the previous page, or null
     * @param limit the page size
     * @return the {@code RolePage} view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> list(String cursor, int limit) {
        String after = decodeCursor(cursor);
        List<RoleEntity> all = roles.findAllByOrderByNameAsc();
        List<RoleEntity> remaining = after == null ? all
                : all.stream().filter(r -> r.getName().compareTo(after) > 0).toList();
        boolean more = remaining.size() > limit;
        List<RoleEntity> items = more ? remaining.subList(0, limit) : remaining;
        Map<UUID, List<String>> permissions = permissionsOf(items.stream().map(RoleEntity::getId).toList());
        Map<UUID, Long> counts = userCounts();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items.stream().map(r -> view(r, permissions.getOrDefault(r.getId(), List.of()),
                counts.getOrDefault(r.getId(), 0L))).toList());
        out.put("nextCursor", more ? encodeCursor(items.get(items.size() - 1).getName()) : null);
        return out;
    }

    /**
     * One role ({@code getRole}).
     *
     * @param id the role
     * @return the {@code Role} view
     * @throws NotFoundException {@code 404 UNKNOWN_ROLE}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) {
        return view(find(id));
    }

    /**
     * Creates a custom role ({@code createRole}).
     *
     * @param name the name
     * @param description the description, or null
     * @param permissionKeys the permission keys
     * @return the {@code Role} view
     * @throws ValidationException {@code 400} for a bad name or unknown keys
     * @throws ConflictException {@code 409 ROLE_NAME_TAKEN}
     */
    @Transactional
    public Map<String, Object> create(String name, String description, List<String> permissionKeys) {
        String validName = validName(name);
        String validDescription = validDescription(description);
        Set<String> keys = validKeys(permissionKeys);
        if (roles.findByNameIgnoreCase(validName).isPresent()) {
            throw nameTaken();
        }
        RoleEntity role = roles.saveAndFlush(new RoleEntity(UUID.randomUUID(), validName, validDescription, false,
                clock.instant()));
        for (String key : keys) {
            rolePermissions.save(new RolePermissionEntity(role.getId(), key));
        }
        rolePermissions.flush();
        return view(role, List.copyOf(keys), 0L);
    }

    /**
     * Replaces a custom role's name, description and permissions ({@code updateRole}).
     *
     * @param id the role
     * @param name the new name
     * @param description the new description, or null
     * @param permissionKeys the complete new permission set
     * @return the {@code Role} view
     * @throws NotFoundException {@code 404 UNKNOWN_ROLE}
     * @throws ConflictException {@code 409 BUILTIN_ROLE_PROTECTED}, {@code 409 ROLE_NAME_TAKEN} or
     *     {@code 409 LAST_ADMIN}
     */
    @Transactional
    public Map<String, Object> update(UUID id, String name, String description, List<String> permissionKeys) {
        String validName = validName(name);
        String validDescription = validDescription(description);
        Set<String> keys = validKeys(permissionKeys);
        lock();
        RoleEntity role = find(id);
        protectBuiltIn(role);
        roles.findByNameIgnoreCase(validName).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
            throw nameTaken();
        });
        Instant now = clock.instant();
        role.setName(validName, now);
        role.setDescription(validDescription, now);
        Set<String> before = new LinkedHashSet<>(rolePermissions.findByRoleId(id).stream()
                .map(RolePermissionEntity::getPermissionKey).toList());
        if (!before.equals(keys)) {
            Map<String, Long> holdersBefore = guardedHolders();
            rolePermissions.deleteByRoleId(id);
            rolePermissions.flush();
            for (String key : keys) {
                rolePermissions.save(new RolePermissionEntity(id, key));
            }
            rolePermissions.flush();
            for (String guarded : GUARDED) {
                if (holdersBefore.get(guarded) > 0 && users.countActiveHolders(guarded) == 0) {
                    throw new ConflictException(ErrorCodes.LAST_ADMIN, "This change would leave no active user"
                            + " with " + guarded + "; give another user that permission first.");
                }
            }
            role.touch(now);
            events.publishEvent(new RolePermissionsChangedEvent(id));
        }
        roles.saveAndFlush(role);
        return view(role);
    }

    /**
     * Deletes a custom role nobody holds ({@code deleteRole}).
     *
     * @param id the role
     * @throws NotFoundException {@code 404 UNKNOWN_ROLE}
     * @throws ConflictException {@code 409 BUILTIN_ROLE_PROTECTED}, or {@code 409 ROLE_IN_USE} with
     *     {@code userCount}
     */
    @Transactional
    public void delete(UUID id) {
        lock();
        RoleEntity role = find(id);
        protectBuiltIn(role);
        long holders = userRoles.countByRoleId(id);
        if (holders > 0) {
            throw new ConflictException(ErrorCodes.ROLE_IN_USE, holders + " user(s) hold this role; give them"
                    + " other roles first.").withProperty("userCount", holders);
        }
        roles.delete(role);
        roles.flush();
        events.publishEvent(new RolePermissionsChangedEvent(id));
    }

    /**
     * The permission catalog ({@code listPermissions}), grouped identity, platform, self, data, in catalog order
     * within a group. Straight from {@link Permissions}: the catalog is fixed data, seeded by migration and repaired by
     * {@link BuiltInRoleSeeder}, never created at runtime.
     *
     * @return the {@code PermissionList} view
     */
    public Map<String, Object> permissions() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (PermissionGroup group : PermissionGroup.values()) {
            for (Permissions permission : Permissions.inGroup(group)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("key", permission.key());
                item.put("group", group.wireValue());
                item.put("description", permission.description());
                items.add(item);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        return out;
    }

    private Map<String, Object> view(RoleEntity role) {
        List<String> keys = permissionsOf(List.of(role.getId())).getOrDefault(role.getId(), List.of());
        return view(role, keys, userRoles.countByRoleId(role.getId()));
    }

    /**
     * The {@code Role} view.
     *
     * @param role the role
     * @param permissionKeys its permission keys
     * @param userCount how many users hold it
     * @return the view
     */
    static Map<String, Object> view(RoleEntity role, List<String> permissionKeys, long userCount) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", role.getId().toString());
        out.put("name", role.getName());
        out.put("description", role.getDescription());
        out.put("builtIn", role.isBuiltIn());
        out.put("permissions", permissionKeys.stream().sorted().toList());
        out.put("userCount", userCount);
        out.put("createdAt", role.getCreatedAt());
        out.put("updatedAt", role.getUpdatedAt());
        return out;
    }

    private Map<UUID, List<String>> permissionsOf(List<UUID> roleIds) {
        Map<UUID, List<String>> out = new HashMap<>();
        if (roleIds.isEmpty()) {
            return out;
        }
        for (RolePermissionEntity link : rolePermissions.findByRoleIdIn(roleIds)) {
            out.computeIfAbsent(link.getRoleId(), k -> new ArrayList<>()).add(link.getPermissionKey());
        }
        return out;
    }

    private Map<UUID, Long> userCounts() {
        Map<UUID, Long> out = new HashMap<>();
        for (Object[] row : userRoles.countUsersByRole()) {
            out.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return out;
    }

    private Map<String, Long> guardedHolders() {
        Map<String, Long> out = new HashMap<>();
        for (String guarded : GUARDED) {
            out.put(guarded, users.countActiveHolders(guarded));
        }
        return out;
    }

    private void lock() {
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class,
                UserAdminService.LOCK_KEY);
    }

    private RoleEntity find(UUID id) {
        return roles.findById(id).orElseThrow(RoleAdminService::unknownRole);
    }

    private static void protectBuiltIn(RoleEntity role) {
        if (role.isBuiltIn()) {
            throw new ConflictException(ErrorCodes.BUILTIN_ROLE_PROTECTED, "The built-in role " + role.getName()
                    + " cannot be changed or deleted; create a custom role instead.");
        }
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) {
            throw ValidationException.forField("name", "is required");
        }
        String stripped = name.strip();
        if (stripped.length() > NAME_LENGTH) {
            throw ValidationException.forField("name", "is longer than " + NAME_LENGTH + " characters");
        }
        return stripped;
    }

    private static String validDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String stripped = description.strip();
        if (stripped.length() > DESCRIPTION_LENGTH) {
            throw ValidationException.forField("description", "is longer than " + DESCRIPTION_LENGTH
                    + " characters");
        }
        return stripped;
    }

    /**
     * The requested keys, deduplicated, each checked against the catalog.
     *
     * @param permissionKeys the keys
     * @return the keys in request order
     * @throws ValidationException {@code 400 VALIDATION_FAILED} naming every unknown key
     */
    static Set<String> validKeys(List<String> permissionKeys) {
        if (permissionKeys == null) {
            throw ValidationException.forField("permissions", "is required");
        }
        Set<String> keys = new LinkedHashSet<>(permissionKeys);
        List<String> unknown = keys.stream().filter(k -> Permissions.byKey(k).isEmpty()).toList();
        if (!unknown.isEmpty()) {
            throw new ValidationException("Unknown permission keys: " + String.join(", ", unknown) + ".",
                    unknown.stream().map(k -> new FieldError("permissions", "unknown permission " + k)).toList());
        }
        return keys;
    }

    private static String encodeCursor(String name) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8));
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

    private static ConflictException nameTaken() {
        return new ConflictException(ErrorCodes.ROLE_NAME_TAKEN, "Another role already has that name.");
    }

    /**
     * The {@code 404 UNKNOWN_ROLE} answer.
     *
     * @return the exception
     */
    public static NotFoundException unknownRole() {
        return new NotFoundException(ErrorCodes.UNKNOWN_ROLE, "No such role.");
    }
}
