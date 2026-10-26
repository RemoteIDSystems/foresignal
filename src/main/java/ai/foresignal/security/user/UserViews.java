package ai.foresignal.security.user;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The {@code User} response shape (OpenAPI {@code User}; R2-003 for {@code getMe}, reused by user management):
 * {@code id}, {@code email}, {@code fullName}, {@code status}, {@code roles} ({@code RoleRef}s, by name),
 * {@code mfaEnabled}, {@code createdBy}, {@code createdAt}, {@code updatedAt} and {@code lastLoginAt}. Never the
 * password hash.
 */
@Service
public class UserViews {

    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final RoleRepository roles;

    /**
     * Creates the view builder.
     *
     * @param users the user store
     * @param userRoles user-to-role links
     * @param roles the role store
     */
    public UserViews(UserRepository users, UserRoleRepository userRoles, RoleRepository roles) {
        this.users = users;
        this.userRoles = userRoles;
        this.roles = roles;
    }

    /**
     * The view of a user by id.
     *
     * @param userId the user
     * @return the view, or null when the user no longer exists
     */
    @Transactional(readOnly = true)
    public Map<String, Object> user(UUID userId) {
        return users.findById(userId).map(this::user).orElse(null);
    }

    /**
     * The view of a user.
     *
     * @param user the user
     * @return the view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> user(UserEntity user) {
        return view(user, rolesOf(List.of(user.getId())).getOrDefault(user.getId(), List.of()));
    }

    /**
     * The views of several users with one role lookup.
     *
     * @param list the users
     * @return their views, in order
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> users(List<UserEntity> list) {
        Map<UUID, List<RoleEntity>> byUser = rolesOf(list.stream().map(UserEntity::getId).toList());
        return list.stream().map(u -> view(u, byUser.getOrDefault(u.getId(), List.of()))).toList();
    }

    /**
     * The view of a user with its roles already known.
     *
     * @param user the user
     * @param held its roles
     * @return the view
     */
    public static Map<String, Object> view(UserEntity user, List<RoleEntity> held) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", user.getId().toString());
        out.put("email", user.getEmail());
        out.put("fullName", user.getFullName());
        out.put("status", user.getStatus().wireValue());
        out.put("roles", held.stream().sorted(Comparator.comparing(RoleEntity::getName)).map(r -> {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("id", r.getId().toString());
            ref.put("name", r.getName());
            return ref;
        }).toList());
        out.put("mfaEnabled", user.isMfaEnabled());
        out.put("createdBy", user.getCreatedBy() == null ? null : user.getCreatedBy().toString());
        out.put("createdAt", user.getCreatedAt());
        out.put("updatedAt", user.getUpdatedAt());
        out.put("lastLoginAt", user.getLastLoginAt());
        return out;
    }

    private Map<UUID, List<RoleEntity>> rolesOf(List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<UserRoleEntity> links = userRoles.findByUserIdIn(userIds);
        Map<UUID, RoleEntity> byId = roles.findAllById(links.stream().map(UserRoleEntity::getRoleId).distinct()
                .toList()).stream().collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        return links.stream().filter(l -> byId.containsKey(l.getRoleId())).collect(Collectors.groupingBy(
                UserRoleEntity::getUserId, Collectors.mapping(l -> byId.get(l.getRoleId()), Collectors.toList())));
    }
}
