package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.test.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/** The credential engine against the database (prompt R2-002): activation, reset, replay, expiry and the hook. */
@IntegrationTest
@RecordApplicationEvents
class UserCredentialsIT {

    @Autowired private UserRepository users;
    @Autowired private UserTokenService tokens;
    @Autowired private UserLifecycleService lifecycle;
    @Autowired private PrincipalService principals;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ApplicationEvents events;

    private UserEntity invited(String email) {
        return users.saveAndFlush(new UserEntity(UUID.randomUUID(), email, "Test User",
                principals.createFor(email, PrincipalKind.PERSON).getId(), null, Instant.now()));
    }

    @Test
    @DisplayName("activation consumes once, sets an argon2id hash, activates, and fires the hook; replay fails")
    void activation() {
        UserEntity user = invited("ivy@example.com");
        IssuedUserToken issued = tokens.issueActivation(user, null, null);
        lifecycle.activateWithToken(issued.plaintext(), "a strong first password");
        UserEntity read = users.findById(user.getId()).orElseThrow();
        assertThat(read.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(read.getPasswordHash()).startsWith("$argon2id$");
        assertThat(lifecycle.verify(read, "a strong first password")).isTrue();
        assertThat(events.stream(UserSessionsRevokedEvent.class)).containsExactly(new UserSessionsRevokedEvent(
                user.getId(), UserSessionsRevokedEvent.Reason.PASSWORD_CHANGED));
        assertThatThrownBy(() -> lifecycle.activateWithToken(issued.plaintext(), "a strong first password"))
                .isInstanceOf(ValidationException.class);
        assertThat(jdbc.queryForObject("select count(*) from user_token where token_hash = ? and consumed_at is not "
                + "null", Long.class, UserTokenService.sha256(issued.plaintext()))).isEqualTo(1L);
    }

    @Test
    @DisplayName("an expired reset token is refused; a re-request invalidates the earlier link")
    void resetExpiryAndReplacement() {
        UserEntity user = invited("jo@example.com");
        lifecycle.activateWithToken(tokens.issueActivation(user, null, null).plaintext(), "first password ok");
        IssuedUserToken first = tokens.requestReset("JO@example.com").orElseThrow();
        jdbc.update("update user_token set expires_at = now() - interval '1 second' where id = ?", first.token().getId());
        assertThatThrownBy(() -> lifecycle.resetWithToken(first.plaintext(), "second password ok"))
                .isInstanceOf(ValidationException.class);

        IssuedUserToken a = tokens.requestReset("jo@example.com").orElseThrow();
        IssuedUserToken b = tokens.requestReset("jo@example.com").orElseThrow();
        assertThatThrownBy(() -> lifecycle.resetWithToken(a.plaintext(), "second password ok"))
                .isInstanceOf(ValidationException.class);
        lifecycle.resetWithToken(b.plaintext(), "second password ok");
        assertThat(lifecycle.verify(users.findById(user.getId()).orElseThrow(), "second password ok")).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from user_token where user_id = ? and kind = 'reset' and "
                + "consumed_at is null", Long.class, user.getId())).isZero();
    }

    @Test
    @DisplayName("a suspended or disabled user cannot authenticate, and the hook fires for each")
    void suspendDisable() {
        UserEntity user = invited("kim@example.com");
        lifecycle.activateWithToken(tokens.issueActivation(user, null, null).plaintext(), "kims password 1");
        UserEntity active = users.findById(user.getId()).orElseThrow();
        lifecycle.suspend(active);
        assertThat(lifecycle.verify(users.findById(user.getId()).orElseThrow(), "kims password 1")).isFalse();
        lifecycle.disable(users.findById(user.getId()).orElseThrow());
        assertThat(users.findById(user.getId()).orElseThrow().getStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(events.stream(UserSessionsRevokedEvent.class).map(UserSessionsRevokedEvent::reason))
                .containsExactly(UserSessionsRevokedEvent.Reason.PASSWORD_CHANGED,
                        UserSessionsRevokedEvent.Reason.SUSPENDED, UserSessionsRevokedEvent.Reason.DISABLED);
    }
}
