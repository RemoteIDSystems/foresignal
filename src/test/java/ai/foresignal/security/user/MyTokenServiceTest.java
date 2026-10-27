package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

/** Self-service tokens (prompt R2-025): owned by the caller's principal, marked {@code self}, no wider than the user. */
class MyTokenServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final TokenService tokens = mock(TokenService.class);
    private final MyTokenService service = new MyTokenService(tokens);

    private static EffectivePermissions of(BuiltInRole role) {
        return EffectivePermissions.of(role.permissions().stream().map(Permissions::key)
                .collect(Collectors.toSet()));
    }

    private static AuthenticatedSession session(BuiltInRole role) {
        Instant now = Instant.now();
        return new AuthenticatedSession(UUID.randomUUID(), UUID.randomUUID(), "u@example.com", "01PRINCIPAL0000000000000000",
                "fss_abcd", now.plusSeconds(3600), now.plusSeconds(600), of(role));
    }

    private void parses(Scope scope, boolean privateAccess) {
        when(tokens.parse(any(), eq(TokenCreatedVia.SELF))).thenReturn(new TokenService.TokenSpec("t", scope, null,
                TokenRestrictions.NONE, null, TokenCreatedVia.SELF, null, privateAccess, List.of()));
    }

    @Test
    @DisplayName("a minted token is owned by the caller's principal and marked self")
    void ownedByCaller() {
        AuthenticatedSession viewer = session(BuiltInRole.VIEWER);
        parses(Scope.READ, false);

        service.create(viewer, JSON.readTree("{\"name\":\"t\",\"scope\":\"read\"}"));

        ArgumentCaptor<TokenService.TokenSpec> spec = ArgumentCaptor.forClass(TokenService.TokenSpec.class);
        verify(tokens).create(spec.capture());
        assertThat(spec.getValue().principalId()).isEqualTo(viewer.principalId());
        assertThat(spec.getValue().createdVia()).isEqualTo(TokenCreatedVia.SELF);
        assertThat(TokenCreatedVia.SELF.wireValue()).isEqualTo("self");
    }

    @Test
    @DisplayName("a scope or private grant beyond the caller's is refused; a principalId is refused")
    void clamped() {
        AuthenticatedSession viewer = session(BuiltInRole.VIEWER);
        parses(Scope.AGENT, false);
        assertThatThrownBy(() -> service.create(viewer, JSON.readTree("{}")))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo("TOKEN_EXCEEDS_USER"));
        parses(Scope.READ, true);
        assertThatThrownBy(() -> service.create(viewer, JSON.readTree("{}")))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo("TOKEN_EXCEEDS_USER"));
        assertThatThrownBy(() -> service.create(viewer, JSON.readTree("{\"principalId\":\"someone\"}")))
                .isInstanceOf(ValidationException.class);
        parses(Scope.ADMIN, false);
        assertThatThrownBy(() -> service.create(session(BuiltInRole.OPERATOR), JSON.readTree("{}")))
                .as("an Operator lacks the identity permissions an admin token would pass")
                .isInstanceOf(ForesignalException.class);
        verify(tokens, never()).create(any(TokenService.TokenSpec.class));
    }

    @Test
    @DisplayName("the widest scope follows the roles: admin only with every permission")
    void maxScope() {
        assertThat(TokenOwnership.maxScope(of(BuiltInRole.ADMIN))).isEqualTo(Scope.ADMIN);
        assertThat(TokenOwnership.maxScope(of(BuiltInRole.OPERATOR))).isEqualTo(Scope.AGENT);
        assertThat(TokenOwnership.maxScope(of(BuiltInRole.EDITOR))).isEqualTo(Scope.AGENT);
        assertThat(TokenOwnership.maxScope(of(BuiltInRole.VIEWER))).isEqualTo(Scope.READ);
        assertThat(TokenOwnership.maxScope(of(BuiltInRole.AUDITOR))).isEqualTo(Scope.READ);
        assertThat(TokenOwnership.maxScope(EffectivePermissions.NONE)).isEqualTo(Scope.READ);
        assertThat(TokenOwnership.mayGrantPrivate(of(BuiltInRole.OPERATOR))).isTrue();
        assertThat(TokenOwnership.mayGrantPrivate(of(BuiltInRole.EDITOR))).isFalse();
    }
}
