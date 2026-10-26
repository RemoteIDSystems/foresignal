package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PermissionResolver}: the union of a user's roles, cached per user and invalidated by events (R2-004). */
class PermissionResolverTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();
    private static final UUID ROLE_A = UUID.randomUUID();
    private static final UUID ROLE_B = UUID.randomUUID();

    private UserRoleRepository userRoles;
    private RolePermissionRepository rolePermissions;
    private PermissionResolver resolver;

    @BeforeEach
    void setUp() {
        userRoles = mock(UserRoleRepository.class);
        rolePermissions = mock(RolePermissionRepository.class);
        resolver = new PermissionResolver(userRoles, rolePermissions);
        when(userRoles.findByUserId(USER)).thenReturn(
                List.of(new UserRoleEntity(USER, ROLE_A), new UserRoleEntity(USER, ROLE_B)));
        when(userRoles.findByUserId(OTHER)).thenReturn(List.of(new UserRoleEntity(OTHER, ROLE_A)));
        when(rolePermissions.findByRoleIdIn(any())).thenAnswer(invocation -> {
            List<UUID> roles = List.copyOf(invocation.getArgument(0));
            List<RolePermissionEntity> rows = new java.util.ArrayList<>();
            if (roles.contains(ROLE_A)) {
                rows.add(new RolePermissionEntity(ROLE_A, "data.read"));
            }
            if (roles.contains(ROLE_B)) {
                rows.add(new RolePermissionEntity(ROLE_B, "audit.read"));
                rows.add(new RolePermissionEntity(ROLE_B, "data.read"));
            }
            return rows;
        });
    }

    @Test
    @DisplayName("the permissions are the union across the user's roles")
    void union() {
        assertThat(resolver.forUser(USER).keys()).containsExactlyInAnyOrder("data.read", "audit.read");
    }

    @Test
    @DisplayName("a user without roles has none")
    void noRoles() {
        UUID nobody = UUID.randomUUID();
        when(userRoles.findByUserId(nobody)).thenReturn(List.of());
        assertThat(resolver.forUser(nobody)).isEqualTo(EffectivePermissions.NONE);
        verify(rolePermissions, never()).findByRoleIdIn(any());
    }

    @Test
    @DisplayName("a second lookup is served from the cache")
    void cached() {
        resolver.forUser(USER);
        resolver.forUser(USER);
        verify(userRoles, times(1)).findByUserId(USER);
    }

    @Test
    @DisplayName("UserRolesChangedEvent drops only that user")
    void userEventInvalidatesOne() {
        resolver.forUser(USER);
        resolver.forUser(OTHER);
        resolver.onUserRolesChanged(new UserRolesChangedEvent(USER));
        resolver.forUser(USER);
        resolver.forUser(OTHER);
        verify(userRoles, times(2)).findByUserId(USER);
        verify(userRoles, times(1)).findByUserId(OTHER);
    }

    @Test
    @DisplayName("RolePermissionsChangedEvent drops every user")
    void roleEventInvalidatesAll() {
        resolver.forUser(USER);
        resolver.forUser(OTHER);
        resolver.onRolePermissionsChanged(new RolePermissionsChangedEvent(ROLE_A));
        resolver.forUser(USER);
        resolver.forUser(OTHER);
        verify(userRoles, times(2)).findByUserId(USER);
        verify(userRoles, times(2)).findByUserId(OTHER);
    }

    @Test
    @DisplayName("revoking a user's sessions drops the user's entry")
    void sessionsRevokedInvalidates() {
        resolver.forUser(USER);
        resolver.onSessionsRevoked(new UserSessionsRevokedEvent(USER, UserSessionsRevokedEvent.Reason.SUSPENDED));
        resolver.forUser(USER);
        verify(userRoles, times(2)).findByUserId(USER);
    }

    @Test
    @DisplayName("invalidateAll empties the cache")
    void invalidateAll() {
        resolver.forUser(USER);
        resolver.invalidateAll();
        resolver.forUser(USER);
        verify(userRoles, times(2)).findByUserId(USER);
    }
}
