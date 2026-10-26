package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The Java side of the identity model (prompt R2-001): the catalog, the built-in role matrix and the entities. */
class IdentityModelTest {

    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    private static Set<String> keys(Set<Permissions> permissions) {
        return permissions.stream().map(Permissions::key).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("the catalog has exactly the 21 keys of the four groups, each unique")
    void catalogKeys() {
        assertThat(Arrays.stream(Permissions.values()).map(Permissions::key)).doesNotHaveDuplicates().hasSize(21);
        assertThat(keys(Permissions.inGroup(PermissionGroup.IDENTITY))).containsExactlyInAnyOrder("users.manage",
                "roles.manage");
        assertThat(keys(Permissions.inGroup(PermissionGroup.PLATFORM))).containsExactlyInAnyOrder("system.settings",
                "collection.manage", "collector.settings", "llm.shared.manage", "channels.global", "storage.manage",
                "network.manage", "audit.read");
        assertThat(keys(Permissions.inGroup(PermissionGroup.SELF))).containsExactlyInAnyOrder("self.sources",
                "self.alerts", "self.channels", "self.llm", "self.views", "self.preferences", "self.workspaces");
        assertThat(keys(Permissions.inGroup(PermissionGroup.DATA))).containsExactlyInAnyOrder("data.read",
                "data.export", "intel.read", "forecast.read");
        assertThat(Arrays.stream(Permissions.values())).allMatch(p -> !p.description().isBlank());
    }

    @Test
    @DisplayName("byKey finds every catalog key and nothing else")
    void byKey() {
        for (Permissions p : Permissions.values()) {
            assertThat(Permissions.byKey(p.key())).contains(p);
        }
        assertThat(Permissions.byKey("users.delete")).isEmpty();
    }

    @Test
    @DisplayName("group, status and token kind wire values round-trip and reject unknowns")
    void wireValues() {
        for (PermissionGroup g : PermissionGroup.values()) {
            assertThat(PermissionGroup.fromWire(g.wireValue())).isEqualTo(g);
        }
        assertThat(PermissionGroup.SELF.wireValue()).isEqualTo("self");
        for (UserStatus s : UserStatus.values()) {
            assertThat(UserStatus.fromWire(s.wireValue())).isEqualTo(s);
        }
        for (UserTokenKind k : UserTokenKind.values()) {
            assertThat(UserTokenKind.fromWire(k.wireValue())).isEqualTo(k);
        }
        assertThatThrownBy(() -> PermissionGroup.fromWire("global")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UserStatus.fromWire("deleted")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UserTokenKind.fromWire("session")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Admin holds the whole catalog")
    void admin() {
        assertThat(BuiltInRole.ADMIN.permissions()).isEqualTo(EnumSet.allOf(Permissions.class));
    }

    @Test
    @DisplayName("Operator holds all Platform and Data, and neither identity permission")
    void operator() {
        assertThat(BuiltInRole.OPERATOR.permissions()).containsAll(Permissions.inGroup(PermissionGroup.PLATFORM))
                .containsAll(Permissions.inGroup(PermissionGroup.DATA))
                .doesNotContain(Permissions.USERS_MANAGE, Permissions.ROLES_MANAGE)
                .hasSize(Permissions.inGroup(PermissionGroup.PLATFORM).size()
                        + Permissions.inGroup(PermissionGroup.DATA).size());
    }

    @Test
    @DisplayName("Editor holds all Self-service and all Data, nothing else")
    void editor() {
        EnumSet<Permissions> expected = EnumSet.copyOf(Permissions.inGroup(PermissionGroup.SELF));
        expected.addAll(Permissions.inGroup(PermissionGroup.DATA));
        assertThat(BuiltInRole.EDITOR.permissions()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Viewer is read-only: data.read, self.views and self.preferences")
    void viewer() {
        assertThat(keys(BuiltInRole.VIEWER.permissions())).containsExactlyInAnyOrder("data.read", "self.views",
                "self.preferences");
    }

    @Test
    @DisplayName("Auditor holds audit.read and data.read only")
    void auditor() {
        assertThat(keys(BuiltInRole.AUDITOR.permissions())).containsExactlyInAnyOrder("audit.read", "data.read");
    }

    @Test
    @DisplayName("built-in ids and names are unique and byId resolves them")
    void builtInIds() {
        assertThat(Arrays.stream(BuiltInRole.values()).map(BuiltInRole::id)).doesNotHaveDuplicates();
        assertThat(Arrays.stream(BuiltInRole.values()).map(BuiltInRole::roleName))
                .containsExactly("Admin", "Operator", "Editor", "Viewer", "Auditor");
        for (BuiltInRole r : BuiltInRole.values()) {
            assertThat(BuiltInRole.byId(r.id())).contains(r);
        }
        assertThat(BuiltInRole.byId(UUID.randomUUID())).isEmpty();
        assertThatThrownBy(() -> BuiltInRole.ADMIN.permissions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("a new user is invited, has no password, and stores a normalized email")
    void newUser() {
        UUID admin = UUID.randomUUID();
        UserEntity user = new UserEntity(UUID.randomUUID(), "  Ada.Lovelace@Example.COM ", "Ada Lovelace",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", admin, T0);
        assertThat(user.getEmail()).isEqualTo("ada.lovelace@example.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.INVITED);
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.isMfaEnabled()).isFalse();
        assertThat(user.getCreatedBy()).isEqualTo(admin);
        assertThat(user.getCreatedAt()).isEqualTo(T0);
        assertThat(user.getUpdatedAt()).isEqualTo(T0);
        assertThat(user.getLastLoginAt()).isNull();
        assertThat(UserEntity.normalizeEmail(null)).isNull();
    }

    @Test
    @DisplayName("user setters move updatedAt; recording a login does not")
    void userChanges() {
        UserEntity user = new UserEntity(UUID.randomUUID(), "a@example.com", "A", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", null,
                T0);
        Instant t1 = T0.plusSeconds(60);
        user.setEmail("B@Example.com", t1);
        user.setFullName("B", t1);
        user.setPasswordHash("$argon2id$v=19$stub", t1);
        user.setStatus(UserStatus.ACTIVE, t1);
        user.setMfaEnabled(true, t1);
        assertThat(user.getEmail()).isEqualTo("b@example.com");
        assertThat(user.getFullName()).isEqualTo("B");
        assertThat(user.getPasswordHash()).isEqualTo("$argon2id$v=19$stub");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isMfaEnabled()).isTrue();
        assertThat(user.getUpdatedAt()).isEqualTo(t1);
        user.recordLogin(T0.plusSeconds(120));
        assertThat(user.getLastLoginAt()).isEqualTo(T0.plusSeconds(120));
        assertThat(user.getUpdatedAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("role setters move updatedAt")
    void roleChanges() {
        RoleEntity role = new RoleEntity(UUID.randomUUID(), "Analyst", null, false, T0);
        Instant t1 = T0.plusSeconds(5);
        role.setName("Senior Analyst", t1);
        role.setDescription("Reads and exports", t1);
        assertThat(role.getName()).isEqualTo("Senior Analyst");
        assertThat(role.getDescription()).isEqualTo("Reads and exports");
        assertThat(role.isBuiltIn()).isFalse();
        assertThat(role.getCreatedAt()).isEqualTo(T0);
        role.touch(T0.plusSeconds(9));
        assertThat(role.getUpdatedAt()).isEqualTo(T0.plusSeconds(9));
    }

    @Test
    @DisplayName("a user token is usable until it expires or is consumed, and consumes once")
    void userToken() {
        UserTokenEntity token = new UserTokenEntity(UUID.randomUUID(), UUID.randomUUID(), UserTokenKind.RESET,
                "a".repeat(64), "fsu_abcd", null, T0, T0.plusSeconds(3600));
        assertThat(token.getKind()).isEqualTo(UserTokenKind.RESET);
        assertThat(token.isUsable(T0)).isTrue();
        assertThat(token.isUsable(T0.plusSeconds(3600))).isFalse();
        token.consume(T0.plusSeconds(10));
        assertThat(token.getConsumedAt()).isEqualTo(T0.plusSeconds(10));
        assertThat(token.isUsable(T0.plusSeconds(11))).isFalse();
        assertThatThrownBy(() -> token.consume(T0.plusSeconds(12))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("link keys compare by value")
    void linkKeys() {
        UUID u = UUID.randomUUID();
        UUID r = UUID.randomUUID();
        assertThat(new UserRoleEntity.Key(u, r)).isEqualTo(new UserRoleEntity.Key(u, r))
                .hasSameHashCodeAs(new UserRoleEntity.Key(u, r)).isNotEqualTo(new UserRoleEntity.Key(r, u));
        assertThat(new RolePermissionEntity.Key(r, "data.read")).isEqualTo(new RolePermissionEntity.Key(r, "data.read"))
                .hasSameHashCodeAs(new RolePermissionEntity.Key(r, "data.read"))
                .isNotEqualTo(new RolePermissionEntity.Key(r, "data.export"));
        UserRoleEntity link = new UserRoleEntity(u, r);
        assertThat(link.getUserId()).isEqualTo(u);
        assertThat(link.getRoleId()).isEqualTo(r);
        RolePermissionEntity grant = new RolePermissionEntity(r, "data.read");
        assertThat(grant.getRoleId()).isEqualTo(r);
        assertThat(grant.getPermissionKey()).isEqualTo("data.read");
    }
}
