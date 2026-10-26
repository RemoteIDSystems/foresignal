package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** {@link UserLifecycleService}: the status machine, password setting and the session-revocation hook (R2-002). */
class UserLifecycleServiceTest {

    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    private UserRepository users;
    private ApplicationEventPublisher events;
    private UserTokenService tokens;
    private UserLifecycleService service;
    private final PasswordHasher hasher = new PasswordHasher();

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        events = mock(ApplicationEventPublisher.class);
        tokens = mock(UserTokenService.class);
        service = new UserLifecycleService(users, hasher, new PasswordPolicy(), events, tokens, new MutableClock(T0));
    }

    private static UserEntity user(UserStatus status) {
        UserEntity user = new UserEntity(UUID.randomUUID(), "u@example.com", "U", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", null,
                T0);
        user.setStatus(status, T0);
        return user;
    }

    @Test
    @DisplayName("the transition table is exactly the documented one")
    void table() {
        for (UserStatus from : UserStatus.values()) {
            for (UserStatus to : UserStatus.values()) {
                boolean expected = (from == UserStatus.INVITED && to == UserStatus.ACTIVE)
                        || (from == UserStatus.ACTIVE && to == UserStatus.SUSPENDED)
                        || (from == UserStatus.SUSPENDED && to == UserStatus.ACTIVE)
                        || (from != UserStatus.DISABLED && to == UserStatus.DISABLED);
                assertThat(UserLifecycleService.isLegal(from, to)).as(from + " -> " + to).isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("only an active user may authenticate")
    void onlyActiveAuthenticates() {
        for (UserStatus s : UserStatus.values()) {
            assertThat(UserLifecycleService.canAuthenticate(user(s))).isEqualTo(s == UserStatus.ACTIVE);
        }
        assertThat(UserLifecycleService.canAuthenticate(null)).isFalse();
    }

    @Test
    @DisplayName("activate moves invited to active and refuses any other status")
    void activate() {
        UserEntity u = user(UserStatus.INVITED);
        service.activate(u);
        assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThatThrownBy(() -> service.activate(u)).isInstanceOfSatisfying(ConflictException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.USER_STATUS_CONFLICT));
    }

    @Test
    @DisplayName("suspend and disable block authentication and revoke sessions; unsuspend restores")
    void suspendAndDisable() {
        UserEntity u = user(UserStatus.ACTIVE);
        u.setPasswordHash(hasher.hash("a fine password"), T0);
        assertThat(service.verify(u, "a fine password")).isTrue();

        service.suspend(u);
        assertThat(u.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(service.verify(u, "a fine password")).isFalse();
        verify(events).publishEvent(new UserSessionsRevokedEvent(u.getId(), UserSessionsRevokedEvent.Reason.SUSPENDED));
        assertThatThrownBy(() -> service.suspend(u)).isInstanceOf(ConflictException.class);

        service.unsuspend(u);
        assertThat(service.verify(u, "a fine password")).isTrue();
        assertThatThrownBy(() -> service.unsuspend(u)).isInstanceOf(ConflictException.class);

        service.disable(u);
        assertThat(u.getStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(service.verify(u, "a fine password")).isFalse();
        verify(events).publishEvent(new UserSessionsRevokedEvent(u.getId(), UserSessionsRevokedEvent.Reason.DISABLED));
        assertThatThrownBy(() -> service.disable(u)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.unsuspend(u)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("setting a password stores an argon2id hash and fires the session-revocation hook")
    void setPassword() {
        UserEntity u = user(UserStatus.ACTIVE);
        service.setPassword(u, "brand new password", "password");
        assertThat(u.getPasswordHash()).startsWith("$argon2id$");
        assertThat(hasher.matches("brand new password", u.getPasswordHash())).isTrue();
        verify(events).publishEvent(new UserSessionsRevokedEvent(u.getId(),
                UserSessionsRevokedEvent.Reason.PASSWORD_CHANGED));
    }

    @Test
    @DisplayName("a refused password changes nothing and fires nothing")
    void refusedPassword() {
        UserEntity u = user(UserStatus.ACTIVE);
        assertThatThrownBy(() -> service.setPassword(u, "short", "password")).isInstanceOf(ValidationException.class);
        assertThat(u.getPasswordHash()).isNull();
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("activation with a token sets the password and activates; the policy is checked before the token")
    void activateWithToken() {
        UserEntity u = user(UserStatus.INVITED);
        when(tokens.consume("fsv_token", UserTokenKind.ACTIVATION)).thenReturn(u);
        assertThatThrownBy(() -> service.activateWithToken("fsv_token", "short")).isInstanceOf(
                ValidationException.class);
        verify(tokens, never()).consume(any(), any());

        assertThat(service.activateWithToken("fsv_token", "my first password")).isSameAs(u);
        assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(hasher.matches("my first password", u.getPasswordHash())).isTrue();
        verify(events).publishEvent(new UserSessionsRevokedEvent(u.getId(),
                UserSessionsRevokedEvent.Reason.PASSWORD_CHANGED));
    }

    @Test
    @DisplayName("activation of an account that is no longer invited is refused")
    void activateNotInvited() {
        UserEntity u = user(UserStatus.DISABLED);
        when(tokens.consume("fsv_token", UserTokenKind.ACTIVATION)).thenReturn(u);
        assertThatThrownBy(() -> service.activateWithToken("fsv_token", "my first password"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("reset with a token sets the password for an active user and refuses a suspended one like a bad link")
    void resetWithToken() {
        UserEntity active = user(UserStatus.ACTIVE);
        when(tokens.consume("fsw_a", UserTokenKind.RESET)).thenReturn(active);
        service.resetWithToken("fsw_a", "my new password!");
        assertThat(hasher.matches("my new password!", active.getPasswordHash())).isTrue();

        UserEntity suspended = user(UserStatus.SUSPENDED);
        when(tokens.consume("fsw_s", UserTokenKind.RESET)).thenReturn(suspended);
        assertThatThrownBy(() -> service.resetWithToken("fsw_s", "my new password!"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.USER_TOKEN_INVALID));
        assertThat(suspended.getPasswordHash()).isNull();
    }

    @Test
    @DisplayName("verify is false for no user, no password, or a wrong password")
    void verifyFalse() {
        UserEntity u = user(UserStatus.ACTIVE);
        assertThat(service.verify(null, "whatever password")).isFalse();
        assertThat(service.verify(u, "whatever password")).isFalse();
        u.setPasswordHash(hasher.hash("the right password"), T0);
        assertThat(service.verify(u, "the wrong password")).isFalse();
    }
}
