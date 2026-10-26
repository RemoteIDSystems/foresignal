package ai.foresignal.view;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.EffectivePermissions;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SavedViewScope} (prompt R2-012): who sees and manages which saved view. */
class SavedViewScopeTest {

    private static AuthenticatedSession session(String principal, String... permissions) {
        Instant t = Instant.parse("2026-10-26T12:00:00Z");
        return new AuthenticatedSession(UUID.randomUUID(), UUID.randomUUID(), principal + "@example.com", principal,
                "fss_test", t.plusSeconds(3600), t.plusSeconds(600), EffectivePermissions.of(List.of(permissions)));
    }

    @Test
    @DisplayName("a user sees their own and the shared views and manages only their own")
    void user() {
        SavedViewScope scope = SavedViewScope.of(session("alice", "self.views"));
        assertThat(scope.all()).isFalse();
        assertThat(scope.sees("alice")).isTrue();
        assertThat(scope.sees(null)).isTrue();
        assertThat(scope.sees("bob")).isFalse();
        assertThat(scope.manages("alice")).isTrue();
        assertThat(scope.manages(null)).isFalse();
        assertThat(scope.manages("bob")).isFalse();
        assertThat(scope.ownerForNew()).isEqualTo("alice");
    }

    @Test
    @DisplayName("users.manage sees and manages every view and still owns what it creates")
    void admin() {
        SavedViewScope scope = SavedViewScope.of(session("root", "users.manage", "self.views"));
        assertThat(scope.all()).isTrue();
        assertThat(scope.sees("bob")).isTrue();
        assertThat(scope.manages(null)).isTrue();
        assertThat(scope.ownerForNew()).isEqualTo("root");
    }

    @Test
    @DisplayName("an API token manages everything and creates shared views; others see shared views only")
    void machineAndAnonymous() {
        assertThat(SavedViewScope.MACHINE.sees("bob")).isTrue();
        assertThat(SavedViewScope.MACHINE.manages(null)).isTrue();
        assertThat(SavedViewScope.MACHINE.ownerForNew()).isNull();
        assertThat(SavedViewScope.of((jakarta.servlet.http.HttpServletRequest) null)).isEqualTo(SavedViewScope.MACHINE);
        assertThat(SavedViewScope.SHARED_ONLY.sees(null)).isTrue();
        assertThat(SavedViewScope.SHARED_ONLY.sees("bob")).isFalse();
        assertThat(SavedViewScope.SHARED_ONLY.manages(null)).isFalse();
    }

    @Test
    @DisplayName("the shared-view refusal is 403 naming users.manage")
    void refusal() {
        ForesignalException e = SavedViewScope.sharedDenied();
        assertThat(e.getStatus().value()).isEqualTo(403);
        assertThat(e.getCode()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(e.getProperties()).containsEntry("requiredPermission", "users.manage");
    }
}
