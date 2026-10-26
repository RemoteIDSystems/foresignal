package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EffectivePermissions} and the session's token view (prompt R2-003). */
class EffectivePermissionsTest {

    private static EffectivePermissions of(BuiltInRole... roles) {
        return EffectivePermissions.of(java.util.Arrays.stream(roles).flatMap(r -> r.permissions().stream())
                .map(Permissions::key).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("the derived scope of each built-in role follows the documented mapping")
    void builtInScopes() {
        assertThat(of(BuiltInRole.ADMIN).scope()).isEqualTo(Scope.ADMIN);
        assertThat(of(BuiltInRole.OPERATOR).scope()).isEqualTo(Scope.ADMIN);
        assertThat(of(BuiltInRole.EDITOR).scope()).isEqualTo(Scope.AGENT);
        assertThat(of(BuiltInRole.VIEWER).scope()).isEqualTo(Scope.READ);
        assertThat(of(BuiltInRole.AUDITOR).scope()).isEqualTo(Scope.READ);
        assertThat(EffectivePermissions.NONE.scope()).isEqualTo(Scope.READ);
    }

    @Test
    @DisplayName("one identity or platform permission makes admin, except audit.read; data.export makes agent")
    void singlePermissions() {
        assertThat(EffectivePermissions.of(List.of("users.manage")).scope()).isEqualTo(Scope.ADMIN);
        assertThat(EffectivePermissions.of(List.of("storage.manage")).scope()).isEqualTo(Scope.ADMIN);
        assertThat(EffectivePermissions.of(List.of("audit.read")).scope()).isEqualTo(Scope.READ);
        assertThat(EffectivePermissions.of(List.of("data.export")).scope()).isEqualTo(Scope.AGENT);
        assertThat(EffectivePermissions.of(List.of("self.alerts")).scope()).isEqualTo(Scope.AGENT);
        assertThat(EffectivePermissions.of(List.of("self.views", "self.preferences", "data.read")).scope())
                .isEqualTo(Scope.READ);
        assertThat(EffectivePermissions.of(List.of("not.a.permission")).scope()).isEqualTo(Scope.READ);
    }

    @Test
    @DisplayName("the union of two roles holds both sets, sorted")
    void union() {
        EffectivePermissions p = of(BuiltInRole.VIEWER, BuiltInRole.AUDITOR);
        assertThat(p.keys()).containsExactly("audit.read", "data.read", "self.preferences", "self.views");
        assertThat(p.has(Permissions.AUDIT_READ)).isTrue();
        assertThat(p.has(Permissions.DATA_EXPORT)).isFalse();
        assertThat(p.asList()).isSorted();
    }

    @Test
    @DisplayName("a session's token view: session id, session: name, derived scope, private grant only when admin")
    void asToken() {
        UUID id = UUID.randomUUID();
        AuthenticatedSession viewer = new AuthenticatedSession(id, UUID.randomUUID(), "v@example.com",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "fss_abcd", null, null, of(BuiltInRole.VIEWER));
        assertThat(viewer.asToken().id()).isEqualTo(id);
        assertThat(viewer.asToken().name()).isEqualTo("session:v@example.com");
        assertThat(viewer.asToken().scope()).isEqualTo(Scope.READ);
        assertThat(viewer.asToken().privateAccess()).isFalse();
        assertThat(viewer.asToken().bootstrap()).isFalse();
        assertThat(viewer.asToken().principalId()).isEqualTo("01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        AuthenticatedSession admin = new AuthenticatedSession(id, UUID.randomUUID(), "a@example.com", "P", "fss_abcd",
                null, null, of(BuiltInRole.ADMIN));
        assertThat(admin.asToken().scope()).isEqualTo(Scope.ADMIN);
        assertThat(admin.asToken().privateAccess()).isTrue();
        assertThat(Set.copyOf(admin.permissions().keys())).hasSize(Permissions.values().length);
    }
}
